package utils.netService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

public final class ResponseParsers {
    private ResponseParsers() {
    }

    public static String message(JSONObject jsonObject) {
        return jsonObject.optString("message", "未知错误");
    }

    public static String objectString(JSONObject jsonObject, String key, String defaultValue) {
        Object value = jsonObject.opt(key);
        return value == null ? defaultValue : value.toString();
    }

    public static String firstArray(JSONObject jsonObject, String... keys) {
        for (String key : keys) {
            JSONArray array = jsonObject.optJSONArray(key);
            if (array != null) {
                return array.toString();
            }
        }
        return "[]";
    }

    public static String extractAudioFromJson(JSONObject jsonObject) {
        return extractBinaryFromJson(jsonObject, "audio");
    }

    public static String extractBinaryFromJson(JSONObject jsonObject, String... keys) {
        for (String key : keys) {
            String value = extractNestedString(jsonObject, key);
            if (!isBlank(value)) {
                return value;
            }
        }
        JSONObject audioObject = jsonObject.optJSONObject("audio");
        if (audioObject != null) {
            for (String key : keys) {
                String value = extractNestedString(audioObject, key);
                if (!isBlank(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private static String extractNestedString(JSONObject jsonObject, String key) {
        String direct = jsonObject.optString(key, null);
        if (!isBlank(direct)) {
            return direct;
        }
        String alternateCase = jsonObject.optString(capitalize(key), null);
        if (!isBlank(alternateCase)) {
            return alternateCase;
        }
        JSONObject nested = jsonObject.optJSONObject(key);
        if (nested != null) {
            String nestedValue = nested.optString(key, null);
            if (!isBlank(nestedValue)) {
                return nestedValue;
            }
            nestedValue = nested.optString(capitalize(key), null);
            if (!isBlank(nestedValue)) {
                return nestedValue;
            }
        }
        return null;
    }

    private static String capitalize(String key) {
        if (isBlank(key)) {
            return key;
        }
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    public static String encodeAudio(byte[] bytes) {
        return encodeBinary(bytes);
    }

    public static String encodeBinary(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
