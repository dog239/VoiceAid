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
        LinkedHashSet<String> matchedTags = new LinkedHashSet<>();
        double score = 0.0d;

        for (String tag : safeList(doc.tags)) {
            String normalizedTag = normalize(tag);
            if (contains(query.problemTags, normalizedTag)) {
                score += 3.0d;
                matchedTags.add(normalizedTag);
            }
            if (contains(query.goalTags, normalizedTag)) {
                score += 2.5d;
                matchedTags.add(normalizedTag);
            }
            if (contains(query.weakPoints, normalizedTag)) {
                score += 2.0d;
                matchedTags.add(normalizedTag);
            }
            if (!query.severity.isEmpty() && query.severity.equals(normalizedTag)) {
                score += 1.5d;
                matchedTags.add(normalizedTag);
            }
        }

        for (String subtype : safeList(doc.subtypes)) {
            String normalizedSubtype = normalize(subtype);
            if (contains(query.goalTags, normalizedSubtype) || contains(query.weakPoints, normalizedSubtype)) {
                score += 1.0d;
                matchedTags.add(normalizedSubtype);
            }
        }

        score += Math.max(0, doc.priority) * 0.1d;
        if (matchedTags.isEmpty()) {
            return null;
        }
        return new RagHit(doc, score, new ArrayList<>(matchedTags));
    }

    private boolean contains(List<String> values, String expected) {
        if (values == null || expected == null || expected.trim().isEmpty()) {
            return false;
        }
        for (String value : values) {
            if (normalize(value).equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? Collections.<String>emptyList() : values;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
