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
        if (isSocialHits(hits)) {
            return buildSocialContext(hits, maxContentLength);
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

    private String buildSocialContext(List<RagHit> hits, int maxContentLength) {
        Map<String, List<KnowledgeDoc>> sections = new LinkedHashMap<>();
        sections.put("interaction", new ArrayList<KnowledgeDoc>());
        sections.put("generalization", new ArrayList<KnowledgeDoc>());
        sections.put("family", new ArrayList<KnowledgeDoc>());

        LinkedHashSet<String> seenKeys = new LinkedHashSet<>();
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc == null || doc.id.trim().isEmpty()) {
                continue;
            }
            String section = normalizeSocialSection(hit, doc);
            String dedupeKey = section + "|" + doc.id.trim();
            if (!seenKeys.add(dedupeKey)) {
                continue;
            }
            sections.get(section).add(doc);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("【检索参考知识】\n");
        int index = 1;
        index = appendSection(builder, "【社交互动相关建议】", sections.get("interaction"), index, maxContentLength);
        index = appendSection(builder, "【情境与泛化相关建议】", sections.get("generalization"), index, maxContentLength);
        index = appendSection(builder, "【家庭与支持建议】", sections.get("family"), index, maxContentLength);
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

    private boolean isSocialHits(List<RagHit> hits) {
        for (RagHit hit : hits) {
            KnowledgeDoc doc = hit == null ? null : hit.doc;
            if (doc != null && "social".equals(normalize(doc.module))) {
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

    private String normalizeSocialSection(RagHit hit, KnowledgeDoc doc) {
        if (doc == null) {
            return "interaction";
        }
        if (containsAudience(doc, "family", "caregiver", "parent")) {
            return "family";
        }
        if (containsScenario(doc, "daily_interaction", "peer_interaction", "picture_or_story_context",
                "adult_led_interaction", "challenging_social_situation", "familiar_context")
                || containsMatched(hit, "social_scenario:")
                || containsMatched(hit, "supporting:cross_scenario_sampling")
                || containsText(doc, "generalization", "daily", "peer", "group", "story", "routine")) {
            return "generalization";
        }
        return "interaction";
    }

    private boolean containsAudience(KnowledgeDoc doc, String... keywords) {
        if (doc == null || doc.audience == null) {
            return false;
        }
        for (String audience : doc.audience) {
            if (containsAny(audience, keywords)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsScenario(KnowledgeDoc doc, String... keywords) {
        if (doc == null || doc.scenarioTags == null) {
            return false;
        }
        for (String tag : doc.scenarioTags) {
            if (containsAny(tag, keywords)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsMatched(RagHit hit, String prefix) {
        if (hit == null || hit.matchedTags == null || prefix == null) {
            return false;
        }
        for (String tag : hit.matchedTags) {
            if (tag != null && tag.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsText(KnowledgeDoc doc, String... keywords) {
        String text = safe(doc == null ? "" : doc.title) + " " + safe(doc == null ? "" : doc.content);
        return containsAny(text, keywords);
    }

    private boolean containsAny(String value, String... keywords) {
        String normalized = normalize(value);
        if (normalized.isEmpty() || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (!normalize(keyword).isEmpty() && normalized.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
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
