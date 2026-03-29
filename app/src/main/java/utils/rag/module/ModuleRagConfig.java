package utils.rag.module;

import java.util.Locale;

public final class ModuleRagConfig {
    public static final boolean ENABLE_MODULE_RAG = true;
    public static final int DEFAULT_TOP_K = 4;
    public static final int DEFAULT_MAX_CONTENT_LENGTH = 160;

    private ModuleRagConfig() {
    }

    public static boolean isEnabledFor(String moduleType) {
        String normalized = normalize(moduleType);
        return ENABLE_MODULE_RAG
                && ("articulation".equals(normalized)
                || "syntax".equals(normalized)
                || "vocabulary".equals(normalized));
    }

    public static String assetPathForModule(String moduleType) {
        String normalized = normalize(moduleType);
        if ("articulation".equals(normalized)) {
            return "kb/articulation_kb.json";
        }
        if ("syntax".equals(normalized)) {
            return "kb/syntax_kb.json";
        }
        if ("vocabulary".equals(normalized)) {
            return "kb/vocabulary_kb.json";
        }
        return "";
    }

    static String normalize(String moduleType) {
        return moduleType == null ? "" : moduleType.trim().toLowerCase(Locale.ROOT);
    }
}
