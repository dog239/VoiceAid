package utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleInterventionPromptBuilderTest {

    @Test
    public void buildUserPrompt_shouldWorkWithoutRag() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("articulation", articulationChildData());

        assertTrue(prompt.contains("\"moduleType\":\"articulation\""));
        assertTrue(prompt.contains("schema"));
    }

    @Test
    public void buildUserPromptWithRag_shouldIncludeRagContext() throws Exception {
        String ragContext = "【检索参考知识】\n[1] Substitution principle: stabilize initial-position targets.";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "articulation",
                articulationChildData(),
                ragContext
        );

        assertTrue(prompt.contains(ragContext));
    }

    @Test
    public void buildUserPromptWithRag_shouldKeepSchemaConstraint() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "articulation",
                articulationChildData(),
                "【检索参考知识】\n[1] Example reference."
        );

        assertTrue(prompt.contains("schema"));
        assertTrue(prompt.contains("\"smartGoal\""));
    }

    @Test
    public void buildUserPromptWithRag_shouldFallbackWhenRagEmpty() throws Exception {
        String withoutRag = ModuleInterventionPromptBuilder.buildUserPrompt("articulation", articulationChildData());
        String withEmptyRag = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "articulation",
                articulationChildData(),
                ""
        );

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
    }

    @Test
    public void buildUserPrompt_withPrelinguistic_shouldKeepSchemaConstraint() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("prelinguistic", prelinguisticChildData());

        assertTrue(prompt.contains("schema"));
        assertTrue(prompt.contains("\"smartGoal\""));
        assertTrue(prompt.contains("\"homeGuidance\""));
    }

    @Test
    public void buildUserPrompt_withVocabulary_shouldMentionMergedVocabularyLogic() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("vocabulary", vocabularyChildData());

        assertTrue(prompt.contains("\"moduleType\":\"vocabulary\""));
        assertTrue(prompt.contains("output only one merged report"));
        assertTrue(prompt.contains("combined evidence inside the same vocabulary module"));
    }

    @Test
    public void buildUserPrompt_withVocabularyRag_shouldIncludeRagContext() throws Exception {
        String ragContext = "【检索参考知识】\n[1] Receptive vocabulary may outpace expressive naming in early stages.";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "vocabulary",
                vocabularyChildData(),
                ragContext
        );

        assertTrue(prompt.contains(ragContext));
        assertTrue(prompt.contains("The case summary is the primary evidence"));
    }

    @Test
    public void buildUserPrompt_withVocabulary_shouldNotSplitReceptiveExpressiveReports() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("vocabulary", vocabularyChildData());

        assertTrue(prompt.contains("Never split the output into a receptive report and an expressive report"));
        assertTrue(prompt.contains("one unified vocabulary intervention report"));
    }

    @Test
    public void buildUserPrompt_withVocabulary_shouldMentionReSnwrAsSupportingSignals() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("vocabulary", vocabularyChildData());

        assertTrue(prompt.contains("RE, S, and NWR"));
        assertTrue(prompt.contains("supporting signals"));
        assertTrue(prompt.contains("must not become the main training line by themselves"));
    }

    @Test
    public void buildUserPrompt_withVocabulary_shouldKeepSchemaConstraint() throws Exception {
        String prompt = ModuleInterventionPromptBuilder.buildUserPrompt("vocabulary", vocabularyChildData());

        assertTrue(prompt.contains("schema"));
        assertTrue(prompt.contains("\"smartGoal\""));
        assertTrue(prompt.contains("\"homeGuidance\""));
    }

    @Test
    public void buildUserPromptWithRag_shouldKeepSyntaxUnifiedReport() throws Exception {
        String ragContext = "【检索参考知识】\n【句法理解相关建议】\n[1] 理解策略：支持复杂句理解\n\n【句法表达相关建议】\n[2] 表达策略：支持句式扩展";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "syntax",
                syntaxChildData(),
                ragContext
        );

        assertTrue(prompt.contains(ragContext));
        assertTrue(prompt.contains("one combined syntax assessment module report"));
    }

    @Test
    public void buildUserPromptWithRag_shouldKeepSocialUnifiedReport() throws Exception {
        String ragContext = "【检索参考知识】\n【社交互动相关建议】\n[1] Response support：Build turn-taking in adult-led play.\n\n【情境与泛化相关建议】\n[2] Generalization：Expand peer interaction across daily routines.";
        String prompt = ModuleInterventionPromptBuilder.buildUserPromptWithRag(
                "social",
                socialChildData(),
                ragContext
        );

        assertTrue(prompt.contains(ragContext));
        assertTrue(prompt.contains("\"moduleType\":\"social\""));
    }

    private JSONObject articulationChildData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2020-01-01")
                .put("testDate", "2024-01-01")
                .put("chiefComplaint", "Articulation is unclear.");
        JSONObject evaluations = new JSONObject();
        evaluations.put("A", new JSONArray());

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }

    private JSONObject prelinguisticChildData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2021-01-01")
                .put("testDate", "2024-01-01")
                .put("chiefComplaint", "Low interaction initiative.");

        JSONObject plA = new JSONObject()
                .put("name", "joint_attention")
                .put("score", 1)
                .put("total", 2)
                .put("scene", "A");
        JSONObject plB = new JSONObject()
                .put("name", "imitation")
                .put("score", 0)
                .put("total", 2)
                .put("scene", "B");

        JSONObject evaluations = new JSONObject()
                .put("PL", new JSONArray())
                .put("PL_A", new JSONArray().put(plA))
                .put("PL_B", new JSONArray().put(plB));

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }

    private JSONObject vocabularyChildData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2020-06-01")
                .put("testDate", "2024-06-01")
                .put("chiefComplaint", "Vocabulary understanding and expression are uneven.");

        JSONObject expressiveItem = new JSONObject()
                .put("word", "apple")
                .put("result", false)
                .put("type", "noun");
        JSONObject receptiveItem = new JSONObject()
                .put("word", "banana")
                .put("result", true)
                .put("type", "noun");
        JSONObject reItem = new JSONObject()
                .put("score", 1)
                .put("label", "repetition");
        JSONObject sItem = new JSONObject()
                .put("result", false)
                .put("label", "sentence_load");
        JSONObject nwrItem = new JSONObject()
                .put("score", 0)
                .put("label", "nonword_repeat");

        JSONObject evaluations = new JSONObject()
                .put("E", new JSONArray().put(expressiveItem))
                .put("EV", new JSONArray().put(receptiveItem))
                .put("RE", new JSONArray().put(reItem))
                .put("S", new JSONArray().put(sItem))
                .put("NWR", new JSONArray().put(nwrItem));

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }

    private JSONObject syntaxChildData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2020-06-01")
                .put("testDate", "2024-06-01")
                .put("chiefComplaint", "Syntax comprehension and expression both need support.");

        JSONObject rgItem = new JSONObject()
                .put("question", "理解复杂句")
                .put("result", false);
        JSONObject seItem = new JSONObject()
                .put("question", "句式表达")
                .put("result", false);

        JSONObject evaluations = new JSONObject()
                .put("RG", new JSONArray().put(rgItem))
                .put("SE", new JSONArray().put(seItem));

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }

    private JSONObject socialChildData() throws Exception {
        JSONObject info = new JSONObject()
                .put("birthDate", "2020-06-01")
                .put("testDate", "2024-06-01")
                .put("chiefComplaint", "Social response is inconsistent.");

        JSONObject social1 = new JSONObject()
                .put("num", 12)
                .put("score", 0)
                .put("ability", "response to peer")
                .put("focus", "peer interaction");
        JSONObject social2 = new JSONObject()
                .put("num", 24)
                .put("score", 1)
                .put("ability", "turn taking")
                .put("focus", "daily interaction");

        JSONObject evaluations = new JSONObject()
                .put("SOCIAL1", new JSONArray().put(social1))
                .put("SOCIAL2", new JSONArray().put(social2));

        return new JSONObject()
                .put("info", info)
                .put("evaluations", evaluations);
    }
}
