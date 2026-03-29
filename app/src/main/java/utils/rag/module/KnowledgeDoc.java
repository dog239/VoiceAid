package utils.rag.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KnowledgeDoc {
    public final String id;
    public final String module;
    public final String title;
    public final String content;
    public final String knowledgeType;
    public final List<String> errorTypes;
    public final List<String> targetSounds;
    public final List<String> targetPositions;
    public final List<String> goalTags;
    public final List<String> applicableStages;
    public final List<String> audience;
    public final int priority;
    public final String source;

    public KnowledgeDoc(String id,
                        String module,
                        String title,
                        String content,
                        String knowledgeType,
                        List<String> errorTypes,
                        List<String> targetSounds,
                        List<String> targetPositions,
                        List<String> goalTags,
                        List<String> applicableStages,
                        List<String> audience,
                        int priority,
                        String source) {
        this.id = safe(id);
        this.module = safe(module);
        this.title = safe(title);
        this.content = safe(content);
        this.knowledgeType = safe(knowledgeType);
        this.errorTypes = immutableCopy(errorTypes);
        this.targetSounds = immutableCopy(targetSounds);
        this.targetPositions = immutableCopy(targetPositions);
        this.goalTags = immutableCopy(goalTags);
        this.applicableStages = immutableCopy(applicableStages);
        this.audience = immutableCopy(audience);
        this.priority = priority;
        this.source = safe(source);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static List<String> immutableCopy(List<String> source) {
        return source == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(source));
    }
}
