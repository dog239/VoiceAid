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
        List<RagHit> hits = retriever.retrieve(query(), docs(), 5);

        assertEquals(2, hits.size());
        assertEquals("ART-001", hits.get(0).doc.id);
    }

    @Test
    public void retrieve_shouldSortHitsByScore() {
        List<RagHit> hits = retriever.retrieve(query(), docs(), 5);

        assertTrue(hits.get(0).score >= hits.get(1).score);
    }

    @Test
    public void retrieve_shouldReturnEmpty_whenNoDocs() {
        List<RagHit> hits = retriever.retrieve(query(), Collections.<KnowledgeDoc>emptyList(), 5);

        assertTrue(hits.isEmpty());
    }

    @Test
    public void retrieve_shouldRespectTopK() {
        List<RagHit> hits = retriever.retrieve(query(), docs(), 1);

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

    private RagQuery query() {
        return new RagQuery(
                "articulation",
                Arrays.asList("substitution", "omission"),
                Arrays.asList("k", "s"),
                Collections.singletonList("initial"),
                Collections.singletonList("home_practice"),
                "moderate"
        );
    }

    private List<KnowledgeDoc> docs() {
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
                ),
                new KnowledgeDoc(
                        "VOC-001",
                        "vocabulary",
                        "Vocabulary guidance",
                        "Should not match.",
                        "training_strategy",
                        Collections.singletonList("substitution"),
                        Collections.singletonList("k"),
                        Collections.singletonList("initial"),
                        Collections.singletonList("home_practice"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("family"),
                        10,
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
}
