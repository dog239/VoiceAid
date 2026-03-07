package utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kb.KbRepository;
import kb.KbStrategy;
import utils.rag.RagKbHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RagKbHelperUnitTest {

    @Test
    public void build_extractsAndRetrieves_whenKbQueriesProvided() {
        KbRepository fakeRepo = new KbRepository() {
            @Override
            public List<KbStrategy> query(String module, String tag, int topK) {
                if ("a".equalsIgnoreCase(module) && "error_type".equalsIgnoreCase(tag)) {
                    return Arrays.asList(
                            new KbStrategy("A-001", "A", Collections.singletonList("error_type"),
                                    "stabilize_then_speed", "B1", "Start with slow and clear articulation before increasing speed.", "Avoid continuous high-intensity correction"),
                            new KbStrategy("A-002", "A", Collections.singletonList("error_type"),
                                    "minimal_pairs", "B1", "Use minimal-pair drills around target phonemes.", "Do not rely on mechanical repetition")
                    );
                }
                return Collections.emptyList();
            }
        };

        Map<String, Object> root = new LinkedHashMap<>();
        List<Object> kbQueries = new ArrayList<>();
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("module", "A");
        query.put("tag", "error_type");
        kbQueries.add(query);
        root.put("kb_queries", kbQueries);

        RagKbHelper.RagResult result = RagKbHelper.build(
                root,
                fakeRepo,
                3,
                10,
                "assets/kb/strategies.jsonl",
                "B1_module+tag"
        );

        assertEquals(1, result.keys.size());
        assertEquals("a", result.keys.get(0).module);
        assertEquals(1, result.keys.get(0).tags.size());
        assertEquals("error_type", result.keys.get(0).tags.get(0));

        assertEquals(2, result.ragKbItems.size());
        assertTrue(result.ragContext.contains("A-001"));
        assertEquals("B1_module+tag", result.meta.get("retrieval"));
        assertEquals(2, ((Number) result.meta.get("hit_count")).intValue());
        assertEquals(3, ((Number) result.meta.get("topK_per_tag")).intValue());
        assertEquals(10, ((Number) result.meta.get("max_context_items")).intValue());
        assertTrue(((Number) result.meta.get("latency_ms")).longValue() >= 0L);
        assertTrue(String.valueOf(result.meta.get("kb_version")).trim().length() > 0);
        assertTrue(String.valueOf(result.meta.get("build_time")).trim().length() > 0);
        assertEquals(2, result.ragKbIds.size());
        assertEquals("A-001", result.ragKbIds.get(0));
    }

    @Test
    public void build_keepsUsableOutput_whenKbQueriesEmpty() {
        KbRepository fakeRepo = new KbRepository() {
            @Override
            public List<KbStrategy> query(String module, String tag, int topK) {
                return Collections.emptyList();
            }
        };

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("kb_queries", Collections.emptyList());

        RagKbHelper.RagResult result = RagKbHelper.build(
                root,
                fakeRepo,
                3,
                10,
                "assets/kb/strategies.jsonl",
                "B1_module+tag"
        );

        assertTrue(result.keys.isEmpty());
        assertTrue(result.ragKbItems.isEmpty());
        assertTrue(result.ragKbIds.isEmpty());
        assertEquals("", result.ragContext);
        assertEquals("B1_module+tag", result.meta.get("retrieval"));
        assertEquals(0, ((Number) result.meta.get("query_count")).intValue());
        assertEquals(0, ((Number) result.meta.get("hit_count")).intValue());
        assertTrue(((Number) result.meta.get("latency_ms")).longValue() >= 0L);
        assertTrue(String.valueOf(result.meta.get("kb_version")).trim().length() > 0);
        assertTrue(String.valueOf(result.meta.get("build_time")).trim().length() > 0);
        assertFalse(result.meta.isEmpty());
    }

    @Test
    public void build_dedupesSameStrategyIdAcrossTags_beforeLimit() {
        KbRepository fakeRepo = new KbRepository() {
            @Override
            public List<KbStrategy> query(String module, String tag, int topK) {
                if ("a".equalsIgnoreCase(module)) {
                    List<KbStrategy> out = new ArrayList<>();
                    out.add(new KbStrategy("A-001", "A", Collections.singletonList("error_type"),
                            "same_id", "B1", "t", "dn"));
                    if ("phonology_process".equalsIgnoreCase(tag)) {
                        out.add(new KbStrategy("A-002", "A", Collections.singletonList("phonology_process"),
                                "other_id", "B1", "t2", "dn2"));
                    }
                    return out;
                }
                return Collections.emptyList();
            }
        };

        Map<String, Object> root = new LinkedHashMap<>();
        List<Object> kbQueries = new ArrayList<>();
        Map<String, Object> q1 = new LinkedHashMap<>();
        q1.put("module", "A");
        q1.put("tag", "error_type");
        Map<String, Object> q2 = new LinkedHashMap<>();
        q2.put("module", "A");
        q2.put("tag", "phonology_process");
        kbQueries.add(q1);
        kbQueries.add(q2);
        root.put("kb_queries", kbQueries);

        RagKbHelper.RagResult result = RagKbHelper.build(root, fakeRepo, 3, 10, "assets/kb/strategies.jsonl", "B1_module+tag");
        assertEquals(2, result.ragKbItems.size());
        assertEquals(2, result.ragKbIds.size());
        assertEquals("A-001", result.ragKbIds.get(0));
        assertEquals("A-002", result.ragKbIds.get(1));
        assertEquals(2, ((Number) result.meta.get("hit_count")).intValue());
    }

    @Test
    public void buildRagContext_noSecondaryTrim_whenItemsAlreadyLimited() {
        List<Map<String, String>> items = new ArrayList<>();
        Map<String, String> one = new LinkedHashMap<>();
        one.put("id", "A-001");
        one.put("module", "A");
        one.put("skill_tag", "error_type");
        one.put("title", "t1");
        one.put("text", "x1");
        one.put("do_not", "d1");
        Map<String, String> two = new LinkedHashMap<>();
        two.put("id", "A-002");
        two.put("module", "A");
        two.put("skill_tag", "phonology_process");
        two.put("title", "t2");
        two.put("text", "x2");
        two.put("do_not", "d2");
        items.add(one);
        items.add(two);

        String context = RagKbHelper.buildRagContext(items);
        assertTrue(context.contains("A-001"));
        assertTrue(context.contains("A-002"));
    }
}
