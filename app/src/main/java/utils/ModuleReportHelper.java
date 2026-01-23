package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ModuleReportHelper {
    private static final String DEFAULT_NO_DATA = "\u672a\u4f5c\u7b54";

    private ModuleReportHelper() {
    }

    public static void applyModuleFindings(JSONObject plan, JSONObject evaluations) {
        if (plan == null || evaluations == null) {
            return;
        }
        try {
            JSONObject modulePlan = plan.optJSONObject("module_plan");
            if (modulePlan == null) {
                modulePlan = new JSONObject();
                plan.put("module_plan", modulePlan);
            }
            applyFindings(modulePlan, "speech_sound", buildSpeechSoundFindings(evaluations.optJSONArray("A")));
            applyFindings(modulePlan, "prelinguistic", buildPrelinguisticFindings());
            applyFindings(modulePlan, "vocabulary", buildVocabularyFindings(evaluations));
            applyFindings(modulePlan, "syntax", buildSyntaxFindings(evaluations));
            applyFindings(modulePlan, "social_pragmatics", buildSocialFindings(evaluations));
        } catch (JSONException ignored) {
        }
    }

    private static void applyFindings(JSONObject modulePlan, String moduleKey, List<String> findings) throws JSONException {
        if (modulePlan == null || moduleKey == null) {
            return;
        }
        JSONObject module = modulePlan.optJSONObject(moduleKey);
        if (module == null) {
            module = new JSONObject();
            modulePlan.put(moduleKey, module);
        }
        JSONArray arr = new JSONArray();
        if (findings == null || findings.isEmpty()) {
            arr.put(DEFAULT_NO_DATA);
        } else {
            for (String item : findings) {
                String text = safeText(item);
                if (!text.isEmpty()) {
                    arr.put(text);
                }
            }
            if (arr.length() == 0) {
                arr.put(DEFAULT_NO_DATA);
            }
        }
        module.put("key_findings", arr);
    }

    private static List<String> buildSpeechSoundFindings(JSONArray evaluationsA) {
        String summary = TreatmentPromptBuilder.buildSpeechSoundSummary(evaluationsA);
        return toList(summary);
    }

    private static List<String> buildPrelinguisticFindings() {
        return toList(DEFAULT_NO_DATA);
    }

    private static List<String> buildVocabularyFindings(JSONObject evaluations) {
        String summary = TreatmentPromptBuilder.buildVocabularySummary(evaluations);
        return toList(summary);
    }

    private static List<String> buildSyntaxFindings(JSONObject evaluations) {
        String summary = TreatmentPromptBuilder.buildSyntaxSummary(evaluations);
        return toList(summary);
    }

    private static List<String> buildSocialFindings(JSONObject evaluations) {
        String summary = TreatmentPromptBuilder.buildNarrativeSummary(evaluations);
        return toList(summary);
    }

    private static List<String> toList(String value) {
        List<String> list = new ArrayList<>();
        String text = safeText(value);
        if (text.isEmpty()) {
            list.add(DEFAULT_NO_DATA);
            return list;
        }
        list.add(text);
        return list;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
