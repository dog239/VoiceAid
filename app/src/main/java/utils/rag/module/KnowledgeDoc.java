package utils.rag.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KnowledgeDoc {
    public final String id;
    public final String module;
    public final String title;
    public final String content;
    public final List<String> tags;
    public final List<String> subtypes;
    public final int priority;
    public final String source;

    public KnowledgeDoc(String id,
                        String module,
                        String title,
                        String content,
                        List<String> tags,
                        List<String> subtypes,
                        int priority,
                        String source) {
        this.id = id == null ? "" : id;
        this.module = module == null ? "" : module;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.tags = tags == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(tags));
        this.subtypes = subtypes == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(subtypes));
        this.priority = priority;
        this.source = source == null ? "" : source;
    }
}
