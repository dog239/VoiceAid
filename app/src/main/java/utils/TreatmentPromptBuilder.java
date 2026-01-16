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
            "      \"targets\": [],\n" +
            "      \"methods\": [],\n" +
            "      \"sample_activities\": [],\n" +
            "      \"home_practice\": [],\n" +
            "      \"metrics\": []\n" +
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
        if (array == null || array.length() == 0) {
            return "\u672a\u4f5c\u7b54";
        }
        int completed = countCompletedByTime(array);
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
}
