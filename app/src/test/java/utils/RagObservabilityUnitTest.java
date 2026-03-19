package utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kb.KbMeta;
import kb.KbMetaLoader;
import kb.KbRepository;
import kb.KbStrategy;
import utils.rag.RagKbHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RagObservabilityUnitTest {

    @Test
    public void ragBuild_withEvaluationsOnly_populatesObservabilityMetaAndIds() {
        ReportPipeline pipeline = new ReportPipeline(null);
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("evaluations", buildEvaluations());
        Map<String, Object> ensured = pipeline.ensureKbQueries(input);

        RagKbHelper.RagResult result = RagKbHelper.build(
                ensured,
                fakeRepo(),
                3,
                10,
                "assets/kb/strategies.jsonl",
                "B1_module+tag"
        );
        KbMeta expectedMeta = KbMetaLoader.loadFromMainAssets();

        assertTrue(((Number) result.meta.get("query_count")).intValue() > 0);
        assertTrue(((Number) result.meta.get("hit_count")).intValue() >= 1);
        assertTrue(((Number) result.meta.get("latency_ms")).longValue() >= 0L);
        assertEquals(expectedMeta.kbVersion, String.valueOf(result.meta.get("kb_version")));
        assertEquals(expectedMeta.buildTime, String.valueOf(result.meta.get("build_time")));
        assertTrue(String.valueOf(result.meta.get("kb_version")).trim().length() > 0);
        assertTrue(String.valueOf(result.meta.get("build_time")).trim().length() > 0);
        assertTrue(((Number) result.meta.get("hit_count")).intValue() == result.ragKbIds.size());
        assertFalse(result.ragKbIds.isEmpty());
        for (String id : result.ragKbIds) {
            assertTrue(id != null && !id.trim().isEmpty());
        }
    }

    @Test
    public void attachRagFields_releaseMode_keepsMetaAndIds_andOmitsRagKb() throws Exception {
        TestableReportPipeline pipeline = new TestableReportPipeline(false);
        Map<String, Object> plan = new LinkedHashMap<>();
        RagKbHelper.RagResult ragResult = buildSampleRagResult();

        pipeline.attachRagFields(plan, ragResult);

        assertTrue(plan.containsKey("rag_kb_meta"));
        assertTrue(plan.containsKey("rag_kb_ids"));
        assertTrue(plan.get("rag_kb_ids") instanceof List);
        assertTrue(((List<?>) plan.get("rag_kb_ids")).size() == ragResult.ragKbIds.size());
        assertFalse(plan.containsKey("rag_kb"));
        if (plan.containsKey("rag_context")) {
            String value = String.valueOf(plan.get("rag_context"));
            assertTrue(value.length() <= pipeline.releaseRagContextMaxChars());
        }
    }

    private Map<String, Object> buildEvaluations() {
        Map<String, Object> eval = new LinkedHashMap<>();

        List<Object> a = new ArrayList<>();
        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("errorType", "substitution");
        a.add(a1);
        eval.put("A", a);

        List<Object> e = new ArrayList<>();
        Map<String, Object> e1 = new LinkedHashMap<>();
        e1.put("result", false);
        e.add(e1);
        eval.put("E", e);
        return eval;
    }

    private KbRepository fakeRepo() {
        return new KbRepository() {
            @Override
            public List<KbStrategy> query(String module, String tag, int topK) {
                if ("a".equalsIgnoreCase(module) && "error_type".equalsIgnoreCase(tag)) {
                    return Arrays.asList(
                            new KbStrategy("A-001", "A", Collections.singletonList("error_type"),
                                    "t1", "basic", "tx1", "dn1")
                    );
                }
                if ("e".equalsIgnoreCase(module) && "training_needed".equalsIgnoreCase(tag)) {
                    return Arrays.asList(
                            new KbStrategy("E-001", "E", Collections.singletonList("training_needed"),
                                    "t2", "basic", "tx2", "dn2")
                    );
                }
                return Collections.emptyList();
            }
        };
    }

    private RagKbHelper.RagResult buildSampleRagResult() {
        List<RagKbHelper.QueryKey> keys = Collections.singletonList(
                new RagKbHelper.QueryKey("a", Collections.singletonList("error_type")));
        List<Map<String, String>> items = new ArrayList<>();
        Map<String, String> item = new LinkedHashMap<>();
        item.put("id", "A-001");
        item.put("module", "A");
        item.put("skill_tag", "error_type");
        item.put("title", "t1");
        item.put("level", "basic");
        item.put("text", "tx1");
        item.put("do_not", "dn1");
        items.add(item);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("source", "assets/kb/strategies.jsonl");
        meta.put("retrieval", "B1_module+tag");
        meta.put("query_count", 1);
        meta.put("hit_count", 1);
        meta.put("topK_per_tag", 3);
        meta.put("max_context_items", 10);
        meta.put("latency_ms", 1L);
        meta.put("kb_version", "assets_main");
        meta.put("build_time", "1");
        return new RagKbHelper.RagResult(keys, items, "sample_context", meta, Collections.singletonList("A-001"));
    }

    private static final class TestableReportPipeline extends ReportPipeline {
        private final boolean expose;

        TestableReportPipeline(boolean expose) {
            super(null);
            this.expose = expose;
        }

        @Override
        boolean shouldExposeRagDetails() {
            return expose;
        }
    }
}
