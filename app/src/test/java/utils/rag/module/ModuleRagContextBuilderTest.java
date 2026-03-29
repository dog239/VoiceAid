package utils.rag.module;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleRagContextBuilderTest {
    private final ModuleRagContextBuilder builder = new ModuleRagContextBuilder();

    @Test
    public void build_shouldFormatHitsAsContext() {
        String context = builder.build(Arrays.asList(
                articulationHit("ART-001", "Substitution principle", "Stabilize initial-position target sounds first.")
        ));

        assertTrue(context.contains("【检索参考知识】"));
        assertTrue(context.contains("[1] Substitution principle"));
    }

    @Test
    public void build_shouldReturnEmpty_whenHitsEmpty() {
        assertEquals("", builder.build(Collections.<RagHit>emptyList()));
    }

    @Test
    public void build_shouldDeduplicateHits() {
        String context = builder.build(Arrays.asList(
                articulationHit("ART-001", "Title A", "Content A"),
                articulationHit("ART-001", "Title A", "Content A")
        ));

        assertEquals(1, context.split("\\[1\\]").length - 1);
    }

    @Test
    public void build_shouldTrimLongContent() {
        String context = builder.build(Arrays.asList(
                articulationHit("ART-002", "Title B", repeat("LongContent", 20))
        ), 20);

        assertTrue(context.contains("..."));
    }

    @Test
    public void buildSyntaxContext_shouldGroupHitsBySubModule() {
        String context = builder.build(Arrays.asList(
                syntaxHit("SYN-C-001", "comprehension", "Comprehension strategy", "Support complex sentence comprehension."),
                syntaxHit("SYN-E-001", "expression", "Expression strategy", "Support sentence variety expansion.")
        ));

        assertTrue(context.contains("【句法理解相关建议】"));
        assertTrue(context.contains("【句法表达相关建议】"));
    }

    @Test
    public void buildSyntaxContext_shouldHandleSingleSubModuleOnly() {
        String context = builder.build(Collections.singletonList(
                syntaxHit("SYN-C-001", "comprehension", "Comprehension strategy", "Support complex sentence comprehension.")
        ));

        assertTrue(context.contains("【句法理解相关建议】"));
        assertTrue(context.contains("[1] Comprehension strategy"));
    }

    @Test
    public void buildSyntaxContext_shouldTrimAndDeduplicate() {
        String context = builder.build(Arrays.asList(
                syntaxHit("SYN-C-001", "comprehension", "Comprehension strategy", repeat("Content", 30)),
                syntaxHit("SYN-C-001", "comprehension", "Comprehension strategy", repeat("Content", 30))
        ), 20);

        assertTrue(context.contains("..."));
        assertEquals(1, context.split("\\[1\\]").length - 1);
    }

    @Test
    public void buildVocabularyContext_shouldGroupHitsBySubModule() {
        String context = builder.build(Arrays.asList(
                vocabularyHit("VOC-R-001", "receptive", "Receptive support", "Support semantic understanding and category mapping."),
                vocabularyHit("VOC-E-001", "expressive", "Expressive support", "Support naming and lexical retrieval."),
                vocabularyHit("VOC-G-001", "", "General support", "Use lighter load and repetition in daily routines.")
        ));

        assertTrue(context.contains("【词汇理解相关建议】"));
        assertTrue(context.contains("【词汇表达相关建议】"));
        assertTrue(context.contains("【词汇训练通用建议】"));
    }

    @Test
    public void buildVocabularyContext_shouldHandleSingleSubModuleOnly() {
        String context = builder.build(Collections.singletonList(
                vocabularyHit("VOC-R-001", "receptive", "Receptive support", "Support semantic understanding and category mapping.")
        ));

        assertTrue(context.contains("【词汇理解相关建议】"));
        assertTrue(context.contains("[1] Receptive support"));
    }

    @Test
    public void buildVocabularyContext_shouldKeepSupportingSignalsInGeneralSection() {
        String context = builder.build(Arrays.asList(
                vocabularyHit("VOC-G-001", "", "Load management", "Use repetition and phonological memory support under task load.")
        ));

        assertTrue(context.contains("【词汇训练通用建议】"));
        assertTrue(!context.contains("【RE"));
        assertTrue(!context.contains("【NWR"));
    }

    @Test
    public void buildVocabularyContext_shouldTrimAndDeduplicate() {
        String context = builder.build(Arrays.asList(
                vocabularyHit("VOC-E-001", "expressive", "Expressive support", repeat("VocabularyContent", 20)),
                vocabularyHit("VOC-E-001", "expressive", "Expressive support", repeat("VocabularyContent", 20))
        ), 30);

        assertTrue(context.contains("..."));
        assertEquals(1, context.split("\\[1\\]").length - 1);
    }

    private RagHit articulationHit(String id, String title, String content) {
        return new RagHit(
                new KnowledgeDoc(
                        id,
                        "articulation",
                        title,
                        content,
                        "training_strategy",
                        Collections.singletonList("substitution"),
                        Collections.singletonList("k"),
                        Collections.singletonList("initial"),
                        Collections.singletonList("stabilization"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("therapist"),
                        1,
                        "test"
                ),
                1.0d,
                Collections.singletonList("error:substitution")
        );
    }

    private RagHit syntaxHit(String id, String subModule, String title, String content) {
        return new RagHit(
                new KnowledgeDoc(
                        id,
                        "syntax",
                        subModule,
                        title,
                        content,
                        "training_strategy",
                        Collections.singletonList(subModule + "_problem"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("syntax_" + subModule + "_support"),
                        Collections.singletonList("sentence_level"),
                        Collections.singletonList("therapist"),
                        1,
                        "test"
                ),
                1.0d,
                subModule,
                Collections.singletonList("problem:" + subModule)
        );
    }

    private RagHit vocabularyHit(String id, String subModule, String title, String content) {
        return new RagHit(
                new KnowledgeDoc(
                        id,
                        "vocabulary",
                        subModule,
                        title,
                        content,
                        "training_strategy",
                        Collections.singletonList("semantic_comprehension_difficulty"),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.singletonList("receptive_vocabulary_support"),
                        Collections.singletonList("word_level"),
                        Collections.singletonList("therapist"),
                        1,
                        "test"
                ),
                1.0d,
                subModule,
                Collections.singletonList("problem:" + subModule)
        );
    }

    private String repeat(String text, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }
}
