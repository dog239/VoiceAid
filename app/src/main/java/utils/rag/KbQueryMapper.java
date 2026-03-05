package utils.rag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class KbQueryMapper {
    public static final int MAX_AUTO_KB_QUERIES = 8;
    private static final double LOW_SCORE_THRESHOLD = 3.0d;

    private KbQueryMapper() {
    }

    public static List<Map<String, Object>> buildKbQueriesFromEvaluations(Map<String, Object> evaluationsOrRoot) {
        Map<String, Object> eval = resolveEvaluations(evaluationsOrRoot);
        LinkedHashMap<String, Map<String, Object>> out = new LinkedHashMap<>();

        mapArticulation(asListOrSingleton(eval.get("A")), out);
        mapResultFalseModules(eval, out);
        mapNonWordRepetition(asListOrSingleton(eval.get("NWR")), out);
        mapScoreBasedModules(asListOrSingleton(eval.get("PST")), "PST", out);
        mapScoreBasedModules(asListOrSingleton(eval.get("PN")), "PN", out);

        if (out.isEmpty()) {
            addFallback(out);
        }
        return limitQueries(out, MAX_AUTO_KB_QUERIES);
    }

    private static Map<String, Object> resolveEvaluations(Map<String, Object> evaluationsOrRoot) {
        if (evaluationsOrRoot == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> eval = asMap(evaluationsOrRoot.get("evaluations"));
        return eval == null ? evaluationsOrRoot : eval;
    }

    private static void mapArticulation(List<Object> aItems, LinkedHashMap<String, Map<String, Object>> out) {
        if (aItems == null) {
            return;
        }
        for (Object item : aItems) {
            Map<String, Object> row = asMap(item);
            if (row == null) {
                continue;
            }

            if (!isBlank(text(row.get("errorType")))) {
                putQuery(out, "A", "error_type", "articulation error type training");
            }
            if (!isBlank(text(row.get("phonologyProcess")))) {
                putQuery(out, "A", "phonology_process", "phonological process intervention");
            }
        }
    }

    private static void mapResultFalseModules(Map<String, Object> eval, LinkedHashMap<String, Map<String, Object>> out) {
        mapResultFalseForModule(eval, "E", out);
        mapResultFalseForModule(eval, "RE", out);
        mapResultFalseForModule(eval, "S", out);
        mapResultFalseForModule(eval, "RG", out);
    }

    private static void mapResultFalseForModule(Map<String, Object> eval, String module, LinkedHashMap<String, Map<String, Object>> out) {
        for (Object rowObj : asListOrSingleton(eval.get(module))) {
            Map<String, Object> row = asMap(rowObj);
            if (row == null) {
                continue;
            }
            Object result = row.get("result");
            if (result != null && !asBoolean(result)) {
                putQuery(out, module, "training_needed", module.toLowerCase() + " training needed");
                return;
            }
        }
    }

    private static void mapNonWordRepetition(List<Object> nwrItems, LinkedHashMap<String, Map<String, Object>> out) {
        if (nwrItems == null || nwrItems.isEmpty()) {
            return;
        }
        boolean hasLow = false;
        for (Object rowObj : nwrItems) {
            Map<String, Object> row = asMap(rowObj);
            if (row == null) {
                continue;
            }
            for (int i = 1; i <= 6; i++) {
                Object value = row.get("results" + i);
                if (value == null) {
                    continue;
                }
                if (!asBoolean(value)) {
                    hasLow = true;
                    break;
                }
            }
            if (hasLow) {
                break;
            }
        }
        if (hasLow) {
            putQuery(out, "NWR", "nonword_repetition", "nonword repetition training");
        }
    }

    private static void mapScoreBasedModules(List<Object> items, String module, LinkedHashMap<String, Map<String, Object>> out) {
        if (items == null || items.isEmpty()) {
            return;
        }
        double sum = 0.0d;
        int count = 0;
        for (Object rowObj : items) {
            Map<String, Object> row = asMap(rowObj);
            Object score = row == null ? null : row.get("score");
            Double value = asDouble(score);
            if (value == null) {
                continue;
            }
            sum += value;
            count++;
        }
        if (count > 0 && (sum / count) < LOW_SCORE_THRESHOLD) {
            putQuery(out, module, "low_score", module.toLowerCase() + " low score training");
        }
    }

    private static void addFallback(LinkedHashMap<String, Map<String, Object>> out) {
        putQuery(out, "A", "error_type", "articulation error type training");
        putQuery(out, "E", "training_needed", "expression training needed");
        putQuery(out, "S", "training_needed", "semantic training needed");
        putQuery(out, "PST", "low_score", "pst low score training");
    }

    private static void putQuery(LinkedHashMap<String, Map<String, Object>> out, String module, String tag, String query) {
        if (out == null || isBlank(module) || isBlank(tag) || isBlank(query)) {
            return;
        }
        String key = module + "|" + tag;
        if (out.containsKey(key)) {
            return;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("module", module);
        item.put("tag", tag);
        item.put("query", query);
        out.put(key, item);
    }

    private static List<Map<String, Object>> limitQueries(LinkedHashMap<String, Map<String, Object>> out, int maxCount) {
        if (out == null || out.isEmpty() || maxCount <= 0) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<>(out.values());
        if (list.size() <= maxCount) {
            return list;
        }
        return new ArrayList<>(list.subList(0, maxCount));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map)) {
            return null;
        }
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        if (!(value instanceof List)) {
            return null;
        }
        return (List<Object>) value;
    }

    private static List<Object> asListOrSingleton(Object node) {
        if (node instanceof Map) {
            List<Object> singleton = new ArrayList<>(1);
            singleton.add(node);
            return singleton;
        }
        List<Object> list = asList(node);
        return list == null ? Collections.<Object>emptyList() : list;
    }

    private static String text(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private static boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
