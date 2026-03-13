package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ModuleInterventionPromptBuilder {
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private ModuleInterventionPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return "你是儿童言语语言治疗干预报告助手。只输出严格合法 JSON，不要 Markdown。"
                + "必须使用简体中文值，JSON key 保持英文。";
    }

    public static String buildUserPrompt(String moduleType, JSONObject childData) throws JSONException {
        String safeModuleType = ModuleReportHelper.normalizeModuleType(moduleType);
        String moduleTitle = ModuleReportHelper.moduleTitle(safeModuleType);
        JSONArray subtypes = ModuleReportHelper.defaultSubtypes(safeModuleType);
        JSONObject info = childData == null ? null : childData.optJSONObject("info");
        JSONObject evaluations = childData == null ? null : childData.optJSONObject("evaluations");

        int ageMonths = calculateAgeMonths(
                info == null ? "" : info.optString("birthDate", ""),
                info == null ? "" : info.optString("testDate", ""));
        if (ageMonths < 0) {
            ageMonths = 0;
        }

        JSONObject moduleInput = new JSONObject();
        moduleInput.put("moduleType", safeModuleType);
        moduleInput.put("moduleTitle", moduleTitle);
        moduleInput.put("subtypes", subtypes);
        moduleInput.put("ageMonths", ageMonths);
        moduleInput.put("chiefComplaint", info == null ? "" : safe(info.optString("chiefComplaint", "")));
        moduleInput.put("moduleEvaluations", selectModuleEvaluations(safeModuleType, evaluations));
        moduleInput.put("existingModuleReport", loadCurrentModuleReport(childData, safeModuleType));

        JSONObject schema = ModuleInterventionGuideSchema.createDefault(safeModuleType, moduleTitle, subtypes);

        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下模块评测结构化数据，生成该模块的干预报告 JSON。");
        prompt.append("必须只输出 JSON，不要解释文本。\n");
        prompt.append("要求：\n");
        prompt.append("1) 输出字段必须与 schema 完全一致。\n");
        prompt.append("2) 字段允许为空，但结构必须稳定。\n");
        prompt.append("3) smartGoal.cycleWeeks 为正整数，accuracyThreshold 为 0~1 小数。\n");
        prompt.append("4) meta.reviewStatus 初始为 draft，reviewedByTherapist 初始为 false。\n");
        prompt.append("5) 输出中不要包含姓名、电话、地址等隐私信息。\n");

        // 模块特定的 Prompt 指令
        if ("articulation".equals(safeModuleType)) {
            prompt.append("6) [构音模块] 请在 custom.oralMotorSuggestions 字段中提供针对性的口肌训练建议（如唇舌操）。\n");
            prompt.append("7) [构音模块] 请将“未掌握能力的整体说明”拆解：\n");
            prompt.append("   - 尚未建立/完全不会的能力 -> 放入 'focus' 数组（对应【需要重点关注的能力】）；\n");
            prompt.append("   - 已出现但不稳定的能力 -> 放入 'unstable' 数组（对应【不稳定的能力】）；\n");
            prompt.append("   - notMasteredOverview 仅保留一句话的总体描述。\n");
        } else if ("social".equals(safeModuleType)) {
            prompt.append("6) [社交模块] 请在 custom.socialScripts 字段中提供2-3个具体的社交情境对话脚本。\n");
        }

        prompt.append("\n输入数据:\n").append(moduleInput.toString()).append("\n");
        prompt.append("\n输出 schema:\n").append(schema.toString()).append("\n");
        return prompt.toString();
    }

    private static JSONObject selectModuleEvaluations(String moduleType, JSONObject evaluations) throws JSONException {
        JSONObject out = new JSONObject();
        JSONObject source = evaluations == null ? new JSONObject() : evaluations;
        switch (moduleType) {
            case "articulation":
                out.put("A", cloneArray(source.optJSONArray("A")));
                out.put("speech_intelligibility", safe(source.optString("speech_intelligibility", "")));
                out.put("clinical_diagnosis", safe(source.optString("clinical_diagnosis", "")));
                out.put("assessment_suggestions", safe(source.optString("assessment_suggestions", "")));
                break;
            case "syntax":
                out.put("RG", cloneArray(source.optJSONArray("RG")));
                out.put("SE", cloneArray(source.optJSONArray("SE")));
                break;
            case "social":
                out.put("SOCIAL", cloneArray(source.optJSONArray("SOCIAL")));
                break;
            case "prelinguistic":
                out.put("PL", cloneArray(source.optJSONArray("PL")));
                break;
            case "vocabulary":
                out.put("E", cloneArray(source.optJSONArray("E")));
                out.put("EV", cloneArray(source.optJSONArray("EV")));
                out.put("RE", cloneArray(source.optJSONArray("RE")));
                out.put("S", cloneArray(source.optJSONArray("S")));
                out.put("NWR", cloneArray(source.optJSONArray("NWR")));
                break;
            default:
                out.put("raw", source);
                break;
        }
        return out;
    }

    private static JSONObject loadCurrentModuleReport(JSONObject childData, String moduleType) {
        if (childData == null) {
            return new JSONObject();
        }
        JSONObject report = ModuleReportHelper.loadModuleReport(childData, moduleType);
        return report == null ? new JSONObject() : report;
    }

    private static JSONArray cloneArray(JSONArray source) {
        JSONArray out = new JSONArray();
        if (source == null) {
            return out;
        }
        for (int i = 0; i < source.length(); i++) {
            out.put(source.opt(i));
        }
        return out;
    }

    private static int calculateAgeMonths(String birthDate, String testDate) {
        Date birth = parseDate(birthDate);
        if (birth == null) {
            return -1;
        }
        Date test = parseDate(testDate);
        if (test == null) {
            test = new Date();
        }
        Calendar b = Calendar.getInstance();
        b.setTime(birth);
        Calendar t = Calendar.getInstance();
        t.setTime(test);
        int months = (t.get(Calendar.YEAR) - b.get(Calendar.YEAR)) * 12
                + (t.get(Calendar.MONTH) - b.get(Calendar.MONTH));
        if (t.get(Calendar.DAY_OF_MONTH) < b.get(Calendar.DAY_OF_MONTH)) {
            months -= 1;
        }
        return months;
    }

    private static Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN, Locale.US).parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

