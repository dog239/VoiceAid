package utils;

import android.content.Context;
import android.util.Log;

import com.example.CCLEvaluation.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kb.AssetsKbRepository;
import kb.KbMetaLoader;
import kb.KbRepository;
import utils.rag.RagKbHelper;

public class ReportPipeline {
    public static boolean useRag = false;
    private static final String TAG = "ReportPipeline";
    private static final int KB_TOP_K_PER_TAG = 3;
    private static final int RAG_CONTEXT_MAX_ITEMS = 10;
    private static final String KB_SOURCE = "assets/kb/strategies.jsonl";
    private static final String KB_RETRIEVAL = "B1_module+tag";

    private final Context appContext;
    private final KbRepository injectedKbRepo;
    private volatile KbRepository kbRepo;

    public interface Callback {
        void onSuccess(JSONObject plan);

        default void onPartialSuccess(JSONObject partialPlan, String errorMessage) {
            onError(errorMessage);
        }

        void onError(String errorMessage);
    }

    public ReportPipeline(Context context) {
        this(context, null);
    }

    ReportPipeline(Context context, KbRepository injectedKbRepo) {
        this.appContext = context != null ? context.getApplicationContext() : null;
        this.injectedKbRepo = injectedKbRepo;
        if (this.appContext != null) {
            RagKbHelper.setKbMetaOverride(KbMetaLoader.loadFromAssets(this.appContext));
        }
    }

    public void generateTreatmentPlan(String childIdOrPath, Callback cb) {
        if (useRag) {
            try {
                generateTreatmentPlanWithRag(childIdOrPath, cb);
                return;
            } catch (Exception e) {
                Log.e(TAG, "fallback_reason=rag_exception, route=legacy, child=" + childIdOrPath, e);
            }
        }
        generateTreatmentPlanLegacy(childIdOrPath, cb);
    }

    private void generateTreatmentPlanLegacy(String childIdOrPath, Callback cb) {
        JSONObject latestData;
        try {
            latestData = dataManager.getInstance().loadData(childIdOrPath);
        } catch (Exception e) {
            if (cb != null) {
                cb.onError("加载评测数据失败");
            }
            return;
        }

        Map<String, String> prompts;
        try {
            prompts = TreatmentPromptBuilder.buildConcurrentPrompts(latestData);
        } catch (JSONException e) {
            if (cb != null) {
                cb.onError("构建提示词失败");
            }
            return;
        }

        try {
            LlmPlanService service = new LlmPlanService();
            service.generateTreatmentPlanConcurrent(prompts, new LlmPlanService.PlanCallback() {
                @Override
                public void onSuccess(JSONObject plan) {
                    if (cb != null) {
                        cb.onSuccess(preparePlanForDelivery(plan, false, null));
                    }
                }

                @Override
                public void onPartialSuccess(JSONObject partialPlan, String errorMessage) {
                    if (cb != null) {
                        cb.onPartialSuccess(preparePlanForDelivery(partialPlan, true, errorMessage), errorMessage);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (cb != null) {
                        cb.onError(errorMessage);
                    }
                }
            });
        } catch (Exception e) {
            if (cb != null) {
                cb.onError("生成治疗计划失败: " + e.getMessage());
            }
        }
    }

    private void generateTreatmentPlanWithRag(String childIdOrPath, Callback cb) {
        JSONObject latestData;
        try {
            latestData = dataManager.getInstance().loadData(childIdOrPath);
        } catch (Exception e) {
            if (cb != null) {
                cb.onError("加载评测数据失败");
            }
            return;
        }

        Map<String, String> prompts;
        try {
            prompts = TreatmentPromptBuilder.buildConcurrentPrompts(latestData);
        } catch (JSONException e) {
            if (cb != null) {
                cb.onError("构建提示词失败");
            }
            return;
        }

        RagKbHelper.RagResult ragResult = buildRagResult(latestData, KB_TOP_K_PER_TAG, RAG_CONTEXT_MAX_ITEMS);
        if (ragResult.keys.isEmpty()) {
            safeLogW("no_kb_queries_extracted, route=rag, child=" + childIdOrPath);
        }
        safeLogI("rag_debug"
                + ", query_count=" + asInt(ragResult.meta.get("query_count"))
                + ", hit_count=" + asInt(ragResult.meta.get("hit_count"))
                + ", latency_ms=" + asLong(ragResult.meta.get("latency_ms"))
                + ", topK_per_tag=" + asInt(ragResult.meta.get("topK_per_tag"))
                + ", max_context_items=" + asInt(ragResult.meta.get("max_context_items"))
                + ", kb_queries=" + previewQueryPairs(ragResult.keys, 5)
                + ", rag_kb_ids=" + previewIds(ragResult.ragKbIds, 5));

        Map<String, String> ragPrompts = appendRagContext(prompts, ragResult.ragContext);
        try {
            LlmPlanService service = new LlmPlanService();
            service.generateTreatmentPlanConcurrent(ragPrompts, new LlmPlanService.PlanCallback() {
                @Override
                public void onSuccess(JSONObject plan) {
                    JSONObject output = preparePlanForDelivery(plan, false, null);
                    attachRagFields(output, ragResult);
                    if (cb != null) {
                        cb.onSuccess(output);
                    }
                }

                @Override
                public void onPartialSuccess(JSONObject partialPlan, String errorMessage) {
                    JSONObject output = preparePlanForDelivery(partialPlan, true, errorMessage);
                    attachRagFields(output, ragResult);
                    if (cb != null) {
                        cb.onPartialSuccess(output, errorMessage);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    safeLogE("fallback_reason=rag_llm_error, route=legacy, child=" + childIdOrPath + ", err=" + errorMessage, null);
                    fallbackLegacyWithRag(childIdOrPath, cb, ragResult);
                }
            });
        } catch (Exception e) {
            safeLogE("fallback_reason=rag_exception, route=legacy, child=" + childIdOrPath, e);
            fallbackLegacyWithRag(childIdOrPath, cb, ragResult);
        }
    }

    private void fallbackLegacyWithRag(String childIdOrPath, Callback cb, RagKbHelper.RagResult ragResult) {
        generateTreatmentPlanLegacy(childIdOrPath, new Callback() {
            @Override
            public void onSuccess(JSONObject plan) {
                JSONObject output = preparePlanForDelivery(plan, false, null);
                attachRagFields(output, ragResult);
                if (cb != null) {
                    cb.onSuccess(output);
                }
            }

            @Override
            public void onPartialSuccess(JSONObject partialPlan, String errorMessage) {
                JSONObject output = preparePlanForDelivery(partialPlan, true, errorMessage);
                attachRagFields(output, ragResult);
                if (cb != null) {
                    cb.onPartialSuccess(output, errorMessage);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (cb != null) {
                    cb.onError(errorMessage);
                }
            }
        });
    }

    private KbRepository kbRepo() {
        KbRepository local = kbRepo;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            local = kbRepo;
            if (local == null) {
                local = injectedKbRepo != null ? injectedKbRepo : new AssetsKbRepository(appContext);
                kbRepo = local;
            }
        }
        return local;
    }

    private JSONObject preparePlanForDelivery(JSONObject plan, boolean partial, String warningMessage) {
        JSONObject output = plan != null ? plan : new JSONObject();
        try {
            output.put("partial", partial);
            if (partial) {
                output.put("warningMessage", safeText(warningMessage));
            } else if (!output.has("warningMessage")) {
                output.put("warningMessage", "");
            }
            if (!output.has("failedModules") || !(output.opt("failedModules") instanceof JSONArray)) {
                output.put("failedModules", new JSONArray());
            }
            if (!output.has("generatedAt")) {
                output.put("generatedAt", System.currentTimeMillis());
            }
        } catch (JSONException e) {
            safeLogE("preparePlanForDelivery failed", e);
        }
        return output;
    }

    private RagKbHelper.RagResult buildRagResult(JSONObject structuredInputOrEval, int topKPerTag, int maxContextItems) {
        JSONObject payload = ensureKbQueries(structuredInputOrEval);
        Map<String, Object> structuredMap = jsonObjectToMap(payload);
        return RagKbHelper.build(structuredMap, kbRepo(), topKPerTag, maxContextItems, KB_SOURCE, KB_RETRIEVAL);
    }

    private JSONObject ensureKbQueries(JSONObject structuredInputOrEval) {
        JSONObject payload = structuredInputOrEval == null ? new JSONObject() : cloneJson(structuredInputOrEval);
        if (validateKbQueriesShape(payload)) {
            return payload;
        }
        if (payload.has("kb_queries")) {
            safeLogW("invalid kb_queries shape, regenerating");
        }
        JSONObject eval = payload.optJSONObject("evaluations");
        if (eval == null) {
            eval = payload;
        }
        List<Map<String, Object>> generated = RagKbHelper.buildKbQueriesFromEvalMap(jsonObjectToMap(eval));
        if (!generated.isEmpty()) {
            try {
                JSONArray arr = new JSONArray();
                for (Map<String, Object> item : generated) {
                    if (item == null) {
                        continue;
                    }
                    arr.put(new JSONObject(item));
                }
                payload.put("kb_queries", arr);
            } catch (JSONException ignored) {
            }
        }
        return payload;
    }

    Map<String, Object> ensureKbQueries(Map<String, Object> payloadOrEval) {
        Map<String, Object> payload = payloadOrEval == null
                ? new LinkedHashMap<String, Object>()
                : new LinkedHashMap<>(payloadOrEval);
        if (validateKbQueriesShape(payload)) {
            return payload;
        }
        if (payload.containsKey("kb_queries")) {
            safeLogW("invalid kb_queries shape, regenerating");
        }

        Map<String, Object> eval = asObjectMap(payload.get("evaluations"));
        if (eval == null) {
            eval = payload;
        }

        List<Map<String, Object>> generated = RagKbHelper.buildKbQueriesFromEvalMap(eval);
        if (!generated.isEmpty()) {
            List<Map<String, Object>> kbQueries = new ArrayList<>();
            for (Map<String, Object> item : generated) {
                if (item == null) {
                    continue;
                }
                kbQueries.add(new LinkedHashMap<>(item));
            }
            payload.put("kb_queries", kbQueries);
        }
        return payload;
    }

    private boolean validateKbQueriesShape(JSONObject payload) {
        if (payload == null) {
            return false;
        }
        Object value = payload.opt("kb_queries");
        if (!(value instanceof JSONArray)) {
            return false;
        }
        JSONArray array = (JSONArray) value;
        if (array.length() <= 0) {
            return false;
        }
        for (int i = 0; i < array.length(); i++) {
            Object item = array.opt(i);
            if (!(item instanceof JSONObject)) {
                return false;
            }
            JSONObject query = (JSONObject) item;
            if (!query.has("module") || query.isNull("module")) {
                return false;
            }
            boolean hasAnyTag = (query.has("tag") && !query.isNull("tag"))
                    || (query.has("skill_tag") && !query.isNull("skill_tag"))
                    || (query.has("tags") && !query.isNull("tags"));
            if (!hasAnyTag) {
                return false;
            }
        }
        return true;
    }

    private boolean validateKbQueriesShape(Map<String, Object> payload) {
        if (payload == null) {
            return false;
        }
        Object value = payload.get("kb_queries");
        if (!(value instanceof List)) {
            return false;
        }
        List<?> array = (List<?>) value;
        if (array.isEmpty()) {
            return false;
        }
        for (Object item : array) {
            if (!(item instanceof Map)) {
                return false;
            }
            Map<?, ?> query = (Map<?, ?>) item;
            Object module = query.get("module");
            if (module == null || isBlank(String.valueOf(module))) {
                return false;
            }
            boolean hasAnyTag = query.get("tag") != null
                    || query.get("skill_tag") != null
                    || query.get("tags") != null;
            if (!hasAnyTag) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asObjectMap(Object value) {
        if (!(value instanceof Map)) {
            return null;
        }
        return (Map<String, Object>) value;
    }

    private JSONObject cloneJson(JSONObject source) {
        if (source == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(source.toString());
        } catch (Exception ignored) {
            return source;
        }
    }

    private Map<String, String> appendRagContext(Map<String, String> prompts, String ragContext) {
        if (prompts == null || prompts.isEmpty()) {
            return Collections.emptyMap();
        }
        if (isBlank(ragContext)) {
            return prompts;
        }
        Map<String, String> out = new HashMap<>();
        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            String prompt = entry.getValue() == null ? "" : entry.getValue();
            StringBuilder sb = new StringBuilder(prompt.length() + ragContext.length() + 300);
            sb.append(prompt);
            sb.append("\n\n[Reference KB - RAG]\n");
            sb.append(ragContext);
            sb.append("\n\nGenerate auditable structured advice using only the KB snippets and structured results above. State missing info explicitly and do not fabricate.");
            out.put(entry.getKey(), sb.toString());
        }
        return out;
    }

    void attachRagFields(JSONObject plan, RagKbHelper.RagResult ragResult) {
        if (plan == null || ragResult == null) {
            return;
        }
        try {
            Map<String, Object> fields = buildRagFieldsMap(ragResult);
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                plan.put(entry.getKey(), toJsonValue(entry.getValue()));
            }
        } catch (JSONException e) {
            safeLogE("attach_rag_failed", e);
        }
    }

    void attachRagFields(Map<String, Object> plan, RagKbHelper.RagResult ragResult) {
        if (plan == null || ragResult == null) {
            return;
        }
        plan.putAll(buildRagFieldsMap(ragResult));
    }

    private Map<String, Object> buildRagFieldsMap(RagKbHelper.RagResult ragResult) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("rag_kb_meta", new LinkedHashMap<>(ragResult.meta));
        fields.put("rag_kb_ids", new ArrayList<>(ragResult.ragKbIds));
        if (shouldExposeRagDetails()) {
            fields.put("rag_kb", new ArrayList<>(ragResult.ragKbItems));
            fields.put("rag_context", ragResult.ragContext == null ? "" : ragResult.ragContext);
        } else {
            String clipped = clipText(ragResult.ragContext, releaseRagContextMaxChars());
            if (!isBlank(clipped)) {
                fields.put("rag_context", clipped);
            }
        }
        return fields;
    }

    private Map<String, Object> jsonObjectToMap(JSONObject object) {
        if (object == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonToJavaValue(object.opt(key)));
        }
        return map;
    }

    private List<Object> jsonArrayToList(JSONArray array) {
        if (array == null) {
            return Collections.emptyList();
        }
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(jsonToJavaValue(array.opt(i)));
        }
        return list;
    }

    private Object jsonToJavaValue(Object value) {
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

    private JSONObject toJsonObject(Map<String, ?> map) throws JSONException {
        JSONObject object = new JSONObject();
        if (map == null) {
            return object;
        }
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            object.put(entry.getKey(), toJsonValue(entry.getValue()));
        }
        return object;
    }

    private JSONArray toJsonArray(List<?> list) throws JSONException {
        JSONArray array = new JSONArray();
        if (list == null) {
            return array;
        }
        for (Object item : list) {
            array.put(toJsonValue(item));
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private Object toJsonValue(Object value) throws JSONException {
        if (value == null) {
            return JSONObject.NULL;
        }
        if (value instanceof Map) {
            return toJsonObject((Map<String, ?>) value);
        }
        if (value instanceof List) {
            return toJsonArray((List<?>) value);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    boolean shouldExposeRagDetails() {
        return BuildConfig.DEBUG;
    }

    int releaseRagContextMaxChars() {
        return 2000;
    }

    private String clipText(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars);
    }

    private String previewQueryPairs(List<RagKbHelper.QueryKey> keys, int maxItems) {
        if (keys == null || keys.isEmpty() || maxItems <= 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int count = 0;
        for (RagKbHelper.QueryKey key : keys) {
            if (key == null || isBlank(key.module) || key.tags == null || key.tags.isEmpty()) {
                continue;
            }
            for (String tag : key.tags) {
                if (isBlank(tag)) {
                    continue;
                }
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(key.module).append("|").append(tag);
                count++;
                if (count >= maxItems) {
                    sb.append(", ...");
                    sb.append("]");
                    return sb.toString();
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String previewIds(List<String> ids, int maxItems) {
        if (ids == null || ids.isEmpty() || maxItems <= 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int count = Math.min(maxItems, ids.size());
        for (int i = 0; i < count; i++) {
            String id = ids.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(id == null ? "" : id);
        }
        if (ids.size() > maxItems) {
            sb.append(", ...");
        }
        sb.append("]");
        return sb.toString();
    }

    private int asInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private void safeLogI(String msg) {
        try {
            Log.i(TAG, msg);
        } catch (Throwable ignored) {
        }
    }

    private void safeLogW(String msg) {
        try {
            Log.w(TAG, msg);
        } catch (Throwable ignored) {
        }
    }

    private void safeLogE(String msg, Throwable t) {
        try {
            if (t == null) {
                Log.e(TAG, msg);
            } else {
                Log.e(TAG, msg, t);
            }
        } catch (Throwable ignored) {
        }
    }
}
