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
     * 4. Syntax and vocabulary keep a global layer plus sub-modules for retrieval.
     */
    public static RagQuery build(String moduleType, JSONObject moduleInput) {
        String normalizedModuleType = normalize(moduleType);
        if ("articulation".equals(normalizedModuleType)) {
            return buildArticulationQuery(normalizedModuleType, moduleInput);
        }
        if ("syntax".equals(normalizedModuleType)) {
            return buildSyntaxQuery(normalizedModuleType, moduleInput);
        }
        if ("vocabulary".equals(normalizedModuleType)) {
            return buildVocabularyQuery(normalizedModuleType, moduleInput);
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
        if (containsKeyword(optStringArray(syntaxAssessment, "unstable"), "unstable")) {
            globalGoalTags.add("stability");
        }

        List<RagQuery.SubModuleQuery> subModules = new ArrayList<>();
        subModules.add(comprehension);
        subModules.add(expression);
        String severity = normalize(syntaxAssessment == null ? "" : syntaxAssessment.optString("overallLevel", ""));
        return new RagQuery(
                normalizedModuleType,
                empty(),
                empty(),
                empty(),
                new ArrayList<>(globalGoalTags),
                severity,
                new RagQuery.GlobalQuery(new ArrayList<>(globalGoalTags), severity),
                subModules
        );
    }

    private static RagQuery buildVocabularyQuery(String normalizedModuleType, JSONObject moduleInput) {
        JSONObject summary = optObject(optObject(moduleInput, "moduleEvaluations"), "summary");
        JSONObject vocabularyAssessment = optObject(summary, "vocabularyAssessment");
        JSONObject receptive = optObject(vocabularyAssessment, "receptive");
        JSONObject expressive = optObject(vocabularyAssessment, "expressive");

        RagQuery.SubModuleQuery receptiveQuery = new RagQuery.SubModuleQuery(
                "receptive",
                buildVocabularyProblemTags(vocabularyAssessment, receptive, true),
                buildVocabularyGoalTags(vocabularyAssessment, receptive, true),
                normalize(readAccuracyOrLevel(receptive))
        );
        RagQuery.SubModuleQuery expressiveQuery = new RagQuery.SubModuleQuery(
                "expressive",
                buildVocabularyProblemTags(vocabularyAssessment, expressive, false),
                buildVocabularyGoalTags(vocabularyAssessment, expressive, false),
                normalize(readAccuracyOrLevel(expressive))
        );

        LinkedHashSet<String> globalGoalTags = new LinkedHashSet<>();
        addAll(globalGoalTags, normalizeVocabularyGlobalGoalTags(vocabularyAssessment));
        if (!receptiveQuery.problemTags.isEmpty() && !expressiveQuery.problemTags.isEmpty()) {
            globalGoalTags.add("vocabulary_integration");
        }

        List<RagQuery.SubModuleQuery> subModules = new ArrayList<>();
        subModules.add(receptiveQuery);
        subModules.add(expressiveQuery);

        List<String> supportingSignals = buildVocabularySupportingSignals(summary);
        String severity = normalize(vocabularyAssessment == null ? "" : vocabularyAssessment.optString("overallLevel", ""));
        return new RagQuery(
                normalizedModuleType,
                empty(),
                empty(),
                empty(),
                new ArrayList<>(globalGoalTags),
                severity,
                supportingSignals,
                new RagQuery.GlobalQuery(new ArrayList<>(globalGoalTags), severity),
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
        if (!overall.isEmpty() && !overall.contains("no_data")) {
            tags.add(comprehension ? "syntax_comprehension_difficulty" : "syntax_expression_difficulty");
            if (overall.contains("unstable")) {
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

    private static List<String> buildVocabularyProblemTags(JSONObject vocabularyAssessment,
                                                           JSONObject profile,
                                                           boolean receptive) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        addVocabularyTags(tags, optStringArray(vocabularyAssessment, "focus"), receptive);
        addVocabularyTags(tags, optStringArray(vocabularyAssessment, "unstable"), receptive);
        addVocabularyCategoryTags(tags, optObject(profile, "categories"), receptive);

        String relativeProfile = normalize(vocabularyAssessment == null
                ? ""
                : vocabularyAssessment.optString("relativeProfile", ""));
        if (receptive && containsAny(relativeProfile, "receptive_stronger", "understanding stronger")) {
            tags.add("expressive_weaker_than_receptive");
        }
        if (!receptive && containsAny(relativeProfile, "receptive_stronger", "understanding stronger")) {
            tags.add("understanding_output_imbalance");
        }
        if (receptive && containsAny(relativeProfile, "expressive_stronger", "expression stronger")) {
            tags.add("input_weaker_than_output");
        }
        if (!receptive && containsAny(relativeProfile, "expressive_stronger", "expression stronger")) {
            tags.add("expressive_relative_strength");
        }
        return new ArrayList<>(tags);
    }

    private static List<String> buildVocabularyGoalTags(JSONObject vocabularyAssessment,
                                                        JSONObject profile,
                                                        boolean receptive) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        List<String> problemTags = buildVocabularyProblemTags(vocabularyAssessment, profile, receptive);
        if (!problemTags.isEmpty()) {
            tags.add(receptive ? "receptive_vocabulary_support" : "expressive_vocabulary_support");
        }
        for (String problemTag : problemTags) {
            if (problemTag.contains("unstable")) {
                tags.add("stability");
            }
            if (problemTag.contains("semantic") || problemTag.contains("meaning")) {
                tags.add("semantic_support");
            }
            if (problemTag.contains("naming") || problemTag.contains("retrieval")) {
                tags.add("retrieval_support");
            }
        }
        return new ArrayList<>(tags);
    }

    private static void addVocabularyTags(LinkedHashSet<String> tags, List<String> values, boolean receptive) {
        for (String value : values) {
            String normalized = normalize(value);
            if (normalized.isEmpty()) {
                continue;
            }
            if (receptive) {
                if (containsAny(normalized, "receptive", "understand", "comprehension", "词义理解", "语义理解")) {
                    tags.add("semantic_comprehension_difficulty");
                }
                if (containsAny(normalized, "category", "分类")) {
                    tags.add("category_comprehension_difficulty");
                }
                if (containsAny(normalized, "accuracy", "正确率", "提示减少")) {
                    tags.add("unstable_receptive_accuracy");
                }
                if (containsAny(normalized, "input", "输入端")) {
                    tags.add("input_side_weakness");
                }
            } else {
                if (containsAny(normalized, "naming", "命名")) {
                    tags.add("naming_difficulty");
                }
                if (containsAny(normalized, "retrieve", "提取")) {
                    tags.add("slow_lexical_retrieval");
                }
                if (containsAny(normalized, "expressive", "表达", "output")) {
                    tags.add("expressive_vocabulary_weakness");
                }
                if (containsAny(normalized, "unstable", "不稳定")) {
                    tags.add("unstable_expressive_output");
                }
                if (containsAny(normalized, "主动表达", "主动")) {
                    tags.add("limited_spontaneous_expression");
                }
            }
            tags.add(normalized.replace(' ', '_'));
        }
    }

    private static void addVocabularyCategoryTags(LinkedHashSet<String> tags, JSONObject categories, boolean receptive) {
        if (categories == null) {
            return;
        }
        String[] keys = new String[]{"noun", "verb", "adjective", "category_noun"};
        for (String key : keys) {
            JSONObject category = optObject(categories, key);
            String status = normalize(category == null ? "" : category.optString("status", ""));
            if ("focus".equals(status)) {
                tags.add(receptive ? key + "_receptive_focus" : key + "_expressive_focus");
            } else if ("unstable".equals(status)) {
                tags.add(receptive ? key + "_receptive_unstable" : key + "_expressive_unstable");
            }
        }
    }

    private static List<String> normalizeVocabularyGlobalGoalTags(JSONObject vocabularyAssessment) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        addAll(tags, normalizeVocabularyCategoryArray(optStringArray(vocabularyAssessment, "crossDomainPriorityCategories")));
        addAll(tags, normalizeVocabularyCategoryArray(optStringArray(vocabularyAssessment, "crossDomainUnstableCategories")));
        String overallLevel = normalize(vocabularyAssessment == null ? "" : vocabularyAssessment.optString("overallLevel", ""));
        if (containsAny(overallLevel, "receptive_stronger", "expressive_stronger", "mixed", "both_need_support")) {
            tags.add("balance_support");
        }
        if (containsAny(overallLevel, "both_need_support")) {
            tags.add("high_frequency_core_words");
        }
        return new ArrayList<>(tags);
    }

    private static List<String> buildVocabularySupportingSignals(JSONObject summary) {
        LinkedHashSet<String> signals = new LinkedHashSet<>();
        addSupportingSignal(signals, "re", optObject(summary, "RE"));
        addSupportingSignal(signals, "s", optObject(summary, "S"));
        addSupportingSignal(signals, "nwr", optObject(summary, "NWR"));
        return new ArrayList<>(signals);
    }

    private static void addSupportingSignal(LinkedHashSet<String> signals, String signal, JSONObject profile) {
        if (profile == null) {
            return;
        }
        String accuracy = normalize(profile.optString("accuracy", ""));
        int total = profile.optInt("total", 0);
        int completed = profile.optInt("completed", total);
        if (total > 0 || completed > 0 || !accuracy.isEmpty()) {
            signals.add(signal.toUpperCase(Locale.ROOT));
        }
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
                if (containsAny(normalized, "complex", "复杂句", "从句")) {
                    out.add("complex_sentence_comprehension");
                }
                if (containsAny(normalized, "relation", "结构关系", "语序")) {
                    out.add("structure_relation_comprehension");
                }
                if (containsAny(normalized, "multi", "多成分", "多信息", "多步")) {
                    out.add("multi_component_comprehension");
                }
                if (containsAny(normalized, "prompt", "提示", "辅助", "依赖")) {
                    out.add("prompt_dependent_comprehension");
                }
                if (containsAny(normalized, "unstable", "不稳定", "波动")) {
                    out.add("unstable_comprehension_accuracy");
                }
                if (containsAny(normalized, "comprehension", "理解")) {
                    out.add("syntax_comprehension_difficulty");
                }
            } else {
                if (containsAny(normalized, "short", "句长短", "短句")) {
                    out.add("short_sentence_length");
                }
                if (containsAny(normalized, "variety", "句式单一", "单一")) {
                    out.add("limited_sentence_variety");
                }
                if (containsAny(normalized, "simple", "结构简单", "简单句")) {
                    out.add("simple_grammar_output");
                }
                if (containsAny(normalized, "omission", "遗漏", "漏词", "省略")) {
                    out.add("omission_in_expression");
                }
                if (containsAny(normalized, "organization", "组织", "排序")) {
                    out.add("weak_sentence_organization");
                }
                if (containsAny(normalized, "unstable", "不稳定", "波动")) {
                    out.add("unstable_expression_output");
                }
                if (containsAny(normalized, "expression", "表达", "造句", "output")) {
                    out.add("syntax_expression_difficulty");
                }
            }
            if (!containsAny(normalized, "no_data", "暂无")) {
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

    private static List<String> normalizeVocabularyCategoryArray(List<String> source) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String item : source) {
            String normalized = normalize(item);
            if (!normalized.isEmpty()) {
                out.add(normalized.replace(' ', '_'));
            }
        }
        return new ArrayList<>(out);
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
            if (normalize(value).contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String value, String... keywords) {
        if (value == null || value.trim().isEmpty() || keywords == null) {
            return false;
        }
        String normalizedValue = normalize(value);
        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            if (normalizedValue.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
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

    private static String readAccuracyOrLevel(JSONObject profile) {
        if (profile == null) {
            return "";
        }
        String accuracy = profile.optString("accuracy", "");
        if (!normalize(accuracy).isEmpty()) {
            return accuracy;
        }
        return profile.optString("overallLevel", "");
    }

    private static List<String> empty() {
        return new ArrayList<>();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
