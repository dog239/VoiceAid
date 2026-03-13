package utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import utils.rag.RagKbHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReportPipelineKbQueriesStabilityTest {

    @Test
    public void ensureKbQueries_autoInjectedPayload_canBeConsumedByExtractQueryKeys() {
        ReportPipeline pipeline = new ReportPipeline(null);
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("evaluations", buildStableEvaluations());

        Map<String, Object> ensured = pipeline.ensureKbQueries(input);
        List<RagKbHelper.QueryKey> keys = RagKbHelper.extractQueryKeys(ensured);

        assertTrue(ensured.get("kb_queries") instanceof List);
        assertFalse(((List<?>) ensured.get("kb_queries")).isEmpty());
        assertFalse(keys.isEmpty());

        boolean foundModuleAndTag = false;
        for (RagKbHelper.QueryKey key : keys) {
            if (key != null
                    && key.module != null
                    && !key.module.trim().isEmpty()
                    && key.tags != null
                    && !key.tags.isEmpty()) {
                foundModuleAndTag = true;
                break;
            }
        }
        assertTrue(foundModuleAndTag);
    }

    @Test
    public void ensureKbQueries_invalidExplicitShape_regeneratesAndReturnsUsableKeys() {
        ReportPipeline pipeline = new ReportPipeline(null);
        Map<String, Object> input = new LinkedHashMap<>();

        List<Object> badQueries = new ArrayList<>();
        Map<String, Object> badQuery = new LinkedHashMap<>();
        badQuery.put("query", "bad");
        badQueries.add(badQuery);
        input.put("kb_queries", badQueries);
        input.put("evaluations", buildStableEvaluations());

        Map<String, Object> ensured = pipeline.ensureKbQueries(input);
        List<RagKbHelper.QueryKey> keys = RagKbHelper.extractQueryKeys(ensured);

        assertTrue(ensured.get("kb_queries") instanceof List);
        List<?> regenerated = (List<?>) ensured.get("kb_queries");
        assertFalse(regenerated.isEmpty());
        assertTrue(regenerated.get(0) instanceof Map);
        Map<?, ?> first = (Map<?, ?>) regenerated.get(0);
        assertTrue(first.containsKey("module"));
        assertTrue(first.containsKey("tag") || first.containsKey("skill_tag") || first.containsKey("tags"));
        assertFalse(keys.isEmpty());
    }

    private Map<String, Object> buildStableEvaluations() {
        Map<String, Object> eval = new LinkedHashMap<>();
        List<Object> a = new ArrayList<>();
        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("errorType", "substitution");
        a1.put("phonologyProcess", "fronting");
        a.add(a1);
        eval.put("A", a);
        return eval;
    }
}
