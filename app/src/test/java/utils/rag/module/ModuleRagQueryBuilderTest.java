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

        assertTrue(query.problemTags.contains("替代"));
        assertTrue(query.goalTags.contains("k"));
        assertTrue(query.weakPoints.contains("g"));
    }

    @Test
    public void build_shouldHandleMissingFields() {
        RagQuery query = ModuleRagQueryBuilder.build("articulation", new JSONObject());

        assertNotNull(query);
        assertNotNull(query.problemTags);
        assertNotNull(query.goalTags);
        assertNotNull(query.weakPoints);
    }

    @Test
    public void build_shouldReturnNonNullQuery_whenSummaryEmpty() throws Exception {
        JSONObject moduleInput = new JSONObject();
        moduleInput.put("moduleEvaluations", new JSONObject().put("summary", new JSONObject()));

        RagQuery query = ModuleRagQueryBuilder.build("articulation", moduleInput);

        assertNotNull(query);
        assertTrue(query.problemTags.isEmpty());
    }

    @Test
    public void build_shouldNotIncludePrivacyFields() throws Exception {
        JSONObject input = articulationModuleInput();
        input.put("chiefComplaint", "家长联系电话13800138000，地址测试路");

        RagQuery query = ModuleRagQueryBuilder.build("articulation", input);
        String queryJson = query.toJson().toString();

        assertFalse(queryJson.contains("13800138000"));
        assertFalse(queryJson.contains("地址"));
    }

    private JSONObject articulationModuleInput() throws Exception {
        JSONObject summary = new JSONObject();
        summary.put("intelligibility", "中度");
        summary.put("initial_consonant_accuracy", new JSONObject().put("accuracy", "45.00%"));
        summary.put("phonemeSummary", new JSONObject().put("targets", new JSONArray().put("k").put("s")));
        summary.put("articulationProfile", new JSONObject()
                .put("focusPhonemes", new JSONArray().put("g").put("sh"))
                .put("unstablePhonemes", new JSONArray().put("s"))
                .put("wordPositionFocus", new JSONArray().put("词首位置"))
                .put("wordPositionUnstable", new JSONArray().put("多音节"))
                .put("phonologyProcesses", new JSONArray().put("替代")));

        return new JSONObject()
                .put("moduleType", "articulation")
                .put("moduleEvaluations", new JSONObject().put("summary", summary));
    }
}
