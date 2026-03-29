package utils.rag.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class ModuleRagRetriever {
    public List<RagHit> retrieve(RagQuery query, List<KnowledgeDoc> docs, int topK) {
        if (query == null || query.moduleType.trim().isEmpty() || docs == null || docs.isEmpty() || topK <= 0) {
            return Collections.emptyList();
        }

        List<RagHit> hits = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            if (doc == null || !normalize(query.moduleType).equals(normalize(doc.module))) {
                continue;
            }
            RagHit hit = score(query, doc);
            if (hit != null && hit.score > 0) {
                hits.add(hit);
                System.out.println("ModuleRagRetriever docId=" + doc.id + " score=" + hit.score);
            }
        }

        hits.sort(new Comparator<RagHit>() {
            @Override
            public int compare(RagHit left, RagHit right) {
                int scoreCompare = Double.compare(right.score, left.score);
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                return normalize(left.doc == null ? "" : left.doc.id)
                        .compareTo(normalize(right.doc == null ? "" : right.doc.id));
            }
        });

        if (hits.size() <= topK) {
            return hits;
        }
        return new ArrayList<>(hits.subList(0, topK));
    }

    private RagHit score(RagQuery query, KnowledgeDoc doc) {
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
