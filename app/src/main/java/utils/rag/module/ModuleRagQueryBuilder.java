package utils.rag.module;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class ModuleRagQueryBuilder {
    private ModuleRagQueryBuilder() {
    }

    /**
     * Retrieval input policy:
     * 1. Query only uses stable structured summary fields from moduleEvaluations.summary.
     * 2. Prompt-only fields such as chiefComplaint and existingModuleReport stay out of retrieval.
     * 3. Privacy-sensitive fields like name, address, phone and family text never enter query.
     * 4. Syntax keeps a global layer plus comprehension/expression sub-modules for retrieval.
     */
    public static RagQuery build(String moduleType, JSONObject moduleInput) {
        String normalizedModuleType = normalize(moduleType);
        if ("articulation".equals(normalizedModuleType)) {
            return buildArticulationQuery(normalizedModuleType, moduleInput);
        }
        if ("syntax".equals(normalizedModuleType)) {
            return buildSyntaxQuery(normalizedModuleType, moduleInput);
        }
        return new RagQuery(normalizedModuleType, empty(), empty(), empty(), empty(), "");
    }

    private static RagQuery buildArticulationQuery(String normalizedModuleType, JSONObject moduleInput) {
        JSONObject summary = optObject(optObject(moduleInput, "moduleEvaluations"), "summary");
        JSONObject phonemeSummary = optObject(summary, "phonemeSummary");
        JSONObject articulationProfile = optObject(summary, "articulationProfile");

        LinkedHashSet<String> errorTypes = new LinkedHashSet<>();
        addAll(errorTypes, normalizeErrorTypes(optStringArray(articulationProfile, "phonologyProcesses")));

        LinkedHashSet<String> targetSounds = new LinkedHashSet<>();
        addAll(targetSounds, optStringArray(phonemeSummary, "targets"));
        addAll(targetSounds, optStringArray(articulationProfile, "focusPhonemes"));
        addAll(targetSounds, optStringArray(articulationProfile, "unstablePhonemes"));

        LinkedHashSet<String> targetPositions = new LinkedHashSet<>();
        addAll(targetPositions, normalizePositions(optStringArray(articulationProfile, "wordPositionFocus")));
        addAll(targetPositions, normalizePositions(optStringArray(articulationProfile, "wordPositionUnstable")));

        LinkedHashSet<String> goalTags = new LinkedHashSet<>();
        if (!targetSounds.isEmpty()) {
            goalTags.add("stabilization");
        }
        if (!targetPositions.isEmpty()) {
            goalTags.add("carryover");
        }
        if (containsMultisyllable(summary, articulationProfile)) {
            goalTags.add("multisyllable");
        }
        goalTags.add("home_practice");

        return new RagQuery(
                normalizedModuleType,
                new ArrayList<>(errorTypes),
                new ArrayList<>(targetSounds),
                new ArrayList<>(targetPositions),
                new ArrayList<>(goalTags),
                buildSeverity(summary)
        );
    }

    private static RagQuery buildSyntaxQuery(String normalizedModuleType, JSONObject moduleInput) {
        JSONObject summary = optObject(optObject(moduleInput, "moduleEvaluations"), "summary");
        JSONObject syntaxAssessment = optObject(summary, "syntaxAssessment");
        JSONObject rgComprehension = optObject(summary, "rgComprehension");
        JSONObject seExpression = optObject(summary, "seExpression");

        RagQuery.SubModuleQuery comprehension = new RagQuery.SubModuleQuery(
                "comprehension",
                buildSyntaxProblemTags(rgComprehension, true),
                buildSyntaxGoalTags(rgComprehension, true),
                normalize(rgComprehension == null ? "" : rgComprehension.optString("overallLevel", ""))
        );
        RagQuery.SubModuleQuery expression = new RagQuery.SubModuleQuery(
                "expression",
                buildSyntaxProblemTags(seExpression, false),
                buildSyntaxGoalTags(seExpression, false),
                normalize(seExpression == null ? "" : seExpression.optString("overallLevel", ""))
        );

        LinkedHashSet<String> globalGoalTags = new LinkedHashSet<>();
        addAll(globalGoalTags, normalizeSyntaxFamilies(optStringArray(syntaxAssessment, "sharedPriorityStructures")));
        addAll(globalGoalTags, normalizeSyntaxFamilies(optStringArray(syntaxAssessment, "sharedUnstableStructures")));
        if (!comprehension.problemTags.isEmpty() && !expression.problemTags.isEmpty()) {
            globalGoalTags.add("syntax_integration");
        }
        if (containsKeyword(optStringArray(syntaxAssessment, "unstable"), "不稳定")
                || containsKeyword(optStringArray(syntaxAssessment, "unstable"), "unstable")) {
            globalGoalTags.add("stability");
        }

        List<RagQuery.SubModuleQuery> subModules = new ArrayList<>();
        subModules.add(comprehension);
        subModules.add(expression);
        return new RagQuery(
                normalizedModuleType,
                empty(),
                empty(),
                empty(),
                new ArrayList<>(globalGoalTags),
                normalize(syntaxAssessment == null ? "" : syntaxAssessment.optString("overallLevel", "")),
                new RagQuery.GlobalQuery(new ArrayList<>(globalGoalTags),
                        normalize(syntaxAssessment == null ? "" : syntaxAssessment.optString("overallLevel", ""))),
                subModules
        );
    }

    private static List<String> buildSyntaxProblemTags(JSONObject profile, boolean comprehension) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        addAll(tags, normalizeSyntaxProblems(optStringArray(profile, "focusStructures"), comprehension));
        addAll(tags, normalizeSyntaxProblems(optStringArray(profile, "unstableStructures"), comprehension));
        if (tags.isEmpty()) {
            addAll(tags, normalizeSyntaxProblems(optStringArray(profile, "observedStructures"), comprehension));
        }
        String overall = normalize(profile == null ? "" : profile.optString("overallLevel", ""));
        if (!overall.isEmpty() && !overall.contains("暂无")) {
            tags.add(comprehension ? "syntax_comprehension_difficulty" : "syntax_expression_difficulty");
            if (overall.contains("不稳定") || overall.contains("unstable")) {
                tags.add(comprehension ? "unstable_comprehension_accuracy" : "unstable_expression_output");
            }
        }
        return new ArrayList<>(tags);
    }

    private static List<String> buildSyntaxGoalTags(JSONObject profile, boolean comprehension) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        List<String> problemTags = buildSyntaxProblemTags(profile, comprehension);
        if (!problemTags.isEmpty()) {
            tags.add(comprehension ? "syntax_comprehension_support" : "syntax_expression_support");
        }
        for (String problemTag : problemTags) {
            if (problemTag.contains("unstable")) {
                tags.add("stability");
            }
            if (problemTag.contains("prompt_dependent")) {
                tags.add("fade_support");
            }
            if (problemTag.contains("complex_sentence")) {
                tags.add("complex_structure");
            }
        }
        return new ArrayList<>(tags);
    }

    private static List<String> normalizeSyntaxProblems(List<String> source, boolean comprehension) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (source == null) {
            return new ArrayList<>(out);
        }
        for (String item : source) {
            String normalized = normalize(item);
            if (normalized.isEmpty()) {
                continue;
            }
            if (comprehension) {
                if (containsAny(normalized, "复杂", "复句", "从句", "complex")) {
                    out.add("complex_sentence_comprehension");
                }
                if (containsAny(normalized, "结构关系", "关系", "语序", "relation")) {
                    out.add("structure_relation_comprehension");
                }
                if (containsAny(normalized, "多成分", "多信息", "多步", "multi")) {
                    out.add("multi_component_comprehension");
                }
                if (containsAny(normalized, "提示", "辅助", "依赖")) {
                    out.add("prompt_dependent_comprehension");
                }
                if (containsAny(normalized, "不稳定", "波动", "unstable")) {
                    out.add("unstable_comprehension_accuracy");
                }
                if (containsAny(normalized, "理解", "comprehension")) {
                    out.add("syntax_comprehension_difficulty");
                }
            } else {
                if (containsAny(normalized, "句长", "短句", "句子短", "short")) {
                    out.add("short_sentence_length");
                }
                if (containsAny(normalized, "句式单一", "单一", "variety")) {
                    out.add("limited_sentence_variety");
                }
                if (containsAny(normalized, "结构简单", "简单句", "simple")) {
                    out.add("simple_grammar_output");
                }
                if (containsAny(normalized, "遗漏", "漏词", "省略", "omission")) {
                    out.add("omission_in_expression");
                }
                if (containsAny(normalized, "组织", "组织弱", "排序", "organization")) {
                    out.add("weak_sentence_organization");
                }
                if (containsAny(normalized, "不稳定", "波动", "unstable")) {
                    out.add("unstable_expression_output");
                }
                if (containsAny(normalized, "表达", "造句", "output", "expression")) {
                    out.add("syntax_expression_difficulty");
                }
            }
            if (out.isEmpty() || !containsAny(normalized, "暂无", "no_data")) {
                out.add(normalized.replace(' ', '_'));
            }
        }
        return new ArrayList<>(out);
    }

    private static List<String> normalizeSyntaxFamilies(List<String> source) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (source == null) {
            return new ArrayList<>(out);
        }
        for (String item : source) {
            String normalized = normalize(item);
            if (!normalized.isEmpty()) {
                out.add(normalized.replace("理解", "").replace("表达", "").trim().replace(' ', '_'));
            }
        }
        return new ArrayList<>(out);
    }

    private static List<String> empty() {
        return new ArrayList<>();
    }

    private static String buildSeverity(JSONObject summary) {
        if (summary == null) {
            return "";
        }
        String intelligibility = normalize(summary.optString("intelligibility", ""));
        if (!intelligibility.isEmpty()) {
            return intelligibility;
        }
        JSONObject accuracy = optObject(summary, "initial_consonant_accuracy");
        return normalize(accuracy == null ? "" : accuracy.optString("accuracy", ""));
    }

    private static boolean containsMultisyllable(JSONObject summary, JSONObject articulationProfile) {
        return containsKeyword(optStringArray(articulationProfile, "wordPositionFocus"), "multi")
                || containsKeyword(optStringArray(articulationProfile, "wordPositionUnstable"), "multi")
                || containsKeyword(optStringArray(articulationProfile, "wordPositionFocus"), "syll")
                || containsKeyword(optStringArray(articulationProfile, "wordPositionUnstable"), "syll")
                || containsKeyword(optStringArray(summary, "focus"), "multi")
                || containsKeyword(optStringArray(summary, "focus"), "syll");
    }

    private static boolean containsKeyword(List<String> values, String keyword) {
        if (values == null || keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        for (String value : values) {
            if (normalize(value).contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String value, String... keywords) {
        if (value == null || value.trim().isEmpty() || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty() && value.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> normalizeErrorTypes(List<String> source) {
        List<String> out = new ArrayList<>();
        if (source == null) {
            return out;
        }
        for (String item : source) {
            String normalized = normalize(item);
            if (normalized.isEmpty()) {
                continue;
            }
            if (normalized.contains("substitution") || normalized.contains("replace")) {
                out.add("substitution");
            } else if (normalized.contains("omission") || normalized.contains("delete")) {
                out.add("omission");
            } else if (normalized.contains("distortion")) {
                out.add("distortion");
            } else if (normalized.contains("instability") || normalized.contains("inconsistent")) {
                out.add("instability");
            } else {
                out.add(normalized);
            }
        }
        return out;
    }

    private static List<String> normalizePositions(List<String> source) {
        List<String> out = new ArrayList<>();
        if (source == null) {
            return out;
        }
        for (String item : source) {
            String normalized = normalize(item);
            if (normalized.isEmpty()) {
                continue;
            }
            if (normalized.contains("initial")) {
                out.add("initial");
            } else if (normalized.contains("medial") || normalized.contains("middle")) {
                out.add("medial");
            } else if (normalized.contains("final") || normalized.contains("ending")) {
                out.add("final");
            } else if (normalized.contains("multi") || normalized.contains("syll")) {
                out.add("multisyllable");
            } else {
                out.add(normalized);
            }
        }
        return out;
    }

    private static List<String> optStringArray(JSONObject source, String key) {
        JSONArray array = source == null ? null : source.optJSONArray(key);
        List<String> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (int i = 0; i < array.length(); i++) {
            String value = normalize(array.optString(i, ""));
            if (!value.isEmpty()) {
                out.add(value);
            }
        }
        return out;
    }

    private static JSONObject optObject(JSONObject source, String key) {
        return source == null ? null : source.optJSONObject(key);
    }

    private static void addAll(LinkedHashSet<String> target, List<String> values) {
        if (target == null || values == null) {
            return;
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isEmpty()) {
                target.add(normalized);
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
