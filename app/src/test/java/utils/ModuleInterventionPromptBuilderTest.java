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
        assertTrue(prompt.contains("输出 schema"));
    }

    @Test
    public void buildUserPromptWithRag_shouldIncludeRagContext() throws Exception {
        String ragContext = "【检索参考知识】\n[1] 构音替代错误干预原则：先稳定词首。";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag("articulation", childData(), ragContext);

        assertTrue(prompt.contains(ragContext));
    }

    @Test
    public void buildUserPromptWithRag_shouldKeepSchemaConstraint() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "articulation",
                childData(),
                "【检索参考知识】\n[1] 示例：参考。"
        );

        assertTrue(prompt.contains("输出 schema"));
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
                .put("chiefComplaint", "构音不清");
        JSONObject evaluations = new JSONObject();
        evaluations.put("A", new org.json.JSONArray());

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }
}
