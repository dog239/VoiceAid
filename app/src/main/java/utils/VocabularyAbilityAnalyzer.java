package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class VocabularyAbilityAnalyzer {
    private static final String DOMAIN_RECEPTIVE = "词汇理解";
    private static final String DOMAIN_EXPRESSIVE = "词汇表达";

    private static final CategorySpec[] CATEGORY_SPECS = new CategorySpec[]{
            new CategorySpec("noun", "名词", 1, 2,
                    "能较稳定理解常见物品和身体部位名称",
                    "能较稳定表达常见物品和身体部位名称",
                    "常见名词理解仍需加强",
                    "常见名词表达仍需加强",
                    "对常见名词的理解已开始建立，但稳定性仍需加强",
                    "对常见名词的表达已开始出现，但主动命名仍不够稳定"),
            new CategorySpec("verb", "动词", 3, 4,
                    "能理解基础动作词",
                    "能表达基础动作词",
                    "常见动词理解仍需加强",
                    "常见动词表达仍需加强",
                    "对常见动词的理解已开始建立，但稳定性仍需加强",
                    "对常见动词的表达已出现一定基础，但使用仍不够稳定"),
            new CategorySpec("adjective", "形容词", 5, 6,
                    "能理解基础描述性词汇，如颜色、大小或长短等",
                    "能表达基础描述性词汇，如颜色、大小或长短等",
                    "描述性词汇理解仍需加强",
                    "描述性词汇表达仍需加强",
                    "对描述性词汇的理解已开始建立，但仍不够稳定",
                    "对描述性词汇的表达出现了一定基础，但使用还不够稳定"),
            new CategorySpec("category_noun", "分类名词（上位词）", 7, 7,
                    "对常见分类概念词有较稳定理解",
                    "能表达常见分类概念词",
                    "分类名词（上位词）理解掌握不足",
                    "分类名词（上位词）表达掌握不足",
                    "分类概念词的理解已开始出现，但泛化仍需支持",
                    "分类概念词的表达已开始出现，但泛化使用仍需支持")
    };

    private VocabularyAbilityAnalyzer() {
    }

    static Summary summarize(JSONObject evaluations) {
        JSONArray expressiveArray = cloneArray(evaluations == null ? null : evaluations.optJSONArray("E"));
        ReceptiveSource receptive = resolveReceptiveSource(evaluations);
        ModeProfile expressive = buildModeProfile(DOMAIN_EXPRESSIVE, expressiveArray);
        ModeProfile receptiveProfile = buildModeProfile(DOMAIN_RECEPTIVE, receptive.items);

        Summary summary = new Summary();
        summary.expressive = expressive;
        summary.receptive = receptiveProfile;
        summary.receptiveSource = receptive.source;
        summary.overallLevel = buildOverallLevel(receptiveProfile, expressive);
        summary.relativeProfile = buildRelativeProfile(receptiveProfile, expressive);
        summary.overallSummaryHint = buildOverallSummaryHint(receptiveProfile, expressive, summary.relativeProfile);
        buildAbilityLists(summary, receptiveProfile, expressive);
        summary.smartGoalSeeds = buildGoalSeeds(receptiveProfile, expressive);
        summary.homeGuidanceDirections = buildHomeDirections(receptiveProfile, expressive);
        summary.extractionNote = buildExtractionNote(receptive, receptiveProfile, expressive);
        return summary;
    }

    static JSONArray getPrimaryReceptiveArray(JSONObject evaluations) {
        return resolveReceptiveSource(evaluations).items;
    }

    private static ReceptiveSource resolveReceptiveSource(JSONObject evaluations) {
        JSONArray ev = cloneArray(evaluations == null ? null : evaluations.optJSONArray("EV"));
        if (countCompleted(ev) > 0 || ev.length() > 0) {
            return new ReceptiveSource("EV", ev);
        }
        JSONArray re = cloneArray(evaluations == null ? null : evaluations.optJSONArray("RE"));
        if (countCompleted(re) > 0 || re.length() > 0) {
            return new ReceptiveSource("RE", re);
        }
        return new ReceptiveSource("EV", new JSONArray());
    }

    private static ModeProfile buildModeProfile(String domain, JSONArray array) {
        ModeProfile profile = new ModeProfile(domain);
        for (int i = 0; i < CATEGORY_SPECS.length; i++) {
            CategorySpec spec = CATEGORY_SPECS[i];
            CategoryScore score = new CategoryScore(spec);
            for (int num = spec.startNum; num <= spec.endNum; num++) {
                JSONObject item = findItemByNum(array, num);
                if (item == null || !item.has("result") || item.isNull("result")) {
                    continue;
                }
                boolean correct = item.optBoolean("result");
                String target = safeText(item.optString("target", ""));
                score.total++;
                if (correct) {
                    score.correct++;
                    addUnique(score.correctTargets, target);
                } else {
                    addUnique(score.wrongTargets, target);
                }
            }
            score.status = describeStatus(score);
            profile.byCategory.put(spec.key, score);
            profile.total += score.total;
            profile.correct += score.correct;
        }
        return profile;
    }

    private static JSONObject findItemByNum(JSONArray array, int num) {
        if (array == null) {
            return null;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            int itemNum = item.optInt("num", i + 1);
            if (itemNum == num) {
                return item;
            }
        }
        return null;
    }

    private static String describeStatus(CategoryScore score) {
        if (score == null || score.total <= 0) {
            return "no_data";
        }
        if (score.correct == score.total) {
            return "mastered";
        }
        if (score.correct == 0) {
            return "focus";
        }
        return "unstable";
    }

    private static void buildAbilityLists(Summary summary, ModeProfile receptive, ModeProfile expressive) {
        for (int i = 0; i < CATEGORY_SPECS.length; i++) {
            CategorySpec spec = CATEGORY_SPECS[i];
            CategoryScore receptiveScore = receptive.byCategory.get(spec.key);
            CategoryScore expressiveScore = expressive.byCategory.get(spec.key);

            if (isMastered(receptiveScore) && isMastered(expressiveScore)) {
                addUnique(summary.mastered, "能较稳定理解并表达" + describeCategoryAbility(spec));
                continue;
            }
            if (isFocus(receptiveScore) && isFocus(expressiveScore)) {
                addUnique(summary.focus, spec.label + "理解与表达均需重点关注");
                continue;
            }
            if (isUnstable(receptiveScore) && isUnstable(expressiveScore)) {
                addUnique(summary.unstable, spec.label + "在理解与表达两侧均已出现一定基础，但稳定性仍需继续支持");
            }

            appendSingleSideAbilities(summary.mastered, summary.focus, summary.unstable, spec, receptiveScore, true);
            appendSingleSideAbilities(summary.mastered, summary.focus, summary.unstable, spec, expressiveScore, false);

            if (isMastered(receptiveScore) && isFocus(expressiveScore)) {
                addUnique(summary.focus, spec.label + "表达明显弱于理解，建议作为近期重点训练方向");
            } else if (isMastered(expressiveScore) && isFocus(receptiveScore)) {
                addUnique(summary.focus, spec.label + "理解明显弱于表达，建议优先加强词义理解与辨认");
            } else if (isMastered(receptiveScore) && isUnstable(expressiveScore)) {
                addUnique(summary.unstable, spec.label + "理解较稳定，但表达仍不够稳定");
            } else if (isUnstable(receptiveScore) && isMastered(expressiveScore)) {
                addUnique(summary.unstable, spec.label + "表达相对较好，但理解稳定性仍需加强");
            }
        }
    }

    private static void appendSingleSideAbilities(List<String> mastered,
                                                  List<String> focus,
                                                  List<String> unstable,
                                                  CategorySpec spec,
                                                  CategoryScore score,
                                                  boolean receptive) {
        if (score == null || "no_data".equals(score.status)) {
            return;
        }
        if ("mastered".equals(score.status)) {
            addUnique(mastered, receptive ? spec.receptiveMasteredText : spec.expressiveMasteredText);
        } else if ("focus".equals(score.status)) {
            addUnique(focus, receptive ? spec.receptiveFocusText : spec.expressiveFocusText);
        } else if ("unstable".equals(score.status)) {
            addUnique(unstable, receptive ? spec.receptiveUnstableText : spec.expressiveUnstableText);
        }
    }

    private static List<GoalSeed> buildGoalSeeds(ModeProfile receptive, ModeProfile expressive) {
        List<GoalSeed> seeds = new ArrayList<>();
        for (int i = 0; i < CATEGORY_SPECS.length; i++) {
            CategorySpec spec = CATEGORY_SPECS[i];
            CategoryScore receptiveScore = receptive.byCategory.get(spec.key);
            CategoryScore expressiveScore = expressive.byCategory.get(spec.key);
            if (isFocus(receptiveScore) || isUnstable(receptiveScore)) {
                seeds.add(new GoalSeed(DOMAIN_RECEPTIVE, spec.label, buildReceptiveGoalDirection(spec, receptiveScore),
                        listExamples(receptiveScore)));
            }
            if (isFocus(expressiveScore) || isUnstable(expressiveScore)) {
                seeds.add(new GoalSeed(DOMAIN_EXPRESSIVE, spec.label, buildExpressiveGoalDirection(spec, expressiveScore),
                        listExamples(expressiveScore)));
            }
        }
        if (seeds.size() > 4) {
            return new ArrayList<>(seeds.subList(0, 4));
        }
        return seeds;
    }

    private static List<String> buildHomeDirections(ModeProfile receptive, ModeProfile expressive) {
        List<String> directions = new ArrayList<>();
        for (int i = 0; i < CATEGORY_SPECS.length; i++) {
            CategorySpec spec = CATEGORY_SPECS[i];
            CategoryScore receptiveScore = receptive.byCategory.get(spec.key);
            CategoryScore expressiveScore = expressive.byCategory.get(spec.key);
            if (isFocus(receptiveScore) || isUnstable(receptiveScore)) {
                addUnique(directions, buildReceptiveHomeDirection(spec));
            }
            if (isFocus(expressiveScore) || isUnstable(expressiveScore)) {
                addUnique(directions, buildExpressiveHomeDirection(spec));
            }
        }
        if (directions.isEmpty()) {
            directions.add("在绘本共读、生活命名和分类游戏中持续提供清晰词汇输入，巩固已建立的名词、动词、形容词和分类概念词。");
        }
        if (directions.size() > 5) {
            return new ArrayList<>(directions.subList(0, 5));
        }
        return directions;
    }

    private static String buildReceptiveGoalDirection(CategorySpec spec, CategoryScore score) {
        String examples = joinExamples(score);
        if ("category_noun".equals(spec.key)) {
            return "在分类游戏或图片选择任务中理解常见上位词概念" + examples;
        }
        return "在图片或实物选择任务中正确理解" + spec.label + "相关词汇" + examples;
    }

    private static String buildExpressiveGoalDirection(CategorySpec spec, CategoryScore score) {
        String examples = joinExamples(score);
        if ("category_noun".equals(spec.key)) {
            return "在分类活动中主动表达常见上位词概念" + examples;
        }
        return "在结构化命名或描述活动中主动表达" + spec.label + "相关词汇" + examples;
    }

    private static String buildReceptiveHomeDirection(CategorySpec spec) {
        if ("noun".equals(spec.key)) {
            return "在吃饭、穿衣、洗澡和收玩具时反复做“给我/找到/指一指”名词指认游戏，帮助孩子理解常见物品和身体部位名称。";
        }
        if ("verb".equals(spec.key)) {
            return "在日常动作和绘本中一边做动作一边说词，如“跑、吃、睡觉”，多做“谁在做什么”的理解任务。";
        }
        if ("adjective".equals(spec.key)) {
            return "在生活中通过大小、颜色、长短对比做“找一找/拿一拿”任务，帮助孩子理解描述性词汇。";
        }
        return "通过“它们是什么类”的分类游戏，引导孩子在水果、动物、交通工具等熟悉主题中理解上位词概念。";
    }

    private static String buildExpressiveHomeDirection(CategorySpec spec) {
        if ("noun".equals(spec.key)) {
            return "利用实物和图片多做“这是什么”命名活动，等待孩子主动说出名称，再用完整短句扩展示范。";
        }
        if ("verb".equals(spec.key)) {
            return "在动作模仿和生活情境中多问“他在做什么”，鼓励孩子说出动作词，并及时示范和重复。";
        }
        if ("adjective".equals(spec.key)) {
            return "在穿衣、收纳和绘本描述中多问“什么颜色/哪个大/哪个长”，帮助孩子练习颜色、大小和长短等形容词表达。";
        }
        return "在分类整理和配对游戏中多问“这是什么类”，鼓励孩子说出动物、水果等分类名词，并在答后做重复扩展。";
    }

    private static String buildOverallLevel(ModeProfile receptive, ModeProfile expressive) {
        double receptiveRate = rate(receptive);
        double expressiveRate = rate(expressive);
        if (receptive.total == 0 && expressive.total == 0) {
            return "no_data";
        }
        if (receptiveRate >= 0.8d && expressiveRate >= 0.8d) {
            return "strong";
        }
        if (receptiveRate >= 0.75d && expressiveRate < 0.6d) {
            return "receptive_stronger";
        }
        if (expressiveRate >= 0.75d && receptiveRate < 0.6d) {
            return "expressive_stronger";
        }
        if (receptiveRate < 0.6d && expressiveRate < 0.6d) {
            return "both_need_support";
        }
        return "mixed";
    }

    private static String buildRelativeProfile(ModeProfile receptive, ModeProfile expressive) {
        double receptiveRate = rate(receptive);
        double expressiveRate = rate(expressive);
        if (receptive.total == 0 && expressive.total == 0) {
            return "无可用词汇测评数据";
        }
        if (Math.abs(receptiveRate - expressiveRate) < 0.15d) {
            if (receptiveRate >= 0.75d && expressiveRate >= 0.75d) {
                return "理解与表达均较稳定";
            }
            return "理解与表达表现接近";
        }
        if (receptiveRate > expressiveRate) {
            return "理解相对强于表达";
        }
        return "表达相对强于理解";
    }

    private static String buildOverallSummaryHint(ModeProfile receptive, ModeProfile expressive, String relativeProfile) {
        if (receptive.total == 0 && expressive.total == 0) {
            return "暂无可用的词汇理解与表达测评数据。";
        }
        double receptiveRate = rate(receptive);
        double expressiveRate = rate(expressive);
        if (receptiveRate >= 0.8d && expressiveRate >= 0.8d) {
            return "孩子在本次词汇能力测评中整体表现较好，对常见名词、动词、形容词及分类概念词的理解和表达能力较为稳定。";
        }
        if (receptiveRate - expressiveRate >= 0.2d) {
            return "孩子在本次词汇能力测评中，对词汇的理解能力相对较好，但主动表达仍需进一步提升，尤其需要继续巩固较弱词类的命名与描述。";
        }
        if (expressiveRate - receptiveRate >= 0.2d) {
            return "孩子在本次词汇能力测评中，词汇表达相对强于理解，提示其在词义理解、辨认与分类概念掌握方面仍需进一步支持。";
        }
        return "孩子在本次词汇能力测评中，词汇理解与表达均已有一定基础，但不同词类间表现不均衡，部分能力尚未稳定建立，仍需继续支持。";
    }

    private static String buildExtractionNote(ReceptiveSource receptiveSource, ModeProfile receptive, ModeProfile expressive) {
        if (receptive.total == 0 && expressive.total == 0) {
            return "暂无可用的 E/EV/RE 词汇题目结果。";
        }
        return "词汇模块摘要按 E 作为表达结果、" + receptiveSource.source + " 作为理解结果，基于题号映射名词/动词/形容词/分类名词四类能力，并按全对/部分对/全错归类为已掌握、不稳定和重点关注。";
    }

    private static String describeCategoryAbility(CategorySpec spec) {
        if (spec == null) {
            return "目标词汇";
        }
        if ("noun".equals(spec.key)) {
            return "常见物品和身体部位名称";
        }
        if ("verb".equals(spec.key)) {
            return "基础动作词";
        }
        if ("adjective".equals(spec.key)) {
            return "基础描述性词汇";
        }
        return "常见分类概念词";
    }

    private static boolean isMastered(CategoryScore score) {
        return score != null && "mastered".equals(score.status);
    }

    private static boolean isFocus(CategoryScore score) {
        return score != null && "focus".equals(score.status);
    }

    private static boolean isUnstable(CategoryScore score) {
        return score != null && "unstable".equals(score.status);
    }

    private static double rate(ModeProfile profile) {
        if (profile == null || profile.total <= 0) {
            return 0d;
        }
        return profile.correct * 1.0d / profile.total;
    }

    private static int countCompleted(JSONArray array) {
        int count = 0;
        if (array == null) {
            return 0;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null && item.has("result") && !item.isNull("result")) {
                count++;
            }
        }
        return count;
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

    private static String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\n", " ").trim();
    }

    private static void addUnique(List<String> list, String value) {
        String text = safeText(value);
        if (!text.isEmpty() && !list.contains(text)) {
            list.add(text);
        }
    }

    private static JSONArray toJsonArray(List<String> values) {
        JSONArray out = new JSONArray();
        if (values == null) {
            return out;
        }
        for (int i = 0; i < values.size(); i++) {
            String value = safeText(values.get(i));
            if (!value.isEmpty()) {
                out.put(value);
            }
        }
        return out;
    }

    private static String joinExamples(CategoryScore score) {
        List<String> examples = listExamples(score);
        if (examples.isEmpty()) {
            return "";
        }
        return "（如" + joinWithSeparator(examples, "、") + "等）";
    }

    private static List<String> listExamples(CategoryScore score) {
        if (score == null) {
            return new ArrayList<>();
        }
        List<String> examples = new ArrayList<>();
        List<String> source = !score.wrongTargets.isEmpty() ? score.wrongTargets : score.correctTargets;
        for (int i = 0; i < source.size() && i < 3; i++) {
            addUnique(examples, source.get(i));
        }
        return examples;
    }

    private static String joinWithSeparator(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private static final class ReceptiveSource {
        final String source;
        final JSONArray items;

        ReceptiveSource(String source, JSONArray items) {
            this.source = source;
            this.items = items == null ? new JSONArray() : items;
        }
    }

    private static final class CategorySpec {
        final String key;
        final String label;
        final int startNum;
        final int endNum;
        final String receptiveMasteredText;
        final String expressiveMasteredText;
        final String receptiveFocusText;
        final String expressiveFocusText;
        final String receptiveUnstableText;
        final String expressiveUnstableText;

        CategorySpec(String key,
                     String label,
                     int startNum,
                     int endNum,
                     String receptiveMasteredText,
                     String expressiveMasteredText,
                     String receptiveFocusText,
                     String expressiveFocusText,
                     String receptiveUnstableText,
                     String expressiveUnstableText) {
            this.key = key;
            this.label = label;
            this.startNum = startNum;
            this.endNum = endNum;
            this.receptiveMasteredText = receptiveMasteredText;
            this.expressiveMasteredText = expressiveMasteredText;
            this.receptiveFocusText = receptiveFocusText;
            this.expressiveFocusText = expressiveFocusText;
            this.receptiveUnstableText = receptiveUnstableText;
            this.expressiveUnstableText = expressiveUnstableText;
        }
    }

    private static final class CategoryScore {
        final CategorySpec spec;
        int total;
        int correct;
        String status = "no_data";
        final List<String> correctTargets = new ArrayList<>();
        final List<String> wrongTargets = new ArrayList<>();

        CategoryScore(CategorySpec spec) {
            this.spec = spec;
        }

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("label", spec.label);
            out.put("total", total);
            out.put("correct", correct);
            out.put("accuracy", total > 0 ? String.format(Locale.US, "%.2f%%", correct * 100.0 / total) : "");
            out.put("status", status);
            out.put("correctTargets", toJsonArray(correctTargets));
            out.put("wrongTargets", toJsonArray(wrongTargets));
            return out;
        }
    }

    static final class ModeProfile {
        final String domain;
        int total;
        int correct;
        final Map<String, CategoryScore> byCategory = new LinkedHashMap<>();

        ModeProfile(String domain) {
            this.domain = domain;
        }

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("domain", domain);
            out.put("total", total);
            out.put("correct", correct);
            out.put("accuracy", total > 0 ? String.format(Locale.US, "%.2f%%", correct * 100.0 / total) : "");
            JSONObject categories = new JSONObject();
            for (CategorySpec spec : CATEGORY_SPECS) {
                CategoryScore score = byCategory.get(spec.key);
                categories.put(spec.key, score == null ? new JSONObject() : score.toJson());
            }
            out.put("categories", categories);
            return out;
        }
    }

    private static final class GoalSeed {
        final String domain;
        final String category;
        final String direction;
        final List<String> examples;

        GoalSeed(String domain, String category, String direction, List<String> examples) {
            this.domain = domain;
            this.category = category;
            this.direction = direction;
            this.examples = examples == null ? new ArrayList<String>() : examples;
        }

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("domain", domain);
            out.put("category", category);
            out.put("direction", direction);
            out.put("examples", toJsonArray(examples));
            return out;
        }
    }

    static final class Summary {
        ModeProfile expressive;
        ModeProfile receptive;
        String receptiveSource;
        String overallLevel;
        String relativeProfile;
        String overallSummaryHint;
        String extractionNote;
        final List<String> mastered = new ArrayList<>();
        final List<String> focus = new ArrayList<>();
        final List<String> unstable = new ArrayList<>();
        List<GoalSeed> smartGoalSeeds = new ArrayList<>();
        List<String> homeGuidanceDirections = new ArrayList<>();

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("overallLevel", safeText(overallLevel));
            out.put("relativeProfile", safeText(relativeProfile));
            out.put("overallSummaryHint", safeText(overallSummaryHint));
            out.put("receptiveSource", safeText(receptiveSource));
            out.put("receptive", receptive == null ? new JSONObject() : receptive.toJson());
            out.put("expressive", expressive == null ? new JSONObject() : expressive.toJson());
            out.put("mastered", toJsonArray(mastered));
            out.put("focus", toJsonArray(focus));
            out.put("unstable", toJsonArray(unstable));
            JSONArray goalSeedArray = new JSONArray();
            for (int i = 0; i < smartGoalSeeds.size(); i++) {
                goalSeedArray.put(smartGoalSeeds.get(i).toJson());
            }
            out.put("smartGoalSeeds", goalSeedArray);
            out.put("homeGuidanceDirections", toJsonArray(homeGuidanceDirections));
            out.put("extractionNote", safeText(extractionNote));
            out.put("crossDomainPriorityCategories", buildCrossDomainCategories(expressive, receptive, "focus"));
            out.put("crossDomainUnstableCategories", buildCrossDomainCategories(expressive, receptive, "unstable"));
            return out;
        }

        private JSONArray buildCrossDomainCategories(ModeProfile expressive,
                                                     ModeProfile receptive,
                                                     String targetStatus) {
            JSONArray out = new JSONArray();
            Set<String> labels = new LinkedHashSet<>();
            for (CategorySpec spec : CATEGORY_SPECS) {
                CategoryScore expressiveScore = expressive == null ? null : expressive.byCategory.get(spec.key);
                CategoryScore receptiveScore = receptive == null ? null : receptive.byCategory.get(spec.key);
                if (expressiveScore == null || receptiveScore == null) {
                    continue;
                }
                if (targetStatus.equals(expressiveScore.status) && targetStatus.equals(receptiveScore.status)) {
                    labels.add(spec.label);
                }
            }
            for (String label : labels) {
                out.put(label);
            }
            return out;
        }
    }
}
