package utils.rag.module;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RagQuery {
    public static final class GlobalQuery {
        public final List<String> goalTags;
        public final String severity;

        public GlobalQuery(List<String> goalTags, String severity) {
            this.goalTags = immutableCopy(goalTags);
            this.severity = severity == null ? "" : severity;
        }

        JSONObject toJson() {
            JSONObject out = new JSONObject();
            try {
                out.put("goalTags", toJsonArray(goalTags));
                out.put("severity", severity);
            } catch (JSONException ignored) {
            }
            return out;
        }

        boolean isEmpty() {
            return goalTags.isEmpty() && severity.trim().isEmpty();
        }
    }

    public static final class SubModuleQuery {
        public final String name;
        public final List<String> problemTags;
        public final List<String> goalTags;
        public final String difficulty;

        public SubModuleQuery(String name, List<String> problemTags, List<String> goalTags, String difficulty) {
            this.name = name == null ? "" : name;
            this.problemTags = immutableCopy(problemTags);
            this.goalTags = immutableCopy(goalTags);
            this.difficulty = difficulty == null ? "" : difficulty;
        }

        JSONObject toJson() {
            JSONObject out = new JSONObject();
            try {
                out.put("name", name);
                out.put("problemTags", toJsonArray(problemTags));
                out.put("goalTags", toJsonArray(goalTags));
                out.put("difficulty", difficulty);
            } catch (JSONException ignored) {
            }
            return out;
        }

        boolean isEmpty() {
            return name.trim().isEmpty()
                    && problemTags.isEmpty()
                    && goalTags.isEmpty()
                    && difficulty.trim().isEmpty();
        }
    }

    public final String moduleType;
    public final List<String> errorTypes;
    public final List<String> targetSounds;
    public final List<String> targetPositions;
    public final List<String> goalTags;
    public final String severity;
    public final GlobalQuery global;
    public final List<SubModuleQuery> subModules;

    public RagQuery(String moduleType,
                    List<String> errorTypes,
                    List<String> targetSounds,
                    List<String> targetPositions,
                    List<String> goalTags,
                    String severity) {
        this(moduleType,
                errorTypes,
                targetSounds,
                targetPositions,
                goalTags,
                severity,
                new GlobalQuery(goalTags, severity),
                Collections.<SubModuleQuery>emptyList());
    }

    public RagQuery(String moduleType,
                    List<String> errorTypes,
                    List<String> targetSounds,
                    List<String> targetPositions,
                    List<String> goalTags,
                    String severity,
                    GlobalQuery global,
                    List<SubModuleQuery> subModules) {
        this.moduleType = moduleType == null ? "" : moduleType;
        this.errorTypes = immutableCopy(errorTypes);
        this.targetSounds = immutableCopy(targetSounds);
        this.targetPositions = immutableCopy(targetPositions);
        this.goalTags = immutableCopy(goalTags);
        this.severity = severity == null ? "" : severity;
        this.global = global == null ? new GlobalQuery(goalTags, severity) : global;
        this.subModules = immutableSubModules(subModules);
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
            out.put("global", global.toJson());
            JSONArray subModuleArray = new JSONArray();
            for (SubModuleQuery subModule : subModules) {
                if (subModule != null && !subModule.isEmpty()) {
                    subModuleArray.put(subModule.toJson());
                }
            }
            out.put("subModules", subModuleArray);
        } catch (JSONException ignored) {
        }
        return out;
    }

    public boolean isEmpty() {
        return errorTypes.isEmpty()
                && targetSounds.isEmpty()
                && targetPositions.isEmpty()
                && goalTags.isEmpty()
                && severity.trim().isEmpty()
                && (global == null || global.isEmpty())
                && areSubModulesEmpty(subModules);
    }

    private static List<String> immutableCopy(List<String> source) {
        return source == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(source));
    }

    private static List<SubModuleQuery> immutableSubModules(List<SubModuleQuery> source) {
        return source == null
                ? Collections.<SubModuleQuery>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(source));
    }

    private static boolean areSubModulesEmpty(List<SubModuleQuery> source) {
        if (source == null || source.isEmpty()) {
            return true;
        }
        for (SubModuleQuery query : source) {
            if (query != null && !query.isEmpty()) {
                return false;
            }
        }
        return true;
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
