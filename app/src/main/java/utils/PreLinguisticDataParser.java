package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PreLinguisticDataParser {
    private static final String[] PRELINGUISTIC_KEYS = new String[]{
            "PL", "pl", "prelinguistic", "pre_linguistic", "prelinguistic_eval", "prelinguistic_evaluations", "P", "pre"
    };
    private static final String[] DIAGNOSIS_KEYS = new String[]{
            "prelinguistic_diagnosis_text", "prelinguistic_diagnosis", "pre_linguistic_diagnosis_text",
            "pre_linguistic_diagnosis", "pl_diagnosis_text", "pl_diagnosis", "prelinguistic_conclusion"
    };
    private static final String[] SUGGESTION_KEYS = new String[]{
            "prelinguistic_assessment_suggestions", "pre_linguistic_assessment_suggestions",
            "pl_assessment_suggestions", "prelinguistic_suggestions", "pre_linguistic_suggestions",
            "pl_suggestions", "assessment_suggestions_prelinguistic", "assessment_suggestions_pl"
    };

    private PreLinguisticDataParser() {
    }

    public static PreLinguisticHeader parseHeader(JSONObject evaluations) {
        PreLinguisticHeader header = new PreLinguisticHeader();
        if (evaluations == null) {
            return header;
        }
        Map<String, DimensionResult> dimensions = buildDefaultDimensions();
        JSONArray array = extractEvaluations(evaluations);
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) {
                    continue;
                }
                String skill = safeText(obj.optString("skill", ""));
                String key = resolveDimensionKey(skill);
                DimensionResult result = dimensions.get(key);
                if (result == null) {
                    result = dimensions.get(DimensionKeys.OTHER);
                }
                Integer score = extractScore(obj);
                if (score == null) {
                    continue;
                }
                result.total++;
                if (score == 1) {
                    result.achieved++;
                }
            }
        }
        for (DimensionResult result : dimensions.values()) {
            result.status = resolveStatus(result);
            header.dimensions.add(result);
        }
        header.diagnosisText = pickFirstNonEmpty(evaluations, DIAGNOSIS_KEYS);
        if (!isValidText(header.diagnosisText)) {
            header.diagnosisText = "";
        }
        header.suggestions = pickFirstNonEmpty(evaluations, SUGGESTION_KEYS);
        header.jointAttentionWeak = isWeak(dimensions.get(DimensionKeys.JOINT_ATTENTION));
        header.imitationWeak = isWeak(dimensions.get(DimensionKeys.IMITATION));
        return header;
    }

    private static Map<String, DimensionResult> buildDefaultDimensions() {
        Map<String, DimensionResult> map = new LinkedHashMap<>();
        map.put(DimensionKeys.JOINT_ATTENTION, new DimensionResult(DimensionKeys.JOINT_ATTENTION));
        map.put(DimensionKeys.IMITATION, new DimensionResult(DimensionKeys.IMITATION));
        map.put(DimensionKeys.PLAY, new DimensionResult(DimensionKeys.PLAY));
        map.put(DimensionKeys.COMMUNICATION, new DimensionResult(DimensionKeys.COMMUNICATION));
        map.put(DimensionKeys.OTHER, new DimensionResult(DimensionKeys.OTHER));
        return map;
    }

    private static JSONArray extractEvaluations(JSONObject evaluations) {
        if (evaluations == null) {
            return null;
        }
        for (String key : PRELINGUISTIC_KEYS) {
            Object value = evaluations.opt(key);
            if (value instanceof JSONArray) {
                return (JSONArray) value;
            }
        }
        return null;
    }

    private static Integer extractScore(JSONObject obj) {
        if (obj == null || !obj.has("score") || obj.isNull("score")) {
            return null;
        }
        return obj.optInt("score", 0);
    }

    private static String resolveDimensionKey(String skill) {
        String text = safeLower(skill);
        if (containsAny(text, "共同注意", "目光", "眼神", "追视", "轮流", "指示", "听指令", "指令", "回应", "joint attention")) {
            return DimensionKeys.JOINT_ATTENTION;
        }
        if (containsAny(text, "模仿", "imitat")) {
            return DimensionKeys.IMITATION;
        }
        if (containsAny(text, "玩", "游戏", "玩具", "play")) {
            return DimensionKeys.PLAY;
        }
        if (containsAny(text, "提要求", "沟通", "表达", "确认", "发出声音", "发声", "语音", "意图", "communic")) {
            return DimensionKeys.COMMUNICATION;
        }
        return DimensionKeys.OTHER;
    }

    private static boolean containsAny(String text, String... keywords) {
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

    private static String resolveStatus(DimensionResult result) {
        if (result == null || result.total == 0) {
            return "未评估";
        }
        if (result.achieved == result.total) {
            return "具备";
        }
        if (result.achieved == 0) {
            return "未具备";
        }
        return "部分具备";
    }

    private static boolean isWeak(DimensionResult result) {
        return result != null && result.total > 0 && result.achieved < result.total;
    }

    private static String pickFirstNonEmpty(JSONObject source, String... keys) {
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

    private static boolean isValidText(String value) {
        String text = safeText(value);
        if (text.isEmpty()) {
            return false;
        }
        return !"null".equalsIgnoreCase(text);
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.CHINA);
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class DimensionKeys {
        private static final String JOINT_ATTENTION = "共同注意";
        private static final String IMITATION = "模仿";
        private static final String PLAY = "游戏技能";
        private static final String COMMUNICATION = "沟通意图";
        private static final String OTHER = "其他";
    }

    public static final class PreLinguisticHeader {
        public final List<DimensionResult> dimensions = new ArrayList<>();
        public String diagnosisText = "";
        public String suggestions = "";
        public boolean jointAttentionWeak;
        public boolean imitationWeak;
    }

    public static final class DimensionResult {
        public final String name;
        public int achieved;
        public int total;
        public String status = "";

        DimensionResult(String name) {
            this.name = name;
        }
    }
}
