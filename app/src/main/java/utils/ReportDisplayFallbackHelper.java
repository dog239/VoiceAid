package utils;

import org.json.JSONArray;

public final class ReportDisplayFallbackHelper {
    public static final String FIELD_OVERALL_SUMMARY = "overallSummary";
    public static final String FIELD_MASTERED = "mastered";
    public static final String FIELD_NOT_MASTERED_OVERVIEW = "notMasteredOverview";
    public static final String FIELD_FOCUS = "focus";
    public static final String FIELD_UNSTABLE = "unstable";
    public static final String FIELD_SMART_GOAL_TEXT = "smartGoal.text";
    public static final String FIELD_HOME_GUIDANCE = "homeGuidance";

    private ReportDisplayFallbackHelper() {
    }

    public static String getDisplayTextOrFallback(String value, String fieldKey) {
        String text = safeText(value);
        return text.isEmpty() ? getFallback(fieldKey) : text;
    }

    public static String getFallback(String fieldKey) {
        switch (fieldKey) {
            case FIELD_OVERALL_SUMMARY:
                return "暂无整体评估结果，需进一步检查。";
            case FIELD_MASTERED:
                return "暂无明确已掌握能力，建议结合后续观察进一步判断。";
            case FIELD_NOT_MASTERED_OVERVIEW:
                return "暂无未掌握能力的明确说明，需结合后续评估进一步分析。";
            case FIELD_FOCUS:
                return "当前暂无明确重点关注项，建议结合后续评估持续观察。";
            case FIELD_UNSTABLE:
                return "当前暂无明显不稳定能力表现，建议结合后续观察进一步确认。";
            case FIELD_SMART_GOAL_TEXT:
                return "暂无明确干预目标，建议治疗师结合评估结果进一步制定。";
            case FIELD_HOME_GUIDANCE:
                return "暂无家庭干预指导建议，建议结合治疗师意见进一步补充。";
            default:
                return "暂无内容。";
        }
    }

    public static boolean hasDisplayItems(JSONArray array) {
        if (array == null || array.length() == 0) {
            return false;
        }
        for (int i = 0; i < array.length(); i++) {
            String item = safeText(array.optString(i, ""));
            if (!item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
