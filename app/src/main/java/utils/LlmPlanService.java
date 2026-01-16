package utils;

import com.example.CCLEvaluation.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final double ENGLISH_RATIO_THRESHOLD = 0.12;
    private static final int ENGLISH_COUNT_THRESHOLD = 30;
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
        generateTreatmentPlanInternal(systemPrompt, userPrompt, callback, 0);
    }

    private void generateTreatmentPlanInternal(String systemPrompt, String userPrompt, PlanCallback callback, int retryCount) {
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
            payload.put("model", "deepseek-chat");
            payload.put("temperature", 0.2);
            payload.put("stream", false);
            payload.put("response_format", new JSONObject().put("type", "json_object"));
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userPrompt));
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
                        if (shouldRewrite && retryCount < 1) {
                            String rewriteSystemPrompt = "\u4f60\u662fJSON\u4e34\u5e8a\u6587\u6848\u6539\u5199\u5668\uff1b\u4fdd\u6301 key/\u7ed3\u6784\u5b8c\u5168\u4e0d\u53d8\uff1b\u6240\u6709 value \u6539\u5199\u4e3a\u4e2d\u6587\u4e3a\u4e3b\u7684\u4e34\u5e8a\u8868\u8ff0\uff1b\u5141\u8bb8\u5c11\u91cf\u7f29\u5199\uff1b\u53ea\u8f93\u51fa\u4e25\u683c\u5408\u6cd5 JSON\u3002";
                            String rewriteUserPrompt = plan.toString();
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
                            }, retryCount + 1);
                            return;
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
        EnglishStats stats = countEnglishStats(values);
        double ratio = stats.totalChars == 0 ? 0.0 : (double) stats.englishLetters / stats.totalChars;
        return stats.englishLetters > ENGLISH_COUNT_THRESHOLD && ratio > ENGLISH_RATIO_THRESHOLD;
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

    private String stripWhitelistedAbbreviations(String text) {
        String normalized = text;
        for (String abbr : ENGLISH_ABBREVIATION_WHITELIST) {
            normalized = normalized.replaceAll("(?i)\\b" + Pattern.quote(abbr) + "\\b", "");
        }
        return normalized;
    }

    private boolean isEnglishLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static class EnglishStats {
        int englishLetters;
        int totalChars;
    }
}
