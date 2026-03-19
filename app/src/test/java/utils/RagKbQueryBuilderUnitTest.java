package utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.rag.RagKbHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RagKbQueryBuilderUnitTest {

    @Test
    public void buildKbQueriesFromEval_extractsExpectedKeywordsFromTypicalEval() {
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
        Map<String, Object> e2 = new LinkedHashMap<>();
        e2.put("result", true);
        Map<String, Object> e3 = new LinkedHashMap<>();
        e3.put("result", false);
        e.add(e1);
        e.add(e2);
        e.add(e3);
        eval.put("E", e);

        List<Map<String, Object>> queries = RagKbHelper.buildKbQueriesFromEvalMap(eval);

        assertFalse(queries.isEmpty());
        assertTrue(containsModuleTag(queries, "A", "error_type"));
        assertTrue(containsModuleTag(queries, "A", "phonology_process"));
        assertTrue(containsModuleTag(queries, "E", "training_needed"));
    }

    @Test
    public void buildKbQueriesFromEval_returnsFallbackWhenEvalMissingFields() {
        List<Map<String, Object>> queries = RagKbHelper.buildKbQueriesFromEvalMap(new LinkedHashMap<String, Object>());
        assertFalse(queries.isEmpty());
        assertTrue(queries.size() >= 3);
        assertTrue(containsModuleTag(queries, "A", "error_type"));
        assertTrue(containsModuleTag(queries, "E", "training_needed"));
    }

    @Test
    public void buildKbQueriesFromEval_deduplicatesAndStableSorts() {
        Map<String, Object> eval = new LinkedHashMap<>();
        List<Object> a = new ArrayList<>();
        Map<String, Object> firstA = new LinkedHashMap<>();
        firstA.put("errorType", "x");
        Map<String, Object> secondA = new LinkedHashMap<>();
        secondA.put("errorType", "y");
        a.add(firstA);
        a.add(secondA);
        eval.put("A", a);

        List<Map<String, Object>> first = RagKbHelper.buildKbQueriesFromEvalMap(eval);
        List<Map<String, Object>> second = RagKbHelper.buildKbQueriesFromEvalMap(eval);

        assertEquals(first.toString(), second.toString());
        Set<String> unique = new HashSet<>();
        for (Map<String, Object> query : first) {
            unique.add(String.valueOf(query.get("module")) + "|" + String.valueOf(query.get("tag")));
        }
        assertEquals(unique.size(), first.size());
    }

    @Test
    public void buildKbQueriesFromEval_honorsMaxCount() {
        Map<String, Object> eval = new LinkedHashMap<>();
        List<Object> a = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("errorType", "err" + i);
            item.put("phonologyProcess", "proc" + i);
            a.add(item);
        }
        eval.put("A", a);

        for (String module : new String[]{"E", "RE", "S", "RG"}) {
            List<Object> rows = new ArrayList<>();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", false);
            rows.add(row);
            eval.put(module, rows);
        }

        List<Map<String, Object>> queries = RagKbHelper.buildKbQueriesFromEvalMap(eval);
        assertTrue(queries.size() <= RagKbHelper.MAX_AUTO_KB_QUERIES);
    }

    private boolean containsModuleTag(List<Map<String, Object>> queries, String module, String tag) {
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
