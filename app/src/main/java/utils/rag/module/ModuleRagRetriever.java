package utils.rag.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModuleRagRetriever {
    public List<RagHit> retrieve(RagQuery query, List<KnowledgeDoc> docs, int topK) {
        if (query == null || query.moduleType.trim().isEmpty() || docs == null || docs.isEmpty() || topK <= 0) {
            return Collections.emptyList();
        }

        String moduleType = normalize(query.moduleType);
        List<RagHit> hits;
        if ("syntax".equals(moduleType)) {
            hits = retrieveStructured(query, docs, topK, "syntax");
        } else if ("vocabulary".equals(moduleType)) {
            hits = retrieveStructured(query, docs, topK, "vocabulary");
        } else {
            hits = retrieveDefault(query, docs, topK);
        }
        if (hits.size() <= topK) {
            return hits;
        }
        return new ArrayList<>(hits.subList(0, topK));
    }

    private List<RagHit> retrieveDefault(RagQuery query, List<KnowledgeDoc> docs, int topK) {
        List<RagHit> hits = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            if (doc == null || !normalize(query.moduleType).equals(normalize(doc.module))) {
                continue;
            }
            RagHit hit = scoreDefault(query, doc);
            if (hit != null && hit.score > 0) {
                hits.add(hit);
                logHit("", hit);
            }
        }
        sortHits(hits);
        return hits.size() <= topK ? hits : new ArrayList<>(hits.subList(0, topK));
    }

    private List<RagHit> retrieveStructured(RagQuery query, List<KnowledgeDoc> docs, int topK, String moduleType) {
        Map<String, RagHit> merged = new LinkedHashMap<>();
        for (KnowledgeDoc doc : docs) {
            if (doc == null || !moduleType.equals(normalize(doc.module))) {
                continue;
            }
            RagHit globalHit = scoreStructuredGlobal(query, doc, moduleType);
            mergeHit(merged, globalHit);

            for (RagQuery.SubModuleQuery subModule : query.subModules) {
                RagHit hit = scoreStructuredSubModule(query, subModule, doc, moduleType);
                mergeHit(merged, hit);
            }
        }

        List<RagHit> hits = new ArrayList<>(merged.values());
        sortHits(hits);
        return hits.size() <= topK ? hits : new ArrayList<>(hits.subList(0, topK));
    }

    private void mergeHit(Map<String, RagHit> merged, RagHit hit) {
        if (merged == null || hit == null || hit.doc == null || hit.score <= 0) {
            return;
        }
        String key = normalize(hit.doc.id) + "|" + normalize(hit.subModuleName);
        RagHit existing = merged.get(key);
        if (existing == null || hit.score > existing.score) {
            merged.put(key, hit);
            logHit(hit.subModuleName, hit);
        }
    }

    private RagHit scoreDefault(RagQuery query, KnowledgeDoc doc) {
        LinkedHashSet<String> matched = new LinkedHashSet<>();
        double score = 0.0d;

        score += addMatches(matched, query.errorTypes, doc.errorTypes, 5.0d, "error:");
        score += addMatches(matched, query.targetSounds, doc.targetSounds, 3.0d, "sound:");
        score += addMatches(matched, query.targetPositions, doc.targetPositions, 2.0d, "position:");
        score += addMatches(matched, query.goalTags, doc.goalTags, 1.5d, "goal:");
        score += Math.max(0, doc.priority) * 0.1d;

        if (matched.isEmpty()) {
            return null;
        }
        return new RagHit(doc, score, new ArrayList<>(matched));
    }

    private RagHit scoreStructuredGlobal(RagQuery query, KnowledgeDoc doc, String moduleType) {
        if (query.global == null || query.global.isEmpty()) {
            return null;
        }
        if (!normalize(doc.subModule).isEmpty()) {
            return null;
        }

        LinkedHashSet<String> matched = new LinkedHashSet<>();
        double score = 0.0d;
        score += addMatches(matched, query.global.goalTags, doc.goalTags, 1.5d, "global_goal:");
        score += addMatches(matched, query.global.goalTags, doc.problemTags, 1.2d, "global_problem:");
        score += addTextMatches(matched, query.global.goalTags, doc, 0.8d, "global_text:");
        if ("vocabulary".equals(moduleType)) {
            score += addSupportingSignalWeight(matched, query.supportingSignals, doc);
        }
        score += Math.max(0, doc.priority) * 0.1d;
        if (matched.isEmpty()) {
            return null;
        }
        return new RagHit(doc, score, "global", new ArrayList<>(matched));
    }

    private RagHit scoreStructuredSubModule(RagQuery query,
                                            RagQuery.SubModuleQuery subModule,
                                            KnowledgeDoc doc,
                                            String moduleType) {
        if (subModule == null || subModule.isEmpty()) {
            return null;
        }
        String normalizedSubModule = normalize(subModule.name);
        String docSubModule = normalize(doc.subModule);
        if (!docSubModule.isEmpty() && !docSubModule.equals(normalizedSubModule)) {
            return null;
        }

        LinkedHashSet<String> matched = new LinkedHashSet<>();
        double score = 0.0d;
        score += addMatches(matched, subModule.problemTags, doc.problemTags, 5.0d, "problem:");
        score += addMatches(matched, subModule.problemTags, doc.goalTags, 2.0d, "problem_goal:");
        score += addMatches(matched, subModule.goalTags, doc.goalTags, 1.5d, "goal:");
        score += addTextMatches(matched, subModule.problemTags, doc, 1.0d, "text:");
        score += addTextMatches(matched, subModule.goalTags, doc, 0.6d, "goal_text:");
        if (!docSubModule.isEmpty() && docSubModule.equals(normalizedSubModule)) {
            score += 1.0d;
            matched.add("submodule:" + docSubModule);
        }
        if ("vocabulary".equals(moduleType)) {
            score += addSupportingSignalWeight(matched, query.supportingSignals, doc);
        }
        score += Math.max(0, doc.priority) * 0.1d;
        if (matched.isEmpty()) {
            return null;
        }
        return new RagHit(doc, score, normalizedSubModule, new ArrayList<>(matched));
    }

    private double addSupportingSignalWeight(LinkedHashSet<String> matched, List<String> supportingSignals, KnowledgeDoc doc) {
        if (supportingSignals == null || supportingSignals.isEmpty() || doc == null) {
            return 0.0d;
        }
        String text = normalize(doc.title) + " " + normalize(doc.content);
        double score = 0.0d;
        for (String signal : supportingSignals) {
            String normalized = normalize(signal);
            if (normalized.isEmpty()) {
                continue;
            }
            if ("re".equals(normalized) && containsAny(text, "repeat", "retention", "rehearsal", "保持", "复述")) {
                matched.add("supporting:re");
                score += 0.35d;
            } else if ("s".equals(normalized) && containsAny(text, "load", "stability", "task demand", "负荷", "稳定")) {
                matched.add("supporting:s");
                score += 0.35d;
            } else if ("nwr".equals(normalized) && containsAny(text, "phonological", "memory", "nonword", "音系", "保持")) {
                matched.add("supporting:nwr");
                score += 0.4d;
            }
        }
        return score;
    }

    private double addMatches(LinkedHashSet<String> matched,
                              List<String> queryValues,
                              List<String> docValues,
                              double weight,
                              String prefix) {
        if (queryValues == null || queryValues.isEmpty() || docValues == null || docValues.isEmpty()) {
            return 0.0d;
        }
        double score = 0.0d;
        for (String queryValue : queryValues) {
            String normalizedQuery = normalize(queryValue);
            if (normalizedQuery.isEmpty()) {
                continue;
            }
            for (String docValue : docValues) {
                String normalizedDoc = normalize(docValue);
                if (!normalizedDoc.isEmpty() && normalizedDoc.equals(normalizedQuery)) {
                    score += weight;
                    matched.add(prefix + normalizedDoc);
                    break;
                }
            }
        }
        return score;
    }

    private double addTextMatches(LinkedHashSet<String> matched,
                                  List<String> queryValues,
                                  KnowledgeDoc doc,
                                  double weight,
                                  String prefix) {
        if (queryValues == null || queryValues.isEmpty() || doc == null) {
            return 0.0d;
        }
        String text = normalize(doc.title) + " " + normalize(doc.content);
        double score = 0.0d;
        for (String queryValue : queryValues) {
            String normalizedQuery = normalize(queryValue);
            if (!normalizedQuery.isEmpty() && text.contains(normalizedQuery)) {
                matched.add(prefix + normalizedQuery);
                score += weight;
            }
        }
        return score;
    }

    private void sortHits(List<RagHit> hits) {
        hits.sort(new Comparator<RagHit>() {
            @Override
            public int compare(RagHit left, RagHit right) {
                int scoreCompare = Double.compare(right.score, left.score);
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                int sectionCompare = normalize(left.subModuleName).compareTo(normalize(right.subModuleName));
                if (sectionCompare != 0) {
                    return sectionCompare;
                }
                return normalize(left.doc == null ? "" : left.doc.id)
                        .compareTo(normalize(right.doc == null ? "" : right.doc.id));
            }
        });
    }

    private void logHit(String subModuleName, RagHit hit) {
        if (hit == null || hit.doc == null) {
            return;
        }
        System.out.println("ModuleRagRetriever subModule="
                + (subModuleName == null ? "" : subModuleName)
                + " docId=" + hit.doc.id
                + " score=" + hit.score);
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        String normalizedText = normalize(text);
        for (String keyword : keywords) {
            if (keyword != null && normalizedText.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
