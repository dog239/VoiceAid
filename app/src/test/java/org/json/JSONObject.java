package org.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONObject {
    public static final Object NULL = new Object();

    private final Map<String, Object> values = new LinkedHashMap<>();

    public JSONObject() {
    }

    public JSONObject(String source) throws JSONException {
        String text = source == null ? "" : source.trim();
        if ("{}".equals(text) || text.isEmpty()) {
            return;
        }
        throw new JSONException("String parsing is not supported in unit-test stub JSONObject");
    }

    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("key == null");
        }
        values.put(key, value == null ? NULL : value);
        return this;
    }

    public JSONObject put(String key, int value) throws JSONException {
        return put(key, Integer.valueOf(value));
    }

    public JSONObject put(String key, long value) throws JSONException {
        return put(key, Long.valueOf(value));
    }

    public JSONObject put(String key, double value) throws JSONException {
        return put(key, Double.valueOf(value));
    }

    public JSONObject put(String key, boolean value) throws JSONException {
        return put(key, Boolean.valueOf(value));
    }

    public Object opt(String key) {
        return key == null ? null : values.get(key);
    }

    public JSONObject optJSONObject(String key) {
        Object value = opt(key);
        return value instanceof JSONObject ? (JSONObject) value : null;
    }

    public JSONArray optJSONArray(String key) {
        Object value = opt(key);
        return value instanceof JSONArray ? (JSONArray) value : null;
    }

    public String optString(String key) {
        return optString(key, "");
    }

    public String optString(String key, String fallback) {
        Object value = opt(key);
        if (value == null || value == NULL) {
            return fallback;
        }
        return String.valueOf(value);
    }

    public int optInt(String key, int fallback) {
        Object value = opt(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public int optInt(String key) {
        return optInt(key, 0);
    }

    public double optDouble(String key, double fallback) {
        Object value = opt(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value == null ? fallback : Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public double optDouble(String key) {
        return optDouble(key, 0.0d);
    }

    public boolean optBoolean(String key, boolean fallback) {
        Object value = opt(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public boolean has(String key) {
        return key != null && values.containsKey(key);
    }

    public boolean isNull(String key) {
        Object value = opt(key);
        return value == null || value == NULL;
    }

    public JSONArray names() {
        JSONArray array = new JSONArray();
        for (String key : values.keySet()) {
            array.put(key);
        }
        return array.length() == 0 ? null : array;
    }

    public int length() {
        return values.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                builder.append(",");
            }
            first = false;
            builder.append("\"").append(escape(entry.getKey())).append("\":")
                    .append(valueToString(entry.getValue()));
        }
        builder.append("}");
        return builder.toString();
    }

    static String valueToString(Object value) {
        if (value == null || value == NULL) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return String.valueOf(value);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
