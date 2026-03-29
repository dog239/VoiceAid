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
     * Retrieval input policy for the current single-module articulation flow:
     * 1. Query only uses stable structured summary fields from moduleEvaluations.summary.
     * 2. Preferred fields for v2 KB retrieval are errorTypes, targetSounds, targetPositions, goalTags.
     * 3. Prompt-only fields such as chiefComplaint and existingModuleReport stay out of retrieval.
     * 4. Privacy-sensitive or free-text fields like name, address, phone and family text never enter query.
     */
    public static RagQuery build(String moduleType, JSONObject moduleInput) {
        String normalizedModuleType = normalize(moduleType);
        if (!"articulation".equals(normalizedModuleType)) {
            return new RagQuery(normalizedModuleType, empty(), empty(), empty(), empty(), "");
        }

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
