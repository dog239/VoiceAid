package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModuleReportHelper {
    private static final String DEFAULT_NO_DATA = "\u672a\u4f5c\u7b54";
    private static final String PRELINGUISTIC_KEY = "PL";
    private static final String SOCIAL_KEY = "SOCIAL";
    private static final int[] RG_GROUP_LENGTHS = new int[]{21, 18, 18, 15};
    private static final String PRELINGUISTIC_OPTION_TEXT_1 = "\u7531\u4e8e\u5b69\u5b50\u7684\u751f\u7406\u5e74\u9f84\u548c\u5b9e\u9645\u7684\u6c9f\u901a\u80fd\u529b\u6c34\u5e73\u4e4b\u95f4\u5dee\u8ddd\u8f83\u5927\uff0c\u5b69\u5b50\u5728\u6c9f\u901a\u4e2d\u9891\u7e41\u5730\u53d7\u632b\u5207\u51fa\u73b0\u6cad\u4e27\uff0c\u751f\u6d3b\u8d28\u91cf\u53d7\u5230\u663e\u8457\u5f71\u54cd\uff0c\u5efa\u8bae\u7acb\u523b\u54a8\u8be2\u4e13\u4e1a\u7684\u8bed\u8a00\u6cbb\u7597\u5e08\u56e2\u961f\uff0c\u8fdb\u884c\u79d1\u5b66\u7cbe\u51c6\u5bc6\u96c6\u7684\u5e72\u9884\u8bad\u7ec3\u3002\u5efa\u8bae\u8bad\u7ec3\u9891\u6b21\u4e0d\u4f4e\u4e8e3/4/5\u6b21\u6bcf\u5468\uff0c\u6bcf\u5929\u4e0d\u5c11\u4e8e30/45/60\u5206\u949f\u3002";
    private static final String PRELINGUISTIC_OPTION_TEXT_2 = "\u7531\u4e8e\u5b69\u5b50\u5f53\u524d\u751f\u7406\u5e74\u9f84\u548c\u9884\u671f\u80fd\u529b\u6c34\u5e73\u4e4b\u95f4\u5dee\u8ddd\u4e0d\u663e\u8457\uff0c\u5efa\u8bae\u5bb6\u957f\u6839\u636e\u4ee5\u4e0b\u8981\u70b9\uff0c\u6bcf\u5929\u5f00\u5c55\u4e0d\u4f4e\u4e8e30/45\u5206\u949f\u7684\u9ad8\u8d28\u91cf\u5bb6\u5ead\u4eb2\u5b50\u4e92\u52a8\u3002\u68c0\u6d4b\u8fdb\u5c55\u0031/2/3\u4e2a\u6708\u540e\uff0c\u82e5\u65e0\u663e\u8457\u6539\u5584\uff0c\u7acb\u5373\u8054\u7cfb\u4e13\u4e1a\u7684\u8bed\u8a00\u6cbb\u7597\u5e08\u56e2\u961f\u8fdb\u884c\u79d1\u5b66\u5e72\u9884\u3002";

    private ModuleReportHelper() {
    }

    public static String normalizeModuleType(String moduleType) {
        String key = safeText(moduleType).toLowerCase();
        if (key.isEmpty()) {
            return "articulation";
        }
        switch (key) {
            case "a":
            case "speech_sound":
                return "articulation";
            case "rg":
            case "se":
                return "syntax";
            case "social_pragmatics":
            case "social":
                return "social";
            case "pl":
            case "pre_linguistic":
                return "prelinguistic";
            case "e":
            case "ev":
            case "re":
            case "s":
            case "nwr":
                return "vocabulary";
            default:
                return key;
        }
    }

    public static String moduleTitle(String moduleType) {
        switch (normalizeModuleType(moduleType)) {
            case "articulation":
                return "构音模块";
            case "syntax":
                return "句法模块";
            case "social":
                return "社交模块";
            case "prelinguistic":
                return "前语言模块";
            case "vocabulary":
                return "词汇模块";
            default:
                return "干预模块";
        }
    }

    public static JSONArray defaultSubtypes(String moduleType) {
        String key = normalizeModuleType(moduleType);
        JSONArray out = new JSONArray();
        if ("syntax".equals(key)) {
            out.put("RG");
            out.put("SE");
        } else if ("vocabulary".equals(key)) {
            out.put("expressive");
            out.put("receptive");
        }
        return out;
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
        PreLinguisticDataParser.PreLinguisticHeader header = PreLinguisticDataParser.parseHeader(normalizePrelinguisticEvaluations(evaluations));
        String suggestionOverride = resolvePrelinguisticSuggestionText(report);
        String text = formatPrelinguisticHeader(header, suggestionOverride);
        if (safeText(text).isEmpty()) {
            return toList(DEFAULT_NO_DATA);
        }
        return toList(text);
    }

    private static List<String> buildVocabularyFindings(JSONObject evaluations) {
        VocabularyAbilityAnalyzer.Summary summary = VocabularyAbilityAnalyzer.summarize(evaluations);
        List<String> findings = new ArrayList<>();
        String overall = safeText(summary.overallSummaryHint);
        if (!overall.isEmpty()) {
            findings.add("词汇整体：" + overall);
        }
        if (!summary.mastered.isEmpty()) {
            findings.add("词汇已掌握：" + joinWithSeparator(summary.mastered, "；"));
        }
        if (!summary.focus.isEmpty()) {
            findings.add("词汇重点关注：" + joinWithSeparator(summary.focus, "；"));
        }
        if (!summary.unstable.isEmpty()) {
            findings.add("词汇不稳定能力：" + joinWithSeparator(summary.unstable, "；"));
        }
        if (findings.isEmpty()) {
            findings.add(DEFAULT_NO_DATA);
        }
        return findings;
    }

    private static List<String> buildSyntaxFindings(JSONObject evaluations) {
        String summary = buildSyntaxSummary(evaluations);
        return toList(summary);
    }

    static String buildSyntaxSummary(JSONObject evaluations) {
        String rgSummary = buildRGSummary(evaluations);
        String seSummary = buildSESummary(evaluations);
        return "语法能力综合评估与建议：\n\n" + rgSummary + seSummary;
    }

    private static String buildRGSummary(JSONObject evaluations) {
        JSONArray rgArray = evaluations != null ? evaluations.optJSONArray("RG") : null;
        int correct = 0;
        int total = 0;
        if (rgArray != null) {
            for (int i = 0; i < rgArray.length(); i++) {
                JSONObject obj = rgArray.optJSONObject(i);
                if (obj != null && obj.has("result") && !obj.isNull("result")) {
                    total++;
                    if (obj.optBoolean("result")) {
                        correct++;
                    }
                }
            }
        }
        double accuracy = total > 0 ? (correct * 100.0 / total) : 0;
        String accuracyStr = String.format(java.util.Locale.getDefault(), "%.2f%%", accuracy);
        
        StringBuilder builder = new StringBuilder();
        builder.append("句法理解（RG）评估结果：\n");
        builder.append("共").append(total).append("题，正确").append(correct).append("题，正确率：").append(accuracyStr).append("\n");
        builder.append("评估建议：\n");
        
        if (accuracy >= 90) {
            builder.append("- 句法理解能力优秀，能够理解复杂的句子结构和语法规则\n");
            builder.append("- 建议继续丰富语言输入，增加阅读量，提高语言理解的深度和广度\n");
            builder.append("- 可以尝试理解更复杂的逻辑关系和抽象概念\n");
        } else if (accuracy >= 70) {
            builder.append("- 句法理解能力良好，能够理解基本的句子结构和语法规则\n");
            builder.append("- 建议加强对复杂句式的理解练习，如复合句、被动句等\n");
            builder.append("- 可以通过阅读故事书、听故事等方式提高语言理解能力\n");
        } else if (accuracy >= 50) {
            builder.append("- 句法理解能力一般，能够理解简单的句子结构\n");
            builder.append("- 建议从基础句式开始练习，如简单句、疑问句等\n");
            builder.append("- 可以通过图片配对、实物指认等方式加强语言理解\n");
        } else {
            builder.append("- 句法理解能力较弱，需要加强基础语言理解练习\n");
            builder.append("- 建议从单词和简单短语开始，逐步过渡到简单句子\n");
            builder.append("- 可以通过日常生活中的简单指令、重复练习等方式提高理解能力\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    private static String buildSESummary(JSONObject evaluations) {
        JSONArray seArray = evaluations != null ? evaluations.optJSONArray("SE") : null;
        int correct = 0;
        int total = 0;
        if (seArray != null) {
            for (int i = 0; i < seArray.length(); i++) {
                JSONObject obj = seArray.optJSONObject(i);
                if (obj != null && obj.has("result") && !obj.isNull("result")) {
                    total++;
                    if (obj.optBoolean("result")) {
                        correct++;
                    }
                }
            }
        }
        double accuracy = total > 0 ? (correct * 100.0 / total) : 0;
        String accuracyStr = String.format(java.util.Locale.getDefault(), "%.2f%%", accuracy);
        
        StringBuilder builder = new StringBuilder();
        builder.append("句法表达（SE）评估结果：\n");
        builder.append("共").append(total).append("题，正确").append(correct).append("题，正确率：").append(accuracyStr).append("\n");
        builder.append("评估建议：\n");
        
        if (accuracy >= 90) {
            builder.append("- 句法表达能力优秀，能够使用复杂的句子结构和语法规则\n");
            builder.append("- 建议继续丰富词汇量，提高语言表达的准确性和流畅性\n");
            builder.append("- 可以尝试表达更复杂的思想和情感，提高语言表达的深度\n");
        } else if (accuracy >= 70) {
            builder.append("- 句法表达能力良好，能够使用基本的句子结构和语法规则\n");
            builder.append("- 建议加强对复杂句式的表达练习，如复合句、被动句等\n");
            builder.append("- 可以通过复述故事、描述图片等方式提高语言表达能力\n");
        } else if (accuracy >= 50) {
            builder.append("- 句法表达能力一般，能够使用简单的句子结构\n");
            builder.append("- 建议从基础句式开始练习，如简单句、疑问句等\n");
            builder.append("- 可以通过模仿说话、重复练习等方式加强语言表达\n");
        } else {
            builder.append("- 句法表达能力较弱，需要加强基础语言表达练习\n");
            builder.append("- 建议从单词和简单短语开始，逐步过渡到简单句子\n");
            builder.append("- 可以通过日常生活中的简单对话、模仿发音等方式提高表达能力\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    private static List<String> buildSocialFindings(JSONObject evaluations) {
        try {
            SocialAbilitySummarizer.Summary summary = SocialAbilitySummarizer.summarize(
                    evaluations == null ? null : evaluations.optJSONArray(SOCIAL_KEY));
            List<String> findings = new ArrayList<>();
            if (!summary.focus.isEmpty()) {
                findings.add("\u793e\u4ea4\u91cd\u70b9\u5173\u6ce8\uff1a" + joinWithSeparator(summary.focus, "\u3001"));
            }
            if (!summary.unstable.isEmpty()) {
                findings.add("\u793e\u4ea4\u4e0d\u7a33\u5b9a\u80fd\u529b\uff1a" + joinWithSeparator(summary.unstable, "\u3001"));
            }
            if (findings.isEmpty()) {
                findings.add(summary.overallSummaryHint.isEmpty()
                        ? "\u793e\u4ea4\u6a21\u5757\u6682\u65e0\u53ef\u89e3\u91ca\u7684\u5df2\u6d4b\u7ed3\u679c"
                        : summary.overallSummaryHint);
            }
            return findings;
        } catch (Exception ignored) {
            String summary = TreatmentPromptBuilder.buildNarrativeSummary(evaluations);
            return toList(summary);
        }
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

    static SpeechSoundClinical extractSpeechSoundClinical(JSONObject evaluations, JSONArray evaluationsA) {
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

    static final class SpeechSoundClinical {
        String intelligibility = "";
        String diagnosis = "";
        String suggestions = "";
    }

    static final class InitialAccuracy {
        int total;
        int correct;
    }

    public static JSONObject getArticulationSummaryJson(JSONObject evaluations) throws JSONException {
        JSONObject summary = new JSONObject();
        JSONArray evaluationsA = evaluations != null ? evaluations.optJSONArray("A") : null;
        SpeechSoundClinical clinical = extractSpeechSoundClinical(evaluations, evaluationsA);
        InitialAccuracy accuracy = calcInitialAccuracy(evaluationsA);
        ArticulationDataParser.PhonemeSummary phonemeSummary = ArticulationDataParser.parsePhonemeSummary(evaluationsA);
        ArticulationProfile profile = buildArticulationProfile(evaluationsA, clinical, phonemeSummary, accuracy);

        summary.put("intelligibility", clinical.intelligibility);
        summary.put("diagnosis", clinical.diagnosis);
        summary.put("suggestions", clinical.suggestions);

        JSONObject accObj = new JSONObject();
        accObj.put("total", accuracy.total);
        accObj.put("correct", accuracy.correct);
        if (accuracy.total > 0) {
            accObj.put("accuracy", String.format(Locale.US, "%.2f%%", accuracy.correct * 100.0 / accuracy.total));
        }
        summary.put("initial_consonant_accuracy", accObj);

        JSONObject phonemeObj = new JSONObject();
        phonemeObj.put("mastered", toJsonArray(phonemeSummary.masteredPhonemes));
        phonemeObj.put("targets", toJsonArray(phonemeSummary.targetPhonemes));
        summary.put("phonemeSummary", phonemeObj);
        summary.put("articulationProfile", buildArticulationProfileJson(profile));
        return summary;
    }

    private static JSONObject buildArticulationProfileJson(ArticulationProfile profile) throws JSONException {
        JSONObject out = new JSONObject();
        out.put("totalItems", profile.totalItems);
        out.put("completedItems", profile.completedItems);
        out.put("syllableComparisons", profile.totalSyllables);
        out.put("correctSyllables", profile.correctSyllables);
        out.put("overallLevel", profile.overallLevel);
        out.put("masteredPhonemes", toJsonArray(profile.masteredPhonemes));
        out.put("focusPhonemes", toJsonArray(profile.focusPhonemes));
        out.put("unstablePhonemes", toJsonArray(profile.unstablePhonemes));
        out.put("observedPhonemes", toJsonArray(profile.observedPhonemes));
        out.put("wordPositionStable", toJsonArray(profile.wordPositionStable));
        out.put("wordPositionFocus", toJsonArray(profile.wordPositionFocus));
        out.put("wordPositionUnstable", toJsonArray(profile.wordPositionUnstable));
        out.put("multisyllabicRetention", profile.multisyllabicRetention);
        out.put("connectedSpeechCarryover", profile.connectedSpeechCarryover);
        out.put("phonologyProcesses", toJsonArray(profile.phonologyProcesses));
        out.put("extractionNote", profile.extractionNote);
        return out;
    }

    private static ArticulationProfile buildArticulationProfile(JSONArray evaluationsA,
                                                               SpeechSoundClinical clinical,
                                                               ArticulationDataParser.PhonemeSummary phonemeSummary,
                                                               InitialAccuracy accuracy) {
        ArticulationProfile profile = new ArticulationProfile();
        Map<String, StructureScore> phonemeScores = new LinkedHashMap<>();
        Map<String, StructureScore> positionScores = new LinkedHashMap<>();
        boolean hasSpeechLevelEvidence = false;

        if (evaluationsA != null) {
            for (int i = 0; i < evaluationsA.length(); i++) {
                JSONObject item = evaluationsA.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                JSONArray targets = item.optJSONArray("targetWord");
                JSONArray answers = item.optJSONArray("answerPhonology");
                if (targets == null || targets.length() == 0) {
                    continue;
                }
                profile.totalItems++;
                boolean comparedAny = false;
                boolean itemAllCorrect = true;
                int comparableCount = 0;
                for (int idx = 0; idx < targets.length(); idx++) {
                    JSONObject target = targets.optJSONObject(idx);
                    String targetInitial = normalizeArticulationPhoneme(extractInitial(target));
                    if (targetInitial.isEmpty()) {
                        continue;
                    }
                    comparableCount++;
                    JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                    String answerInitial = normalizeArticulationPhoneme(extractInitial(answer));
                    boolean matched = !answerInitial.isEmpty() && targetInitial.equals(answerInitial);
                    comparedAny = true;
                    profile.totalSyllables++;
                    if (matched) {
                        profile.correctSyllables++;
                    } else {
                        itemAllCorrect = false;
                    }
                    updateStructureScore(phonemeScores, targetInitial, matched);
                    updateStructureScore(positionScores, describeArticulationWordPosition(targets.length(), idx), matched);
                }
                if (comparedAny) {
                    profile.completedItems++;
                }
                if (targets.length() > 1 && comparableCount > 0) {
                    hasSpeechLevelEvidence = true;
                    profile.multisyllabicWords++;
                    if (itemAllCorrect) {
                        profile.multisyllabicStableWords++;
                    }
                }
                String process = safeText(item.optString("phonologyProcess", ""));
                if (!process.isEmpty()) {
                    addUniqueText(profile.phonologyProcesses, process);
                }
            }
        }

        for (Map.Entry<String, StructureScore> entry : phonemeScores.entrySet()) {
            String phoneme = safeText(entry.getKey());
            StructureScore score = entry.getValue();
            if (phoneme.isEmpty() || score == null || score.total <= 0) {
                continue;
            }
            addUniqueText(profile.observedPhonemes, phoneme);
            double rate = score.correct * 1.0d / score.total;
            if (rate >= 0.8d) {
                addUniqueText(profile.masteredPhonemes, phoneme);
            } else if (rate <= 0.3d) {
                addUniqueText(profile.focusPhonemes, phoneme);
            } else {
                addUniqueText(profile.unstablePhonemes, phoneme);
            }
        }

        if (profile.observedPhonemes.isEmpty() && phonemeSummary != null) {
            for (String phoneme : phonemeSummary.masteredPhonemes) {
                addUniqueText(profile.observedPhonemes, phoneme);
                addUniqueText(profile.masteredPhonemes, phoneme);
            }
            for (String phoneme : phonemeSummary.targetPhonemes) {
                addUniqueText(profile.observedPhonemes, phoneme);
                if (!profile.masteredPhonemes.contains(phoneme)) {
                    addUniqueText(profile.focusPhonemes, phoneme);
                }
            }
        }

        classifyArticulationPositionScores(positionScores, profile);
        profile.multisyllabicRetention = describeMultisyllabicRetention(profile.multisyllabicStableWords,
                profile.multisyllabicWords);
        profile.connectedSpeechCarryover = describeConnectedSpeechCarryover(hasSpeechLevelEvidence,
                profile.multisyllabicStableWords, profile.multisyllabicWords);
        profile.overallLevel = describeArticulationOverallLevel(clinical, accuracy,
                profile.focusPhonemes, profile.unstablePhonemes);
        profile.extractionNote = buildArticulationExtractionNote(profile, phonemeScores, positionScores);
        return profile;
    }

    private static void classifyArticulationPositionScores(Map<String, StructureScore> positionScores,
                                                           ArticulationProfile profile) {
        for (Map.Entry<String, StructureScore> entry : positionScores.entrySet()) {
            String position = safeText(entry.getKey());
            StructureScore score = entry.getValue();
            if (position.isEmpty() || score == null || score.total <= 0) {
                continue;
            }
            double rate = score.correct * 1.0d / score.total;
            if (rate >= 0.8d) {
                addUniqueText(profile.wordPositionStable, position + " keeps target sounds relatively stable");
            } else if (rate <= 0.3d) {
                addUniqueText(profile.wordPositionFocus, position + " shows clear difficulty for accurate target-sound production");
            } else {
                addUniqueText(profile.wordPositionUnstable, position + " has emerging but inconsistent target-sound production");
            }
        }
    }

    private static String describeArticulationWordPosition(int syllableCount, int syllableIndex) {
        if (syllableCount <= 1) {
            return "single-syllable word targets";
        }
        if (syllableIndex == 0) {
            return "multisyllabic word initial position";
        }
        if (syllableIndex == syllableCount - 1) {
            return "multisyllabic word final position";
        }
        return "multisyllabic word medial position";
    }

    private static String normalizeArticulationPhoneme(String value) {
        String text = safeLower(value);
        if (text.isEmpty()) {
            return "";
        }
        if (text.startsWith("/") && text.endsWith("/") && text.length() > 2) {
            return text;
        }
        return "/" + text.replace("/", "") + "/";
    }

    private static String describeMultisyllabicRetention(int stableCount, int totalCount) {
        if (totalCount <= 0) {
            return "Current articulation evidence is mostly at single-word level, with limited multisyllabic retention data.";
        }
        double rate = stableCount * 1.0d / totalCount;
        if (rate >= 0.8d) {
            return "Target-sound retention in multisyllabic words is relatively stable and may generalize to higher speech-load tasks.";
        }
        if (rate >= 0.4d) {
            return "Target sounds appear in multisyllabic words but lose stability as syllable load increases.";
        }
        return "Target-sound retention in multisyllabic words remains difficult, with more errors as syllable load increases.";
    }

    private static String describeConnectedSpeechCarryover(boolean hasSpeechLevelEvidence,
                                                           int stableCount,
                                                           int totalCount) {
        if (!hasSpeechLevelEvidence) {
            return "Current data mainly reflects word-level articulation; connected-speech carryover should be judged conservatively.";
        }
        if (totalCount <= 0) {
            return "There is not enough phrase or connected-speech evidence to make a stable carryover judgment.";
        }
        double rate = stableCount * 1.0d / totalCount;
        if (rate >= 0.8d) {
            return "Retention under higher speech load looks promising, but phrase and connected-speech carryover still needs direct observation.";
        }
        return "Target-sound stability drops as speech load increases, so connected-speech carryover remains a priority for follow-up training.";
    }

    private static String describeArticulationOverallLevel(SpeechSoundClinical clinical,
                                                           InitialAccuracy accuracy,
                                                           List<String> focusPhonemes,
                                                           List<String> unstablePhonemes) {
        String intelligibility = safeText(clinical == null ? "" : clinical.intelligibility);
        double rate = accuracy != null && accuracy.total > 0 ? accuracy.correct * 1.0d / accuracy.total : -1d;
        if (rate < 0d) {
            if (!focusPhonemes.isEmpty() || !unstablePhonemes.isEmpty()) {
                return "Articulation is not yet fully age-appropriate, and some target sounds remain not established or unstable.";
            }
            return "No usable articulation assessment data is currently available.";
        }
        if (rate >= 0.85d) {
            return "Articulation has some stable foundations, but age-expected mastery is not yet complete for more complex sounds or higher speech-load conditions.";
        }
        if (rate >= 0.6d) {
            return "Articulation has not yet fully reached age expectations; some speech sounds are emerging, but target-sound stability is still limited across sounds, positions, or multisyllabic words.";
        }
        if (!intelligibility.isEmpty()) {
            return "Articulation is below age expectation, with intelligibility noted as " + intelligibility + ", and multiple target sounds show clear difficulty or instability.";
        }
        return "Articulation is below age expectation, and multiple target sounds still show clear production difficulty and unstable performance.";
    }

    private static String buildArticulationExtractionNote(ArticulationProfile profile,
                                                          Map<String, StructureScore> phonemeScores,
                                                          Map<String, StructureScore> positionScores) {
        if (profile.totalItems <= 0) {
            return "No usable A articulation item results were found.";
        }
        if (phonemeScores.isEmpty()) {
            return "The summary falls back to ArticulationDataParser phoneme output and clinical header fields because raw item comparisons are limited.";
        }
        if (positionScores.isEmpty()) {
            return "Target-sound production was extracted, but word-position stability is summarized conservatively because position-level comparisons are limited.";
        }
        return "The summary combines clinical header fields, raw A targetWord/answerPhonology comparisons, phonology processes, and multisyllabic retention clues.";
    }

    public static JSONObject getSyntaxSummaryJson(JSONObject evaluations) throws JSONException {

        JSONObject summary = new JSONObject();
        JSONArray rgArray = getMergedSyntaxArray(evaluations, "RG");
        JSONArray seArray = getMergedSyntaxArray(evaluations, "SE");
        summary.put("RG", getBoolAccuracyJson(rgArray, "result"));
        summary.put("SE", getBoolAccuracyJson(seArray, "result"));
        summary.put("rgComprehension", buildRgProfileJson(evaluations));
        summary.put("seExpression", buildSeProfileJson(evaluations));
        summary.put("syntaxAssessment", buildSyntaxAssessmentJson(evaluations));
        return summary;
    }

    public static JSONObject getVocabularySummaryJson(JSONObject evaluations) throws JSONException {
        JSONObject summary = new JSONObject();
        JSONArray expressiveArray = evaluations != null ? evaluations.optJSONArray("E") : null;
        JSONArray evArray = evaluations != null ? evaluations.optJSONArray("EV") : null;
        JSONArray reArray = evaluations != null ? evaluations.optJSONArray("RE") : null;
        VocabularyAbilityAnalyzer.Summary vocabularySummary = VocabularyAbilityAnalyzer.summarize(evaluations);
        summary.put("expressive", getBoolAccuracyJson(expressiveArray, "result"));
        summary.put("receptive", getBoolAccuracyJson(getPrimaryVocabularyReceptiveArray(evaluations), "result"));
        summary.put("EV", getBoolAccuracyJson(evArray, "result"));
        summary.put("RE", getBoolAccuracyJson(reArray, "result"));
        summary.put("S", getBoolAccuracyJson(evaluations != null ? evaluations.optJSONArray("S") : null, "result"));
        summary.put("NWR", getNwrAccuracyJson(evaluations != null ? evaluations.optJSONArray("NWR") : null));
        summary.put("vocabularyAssessment", vocabularySummary.toJson());
        return summary;
    }

    public static JSONArray getPrimaryVocabularyReceptiveArray(JSONObject evaluations) {
        return VocabularyAbilityAnalyzer.getPrimaryReceptiveArray(evaluations);
    }

    public static JSONObject getSocialSummaryJson(JSONObject evaluations) throws JSONException {
        SocialAbilitySummarizer.Summary socialSummary = SocialAbilitySummarizer.summarize(
                getMeasuredSocialArray(evaluations == null ? null : evaluations.optJSONArray(SOCIAL_KEY)));
        return socialSummary.toJson();
    }

    public static JSONObject getSocialSummaryJson(JSONArray evaluationsSocial) throws JSONException {
        SocialAbilitySummarizer.Summary socialSummary = SocialAbilitySummarizer.summarize(
                getMeasuredSocialArray(evaluationsSocial));
        return socialSummary.toJson();
    }

    public static JSONArray getMeasuredSocialArray(JSONArray evaluationsSocial) {
        JSONArray measured = new JSONArray();
        if (evaluationsSocial == null) {
            return measured;
        }
        for (int i = 0; i < evaluationsSocial.length(); i++) {
            JSONObject item = evaluationsSocial.optJSONObject(i);
            if (item == null || !item.has("score") || item.isNull("score")) {
                continue;
            }
            measured.put(item);
        }
        return measured;
    }

    public static JSONObject getPrelinguisticSummaryJson(JSONObject evaluations) throws JSONException {
        JSONObject summary = new JSONObject();
        JSONObject normalizedEvaluations = normalizePrelinguisticEvaluations(evaluations);
        PreLinguisticDataParser.PreLinguisticHeader header = PreLinguisticDataParser.parseHeader(normalizedEvaluations);
        if (header != null) {
            JSONArray dimensions = new JSONArray();
            for (PreLinguisticDataParser.DimensionResult result : header.dimensions) {
                if (shouldIncludeDimension(result)) {
                    JSONObject dim = new JSONObject();
                    dim.put("name", result.name);
                    dim.put("status", result.status);
                    dim.put("score", result.achieved + "/" + result.total);
                    dimensions.put(dim);
                }
            }
            summary.put("dimensions", dimensions);
            summary.put("diagnosis", header.diagnosisText);
            summary.put("suggestions", header.suggestions);
        }
        summary.put("interactionProfile", buildPrelinguisticInteractionProfileJson(normalizedEvaluations, header));
        return summary;
    }

    public static JSONArray getMergedPrelinguisticArray(JSONObject evaluations) {
        JSONArray merged = new JSONArray();
        if (evaluations == null) {
            return merged;
        }
        JSONArray direct = evaluations.optJSONArray("PL");
        if (direct != null && direct.length() > 0) {
            appendArray(merged, direct);
            return merged;
        }
        appendArray(merged, evaluations.optJSONArray("PL_A"));
        appendArray(merged, evaluations.optJSONArray("PL_B"));
        return merged;
    }

    private static JSONObject normalizePrelinguisticEvaluations(JSONObject evaluations) {
        JSONObject normalized = new JSONObject();
        if (evaluations == null) {
            return normalized;
        }
        JSONArray names = evaluations.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                String key = names.optString(i, "");
                if (!key.isEmpty()) {
                    try {
                        normalized.put(key, evaluations.opt(key));
                    } catch (JSONException ignored) {
                    }
                }
            }
        }
        try {
            normalized.put("PL", getMergedPrelinguisticArray(evaluations));
        } catch (JSONException ignored) {
        }
        return normalized;
    }

    private static JSONObject buildPrelinguisticInteractionProfileJson(JSONObject evaluations,
                                                                       PreLinguisticDataParser.PreLinguisticHeader header) throws JSONException {
        PrelinguisticProfile profile = buildPrelinguisticProfile(evaluations, header);
        JSONObject out = new JSONObject();
        out.put("totalObserved", profile.total);
        out.put("achievedCount", profile.achieved);
        out.put("overallLevel", profile.overallLevel);
        out.put("masteredBehaviors", toJsonArray(profile.mastered));
        out.put("focusBehaviors", toJsonArray(profile.focus));
        out.put("unstableBehaviors", toJsonArray(profile.unstable));
        out.put("observedBehaviors", toJsonArray(profile.observed));
        out.put("coreDomains", profile.coreDomains);
        out.put("extractionNote", profile.extractionNote);
        return out;
    }

    private static PrelinguisticProfile buildPrelinguisticProfile(JSONObject evaluations,
                                                                  PreLinguisticDataParser.PreLinguisticHeader header) {
        PrelinguisticProfile profile = new PrelinguisticProfile();
        Map<String, StructureScore> behaviorScores = new LinkedHashMap<>();
        JSONArray plArray = getMergedPrelinguisticArray(evaluations);
        for (int i = 0; i < plArray.length(); i++) {
            JSONObject item = plArray.optJSONObject(i);
            if (item == null || !item.has("score") || item.isNull("score")) {
                continue;
            }
            int score = item.optInt("score", 0);
            String behavior = inferPrelinguisticBehavior(item);
            updateStructureScore(behaviorScores, behavior, score > 0);
            profile.total++;
            if (score > 0) {
                profile.achieved++;
            }
        }

        for (Map.Entry<String, StructureScore> entry : behaviorScores.entrySet()) {
            String behavior = safeText(entry.getKey());
            if (behavior.isEmpty()) {
                continue;
            }
            profile.observed.add(behavior);
            StructureScore score = entry.getValue();
            if (score == null || score.total <= 0) {
                continue;
            }
            if (score.correct == score.total) {
                addUniqueText(profile.mastered, behavior);
            } else if (score.correct == 0) {
                addUniqueText(profile.focus, behavior);
            } else {
                addUniqueText(profile.unstable, behavior);
            }
        }

        if (header != null) {
            for (int i = 0; i < header.dimensions.size(); i++) {
                PreLinguisticDataParser.DimensionResult result = header.dimensions.get(i);
                if (!shouldIncludeDimension(result)) {
                    continue;
                }
                JSONObject domain = new JSONObject();
                try {
                    domain.put("name", result.name);
                    domain.put("status", result.status);
                    domain.put("score", result.achieved + "/" + result.total);
                } catch (JSONException ignored) {
                }
                profile.coreDomains.put(domain);
                mergePrelinguisticDimensionFallback(profile, i, result);
            }
        }

        profile.overallLevel = describePrelinguisticOverallLevel(profile.achieved, profile.total, profile.focus, profile.unstable);
        if (profile.total > 0) {
            profile.extractionNote = "优先使用合并后的 PL 原始题目，按互动行为关键词归类；题目标签不足时再退回 PreLinguisticDataParser 维度摘要。";
        } else if (header != null && !header.dimensions.isEmpty()) {
            profile.extractionNote = "未取得足够的 PL 原始题目得分，已退回使用 PreLinguisticDataParser 的维度摘要做前语言归纳。";
        } else {
            profile.extractionNote = "暂无可用的前语言评估数据。";
        }
        return profile;
    }

    private static void mergePrelinguisticDimensionFallback(PrelinguisticProfile profile,
                                                            int index,
                                                            PreLinguisticDataParser.DimensionResult result) {
        if (profile == null || result == null || result.total <= 0) {
            return;
        }
        String label = mapPrelinguisticDimensionLabel(index);
        if (label.isEmpty()) {
            return;
        }
        addUniqueText(profile.observed, label);
        if (result.achieved == result.total) {
            addUniqueText(profile.mastered, label);
            return;
        }
        if (result.achieved == 0) {
            addUniqueText(profile.focus, label);
            return;
        }
        addUniqueText(profile.unstable, label);
    }

    private static String mapPrelinguisticDimensionLabel(int index) {
        switch (index) {
            case 0:
                return "共同注意与互动关注";
            case 1:
                return "模仿能力";
            case 2:
                return "游戏参与与功能性操作";
            case 3:
                return "沟通意图与互动回应";
            default:
                return "其他前语言互动能力";
        }
    }

    private static String inferPrelinguisticBehavior(JSONObject item) {
        if (item == null) {
            return "其他前语言互动行为";
        }
        String skill = safeText(item.optString("skill", ""));
        String prompt = safeText(item.optString("prompt", ""));
        String observation = safeText(item.optString("observation", ""));
        String combined = (skill + " " + prompt + " " + observation).toLowerCase(Locale.CHINA);
        String mapped = mapPrelinguisticBehaviorFromText(combined);
        return mapped.isEmpty() ? "其他前语言互动行为" : mapped;
    }

    private static String mapPrelinguisticBehaviorFromText(String text) {
        if (containsAnyKeyword(text, "共同注意", "联合注意", "回应性共同关注", "发起共同关注", "指物", "指示", "共享关注", "看图")) {
            return "共同注意与共享关注";
        }
        if (containsAnyKeyword(text, "目光", "眼神", "注视", "看向", "看着对方", "社交注视", "看人")) {
            return "目光交流与社交注视";
        }
        if (containsAnyKeyword(text, "模仿", "imitat")) {
            return "模仿能力";
        }
        if (containsAnyKeyword(text, "轮流", "轮替")) {
            return "轮流互动能力";
        }
        if (containsAnyKeyword(text, "提要求", "发起", "邀请", "还要", "主动", "意图性沟通", "意图表达")) {
            return "主动发起与沟通意图表达";
        }
        if (containsAnyKeyword(text, "回应", "听指令", "确认", "点头", "摇头", "提示", "招手", "跟过去")) {
            return "对成人互动或提示的回应";
        }
        if (containsAnyKeyword(text, "玩玩具", "游戏", "假装游戏", "参与", "功能地玩", "玩球", "泡泡")) {
            return "游戏参与与功能性操作";
        }
        if (containsAnyKeyword(text, "发出声音", "发声", "语音", "声音", "沟通性声音")) {
            return "沟通意图与表达尝试";
        }
        return "";
    }

    private static boolean containsAnyKeyword(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isEmpty() && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static void addUniqueText(List<String> list, String value) {
        if (list == null) {
            return;
        }
        String text = safeText(value);
        if (!text.isEmpty() && !list.contains(text)) {
            list.add(text);
        }
    }

    private static String describePrelinguisticOverallLevel(int achieved, int total,
                                                            List<String> focus,
                                                            List<String> unstable) {
        if (total <= 0) {
            if (focus != null && !focus.isEmpty()) {
                return "前语言能力提示交流基础尚未稳定建立，尤其在共同注意、回应或主动互动方面仍需继续支持。";
            }
            return "暂无可用的前语言评估数据。";
        }
        double ratio = achieved * 1.0d / total;
        if (ratio >= 0.75d) {
            return "前语言能力已出现一定基础，但持续共同注意、主动发起或互动稳定性仍需继续巩固。";
        }
        if (ratio >= 0.4d) {
            return "前语言能力评估显示，孩子的交流基础能力尚未完全稳定建立，部分互动行为已出现，但在维持互动、回应一致性和共同注意方面仍不稳定。";
        }
        return "前语言能力评估显示，孩子当前的交流基础能力尚未达到稳定水平，需优先支持共同注意、主动发起、回应与游戏互动等核心前语言能力。";
    }

    static class BoolAccuracy {
        int total;
        int correct;
    }

    static BoolAccuracy calcBooleanAccuracy(JSONArray array, String key) {
        BoolAccuracy acc = new BoolAccuracy();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null && obj.has(key) && !obj.isNull(key)) {
                    acc.total++;
                    if (obj.optBoolean(key)) {
                        acc.correct++;
                    }
                }
            }
        }
        return acc;
    }

    private static JSONObject getBoolAccuracyJson(JSONArray array, String key) throws JSONException {
        JSONObject obj = new JSONObject();
        if (array == null || array.length() == 0) {
            obj.put("status", "no_data");
            return obj;
        }
        BoolAccuracy acc = calcBooleanAccuracy(array, key);
        obj.put("total", acc.total);
        obj.put("correct", acc.correct);
        if (acc.total > 0) {
            obj.put("accuracy", String.format(Locale.US, "%.2f%%", acc.correct * 100.0 / acc.total));
        }
        return obj;
    }
    
    private static JSONObject getNwrAccuracyJson(JSONArray array) throws JSONException {
         JSONObject obj = new JSONObject();
         if (array == null) {
             obj.put("status", "no_data");
             return obj;
         }
         int correct = 0;
         int total = 0;
         for (int i = 0; i < array.length(); i++) {
             JSONObject item = array.optJSONObject(i);
             if (item == null) continue;
             for (int j = 1; j <= 6; j++) {
                 String key = "results" + j;
                 if (item.has(key) && !item.isNull(key) && !"null".equals(item.optString(key))) {
                     total++;
                     if (item.optBoolean(key)) correct++;
                 }
             }
         }
         obj.put("total", total);
         obj.put("correct", correct);
         if (total > 0) {
             obj.put("accuracy", String.format(Locale.US, "%.2f%%", correct * 100.0 / total));
         }
         return obj;
    }

    public static JSONArray getMergedSyntaxArray(JSONObject evaluations, String prefix) {
        JSONArray merged = new JSONArray();
        if (evaluations == null) {
            return merged;
        }
        JSONArray direct = evaluations.optJSONArray(prefix);
        if (direct != null && direct.length() > 0) {
            appendArray(merged, direct);
            return merged;
        }
        for (int i = 1; i <= 4; i++) {
            JSONArray groupArray = evaluations.optJSONArray(prefix + i);
            if (groupArray != null && groupArray.length() > 0) {
                appendArray(merged, groupArray);
            }
        }
        return merged;
    }

    private static JSONObject buildRgProfileJson(JSONObject evaluations) throws JSONException {
        SyntaxProfile profile = buildRgProfile(evaluations);
        JSONObject out = new JSONObject();
        out.put("totalCompleted", profile.total);
        out.put("correctCount", profile.correct);
        if (profile.total > 0) {
            out.put("accuracy", formatAccuracyPercent(profile.correct, profile.total));
        }
        out.put("overallLevel", profile.overallLevel);
        out.put("masteredStructures", toJsonArray(profile.mastered));
        out.put("focusStructures", toJsonArray(profile.focus));
        out.put("unstableStructures", toJsonArray(profile.unstable));
        out.put("logicRelatedStructures", toJsonArray(profile.logicRelated));
        out.put("observedStructures", toJsonArray(profile.observedStructures));
        out.put("extractionNote", profile.extractionNote);
        return out;
    }

    private static JSONObject buildSeProfileJson(JSONObject evaluations) throws JSONException {
        SyntaxProfile profile = buildSeProfile(evaluations);
        JSONObject out = new JSONObject();
        out.put("totalCompleted", profile.total);
        out.put("correctCount", profile.correct);
        if (profile.total > 0) {
            out.put("accuracy", formatAccuracyPercent(profile.correct, profile.total));
        }
        out.put("overallLevel", profile.overallLevel);
        out.put("masteredStructures", toJsonArray(profile.mastered));
        out.put("focusStructures", toJsonArray(profile.focus));
        out.put("unstableStructures", toJsonArray(profile.unstable));
        out.put("logicRelatedStructures", toJsonArray(profile.logicRelated));
        out.put("observedStructures", toJsonArray(profile.observedStructures));
        out.put("extractionNote", profile.extractionNote);
        return out;
    }

    private static JSONObject buildSyntaxAssessmentJson(JSONObject evaluations) throws JSONException {
        SyntaxProfile rgProfile = buildRgProfile(evaluations);
        SyntaxProfile seProfile = buildSeProfile(evaluations);
        JSONObject out = new JSONObject();
        out.put("overallLevel", buildCombinedSyntaxOverallLevel(rgProfile, seProfile));
        out.put("mastered", toJsonArray(mergeTaggedStructures("\u53e5\u6cd5\u7406\u89e3", rgProfile.mastered,
                "\u53e5\u6cd5\u8868\u8fbe", seProfile.mastered)));
        out.put("focus", toJsonArray(mergeTaggedStructures("\u53e5\u6cd5\u7406\u89e3", rgProfile.focus,
                "\u53e5\u6cd5\u8868\u8fbe", seProfile.focus)));
        out.put("unstable", toJsonArray(mergeTaggedStructures("\u53e5\u6cd5\u7406\u89e3", rgProfile.unstable,
                "\u53e5\u6cd5\u8868\u8fbe", seProfile.unstable)));
        out.put("sharedPriorityStructures", toJsonArray(intersectStructures(rgProfile.focus, seProfile.focus)));
        out.put("sharedUnstableStructures", toJsonArray(intersectStructures(rgProfile.unstable, seProfile.unstable)));
        out.put("extractionNote", buildCombinedExtractionNote(rgProfile, seProfile));
        return out;
    }

    private static SyntaxProfile buildRgProfile(JSONObject evaluations) {
        Map<String, StructureScore> structureScores = new LinkedHashMap<>();
        JSONArray direct = evaluations != null ? evaluations.optJSONArray("RG") : null;
        int total = 0;
        int correct = 0;
        boolean usedFallback = false;

        if (direct != null && direct.length() > 0) {
            FallbackProgress progress = new FallbackProgress();
            for (int i = 0; i < direct.length(); i++) {
                JSONObject item = direct.optJSONObject(i);
                if (item == null || !item.has("result") || item.isNull("result")) {
                    continue;
                }
                String structure = inferRgStructure(item, i, -1, progress);
                updateStructureScore(structureScores, structure, item.optBoolean("result"));
                total++;
                if (item.optBoolean("result")) {
                    correct++;
                }
                if ("\u672a\u5206\u7c7b\u53e5\u6cd5\u7406\u89e3\u7ed3\u6784".equals(structure)) {
                    usedFallback = true;
                }
            }
        } else {
            for (int group = 1; group <= 4; group++) {
                JSONArray groupArray = evaluations != null ? evaluations.optJSONArray("RG" + group) : null;
                if (groupArray == null) {
                    continue;
                }
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject item = groupArray.optJSONObject(i);
                    if (item == null || !item.has("result") || item.isNull("result")) {
                        continue;
                    }
                    String structure = inferRgStructure(item, i, group, null);
                    updateStructureScore(structureScores, structure, item.optBoolean("result"));
                    total++;
                    if (item.optBoolean("result")) {
                        correct++;
                    }
                    if ("\u672a\u5206\u7c7b\u53e5\u6cd5\u7406\u89e3\u7ed3\u6784".equals(structure)) {
                        usedFallback = true;
                    }
                }
            }
        }

        SyntaxProfile profile = new SyntaxProfile();
        profile.total = total;
        profile.correct = correct;
        profile.overallLevel = describeRgOverallLevel(correct, total);
        for (Map.Entry<String, StructureScore> entry : structureScores.entrySet()) {
            String structure = entry.getKey();
            StructureScore score = entry.getValue();
            profile.observedStructures.add(structure);
            double accuracy = score.total > 0 ? (double) score.correct / score.total : 0d;
            if (accuracy >= 0.8d) {
                profile.mastered.add(structure);
            } else if (accuracy <= 0.3d) {
                profile.focus.add(structure);
            } else {
                profile.unstable.add(structure);
            }
            if (isLogicRelatedStructure(structure)) {
                profile.logicRelated.add(structure);
            }
        }

        if (profile.observedStructures.isEmpty()) {
            profile.extractionNote = "\u6682\u65e0\u53ef\u7528\u7684 RG \u53e5\u6cd5\u7406\u89e3\u9898\u76ee\u7ed3\u679c\u3002";
        } else if (usedFallback) {
            profile.extractionNote = "\u5df2\u4f18\u5148\u4f7f\u7528 RG \u9898\u7ec4/\u9898\u53f7\u6620\u5c04\u5f52\u7eb3\u7ed3\u6784\uff0c\u65e0\u6cd5\u7a33\u5b9a\u8bc6\u522b\u65f6\u4f7f\u7528\u9898\u76ee\u6587\u672c\u515c\u5e95\u3002";
        } else {
            profile.extractionNote = "\u5df2\u5f52\u7eb3 RG \u4e2d\u5df2\u638c\u63e1\u3001\u5c1a\u672a\u5efa\u7acb\u4e0e\u4e0d\u7a33\u5b9a\u7684\u53e5\u6cd5\u7406\u89e3\u7ed3\u6784\u3002";
        }
        return profile;
    }

    private static SyntaxProfile buildSeProfile(JSONObject evaluations) {
        Map<String, StructureScore> structureScores = new LinkedHashMap<>();
        JSONArray direct = evaluations != null ? evaluations.optJSONArray("SE") : null;
        int total = 0;
        int correct = 0;
        boolean usedFallback = false;

        if (direct != null && direct.length() > 0) {
            FallbackProgress progress = new FallbackProgress();
            for (int i = 0; i < direct.length(); i++) {
                JSONObject item = direct.optJSONObject(i);
                if (item == null || !item.has("result") || item.isNull("result")) {
                    continue;
                }
                String structure = inferSeStructure(item, i, -1, progress);
                updateStructureScore(structureScores, structure, item.optBoolean("result"));
                total++;
                if (item.optBoolean("result")) {
                    correct++;
                }
                if ("\u672a\u5206\u7c7b\u53e5\u6cd5\u8868\u8fbe\u7ed3\u6784".equals(structure)) {
                    usedFallback = true;
                }
            }
        } else {
            for (int group = 1; group <= 4; group++) {
                JSONArray groupArray = evaluations != null ? evaluations.optJSONArray("SE" + group) : null;
                if (groupArray == null) {
                    continue;
                }
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject item = groupArray.optJSONObject(i);
                    if (item == null || !item.has("result") || item.isNull("result")) {
                        continue;
                    }
                    String structure = inferSeStructure(item, i, group, null);
                    updateStructureScore(structureScores, structure, item.optBoolean("result"));
                    total++;
                    if (item.optBoolean("result")) {
                        correct++;
                    }
                    if ("\u672a\u5206\u7c7b\u53e5\u6cd5\u8868\u8fbe\u7ed3\u6784".equals(structure)) {
                        usedFallback = true;
                    }
                }
            }
        }

        SyntaxProfile profile = new SyntaxProfile();
        profile.total = total;
        profile.correct = correct;
        profile.overallLevel = describeSeOverallLevel(correct, total);
        for (Map.Entry<String, StructureScore> entry : structureScores.entrySet()) {
            String structure = entry.getKey();
            StructureScore score = entry.getValue();
            profile.observedStructures.add(structure);
            double accuracy = score.total > 0 ? (double) score.correct / score.total : 0d;
            if (accuracy >= 0.8d) {
                profile.mastered.add(structure);
            } else if (accuracy <= 0.3d) {
                profile.focus.add(structure);
            } else {
                profile.unstable.add(structure);
            }
            if (isLogicRelatedStructure(structure)) {
                profile.logicRelated.add(structure);
            }
        }

        if (profile.observedStructures.isEmpty()) {
            profile.extractionNote = "\u6682\u65e0\u53ef\u7528\u7684 SE \u53e5\u6cd5\u8868\u8fbe\u9898\u76ee\u7ed3\u679c\u3002";
        } else if (usedFallback) {
            profile.extractionNote = "\u5df2\u4f18\u5148\u4f7f\u7528 SE \u9898\u7ec4/\u9898\u53f7\u6620\u5c04\u5f52\u7eb3\u7ed3\u6784\uff0c\u65e0\u6cd5\u7a33\u5b9a\u8bc6\u522b\u65f6\u4f7f\u7528\u9898\u76ee\u6587\u672c\u515c\u5e95\u3002";
        } else {
            profile.extractionNote = "\u5df2\u5f52\u7eb3 SE \u4e2d\u5df2\u638c\u63e1\u3001\u5c1a\u672a\u5efa\u7acb\u4e0e\u4e0d\u7a33\u5b9a\u7684\u53e5\u6cd5\u8868\u8fbe\u7ed3\u6784\u3002";
        }
        return profile;
    }

    private static String inferRgStructure(JSONObject item, int index, int explicitGroup, FallbackProgress progress) {
        String question = safeText(item == null ? "" : item.optString("question", ""));
        String fromQuestion = inferStructureFromQuestion(question);
        if (!fromQuestion.isEmpty()) {
            return fromQuestion;
        }
        int group = explicitGroup;
        int questionNumber = index + 1;
        if (group <= 0 && progress != null) {
            group = resolveFallbackGroup(index, progress);
            questionNumber = index - progress.groupStartIndex + 1;
        }
        String mapped = mapRgStructureByGroup(group, questionNumber);
        return mapped.isEmpty() ? "\u672a\u5206\u7c7b\u53e5\u6cd5\u7406\u89e3\u7ed3\u6784" : mapped;
    }

    private static String inferSeStructure(JSONObject item, int index, int explicitGroup, FallbackProgress progress) {
        String question = safeText(item == null ? "" : item.optString("question", ""));
        String fromQuestion = inferStructureFromQuestion(question);
        if (!fromQuestion.isEmpty()) {
            return fromQuestion.replace("\u7406\u89e3", "\u8868\u8fbe");
        }
        int group = explicitGroup;
        int questionNumber = index + 1;
        if (group <= 0 && progress != null) {
            group = resolveFallbackGroup(index, progress);
            questionNumber = index - progress.groupStartIndex + 1;
        }
        String mapped = mapSeStructureByGroup(group, questionNumber);
        return mapped.isEmpty() ? "\u672a\u5206\u7c7b\u53e5\u6cd5\u8868\u8fbe\u7ed3\u6784" : mapped;
    }

    private static int resolveFallbackGroup(int index, FallbackProgress progress) {
        while (progress.group <= RG_GROUP_LENGTHS.length
                && index >= progress.groupStartIndex + RG_GROUP_LENGTHS[progress.group - 1]) {
            progress.groupStartIndex += RG_GROUP_LENGTHS[progress.group - 1];
            progress.group++;
        }
        return progress.group <= RG_GROUP_LENGTHS.length ? progress.group : RG_GROUP_LENGTHS.length;
    }

    private static String inferStructureFromQuestion(String question) {
        if (question.isEmpty()) {
            return "";
        }
        if (question.contains("\u88ab")) return "\u88ab\u52a8\u53e5\u7406\u89e3";
        if (question.contains("\u56e0\u4e3a") || question.contains("\u6240\u4ee5")) return "\u56e0\u679c\u590d\u53e5\u7406\u89e3";
        if (question.contains("\u9664\u975e") || question.contains("\u5426\u5219")
                || question.contains("\u5982\u679c") || question.contains("\u5c31")) return "\u6761\u4ef6\u53e5\u7406\u89e3";
        if (question.contains("\u6bd4") || question.contains("\u66f4") || question.contains("\u6700")) return "\u6bd4\u8f83\u53e5\u7406\u89e3";
        if (question.contains("\u4e0d") || question.contains("\u6ca1")) return "\u5426\u5b9a\u53e5\u7406\u89e3";
        if (question.contains("\u5148") || question.contains("\u540e") || question.contains("\u518d")) return "\u65f6\u95f4\u987a\u5e8f\u53e5\u7406\u89e3";
        if (question.contains("\u54ea") || question.contains("\u8c01") || question.contains("\u600e\u4e48")
                || question.contains("\u662f\u4e0d\u662f")) return "\u95ee\u53e5\u7406\u89e3";
        if (question.contains("\u7ea2") || question.contains("\u9ad8") || question.contains("\u80d6")
                || question.contains("\u5927")) return "\u4fee\u9970\u7ed3\u6784\u7406\u89e3";
        return "";
    }

    private static String mapRgStructureByGroup(int group, int questionNumber) {
        switch (group) {
            case 1:
                if (questionNumber <= 3) return "\u7b80\u5355\u4e3b\u8c13\u7ed3\u6784\u7406\u89e3";
                if (questionNumber <= 6) return "\u5e38\u89c1\u52a8\u5bbe\u7ed3\u6784\u7406\u89e3";
                if (questionNumber <= 9) return "\u4e3b\u8c13\u5bbe\u7ed3\u6784\u7406\u89e3";
                if (questionNumber <= 12) return "\u5426\u5b9a\u53e5\u7406\u89e3";
                if (questionNumber <= 15) return "\u4e00\u822c\u95ee\u53e5\u7406\u89e3";
                if (questionNumber <= 18) return "\u7279\u6b8a\u95ee\u53e5\u7406\u89e3";
                if (questionNumber <= 21) return "\u5355\u4e00\u5f62\u5bb9\u8bcd\u4fee\u9970\u540d\u8bcd\u7406\u89e3";
                return "";
            case 2:
                if (questionNumber <= 3) return "\u591a\u91cd\u4fee\u9970\u7ed3\u6784\u7406\u89e3";
                if (questionNumber <= 6) return "\u53cc\u5bbe\u7ed3\u6784\u7406\u89e3";
                if (questionNumber <= 9) return "\u662f\u975e\u95ee\u53e5\u7406\u89e3";
                if (questionNumber <= 12) return "\u5730\u70b9\u95ee\u53e5\u7406\u89e3";
                if (questionNumber <= 15) return "\u526f\u8bcd\"\u90fd\"\u7406\u89e3";
                if (questionNumber <= 18) return "\u8bed\u5e8f\u7406\u89e3";
                return "";
            case 3:
                if (questionNumber <= 3) return "\u88ab\u52a8\u53e5\u7406\u89e3";
                if (questionNumber <= 6) return "\u6bd4\u8f83\u53e5\u7406\u89e3";
                if (questionNumber <= 9) return "\u5b8c\u6210\u4f53\u53e5\u7406\u89e3";
                if (questionNumber <= 12) return "\u56e0\u679c\u590d\u53e5\u7406\u89e3";
                if (questionNumber <= 15) return "\u8f6c\u6298\u53e5\u7406\u89e3";
                if (questionNumber <= 18) return "\u65f6\u95f4\u987a\u5e8f\u53e5\u7406\u89e3";
                return "";
            case 4:
                if (questionNumber <= 3) return "\u6301\u7eed\u4f53\u53e5\u7406\u89e3";
                if (questionNumber <= 6) return "\u53cc\u91cd\u5426\u5b9a\u7406\u89e3";
                if (questionNumber <= 9) return "\u6761\u4ef6\u53e5\u7406\u89e3";
                if (questionNumber <= 15) return "\u7bc7\u7ae0\u4e0e\u4e8b\u4ef6\u903b\u8f91\u7406\u89e3";
                return "";
            default:
                return "";
        }
    }

    private static String mapSeStructureByGroup(int group, int questionNumber) {
        switch (group) {
            case 1:
                if (questionNumber <= 3) return "\u7b80\u5355\u4e3b\u8c13\u7ed3\u6784\u8868\u8fbe";
                if (questionNumber <= 6) return "\u5e38\u89c1\u52a8\u5bbe\u7ed3\u6784\u8868\u8fbe";
                if (questionNumber <= 9) return "\u5355\u4e00\u5f62\u5bb9\u8bcd+\u540d\u8bcd\u7ec4\u5408\u8868\u8fbe";
                if (questionNumber <= 12) return "\u4e3b\u8c13\u5bbe\u7ed3\u6784\u8868\u8fbe";
                if (questionNumber <= 15) return "\u5426\u5b9a\u53e5\u8868\u8fbe";
                return "";
            case 2:
                if (questionNumber <= 3) return "\u53cc\u5bbe\u7ed3\u6784\u8868\u8fbe";
                if (questionNumber <= 6) return "\u4e00\u822c\u7591\u95ee\u53e5\u8868\u8fbe";
                if (questionNumber <= 9) return "\u7279\u6b8a\u7591\u95ee\u53e5\u8868\u8fbe";
                if (questionNumber <= 12) return "\u65b9\u4f4d\u7ed3\u6784\u8868\u8fbe";
                if (questionNumber <= 15) return "\u591a\u91cd\u4fee\u9970\u7ed3\u6784\u8868\u8fbe";
                return "";
            case 3:
                if (questionNumber <= 3) return "\u88ab\u52a8\u53e5\u8868\u8fbe";
                if (questionNumber <= 6) return "\u5b8c\u6210\u4f53\u53e5\u8868\u8fbe";
                if (questionNumber <= 9) return "\u526f\u8bcd\"\u90fd\"\u8868\u8fbe";
                if (questionNumber <= 12) return "\u8bed\u5e8f\u8868\u8fbe";
                if (questionNumber <= 15) return "\u65f6\u95f4\u987a\u5e8f\u53e5\u8868\u8fbe";
                return "";
            case 4:
                if (questionNumber <= 3) return "\u56e0\u679c\u590d\u53e5\u8868\u8fbe";
                if (questionNumber <= 6) return "\u8f6c\u6298\u53e5\u8868\u8fbe";
                if (questionNumber <= 9) return "\u8fdb\u884c\u4f53\u53e5\u8868\u8fbe";
                if (questionNumber <= 12) return "\u6761\u4ef6\u53e5\u8868\u8fbe";
                if (questionNumber <= 15) return "\u6bd4\u8f83\u53e5\u8868\u8fbe";
                if (questionNumber == 16) return "\u770b\u56fe\u8bb2\u6545\u4e8b\u53e5\u6cd5\u7ec4\u7ec7";
                return "";
            default:
                return "";
        }
    }

    private static boolean isLogicRelatedStructure(String structure) {
        return structure.contains("\u56e0\u679c")
                || structure.contains("\u6761\u4ef6")
                || structure.contains("\u65f6\u95f4\u987a\u5e8f")
                || structure.contains("\u88ab\u52a8")
                || structure.contains("\u8bed\u5e8f")
                || structure.contains("\u53cc\u91cd\u5426\u5b9a")
                || structure.contains("\u7bc7\u7ae0");
    }

    private static void updateStructureScore(Map<String, StructureScore> scoreMap, String structure, boolean result) {
        String key = safeText(structure);
        if (key.isEmpty()) {
            key = "\u672a\u5206\u7c7b\u53e5\u6cd5\u7406\u89e3\u7ed3\u6784";
        }
        StructureScore score = scoreMap.get(key);
        if (score == null) {
            score = new StructureScore();
            scoreMap.put(key, score);
        }
        score.total++;
        if (result) {
            score.correct++;
        }
    }

    private static void appendArray(JSONArray target, JSONArray source) {
        if (target == null || source == null) {
            return;
        }
        for (int i = 0; i < source.length(); i++) {
            target.put(source.opt(i));
        }
    }

    private static String formatAccuracyPercent(int correct, int total) {
        if (total <= 0) {
            return "0.00%";
        }
        return String.format(Locale.US, "%.2f%%", correct * 100.0d / total);
    }

    private static String describeRgOverallLevel(int correct, int total) {
        if (total <= 0) {
            return "\u6682\u65e0 RG \u53e5\u6cd5\u7406\u89e3\u6570\u636e";
        }
        double accuracy = correct * 100.0d / total;
        if (accuracy >= 80d) {
            return "\u53e5\u6cd5\u7406\u89e3\u57fa\u7840\u8f83\u7a33\u5b9a\uff0c\u4f46\u4ecd\u9700\u89c2\u5bdf\u590d\u6742\u7ed3\u6784\u5728\u8bed\u5883\u53d8\u5316\u65f6\u7684\u7406\u89e3\u8868\u73b0";
        }
        if (accuracy >= 60d) {
            return "\u53e5\u6cd5\u7406\u89e3\u5df2\u5177\u5907\u4e00\u5b9a\u57fa\u7840\uff0c\u4f46\u53e5\u6cd5\u5173\u7cfb\u548c\u4e8b\u4ef6\u903b\u8f91\u7406\u89e3\u4ecd\u4e0d\u7a33\u5b9a";
        }
        return "\u53e5\u6cd5\u7406\u89e3\u76ee\u524d\u5c1a\u672a\u8fbe\u5230\u9884\u671f\u53d1\u5c55\u6c34\u5e73\uff0c\u9700\u4ece\u57fa\u7840\u7ed3\u6784\u7406\u89e3\u9010\u6b65\u5efa\u7acb";
    }

    private static String describeSeOverallLevel(int correct, int total) {
        if (total <= 0) {
            return "\u6682\u65e0 SE \u53e5\u6cd5\u8868\u8fbe\u6570\u636e";
        }
        double accuracy = correct * 100.0d / total;
        if (accuracy >= 80d) {
            return "\u53e5\u6cd5\u8868\u8fbe\u57fa\u7840\u8f83\u7a33\u5b9a\uff0c\u4f46\u8f83\u590d\u6742\u53e5\u578b\u548c\u4f7f\u7528\u573a\u666f\u4ecd\u9700\u7ee7\u7eed\u6cdb\u5316";
        }
        if (accuracy >= 60d) {
            return "\u53e5\u6cd5\u8868\u8fbe\u5df2\u5177\u5907\u4e00\u5b9a\u57fa\u7840\uff0c\u4f46\u590d\u6742\u53e5\u578b\u3001\u5b8c\u6574\u5e94\u7b54\u4e0e\u60c5\u5883\u5316\u8f93\u51fa\u4ecd\u4e0d\u7a33\u5b9a";
        }
        return "\u53e5\u6cd5\u8868\u8fbe\u76ee\u524d\u5c1a\u672a\u8fbe\u5230\u9884\u671f\u53d1\u5c55\u6c34\u5e73\uff0c\u9700\u4ece\u57fa\u7840\u53e5\u578b\u4ea7\u51fa\u4e0e\u4eff\u8bf4\u8868\u8fbe\u9010\u6b65\u5efa\u7acb";
    }

    private static String buildCombinedSyntaxOverallLevel(SyntaxProfile rgProfile, SyntaxProfile seProfile) {
        if (rgProfile.total <= 0 && seProfile.total <= 0) {
            return "\u6682\u65e0\u53ef\u7528\u7684 syntax \u53e5\u6cd5\u8bc4\u4f30\u6570\u636e";
        }
        if (rgProfile.total > 0 && seProfile.total > 0) {
            return "\u53e5\u6cd5\u7406\u89e3\u4e0e\u53e5\u6cd5\u8868\u8fbe\u5747\u9700\u7ed3\u5408\u5e74\u9f84\u53d1\u5c55\u6c34\u5e73\u7ee7\u7eed\u5e72\u9884\uff0c\u90e8\u5206\u57fa\u7840\u53e5\u578b\u5df2\u51fa\u73b0\uff0c\u4f46\u590d\u6742\u53e5\u6cd5\u7ed3\u6784\u5728\u7406\u89e3\u6216\u8868\u8fbe\u4e0a\u4ecd\u4e0d\u7a33\u5b9a\u3002";
        }
        return rgProfile.total > 0 ? rgProfile.overallLevel : seProfile.overallLevel;
    }

    private static List<String> mergeTaggedStructures(String firstTag, List<String> first,
                                                      String secondTag, List<String> second) {
        List<String> out = new ArrayList<>();
        addTaggedStructures(out, firstTag, first);
        addTaggedStructures(out, secondTag, second);
        return out;
    }

    private static void addTaggedStructures(List<String> out, String tag, List<String> values) {
        if (out == null || values == null) {
            return;
        }
        for (String value : values) {
            String text = safeText(value);
            if (!text.isEmpty()) {
                out.add(tag + "：" + text);
            }
        }
    }

    private static List<String> intersectStructures(List<String> first, List<String> second) {
        List<String> out = new ArrayList<>();
        if (first == null || second == null) {
            return out;
        }
        for (String left : first) {
            String normalizedLeft = normalizeStructureFamily(left);
            if (normalizedLeft.isEmpty()) {
                continue;
            }
            for (String right : second) {
                if (normalizedLeft.equals(normalizeStructureFamily(right)) && !out.contains(normalizedLeft)) {
                    out.add(normalizedLeft);
                    break;
                }
            }
        }
        return out;
    }

    private static String normalizeStructureFamily(String value) {
        String text = safeText(value);
        if (text.isEmpty()) {
            return "";
        }
        return text.replace("\u7406\u89e3", "").replace("\u8868\u8fbe", "").trim();
    }

    private static String buildCombinedExtractionNote(SyntaxProfile rgProfile, SyntaxProfile seProfile) {
        return "RG\uff1a" + safeText(rgProfile.extractionNote) + " SE\uff1a" + safeText(seProfile.extractionNote);
    }

    private static final class ArticulationProfile {
        int totalItems;
        int completedItems;
        int totalSyllables;
        int correctSyllables;
        int multisyllabicWords;
        int multisyllabicStableWords;
        String overallLevel = "";
        String multisyllabicRetention = "";
        String connectedSpeechCarryover = "";
        String extractionNote = "";
        final List<String> masteredPhonemes = new ArrayList<>();
        final List<String> focusPhonemes = new ArrayList<>();
        final List<String> unstablePhonemes = new ArrayList<>();
        final List<String> observedPhonemes = new ArrayList<>();
        final List<String> wordPositionStable = new ArrayList<>();
        final List<String> wordPositionFocus = new ArrayList<>();
        final List<String> wordPositionUnstable = new ArrayList<>();
        final List<String> phonologyProcesses = new ArrayList<>();
    }

    private static final class PrelinguisticProfile {
        int total;
        int achieved;
        String overallLevel = "";
        final List<String> mastered = new ArrayList<>();
        final List<String> focus = new ArrayList<>();
        final List<String> unstable = new ArrayList<>();
        final List<String> observed = new ArrayList<>();
        final JSONArray coreDomains = new JSONArray();
        String extractionNote = "";
    }

    private static final class StructureScore {
        int total;
        int correct;
    }

    private static final class FallbackProgress {
        int group = 1;
        int groupStartIndex = 0;
    }

    private static final class SyntaxProfile {
        int total;
        int correct;
        String overallLevel = "";
        final List<String> mastered = new ArrayList<>();
        final List<String> focus = new ArrayList<>();
        final List<String> unstable = new ArrayList<>();
        final List<String> logicRelated = new ArrayList<>();
        final List<String> observedStructures = new ArrayList<>();
        String extractionNote = "";
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

    public static JSONObject loadModuleReport(JSONObject childData, String moduleType) {
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

        String key = normalizeModuleType(moduleType);
        JSONObject direct = optJSONObjectFlexible(reports, key);
        if (direct != null) {
            return direct;
        }

        // Backward compatibility aliases.
        if ("prelinguistic".equals(key)) {
            return optJSONObjectFlexible(reports, PRELINGUISTIC_KEY);
        }
        if ("social".equals(key)) {
            return optJSONObjectFlexible(reports, SOCIAL_KEY);
        }
        if ("articulation".equals(key)) {
            return optJSONObjectFlexible(reports, "A");
        }
        return null;
    }

    public static JSONObject saveModuleInterventionGuide(JSONObject childData,
                                                         String moduleType,
                                                         JSONObject interventionGuide) throws JSONException {
        if (childData == null) {
            return interventionGuide;
        }
        JSONObject reports = optJSONObjectFlexible(childData, "moduleReports");
        if (reports == null) {
            reports = new JSONObject();
            childData.put("moduleReports", reports);
        }

        String key = normalizeModuleType(moduleType);
        JSONObject moduleReport = optJSONObjectFlexible(reports, key);
        if (moduleReport == null) {
            moduleReport = new JSONObject();
            reports.put(key, moduleReport);
        }
        moduleReport.put("interventionGuide", interventionGuide == null ? new JSONObject() : interventionGuide);
        return moduleReport;
    }

    public static JSONObject loadModuleInterventionGuide(JSONObject childData, String moduleType) {
        JSONObject moduleReport = loadModuleReport(childData, moduleType);
        if (moduleReport == null) {
            return null;
        }
        JSONObject guide = optJSONObjectFlexible(moduleReport, "interventionGuide");
        if (guide == null) {
            guide = optJSONObjectFlexible(moduleReport, "intervention_guide");
        }
        return guide;
    }

    public static void saveTreatmentPlanSummary(JSONObject childData, JSONObject summary) throws JSONException {
        if (childData == null) {
            return;
        }
        childData.put("treatmentPlanSummary", summary == null ? new JSONObject() : summary);
    }

    public static JSONObject loadTreatmentPlanSummary(JSONObject childData) {
        if (childData == null) {
            return null;
        }
        return optJSONObjectFlexible(childData, "treatmentPlanSummary");
    }

    public static JSONObject buildSocialReport(int totalScore, List<String> strengths, List<String> inProgress, List<String> weaknesses) {
        JSONObject report = new JSONObject();
        try {
            int measuredItemCount = (strengths == null ? 0 : strengths.size())
                    + (inProgress == null ? 0 : inProgress.size())
                    + (weaknesses == null ? 0 : weaknesses.size());
            report.put("totalScore", totalScore);
            report.put("measuredItemCount", measuredItemCount);
            report.put("strengths", toJsonArray(strengths));
            report.put("inProgress", toJsonArray(inProgress));
            report.put("weaknesses", toJsonArray(weaknesses));
            report.put("overallAssessment", getSocialOverallAssessment(totalScore, measuredItemCount));
            report.put("recommendations", getSocialRecommendations(strengths, inProgress, weaknesses));
        } catch (JSONException ignored) {
        }
        return report;
    }

    private static String getSocialOverallAssessment(int totalScore, int measuredItemCount) {
        if (measuredItemCount <= 0) {
            return "本次社交模块暂无可解释的已测题目，建议先确认实际测量的组别和答题记录。";
        }
        double ratio = totalScore / (measuredItemCount * 2.0d);
        if (ratio >= 0.75d) {
            return "在本次实际测量的社交能力范围内，儿童整体表现较好，相关能力多已开始建立，但仍需关注少数不稳定或未出现的社交能力。";
        }
        if (ratio >= 0.45d) {
            return "在本次实际测量的社交能力范围内，儿童部分能力已出现，但整体表现仍呈现不均衡，仍有若干能力尚未稳定发展，需继续支持与练习。";
        }
        return "在本次实际测量的社交能力范围内，儿童仍有多项核心能力未稳定建立，建议围绕已测组别的基础社交能力持续进行引导和干预。";
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
