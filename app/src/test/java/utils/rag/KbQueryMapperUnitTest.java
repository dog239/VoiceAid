package utils.rag;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KbQueryMapperUnitTest {

    @Test
    public void buildKbQueries_mapsAErrorType_whenAIsList() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> eval = new LinkedHashMap<>();
        List<Object> a = new ArrayList<>();
        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("errorType", "substitution");
        a.add(a1);
        eval.put("A", a);
        root.put("evaluations", eval);

        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(root);

        assertTrue(containsModuleTag(queries, "A", "error_type"));
    }

    @Test
    public void buildKbQueries_mapsAErrorType_whenAIsMap() {
        Map<String, Object> eval = new LinkedHashMap<>();
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("errorType", "substitution");
        eval.put("A", a);

        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(eval);

        assertTrue(containsModuleTag(queries, "A", "error_type"));
    }

    @Test
    public void buildKbQueries_mapsResultFalse() {
        Map<String, Object> eval = new LinkedHashMap<>();
        List<Object> e = new ArrayList<>();
        Map<String, Object> e1 = new LinkedHashMap<>();
        e1.put("result", false);
        e.add(e1);
        eval.put("E", e);

        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(eval);

        assertTrue(containsModuleTag(queries, "E", "training_needed"));
    }

    @Test
    public void buildKbQueries_mapsNwrToNwrModule() {
        Map<String, Object> eval = new LinkedHashMap<>();
        Map<String, Object> nwrRow = new LinkedHashMap<>();
        nwrRow.put("results1", false);
        eval.put("NWR", nwrRow);

        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(eval);

        assertTrue(containsModuleTag(queries, "NWR", "nonword_repetition"));
    }

    @Test
    public void buildKbQueries_deduplicatesByModuleTag() {
        Map<String, Object> eval = new LinkedHashMap<>();
        List<Object> a = new ArrayList<>();
        Map<String, Object> one = new LinkedHashMap<>();
        one.put("errorType", "substitution");
        Map<String, Object> two = new LinkedHashMap<>();
        two.put("errorType", "distortion");
        a.add(one);
        a.add(two);
        eval.put("A", a);

        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(eval);

        int count = 0;
        for (Map<String, Object> q : queries) {
            if ("A".equals(String.valueOf(q.get("module"))) && "error_type".equals(String.valueOf(q.get("tag")))) {
                count++;
            }
        }
        assertEquals(1, count);
    }

    @Test
    public void buildKbQueries_returnsFallbackWithValidShape() {
        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(new LinkedHashMap<String, Object>());

        assertTrue(queries.size() >= 3);
        for (Map<String, Object> query : queries) {
            assertTrue(query.containsKey("module"));
            assertTrue(query.containsKey("tag"));
            assertFalse(String.valueOf(query.get("module")).trim().isEmpty());
            assertFalse(String.valueOf(query.get("tag")).trim().isEmpty());
        }
    }

    @Test
    public void buildKbQueries_stableOrderForSameInput() {
        Map<String, Object> eval = new LinkedHashMap<>();

        List<Object> a = new ArrayList<>();
        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("errorType", "substitution");
        a1.put("phonologyProcess", "fronting");
        a.add(a1);
        eval.put("A", a);

        List<Object> e = new ArrayList<>();
        Map<String, Object> e1 = new LinkedHashMap<>();
        e1.put("result", false);
        e.add(e1);
        eval.put("E", e);

        List<Map<String, Object>> first = KbQueryMapper.buildKbQueriesFromEvaluations(eval);
        List<Map<String, Object>> second = KbQueryMapper.buildKbQueriesFromEvaluations(eval);

        assertEquals(first.toString(), second.toString());
    }

    @Test
    public void buildKbQueries_limitsToMaxCount() {
        Map<String, Object> eval = new LinkedHashMap<>();

        List<Object> a = new ArrayList<>();
        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("errorType", "substitution");
        a1.put("phonologyProcess", "fronting");
        a.add(a1);
        eval.put("A", a);

        for (String module : new String[]{"E", "RE", "S", "RG"}) {
            List<Object> rows = new ArrayList<>();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", false);
            rows.add(row);
            eval.put(module, rows);
        }

        List<Object> nwr = new ArrayList<>();
        Map<String, Object> nwrRow = new LinkedHashMap<>();
        nwrRow.put("results1", false);
        nwr.add(nwrRow);
        eval.put("NWR", nwr);

        List<Object> pst = new ArrayList<>();
        Map<String, Object> pstRow = new LinkedHashMap<>();
        pstRow.put("score", 1.0d);
        pst.add(pstRow);
        eval.put("PST", pst);

        List<Object> pn = new ArrayList<>();
        Map<String, Object> pnRow = new LinkedHashMap<>();
        pnRow.put("score", 1.0d);
        pn.add(pnRow);
        eval.put("PN", pn);

        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(eval);

        assertFalse(queries.isEmpty());
        assertTrue(queries.size() <= KbQueryMapper.MAX_AUTO_KB_QUERIES);
    }

    private boolean containsModuleTag(List<Map<String, Object>> queries, String module, String tag) {
        if (queries == null) {
            return false;
        }
        for (Map<String, Object> query : queries) {
            if (query == null) {
                continue;
            }
            if (module.equals(String.valueOf(query.get("module")))
                    && tag.equals(String.valueOf(query.get("tag")))) {
                return true;
            }
        }
        return false;
    }
}
