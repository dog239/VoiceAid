package utils.rag.module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModuleRagContextBuilder {
    public String build(List<RagHit> hits) {
        return build(hits, ModuleRagConfig.DEFAULT_MAX_CONTENT_LENGTH);
    }

    public String build(List<RagHit> hits, int maxContentLength) {
        if (hits == null || hits.isEmpty()) {
            return "";
        }
        if (isSyntaxHits(hits)) {
            return buildStructuredContext(hits, maxContentLength, "syntax");
        }
        if (isVocabularyHits(hits)) {
            return buildStructuredContext(hits, maxContentLength, "vocabulary");
        }
        return buildDefaultContext(hits, maxContentLength);
    }

    private String buildDefaultContext(List<RagHit> hits, int maxContentLength) {
        StringBuilder builder = new StringBuilder();
        builder.append("【检索参考知识】\n");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        int index = 1;
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc == null || doc.id.trim().isEmpty() || !seen.add(doc.id.trim())) {
                continue;
            }
            appendEntry(builder, index++, doc, maxContentLength);
        }
        if (index == 1) {
            return "";
        }
        return builder.toString().trim();
    }

    private String buildStructuredContext(List<RagHit> hits, int maxContentLength, String moduleType) {
        Map<String, List<KnowledgeDoc>> sections = new LinkedHashMap<>();
        if ("syntax".equals(moduleType)) {
            sections.put("comprehension", new ArrayList<KnowledgeDoc>());
            sections.put("expression", new ArrayList<KnowledgeDoc>());
            sections.put("global", new ArrayList<KnowledgeDoc>());
        } else {
            sections.put("receptive", new ArrayList<KnowledgeDoc>());
            sections.put("expressive", new ArrayList<KnowledgeDoc>());
            sections.put("global", new ArrayList<KnowledgeDoc>());
        }

        LinkedHashSet<String> seenKeys = new LinkedHashSet<>();
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc == null || doc.id.trim().isEmpty()) {
                continue;
            }
            String section = normalizeSection(moduleType, hit.subModuleName, doc.subModule);
            String dedupeKey = section + "|" + doc.id.trim();
            if (!seenKeys.add(dedupeKey)) {
                continue;
            }
            List<KnowledgeDoc> docs = sections.get(section);
            if (docs == null) {
                docs = new ArrayList<>();
                sections.put(section, docs);
            }
            docs.add(doc);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("【检索参考知识】\n");
        int index = 1;
        if ("syntax".equals(moduleType)) {
            index = appendSection(builder, "【句法理解相关建议】", sections.get("comprehension"), index, maxContentLength);
            index = appendSection(builder, "【句法表达相关建议】", sections.get("expression"), index, maxContentLength);
            index = appendSection(builder, "【句法通用建议】", sections.get("global"), index, maxContentLength);
        } else {
            index = appendSection(builder, "【词汇理解相关建议】", sections.get("receptive"), index, maxContentLength);
            index = appendSection(builder, "【词汇表达相关建议】", sections.get("expressive"), index, maxContentLength);
            index = appendSection(builder, "【词汇训练通用建议】", sections.get("global"), index, maxContentLength);
        }
        if (index == 1) {
            return "";
        }
        return builder.toString().trim();
    }

    private int appendSection(StringBuilder builder,
                              String title,
                              List<KnowledgeDoc> docs,
                              int startIndex,
                              int maxContentLength) {
        if (docs == null || docs.isEmpty()) {
            return startIndex;
        }
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }
        builder.append(title).append('\n');
        int index = startIndex;
        for (KnowledgeDoc doc : docs) {
            appendEntry(builder, index++, doc, maxContentLength);
        }
        return index;
    }

    private void appendEntry(StringBuilder builder, int index, KnowledgeDoc doc, int maxContentLength) {
        builder.append("[")
                .append(index)
                .append("] ")
                .append(safe(doc == null ? "" : doc.title))
                .append("：")
                .append(trim(safe(doc == null ? "" : doc.content), maxContentLength))
                .append("\n");
    }

    private boolean isSyntaxHits(List<RagHit> hits) {
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc != null && "syntax".equals(normalize(doc.module))) {
                return true;
            }
            if (hit != null && ("comprehension".equals(normalize(hit.subModuleName))
                    || "expression".equals(normalize(hit.subModuleName)))) {
                return true;
            }
        }
        return false;
    }

    private boolean isVocabularyHits(List<RagHit> hits) {
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc != null && "vocabulary".equals(normalize(doc.module))) {
                return true;
            }
            if (hit != null && ("receptive".equals(normalize(hit.subModuleName))
                    || "expressive".equals(normalize(hit.subModuleName)))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeSection(String moduleType, String hitSubModule, String docSubModule) {
        String normalized = normalize(hitSubModule);
        if (normalized.isEmpty()) {
            normalized = normalize(docSubModule);
        }
        if ("syntax".equals(moduleType)) {
            if ("comprehension".equals(normalized) || "expression".equals(normalized)) {
                return normalized;
            }
        } else if ("vocabulary".equals(moduleType)) {
            if ("receptive".equals(normalized) || "expressive".equals(normalized)) {
                return normalized;
            }
        }
        return "global";
    }

    private String trim(String value, int maxContentLength) {
        if (maxContentLength <= 0 || value.length() <= maxContentLength) {
            return value;
        }
        return value.substring(0, maxContentLength).trim() + "...";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
