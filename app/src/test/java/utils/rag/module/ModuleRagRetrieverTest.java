package utils.rag.module;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleRagRetrieverTest {
    private final ModuleRagRetriever retriever = new ModuleRagRetriever();

    @Test
    public void retrieve_shouldMatchDocsByProblemTags() {
        List<RagHit> hits = retriever.retrieve(articulationQuery(), articulationDocs(), 5);

        assertEquals(2, hits.size());
        assertEquals("ART-001", hits.get(0).doc.id);
    }

    @Test
    public void retrieve_shouldSortHitsByScore() {
        List<RagHit> hits = retriever.retrieve(articulationQuery(), articulationDocs(), 5);

        assertTrue(hits.get(0).score >= hits.get(1).score);
    }

    @Test
    public void retrieve_shouldReturnEmpty_whenNoDocs() {
        List<RagHit> hits = retriever.retrieve(articulationQuery(), Collections.<KnowledgeDoc>emptyList(), 5);

        assertTrue(hits.isEmpty());
    }

    @Test
    public void retrieve_shouldRespectTopK() {
        List<RagHit> hits = retriever.retrieve(articulationQuery(), articulationDocs(), 1);

        assertEquals(1, hits.size());
        assertEquals("ART-001", hits.get(0).doc.id);
    }

    @Test
    public void retrieveSyntax_shouldSearchBySubModuleIndependently() {
        List<RagHit> hits = retriever.retrieve(syntaxQuery(), syntaxDocs(), 5);

        assertTrue(containsHit(hits, "SYN-C-001", "comprehension"));
        assertTrue(containsHit(hits, "SYN-E-001", "expression"));
    }

    @Test
    public void retrieveSyntax_shouldMergeComprehensionAndExpressionHits() {
        List<RagHit> hits = retriever.retrieve(syntaxQuery(), syntaxDocs(), 5);

        assertEquals(3, hits.size());
    }

    @Test
    public void retrieveSyntax_shouldRespectTopK() {
        List<RagHit> hits = retriever.retrieve(syntaxQuery(), syntaxDocs(), 2);

        assertEquals(2, hits.size());
    }

    @Test
    public void retrieveSyntax_shouldHandleDocsWithoutSubModuleField() {
        List<RagHit> hits = retriever.retrieve(syntaxQuery(), syntaxDocs(), 5);

        assertTrue(containsHit(hits, "SYN-G-001", "global"));
    }

    @Test
    public void retrieveVocabulary_shouldSearchBySubModuleIndependently() {
        List<RagHit> hits = retriever.retrieve(vocabularyQuery(), vocabularyDocs(), 6);

        assertTrue(containsHit(hits, "VOC-R-001", "receptive"));
        assertTrue(containsHit(hits, "VOC-E-001", "expressive"));
    }

    @Test
    public void retrieveVocabulary_shouldMergeReceptiveAndExpressiveHits() {
        List<RagHit> hits = retriever.retrieve(vocabularyQuery(), vocabularyDocs(), 6);

        assertTrue(containsHit(hits, "VOC-R-001", "receptive"));
        assertTrue(containsHit(hits, "VOC-E-001", "expressive"));
        assertTrue(containsHit(hits, "VOC-G-001", "global"));
    }

    @Test
    public void retrieveVocabulary_shouldUseSupportingSignalsAsAuxiliaryWeights() {
        List<RagHit> hits = retriever.retrieve(vocabularyQuery(), vocabularyDocs(), 6);

        double receptiveScore = findScore(hits, "VOC-R-001", "receptive");
        double supportScore = findScore(hits, "VOC-G-001", "global");
        assertTrue(receptiveScore > supportScore);
        assertTrue(supportScore > 0.0d);
    }

    @Test
    public void retrieveVocabulary_shouldRespectTopK() {
        List<RagHit> hits = retriever.retrieve(vocabularyQuery(), vocabularyDocs(), 2);

        assertEquals(2, hits.size());
    }

    @Test
    public void retrieveVocabulary_shouldHandleDocsWithoutSubModuleField() {
        List<RagHit> hits = retriever.retrieve(vocabularyQuery(), vocabularyDocs(), 6);

        assertTrue(containsHit(hits, "VOC-G-001", "global"));
    }

    @Test
    public void retrieveSocial_shouldSearchByProblemTagsAndInteractionGoals() {
        List<RagHit> hits = retriever.retrieve(socialQuery(), socialDocs(), 5);

        assertTrue(containsHit(hits, "SOC-001", "social"));
        assertTrue(containsHit(hits, "SOC-002", "social"));
    }

    @Test
    public void retrieveSocial_shouldUseScenarioTagsAsAuxiliaryWeights() {
        List<RagHit> hits = retriever.retrieve(socialQuery(), socialDocs(), 5);

        double interactionScore = findScore(hits, "SOC-001", "social");
        double scenarioScore = findScore(hits, "SOC-002", "social");
        assertTrue(interactionScore >= scenarioScore);
        assertTrue(scenarioScore > 0.0d);
    }

    @Test
    public void retrieveSocial_shouldRespectTopK() {
        List<RagHit> hits = retriever.retrieve(socialQuery(), socialDocs(), 2);

        assertEquals(2, hits.size());
    }

    @Test
    public void retrieveSocial_shouldHandleDocsWithoutScenarioFields() {
        List<RagHit> hits = retriever.retrieve(socialQuery(), socialDocs(), 5);

        assertTrue(containsHit(hits, "SOC-003", "social"));
    }

    private RagQuery articulationQuery() {
        return new RagQuery(
                "articulation",
                Arrays.asList("substitution", "omission"),
                Arrays.asList("k", "s"),
                Collections.singletonList("initial"),
                Collections.singletonList("home_practice"),
                "moderate"
        );
    }

    private List<KnowledgeDoc> articulationDocs() {
        return Arrays.asList(
                new KnowledgeDoc(
                        "ART-001",
                        "articulation",
                        "Substitution strategy",
                        "Stabilize the target sound in initial position first.",
                        "intervention_principle",
                        Collections.singletonList("substitution"),
                        Arrays.asList("k", "g"),
                        Collections.singletonList("initial"),
                        Arrays.asList("stabilization", "home_practice"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("therapist"),
                        10,
                        "test"
                ),
                new KnowledgeDoc(
                        "ART-002",
                        "articulation",
                        "Home practice",
                        "Use short high-frequency home practice.",
                        "home_guidance",
                        Collections.singletonList("omission"),
                        Collections.singletonList("s"),
                        Collections.singletonList("initial"),
                        Collections.singletonList("home_practice"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("family"),
                        5,
                        "test"
                )
        );
    }

    private RagQuery syntaxQuery() {
        return new RagQuery(
                "syntax",
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Collections.singletonList("complex_structure"),
                "needs_support",
                new RagQuery.GlobalQuery(Collections.singletonList("complex_structure"), "needs_support"),
                Arrays.asList(
                        new RagQuery.SubModuleQuery(
                                "comprehension",
                                Arrays.asList("complex_sentence_comprehension", "structure_relation_comprehension"),
                                Collections.singletonList("syntax_comprehension_support"),
                                "unstable"
                        ),
                        new RagQuery.SubModuleQuery(
                                "expression",
                                Arrays.asList("limited_sentence_variety", "omission_in_expression"),
                                Collections.singletonList("syntax_expression_support"),
                                "unstable"
                        )
                )
        );
    }

    private List<KnowledgeDoc> syntaxDocs() {
        return Arrays.asList(
                new KnowledgeDoc(
                        "SYN-C-001",
                        "syntax",
                        "comprehension",
                        "Complex sentence comprehension",
                        "Support understanding of complex sentences and structure relations.",
                        "training_strategy",
                        Arrays.asList("complex_sentence_comprehension", "structure_relation_comprehension"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("syntax_comprehension_support"),
                        Collections.singletonList("sentence_level"),
                        Collections.singletonList("therapist"),
                        10,
                        "test"
                ),
                new KnowledgeDoc(
                        "SYN-E-001",
                        "syntax",
                        "expression",
                        "Sentence expansion",
                        "Expand sentence variety and reduce omission in expression.",
                        "training_strategy",
                        Arrays.asList("limited_sentence_variety", "omission_in_expression"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("syntax_expression_support"),
                        Collections.singletonList("sentence_level"),
                        Collections.singletonList("therapist"),
                        9,
                        "test"
                ),
                new KnowledgeDoc(
                        "SYN-G-001",
                        "syntax",
                        "",
                        "Syntax generalization",
                        "Use complex structures in both comprehension and expression routines.",
                        "general_strategy",
                        Collections.singletonList("syntax_integration"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("complex_structure"),
                        Collections.singletonList("sentence_level"),
                        Collections.singletonList("family"),
                        6,
                        "test"
                )
        );
    }

    private RagQuery vocabularyQuery() {
        return new RagQuery(
                "vocabulary",
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Arrays.asList("noun", "balance_support"),
                "receptive_stronger",
                Arrays.asList("RE", "S", "NWR"),
                new RagQuery.GlobalQuery(Arrays.asList("noun", "balance_support"), "receptive_stronger"),
                Arrays.asList(
                        new RagQuery.SubModuleQuery(
                                "receptive",
                                Arrays.asList("semantic_comprehension_difficulty", "category_comprehension_difficulty"),
                                Collections.singletonList("receptive_vocabulary_support"),
                                "50%"
                        ),
                        new RagQuery.SubModuleQuery(
                                "expressive",
                                Arrays.asList("naming_difficulty", "slow_lexical_retrieval"),
                                Collections.singletonList("expressive_vocabulary_support"),
                                "30%"
                        )
                )
        );
    }

    private List<KnowledgeDoc> vocabularyDocs() {
        return Arrays.asList(
                new KnowledgeDoc(
                        "VOC-R-001",
                        "vocabulary",
                        "receptive",
                        "Semantic comprehension support",
                        "Support semantic comprehension and category understanding when cues fade.",
                        "training_strategy",
                        Arrays.asList("semantic_comprehension_difficulty", "category_comprehension_difficulty"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("receptive_vocabulary_support"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("therapist"),
                        9,
                        "test"
                ),
                new KnowledgeDoc(
                        "VOC-E-001",
                        "vocabulary",
                        "expressive",
                        "Naming retrieval practice",
                        "Support naming difficulty and lexical retrieval under higher task load.",
                        "training_strategy",
                        Arrays.asList("naming_difficulty", "slow_lexical_retrieval"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("expressive_vocabulary_support"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("therapist"),
                        8,
                        "test"
                ),
                new KnowledgeDoc(
                        "VOC-G-001",
                        "vocabulary",
                        "",
                        "Vocabulary load management",
                        "Use repetition, phonological memory supports, and lighter task load to keep vocabulary output stable.",
                        "general_strategy",
                        Collections.singletonList("vocabulary_integration"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Arrays.asList("balance_support", "noun"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("family"),
                        3,
                        "test"
                )
        );
    }

    private RagQuery socialQuery() {
        return new RagQuery(
                "social",
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Arrays.asList("improve_response", "increase_turn_taking"),
                "mixed_profile",
                Arrays.asList("unstable_profile", "cross_scenario_sampling"),
                Arrays.asList("weak_response", "poor_turn_taking", "weak_generalization"),
                Arrays.asList("daily_interaction", "peer_interaction"),
                Arrays.asList("improve_response", "increase_turn_taking", "improve_social_generalization"),
                new RagQuery.GlobalQuery(Arrays.asList("improve_response", "increase_turn_taking"), "mixed_profile"),
                Collections.<RagQuery.SubModuleQuery>emptyList()
        );
    }

    private List<KnowledgeDoc> socialDocs() {
        return Arrays.asList(
                new KnowledgeDoc(
                        "SOC-001",
                        "social",
                        "",
                        "Response and turn-taking support",
                        "Support weak response and turn-taking with stepwise adult support in interaction.",
                        "training_strategy",
                        Arrays.asList("weak_response", "poor_turn_taking"),
                        Collections.<String>emptyList(),
                        Arrays.asList("improve_response", "increase_turn_taking"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Arrays.asList("improve_response", "increase_turn_taking"),
                        Collections.singletonList("interaction"),
                        Collections.singletonList("therapist"),
                        9,
                        "test"
                ),
                new KnowledgeDoc(
                        "SOC-002",
                        "social",
                        "",
                        "Peer routine generalization",
                        "Generalization in peer interaction and daily routines needs repeated practice in group games.",
                        "generalization_strategy",
                        Collections.singletonList("weak_generalization"),
                        Arrays.asList("daily_interaction", "peer_interaction"),
                        Collections.singletonList("improve_social_generalization"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("social_generalization"),
                        Collections.singletonList("interaction"),
                        Collections.singletonList("therapist"),
                        5,
                        "test"
                ),
                new KnowledgeDoc(
                        "SOC-003",
                        "social",
                        "",
                        "Family coaching",
                        "Coach caregivers to use short daily routines and supported peer practice at home.",
                        "home_guidance",
                        Collections.singletonList("prompt_dependent_interaction"),
                        Collections.<String>emptyList(),
                        Collections.singletonList("improve_social_generalization"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("core_social_support"),
                        Collections.singletonList("daily"),
                        Collections.singletonList("family"),
                        4,
                        "test"
                )
        );
    }

    private boolean containsHit(List<RagHit> hits, String docId, String subModuleName) {
        for (RagHit hit : hits) {
            if (hit != null
                    && hit.doc != null
                    && docId.equals(hit.doc.id)
                    && subModuleName.equals(hit.subModuleName)) {
                return true;
            }
        }
        return false;
    }

    private double findScore(List<RagHit> hits, String docId, String subModuleName) {
        for (RagHit hit : hits) {
            if (hit != null
                    && hit.doc != null
                    && docId.equals(hit.doc.id)
                    && subModuleName.equals(hit.subModuleName)) {
                return hit.score;
            }
        }
        return 0.0d;
    }
}
