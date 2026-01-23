package utils;

import com.example.CCLEvaluation.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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
    private static final double ENGLISH_RATIO_THRESHOLD = 0.08;
    private static final int ENGLISH_COUNT_THRESHOLD = 20;
    private static final int ENGLISH_VALUE_THRESHOLD = 12;
    private static final String[] ENGLISH_ABBREVIATION_WHITELIST = {
            "PST", "PN", "NWR", "SLP", "ASD", "ADHD", "SSD", "DLD"
    };
    private static final String ENGLISH_WARNING =
            "\u7cfb\u7edf\u63d0\u793a\uff1a\u5185\u5bb9\u5305\u542b\u8f83\u591a\u82f1\u6587\u672f\u8bed\uff0c\u5df2\u4fdd\u7559\u539f\u59cb\u8f93\u51fa\uff0c\u8bf7\u4eba\u5de5\u786e\u8ba4\u4e2d\u6587\u5316\u3002";
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
                .callTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void generateTreatmentPlan(String systemPrompt, String userPrompt, PlanCallback callback) {
        generateTreatmentPlanInternal(systemPrompt, userPrompt, callback, 0, DEFAULT_TEMPERATURE);
    }

    private void generateTreatmentPlanInternal(String systemPrompt, String userPrompt, PlanCallback callback, int retryCount, double temperature) {
        if (callback == null) {
            return;
        }
        String apiKey = BuildConfig.DEEPSEEK_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("DEEPSEEK_API_KEY is empty. Configure in local.properties.");
            return;
        }
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", "deepseek-reasoner");
            payload.put("temperature", temperature);
            payload.put("stream", false);
            payload.put("response_format", new JSONObject().put("type", "json_object"));
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            String enhancedUserPrompt = userPrompt
                    + "\n\n重要提示："
                    + "\n1) 请务必用简体中文输出所有 value（值），JSON 的 key 仍保持英文不变"
                    + "\n2) 所有内容都必须使用中文临床表述（允许少量缩写：PST、PN、NWR、SLP、ASD、ADHD、SSD、DLD）"
                    + "\n3) 输出必须是严格合法 JSON（不要输出任何 JSON 以外的文字）"
                    + "\n4) 如需使用英文术语，请先中文化再输出，不要输出英文句子";
            messages.put(new JSONObject().put("role", "user").put("content", enhancedUserPrompt));
            payload.put("messages", messages);

            RequestBody body = RequestBody.create(payload.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://api.deepseek.com/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String bodyString = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP " + response.code() + ": " + bodyString);
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
                            callback.onError("Empty content in response.");
                            return;
                        }
                        JSONObject plan = new JSONObject(content);
                        boolean shouldRewrite = shouldRewriteToChinese(plan);
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
                        callback.onSuccess(plan);
                    } catch (JSONException e) {
                        callback.onError("Parse error: " + e.getMessage());
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
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            String normalized = stripWhitelistedAbbreviations(value);
            if (normalized.isEmpty()) {
                continue;
            }
            if (!containsChinese(normalized) && countEnglishLetters(normalized) >= ENGLISH_VALUE_THRESHOLD) {
                return true;
            }
        }
        EnglishStats stats = countEnglishStats(values);
        double ratio = stats.totalChars == 0 ? 0.0 : (double) stats.englishLetters / stats.totalChars;
        return stats.englishLetters >= ENGLISH_COUNT_THRESHOLD && ratio >= ENGLISH_RATIO_THRESHOLD;
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

    private String buildRewriteUserPrompt(JSONObject plan) {
        return "\u8bf7\u4e0d\u6539\u53d8\u4efb\u4f55 key/\u7ed3\u6784/\u6570\u7ec4\u957f\u5ea6/\u7c7b\u578b\uff0c\u5c06\u4ee5\u4e0b JSON \u7684\u6240\u6709 value \u6539\u5199\u4e3a\u7b80\u4f53\u4e2d\u6587\u4e34\u5e8a\u8868\u8ff0\uff0c\u53ea\u8f93\u51fa\u4e25\u683c\u5408\u6cd5 JSON\uff0c\u4e0d\u8981\u8f93\u51fa\u5176\u4ed6\u6587\u5b57\uff1a\n"
                + plan.toString();
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

    private static class EnglishStats {
        int englishLetters;
        int totalChars;
    }
}
