package utils;

import com.example.CCLEvaluation.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.io.IOException;
import java.util.ArrayList;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LlmPlanService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String TAG = "LlmPlanService";
    private static final double DEFAULT_TEMPERATURE = 0.2;
    private static final double REWRITE_TEMPERATURE = 0.0;
    private static final int MAX_REWRITE_ATTEMPTS = 2;
    private static final int LOG_CONTENT_LIMIT = 800;
    private static final double ENGLISH_RATIO_THRESHOLD = 0.30;
    private static final int MIN_ENGLISH_SENTENCES = 2;
    private static final String[] ENGLISH_ABBREVIATION_WHITELIST = {
            "PST", "PN", "NWR", "SLP", "ASD", "ADHD", "SSD", "DLD",
            "SMART", "AI", "PDF", "API", "JSON", "OKR", "IPA"
    };
    private static final String ENGLISH_WARNING =
            "\u7cfb\u7edf\u63d0\u793a\uff1a\u5185\u5bb9\u5305\u542b\u8f83\u591a\u82f1\u6587\u672f\u8bed\uff0c\u5df2\u4fdd\u7559\u539f\u59cb\u8f93\u51fa\uff0c\u8bf7\u4eba\u5de5\u786e\u8ba4\u4e2d\u6587\u5316\u3002";
    private static final AtomicInteger REQUEST_COUNTER = new AtomicInteger(0);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final Map<Integer, Runnable> PENDING_WATCHDOGS = new ConcurrentHashMap<>();
    private final OkHttpClient client;

    public interface PlanCallback {
        void onSuccess(JSONObject plan);

        void onError(String errorMessage);
    }

    public LlmPlanService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(180, TimeUnit.SECONDS)
                .eventListenerFactory(call -> new LoggingEventListener())
                .build();
    }

    public void generateTreatmentPlan(String systemPrompt, String userPrompt, PlanCallback callback) {
        generateTreatmentPlanInternal(systemPrompt, userPrompt, callback, 0, DEFAULT_TEMPERATURE);
    }

    /**
     * Concurrently generate the 6 prompts that align with TreatmentPromptBuilder#buildConcurrentPrompts.
     */
    public void generateTreatmentPlanConcurrent(Map<String, String> promptMap, PlanCallback callback) {
        if (promptMap == null || promptMap.isEmpty()) {
            if (callback != null) {
                callback.onError("Prompt\u751f\u6210\u5931\u8d25\uff1a\u65e0\u4efb\u52a1");
            }
            return;
        }

        final JSONObject finalResult = new JSONObject();
        try {
            finalResult.put("module_plan", new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final int totalTasks = promptMap.size();
        final AtomicInteger completedCount = new AtomicInteger(0);
        final long startTime = System.currentTimeMillis();
        Log.i(TAG, "\u5f00\u59cb\u5e76\u53d1\u751f\u6210\uff0c\u603b\u4efb\u52a1\u6570: " + totalTasks);

        for (Map.Entry<String, String> entry : promptMap.entrySet()) {
            String taskName = entry.getKey();
            String userPrompt = entry.getValue();
            String systemPrompt = "\u4f60\u662f\u4e13\u4e1a\u7684\u8a00\u8bed\u6cbb\u7597\u5e08\u52a9\u624b\u3002\u8bf7\u4e25\u683c\u8f93\u51fa\u5408\u6cd5JSON\uff0c\u4e0d\u8981Markdown\u3002";

            generateTreatmentPlanInternal(systemPrompt, userPrompt, new PlanCallback() {
                @Override
                public void onSuccess(JSONObject partialPlan) {
                    Log.i(TAG, "\u4efb\u52a1\u5b8c\u6210: " + taskName);
                    synchronized (finalResult) {
                        mergeJson(finalResult, partialPlan);
                    }
                    checkAndCallback(totalTasks, completedCount, finalResult, callback, startTime);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "\u4efb\u52a1\u5931\u8d25: " + taskName + " | " + errorMessage);
                    // even if failed, still increment to avoid hanging
                    checkAndCallback(totalTasks, completedCount, finalResult, callback, startTime);
                }
            }, 0, DEFAULT_TEMPERATURE);
        }
    }

    private void generateTreatmentPlanInternal(String systemPrompt, String userPrompt, PlanCallback callback, int retryCount, double temperature) {
        if (callback == null) {
            return;
        }
        int requestId = REQUEST_COUNTER.incrementAndGet();
        long startMs = SystemClock.elapsedRealtime();
        Log.i(TAG, "request#" + requestId + " start retry=" + retryCount
                + " temp=" + temperature
                + " sysLen=" + safeLength(systemPrompt)
                + " userLen=" + safeLength(userPrompt));
        String apiKey = BuildConfig.DEEPSEEK_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("DEEPSEEK_API_KEY is empty. Configure in local.properties.");
            return;
        }
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", "deepseek-chat");
            payload.put("temperature", temperature);
            payload.put("stream", false);
            payload.put("response_format", new JSONObject().put("type", "json_object"));
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            String enhancedUserPrompt = userPrompt;
            messages.put(new JSONObject().put("role", "user").put("content", enhancedUserPrompt));
            payload.put("messages", messages);

            String payloadText = payload.toString();
            Log.i(TAG, "request#" + requestId + " payloadLen=" + payloadText.length());
            RequestBody body = RequestBody.create(payloadText, JSON);
            Request request = new Request.Builder()
                    .url("https://api.deepseek.com/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .tag(Integer.class, requestId)
                    .post(body)
                    .build();

            Call call = client.newCall(request);
            scheduleWatchdog(requestId, call);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    cancelWatchdog(requestId);
                    long elapsedMs = SystemClock.elapsedRealtime() - startMs;
                    Log.w(TAG, "request#" + requestId + " failure after " + elapsedMs + "ms: "
                            + e.getClass().getSimpleName() + " " + e.getMessage());
                    if (retryCount < 1) {
                        Log.w(TAG, "request#" + requestId + " retrying network error, attempt " + (retryCount + 1));
                        generateTreatmentPlanInternal(systemPrompt, userPrompt, callback, retryCount + 1, temperature);
                        return;
                    }
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    cancelWatchdog(requestId);
                    String bodyString;
                    try {
                        bodyString = response.body() != null ? response.body().string() : "";
                    } catch (IOException ioException) {
                        long elapsedMs = SystemClock.elapsedRealtime() - startMs;
                        Log.w(TAG, "request#" + requestId + " read body failed after " + elapsedMs + "ms: "
                                + ioException.getClass().getSimpleName() + " " + ioException.getMessage());
                        if (retryCount < 1) {
                            Log.w(TAG, "request#" + requestId + " retrying read failure, attempt " + (retryCount + 1));
                            generateTreatmentPlanInternal(systemPrompt, userPrompt, callback, retryCount + 1, temperature);
                            return;
                        }
                        callback.onError("Read error: " + ioException.getMessage());
                        return;
                    }
                    long elapsedMs = SystemClock.elapsedRealtime() - startMs;
                    Log.i(TAG, "request#" + requestId + " http=" + response.code()
                            + " in " + elapsedMs + "ms"
                            + " bodyLen=" + bodyString.length());
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP " + response.code() + ": " + truncateContent(bodyString, 100));
                        return;
                    }
                    try {
                        JSONObject root = new JSONObject(bodyString);
                        JSONArray choices = root.optJSONArray("choices");
                        if (choices == null || choices.length() == 0) {
                            callback.onError("Empty choices in response.");
                            return;
                        }
                        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
                        if (message == null) {
                            callback.onError("Missing message in response.");
                            return;
                        }
                        String content = message.optString("content", "").trim();
                        if (content.isEmpty()) {
                            Log.w(TAG, "request#" + requestId + " empty content after " + elapsedMs + "ms");
                            callback.onError("Empty content in response.");
                            return;
                        }
                        JSONObject plan = new JSONObject(content);
                        boolean allowRewrite = shouldAttemptRewrite(systemPrompt, userPrompt);
                        boolean shouldRewrite = allowRewrite && shouldRewriteToChinese(plan);
                        if (retryCount == 0) {
                            logContent(content, shouldRewrite, retryCount, false);
                        }
                        if (shouldRewrite && retryCount < MAX_REWRITE_ATTEMPTS) {
                            String rewriteSystemPrompt = "\u4f60\u662fJSON\u4e34\u5e8a\u6587\u6848\u6539\u5199\u5668\u3002"
                                    + "\u4fdd\u6301\u6240\u6709key\u3001\u7ed3\u6784\u3001\u6570\u7ec4\u957f\u5ea6\u548c\u7c7b\u578b\u5b8c\u5168\u4e0d\u53d8\u3002"
                                    + "\u5c06\u6240\u6709value\u6539\u5199\u4e3a\u7b80\u4f53\u4e2d\u6587\u4e34\u5e8a\u8868\u8ff0\u3002"
                                    + "\u5fc5\u987b\u4f7f\u7528\u4e2d\u6587\uff0c\u4e0d\u5141\u8bb8\u8f93\u51fa\u82f1\u6587\u53e5\u5b50\uff08\u5141\u8bb8\u7684\u7f29\u5199\uff1aPST\u3001PN\u3001NWR\u3001SLP\u3001ASD\u3001ADHD\u3001SSD\u3001DLD\uff09\u3002"
                                    + "\u53ea\u8f93\u51fa\u4e25\u683c\u5408\u6cd5JSON\uff0c\u4e0d\u8981\u8f93\u51fa\u4efb\u4f55\u5176\u4ed6\u6587\u5b57\u3002";
                            String rewriteUserPrompt = buildRewriteUserPrompt(plan);
                            generateTreatmentPlanInternal(rewriteSystemPrompt, rewriteUserPrompt, new PlanCallback() {
                                @Override
                                public void onSuccess(JSONObject rewritten) {
                                    attachOptionalWarningIfNeeded(rewritten);
                                    callback.onSuccess(rewritten);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    attachOptionalWarningIfNeeded(plan);
                                    callback.onSuccess(plan);
                                }
                            }, retryCount + 1, REWRITE_TEMPERATURE);
                            return;
                        }
                        if (retryCount > 0) {
                            logContent(content, shouldRewrite, retryCount, true);
                        }
                        attachOptionalWarningIfNeeded(plan);
                        ensureModuleGuides(plan);
                        callback.onSuccess(plan);
                    } catch (JSONException e) {
                        Log.w(TAG, "request#" + requestId + " parse error after " + elapsedMs + "ms: " + e.getMessage());
                        // 返回前50个字符以便排查是否为HTML
                        String preview = truncateContent(bodyString, 50);
                        callback.onError("非JSON响应(" + preview + "): " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("Request build error: " + e.getMessage());
        }
    }

    private void collectAllStringValues(Object node, List<String> out) {
        if (node == null || JSONObject.NULL.equals(node)) {
            return;
        }
        if (node instanceof JSONObject) {
            JSONObject obj = (JSONObject) node;
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                collectAllStringValues(obj.opt(key), out);
            }
            return;
        }
        if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            for (int i = 0; i < array.length(); i++) {
                collectAllStringValues(array.opt(i), out);
            }
            return;
        }
        if (node instanceof String) {
            String value = ((String) node).trim();
            if (!value.isEmpty()) {
                out.add(value);
            }
        }
    }

    private boolean shouldRewriteToChinese(JSONObject plan) {
        if (plan == null) {
            return false;
        }
        List<String> values = new ArrayList<>();
        collectAllStringValues(plan, values);
        EnglishStats stats = countEnglishStats(values);
        double ratio = stats.totalChars == 0 ? 0.0 : (double) stats.englishLetters / stats.totalChars;
        if (ratio <= ENGLISH_RATIO_THRESHOLD) {
            return false;
        }
        return countEnglishSentences(values) >= MIN_ENGLISH_SENTENCES;
    }

    private void attachOptionalWarningIfNeeded(JSONObject plan) {
        if (plan == null || !shouldRewriteToChinese(plan)) {
            return;
        }
        try {
            JSONArray notes = plan.optJSONArray("notes_for_therapist");
            if (notes == null) {
                notes = new JSONArray();
                plan.put("notes_for_therapist", notes);
            }
            if (!containsNote(notes, ENGLISH_WARNING)) {
                notes.put(ENGLISH_WARNING);
            }
        } catch (JSONException ignored) {
            // Ignore note attachment errors to avoid blocking output.
        }
    }

    private boolean containsNote(JSONArray notes, String note) {
        for (int i = 0; i < notes.length(); i++) {
            if (note.equals(notes.optString(i))) {
                return true;
            }
        }
        return false;
    }

    private EnglishStats countEnglishStats(List<String> values) {
        EnglishStats stats = new EnglishStats();
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }
            String normalized = stripWhitelistedAbbreviations(value);
            for (int i = 0; i < normalized.length(); i++) {
                char c = normalized.charAt(i);
                if (Character.isWhitespace(c)) {
                    continue;
                }
                stats.totalChars++;
                if (isEnglishLetter(c)) {
                    stats.englishLetters++;
                }
            }
        }
        return stats;
    }

    private int countEnglishSentences(List<String> values) {
        int count = 0;
        if (values == null) {
            return count;
        }
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            String normalized = stripWhitelistedAbbreviations(value);
            if (normalized.isEmpty()) {
                continue;
            }
            String[] sentences = normalized.split("(?<=[.!?])");
            for (String sentence : sentences) {
                if (isEnglishSentence(sentence)) {
                    count++;
                    if (count >= MIN_ENGLISH_SENTENCES) {
                        return count;
                    }
                }
            }
        }
        return count;
    }

    private boolean isEnglishSentence(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (containsChinese(trimmed)) {
            return false;
        }
        char last = trimmed.charAt(trimmed.length() - 1);
        if (last != '.' && last != '!' && last != '?') {
            return false;
        }
        return countEnglishWords(trimmed) >= 3;
    }

    private int countEnglishWords(String text) {
        int count = 0;
        boolean inWord = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isEnglishLetter(c)) {
                if (!inWord) {
                    count++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        return count;
    }

    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }

    private void scheduleWatchdog(final int requestId, final Call call) {
        Runnable task = () -> {
            Log.w(TAG, "request#" + requestId + " no response after 185s, canceling call");
            try {
                call.cancel();
            } catch (Exception ignored) {
            }
        };
        PENDING_WATCHDOGS.put(requestId, task);
        MAIN_HANDLER.postDelayed(task, 185_000);
    }

    private void cancelWatchdog(int requestId) {
        Runnable task = PENDING_WATCHDOGS.remove(requestId);
        if (task != null) {
            MAIN_HANDLER.removeCallbacks(task);
        }
    }

    private String buildRewriteUserPrompt(JSONObject plan) {
        return "\u8bf7\u4e0d\u6539\u53d8\u4efb\u4f55 key/\u7ed3\u6784/\u6570\u7ec4\u957f\u5ea6/\u7c7b\u578b\uff0c\u5c06\u4ee5\u4e0b JSON \u7684\u6240\u6709 value \u6539\u5199\u4e3a\u7b80\u4f53\u4e2d\u6587\u4e34\u5e8a\u8868\u8ff0\uff0c\u53ea\u8f93\u51fa\u4e25\u683c\u5408\u6cd5 JSON\uff0c\u4e0d\u8981\u8f93\u51fa\u5176\u4ed6\u6587\u5b57\uff1a\n"
                + plan.toString();
    }

    private boolean shouldAttemptRewrite(String systemPrompt, String userPrompt) {
        return !expectsChineseOutput(systemPrompt, userPrompt);
    }

    private boolean expectsChineseOutput(String systemPrompt, String userPrompt) {
        return containsChineseHint(systemPrompt) || containsChineseHint(userPrompt);
    }

    private boolean containsChineseHint(String text) {
        if (text == null) {
            return false;
        }
        return text.contains("\u7b80\u4f53\u4e2d\u6587") || text.contains("\u4e2d\u6587");
    }

    private String stripWhitelistedAbbreviations(String text) {
        String normalized = text;
        for (String abbr : ENGLISH_ABBREVIATION_WHITELIST) {
            normalized = normalized.replaceAll("(?i)\\b" + Pattern.quote(abbr) + "\\b", "");
        }
        return normalized;
    }

    private int countEnglishLetters(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (isEnglishLetter(text.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private boolean containsChinese(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u4E00' && c <= '\u9FFF') {
                return true;
            }
        }
        return false;
    }

    private boolean isEnglishLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private void logContent(String content, boolean shouldRewrite, int retryCount, boolean isRewrite) {
        String truncated = truncateContent(content, LOG_CONTENT_LIMIT);
        String label = isRewrite ? "REWRITE " : "";
        Log.i(TAG, label + "content=" + truncated + " | shouldRewrite=" + shouldRewrite + " | retryCount=" + retryCount);
    }

    private String truncateContent(String content, int maxChars) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxChars) {
            return content;
        }
        return content.substring(0, maxChars);
    }

    private void checkAndCallback(int total, AtomicInteger current, JSONObject result, PlanCallback callback, long startMs) {
        if (current.incrementAndGet() == total) {
            long duration = System.currentTimeMillis() - startMs;
            Log.i(TAG, "\u6240\u6709\u5e76\u53d1\u4efb\u52a1\u7ed3\u675f\uff0c\u603b\u8017\u65f6: " + duration + "ms");
            ensureModuleGuides(result);
            if (callback != null) {
                callback.onSuccess(result);
            }
        }
    }

    private void mergeJson(JSONObject target, JSONObject source) {
        if (target == null || source == null) {
            return;
        }
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = source.get(key);
                if ("module_plan".equals(key) && value instanceof JSONObject) {
                    JSONObject targetModules = target.optJSONObject("module_plan");
                    if (targetModules == null) {
                        targetModules = new JSONObject();
                        target.put("module_plan", targetModules);
                    }
                    JSONObject sourceModules = (JSONObject) value;
                    Iterator<String> modKeys = sourceModules.keys();
                    while (modKeys.hasNext()) {
                        String modKey = modKeys.next();
                        targetModules.put(modKey, sourceModules.get(modKey));
                    }
                } else {
                    target.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static class EnglishStats {
        int englishLetters;
        int totalChars;
    }

    private void ensureModuleGuides(JSONObject plan) {
        if (plan == null) {
            return;
        }
        try {
            JSONObject modulePlan = plan.optJSONObject("module_plan");
            if (modulePlan == null) {
                modulePlan = new JSONObject();
                plan.put("module_plan", modulePlan);
            }
            String[] modules = new String[]{"speech_sound", "prelinguistic", "vocabulary", "syntax", "social_pragmatics"};
            for (String key : modules) {
                JSONObject module = modulePlan.optJSONObject(key);
                if (module == null) {
                    module = new JSONObject();
                    modulePlan.put(key, module);
                }
                JSONObject guide = module.optJSONObject("intervention_guide");
                if (guide == null) {
                    guide = new JSONObject();
                    module.put("intervention_guide", guide);
                }
                ensureGuideSection(guide, "overall_summary", "text", "\u4fe1\u606f\u4e0d\u8db3\uff0c\u9700\u4eba\u5de5\u8865\u5145\u3002");
                ensureGuideSection(guide, "mastered", "items", new JSONArray());
                ensureGuideSection(guide, "not_mastered_overview", "text", "\u4fe1\u606f\u4e0d\u8db3\uff0c\u9700\u4eba\u5de5\u8865\u5145\u3002");
                ensureGuideSection(guide, "focus", "items", new JSONArray());
                ensureGuideSection(guide, "unstable", "items", new JSONArray());
                ensureGuideSection(guide, "smart_goal", "text", "\u4fe1\u606f\u4e0d\u8db3\uff0c\u9700\u4eba\u5de5\u8865\u5145\u3002");
                ensureGuideSection(guide, "home_guidance", "items", new JSONArray());
            }
        } catch (JSONException ignored) {
        }
    }

    private void ensureGuideSection(JSONObject guide, String sectionKey, String valueKey, Object defaultValue) {
        if (guide == null || sectionKey == null || valueKey == null) {
            return;
        }
        try {
            JSONObject section = guide.optJSONObject(sectionKey);
            if (section == null) {
                section = new JSONObject();
                guide.put(sectionKey, section);
            }
            if (!section.has(valueKey)) {
                section.put(valueKey, defaultValue);
            }
        } catch (JSONException ignored) {
        }
    }

    private static final class LoggingEventListener extends EventListener {
        private int resolveId(Call call) {
            if (call == null || call.request() == null) {
                return -1;
            }
            Integer id = call.request().tag(Integer.class);
            return id == null ? -1 : id;
        }

        @Override
        public void callStart(Call call) {
            Log.i(TAG, "request#" + resolveId(call) + " callStart");
        }

        @Override
        public void dnsStart(Call call, String domainName) {
            Log.i(TAG, "request#" + resolveId(call) + " dnsStart " + domainName);
        }

        @Override
        public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
            Log.i(TAG, "request#" + resolveId(call) + " dnsEnd addrs=" + (inetAddressList == null ? 0 : inetAddressList.size()));
        }

        @Override
        public void connectStart(Call call, java.net.InetSocketAddress inetSocketAddress, java.net.Proxy proxy) {
            Log.i(TAG, "request#" + resolveId(call) + " connectStart " + inetSocketAddress);
        }

        @Override
        public void secureConnectStart(Call call) {
            Log.i(TAG, "request#" + resolveId(call) + " tlsStart");
        }

        @Override
        public void secureConnectEnd(Call call, Handshake handshake) {
            Log.i(TAG, "request#" + resolveId(call) + " tlsEnd");
        }

        @Override
        public void connectEnd(Call call, java.net.InetSocketAddress inetSocketAddress, java.net.Proxy proxy, okhttp3.Protocol protocol) {
            Log.i(TAG, "request#" + resolveId(call) + " connectEnd " + protocol);
        }

        @Override
        public void callFailed(Call call, IOException ioe) {
            Log.w(TAG, "request#" + resolveId(call) + " callFailed " + ioe.getClass().getSimpleName() + " " + ioe.getMessage());
        }

        @Override
        public void callEnd(Call call) {
            Log.i(TAG, "request#" + resolveId(call) + " callEnd");
        }
    }
}
