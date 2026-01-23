package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TreatmentPromptBuilder {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
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
            "        \"overall_summary\": { \"level\": \"below_age_expected|age_expected|uncertain\", \"text\": \"\" },\n" +
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
            "    \"prelinguistic\": { \"targets\": [], \"activities\": [], \"home_practice\": [], \"metrics\": [] },\n" +
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
        prompt.append("\u6784\u97f3\u6a21\u5757\u8f93\u51fa\u8981\u6c42\uff1aspeech_sound.articulation \u5fc5\u987b\u5305\u542b\u4e03\u6bb5\u4f9d\u6b21\u4e3a\uff1a\u8bc4\u4f30\u7ed3\u679c\uff08\u6574\u4f53\uff09\u3001\u5df2\u638c\u63e1\u80fd\u529b\u3001\u672a\u638c\u63e1\u80fd\u529b\u603b\u4f53\u8bf4\u660e\u3001\u91cd\u70b9\u5173\u6ce8\u80fd\u529b\u5217\u8868\u3001\u4e0d\u7a33\u5b9a\u80fd\u529b\u5217\u8868\u3001SMART\u5e72\u9884\u76ee\u6807\u3001\u5bb6\u5ead\u5e72\u9884\u6307\u5bfc\u5efa\u8bae\u5217\u8868\u3002\n");
        prompt.append("SMART\u5b57\u6bb5\u7ea6\u675f\uff1a\u5468\u671f8-10\u5468\uff0c\u63d0\u793a\u652f\u6301\u5305\u542b\u89c6\u89c9+\u542c\u89c9\uff0c\u8bad\u7ec3\u5c42\u7ea7\u9ed8\u8ba4\u5355\u8bcd\uff0c\u8fbe\u6807\u6807\u51c680%\uff0c\u7a33\u5b9a\u6027\u89c4\u5219\u4e3a\u8fde\u7eed\u4e24\u6b21\u8bc4\u4f30\u4fdd\u6301\u7a33\u5b9a\u3002\n");
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
        StringBuilder summary = new StringBuilder();
        summary.append("- \u8bed\u97f3/\u6784\u97f3\uff08A\uff09: ").append(formatSpeechSound(evaluations.optJSONArray("A"))).append("\n");
        summary.append("- \u8bcd\u6c47\uff08E/RE/S/NWR\uff09: ").append(formatVocabulary(evaluations)).append("\n");
        summary.append("- \u8bed\u6cd5\u7406\u89e3\uff08RG\uff09: ").append(formatBooleanAccuracy(evaluations.optJSONArray("RG"), "result")).append("\n");
        summary.append("- \u53d9\u8ff0\uff08PST/PN\uff09: ").append(formatNarrative(evaluations)).append("\n");
        summary.append("- \u8bed\u524d: \u6682\u65e0\u4e13\u95e8\u6a21\u5757\u6570\u636e\u3002");
        return summary.toString();
    }

    private static String formatSpeechSound(JSONArray array) {
        String summary = summarizeSpeechSound(array);
        return summary + "\u3002\u8f93\u51fa\u5fc5\u987b\u5305\u542b speech_sound.articulation \u7684\u4e03\u6bb5\uff0c\u5e76\u6ee1\u8db3 SMART \u7ea6\u675f\uff1a8-10\u5468\u3001\u89c6\u89c9+\u542c\u89c9\u63d0\u793a\u3001\u9ed8\u8ba4\u5355\u8bcd\u5c42\u7ea7\u300180%\u8fbe\u6807\u3001\u8fde\u7eed\u4e24\u6b21\u8bc4\u4f30\u4fdd\u6301\u7a33\u5b9a\u3002";
    }

    public static String buildSpeechSoundSummary(JSONArray array) {
        return summarizeSpeechSound(array);
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
}
