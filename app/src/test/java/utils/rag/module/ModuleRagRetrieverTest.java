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
                Arrays.asList("替代", "家庭训练"),
                Collections.singletonList("k"),
                Collections.singletonList("g"),
                "中度"
        );
    }

    private List<KnowledgeDoc> docs() {
        return Arrays.asList(
                new KnowledgeDoc("ART-001", "articulation", "替代原则", "先稳定目标音", Arrays.asList("替代", "k"), Collections.singletonList("k"), 10, "test"),
                new KnowledgeDoc("ART-002", "articulation", "家庭训练", "短时高频家庭练习", Collections.singletonList("家庭训练"), Collections.singletonList("home"), 5, "test"),
                new KnowledgeDoc("VOC-001", "vocabulary", "词汇", "不应命中", Collections.singletonList("替代"), Collections.singletonList("x"), 10, "test")
        );
    }
}
