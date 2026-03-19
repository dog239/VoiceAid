package utils.rag;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kb.KbMeta;
import kb.KbMetaLoader;
import kb.KbRepository;
import kb.KbStrategy;

public final class RagKbHelper {
    public static final int MAX_AUTO_KB_QUERIES = 8;
    private static volatile KbMeta cachedKbMeta;
    private static volatile KbMeta kbMetaOverride;

    private RagKbHelper() {
    }

    public static void setKbMetaOverride(KbMeta meta) {
        synchronized (RagKbHelper.class) {
            kbMetaOverride = meta;
        }
    }

    public static RagResult build(Object structuredInputOrEval,
                                  KbRepository repository,
                                  int topKPerTag,
                                  int maxContextItems,
                                  String source,
                                  String retrieval) {
        long startNs = System.nanoTime();
        List<QueryKey> keys = extractQueryKeys(structuredInputOrEval);
        List<Map<String, String>> retrieved = retrieveKbSnippets(repository, keys, topKPerTag);
        List<Map<String, String>> deduped = dedupeByIdKeepOrder(retrieved);
        List<Map<String, String>> ragKbItems = limitSnippets(deduped, maxContextItems);
        String ragContext = buildRagContext(ragKbItems);
        long latencyMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
        List<String> ragKbIds = extractSnippetIds(ragKbItems);

        Map<String, Object> meta = new LinkedHashMap<>();
        KbMeta kbMeta = kbMeta();
        meta.put("source", source == null ? "" : source);
        meta.put("retrieval", retrieval == null ? "" : retrieval);
        meta.put("topK_per_tag", topKPerTag);
        meta.put("max_context_items", maxContextItems);
        meta.put("query_count", keys.size());
        meta.put("hit_count", ragKbItems.size());
        meta.put("latency_ms", latencyMs);
        meta.put("kb_version", kbMeta.kbVersion);
        meta.put("build_time", kbMeta.buildTime);
        // Keep old key for backward compatibility with existing in-app consumers.
        meta.put("topKPerTag", topKPerTag);

        return new RagResult(keys, ragKbItems, ragContext, meta, ragKbIds);
    }

    public static List<QueryKey> extractQueryKeys(Object structuredInputOrEval) {
        if (!(structuredInputOrEval instanceof Map)) {
            return Collections.emptyList();
        }

        Map<String, Object> root = castMap(structuredInputOrEval);
        LinkedHashMap<String, LinkedHashSet<String>> pairs = new LinkedHashMap<>();

        Object kbQueriesObj = root.get("kb_queries");
        if (kbQueriesObj instanceof List) {
            List<Object> kbQueries = castList(kbQueriesObj);
            for (Object item : kbQueries) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> query = castMap(item);
                String module = asString(query.get("module"));
                for (String tag : extractTagsFromNode(query)) {
                    addPair(pairs, module, tag);
                }
            }
            return toQueryKeys(pairs);
        }

        Object resultsObj = root.get("results");
        if (resultsObj instanceof Map) {
            Object modulesObj = castMap(resultsObj).get("modules");
            if (modulesObj instanceof List) {
                for (Object moduleNodeObj : castList(modulesObj)) {
                    if (!(moduleNodeObj instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> moduleNode = castMap(moduleNodeObj);
                    String module = asString(moduleNode.get("module"));
                    for (String tag : extractTagsFromNode(moduleNode)) {
                        addPair(pairs, module, tag);
                    }
                }
            }
        }

        collectQueryKeys(root, pairs);
        return toQueryKeys(pairs);
    }

    public static List<Map<String, Object>> buildKbQueriesFromEval(JSONObject eval) {
        return buildKbQueriesFromEvalMap(jsonObjectToMap(eval));
    }

    public static List<Map<String, Object>> buildKbQueriesFromEvalMap(Map<String, Object> eval) {
        List<Map<String, Object>> queries = KbQueryMapper.buildKbQueriesFromEvaluations(eval);
        if (queries == null || queries.isEmpty()) {
            return Collections.emptyList();
        }
        if (queries.size() <= MAX_AUTO_KB_QUERIES) {
            return queries;
        }
        return new ArrayList<>(queries.subList(0, MAX_AUTO_KB_QUERIES));
    }

    public static List<Map<String, String>> retrieveKbSnippets(KbRepository repository, List<QueryKey> keys, int topKPerTag) {
        if (repository == null || keys == null || keys.isEmpty() || topKPerTag <= 0) {
            return Collections.emptyList();
        }

        List<Map<String, String>> result = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();

        for (QueryKey key : keys) {
            if (key == null || isBlank(key.module) || key.tags == null || key.tags.isEmpty()) {
                continue;
            }
            for (String tag : key.tags) {
                if (isBlank(tag)) {
                    continue;
                }

                List<KbStrategy> hits;
                try {
                    hits = repository.query(key.module, tag, topKPerTag);
                } catch (Exception ignored) {
                    continue;
                }

                if (hits == null || hits.isEmpty()) {
                    continue;
                }

                for (KbStrategy strategy : hits) {
                    if (strategy == null) {
                        continue;
                    }
                    String dedupeKey = normalize(strategy.id) + "|" + key.module + "|" + normalize(tag);
                    if (!seen.add(dedupeKey)) {
                        continue;
                    }

                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("id", safeString(strategy.id));
                    item.put("module", safeString(strategy.module));
                    item.put("skill_tag", normalize(tag));
                    item.put("title", safeString(strategy.title));
                    item.put("level", safeString(strategy.level));
                    item.put("text", safeString(strategy.text));
                    item.put("do_not", safeString(strategy.doNot));
                    result.add(item);
                }
            }
        }

        return result;
    }

    public static String buildRagContext(List<Map<String, String>> snippets, int maxItems) {
        if (snippets == null || snippets.isEmpty() || maxItems <= 0) {
            return "";
        }

        int count = Math.min(maxItems, snippets.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            Map<String, String> item = snippets.get(i);
            if (item == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n---\n");
            }
            sb.append("[ID:").append(safeString(item.get("id"))).append("]");
            sb.append("[module:").append(safeString(item.get("module"))).append("]");
            sb.append("[tag:").append(safeString(item.get("skill_tag"))).append("] ");
            sb.append(safeString(item.get("title")));
            sb.append("\ntext: ").append(safeString(item.get("text")));
            sb.append("\ndo_not: ").append(safeString(item.get("do_not")));
        }
        return sb.toString();
    }

    public static String buildRagContext(List<Map<String, String>> snippets) {
        if (snippets == null || snippets.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < snippets.size(); i++) {
            Map<String, String> item = snippets.get(i);
            if (item == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n---\n");
            }
            sb.append("[ID:").append(safeString(item.get("id"))).append("]");
            sb.append("[module:").append(safeString(item.get("module"))).append("]");
            sb.append("[tag:").append(safeString(item.get("skill_tag"))).append("] ");
            sb.append(safeString(item.get("title")));
            sb.append("\ntext: ").append(safeString(item.get("text")));
            sb.append("\ndo_not: ").append(safeString(item.get("do_not")));
        }
        return sb.toString();
    }

    private static List<Map<String, String>> limitSnippets(List<Map<String, String>> snippets, int maxItems) {
        if (snippets == null || snippets.isEmpty() || maxItems <= 0) {
            return Collections.emptyList();
        }
        int count = Math.min(maxItems, snippets.size());
        List<Map<String, String>> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Map<String, String> item = snippets.get(i);
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }

    private static List<Map<String, String>> dedupeByIdKeepOrder(List<Map<String, String>> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, String>> out = new ArrayList<>();
        LinkedHashSet<String> seenIds = new LinkedHashSet<>();
        for (Map<String, String> item : items) {
            if (item == null) {
                continue;
            }
            String id = safeString(item.get("id")).trim();
            if (id.isEmpty()) {
                out.add(item);
                continue;
            }
            if (seenIds.add(id)) {
                out.add(item);
            }
        }
        return out;
    }

    private static List<String> extractSnippetIds(List<Map<String, String>> snippets) {
        if (snippets == null || snippets.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (Map<String, String> item : snippets) {
            if (item == null) {
                continue;
            }
            String id = safeString(item.get("id")).trim();
            if (!id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void collectQueryKeys(Object node, LinkedHashMap<String, LinkedHashSet<String>> out) {
        if (node == null) {
            return;
        }

        if (node instanceof Map) {
            Map<String, Object> map = castMap(node);
            String module = asString(map.get("module"));
            if (!isBlank(module)) {
                for (String tag : extractTagsFromNode(map)) {
                    addPair(out, module, tag);
                }
            }
            for (Object child : map.values()) {
                collectQueryKeys(child, out);
            }
            return;
        }

        if (node instanceof List) {
            for (Object item : castList(node)) {
                collectQueryKeys(item, out);
            }
        }
    }

    private static List<String> extractTagsFromNode(Map<String, Object> node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<String> tags = new ArrayList<>();
        appendTags(tags, node.get("tag"));
        appendTags(tags, node.get("skill_tag"));
        appendTags(tags, node.get("tags"));
        return tags;
    }

    private static void appendTags(List<String> out, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof List) {
            for (Object item : castList(value)) {
                appendTags(out, item);
            }
            return;
        }

        String raw = asString(value).trim();
        if (raw.isEmpty()) {
            return;
        }

        String[] splits = raw.split("[,\\uFF0C]");
        if (splits.length <= 1) {
            out.add(raw);
            return;
        }

        for (String split : splits) {
            if (split == null) {
                continue;
            }
            String tag = split.trim();
            if (!tag.isEmpty()) {
                out.add(tag);
            }
        }
    }

    private static void collectFromSpeechSound(List<Object> array, LinkedHashMap<String, Map<String, Object>> out) {
        if (array == null) {
            return;
        }
        int initialTotal = 0;
        int initialCorrect = 0;
        for (int i = 0; i < array.size(); i++) {
            Map<String, Object> item = asMap(array.get(i));
            if (item == null) {
                continue;
            }
            String errorType = safeJsonText(asString(item.get("errorType")));
            if (!errorType.isEmpty()) {
                putQuery(out, "A", "error_type", "构音 " + errorType + " 纠正 训练");
            }
            String process = safeJsonText(asString(item.get("phonologyProcess")));
            if (!process.isEmpty()) {
                putQuery(out, "A", "phonology_process", "构音 " + process + " 音系过程 干预");
            }

            List<Object> targetWord = asList(item.get("targetWord"));
            List<Object> answerPhonology = asList(item.get("answerPhonology"));
            if (targetWord == null || answerPhonology == null) {
                continue;
            }
            int len = Math.min(targetWord.size(), answerPhonology.size());
            for (int j = 0; j < len; j++) {
                String targetInitial = readInitial(asMap(targetWord.get(j)));
                if (targetInitial.isEmpty()) {
                    continue;
                }
                initialTotal++;
                String answerInitial = readInitial(asMap(answerPhonology.get(j)));
                if (targetInitial.equalsIgnoreCase(answerInitial)) {
                    initialCorrect++;
                }
            }
        }
        if (initialTotal > 0) {
            double correctRate = initialCorrect * 1.0d / initialTotal;
            if (correctRate < 0.8d) {
                putQuery(out, "A", "initial_accuracy", "声母 构音 正确率 提升 训练");
            }
        }
    }

    private static void collectFromVocabulary(Map<String, Object> eval, LinkedHashMap<String, Map<String, Object>> out) {
        if (isLowAccuracy(asList(eval.get("E")), "result", 0.75d)) {
            putQuery(out, "vocabulary", "expression", "学前 儿童 词汇 表达 训练 建议");
        }
        if (isLowAccuracy(asList(eval.get("RE")), "result", 0.75d)) {
            putQuery(out, "vocabulary", "comprehension", "学前 儿童 词汇 理解 训练 建议");
        }
        if (isLowAccuracy(asList(eval.get("S")), "result", 0.75d)) {
            putQuery(out, "vocabulary", "semantics", "词义 语义 理解 提升 训练");
        }
        if (isLowNwr(asList(eval.get("NWR")), 0.70d)) {
            putQuery(out, "vocabulary", "nonword_repetition", "非词复述 语音记忆 训练");
        }
    }

    private static void collectFromSyntax(List<Object> rg, LinkedHashMap<String, Map<String, Object>> out) {
        if (isLowAccuracy(rg, "result", 0.75d)) {
            putQuery(out, "syntax", "grammar_comprehension", "学前 儿童 语法 理解 训练");
        }
    }

    private static void collectFromNarrative(List<Object> pst, List<Object> pn, LinkedHashMap<String, Map<String, Object>> out) {
        if (isLowAverageScore(pst, "score", 3.0d) || isLowAverageScore(pn, "score", 3.0d)) {
            putQuery(out, "social_pragmatics", "narrative", "儿童 叙事 复述 组织表达 训练");
        }
    }

    private static void addSupplementQueries(LinkedHashMap<String, Map<String, Object>> out) {
        boolean hasSpeech = hasModule(out, "A");
        boolean hasVocab = hasModule(out, "vocabulary");
        boolean hasSyntax = hasModule(out, "syntax");
        boolean hasSocial = hasModule(out, "social_pragmatics");
        if (hasSpeech) {
            putQuery(out, "A", "module_guidance", "学前 儿童 构音 训练 建议");
        }
        if (hasVocab) {
            putQuery(out, "vocabulary", "module_guidance", "学前 儿童 词汇 能力 干预 方案");
        }
        if (hasSyntax) {
            putQuery(out, "syntax", "module_guidance", "学前 儿童 句法 理解 表达 训练");
        }
        if (hasSocial) {
            putQuery(out, "social_pragmatics", "module_guidance", "学前 儿童 叙事 社交语用 训练");
        }
    }

    private static void addFallbackQueries(LinkedHashMap<String, Map<String, Object>> out) {
        putQuery(out, "A", "error_type", "学前 儿童 构音 训练 建议");
        putQuery(out, "vocabulary", "expression", "学前 儿童 词汇 表达 理解 干预");
        putQuery(out, "syntax", "grammar_comprehension", "学前 儿童 语法 理解 训练");
    }

    private static boolean hasModule(LinkedHashMap<String, Map<String, Object>> out, String module) {
        if (out == null || out.isEmpty() || isBlank(module)) {
            return false;
        }
        for (Map<String, Object> item : out.values()) {
            if (item == null) {
                continue;
            }
            if (module.equalsIgnoreCase(String.valueOf(item.get("module")))) {
                return true;
            }
        }
        return false;
    }

    private static List<Map<String, Object>> limitQueries(LinkedHashMap<String, Map<String, Object>> out, int maxCount) {
        if (out == null || out.isEmpty() || maxCount <= 0) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<>(out.values());
        list.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                String lm = safeString(left.get("module"));
                String rm = safeString(right.get("module"));
                int cmp = lm.compareTo(rm);
                if (cmp != 0) {
                    return cmp;
                }
                String lt = safeString(left.get("tag"));
                String rt = safeString(right.get("tag"));
                cmp = lt.compareTo(rt);
                if (cmp != 0) {
                    return cmp;
                }
                String lq = safeString(left.get("query"));
                String rq = safeString(right.get("query"));
                return lq.compareTo(rq);
            }
        });

        int count = Math.min(maxCount, list.size());
        List<Map<String, Object>> limited = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            limited.add(list.get(i));
        }
        return limited;
    }

    private static void putQuery(LinkedHashMap<String, Map<String, Object>> out, String module, String tag, String query) {
        String normalizedModule = normalize(module);
        String normalizedTag = normalize(tag);
        String normalizedQuery = safeJsonText(query);
        if (isBlank(normalizedModule) || isBlank(normalizedTag) || isBlank(normalizedQuery)) {
            return;
        }
        String key = normalizedModule + "|" + normalizedTag + "|" + normalizedQuery;
        if (out.containsKey(key)) {
            return;
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("module", normalizedModule);
        item.put("tag", normalizedTag);
        item.put("query", normalizedQuery);
        out.put(key, item);
    }

    private static boolean isLowAccuracy(List<Object> array, String key, double threshold) {
        if (array == null || isBlank(key)) {
            return false;
        }
        int total = 0;
        int correct = 0;
        for (int i = 0; i < array.size(); i++) {
            Map<String, Object> obj = asMap(array.get(i));
            if (obj == null || !obj.containsKey(key) || obj.get(key) == null) {
                continue;
            }
            total++;
            Object val = obj.get(key);
            if (val instanceof Boolean ? (Boolean) val : Boolean.parseBoolean(String.valueOf(val))) {
                correct++;
            }
        }
        if (total == 0) {
            return false;
        }
        return (correct * 1.0d / total) < threshold;
    }

    private static boolean isLowNwr(List<Object> array, double threshold) {
        if (array == null) {
            return false;
        }
        int total = 0;
        int correct = 0;
        for (int i = 0; i < array.size(); i++) {
            Map<String, Object> obj = asMap(array.get(i));
            if (obj == null) {
                continue;
            }
            for (int j = 1; j <= 6; j++) {
                String key = "results" + j;
                Object value = obj.get(key);
                if (value == null) {
                    continue;
                }
                total++;
                if (value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value))) {
                    correct++;
                }
            }
        }
        if (total == 0) {
            return false;
        }
        return (correct * 1.0d / total) < threshold;
    }

    private static boolean isLowAverageScore(List<Object> array, String key, double threshold) {
        if (array == null || isBlank(key)) {
            return false;
        }
        double sum = 0.0d;
        int count = 0;
        for (int i = 0; i < array.size(); i++) {
            Map<String, Object> obj = asMap(array.get(i));
            Object value = obj == null ? null : obj.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Number) {
                sum += ((Number) value).doubleValue();
            } else {
                try {
                    sum += Double.parseDouble(String.valueOf(value));
                } catch (Exception ignored) {
                    continue;
                }
            }
            count++;
        }
        if (count == 0) {
            return false;
        }
        return (sum / count) < threshold;
    }

    private static String readInitial(Map<String, Object> characterPhonology) {
        if (characterPhonology == null) {
            return "";
        }
        Map<String, Object> phonology = asMap(characterPhonology.get("phonology"));
        if (phonology == null) {
            return "";
        }
        return safeJsonText(asString(phonology.get("initial")));
    }

    private static String safeJsonText(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        if ("null".equalsIgnoreCase(text)) {
            return "";
        }
        return text;
    }

    private static Map<String, Object> jsonObjectToMap(JSONObject object) {
        if (object == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            JSONArray names = object.names();
            if (names == null) {
                return map;
            }
            for (int i = 0; i < names.length(); i++) {
                String name = names.optString(i, "");
                if (name.isEmpty()) {
                    continue;
                }
                map.put(name, jsonToJavaValue(object.opt(name)));
            }
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
        return map;
    }

    private static List<Object> jsonArrayToList(JSONArray array) {
        if (array == null) {
            return Collections.emptyList();
        }
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(jsonToJavaValue(array.opt(i)));
        }
        return list;
    }

    private static Object jsonToJavaValue(Object value) {
        if (value == null || value == JSONObject.NULL) {
            return null;
        }
        if (value instanceof JSONObject) {
            return jsonObjectToMap((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            return jsonArrayToList((JSONArray) value);
        }
        return value;
    }

    private static void addPair(LinkedHashMap<String, LinkedHashSet<String>> out, String module, String tag) {
        if (isBlank(module) || isBlank(tag)) {
            return;
        }

        String moduleKey = normalize(module);
        LinkedHashSet<String> tags = out.get(moduleKey);
        if (tags == null) {
            tags = new LinkedHashSet<>();
            out.put(moduleKey, tags);
        }
        tags.add(normalize(tag));
    }

    private static List<QueryKey> toQueryKeys(LinkedHashMap<String, LinkedHashSet<String>> map) {
        if (map.isEmpty()) {
            return Collections.emptyList();
        }

        List<QueryKey> keys = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : map.entrySet()) {
            List<String> tags = new ArrayList<>(entry.getValue());
            if (!tags.isEmpty()) {
                keys.add(new QueryKey(entry.getKey(), tags));
            }
        }
        return keys;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    private static String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static KbMeta kbMeta() {
        KbMeta override = kbMetaOverride;
        if (override != null) {
            return override;
        }
        KbMeta local = cachedKbMeta;
        if (local != null) {
            return local;
        }
        synchronized (RagKbHelper.class) {
            local = cachedKbMeta;
            if (local == null) {
                local = KbMetaLoader.loadFromMainAssets();
                cachedKbMeta = local;
            }
        }
        return local;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private static Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map)) {
            return null;
        }
        return castMap(value);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> castList(Object value) {
        return (List<Object>) value;
    }

    private static List<Object> asList(Object value) {
        if (!(value instanceof List)) {
            return null;
        }
        return castList(value);
    }

    public static final class QueryKey {
        public final String module;
        public final List<String> tags;

        public QueryKey(String module, List<String> tags) {
            this.module = module;
            this.tags = tags == null
                    ? Collections.<String>emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(tags));
        }
    }

    public static final class RagResult {
        public final List<QueryKey> keys;
        public final List<Map<String, String>> ragKbItems;
        public final String ragContext;
        public final Map<String, Object> meta;
        public final List<String> ragKbIds;

        public RagResult(List<QueryKey> keys,
                         List<Map<String, String>> ragKbItems,
                         String ragContext,
                         Map<String, Object> meta) {
            this(keys, ragKbItems, ragContext, meta, Collections.<String>emptyList());
        }

        public RagResult(List<QueryKey> keys,
                         List<Map<String, String>> ragKbItems,
                         String ragContext,
                         Map<String, Object> meta,
                         List<String> ragKbIds) {
            this.keys = keys == null ? Collections.<QueryKey>emptyList() : Collections.unmodifiableList(new ArrayList<>(keys));
            this.ragKbItems = ragKbItems == null
                    ? Collections.<Map<String, String>>emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(ragKbItems));
            this.ragContext = ragContext == null ? "" : ragContext;
            this.meta = meta == null
                    ? Collections.<String, Object>emptyMap()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(meta));
            this.ragKbIds = ragKbIds == null
                    ? Collections.<String>emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(ragKbIds));
        }
    }
}
