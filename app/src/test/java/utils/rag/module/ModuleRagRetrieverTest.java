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
}
