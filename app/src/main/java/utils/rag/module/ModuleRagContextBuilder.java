package utils.rag.module;

import java.util.LinkedHashSet;
import java.util.List;

public final class ModuleRagContextBuilder {
    public String build(List<RagHit> hits) {
        return build(hits, ModuleRagConfig.DEFAULT_MAX_CONTENT_LENGTH);
    }

    public String build(List<RagHit> hits, int maxContentLength) {
        if (hits == null || hits.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("【检索参考知识】\n");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        int index = 1;
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc == null || doc.id.trim().isEmpty() || !seen.add(doc.id.trim())) {
                continue;
            }
            builder.append("[").append(index++).append("] ")
                    .append(safe(doc.title))
                    .append("：")
                    .append(trim(safe(doc.content), maxContentLength))
                    .append("\n");
        }
        if (index == 1) {
            return "";
        }
        return builder.toString().trim();
    }

    private String trim(String value, int maxContentLength) {
        if (value.length() <= maxContentLength || maxContentLength <= 0) {
            return value;
        }
        return value.substring(0, maxContentLength).trim() + "...";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
