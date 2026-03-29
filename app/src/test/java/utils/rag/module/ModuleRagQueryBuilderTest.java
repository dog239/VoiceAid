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

    @Test
    public void buildVocabularyQuery_shouldSplitReceptiveAndExpressiveSignals() throws Exception {
        RagQuery query = ModuleRagQueryBuilder.build("vocabulary", vocabularyModuleInput());

        assertNotNull(query);
        assertEquals(2, query.subModules.size());
        assertEquals("receptive", query.subModules.get(0).name);
        assertEquals("expressive", query.subModules.get(1).name);
        assertTrue(query.subModules.get(0).problemTags.contains("semantic_comprehension_difficulty"));
        assertTrue(query.subModules.get(1).problemTags.contains("naming_difficulty"));
    }

    @Test
    public void buildVocabularyQuery_shouldIncludeSupportingSignals() throws Exception {
        RagQuery query = ModuleRagQueryBuilder.build("vocabulary", vocabularyModuleInput());

        assertTrue(query.supportingSignals.contains("RE"));
        assertTrue(query.supportingSignals.contains("S"));
        assertTrue(query.supportingSignals.contains("NWR"));
    }

    @Test
    public void buildVocabularyQuery_shouldHandleMergedSummaryWithoutCrash() throws Exception {
        JSONObject moduleInput = new JSONObject()
                .put("moduleType", "vocabulary")
                .put("moduleEvaluations", new JSONObject().put("summary", new JSONObject()));

        RagQuery query = ModuleRagQueryBuilder.build("vocabulary", moduleInput);

        assertNotNull(query);
        assertNotNull(query.subModules);
        assertEquals(2, query.subModules.size());
        assertNotNull(query.supportingSignals);
    }

    @Test
    public void buildVocabularyQuery_shouldNotLeakPrivacyFields() throws Exception {
        JSONObject input = vocabularyModuleInput();
        input.put("chiefComplaint", "contact 13800138000, address road 1");

        RagQuery query = ModuleRagQueryBuilder.build("vocabulary", input);
        String queryJson = query.toJson().toString();

        assertFalse(queryJson.contains("13800138000"));
        assertFalse(queryJson.contains("address road 1"));
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
                .put("overallLevel", "unstable comprehension")
                .put("focusStructures", new JSONArray().put("complex sentence comprehension").put("structure relation comprehension"))
                .put("unstableStructures", new JSONArray().put("comprehension accuracy unstable"));
        JSONObject se = new JSONObject()
                .put("overallLevel", "expression output unstable")
                .put("focusStructures", new JSONArray().put("sentence variety limited").put("expression omission"))
                .put("unstableStructures", new JSONArray().put("sentence organization weak"));
        JSONObject assessment = new JSONObject()
                .put("overallLevel", "syntax support needed")
                .put("sharedPriorityStructures", new JSONArray().put("cause effect"))
                .put("sharedUnstableStructures", new JSONArray().put("coordination"));
        JSONObject summary = new JSONObject()
                .put("rgComprehension", rg)
                .put("seExpression", se)
                .put("syntaxAssessment", assessment);

        return new JSONObject()
                .put("moduleType", "syntax")
                .put("moduleEvaluations", new JSONObject().put("summary", summary));
    }

    private JSONObject vocabularyModuleInput() throws Exception {
        JSONObject receptiveCategories = new JSONObject()
                .put("noun", new JSONObject().put("status", "focus"))
                .put("verb", new JSONObject().put("status", "unstable"));
        JSONObject expressiveCategories = new JSONObject()
                .put("noun", new JSONObject().put("status", "unstable"))
                .put("verb", new JSONObject().put("status", "focus"));

        JSONObject vocabularyAssessment = new JSONObject()
                .put("overallLevel", "receptive_stronger")
                .put("relativeProfile", "understanding stronger than output")
                .put("focus", new JSONArray()
                        .put("semantic comprehension weak")
                        .put("classification understanding weak")
                        .put("naming difficulty")
                        .put("lexical retrieval slow")
                        .put("expressive output unstable"))
                .put("unstable", new JSONArray()
                        .put("input side weak when cues fade")
                        .put("active expression unstable"))
                .put("crossDomainPriorityCategories", new JSONArray().put("noun"))
                .put("crossDomainUnstableCategories", new JSONArray().put("verb"))
                .put("receptive", new JSONObject()
                        .put("accuracy", "50%")
                        .put("categories", receptiveCategories))
                .put("expressive", new JSONObject()
                        .put("accuracy", "30%")
                        .put("categories", expressiveCategories));

        JSONObject summary = new JSONObject()
                .put("vocabularyAssessment", vocabularyAssessment)
                .put("RE", new JSONObject().put("total", 6).put("accuracy", "40%"))
                .put("S", new JSONObject().put("total", 4).put("accuracy", "50%"))
                .put("NWR", new JSONObject().put("total", 5).put("accuracy", "20%"));

        return new JSONObject()
                .put("moduleType", "vocabulary")
                .put("moduleEvaluations", new JSONObject().put("summary", summary));
    }
}
