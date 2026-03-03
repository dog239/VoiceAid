package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class ArticulationPlanHelper {
    public static final String MISSING_DETAIL_HINT = "\u672c\u6b21\u672a\u83b7\u53d6\u5230\u53ef\u7528\u4e8e\u5206\u9879\u7edf\u8ba1\u7684\u6570\u636e\uff0c\u5efa\u8bae\u8865\u6d4b\u6784\u97f3\u5206\u9879\u4ee5\u7ec6\u5316\u76ee\u6807\u97f3\u3002";
    private static final String DEFAULT_LEVEL = "uncertain";
    private static final String DEFAULT_CYCLE_WEEKS = "8-10";
    private static final String DEFAULT_SUPPORT_VISUAL = "\u89c6\u89c9\u63d0\u793a";
    private static final String DEFAULT_SUPPORT_AUDITORY = "\u542c\u89c9\u63d0\u793a";
    private static final String DEFAULT_LEVEL_TEXT = "\u5355\u8bcd";
    private static final String DEFAULT_STABILITY_RULE = "\u8fde\u7eed\u4e24\u6b21\u8bc4\u4f30\u4fdd\u6301\u7a33\u5b9a";
    private static final String DEFAULT_TARGET_PLACEHOLDER = "\u76ee\u6807\u8f85\u97f3\uff08\u4ee5\u8bc4\u4f30\u7ed3\u679c\u4e3a\u51c6\uff09";

    private ArticulationPlanHelper() {
    }

    public static JSONObject ensureArticulation(JSONObject treatmentPlan, JSONArray evaluationsA) {
        if (treatmentPlan == null) {
            treatmentPlan = new JSONObject();
        }
        try {
            JSONObject modulePlan = treatmentPlan.optJSONObject("module_plan");
            if (modulePlan == null) {
                modulePlan = new JSONObject();
                treatmentPlan.put("module_plan", modulePlan);
            }
            JSONObject speechSound = modulePlan.optJSONObject("speech_sound");
            if (speechSound == null) {
                speechSound = new JSONObject();
                modulePlan.put("speech_sound", speechSound);
            }
            ensureSpeechSoundArticulation(speechSound, evaluationsA);
        } catch (JSONException ignored) {
        }
        return treatmentPlan;
    }

    public static void applyArticulationReport(JSONObject treatmentPlan, JSONObject childData, JSONArray evaluationsA) {
        if (treatmentPlan == null) {
            return;
        }
        try {
            JSONObject modulePlan = treatmentPlan.optJSONObject("module_plan");
            if (modulePlan == null) {
                modulePlan = new JSONObject();
                treatmentPlan.put("module_plan", modulePlan);
            }
            JSONObject speechSound = modulePlan.optJSONObject("speech_sound");
            if (speechSound == null) {
                speechSound = new JSONObject();
                modulePlan.put("speech_sound", speechSound);
            }
            JSONObject articulation = ensureSpeechSoundArticulation(speechSound, evaluationsA);
            JSONObject report = findArticulationReport(childData);
            if (report != null) {
                applyReportFields(articulation, report);
            }
            JSONObject overall = articulation.optJSONObject("overall_summary");
            String overallText = overall == null ? "" : safeText(overall.optString("text", ""));
            if (overallText.isEmpty()) {
                String summary = TreatmentPromptBuilder.buildSpeechSoundSummary(evaluationsA);
                if (!safeText(summary).isEmpty()) {
                    overall = ensureObject(articulation, "overall_summary");
                    overall.put("text", summary);
                }
            }
        } catch (JSONException ignored) {
        }
    }

    public static JSONObject ensureSpeechSoundArticulation(JSONObject speechSound, JSONArray evaluationsA) {
        if (speechSound == null) {
            speechSound = new JSONObject();
        }
        JSONObject articulation = speechSound.optJSONObject("articulation");
        JSONObject defaults = buildDefaultArticulation(evaluationsA);
        if (articulation == null) {
            articulation = defaults;
        } else {
            mergeMissing(articulation, defaults);
        }
        JSONObject smartGoal = articulation.optJSONObject("smart_goal");
        if (smartGoal == null) {
            smartGoal = defaults.optJSONObject("smart_goal");
            try {
                articulation.put("smart_goal", smartGoal);
            } catch (JSONException ignored) {
            }
        }
        try {
            smartGoal.put("text", buildSmartGoalText(smartGoal));
        } catch (JSONException ignored) {
        }
        try {
            speechSound.put("articulation", articulation);
        } catch (JSONException ignored) {
        }
        return articulation;
    }

    public static JSONObject buildDefaultArticulation(JSONArray evaluationsA) {
        JSONObject articulation = new JSONObject();
        try {
            JSONObject overall = new JSONObject();
            overall.put("level", DEFAULT_LEVEL);
            overall.put("text", "\u6574\u4f53\u6784\u97f3\u8868\u73b0\u5c1a\u672a\u5b8c\u5168\u8fbe\u5230\u5e74\u9f84\u53d1\u5c55\u6c34\u5e73\uff0c\u5b58\u5728\u660e\u663e\u56f0\u96be\u6216\u4e0d\u7a33\u5b9a\u8868\u73b0\u3002");
            articulation.put("overall_summary", overall);

            JSONObject mastered = new JSONObject();
            mastered.put("intro", "\u5df2\u89c2\u5bdf\u5230\u90e8\u5206\u5df2\u638c\u63e1\u7684\u6784\u97f3\u80fd\u529b\uff1a");
            JSONArray masteredItems = new JSONArray();
            masteredItems.put("\u591a\u6570\u5143\u97f3\u53d1\u97f3\u76f8\u5bf9\u6e05\u6670\uff0c\u5177\u5907\u4e00\u5b9a\u6784\u97f3\u57fa\u7840\u3002");
            masteredItems.put("\u90e8\u5206\u5e38\u89c1\u97f3\u8282\u7ed3\u6784\u80fd\u591f\u7a33\u5b9a\u4ea7\u751f\u3002");
            mastered.put("items", masteredItems);
            articulation.put("mastered", mastered);

            JSONObject notMastered = new JSONObject();
            notMastered.put("text", "\u672a\u638c\u63e1\u80fd\u529b\u603b\u4f53\u8868\u73b0\u4e3a\u90e8\u5206\u76ee\u6807\u97f3\u4ea7\u51fa\u4e0d\u7a33\u5b9a\uff0c\u9700\u8fdb\u4e00\u6b65\u7ec6\u5316\u76ee\u6807\u97f3\u5e76\u52a0\u5f3a\u7ec3\u4e60\u3002");
            articulation.put("not_mastered_overview", notMastered);

            JSONObject focus = new JSONObject();
            focus.put("title", "\u9700\u8981\u91cd\u70b9\u5173\u6ce8\u7684\u80fd\u529b");
            focus.put("items", new JSONArray());
            focus.put("note", "");
            articulation.put("focus", focus);

            JSONObject unstable = new JSONObject();
            unstable.put("title", "\u4e0d\u7a33\u5b9a\u7684\u80fd\u529b");
            unstable.put("items", new JSONArray());
            articulation.put("unstable", unstable);

            JSONObject smartGoal = new JSONObject();
            smartGoal.put("cycle_weeks", DEFAULT_CYCLE_WEEKS);
            JSONArray support = new JSONArray();
            support.put(DEFAULT_SUPPORT_VISUAL);
            support.put(DEFAULT_SUPPORT_AUDITORY);
            smartGoal.put("support", support);
            smartGoal.put("level", DEFAULT_LEVEL_TEXT);
            smartGoal.put("target_sounds", new JSONArray());
            smartGoal.put("accuracy_threshold", 0.80);
            smartGoal.put("stability_rule", DEFAULT_STABILITY_RULE);
            smartGoal.put("text", buildSmartGoalText(smartGoal));
            articulation.put("smart_goal", smartGoal);

            JSONObject homeGuidance = new JSONObject();
            JSONArray guidanceItems = new JSONArray();
            guidanceItems.put("\u4fdd\u6301\u6162\u901f\u6e05\u6670\u7684\u793a\u8303\uff0c\u5f15\u5bfc\u5b69\u5b50\u6a21\u4eff\u6b63\u786e\u53d1\u97f3\u3002");
            guidanceItems.put("\u7ed3\u5408\u6e38\u620f\u5316\u7ec3\u4e60\u5f3a\u5316\u76ee\u6807\u97f3\uff0c\u907f\u514d\u9891\u7e41\u7ea0\u6b63\u5e26\u6765\u632b\u8d25\u3002");
            homeGuidance.put("items", guidanceItems);
            articulation.put("home_guidance", homeGuidance);
        } catch (JSONException ignored) {
        }
        return articulation;
    }

    public static String buildSmartGoalText(JSONObject smartGoal) {
        if (smartGoal == null) {
            return "";
        }
        String cycleWeeks = safeText(smartGoal.optString("cycle_weeks", DEFAULT_CYCLE_WEEKS));
        if (cycleWeeks.isEmpty()) {
            cycleWeeks = DEFAULT_CYCLE_WEEKS;
        }
        String level = safeText(smartGoal.optString("level", DEFAULT_LEVEL_TEXT));
        if (level.isEmpty()) {
            level = DEFAULT_LEVEL_TEXT;
        }
        String stabilityRule = safeText(smartGoal.optString("stability_rule", DEFAULT_STABILITY_RULE));
        if (stabilityRule.isEmpty()) {
            stabilityRule = DEFAULT_STABILITY_RULE;
        }
        double accuracy = smartGoal.optDouble("accuracy_threshold", 0.80);
        if (accuracy <= 0) {
            accuracy = 0.80;
        }
        int percent = (int) Math.round(accuracy * 100.0);

        List<String> supportList = toStringList(smartGoal.optJSONArray("support"));
        String supportText = formatSupportText(supportList);
        List<String> targetSounds = toStringList(smartGoal.optJSONArray("target_sounds"));
        String targetText = targetSounds.isEmpty() ? DEFAULT_TARGET_PLACEHOLDER : joinWithSeparator(targetSounds, "\u3001");
        return String.format(Locale.CHINA,
                "\u5728%s\u5468\u5e72\u9884\u5468\u671f\u5185\uff0c\u513f\u7ae5\u5728%s\u63d0\u793a\u652f\u6301\u4e0b\uff0c\u80fd\u591f\u5728%s\u6c34\u5e73\u6b63\u786e\u4ea7\u751f%s\uff0c\u6b63\u786e\u7387\u8fbe\u5230%d%%\u4ee5\u4e0a\uff0c\u5e76\u5728%s\u4e2d\u4fdd\u6301\u7a33\u5b9a\u3002",
                cycleWeeks, supportText, level, targetText, percent, stabilityRule);
    }

    private static void mergeMissing(JSONObject target, JSONObject defaults) {
        if (target == null || defaults == null) {
            return;
        }
        Iterator<String> keys = defaults.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object defaultValue = defaults.opt(key);
            Object current = target.opt(key);
            if (current == null || JSONObject.NULL.equals(current)) {
                putClone(target, key, defaultValue);
                continue;
            }
            if (defaultValue instanceof JSONObject) {
                if (current instanceof JSONObject) {
                    mergeMissing((JSONObject) current, (JSONObject) defaultValue);
                } else {
                    putClone(target, key, defaultValue);
                }
            } else if (defaultValue instanceof JSONArray) {
                if (!(current instanceof JSONArray)) {
                    putClone(target, key, defaultValue);
                }
            } else if (defaultValue instanceof String) {
                if (!(current instanceof String)) {
                    putClone(target, key, defaultValue);
                }
            } else if (defaultValue instanceof Number) {
                if (!(current instanceof Number)) {
                    putClone(target, key, defaultValue);
                }
            }
        }
        JSONObject overall = target.optJSONObject("overall_summary");
        if (overall != null) {
            String level = safeText(overall.optString("level", DEFAULT_LEVEL));
            if (!"below_age_expected".equals(level) && !"age_expected".equals(level) && !"uncertain".equals(level)) {
                try {
                    overall.put("level", DEFAULT_LEVEL);
                } catch (JSONException ignored) {
                }
            }
        }
        JSONObject homeGuidance = target.optJSONObject("home_guidance");
        if (homeGuidance != null) {
            JSONArray items = homeGuidance.optJSONArray("items");
            if (items == null || items.length() == 0) {
                try {
                    JSONObject defaultArticulation = buildDefaultArticulation(null);
                    JSONObject defaultsHome = defaultArticulation.optJSONObject("home_guidance");
                    if (defaultsHome != null) {
                        homeGuidance.put("items", defaultsHome.optJSONArray("items"));
                    }
                } catch (JSONException ignored) {
                }
            }
        }
        JSONObject smartGoal = target.optJSONObject("smart_goal");
        if (smartGoal != null) {
            JSONArray support = smartGoal.optJSONArray("support");
            if (support == null || support.length() == 0) {
                JSONArray supportDefaults = new JSONArray();
                supportDefaults.put(DEFAULT_SUPPORT_VISUAL);
                supportDefaults.put(DEFAULT_SUPPORT_AUDITORY);
                try {
                    smartGoal.put("support", supportDefaults);
                } catch (JSONException ignored) {
                }
            }
        }
    }

    private static void putClone(JSONObject target, String key, Object value) {
        try {
            target.put(key, cloneValue(value));
        } catch (JSONException ignored) {
        }
    }

    private static Object cloneValue(Object value) {
        if (value instanceof JSONObject) {
            try {
                return new JSONObject(((JSONObject) value).toString());
            } catch (JSONException ignored) {
                return value;
            }
        }
        if (value instanceof JSONArray) {
            try {
                return new JSONArray(((JSONArray) value).toString());
            } catch (JSONException ignored) {
                return value;
            }
        }
        return value;
    }

    private static String formatSupportText(List<String> supportList) {
        if (supportList == null || supportList.isEmpty()) {
            return DEFAULT_SUPPORT_VISUAL + "\u4e0e" + DEFAULT_SUPPORT_AUDITORY;
        }
        if (supportList.size() == 1) {
            return supportList.get(0);
        }
        if (supportList.size() == 2) {
            return supportList.get(0) + "\u4e0e" + supportList.get(1);
        }
        List<String> trimmed = new ArrayList<>();
        for (String item : supportList) {
            if (!safeText(item).isEmpty()) {
                trimmed.add(item.trim());
            }
        }
        if (trimmed.isEmpty()) {
            return DEFAULT_SUPPORT_VISUAL + "\u4e0e" + DEFAULT_SUPPORT_AUDITORY;
        }
        if (trimmed.size() == 1) {
            return trimmed.get(0);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < trimmed.size(); i++) {
            if (i > 0) {
                builder.append(i == trimmed.size() - 1 ? "\u4e0e" : "\u3001");
            }
            builder.append(trimmed.get(i));
        }
        return builder.toString();
    }

    private static String joinWithSeparator(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            String value = safeText(values.get(i));
            if (value.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private static JSONObject findArticulationReport(JSONObject childData) {
        if (childData == null) {
            return null;
        }
        JSONObject report = optJSONObjectFlexible(childData, "articulationReport");
        if (report == null) {
            report = optJSONObjectFlexible(childData, "speechSoundReport");
        }
        if (report == null) {
            report = optJSONObjectFlexible(childData, "speech_sound_report");
        }
        if (report == null) {
            JSONObject reports = optJSONObjectFlexible(childData, "moduleReports");
            if (reports == null) {
                reports = optJSONObjectFlexible(childData, "module_reports");
            }
            if (reports == null) {
                reports = optJSONObjectFlexible(childData, "reports");
            }
            if (reports == null) {
                reports = optJSONObjectFlexible(childData, "evaluationReports");
            }
            if (reports != null) {
                report = optJSONObjectFlexible(reports, "speech_sound");
                if (report == null) {
                    report = optJSONObjectFlexible(reports, "articulation");
                }
                if (report == null) {
                    report = optJSONObjectFlexible(reports, "A");
                }
            }
        }
        if (report == null) {
            JSONObject evaluations = childData.optJSONObject("evaluations");
            if (evaluations != null) {
                report = optJSONObjectFlexible(evaluations, "A_report");
                if (report == null) {
                    report = optJSONObjectFlexible(evaluations, "AReport");
                }
                if (report == null) {
                    report = optJSONObjectFlexible(evaluations, "speech_sound_report");
                }
            }
        }
        JSONObject nested = report == null ? null : optJSONObjectFlexible(report, "articulation");
        return nested != null ? nested : report;
    }

    private static void applyReportFields(JSONObject articulation, JSONObject report) throws JSONException {
        if (articulation == null || report == null) {
            return;
        }
        String conclusion = extractText(report, "conclusion", "summary", "overall_summary", "overallSummary",
                "result", "evaluation_result", "assessment");
        if (!conclusion.isEmpty()) {
            JSONObject overall = ensureObject(articulation, "overall_summary");
            overall.put("text", conclusion);
        }
        List<String> focusItems = extractList(report, "focus", "focus_items", "focusItems");
        if (!focusItems.isEmpty()) {
            JSONObject focus = ensureObject(articulation, "focus");
            focus.put("items", toJsonArray(focusItems));
        }
        List<String> unstableItems = extractList(report, "unstable", "unstable_items", "unstableItems");
        if (!unstableItems.isEmpty()) {
            JSONObject unstable = ensureObject(articulation, "unstable");
            unstable.put("items", toJsonArray(unstableItems));
        }
        List<String> suggestions = extractList(report, "recommendations", "suggestions", "advice", "guidance",
                "home_guidance", "homeGuidance");
        if (!suggestions.isEmpty()) {
            JSONObject homeGuidance = ensureObject(articulation, "home_guidance");
            homeGuidance.put("items", toJsonArray(suggestions));
        }
    }

    private static JSONObject optJSONObjectFlexible(JSONObject source, String key) {
        if (source == null || key == null) {
            return null;
        }
        Object value = source.opt(key);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        if (value instanceof String) {
            try {
                return new JSONObject((String) value);
            } catch (JSONException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String extractText(JSONObject source, String... keys) {
        if (source == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            String value = safeText(source.optString(key, ""));
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static List<String> extractList(JSONObject source, String... keys) {
        List<String> result = new ArrayList<>();
        if (source == null || keys == null) {
            return result;
        }
        for (String key : keys) {
            Object value = source.opt(key);
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    String text = safeText(array.optString(i, ""));
                    if (!text.isEmpty()) {
                        result.add(text);
                    }
                }
            } else if (value instanceof String) {
                String raw = safeText((String) value);
                if (!raw.isEmpty()) {
                    for (String part : raw.split("[\\n;；]+")) {
                        String text = safeText(part);
                        if (!text.isEmpty()) {
                            result.add(text);
                        }
                    }
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        return result;
    }

    private static JSONArray toJsonArray(List<String> values) {
        JSONArray array = new JSONArray();
        if (values == null) {
            return array;
        }
        for (String value : values) {
            String text = safeText(value);
            if (!text.isEmpty()) {
                array.put(text);
            }
        }
        return array;
    }

    private static JSONObject ensureObject(JSONObject parent, String key) throws JSONException {
        JSONObject obj = parent.optJSONObject(key);
        if (obj == null) {
            obj = new JSONObject();
            parent.put(key, obj);
        }
        return obj;
    }

    private static List<String> toStringList(JSONArray array) {
        List<String> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.length(); i++) {
            String value = safeText(array.optString(i, ""));
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
