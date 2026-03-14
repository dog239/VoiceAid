package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class ModuleInterventionGuideSchema {
    private static final String DEFAULT_MODEL = "deepseek-chat";

    private ModuleInterventionGuideSchema() {
    }

    public static JSONObject normalize(JSONObject raw,
                                       String moduleType,
                                       String moduleTitle,
                                       JSONArray subtypes) throws JSONException {
        JSONObject out = new JSONObject();
        JSONObject src = raw == null ? new JSONObject() : raw;

        put(out, "moduleType", safe(moduleType));
        put(out, "moduleTitle", safe(moduleTitle));
        out.put("subtypes", normalizeArray(src.optJSONArray("subtypes"), subtypes));
        put(out, "overallSummary", firstNonEmpty(
                src.optString("overallSummary", ""),
                optNestedText(src, "overall_summary", "text")
        ));
        out.put("mastered", normalizeStringArray(src, "mastered"));
        put(out, "notMasteredOverview", firstNonEmpty(
                src.optString("notMasteredOverview", ""),
                optNestedText(src, "not_mastered_overview", "text")
        ));
        out.put("focus", normalizeStringArray(src, "focus"));
        out.put("unstable", normalizeStringArray(src, "unstable"));

        JSONObject smartGoal = new JSONObject();
        JSONObject srcSmartGoal = optObject(src, "smartGoal");
        if (srcSmartGoal == null) {
            srcSmartGoal = optObject(src, "smart_goal");
        }
        if (srcSmartGoal == null) {
            srcSmartGoal = new JSONObject();
        }
        put(smartGoal, "text", firstNonEmpty(
                srcSmartGoal.optString("text", ""),
                srcSmartGoal.optString("goal", "")
        ));
        int cycleWeeks = srcSmartGoal.optInt("cycleWeeks", srcSmartGoal.optInt("cycle_weeks", 8));
        double accuracyThreshold = srcSmartGoal.optDouble("accuracyThreshold",
                srcSmartGoal.optDouble("accuracy_threshold", 0.8d));
        if (cycleWeeks <= 0) {
            cycleWeeks = 8;
        }
        if (accuracyThreshold <= 0 || accuracyThreshold > 1.0d) {
            accuracyThreshold = 0.8d;
        }
        smartGoal.put("cycleWeeks", cycleWeeks);
        smartGoal.put("accuracyThreshold", accuracyThreshold);
        smartGoal.put("support", normalizeStringArray(srcSmartGoal, "support"));
        out.put("smartGoal", smartGoal);

        out.put("homeGuidance", normalizeStringArray(src, "homeGuidance", "home_guidance"));
        out.put("notesForTherapist", normalizeStringArray(src, "notesForTherapist", "notes_for_therapist"));

        JSONObject meta = new JSONObject();
        JSONObject srcMeta = optObject(src, "meta");
        if (srcMeta == null) {
            srcMeta = new JSONObject();
        }
        put(meta, "model", firstNonEmpty(srcMeta.optString("model", ""), DEFAULT_MODEL));
        put(meta, "generatedAt", firstNonEmpty(srcMeta.optString("generatedAt", ""), isoNow()));
        put(meta, "reviewStatus", firstNonEmpty(srcMeta.optString("reviewStatus", ""), "draft"));
        meta.put("reviewedByTherapist", srcMeta.optBoolean("reviewedByTherapist", false));
        out.put("meta", meta);

        JSONObject srcCustom = src.optJSONObject("custom");
        if (srcCustom != null) {
            JSONArray extraGuidance = new JSONArray();

            JSONArray oralMotor = normalizeStringArray(srcCustom, "oralMotorSuggestions", "oral_motor_suggestions");
            for (int i = 0; i < oralMotor.length(); i++) {
                extraGuidance.put("[口肌训练] " + oralMotor.optString(i));
            }

            JSONArray scripts = normalizeStringArray(srcCustom, "socialScripts", "social_scripts");
            for (int i = 0; i < scripts.length(); i++) {
                extraGuidance.put("[社交脚本] " + scripts.optString(i));
            }

            if (extraGuidance.length() > 0) {
                JSONArray currentHome = out.optJSONArray("homeGuidance");
                if (currentHome == null) currentHome = new JSONArray();
                for (int i = 0; i < extraGuidance.length(); i++) {
                    currentHome.put(extraGuidance.optString(i));
                }
                out.put("homeGuidance", currentHome);
            }
        }
        return out;
    }

    public static JSONObject createDefault(String moduleType, String moduleTitle, JSONArray subtypes) throws JSONException {
        return normalize(new JSONObject(), moduleType, moduleTitle, subtypes);
    }

    private static JSONObject optObject(JSONObject source, String key) {
        if (source == null || key == null) {
            return null;
        }
        Object value = source.opt(key);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        if (value instanceof String) {
            String text = safe((String) value);
            if (text.startsWith("{")) {
                try {
                    return new JSONObject(text);
                } catch (JSONException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static JSONArray normalizeArray(JSONArray first, JSONArray fallback) {
        if (first != null && first.length() > 0) {
            return copyArray(first);
        }
        if (fallback != null) {
            return copyArray(fallback);
        }
        return new JSONArray();
    }

    private static JSONArray normalizeStringArray(JSONObject src, String... keys) {
        if (src == null || keys == null) {
            return new JSONArray();
        }
        for (String key : keys) {
            JSONArray raw = src.optJSONArray(key);
            if (raw != null) {
                JSONArray normalized = new JSONArray();
                for (int i = 0; i < raw.length(); i++) {
                    String text = safe(raw.optString(i, ""));
                    if (!text.isEmpty()) {
                        normalized.put(text);
                    }
                }
                if (normalized.length() > 0) {
                    return normalized;
                }
            }
            JSONObject block = optObject(src, key);
            if (block != null) {
                JSONArray items = block.optJSONArray("items");
                if (items != null) {
                    JSONArray normalized = new JSONArray();
                    for (int i = 0; i < items.length(); i++) {
                        String text = safe(items.optString(i, ""));
                        if (!text.isEmpty()) {
                            normalized.put(text);
                        }
                    }
                    if (normalized.length() > 0) {
                        return normalized;
                    }
                }
            }
        }
        return new JSONArray();
    }

    private static JSONArray copyArray(JSONArray source) {
        JSONArray out = new JSONArray();
        if (source == null) {
            return out;
        }
        for (int i = 0; i < source.length(); i++) {
            out.put(source.opt(i));
        }
        return out;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String text = safe(value);
            if (!text.isEmpty()) {
                return text;
            }
        }
        return "";
    }

    private static String optNestedText(JSONObject source, String blockKey, String textKey) {
        JSONObject block = optObject(source, blockKey);
        if (block == null) {
            return "";
        }
        return safe(block.optString(textKey, ""));
    }

    private static void put(JSONObject target, String key, String value) throws JSONException {
        target.put(key, safe(value));
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String isoNow() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}

