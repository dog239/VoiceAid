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
     * Single-module LLM input contract and retrieval policy:
     * 1. Current prompt input fields are built from moduleType/moduleTitle/subtypes/ageMonths/chiefComplaint/
     *    moduleEvaluations(summary + raw measured arrays)/existingModuleReport.
     * 2. RAG query only uses structured module summary fields that are stable and low-risk:
     *    articulationProfile.focusPhonemes / unstablePhonemes / phonologyProcesses /
     *    phonemeSummary.targets / intelligibility / initial_consonant_accuracy.
     * 3. chiefComplaint, existingModuleReport, name, address, phone, familyMembers and other free text
     *    are intentionally excluded from retrieval to reduce privacy leakage and noisy matching.
     * 4. First MVP is articulation-only; other modules return an empty query and keep the old prompt path.
     */
    public static RagQuery build(String moduleType, JSONObject moduleInput) {
        String normalizedModuleType = normalize(moduleType);
        if (!"articulation".equals(normalizedModuleType)) {
            return new RagQuery(normalizedModuleType, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "");
        }

        JSONObject moduleEvaluations = optObject(moduleInput, "moduleEvaluations");
        JSONObject summary = optObject(moduleEvaluations, "summary");
        JSONObject phonemeSummary = optObject(summary, "phonemeSummary");
        JSONObject articulationProfile = optObject(summary, "articulationProfile");

        LinkedHashSet<String> problemTags = new LinkedHashSet<>();
        addAll(problemTags, optStringArray(articulationProfile, "phonologyProcesses"));
        addAll(problemTags, optStringArray(articulationProfile, "wordPositionFocus"));
        addAll(problemTags, optStringArray(articulationProfile, "wordPositionUnstable"));
        addIfMeaningful(problemTags, normalize(summary == null ? "" : summary.optString("intelligibility", "")));

        LinkedHashSet<String> goalTags = new LinkedHashSet<>();
        addAll(goalTags, optStringArray(phonemeSummary, "targets"));

        LinkedHashSet<String> weakPoints = new LinkedHashSet<>();
        addAll(weakPoints, optStringArray(articulationProfile, "focusPhonemes"));
        addAll(weakPoints, optStringArray(articulationProfile, "unstablePhonemes"));

        String severity = buildSeverity(summary);
        return new RagQuery(
                normalizedModuleType,
                new ArrayList<>(problemTags),
                new ArrayList<>(goalTags),
                new ArrayList<>(weakPoints),
                severity
        );
    }

    private static String buildSeverity(JSONObject summary) {
        if (summary == null) {
            return "";
        }
        JSONObject accuracy = optObject(summary, "initial_consonant_accuracy");
        String accuracyText = normalize(accuracy == null ? "" : accuracy.optString("accuracy", ""));
        String intelligibility = normalize(summary.optString("intelligibility", ""));
        if (!intelligibility.isEmpty()) {
            return intelligibility;
        }
        return accuracyText;
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
            addIfMeaningful(target, value);
        }
    }

    private static void addIfMeaningful(LinkedHashSet<String> target, String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return;
        }
        if (normalized.contains("138") || normalized.contains("电话") || normalized.contains("地址")) {
            return;
        }
        target.add(normalized);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
