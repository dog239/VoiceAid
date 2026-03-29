package utils.rag.module;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RagQuery {
    public final String moduleType;
    public final List<String> errorTypes;
    public final List<String> targetSounds;
    public final List<String> targetPositions;
    public final List<String> goalTags;
    public final String severity;

    public RagQuery(String moduleType,
                    List<String> errorTypes,
                    List<String> targetSounds,
                    List<String> targetPositions,
                    List<String> goalTags,
                    String severity) {
        this.moduleType = moduleType == null ? "" : moduleType;
        this.errorTypes = immutableCopy(errorTypes);
        this.targetSounds = immutableCopy(targetSounds);
        this.targetPositions = immutableCopy(targetPositions);
        this.goalTags = immutableCopy(goalTags);
        this.severity = severity == null ? "" : severity;
    }

    public JSONObject toJson() {
        JSONObject out = new JSONObject();
        try {
            out.put("moduleType", moduleType);
            out.put("errorTypes", toJsonArray(errorTypes));
            out.put("targetSounds", toJsonArray(targetSounds));
            out.put("targetPositions", toJsonArray(targetPositions));
            out.put("goalTags", toJsonArray(goalTags));
            out.put("severity", severity);
        } catch (JSONException ignored) {
        }
        return out;
    }

    public boolean isEmpty() {
        return errorTypes.isEmpty()
                && targetSounds.isEmpty()
                && targetPositions.isEmpty()
                && goalTags.isEmpty()
                && severity.trim().isEmpty();
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
