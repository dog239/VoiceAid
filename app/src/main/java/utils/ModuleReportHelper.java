package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ModuleReportHelper {
    private static final String DEFAULT_NO_DATA = "\u672a\u4f5c\u7b54";
    private static final String PRELINGUISTIC_KEY = "PL";
    private static final String SOCIAL_KEY = "SOCIAL";
    private static final String PRELINGUISTIC_OPTION_TEXT_1 = "\u7531\u4e8e\u5b69\u5b50\u7684\u751f\u7406\u5e74\u9f84\u548c\u5b9e\u9645\u7684\u6c9f\u901a\u80fd\u529b\u6c34\u5e73\u4e4b\u95f4\u5dee\u8ddd\u8f83\u5927\uff0c\u5b69\u5b50\u5728\u6c9f\u901a\u4e2d\u9891\u7e41\u5730\u53d7\u632b\u5207\u51fa\u73b0\u6cad\u4e27\uff0c\u751f\u6d3b\u8d28\u91cf\u53d7\u5230\u663e\u8457\u5f71\u54cd\uff0c\u5efa\u8bae\u7acb\u523b\u54a8\u8be2\u4e13\u4e1a\u7684\u8bed\u8a00\u6cbb\u7597\u5e08\u56e2\u961f\uff0c\u8fdb\u884c\u79d1\u5b66\u7cbe\u51c6\u5bc6\u96c6\u7684\u5e72\u9884\u8bad\u7ec3\u3002\u5efa\u8bae\u8bad\u7ec3\u9891\u6b21\u4e0d\u4f4e\u4e8e3/4/5\u6b21\u6bcf\u5468\uff0c\u6bcf\u5929\u4e0d\u5c11\u4e8e30/45/60\u5206\u949f\u3002";
    private static final String PRELINGUISTIC_OPTION_TEXT_2 = "\u7531\u4e8e\u5b69\u5b50\u5f53\u524d\u751f\u7406\u5e74\u9f84\u548c\u9884\u671f\u80fd\u529b\u6c34\u5e73\u4e4b\u95f4\u5dee\u8ddd\u4e0d\u663e\u8457\uff0c\u5efa\u8bae\u5bb6\u957f\u6839\u636e\u4ee5\u4e0b\u8981\u70b9\uff0c\u6bcf\u5929\u5f00\u5c55\u4e0d\u4f4e\u4e8e30/45\u5206\u949f\u7684\u9ad8\u8d28\u91cf\u5bb6\u5ead\u4eb2\u5b50\u4e92\u52a8\u3002\u68c0\u6d4b\u8fdb\u5c55\u0031/2/3\u4e2a\u6708\u540e\uff0c\u82e5\u65e0\u663e\u8457\u6539\u5584\uff0c\u7acb\u5373\u8054\u7cfb\u4e13\u4e1a\u7684\u8bed\u8a00\u6cbb\u7597\u5e08\u56e2\u961f\u8fdb\u884c\u79d1\u5b66\u5e72\u9884\u3002";

    private ModuleReportHelper() {
    }

    public static void applyModuleFindings(JSONObject plan, JSONObject evaluations) {
        applyModuleFindings(plan, evaluations, null);
    }

    public static void applyModuleFindings(JSONObject plan, JSONObject evaluations, JSONObject childData) {
        if (plan == null || evaluations == null) {
            return;
        }
        try {
            JSONObject modulePlan = plan.optJSONObject("module_plan");
            if (modulePlan == null) {
                modulePlan = new JSONObject();
                plan.put("module_plan", modulePlan);
            }
            applyFindings(modulePlan, "speech_sound", buildSpeechSoundFindings(evaluations, evaluations.optJSONArray("A")));
            JSONObject prelinguisticReport = childData == null ? null : loadPrelinguisticReport(childData);
            applyFindings(modulePlan, "prelinguistic", buildPrelinguisticFindings(evaluations, prelinguisticReport));
            applyFindings(modulePlan, "vocabulary", buildVocabularyFindings(evaluations));
            applyFindings(modulePlan, "syntax", buildSyntaxFindings(evaluations));
            applyFindings(modulePlan, "social_pragmatics", buildSocialFindings(evaluations));
        } catch (JSONException ignored) {
        }
    }

    private static void applyFindings(JSONObject modulePlan, String moduleKey, List<String> findings) throws JSONException {
        if (modulePlan == null || moduleKey == null) {
            return;
        }
        JSONObject module = modulePlan.optJSONObject(moduleKey);
        if (module == null) {
            module = new JSONObject();
            modulePlan.put(moduleKey, module);
        }
        JSONArray arr = new JSONArray();
        if (findings == null || findings.isEmpty()) {
            arr.put(DEFAULT_NO_DATA);
        } else {
            for (String item : findings) {
                String text = safeText(item);
                if (!text.isEmpty()) {
                    arr.put(text);
                }
            }
            if (arr.length() == 0) {
                arr.put(DEFAULT_NO_DATA);
            }
        }
        module.put("key_findings", arr);
    }

    private static List<String> buildSpeechSoundFindings(JSONObject evaluations, JSONArray evaluationsA) {
        SpeechSoundClinical clinical = extractSpeechSoundClinical(evaluations, evaluationsA);
        InitialAccuracy accuracy = calcInitialAccuracy(evaluationsA);
        String intelligibility = safeText(clinical.intelligibility);
        if (isMissingValue(intelligibility)) {
            String fallback = mapIntelligibilityLevel(accuracy);
            if (!fallback.isEmpty()) {
                intelligibility = fallback;
            }
        }
        boolean hasAny = !isMissingValue(intelligibility)
                || accuracy.total > 0
                || !isMissingValue(clinical.diagnosis)
                || !isMissingValue(clinical.suggestions);
        if (!hasAny) {
            return new ArrayList<>();
        }
        String intelligibilityText = normalizeRequiredValue(intelligibility);
        String accuracyText = formatInitialAccuracy(accuracy);
        String diagnosisText = normalizeRequiredValue(clinical.diagnosis);
        String suggestionText = normalizeRequiredValue(clinical.suggestions);
        List<String> list = new ArrayList<>();
        list.add("语音清晰度等级：" + intelligibilityText);
        list.add("声母正确率：" + accuracyText);
        list.add("诊断结果：" + diagnosisText);
        list.add("语音评估建议：" + suggestionText);
        return list;
    }

    private static List<String> buildPrelinguisticFindings(JSONObject evaluations, JSONObject report) {
        PreLinguisticDataParser.PreLinguisticHeader header = PreLinguisticDataParser.parseHeader(evaluations);
        String suggestionOverride = resolvePrelinguisticSuggestionText(report);
        String text = formatPrelinguisticHeader(header, suggestionOverride);
        if (safeText(text).isEmpty()) {
            return toList(DEFAULT_NO_DATA);
        }
        return toList(text);
    }

    private static List<String> buildVocabularyFindings(JSONObject evaluations) {
        String summary = TreatmentPromptBuilder.buildVocabularySummary(evaluations);
        return toList(summary);
    }

    private static List<String> buildSyntaxFindings(JSONObject evaluations) {
        String summary = TreatmentPromptBuilder.buildSyntaxSummary(evaluations);
        return toList(summary);
    }

    private static List<String> buildSocialFindings(JSONObject evaluations) {
        String summary = TreatmentPromptBuilder.buildNarrativeSummary(evaluations);
        return toList(summary);
    }

    private static String formatPrelinguisticHeader(PreLinguisticDataParser.PreLinguisticHeader header,
                                                    String suggestionOverride) {
        if (header == null || header.dimensions.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (PreLinguisticDataParser.DimensionResult result : header.dimensions) {
            if (!shouldIncludeDimension(result)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(result.name).append("\uff1a").append(result.status);
            if (result.total > 0) {
                builder.append("\uff08").append(result.achieved).append("/").append(result.total).append("\uff09");
            }
        }
        String diagnosis = normalizeOptionalValue(header.diagnosisText);
        if (!diagnosis.isEmpty()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("\u8bca\u65ad/\u7ed3\u8bba\uff1a").append(diagnosis);
        }
        String suggestions = safeText(suggestionOverride);
        if (suggestions.isEmpty()) {
            suggestions = normalizeRequiredValue(header.suggestions);
        }
        if (builder.length() > 0) {
            builder.append("\n");
        }
        builder.append("\u8bc4\u4f30\u5efa\u8bae\uff1a").append(suggestions);
        return builder.toString();
    }

    private static boolean shouldIncludeDimension(PreLinguisticDataParser.DimensionResult result) {
        if (result == null || result.name == null || result.name.trim().isEmpty()) {
            return false;
        }
        return !("\u5176\u4ed6".equals(result.name) && result.total == 0);
    }

    private static List<String> toList(String value) {
        List<String> list = new ArrayList<>();
        String text = safeText(value);
        if (text.isEmpty()) {
            list.add(DEFAULT_NO_DATA);
            return list;
        }
        list.add(text);
        return list;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isMissingValue(String value) {
        if (value == null) {
            return true;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed);
    }

    private static String normalizeRequiredValue(String value) {
        String text = safeText(value);
        return isMissingValue(text) ? DEFAULT_NO_DATA : text;
    }

    private static String normalizeOptionalValue(String value) {
        String text = safeText(value);
        return isMissingValue(text) ? "" : text;
    }

    private static SpeechSoundClinical extractSpeechSoundClinical(JSONObject evaluations, JSONArray evaluationsA) {
        SpeechSoundClinical clinical = new SpeechSoundClinical();
        ArticulationDataParser.ClinicalHeader header = ArticulationDataParser.parseClinicalHeader(evaluations);
        if (header != null) {
            clinical.intelligibility = safeText(header.intelligibilityLevel);
            clinical.diagnosis = safeText(header.diagnosisText);
            clinical.suggestions = safeText(header.suggestions);
        }
        SpeechSoundClinical fromArray = extractSpeechSoundClinical(evaluationsA);
        if (isMissingValue(clinical.intelligibility)) {
            clinical.intelligibility = fromArray.intelligibility;
        }
        if (isMissingValue(clinical.diagnosis)) {
            clinical.diagnosis = fromArray.diagnosis;
        }
        if (isMissingValue(clinical.suggestions)) {
            clinical.suggestions = fromArray.suggestions;
        }
        return clinical;
    }

    private static SpeechSoundClinical extractSpeechSoundClinical(JSONArray evaluationsA) {
        SpeechSoundClinical clinical = new SpeechSoundClinical();
        if (evaluationsA == null) {
            return clinical;
        }
        for (int i = 0; i < evaluationsA.length(); i++) {
            JSONObject obj = evaluationsA.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            if (isMissingValue(clinical.intelligibility)) {
                String value = safeText(obj.optString("speech_intelligibility", ""));
                if (isMissingValue(value)) {
                    value = safeText(obj.optString("intelligibility_level", ""));
                }
                if (!isMissingValue(value)) {
                    clinical.intelligibility = value;
                }
            }
            if (isMissingValue(clinical.diagnosis)) {
                String value = safeText(obj.optString("clinical_diagnosis", ""));
                if (isMissingValue(value)) {
                    value = safeText(obj.optString("diagnosis_text", ""));
                }
                if (!isMissingValue(value)) {
                    clinical.diagnosis = value;
                }
            }
            if (isMissingValue(clinical.suggestions)) {
                String value = safeText(obj.optString("assessment_suggestions", ""));
                if (isMissingValue(value)) {
                    value = safeText(obj.optString("suggestions", ""));
                }
                if (isMissingValue(value)) {
                    value = safeText(obj.optString("clinical_suggestions", ""));
                }
                if (!isMissingValue(value)) {
                    clinical.suggestions = value;
                }
            }
        }
        return clinical;
    }

    private static InitialAccuracy calcInitialAccuracy(JSONArray evaluationsA) {
        InitialAccuracy accuracy = new InitialAccuracy();
        if (evaluationsA == null) {
            return accuracy;
        }
        for (int i = 0; i < evaluationsA.length(); i++) {
            JSONObject obj = evaluationsA.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            JSONArray targets = obj.optJSONArray("targetWord");
            if (targets == null) {
                continue;
            }
            JSONArray answers = obj.optJSONArray("answerPhonology");
            for (int idx = 0; idx < targets.length(); idx++) {
                JSONObject target = targets.optJSONObject(idx);
                String targetInitial = safeLower(extractInitial(target));
                if (targetInitial.isEmpty()) {
                    continue;
                }
                accuracy.total++;
                JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                String answerInitial = safeLower(extractInitial(answer));
                if (!answerInitial.isEmpty() && targetInitial.equals(answerInitial)) {
                    accuracy.correct++;
                }
            }
        }
        return accuracy;
    }

    private static String extractInitial(JSONObject characterPhonology) {
        if (characterPhonology == null) {
            return "";
        }
        JSONObject phonology = characterPhonology.optJSONObject("phonology");
        if (phonology == null) {
            return "";
        }
        return safeText(phonology.optString("initial", ""));
    }

    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(java.util.Locale.getDefault());
    }

    private static String formatInitialAccuracy(InitialAccuracy accuracy) {
        if (accuracy == null || accuracy.total <= 0) {
            return DEFAULT_NO_DATA;
        }
        double percent = accuracy.correct * 100.0d / accuracy.total;
        return String.format(java.util.Locale.getDefault(), "%.2f%%（%d/%d）", percent, accuracy.correct, accuracy.total);
    }

    private static String mapIntelligibilityLevel(InitialAccuracy accuracy) {
        if (accuracy == null || accuracy.total <= 0) {
            return "";
        }
        double rate = accuracy.correct * 1.0d / accuracy.total;
        if (rate >= 0.85d) {
            return "轻度";
        }
        if (rate >= 0.65d) {
            return "轻中度";
        }
        if (rate >= 0.50d) {
            return "中重度";
        }
        return "重度";
    }

    private static final class SpeechSoundClinical {
        String intelligibility = "";
        String diagnosis = "";
        String suggestions = "";
    }

    private static final class InitialAccuracy {
        int total;
        int correct;
    }

    public static JSONObject loadPrelinguisticReport(JSONObject childData) {
        if (childData == null) {
            return null;
        }
        JSONObject reports = optJSONObjectFlexible(childData, "moduleReports");
        if (reports == null) {
            reports = optJSONObjectFlexible(childData, "module_reports");
        }
        if (reports == null) {
            reports = optJSONObjectFlexible(childData, "reports");
        }
        if (reports == null) {
            return null;
        }
        JSONObject report = optJSONObjectFlexible(reports, PRELINGUISTIC_KEY);
        if (report == null) {
            report = optJSONObjectFlexible(reports, "prelinguistic");
        }
        return report;
    }

    public static JSONObject savePrelinguisticReport(JSONObject childData, JSONObject report) throws JSONException {
        if (childData == null) {
            return report;
        }
        JSONObject reports = optJSONObjectFlexible(childData, "moduleReports");
        if (reports == null) {
            reports = new JSONObject();
            childData.put("moduleReports", reports);
        }
        reports.put(PRELINGUISTIC_KEY, report == null ? new JSONObject() : report);
        return report;
    }

    public static JSONObject buildPrelinguisticReport(String scene, int totalScore, List<String> strengths, List<String> weaknesses) {
        JSONObject report = new JSONObject();
        try {
            report.put("scene", safeText(scene));
            report.put("totalScore", totalScore);
            report.put("strengths", toJsonArray(strengths));
            report.put("weaknesses", toJsonArray(weaknesses));
        } catch (JSONException ignored) {
        }
        return report;
    }

    public static String buildPrelinguisticSummaryText(List<String> strengths, List<String> weaknesses) {
        List<String> safeStrengths = strengths == null ? new ArrayList<>() : strengths;
        List<String> safeWeaknesses = weaknesses == null ? new ArrayList<>() : weaknesses;
        String strengthsText = joinWithSeparator(safeStrengths, "\u3001");
        String weaknessesText = joinWithSeparator(safeWeaknesses, "\u3001");
        String weakOne = safeWeaknesses.isEmpty() ? "" : safeText(safeWeaknesses.get(0));
        if (safeStrengths.isEmpty()) {
            return String.format("\u8bed\u524d\u6280\u80fd\u8bc4\u4f30\u7ed3\u679c\u663e\u793a\uff0c\u5b69\u5b50\u8bed\u524d\u6280\u80fd\u76ee\u524d\u6574\u4f53\u8f83\u5f31\uff0c\u5c24\u5176\u5728%s\u65b9\u9762\u4e0d\u8db3\uff0c\u8bc4\u4f30\u663e\u793a%s\u672a\u5145\u5206\u53d1\u5c55\uff0c\u5728\u4fc3\u8fdb\u540e\u53ef\u4ee5\u4f7f\u7528\u3002",
                    weaknessesText, weakOne);
        }
        if (safeWeaknesses.isEmpty()) {
            return String.format("\u8bed\u524d\u6280\u80fd\u8bc4\u4f30\u7ed3\u679c\u663e\u793a\uff0c\u5b69\u5b50\u8bed\u524d\u6280\u80fd\u5728%s\u65b9\u9762\u8f83\u5f3a\u3002\u76ee\u524d\u672a\u89c1\u660e\u663e\u8584\u5f31\u9879\uff0c\u5efa\u8bae\u7ee7\u7eed\u5728\u81ea\u7136\u4e92\u52a8\u4e2d\u5de9\u56fa\u4e0e\u6cdb\u5316\u3002",
                    strengthsText);
        }
        return String.format("\u8bed\u524d\u6280\u80fd\u8bc4\u4f30\u7ed3\u679c\u663e\u793a\uff0c\u5b69\u5b50\u8bed\u524d\u6280\u80fd\u5728%s\u65b9\u9762\u8f83\u5f3a\u3002\u4f46\u662f\uff0c\u8be5\u5c0f\u670b\u53cb\u5728%s\u65b9\u9762\u8f83\u5f31\uff0c\u8bc4\u4f30\u663e\u793a%s\u672a\u5145\u5206\u53d1\u5c55\uff0c\u5728\u4fc3\u8fdb\u540e\u53ef\u4ee5\u4f7f\u7528\u3002",
                strengthsText, weaknessesText, weakOne);
    }

    public static String buildPrelinguisticSuggestionText(int option) {
        return getPrelinguisticSuggestionOptionText(option);
    }

    public static String getPrelinguisticSuggestionOptionText(int option) {
        return option == 2 ? PRELINGUISTIC_OPTION_TEXT_2 : PRELINGUISTIC_OPTION_TEXT_1;
    }

    public static JSONArray buildPrelinguisticSuggestionOptionsArray() {
        JSONArray options = new JSONArray();
        options.put(PRELINGUISTIC_OPTION_TEXT_1);
        options.put(PRELINGUISTIC_OPTION_TEXT_2);
        return options;
    }

    public static JSONObject buildPrelinguisticSuggestionParams(int freqPerWeek, int minutesPerDay,
                                                                int parentMinutesPerDay, int followupMonths) {
        JSONObject params = new JSONObject();
        try {
            params.put("freqPerWeek", freqPerWeek);
            params.put("minutesPerDay", minutesPerDay);
            params.put("parentMinutesPerDay", parentMinutesPerDay);
            params.put("followupMonths", followupMonths);
        } catch (JSONException ignored) {
        }
        return params;
    }

    private static String resolvePrelinguisticSuggestionText(JSONObject report) {
        if (report == null) {
            return "";
        }
        int option = report.optInt("suggestionOption", 1);
        JSONArray options = report.optJSONArray("suggestionOptions");
        if (options != null && options.length() >= 2) {
            String text = safeText(options.optString(option == 2 ? 1 : 0, ""));
            if (!text.isEmpty()) {
                return text;
            }
        }
        String text = safeText(report.optString("suggestionText", ""));
        if (!text.isEmpty()) {
            return text;
        }
        return getPrelinguisticSuggestionOptionText(option);
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

    private static String joinWithSeparator(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String text = safeText(value);
            if (text.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(text);
        }
        return builder.toString();
    }

    public static JSONObject loadSocialReport(JSONObject childData) {
        if (childData == null) {
            return null;
        }
        JSONObject reports = optJSONObjectFlexible(childData, "moduleReports");
        if (reports == null) {
            reports = optJSONObjectFlexible(childData, "module_reports");
        }
        if (reports == null) {
            reports = optJSONObjectFlexible(childData, "reports");
        }
        if (reports == null) {
            return null;
        }
        JSONObject report = optJSONObjectFlexible(reports, SOCIAL_KEY);
        if (report == null) {
            report = optJSONObjectFlexible(reports, "social");
        }
        return report;
    }

    public static JSONObject saveSocialReport(JSONObject childData, JSONObject report) throws JSONException {
        if (childData == null) {
            return report;
        }
        JSONObject reports = optJSONObjectFlexible(childData, "moduleReports");
        if (reports == null) {
            reports = new JSONObject();
            childData.put("moduleReports", reports);
        }
        reports.put(SOCIAL_KEY, report == null ? new JSONObject() : report);
        return report;
    }

    public static JSONObject buildSocialReport(int totalScore, List<String> strengths, List<String> inProgress, List<String> weaknesses) {
        JSONObject report = new JSONObject();
        try {
            report.put("totalScore", totalScore);
            report.put("strengths", toJsonArray(strengths));
            report.put("inProgress", toJsonArray(inProgress));
            report.put("weaknesses", toJsonArray(weaknesses));
            report.put("overallAssessment", getSocialOverallAssessment(totalScore));
            report.put("recommendations", getSocialRecommendations(strengths, inProgress, weaknesses));
        } catch (JSONException ignored) {
        }
        return report;
    }

    private static String getSocialOverallAssessment(int totalScore) {
        // 计算总分，60题，每题最高2分，满分120分
        double percentage = totalScore / 120.0 * 100;
        if (percentage >= 80) {
            return "从整体上来说，孩子的社交能力较好，基本达标。";
        } else {
            return "从整体上来说，孩子的社交能力还有待进一步发展，尚未达标。";
        }
    }

    private static String getSocialRecommendations(List<String> strengths, List<String> inProgress, List<String> weaknesses) {
        StringBuilder recommendations = new StringBuilder();
        
        if (!weaknesses.isEmpty()) {
            recommendations.append("1.需要重点关注：\n");
            recommendations.append(joinWithSeparator(weaknesses, "\n"));
            recommendations.append("\n\n");
        }
        
        if (!inProgress.isEmpty()) {
            recommendations.append("2.有些社交能力已经表现出来，但是不够经常，我们可以多去发展以下能力：\n");
            recommendations.append(joinWithSeparator(inProgress, "\n"));
            recommendations.append("\n\n");
        }
        
        return recommendations.toString();
    }
}
