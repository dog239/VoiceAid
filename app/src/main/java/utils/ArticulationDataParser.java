package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArticulationDataParser {
    public static final String[] ARTICULATION_PHONEME_MAP = buildPhonemeMap();

    private ArticulationDataParser() {
    }

    public static ClinicalHeader parseClinicalHeader(JSONObject evaluations) {
        return extractClinicalHeader(evaluations);
    }

    public static PhonemeSummary parsePhonemeSummary(JSONArray evaluationsA) {
        PhonemeSummary summary = new PhonemeSummary();
        if (evaluationsA == null || ARTICULATION_PHONEME_MAP.length == 0) {
            return summary;
        }
        int limit = Math.min(evaluationsA.length(), ARTICULATION_PHONEME_MAP.length);
        Map<String, PhonemeStat> stats = new LinkedHashMap<>();
        for (int i = 0; i < limit; i++) {
            JSONObject obj = evaluationsA.optJSONObject(i);
            if (obj == null || !hasValidTime(obj)) {
                continue;
            }
            String phoneme = getPhonemeAt(i);
            if (phoneme.isEmpty()) {
                continue;
            }
            PhonemeStat stat = stats.get(phoneme);
            if (stat == null) {
                stat = new PhonemeStat(phoneme);
                stats.put(phoneme, stat);
            }
            stat.total++;
            if (isPhonemeCorrect(obj, phoneme)) {
                stat.correct++;
            }
        }
        for (PhonemeStat stat : stats.values()) {
            if (stat.total == 0) {
                continue;
            }
            if (stat.correct == stat.total) {
                summary.masteredPhonemes.add(stat.phoneme);
            } else {
                summary.targetPhonemes.add(stat.phoneme);
            }
        }
        return summary;
    }

    public static String getPhonemeAt(int index) {
        if (index < 0 || index >= ARTICULATION_PHONEME_MAP.length) {
            return "";
        }
        String value = ARTICULATION_PHONEME_MAP[index];
        return value == null ? "" : value;
    }

    private static ClinicalHeader extractClinicalHeader(JSONObject evaluations) {
        ClinicalHeader header = new ClinicalHeader();
        if (evaluations == null) {
            return header;
        }
        header.intelligibilityLevel = pickFirstNonEmpty(
                evaluations.optString("intelligibility_level", ""),
                evaluations.optString("speech_intelligibility", ""),
                evaluations.optString("intelligibility", "")
        );
        header.diagnosisText = pickFirstNonEmpty(
                evaluations.optString("diagnosis_text", ""),
                evaluations.optString("clinical_diagnosis", ""),
                evaluations.optString("diagnosis", "")
        );
        header.suggestions = pickFirstNonEmpty(
                evaluations.optString("suggestions", ""),
                evaluations.optString("assessment_suggestions", ""),
                evaluations.optString("clinical_suggestions", "")
        );
        if (!isValidDiagnosis(header.diagnosisText)) {
            header.diagnosisText = "";
        }
        return header;
    }

    private static String[] buildPhonemeMap() {
        String[] pinyin = ImageUrls.A_newImageUrlsPinyin;
        if (pinyin != null && pinyin.length > 0) {
            String[] map = new String[pinyin.length];
            for (int i = 0; i < pinyin.length; i++) {
                map[i] = normalizePhoneme(initialFromPinyin(pinyin[i]));
            }
            return map;
        }
        String[][] legacy = ImageUrls.A_proAns;
        if (legacy == null) {
            return new String[0];
        }
        String[] map = new String[legacy.length];
        for (int i = 0; i < legacy.length; i++) {
            String value = "";
            if (legacy[i] != null) {
                value = pickFirstNonEmpty(legacy[i][0], legacy[i][1]);
            }
            map[i] = normalizePhoneme(value);
        }
        return map;
    }

    private static String initialFromPinyin(String pinyin) {
        String text = safeText(pinyin).toLowerCase(Locale.US);
        if (text.isEmpty()) {
            return "";
        }
        if (text.startsWith("zh") || text.startsWith("ch") || text.startsWith("sh")) {
            return text.substring(0, 2);
        }
        if (text.startsWith("er")) {
            return "er";
        }
        return text.substring(0, 1);
    }

    private static boolean isPhonemeCorrect(JSONObject obj, String expectedPhoneme) {
        String expected = stripSlashes(expectedPhoneme);
        if (expected.isEmpty()) {
            return false;
        }
        List<String> initials = extractAnswerInitials(obj);
        if (initials.isEmpty()) {
            return false;
        }
        String normalizedExpected = expected.toLowerCase(Locale.US);
        for (String initial : initials) {
            String normalized = stripSlashes(initial).toLowerCase(Locale.US);
            if (!normalized.isEmpty() && normalizedExpected.equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> extractAnswerInitials(JSONObject obj) {
        List<String> initials = new ArrayList<>();
        if (obj == null) {
            return initials;
        }
        JSONArray answers = obj.optJSONArray("answerPhonology");
        if (answers == null) {
            return initials;
        }
        for (int i = 0; i < answers.length(); i++) {
            JSONObject item = answers.optJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONObject phonology = item.optJSONObject("phonology");
            if (phonology == null) {
                continue;
            }
            String initial = safeText(phonology.optString("initial", ""));
            if (!initial.isEmpty()) {
                initials.add(initial);
            }
        }
        return initials;
    }

    private static boolean hasValidTime(JSONObject obj) {
        return obj.has("time") && !obj.isNull("time") && !"null".equals(obj.optString("time"));
    }

    private static boolean isValidDiagnosis(String diagnosis) {
        String text = safeText(diagnosis);
        if (text.isEmpty()) {
            return false;
        }
        if ("未勾选".equals(text)) {
            return false;
        }
        return !"null".equalsIgnoreCase(text);
    }

    private static String pickFirstNonEmpty(String... values) {
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

    private static String normalizePhoneme(String value) {
        String text = safeText(value);
        if (text.isEmpty()) {
            return "";
        }
        if (text.startsWith("/") && text.endsWith("/")) {
            return text;
        }
        return "/" + text + "/";
    }

    private static String stripSlashes(String value) {
        String text = safeText(value);
        if (text.startsWith("/") && text.endsWith("/") && text.length() > 1) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class ClinicalHeader {
        public String intelligibilityLevel = "";
        public String diagnosisText = "";
        public String suggestions = "";
    }

    public static final class PhonemeSummary {
        public final List<String> masteredPhonemes = new ArrayList<>();
        public final List<String> targetPhonemes = new ArrayList<>();
    }

    private static final class PhonemeStat {
        final String phoneme;
        int total;
        int correct;

        PhonemeStat(String phoneme) {
            this.phoneme = phoneme;
        }
    }
}
