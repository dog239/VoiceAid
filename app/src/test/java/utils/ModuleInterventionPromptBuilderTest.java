package utils;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleInterventionPromptBuilderTest {

    @Test
    public void buildUserPrompt_shouldWorkWithoutRag() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("articulation", childData());

        assertTrue(prompt.contains("\"moduleType\":\"articulation\""));
        assertTrue(prompt.contains("schema"));
    }

    @Test
    public void buildUserPromptWithRag_shouldIncludeRagContext() throws Exception {
        String ragContext = "【检索参考知识】\n[1] Substitution principle：Stabilize initial-position targets.";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag("articulation", childData(), ragContext);

        assertTrue(prompt.contains(ragContext));
    }

    @Test
    public void buildUserPromptWithRag_shouldKeepSchemaConstraint() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "articulation",
                childData(),
                "【检索参考知识】\n[1] Example：Reference."
        );

        assertTrue(prompt.contains("schema"));
        assertTrue(prompt.contains("\"smartGoal\""));
    }

    @Test
    public void buildUserPromptWithRag_shouldFallbackWhenRagEmpty() throws Exception {
        String withoutRag = ModuleInterventionPromptBuilder.buildUserPrompt("articulation", childData());
        String withEmptyRag = ModuleInterventionPromptBuilder.buildUserPromptWithRag("articulation", childData(), "");

        assertEquals(withoutRag, withEmptyRag);
    }

    @Test
    public void buildUserPrompt_withPrelinguistic_shouldMentionMergedSceneLogic() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("prelinguistic", prelinguisticChildData());

        assertTrue(prompt.contains("\"moduleType\":\"prelinguistic\""));
        assertTrue(prompt.contains("PL_A"));
        assertTrue(prompt.contains("PL_B"));
        assertTrue(prompt.contains("output only one merged report"));
        assertTrue(prompt.contains("scene-dependent / context-dependent performance"));
    }

    @Test
    public void buildUserPrompt_withPrelinguisticRag_shouldIncludeRagContext() throws Exception {
        String ragContext = "【检索参考知识】\n[1] Joint attention routines should stay brief and caregiver-led.";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "prelinguistic",
                prelinguisticChildData(),
                ragContext
        );

        assertTrue(prompt.contains(ragContext));
        assertTrue(prompt.contains("The child's merged summary is the primary evidence"));
    }

    @Test
    public void buildUserPrompt_withPrelinguistic_shouldNotSplitSceneReports() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("prelinguistic", prelinguisticChildData());

        assertTrue(prompt.contains("Never split the output into scene A / scene B reports"));
        assertTrue(prompt.contains("output only one merged report"));
        assertTrue(prompt.contains("Never split the output into scene A / scene B reports"));
    }

    @Test
    public void buildUserPrompt_withPrelinguistic_shouldKeepSchemaConstraint() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("prelinguistic", prelinguisticChildData());

        assertTrue(prompt.contains("schema"));
        assertTrue(prompt.contains("\"smartGoal\""));
        assertTrue(prompt.contains("\"homeGuidance\""));
    }

    private JSONObject childData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2020-01-01")
                .put("testDate", "2024-01-01")
                .put("chiefComplaint", "Articulation is unclear.");
        JSONObject evaluations = new JSONObject();
        evaluations.put("A", new org.json.JSONArray());

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }

    private JSONObject prelinguisticChildData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2021-01-01")
                .put("testDate", "2024-01-01")
                .put("chiefComplaint", "互动主动性不足。");

        JSONObject plA = new JSONObject()
                .put("name", "共同注意")
                .put("score", 1)
                .put("total", 2)
                .put("scene", "A");
        JSONObject plB = new JSONObject()
                .put("name", "模仿")
                .put("score", 0)
                .put("total", 2)
                .put("scene", "B");

        JSONObject evaluations = new JSONObject()
                .put("PL", new org.json.JSONArray())
                .put("PL_A", new org.json.JSONArray().put(plA))
                .put("PL_B", new org.json.JSONArray().put(plB));

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }
}
