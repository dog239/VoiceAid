package utils.rag.module;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ModuleRagQueryBuilderTest {

    @Test
    public void build_shouldExtractProblemTags_forArticulationSummary() throws Exception {
        RagQuery query = ModuleRagQueryBuilder.build("articulation", articulationModuleInput());

        assertTrue(query.errorTypes.contains("substitution"));
        assertTrue(query.targetSounds.contains("k"));
        assertTrue(query.targetPositions.contains("initial"));
        assertTrue(query.goalTags.contains("home_practice"));
    }

    @Test
    public void build_shouldHandleMissingFields() {
        RagQuery query = ModuleRagQueryBuilder.build("articulation", new JSONObject());

        assertNotNull(query);
        assertNotNull(query.errorTypes);
        assertNotNull(query.targetSounds);
        assertNotNull(query.targetPositions);
        assertNotNull(query.goalTags);
    }

    @Test
    public void build_shouldReturnNonNullQuery_whenSummaryEmpty() throws Exception {
        JSONObject moduleInput = new JSONObject();
        moduleInput.put("moduleEvaluations", new JSONObject().put("summary", new JSONObject()));

        RagQuery query = ModuleRagQueryBuilder.build("articulation", moduleInput);

        assertNotNull(query);
        assertTrue(query.errorTypes.isEmpty());
    }

    @Test
    public void build_shouldNotIncludePrivacyFields() throws Exception {
        JSONObject input = articulationModuleInput();
        input.put("chiefComplaint", "Parent phone 13800138000 address test road");

        RagQuery query = ModuleRagQueryBuilder.build("articulation", input);
        String queryJson = query.toJson().toString();

        assertFalse(queryJson.contains("13800138000"));
        assertFalse(queryJson.contains("address test road"));
    }

    private JSONObject articulationModuleInput() throws Exception {
        JSONObject summary = new JSONObject();
        summary.put("intelligibility", "moderate");
        summary.put("initial_consonant_accuracy", new JSONObject().put("accuracy", "45.00%"));
        summary.put("phonemeSummary", new JSONObject().put("targets", new JSONArray().put("k").put("s")));
        summary.put("articulationProfile", new JSONObject()
                .put("focusPhonemes", new JSONArray().put("g").put("sh"))
                .put("unstablePhonemes", new JSONArray().put("s"))
                .put("wordPositionFocus", new JSONArray().put("initial"))
                .put("wordPositionUnstable", new JSONArray().put("multisyllable"))
                .put("phonologyProcesses", new JSONArray().put("substitution")));

        return new JSONObject()
                .put("moduleType", "articulation")
                .put("moduleEvaluations", new JSONObject().put("summary", summary));
    }
}
