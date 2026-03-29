package utils.rag.module;

import java.util.List;

public interface ModuleKnowledgeRepository {
    List<KnowledgeDoc> load(String moduleType);
}
