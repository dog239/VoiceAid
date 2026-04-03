package utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ChildBackgroundInfoHelper {

    private ChildBackgroundInfoHelper() {
    }

    public static JSONObject optBackgroundInfo(JSONObject info) {
        if (info == null) {
            return new JSONObject();
        }
        JSONObject object = info.optJSONObject("backgroundInfo");
        return object == null ? new JSONObject() : object;
    }

    public static List<String> buildReportLines(JSONObject backgroundInfo) {
        List<String> lines = new ArrayList<>();
        if (backgroundInfo == null) {
            return lines;
        }

        JSONObject basicCare = backgroundInfo.optJSONObject("basicCare");
        addLine(lines, "0-3岁照顾者", joinSelected(
                mapBoolText(basicCare, "caregiver0To3Parents", "父母"),
                mapBoolText(basicCare, "caregiver0To3Grandparents", "祖父母"),
                mapBoolText(basicCare, "caregiver0To3Nanny", "保姆"),
                mapBoolText(basicCare, "caregiver0To3Other", appendOther("其他", valueOf(basicCare, "caregiver0To3OtherText")))
        ));
        addLine(lines, "0-3岁语言环境", valueOf(basicCare, "language0To3"));
        addLine(lines, "3-6岁照顾者", joinSelected(
                mapBoolText(basicCare, "caregiver3To6Parents", "父母"),
                mapBoolText(basicCare, "caregiver3To6Grandparents", "祖父母"),
                mapBoolText(basicCare, "caregiver3To6Nanny", "保姆"),
                mapBoolText(basicCare, "caregiver3To6Other", appendOther("其他", valueOf(basicCare, "caregiver3To6OtherText")))
        ));
        addLine(lines, "3-6岁语言环境", valueOf(basicCare, "language3To6"));
        addLine(lines, "方言情况", valueOf(basicCare, "dialect"));

        JSONObject birthHistory = backgroundInfo.optJSONObject("birthHistory");
        addLine(lines, "分娩方式", mapDeliveryMethod(valueOf(birthHistory, "deliveryMethod")));
        addLine(lines, "出生/病史", joinSelected(
                mapBoolText(birthHistory, "otitisMedia", "中耳炎"),
                mapBoolText(birthHistory, "respiratoryDisease", "呼吸系统疾病"),
                mapBoolText(birthHistory, "headInjury", "头部受伤"),
                mapBoolText(birthHistory, "epilepsy", "癫痫"),
                mapBoolText(birthHistory, "lowWeight", "体重过轻"),
                mapBoolText(birthHistory, "incubator", appendOther("住保温箱", valueOf(birthHistory, "incubatorDays"))),
                mapBoolText(birthHistory, "jaundice", "黄疸"),
                mapBoolText(birthHistory, "meningitis", "脑膜炎"),
                mapBoolText(birthHistory, "cleftLipPalate", "唇腭裂"),
                mapBoolText(birthHistory, "umbilicalCordNeck", "脐带绕颈"),
                mapBoolText(birthHistory, "hypoxia", "缺氧"),
                mapBoolText(birthHistory, "medication", appendOther("用药", valueOf(birthHistory, "medicationText"))),
                mapBoolText(birthHistory, "other", valueOf(birthHistory, "otherText"))
        ));

        JSONObject growthDevelopment = backgroundInfo.optJSONObject("growthDevelopment");
        addLine(lines, "喂养方式", mapFeedingMethod(valueOf(growthDevelopment, "feedingMethod")));
        addLine(lines, "发育里程碑", joinSelected(
                delayedLabel(growthDevelopment, "smileStatus", "逗笑"),
                delayedLabel(growthDevelopment, "sitStatus", "独坐"),
                delayedLabel(growthDevelopment, "headControlStatus", "抬头"),
                delayedLabel(growthDevelopment, "crawlStatus", "爬行"),
                delayedLabel(growthDevelopment, "walkStatus", "行走"),
                delayedLabel(growthDevelopment, "vocalizationStatus", "发声"),
                delayedLabel(growthDevelopment, "singleWordStatus", "单词"),
                delayedLabel(growthDevelopment, "phraseStatus", "短语")
        ));

        JSONObject diagnosedDisorders = backgroundInfo.optJSONObject("diagnosedDisorders");
        addLine(lines, "既往诊断", joinSelected(
                mapBoolText(diagnosedDisorders, "none", "无"),
                mapBoolText(diagnosedDisorders, "developmentalDelay", "发育迟缓"),
                mapBoolText(diagnosedDisorders, "cerebralPalsy", "脑瘫"),
                mapBoolText(diagnosedDisorders, "autism", "孤独症"),
                mapBoolText(diagnosedDisorders, "downSyndrome", "唐氏综合征"),
                mapBoolText(diagnosedDisorders, "intellectualDisability", "智力障碍"),
                mapBoolText(diagnosedDisorders, "adhd", "多动/注意力缺陷"),
                mapBoolText(diagnosedDisorders, "other", valueOf(diagnosedDisorders, "otherText"))
        ));

        JSONObject generalDevelopment = backgroundInfo.optJSONObject("generalDevelopment");
        addLine(lines, "一般发展", joinSelected(
                abnormalLabel(generalDevelopment, "visionStatus", "视力"),
                abnormalLabel(generalDevelopment, "hearingStatus", "听力"),
                abnormalLabel(generalDevelopment, "eatingHabitStatus", "进食习惯"),
                mapBoolText(generalDevelopment, "eatingHabitChewingDifficulty", "咀嚼困难"),
                mapBoolText(generalDevelopment, "eatingHabitSwallowingDifficulty", "吞咽困难")
        ));

        JSONObject speechMotorFunction = backgroundInfo.optJSONObject("speechMotorFunction");
        addLine(lines, "说话动作与功能", joinSelected(
                abnormalLabel(speechMotorFunction, "lipsStatus", "双唇"),
                abnormalLabel(speechMotorFunction, "tongueStatus", "舌头"),
                abnormalLabel(speechMotorFunction, "jawStatus", "下颌"),
                abnormalLabel(speechMotorFunction, "velopharyngealStatus", "腭咽功能"),
                abnormalLabel(speechMotorFunction, "alternatingMotionStatus", "轮替动作"),
                abnormalLabel(speechMotorFunction, "salivaControlStatus", "口水控制"),
                abnormalLabel(speechMotorFunction, "breathingStatus", "呼吸"),
                abnormalLabel(speechMotorFunction, "voiceStatus", "嗓音"),
                abnormalLabel(speechMotorFunction, "speechEatingStatus", "进食"),
                mapBoolText(speechMotorFunction, "speechEatingChewingDifficulty", "咀嚼困难"),
                mapBoolText(speechMotorFunction, "speechEatingSwallowingDifficulty", "吞咽困难"),
                valueOf(speechMotorFunction, "other")
        ));

        JSONObject expressionMode = backgroundInfo.optJSONObject("expressionMode");
        addLine(lines, "表达方式", joinSelected(
                mapBoolText(expressionMode, "spokenLanguage", "口语表达"),
                mapBoolText(expressionMode, "nonverbal", "非口语表达"),
                mapBoolText(expressionMode, "nonverbalVoicePitch", "声音/音调变化"),
                mapBoolText(expressionMode, "nonverbalBodyLanguage", "肢体语言"),
                mapBoolText(expressionMode, "nonverbalAssistiveDevice", appendOther("辅助器具", valueOf(expressionMode, "nonverbalAssistiveDeviceText")))
        ));

        JSONObject languageConcern = backgroundInfo.optJSONObject("languageConcern");
        addLine(lines, "两岁有词汇", mapYesNo(valueOf(languageConcern, "vocabByTwoYears")));
        addLine(lines, "两岁半有句子", mapYesNo(valueOf(languageConcern, "sentenceByTwoHalfYears")));
        addLine(lines, "家长担心", joinSelected(
                mapBoolText(languageConcern, "parentConcernNormal", "基本正常"),
                mapBoolText(languageConcern, "parentConcernCannotSpeak", "不会说话"),
                mapBoolText(languageConcern, "parentConcernUnclearSpeech", "说话不清楚"),
                mapBoolText(languageConcern, "parentConcernCannotUnderstand", "他人听不懂"),
                mapBoolText(languageConcern, "parentConcernSlowResponse", "反应慢")
        ));
        addLine(lines, "家长主要诉求", valueOf(languageConcern, "parentPrimaryRequest"));
        return lines;
    }

    private static void addLine(List<String> lines, String label, String text) {
        if (text == null) {
            return;
        }
        String safeText = text.trim();
        if (safeText.isEmpty()) {
            return;
        }
        lines.add(label + "：" + safeText);
    }

    private static String valueOf(JSONObject object, String key) {
        return object == null ? "" : object.optString(key, "").trim();
    }

    private static String joinSelected(String... values) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("、");
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }

    private static String mapBoolText(JSONObject object, String key, String text) {
        if (object == null || !object.optBoolean(key)) {
            return "";
        }
        return text == null ? "" : text.trim();
    }

    private static String appendOther(String prefix, String text) {
        String safeText = text == null ? "" : text.trim();
        if (safeText.isEmpty()) {
            return prefix;
        }
        return prefix + "（" + safeText + "）";
    }

    private static String delayedLabel(JSONObject object, String key, String label) {
        String value = valueOf(object, key);
        if ("normal".equals(value)) {
            return label + "正常";
        }
        if ("delayed".equals(value)) {
            return label + "落后";
        }
        return "";
    }

    private static String abnormalLabel(JSONObject object, String key, String label) {
        String value = valueOf(object, key);
        if ("normal".equals(value)) {
            return label + "正常";
        }
        if ("abnormal".equals(value)) {
            return label + "异常";
        }
        return "";
    }

    private static String mapDeliveryMethod(String value) {
        switch (value) {
            case "natural":
                return "顺产";
            case "premature":
                return "早产";
            case "cesarean":
                return "剖腹产";
            case "major_illness":
                return "重大伤病住院";
            default:
                return "";
        }
    }

    private static String mapFeedingMethod(String value) {
        switch (value) {
            case "breast":
                return "母乳";
            case "formula":
                return "人工";
            default:
                return "";
        }
    }

    private static String mapYesNo(String value) {
        switch (value) {
            case "yes":
                return "是";
            case "no":
                return "否";
            default:
                return "";
        }
    }
}
