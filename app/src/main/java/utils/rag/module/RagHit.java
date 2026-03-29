package utils.rag.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RagHit {
    public final KnowledgeDoc doc;
    public final double score;
    public final String subModuleName;
    public final List<String> matchedTags;

    public RagHit(KnowledgeDoc doc, double score, List<String> matchedTags) {
        this(doc, score, doc == null ? "" : doc.subModule, matchedTags);
    }

    public RagHit(KnowledgeDoc doc, double score, String subModuleName, List<String> matchedTags) {
        this.doc = doc;
        this.score = score;
        this.subModuleName = subModuleName == null ? "" : subModuleName;
        this.matchedTags = matchedTags == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(matchedTags));
    }
}
