package kb;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class AssetsKbRepository implements KbRepository {
    private static final String TAG = "AssetsKbRepository";
    private static final String ASSET_PATH = "kb/strategies.jsonl";

    private final Context appContext;
    private final AtomicInteger loadCount = new AtomicInteger(0);

    private volatile List<KbStrategy> cache;
    private volatile boolean initialized;

    public AssetsKbRepository(Context context) {
        this.appContext = context == null ? null : context.getApplicationContext();
    }

    AssetsKbRepository(List<KbStrategy> preloadedStrategies) {
        this.appContext = null;
        this.cache = preloadedStrategies == null
                ? Collections.<KbStrategy>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(preloadedStrategies));
        this.initialized = true;
    }

    @Override
    public List<KbStrategy> query(String module, String tag, int topK) {
        if (topK <= 0 || isBlank(module) || isBlank(tag)) {
            return Collections.emptyList();
        }

        ensureLoaded();

        List<KbStrategy> source = cache;
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        String moduleNormalized = module.trim().toLowerCase(Locale.ROOT);
        String tagNormalized = tag.trim().toLowerCase(Locale.ROOT);

        List<KbStrategy> matched = new ArrayList<>();
        for (KbStrategy strategy : source) {
            if (strategy == null) {
                continue;
            }
            if (!moduleNormalized.equals(normalize(strategy.module))) {
                continue;
            }
            if (!containsTag(strategy.skillTags, tagNormalized)) {
                continue;
            }
            matched.add(strategy);
            if (matched.size() >= topK) {
                break;
            }
        }
        return matched;
    }

    int getLoadCountForTest() {
        return loadCount.get();
    }

    private void ensureLoaded() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            cache = loadFromAssets();
            initialized = true;
        }
    }

    private List<KbStrategy> loadFromAssets() {
        if (appContext == null) {
            Log.w(TAG, "Context is null, skip loading KB");
            return Collections.emptyList();
        }

        loadCount.incrementAndGet();

        List<KbStrategy> result = new ArrayList<>();
        try (InputStream inputStream = appContext.getAssets().open(ASSET_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            result.addAll(parseJsonlLines(reader));
        } catch (Exception e) {
            Log.e(TAG, "Failed to load KB from assets: " + ASSET_PATH, e);
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(result);
    }

    static List<KbStrategy> parseJsonlLines(BufferedReader reader) throws IOException {
        List<KbStrategy> result = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line == null ? "" : line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            KbStrategy strategy = parseLine(trimmedLine);
            if (strategy != null) {
                result.add(strategy);
            }
        }
        return result;
    }

    private static KbStrategy parseLine(String line) {
        try {
            JSONObject object = new JSONObject(line);
            String id = object.optString("id", "");
            String module = object.optString("module", "");
            JSONArray tagsJson = object.optJSONArray("skill_tag");
            List<String> skillTags = new ArrayList<>();
            if (tagsJson != null) {
                for (int i = 0; i < tagsJson.length(); i++) {
                    String tag = tagsJson.optString(i, "");
                    if (!isBlank(tag)) {
                        skillTags.add(tag);
                    }
                }
            }
            String title = object.optString("title", "");
            String level = object.optString("level", "");
            String text = object.optString("text", "");
            String doNot = object.optString("do_not", "");

            return new KbStrategy(id, module, skillTags, title, level, text, doNot);
        } catch (Exception e) {
            Log.w(TAG, "Skip invalid jsonl record", e);
            return null;
        }
    }

    private static boolean containsTag(List<String> tags, String expectedTag) {
        if (tags == null || tags.isEmpty() || isBlank(expectedTag)) {
            return false;
        }
        for (String t : tags) {
            if (expectedTag.equals(normalize(t))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
