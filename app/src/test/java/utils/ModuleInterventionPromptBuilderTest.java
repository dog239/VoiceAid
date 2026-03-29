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
}
