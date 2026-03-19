package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class OverallInterventionReportBuilder {
    public static final String REPORT_MODE_OVERALL_INTERVENTION = "overall_intervention";

    private static final String TITLE_TREATMENT_PLAN = "干预指导/治疗方案";
    private static final String TITLE_OVERALL_REPORT = "总体干预报告";
    private static final String CONTENT_TYPE_PARAGRAPH = "paragraph";
    private static final String CONTENT_TYPE_BULLETS = "bullets";
    private static final String[] MODULE_ORDER = {
            "prelinguistic",
            "social",
            "vocabulary",
            "syntax",
            "articulation"
    };

    private OverallInterventionReportBuilder() {
    }

    public static boolean isReady(JSONObject childJson) {
        if (childJson == null) {
            return false;
        }
        for (String moduleType : MODULE_ORDER) {
            JSONObject normalizedGuide = normalizeGuide(childJson, moduleType);
            if (normalizedGuide == null) {
                return false;
            }
            if (safeText(normalizedGuide.optString("overallSummary", "")).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static JSONObject build(JSONObject childJson) {
        JSONObject report = new JSONObject();
        try {
            JSONArray moduleOrder = new JSONArray();
            JSONArray modules = new JSONArray();

            for (String moduleType : MODULE_ORDER) {
                JSONObject normalizedGuide = normalizeGuide(childJson, moduleType);
                if (normalizedGuide == null) {
                    continue;
                }
                if (safeText(normalizedGuide.optString("overallSummary", "")).isEmpty()) {
                    continue;
                }
                modules.put(buildModule(moduleType, normalizedGuide));
                moduleOrder.put(moduleType);
            }

            report.put("reportMode", REPORT_MODE_OVERALL_INTERVENTION);
            report.put("metadata", buildMetadata(childJson, moduleOrder, modules.length()));
            report.put("childInfo", buildChildInfo(childJson));
            report.put("modules", modules);
        } catch (JSONException ignored) {
        }
        return report;
    }

    private static JSONObject buildMetadata(JSONObject childJson, JSONArray moduleOrder, int moduleCount) throws JSONException {
        JSONObject metadata = new JSONObject();
        JSONObject info = optObject(childJson, "info");
        metadata.put("title", TITLE_TREATMENT_PLAN);
        metadata.put("reportTitle", TITLE_OVERALL_REPORT);
        metadata.put("date", resolveReportDate(info));
        metadata.put("moduleOrder", moduleOrder == null ? new JSONArray() : moduleOrder);
        metadata.put("moduleCount", Math.max(moduleCount, 0));
        return metadata;
    }

    private static JSONObject buildChildInfo(JSONObject childJson) throws JSONException {
        JSONObject info = optObject(childJson, "info");
        if (info == null) {
            info = new JSONObject();
        }

        JSONObject childInfo = new JSONObject();
        childInfo.put("name", firstNonEmpty(info.optString("name", ""), childJson == null ? "" : childJson.optString("name", "")));
        childInfo.put("gender", safeText(info.optString("gender", "")));
        childInfo.put("birthDate", safeText(info.optString("birthDate", "")));
        childInfo.put("phone", safeText(info.optString("phone", "")));
        childInfo.put("address", safeText(info.optString("address", "")));
        childInfo.put("familyStatus", safeText(info.optString("familyStatus", "")));
        childInfo.put("familyMembers", normalizeFamilyMembers(info.optJSONArray("familyMembers")));
        return childInfo;
    }

    private static JSONArray normalizeFamilyMembers(JSONArray source) throws JSONException {
        JSONArray out = new JSONArray();
        if (source == null) {
            return out;
        }
        for (int i = 0; i < source.length(); i++) {
            JSONObject item = source.optJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONObject member = new JSONObject();
            member.put("name", safeText(item.optString("name", "")));
            member.put("relation", safeText(item.optString("relation", "")));
            member.put("phone", firstNonEmpty(item.optString("phone", ""), item.optString("member_phone", "")));
            member.put("occupation", safeText(item.optString("occupation", "")));
            member.put("education", safeText(item.optString("education", "")));
            out.put(member);
        }
        return out;
    }

    private static JSONObject buildModule(String moduleType, JSONObject normalizedGuide) throws JSONException {
        JSONObject module = new JSONObject();
        String moduleTitle = firstNonEmpty(
                normalizedGuide.optString("moduleTitle", ""),
                ModuleReportHelper.moduleTitle(moduleType));

        module.put("moduleType", moduleType);
        module.put("moduleTitle", moduleTitle + "干预报告");
        module.put("sections", buildSections(normalizedGuide));
        module.put("sourceMeta", copyObject(normalizedGuide.optJSONObject("meta")));
        return module;
    }

    private static JSONArray buildSections(JSONObject normalizedGuide) throws JSONException {
        JSONArray sections = new JSONArray();
        sections.put(buildParagraphSection(
                "assessment_result",
                "评估结果",
                normalizedGuide.optString("overallSummary", "")));
        sections.put(buildBulletSection(
                "mastered_abilities",
                "本次评估中已掌握的能力",
                normalizedGuide.optJSONArray("mastered")));
        sections.put(buildParagraphSection(
                "not_mastered_overview",
                "未掌握能力的整体说明",
                normalizedGuide.optString("notMasteredOverview", "")));
        sections.put(buildBulletSection(
                "priority_focus",
                "需要重点关注的能力",
                normalizedGuide.optJSONArray("focus")));
        sections.put(buildBulletSection(
                "unstable_abilities",
                "不稳定的能力",
                normalizedGuide.optJSONArray("unstable")));

        JSONObject smartGoal = normalizedGuide.optJSONObject("smartGoal");
        String smartText = smartGoal == null ? "" : smartGoal.optString("text", "");
        sections.put(buildParagraphSection(
                "smart_goal",
                "干预目标（SMART）",
                smartText));
        sections.put(buildBulletSection(
                "home_guidance",
                "家庭干预指导建议",
                normalizedGuide.optJSONArray("homeGuidance")));
        return sections;
    }

    private static JSONObject buildParagraphSection(String key, String title, String text) throws JSONException {
        JSONObject section = new JSONObject();
        section.put("key", key);
        section.put("title", title);
        section.put("contentType", CONTENT_TYPE_PARAGRAPH);
        section.put("text", safeText(text));
        section.put("items", new JSONArray());
        return section;
    }

    private static JSONObject buildBulletSection(String key, String title, JSONArray sourceItems) throws JSONException {
        JSONObject section = new JSONObject();
        section.put("key", key);
        section.put("title", title);
        section.put("contentType", CONTENT_TYPE_BULLETS);
        section.put("text", joinItems(sourceItems));
        section.put("items", copyStringArray(sourceItems));
        return section;
    }

    private static JSONObject normalizeGuide(JSONObject childJson, String moduleType) {
        JSONObject guide = ModuleReportHelper.loadModuleInterventionGuide(childJson, moduleType);
        if (guide == null) {
            return null;
        }
        try {
            return ModuleInterventionGuideSchema.normalize(
                    guide,
                    moduleType,
                    ModuleReportHelper.moduleTitle(moduleType),
                    ModuleReportHelper.defaultSubtypes(moduleType));
        } catch (JSONException ignored) {
            return null;
        }
    }

    private static JSONObject copyObject(JSONObject source) throws JSONException {
        if (source == null) {
            return new JSONObject();
        }
        return new JSONObject(source.toString());
    }

    private static JSONArray copyStringArray(JSONArray sourceItems) {
        JSONArray out = new JSONArray();
        if (sourceItems == null) {
            return out;
        }
        for (int i = 0; i < sourceItems.length(); i++) {
            String text = safeText(sourceItems.optString(i, ""));
            if (!text.isEmpty()) {
                out.put(text);
            }
        }
        return out;
    }

    private static String joinItems(JSONArray sourceItems) {
        if (sourceItems == null || sourceItems.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sourceItems.length(); i++) {
            String text = safeText(sourceItems.optString(i, ""));
            if (text.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(text);
        }
        return sb.toString();
    }

    private static JSONObject optObject(JSONObject source, String key) {
        if (source == null || key == null) {
            return null;
        }
        Object value = source.opt(key);
        return value instanceof JSONObject ? (JSONObject) value : null;
    }

    private static String resolveReportDate(JSONObject info) {
        String[] candidates = new String[]{
                info == null ? "" : info.optString("testDate", ""),
                info == null ? "" : info.optString("reportDate", ""),
                info == null ? "" : info.optString("updatedAt", "")
        };
        for (String candidate : candidates) {
            String text = safeText(candidate);
            if (!text.isEmpty()) {
                return text;
            }
        }
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String text = safeText(value);
            if (!text.isEmpty()) {
                return text;
            }
        }
        return "";
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
