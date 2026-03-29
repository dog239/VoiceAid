package org.json;

import java.util.ArrayList;
import java.util.List;

public class JSONArray {
    private final List<Object> values = new ArrayList<>();

    public JSONArray() {
    }

    public JSONArray put(Object value) {
        values.add(value);
        return this;
    }

    public JSONArray put(int value) {
        return put(Integer.valueOf(value));
    }

    public JSONArray put(long value) {
        return put(Long.valueOf(value));
    }

    public JSONArray put(double value) {
        return put(Double.valueOf(value));
    }

    public JSONArray put(boolean value) {
        return put(Boolean.valueOf(value));
    }

    public int length() {
        return values.size();
    }

    public Object opt(int index) {
        return index >= 0 && index < values.size() ? values.get(index) : null;
    }

    public String optString(int index) {
        return optString(index, "");
    }

    public String optString(int index, String fallback) {
        Object value = opt(index);
        return value == null ? fallback : String.valueOf(value);
    }

    public JSONObject optJSONObject(int index) {
        Object value = opt(index);
        return value instanceof JSONObject ? (JSONObject) value : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(JSONObject.valueToString(values.get(i)));
        }
        builder.append("]");
        return builder.toString();
    }
}
