package utils.rag.module;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssetModuleKnowledgeRepository implements ModuleKnowledgeRepository {
    private static final String TAG = "ModuleKnowledgeRepo";

    private final Context appContext;
    private final Map<String, List<KnowledgeDoc>> cache = new ConcurrentHashMap<>();
    private final Map<String, String> inlineAssets;

    public AssetModuleKnowledgeRepository(Context context) {
        this(context, null);
    }

    AssetModuleKnowledgeRepository(Context context, Map<String, String> inlineAssets) {
        this.appContext = context == null ? null : context.getApplicationContext();
        this.inlineAssets = inlineAssets;
    }

    @Override
    public List<KnowledgeDoc> load(String moduleType) {
        String normalized = ModuleRagConfig.normalize(moduleType);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }
        List<KnowledgeDoc> cached = cache.get(normalized);
        if (cached != null) {
            return cached;
        }

        String assetPath = ModuleRagConfig.assetPathForModule(normalized);
        if (assetPath.isEmpty()) {
            Log.i(TAG, "No module KB configured for moduleType=" + normalized);
            cache.put(normalized, Collections.<KnowledgeDoc>emptyList());
            return Collections.emptyList();
        }

        List<KnowledgeDoc> docs = readDocs(assetPath, normalized);
        cache.put(normalized, docs);
        return docs;
    }

    private List<KnowledgeDoc> readDocs(String assetPath, String moduleType) {
        try (InputStream inputStream = openStream(assetPath)) {
            if (inputStream == null) {
                Log.w(TAG, "KB asset not found for moduleType=" + moduleType + " path=" + assetPath);
                return Collections.emptyList();
            }
            String json = readAll(inputStream);
            JSONArray array = new JSONArray(json);
            List<KnowledgeDoc> docs = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                docs.add(parseDoc(item, moduleType));
            }
            return Collections.unmodifiableList(docs);
        } catch (Exception e) {
            Log.w(TAG, "Failed to load KB for moduleType=" + moduleType + " path=" + assetPath, e);
            return Collections.emptyList();
        }
    }

    private InputStream openStream(String assetPath) throws Exception {
        if (inlineAssets != null) {
            String content = inlineAssets.get(assetPath);
            if (content != null) {
                return new ByteArrayInputStream(content.getBytes("UTF-8"));
            }
        }
        if (appContext == null) {
            return null;
        }
        return appContext.getAssets().open(assetPath);
    }

    private String readAll(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toString("UTF-8");
    }

    private KnowledgeDoc parseDoc(JSONObject item, String moduleType) {
        return new KnowledgeDoc(
                item.optString("id", ""),
                normalize(item.optString("module", moduleType)),
                normalize(item.optString("subModule", "")),
                item.optString("title", ""),
                item.optString("content", ""),
                normalize(item.optString("knowledgeType", "")),
                toStringList(item.optJSONArray("problemTags")),
                toStringList(item.optJSONArray("scenarioTags")),
                toStringList(item.optJSONArray("interactionGoals")),
                toStringList(item.optJSONArray("errorTypes")),
                toStringList(item.optJSONArray("targetSounds")),
                toStringList(item.optJSONArray("targetPositions")),
                toStringList(item.optJSONArray("goalTags")),
                toStringList(item.optJSONArray("applicableStages")),
                toStringList(item.optJSONArray("audience")),
                item.optInt("priority", 0),
                item.optString("source", "")
        );
    }

    private List<String> toStringList(JSONArray array) {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String value = normalize(array.optString(i, ""));
            if (!value.isEmpty()) {
                out.add(value);
            }
        }
        return out;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
