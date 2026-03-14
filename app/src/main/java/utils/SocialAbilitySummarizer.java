package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class SocialAbilitySummarizer {
    private static final int GROUP_SIZE = 10;
    private static final int MAX_QUESTION = 60;

    private static final GroupSpec[] GROUP_SPECS = new GroupSpec[]{
            new GroupSpec(1, 1, 10,
                    "early interaction, joint attention, imitation, and social motivation",
                    "prioritize joint attention, turn-taking, imitation, and social initiation in adult-child play",
                    "face-to-face games, joint attention, imitation routines, and motivation-building interaction"),
            new GroupSpec(2, 11, 20,
                    "basic social interaction, simple instruction-following, turn-taking, intentional communication, and early pretend participation",
                    "prioritize simple social routines, following everyday instructions, turn-taking, and expressing wants or refusals",
                    "greeting games, simple instruction games, requesting/refusing practice, and early pretend play"),
            new GroupSpec(3, 21, 30,
                    "peer interaction, sharing, turn-taking, rules, cooperation, early theory of mind, and conflict handling",
                    "prioritize sharing, peer turn-taking, simple rule-following, cooperative play, and asking for help during conflicts",
                    "toy-sharing games, rule games, peer cooperation, and guided conflict-resolution practice"),
            new GroupSpec(4, 31, 40,
                    "cooperative pretend play, conversation rules, interaction maintenance, emotion understanding, and group expression",
                    "prioritize maintaining peer interaction, conversation turn rules, cooperative pretend play, and emotional understanding in stories or group play",
                    "role play, story discussion, peer conversation practice, and group participation routines"),
            new GroupSpec(5, 41, 50,
                    "advanced conversation, empathy, goal-directed cooperation, social problem solving, and complex expression",
                    "prioritize longer conversations, empathy, collaborative planning, social problem solving, and explaining intentions clearly",
                    "collaborative tasks, longer conversations, emotion discussion, and social problem-solving tasks"),
            new GroupSpec(6, 51, 60,
                    "rule internalization, self-management, communication repair, advanced emotion understanding, negotiation, and group decision making",
                    "prioritize self-managed interaction, repairing misunderstandings, flexible negotiation, and group decision making",
                    "negotiation games, misunderstanding repair practice, group decisions, and self-regulation in social situations")
    };

    private SocialAbilitySummarizer() {
    }

    static Summary summarize(JSONArray socialArray) {
        Summary summary = new Summary();
        if (socialArray == null) {
            summary.extractionNote = "No SOCIAL array was found. The summary contains no measured social-pragmatic items.";
            return summary;
        }

        Map<Integer, Integer> measuredCountByGroup = new LinkedHashMap<>();
        Set<Integer> measuredQuestionNumbers = new LinkedHashSet<>();
        Set<Integer> measuredGroups = new LinkedHashSet<>();
        Map<String, AbilityRecord> masteredMap = new LinkedHashMap<>();
        Map<String, AbilityRecord> focusMap = new LinkedHashMap<>();
        Map<String, AbilityRecord> unstableMap = new LinkedHashMap<>();
        List<SocialItem> allMeasuredItems = new ArrayList<>();
        List<SocialItem> goalCandidates = new ArrayList<>();

        for (int i = 0; i < socialArray.length(); i++) {
            JSONObject item = socialArray.optJSONObject(i);
            if (item == null || !item.has("score") || item.isNull("score")) {
                continue;
            }
            int num = resolveQuestionNumber(item, i);
            if (num <= 0 || num > MAX_QUESTION) {
                continue;
            }
            int groupNumber = resolveGroupNumber(num);
            int score = clampScore(item.optInt("score", 0));
            String ability = safe(item.optString("ability", ""));
            String focus = safe(item.optString("focus", ""));
            String content = safe(item.optString("content", ""));
            String label = firstNonEmpty(focus, ability, content, "Question " + num);

            SocialItem measured = new SocialItem(num, groupNumber, score, label, ability, focus, content);
            allMeasuredItems.add(measured);
            goalCandidates.add(measured);

            measuredQuestionNumbers.add(num);
            measuredGroups.add(groupNumber);
            measuredCountByGroup.put(groupNumber, measuredCountByGroup.containsKey(groupNumber)
                    ? measuredCountByGroup.get(groupNumber) + 1 : 1);

            summary.measuredItemCount++;
            summary.totalScore += score;
            if (score == 2) {
                summary.score2Count++;
                putAbility(masteredMap, measured);
            } else if (score == 1) {
                summary.score1Count++;
                putAbility(unstableMap, measured);
            } else {
                summary.score0Count++;
                putAbility(focusMap, measured);
            }
        }

        summary.measuredQuestionNumbers.addAll(measuredQuestionNumbers);
        summary.measuredGroups.addAll(measuredGroups);
        summary.measuredItems.addAll(allMeasuredItems);
        summary.mastered.addAll(asOrderedLabels(masteredMap));
        summary.focus.addAll(asOrderedLabels(focusMap));
        summary.unstable.addAll(asOrderedLabels(unstableMap));
        summary.overallLevel = describeOverallLevel(summary);
        summary.overallSummaryHint = buildOverallSummaryHint(summary);
        summary.groupDetails.addAll(buildGroupDetails(measuredCountByGroup, measuredGroups));
        summary.goalSeeds.addAll(selectGoalSeeds(goalCandidates));
        summary.homeGuidanceDirections.addAll(buildHomeDirections(measuredGroups));
        summary.extractionNote = buildExtractionNote(summary);
        return summary;
    }

    private static int resolveQuestionNumber(JSONObject item, int index) {
        int num = item.optInt("num", 0);
        if (num > 0) {
            return num;
        }
        return index + 1;
    }

    private static int resolveGroupNumber(int num) {
        return ((num - 1) / GROUP_SIZE) + 1;
    }

    private static int clampScore(int score) {
        if (score < 0) {
            return 0;
        }
        if (score > 2) {
            return 2;
        }
        return score;
    }

    private static void putAbility(Map<String, AbilityRecord> map, SocialItem item) {
        String key = safe(item.label);
        if (key.isEmpty()) {
            return;
        }
        AbilityRecord existing = map.get(key);
        if (existing == null || item.num < existing.num) {
            map.put(key, new AbilityRecord(item.num, item.groupNumber, key));
        }
    }

    private static List<String> asOrderedLabels(Map<String, AbilityRecord> map) {
        List<AbilityRecord> records = new ArrayList<>(map.values());
        records.sort((left, right) -> {
            if (left.num != right.num) {
                return left.num - right.num;
            }
            return left.label.compareTo(right.label);
        });
        List<String> labels = new ArrayList<>();
        for (AbilityRecord record : records) {
            labels.add(record.label);
        }
        return labels;
    }

    private static List<GroupDetail> buildGroupDetails(Map<Integer, Integer> measuredCountByGroup,
                                                       Set<Integer> measuredGroups) {
        List<GroupDetail> details = new ArrayList<>();
        for (GroupSpec spec : GROUP_SPECS) {
            if (!measuredGroups.contains(spec.groupNumber)) {
                continue;
            }
            details.add(new GroupDetail(spec.groupNumber,
                    spec.startQuestion + "-" + spec.endQuestion,
                    measuredCountByGroup.containsKey(spec.groupNumber)
                            ? measuredCountByGroup.get(spec.groupNumber) : 0,
                    spec.trainingLevel,
                    spec.goalDirection,
                    spec.homeDirection));
        }
        return details;
    }

    private static List<GoalSeed> selectGoalSeeds(List<SocialItem> measuredItems) {
        measuredItems.sort((left, right) -> {
            if (left.score != right.score) {
                return left.score - right.score;
            }
            if (left.groupNumber != right.groupNumber) {
                return left.groupNumber - right.groupNumber;
            }
            return left.num - right.num;
        });
        List<GoalSeed> seeds = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (SocialItem item : measuredItems) {
            if (item.score == 2) {
                continue;
            }
            String key = safe(item.label);
            if (key.isEmpty() || !seen.add(key)) {
                continue;
            }
            seeds.add(new GoalSeed(item.groupNumber, item.num, item.label,
                    item.score == 0 ? "not_yet_established" : "emerging_but_unstable"));
            if (seeds.size() >= 4) {
                break;
            }
        }
        return seeds;
    }

    private static List<String> buildHomeDirections(Set<Integer> measuredGroups) {
        List<String> directions = new ArrayList<>();
        for (GroupSpec spec : GROUP_SPECS) {
            if (measuredGroups.contains(spec.groupNumber)) {
                directions.add(spec.homeDirection);
            }
        }
        return directions;
    }

    private static String describeOverallLevel(Summary summary) {
        if (summary.measuredItemCount <= 0) {
            return "no_measured_social_items";
        }
        double ratio = summary.totalScore / (summary.measuredItemCount * 2.0d);
        if (ratio >= 0.75d && summary.score0Count <= Math.max(1, summary.measuredItemCount / 5)) {
            return "mostly_established_within_measured_groups";
        }
        if (ratio >= 0.45d) {
            return "mixed_profile_with_partly_established_social_skills";
        }
        return "multiple_social_skills_still_need_support_within_measured_groups";
    }

    private static String buildOverallSummaryHint(Summary summary) {
        if (summary.measuredItemCount <= 0) {
            return "No scored SOCIAL items were available, so the report should stay conservative and note that no measured group can be interpreted.";
        }
        double ratio = summary.totalScore / (summary.measuredItemCount * 2.0d);
        if (ratio >= 0.75d) {
            return "Within the social-pragmatic groups that were actually measured this time, performance is generally strong and many related abilities appear established, though the report should still mention any remaining unstable or not-yet-emerged skills.";
        }
        if (ratio >= 0.45d) {
            return "Within the measured social-pragmatic groups, some abilities are present while others remain unstable or not yet established. The report should reflect a mixed profile and recommend continued support.";
        }
        return "Within the measured social-pragmatic groups, several core abilities are still emerging or absent. The report should describe current needs cautiously and emphasize continued support rather than absolute conclusions.";
    }

    private static String buildExtractionNote(Summary summary) {
        if (summary.measuredItemCount <= 0) {
            return "The summary uses only SOCIAL items that have an actual score. Unmeasured questions and unselected groups are excluded.";
        }
        return String.format(Locale.US,
                "The summary uses only SOCIAL items with a recorded score and infers measured groups from question numbers (%d items across %d groups). Unmeasured questions are excluded from mastered/focus/unstable lists, SMART goal seeds, and home-guidance directions.",
                summary.measuredItemCount, summary.measuredGroups.size());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String text = safe(value);
            if (!text.isEmpty()) {
                return text;
            }
        }
        return "";
    }

    static final class Summary {
        int measuredItemCount;
        int totalScore;
        int score2Count;
        int score1Count;
        int score0Count;
        String overallLevel = "no_measured_social_items";
        String overallSummaryHint = "";
        String extractionNote = "";
        final List<Integer> measuredQuestionNumbers = new ArrayList<>();
        final List<Integer> measuredGroups = new ArrayList<>();
        final List<String> mastered = new ArrayList<>();
        final List<String> focus = new ArrayList<>();
        final List<String> unstable = new ArrayList<>();
        final List<SocialItem> measuredItems = new ArrayList<>();
        final List<GroupDetail> groupDetails = new ArrayList<>();
        final List<GoalSeed> goalSeeds = new ArrayList<>();
        final List<String> homeGuidanceDirections = new ArrayList<>();

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("measuredItemCount", measuredItemCount);
            out.put("totalScore", totalScore);
            out.put("overallLevel", overallLevel);
            out.put("overallSummaryHint", overallSummaryHint);

            JSONObject scoreBreakdown = new JSONObject();
            scoreBreakdown.put("score2", score2Count);
            scoreBreakdown.put("score1", score1Count);
            scoreBreakdown.put("score0", score0Count);
            out.put("scoreBreakdown", scoreBreakdown);

            JSONArray questionNumbers = new JSONArray();
            for (Integer num : measuredQuestionNumbers) {
                questionNumbers.put(num);
            }
            out.put("measuredQuestionNumbers", questionNumbers);

            JSONArray groups = new JSONArray();
            for (Integer groupNumber : measuredGroups) {
                groups.put(groupNumber);
            }
            out.put("measuredGroups", groups);

            JSONArray groupArray = new JSONArray();
            for (GroupDetail detail : groupDetails) {
                groupArray.put(detail.toJson());
            }
            out.put("groupDetails", groupArray);

            out.put("masteredAbilities", toStringArray(mastered));
            out.put("focusAbilities", toStringArray(focus));
            out.put("unstableAbilities", toStringArray(unstable));

            JSONArray measuredItemsJson = new JSONArray();
            for (SocialItem item : measuredItems) {
                measuredItemsJson.put(item.toJson());
            }
            out.put("measuredItems", measuredItemsJson);

            JSONArray goalSeedsJson = new JSONArray();
            for (GoalSeed seed : goalSeeds) {
                goalSeedsJson.put(seed.toJson());
            }
            out.put("smartGoalSeeds", goalSeedsJson);
            out.put("homeGuidanceDirections", toStringArray(homeGuidanceDirections));
            out.put("extractionNote", extractionNote);
            return out;
        }

        private JSONArray toStringArray(List<String> values) {
            JSONArray array = new JSONArray();
            for (String value : values) {
                String text = safe(value);
                if (!text.isEmpty()) {
                    array.put(text);
                }
            }
            return array;
        }
    }

    private static final class SocialItem {
        final int num;
        final int groupNumber;
        final int score;
        final String label;
        final String ability;
        final String focus;
        final String content;

        SocialItem(int num, int groupNumber, int score, String label, String ability, String focus, String content) {
            this.num = num;
            this.groupNumber = groupNumber;
            this.score = score;
            this.label = label;
            this.ability = ability;
            this.focus = focus;
            this.content = content;
        }

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("num", num);
            out.put("groupNumber", groupNumber);
            out.put("score", score);
            out.put("label", label);
            out.put("ability", ability);
            out.put("focus", focus);
            out.put("content", content);
            return out;
        }
    }

    private static final class AbilityRecord {
        final int num;
        final int groupNumber;
        final String label;

        AbilityRecord(int num, int groupNumber, String label) {
            this.num = num;
            this.groupNumber = groupNumber;
            this.label = label;
        }
    }

    private static final class GroupDetail {
        final int groupNumber;
        final String questionRange;
        final int measuredQuestionCount;
        final String trainingLevel;
        final String goalDirection;
        final String homeDirection;

        GroupDetail(int groupNumber,
                    String questionRange,
                    int measuredQuestionCount,
                    String trainingLevel,
                    String goalDirection,
                    String homeDirection) {
            this.groupNumber = groupNumber;
            this.questionRange = questionRange;
            this.measuredQuestionCount = measuredQuestionCount;
            this.trainingLevel = trainingLevel;
            this.goalDirection = goalDirection;
            this.homeDirection = homeDirection;
        }

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("groupNumber", groupNumber);
            out.put("questionRange", questionRange);
            out.put("measuredQuestionCount", measuredQuestionCount);
            out.put("trainingLevel", trainingLevel);
            out.put("goalDirection", goalDirection);
            out.put("homeGuidanceDirection", homeDirection);
            return out;
        }
    }

    private static final class GoalSeed {
        final int groupNumber;
        final int questionNumber;
        final String ability;
        final String priority;

        GoalSeed(int groupNumber, int questionNumber, String ability, String priority) {
            this.groupNumber = groupNumber;
            this.questionNumber = questionNumber;
            this.ability = ability;
            this.priority = priority;
        }

        JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put("groupNumber", groupNumber);
            out.put("questionNumber", questionNumber);
            out.put("ability", ability);
            out.put("priority", priority);
            return out;
        }
    }

    private static final class GroupSpec {
        final int groupNumber;
        final int startQuestion;
        final int endQuestion;
        final String trainingLevel;
        final String goalDirection;
        final String homeDirection;

        GroupSpec(int groupNumber,
                  int startQuestion,
                  int endQuestion,
                  String trainingLevel,
                  String goalDirection,
                  String homeDirection) {
            this.groupNumber = groupNumber;
            this.startQuestion = startQuestion;
            this.endQuestion = endQuestion;
            this.trainingLevel = trainingLevel;
            this.goalDirection = goalDirection;
            this.homeDirection = homeDirection;
        }
    }
}
