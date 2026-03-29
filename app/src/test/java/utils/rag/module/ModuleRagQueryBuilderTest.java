package utils.rag.module;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void buildSyntaxQuery_shouldSplitComprehensionAndExpressionSignals() throws Exception {
        RagQuery query = ModuleRagQueryBuilder.build("syntax", syntaxModuleInput());

        assertNotNull(query);
        assertEquals(2, query.subModules.size());
        assertEquals("comprehension", query.subModules.get(0).name);
        assertEquals("expression", query.subModules.get(1).name);
        assertTrue(query.subModules.get(0).problemTags.contains("complex_sentence_comprehension"));
        assertTrue(query.subModules.get(1).problemTags.contains("limited_sentence_variety"));
    }

    @Test
    public void buildSyntaxQuery_shouldHandleMergedSummaryWithoutCrash() throws Exception {
        JSONObject moduleInput = new JSONObject()
                .put("moduleType", "syntax")
                .put("moduleEvaluations", new JSONObject().put("summary", new JSONObject()));

        RagQuery query = ModuleRagQueryBuilder.build("syntax", moduleInput);

        assertNotNull(query);
        assertNotNull(query.subModules);
        assertEquals(2, query.subModules.size());
    }

    @Test
    public void buildSyntaxQuery_shouldNotLeakPrivacyFields() throws Exception {
        JSONObject input = syntaxModuleInput();
        input.put("chiefComplaint", "call me 13800138000");

        RagQuery query = ModuleRagQueryBuilder.build("syntax", input);
        String queryJson = query.toJson().toString();

        assertFalse(queryJson.contains("13800138000"));
        assertFalse(queryJson.contains("chiefComplaint"));
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

    private JSONObject syntaxModuleInput() throws Exception {
        JSONObject rg = new JSONObject()
                .put("overallLevel", "句法理解不稳定，复杂句理解较弱")
                .put("focusStructures", new JSONArray().put("复杂句理解").put("结构关系理解"))
                .put("unstableStructures", new JSONArray().put("理解正确率不稳定"));
        JSONObject se = new JSONObject()
                .put("overallLevel", "句法表达较弱，输出不稳定")
                .put("focusStructures", new JSONArray().put("句式单一").put("表达遗漏"))
                .put("unstableStructures", new JSONArray().put("句子组织弱"));
        JSONObject assessment = new JSONObject()
                .put("overallLevel", "句法理解与表达均需支持")
                .put("sharedPriorityStructures", new JSONArray().put("因果句"))
                .put("sharedUnstableStructures", new JSONArray().put("并列句"));
        JSONObject summary = new JSONObject()
                .put("rgComprehension", rg)
                .put("seExpression", se)
                .put("syntaxAssessment", assessment);

        return new JSONObject()
                .put("moduleType", "syntax")
                .put("moduleEvaluations", new JSONObject().put("summary", summary));
    }
}
