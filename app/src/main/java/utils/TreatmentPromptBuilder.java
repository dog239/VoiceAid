package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TreatmentPromptBuilder {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String REQUIRED_PLACEHOLDER = "\u672a\u63d0\u4f9b";
    private static final String BULLET_SYMBOL = "\u2022";
    private static final String SCHEMA = "{\n" +
            "  \"case_summary\": {\n" +
            "    \"age_months\": 0,\n" +
            "    \"chief_complaint\": \"\",\n" +
            "    \"key_findings\": [],\n" +
            "    \"suspected_diagnosis\": [],\n" +
            "    \"risk_flags\": []\n" +
            "  },\n" +
            "  \"overall_goals\": {\n" +
            "    \"within_4_weeks\": [],\n" +
            "    \"within_12_weeks\": [],\n" +
            "    \"within_24_weeks\": []\n" +
            "  },\n" +
            "  \"module_plan\": {\n" +
            "    \"speech_sound\": {\n" +
            "      \"articulation\": {\n" +
            "        \"overall_summary\": {\n" +
            "          \"level\": \"below_age_expected|age_expected|uncertain\",\n" +
            "          \"intelligibility_level\": \"\",\n" +
            "          \"diagnosis\": \"\",\n" +
            "          \"clinical_suggestions\": \"\",\n" +
            "          \"text\": \"\"\n" +
            "        },\n" +
            "        \"mastered\": { \"intro\": \"\", \"items\": [] },\n" +
            "        \"not_mastered_overview\": { \"text\": \"\" },\n" +
            "        \"focus\": { \"title\": \"\", \"items\": [], \"note\": \"\" },\n" +
            "        \"unstable\": { \"title\": \"\", \"items\": [] },\n" +
            "        \"smart_goal\": {\n" +
            "          \"cycle_weeks\": \"8-10\",\n" +
            "          \"support\": [\"\u89c6\u89c9\u63d0\u793a\", \"\u542c\u89c9\u63d0\u793a\"],\n" +
            "          \"level\": \"\u5355\u8bcd\",\n" +
            "          \"target_sounds\": [],\n" +
            "          \"accuracy_threshold\": 0.80,\n" +
            "          \"stability_rule\": \"\u8fde\u7eed\u4e24\u6b21\u8bc4\u4f30\u4fdd\u6301\u7a33\u5b9a\",\n" +
            "          \"text\": \"\"\n" +
            "        },\n" +
            "        \"home_guidance\": { \"items\": [] }\n" +
            "      },\n" +
            "      \"targets\": [],\n" +
            "      \"methods\": [],\n" +
            "      \"sample_activities\": [],\n" +
            "      \"home_practice\": [],\n" +
            "      \"metrics\": [],\n" +
            "      \"stages\": [\n" +
            "        { \"name\": \"\u9636\u6bb51\uff1a\u542c\u8fa8\u4e0e\u6ce8\u610f\", \"focus\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
            "        { \"name\": \"\u9636\u6bb52\uff1a\u6a21\u4eff\u4e0e\u63a7\u5236\", \"focus\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
            "        { \"name\": \"\u9636\u6bb53\uff1a\u8bcd/\u77ed\u8bed\u5c42\u7ea7\", \"focus\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
            "        { \"name\": \"\u9636\u6bb54\uff1a\u65e5\u5e38\u6cdb\u5316\", \"focus\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"prelinguistic\": { \"key_findings\": [], \"targets\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
            "    \"vocabulary\": { \"targets\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
            "    \"syntax\": { \"targets\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
            "    \"social_pragmatics\": { \"targets\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] }\n" +
            "  },\n" +
            "  \"schedule_recommendation\": {\n" +
            "    \"sessions_per_week\": 0,\n" +
            "    \"minutes_per_session\": 0,\n" +
            "    \"review_in_weeks\": 0\n" +
            "  },\n" +
            "  \"notes_for_therapist\": [],\n" +
            "  \"notes_for_parents\": []\n" +
            "}";

    private TreatmentPromptBuilder() {
    }

    public static String buildUserPrompt(JSONObject childData) throws JSONException {
        JSONObject info = childData != null ? childData.optJSONObject("info") : null;
        JSONObject evaluations = childData != null ? childData.optJSONObject("evaluations") : null;

        String birthDate = info != null ? info.optString("birthDate", "") : "";
        String testDate = info != null ? info.optString("testDate", "") : "";
        int ageMonths = calculateAgeMonths(birthDate, testDate);
        if (ageMonths < 0) {
            ageMonths = 0;
        }
        String chiefComplaint = info != null ? safeText(info.optString("chiefComplaint", "")) : "";
        if (chiefComplaint.isEmpty()) {
            chiefComplaint = "\u65e0/\u672a\u63d0\u4f9b";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("\u786c\u7ea6\u675f\uff1a\u6240\u6709 value \u5fc5\u987b\u4e3a\u7b80\u4f53\u4e2d\u6587\u4e34\u5e8a\u8868\u8ff0\uff0c\u51fa\u73b0\u82f1\u6587\u53e5\u5b50\u89c6\u4e3a\u4e0d\u5408\u683c\u9700\u6539\u5199\uff0c\u4e14\u4e0d\u8981\u4fee\u6539 SCHEMA \u7684\u82f1\u6587 key\u3002\n");
        prompt.append("\u786C\u6027\u8981\u6C42\uFF1A\u53EA\u8F93\u51FA\u4E25\u683C\u5408\u6CD5\u7684JSON\uFF1BJSON\u952E\u4FDD\u6301\u82F1\u6587\u4E0D\u53D8\uFF1B\u6240\u6709\u503C\u4EE5\u4E2D\u6587\u4E3A\u4E3B\u7684\u4E34\u5E8A\u8868\u8FF0\uFF1B\u5141\u8BB8\u5C11\u91CF\u7F29\u5199\uFF08PST/PN/NWR/SLP/ASD/ADHD/SSD/DLD\uFF09\uFF1B\u9664JSON\u5916\u4E0D\u8981\u8F93\u51FA\u5176\u4ED6\u6587\u5B57\u3002\n");
        if (ageMonths == 0) {
            prompt.append("\u5e74\u9f84\uff08\u6708\uff09\uff1a0\uff08\u4fe1\u606f\u4e0d\u8db3\uff0c\u9700\u4eba\u5de5\u786e\u8ba4\uff09\n");
        } else {
            prompt.append("\u5e74\u9f84\uff08\u6708\uff09\uff1a").append(ageMonths).append("\n");
        }
        prompt.append("\u4e3b\u8bc9\uff1a").append(chiefComplaint).append("\n");
        prompt.append("\u8bc4\u4f30\u6458\u8981\uff1a\n");
        prompt.append(buildEvaluationSummary(evaluations));
        prompt.append("\n");

        ArticulationDataParser.ClinicalHeader articulationHeader = ArticulationDataParser.parseClinicalHeader(evaluations);
        ArticulationDataParser.PhonemeSummary phonemeSummary = ArticulationDataParser.parsePhonemeSummary(
                evaluations == null ? null : evaluations.optJSONArray("A")
        );
        String intelligibility = normalizeRequiredValue(articulationHeader.intelligibilityLevel);
        String diagnosis = normalizeOptionalValue(articulationHeader.diagnosisText);
        String suggestions = normalizeRequiredValue(articulationHeader.suggestions);
        String masteredBlock = formatBulletBlock(phonemeSummary.masteredPhonemes, ArticulationPlanHelper.MISSING_DETAIL_HINT);
        String targetBlock = formatBulletBlock(phonemeSummary.targetPhonemes, ArticulationPlanHelper.MISSING_DETAIL_HINT);
        PreLinguisticDataParser.PreLinguisticHeader preHeader = PreLinguisticDataParser.parseHeader(evaluations);
        String prelinguisticHeader = buildPrelinguisticHeaderText(preHeader);

        prompt.append("\u3010\u6784\u97f3\u6a21\u5757\u5ba2\u89c2\u6570\u636e\u5934\uff08\u4ec5\u7528\u4e8e speech_sound.articulation\uff09\u3011\n");
        prompt.append("\u6e05\u6670\u5ea6\u7b49\u7ea7\uff1a").append(intelligibility).append("\n");
        if (!diagnosis.isEmpty()) {
            prompt.append("\u8bca\u65ad\uff1a").append(diagnosis).append("\n");
        }
        prompt.append("\u8bc4\u4f30\u5efa\u8bae\uff1a").append(suggestions).append("\n");
        prompt.append("Mastered_Phonemes:\n").append(masteredBlock).append("\n");
        prompt.append("Target_Phonemes:\n").append(targetBlock).append("\n");

        prompt.append("\u3010\u524d\u8bed\u8a00\u6a21\u5757\u5ba2\u89c2\u6570\u636e\u5934\uff08\u4ec5\u7528\u4e8e module_plan.prelinguistic\uff09\u3011\n");
        prompt.append(prelinguisticHeader).append("\n");
        prompt.append("\u5171\u540c\u6ce8\u610f\u8f83\u5dee\u6807\u8bb0\uff1a").append(preHeader.jointAttentionWeak ? "\u662f" : "\u5426").append("\n");
        prompt.append("\u6a21\u4eff\u8f83\u5dee\u6807\u8bb0\uff1a").append(preHeader.imitationWeak ? "\u662f" : "\u5426").append("\n");

        prompt.append("\u751f\u6210\u8981\u6c42\uff08\u4e25\u683c\u6267\u884c\uff09\uff1a\n");
        prompt.append("1) \u6240\u6709 JSON \u5b57\u7b26\u4e32\u503c\u4f7f\u7528 \\\\n \u8fdb\u884c\u903b\u8f91\u6362\u884c\uff1b\u5df2\u638c\u63e1/\u91cd\u70b9\u5173\u6ce8\u5217\u8868\u9879\u5fc5\u987b\u4ee5 \"").append(BULLET_SYMBOL).append("\" \u5f00\u5934\u3002\n");
        prompt.append("2) \u751f\u6210\u6784\u97f3\u8ba1\u5212\u65f6\u53ea\u80fd\u4f7f\u7528\u6784\u97f3\u6a21\u5757\u6570\u636e\uff1b\u751f\u6210\u524d\u8bed\u8a00\u8ba1\u5212\u65f6\u53ea\u80fd\u4f7f\u7528\u524d\u8bed\u8a00\u6a21\u5757\u6570\u636e\uff0c\u7981\u6b62\u6570\u636e\u4e32\u53f0\u3002\n");
        prompt.append("3) speech_sound.articulation.overall_summary.intelligibility_level/diagnosis/clinical_suggestions \u5fc5\u987b\u4e0e\u5ba2\u89c2\u6570\u636e\u5934\u4e00\u81f4\uff0c\u5176\u4e2d intelligibility_level \u4e0e clinical_suggestions \u5fc5\u987b\u4fdd\u7559\u3002\n");
        prompt.append("4) overall_summary.text \u5fc5\u987b\u4ee5\u5ba2\u89c2\u6570\u636e\u5934\u4e09\u884c\u5f00\u5934\uff08\u4f7f\u7528 \\\\n \u6362\u884c\uff0c\u8bca\u65ad\u4e3a\u7a7a\u5219\u7701\u7565\u8bca\u65ad\u884c\uff09\uff0c\u518d\u6309\u300a\u6784\u97f3\u6a21\u5757.docx\u300b\u6a21\u677f\u8bdd\u672f\u6269\u5c55\u3002\n");
        prompt.append("5) mastered.items \u5fc5\u987b\u9010\u6761\u7b49\u4e8e Mastered_Phonemes\uff1bfocus.items \u5fc5\u987b\u9010\u6761\u7b49\u4e8e Target_Phonemes\u3002\n");
        prompt.append("6) speech_sound.articulation \u5fc5\u987b\u6309\u300a\u6784\u97f3\u6a21\u5757.docx\u300b\u7ed3\u6784\u8f93\u51fa\u4e03\u6bb5\u5185\u5bb9\uff0c\u5e76\u6ee1\u8db3 SMART \u7ea6\u675f\uff1a8-10 \u5468\u3001\u89c6\u89c9+\u542c\u89c9\u63d0\u793a\u3001\u5355\u8bcd\u5c42\u7ea7\u300180%\u8fbe\u6807\u3001\u8fde\u7eed\u4e24\u6b21\u8bc4\u4f30\u4fdd\u6301\u7a33\u5b9a\u3002\n");
        prompt.append("7) module_plan.prelinguistic.key_findings \u7b2c\u4e00\u6761\u5fc5\u987b\u4e3a\u5ba2\u89c2\u7ed3\u679c\u5934\uff08\u7ef4\u5ea6\u8868\u73b0 + \u8bc4\u4f30\u5efa\u8bae\uff09\uff0c\u4f7f\u7528 \\\\n \u5206\u884c\u3002\n");
        prompt.append("8) module_plan.prelinguistic \u6587\u672c\u5fc5\u987b\u53c2\u8003\u300a\u524d\u8bed\u8a00\u6a21\u5757.docx\u300b\u6a21\u677f\u8bdd\u672f\uff0c\u5305\u542b\u201c\u8bc4\u4f30\u7ed3\u679c\uff08\u6574\u4f53\uff09/\u5df2\u638c\u63e1/\u672a\u638c\u63e1/\u91cd\u70b9\u5173\u6ce8/\u4e0d\u7a33\u5b9a/SMART/\u5bb6\u5ead\u6307\u5bfc\u201d\u7684\u7ed3\u6784\u8868\u8fbe\u3002\n");
        prompt.append("9) \u82e5\u5171\u540c\u6ce8\u610f\u8f83\u5dee\uff0ctargets/activities \u5fc5\u987b\u4ee5\u5efa\u7acb\u773c\u795e\u63a5\u89e6/\u8ffd\u89c6\u4e3a\u6838\u5fc3\uff1b\u82e5\u6a21\u4eff\u8f83\u5dee\uff0ctargets/activities \u5fc5\u987b\u4ee5\u52a8\u4f5c\u6a21\u4eff/\u58f0\u97f3\u6a21\u4eff\u4e3a\u6838\u5fc3\u3002\n");
        prompt.append("10) \u6587\u98ce\u5fc5\u987b\u4f7f\u7528\u6a21\u677f\u53e5\u5f0f\uff0c\u4f8b\u5982\uff1a\u76ee\u524d\u7684\u793e\u4ea4\u4e92\u52a8\u57fa\u7840\u8f83\u5f31\uff0c\u5efa\u8bae\u4ece\u2026\u2026\n");
        prompt.append("\u5982\u65e0\u6cd5\u83b7\u53d6\u7ec6\u7c92\u5ea6\u76ee\u6807\u97f3\u6570\u636e\uff0c\u5217\u8868\u4e0d\u8981\u8f93\u51fa\u201c\u672a\u63d0\u4f9b/\u5f85\u8bc4\u4f30\u201d\uff0c\u4f7f\u7528\u4e00\u53e5\u7b80\u77ed\u8bf4\u660e\uff1a\u672c\u6b21\u672a\u83b7\u53d6\u5230\u53ef\u7528\u4e8e\u5206\u9879\u7edf\u8ba1\u7684\u6570\u636e\uff0c\u5efa\u8bae\u8865\u6d4b\u6784\u97f3\u5206\u9879\u4ee5\u7ec6\u5316\u76ee\u6807\u97f3\u3002\n");
        prompt.append("\u8f93\u51fa\u8981\u6c42\uff1a\n");
        prompt.append("1) \u53ea\u8f93\u51fa\u4e25\u683c\u5408\u6cd5\u7684JSON\uff0c\u4e0d\u8981\u8f93\u51fa\u5176\u4ed6\u6587\u5b57\u3002\n");
        prompt.append("2) JSON\u952E\u4FDD\u6301\u82F1\u6587\u4E0D\u53D8\uFF1B\u6240\u6709\u503C\u4EE5\u4E2D\u6587\u4E3A\u4E3B\u7684\u4E34\u5E8A\u8868\u8FF0\uFF1B\u5141\u8BB8\u5C11\u91CF\u7F29\u5199\uFF08PST/PN/NWR/SLP/ASD/ADHD/SSD/DLD\uFF09\uFF1B\u9664JSON\u5916\u4E0D\u8981\u8F93\u51FA\u5176\u4ED6\u6587\u5B57\u3002\n");
        prompt.append("3) \u4e0d\u8981\u5305\u542b\u59d3\u540d\u3001\u7f16\u53f7\u3001\u5730\u70b9\u7b49\u4efb\u4f55\u53ef\u8bc6\u522b\u4e2a\u4eba\u4fe1\u606f\u3002\n");
        prompt.append("4) \u82e5\u5e74\u9f84\uff08\u6708\uff09\u672a\u77e5\uff0cage_months=0\uff0c\u5e76\u5728notes_for_therapist\u8bf4\u660e\u9700\u4eba\u5de5\u786e\u8ba4\u3002\n");
        prompt.append("\u8bf7\u4e25\u683c\u9075\u5faa\u4ee5\u4e0bJSON\u7ed3\u6784\uff1a\n");
        prompt.append(SCHEMA);
        return prompt.toString();
    }

    private static String buildEvaluationSummary(JSONObject evaluations) {
        if (evaluations == null) {
            return "\u65e0\u8bc4\u4f30\u6570\u636e\u3002";
        }
        SpeechSoundClinical clinical = extractSpeechSoundClinical(evaluations, evaluations.optJSONArray("A"));
        StringBuilder summary = new StringBuilder();
        summary.append("- \u8bed\u97f3/\u6784\u97f3\uff08A\uff09: ")
                .append(formatSpeechSound(evaluations.optJSONArray("A"), clinical)).append("\n");
        summary.append("- \u8bcd\u6c47\uff08E/RE/S/NWR\uff09: ").append(formatVocabulary(evaluations)).append("\n");
        summary.append("- \u8bed\u6cd5\u7406\u89e3\uff08RG\uff09: ").append(formatBooleanAccuracy(evaluations.optJSONArray("RG"), "result")).append("\n");
        summary.append("- \u53d9\u8ff0\uff08PST/PN\uff09: ").append(formatNarrative(evaluations)).append("\n");
        summary.append("- \u8bed\u524d\uff08PL\uff09: ").append(buildPrelinguisticSummary(evaluations));
        return summary.toString();
    }

    private static String formatSpeechSound(JSONArray array, SpeechSoundClinical clinical) {
        String summary = formatSpeechSoundSummary(clinical);
        String performance = summarizeSpeechSound(array);
        if (!performance.isEmpty()) {
            summary = summary + "\uff1b\u6d4b\u8bc4\u8868\u73b0\uff1a" + performance;
        }
        return summary + "\u3002\u8f93\u51fa\u5fc5\u987b\u5305\u542b speech_sound.articulation \u7684\u4e03\u6bb5\uff0c\u5e76\u6ee1\u8db3 SMART \u7ea6\u675f\uff1a8-10\u5468\u3001\u89c6\u89c9+\u542c\u89c9\u63d0\u793a\u3001\u9ed8\u8ba4\u5355\u8bcd\u5c42\u7ea7\u300180%\u8fbe\u6807\u3001\u8fde\u7eed\u4e24\u6b21\u8bc4\u4f30\u4fdd\u6301\u7a33\u5b9a\u3002";
    }

    public static String buildSpeechSoundSummary(JSONArray array) {
        SpeechSoundClinical clinical = extractSpeechSoundClinical(null, array);
        return formatSpeechSoundSummary(clinical);
    }

    public static String buildVocabularySummary(JSONObject evaluations) {
        if (evaluations == null) {
            return "\u672a\u4f5c\u7b54";
        }
        return formatVocabulary(evaluations);
    }

    public static String buildSyntaxSummary(JSONObject evaluations) {
        if (evaluations == null) {
            return "\u672a\u4f5c\u7b54";
        }
        return formatBooleanAccuracy(evaluations.optJSONArray("RG"), "result");
    }

    public static String buildNarrativeSummary(JSONObject evaluations) {
        if (evaluations == null) {
            return "\u672a\u4f5c\u7b54";
        }
        return formatNarrative(evaluations);
    }

    private static String buildPrelinguisticSummary(JSONObject evaluations) {
        PreLinguisticDataParser.PreLinguisticHeader header = PreLinguisticDataParser.parseHeader(evaluations);
        if (header.dimensions.isEmpty()) {
            return "\u672a\u4f5c\u7b54";
        }
        StringBuilder sb = new StringBuilder();
        for (PreLinguisticDataParser.DimensionResult result : header.dimensions) {
            if (!shouldIncludeDimension(result)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\u3001");
            }
            sb.append(result.name).append("\uff1a").append(result.status);
            if (result.total > 0) {
                sb.append("\uff08").append(result.achieved).append("/").append(result.total).append("\uff09");
            }
        }
        if (sb.length() == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        return sb.toString();
    }

    private static String buildPrelinguisticHeaderText(PreLinguisticDataParser.PreLinguisticHeader header) {
        StringBuilder builder = new StringBuilder();
        if (header != null) {
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
            String suggestions = normalizeRequiredValue(header.suggestions);
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("\u8bc4\u4f30\u5efa\u8bae\uff1a").append(suggestions);
        }
        if (builder.length() == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        return builder.toString();
    }

    private static boolean shouldIncludeDimension(PreLinguisticDataParser.DimensionResult result) {
        if (result == null || result.name == null || result.name.trim().isEmpty()) {
            return false;
        }
        return !("\u5176\u4ed6".equals(result.name) && result.total == 0);
    }

    private static String formatBulletBlock(List<String> items, String emptyFallback) {
        List<String> safeItems = new ArrayList<>();
        if (items != null) {
            for (String item : items) {
                String text = safeText(item);
                if (!text.isEmpty()) {
                    safeItems.add(text);
                }
            }
        }
        if (safeItems.isEmpty()) {
            String fallback = safeText(emptyFallback);
            if (!fallback.isEmpty()) {
                safeItems.add(fallback);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < safeItems.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(BULLET_SYMBOL).append(safeItems.get(i));
        }
        return builder.toString();
    }

    private static String summarizeSpeechSound(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        int completed = countCompletedByTime(array);
        SpeechSoundAccuracy accuracy = calcSpeechSoundAccuracy(array);
        if (accuracy.total > 0) {
            double percent = (accuracy.correct * 100.0) / accuracy.total;
            return String.format(Locale.US, "\u5df2\u5b8c\u6210 %d/%d \u9898\uff0c\u6b63\u786e\u7387 %.2f%%\uff08%d/%d\uff09",
                    completed, array.length(), percent, accuracy.correct, accuracy.total);
        }
        return "\u5df2\u5b8c\u6210 " + completed + "/" + array.length() + " \u9898";
    }

    private static String formatVocabulary(JSONObject evaluations) {
        StringBuilder sb = new StringBuilder();
        sb.append("E ").append(formatBooleanAccuracy(evaluations.optJSONArray("E"), "result"));
        sb.append(", RE ").append(formatBooleanAccuracy(evaluations.optJSONArray("RE"), "result"));
        sb.append(", S ").append(formatBooleanAccuracy(evaluations.optJSONArray("S"), "result"));
        sb.append(", NWR ").append(formatNwrAccuracy(evaluations.optJSONArray("NWR")));
        return sb.toString();
    }

    private static String formatNarrative(JSONObject evaluations) {
        String pstAvg = formatAverageScore(evaluations.optJSONArray("PST"), "score");
        String pnAvg = formatAverageScore(evaluations.optJSONArray("PN"), "score");
        return "PST \u5e73\u5747\u5206 " + pstAvg + "\uff0cPN \u5e73\u5747\u5206 " + pnAvg;
    }

    private static String formatBooleanAccuracy(JSONArray array, String key) {
        BoolAccuracy accuracy = calcBooleanAccuracy(array, key);
        if (accuracy.total == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        double percent = (accuracy.correct * 100.0) / accuracy.total;
        return String.format(Locale.US, "\u6b63\u786e\u7387 %.2f%%\uff08%d/%d\uff09", percent, accuracy.correct, accuracy.total);
    }

    private static String formatNwrAccuracy(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        int correct = 0;
        int total = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            for (int j = 1; j <= 6; j++) {
                String key = "results" + j;
                if (obj.has(key) && !obj.isNull(key) && !"null".equals(obj.optString(key))) {
                    total++;
                    if (obj.optBoolean(key)) {
                        correct++;
                    }
                }
            }
        }
        if (total == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        double percent = (correct * 100.0) / total;
        return String.format(Locale.US, "\u6b63\u786e\u7387 %.2f%%\uff08%d/%d\uff09", percent, correct, total);
    }

    private static String formatAverageScore(JSONArray array, String key) {
        AvgScore avg = calcAverageScore(array, key);
        if (avg.count == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        double value = avg.sum / (double) avg.count;
        return String.format(Locale.US, "\u5e73\u5747\u5206 %.2f\uff08\u5171 %d \u9898\uff09", value, avg.count);
    }

    private static int countCompletedByTime(JSONArray array) {
        int completed = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null && obj.has("time") && !obj.isNull("time") && !"null".equals(obj.optString("time"))) {
                completed++;
            }
        }
        return completed;
    }

    private static SpeechSoundAccuracy calcSpeechSoundAccuracy(JSONArray array) {
        SpeechSoundAccuracy accuracy = new SpeechSoundAccuracy();
        if (array == null || array.length() == 0) {
            return accuracy;
        }
        if (hasPhonology(array)) {
            return accuracy;
        }
        for (int i = 0; i < array.length() && i < ImageUrls.A_proAns.length; i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null || !hasValidTime(obj)) {
                continue;
            }
            String target1 = safeText(obj.optString("target_tone1", ""));
            String target2 = safeText(obj.optString("target_tone2", ""));
            String expected1 = safeText(ImageUrls.A_proAns[i][0]);
            String expected2 = safeText(ImageUrls.A_proAns[i][1]);
            if (!expected1.isEmpty()) {
                accuracy.total++;
                if (expected1.equals(target1)) {
                    accuracy.correct++;
                }
            }
            if (!expected2.isEmpty()) {
                accuracy.total++;
                if (expected2.equals(target2)) {
                    accuracy.correct++;
                }
            }
        }
        return accuracy;
    }

    private static boolean hasPhonology(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null && obj.has("targetWord") && !obj.isNull("targetWord")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasValidTime(JSONObject obj) {
        return obj.has("time") && !obj.isNull("time") && !"null".equals(obj.optString("time"));
    }

    private static BoolAccuracy calcBooleanAccuracy(JSONArray array, String key) {
        BoolAccuracy accuracy = new BoolAccuracy();
        if (array == null) {
            return accuracy;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            if (obj.has(key) && !obj.isNull(key) && !"null".equals(obj.optString(key))) {
                accuracy.total++;
                if (obj.optBoolean(key)) {
                    accuracy.correct++;
                }
            }
        }
        return accuracy;
    }

    private static AvgScore calcAverageScore(JSONArray array, String key) {
        AvgScore avg = new AvgScore();
        if (array == null) {
            return avg;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            if (obj.has(key) && !obj.isNull(key) && obj.has("time") && !obj.isNull("time")) {
                avg.sum += obj.optInt(key, 0);
                avg.count++;
            }
        }
        return avg;
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

    private static String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\n", " ").trim();
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
        return isMissingValue(text) ? REQUIRED_PLACEHOLDER : text;
    }

    private static String normalizeOptionalValue(String value) {
        String text = safeText(value);
        return isMissingValue(text) ? "" : text;
    }

    private static SpeechSoundClinical extractSpeechSoundClinical(JSONObject evaluations, JSONArray evaluationsA) {
        SpeechSoundClinical clinical = extractSpeechSoundClinical(evaluations);
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

    private static SpeechSoundClinical extractSpeechSoundClinical(JSONObject evaluations) {
        SpeechSoundClinical clinical = new SpeechSoundClinical();
        if (evaluations == null) {
            return clinical;
        }
        ArticulationDataParser.ClinicalHeader header = ArticulationDataParser.parseClinicalHeader(evaluations);
        clinical.intelligibility = safeText(header.intelligibilityLevel);
        clinical.diagnosis = safeText(header.diagnosisText);
        clinical.suggestions = safeText(header.suggestions);
        return clinical;
    }

    private static SpeechSoundClinical extractSpeechSoundClinical(JSONArray array) {
        SpeechSoundClinical clinical = new SpeechSoundClinical();
        if (array == null) {
            return clinical;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            if (isMissingValue(clinical.intelligibility)) {
                String value = safeText(obj.optString("speech_intelligibility", ""));
                if (!isMissingValue(value)) {
                    clinical.intelligibility = value;
                }
            }
            if (isMissingValue(clinical.diagnosis)) {
                String value = safeText(obj.optString("clinical_diagnosis", ""));
                if (!isMissingValue(value)) {
                    clinical.diagnosis = value;
                }
            }
            if (isMissingValue(clinical.suggestions)) {
                String value = safeText(obj.optString("assessment_suggestions", ""));
                if (!isMissingValue(value)) {
                    clinical.suggestions = value;
                }
            }
        }
        return clinical;
    }

    private static String formatSpeechSoundSummary(SpeechSoundClinical clinical) {
        String intelligibility = normalizeRequiredValue(clinical == null ? "" : clinical.intelligibility);
        String suggestions = normalizeRequiredValue(clinical == null ? "" : clinical.suggestions);
        String diagnosis = normalizeOptionalValue(clinical == null ? "" : clinical.diagnosis);
        StringBuilder builder = new StringBuilder();
        builder.append("\u6e05\u6670\u5ea6\u7b49\u7ea7\uff1a").append(intelligibility);
        if (!diagnosis.isEmpty()) {
            builder.append("\uff1b\u8bca\u65ad\uff1a").append(diagnosis);
        }
        builder.append("\uff1b\u8bc4\u4f30\u5efa\u8bae\uff1a").append(suggestions);
        return builder.toString();
    }

    private static class BoolAccuracy {
        int total;
        int correct;
    }

    private static class AvgScore {
        int count;
        int sum;
    }

    private static class SpeechSoundAccuracy {
        int total;
        int correct;
    }

    private static class SpeechSoundClinical {
        String intelligibility;
        String diagnosis;
        String suggestions;
    }
}
