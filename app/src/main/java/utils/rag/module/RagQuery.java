package utils.rag.module;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RagQuery {
    public final String moduleType;
    public final List<String> problemTags;
    public final List<String> goalTags;
    public final List<String> weakPoints;
    public final String severity;

    public RagQuery(String moduleType,
                    List<String> problemTags,
                    List<String> goalTags,
                    List<String> weakPoints,
                    String severity) {
        this.moduleType = moduleType == null ? "" : moduleType;
        this.problemTags = immutableCopy(problemTags);
        this.goalTags = immutableCopy(goalTags);
        this.weakPoints = immutableCopy(weakPoints);
        this.severity = severity == null ? "" : severity;
    }

    public JSONObject toJson() {
        JSONObject out = new JSONObject();
        try {
            out.put("moduleType", moduleType);
            out.put("problemTags", toJsonArray(problemTags));
            out.put("goalTags", toJsonArray(goalTags));
            out.put("weakPoints", toJsonArray(weakPoints));
            out.put("severity", severity);
        } catch (JSONException ignored) {
        }
        return out;
    }

    public boolean isEmpty() {
        return problemTags.isEmpty() && goalTags.isEmpty() && weakPoints.isEmpty() && severity.trim().isEmpty();
    }

    private static List<String> immutableCopy(List<String> source) {
        return source == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(source));
    }

    private static JSONArray toJsonArray(List<String> values) {
        JSONArray out = new JSONArray();
        if (values == null) {
            return out;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                out.put(value.trim());
            }
        }
        return out;
    }
}
