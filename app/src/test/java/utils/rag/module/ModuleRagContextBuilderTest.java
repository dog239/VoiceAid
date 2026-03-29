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
                syntaxHit("SYN-C-001", "comprehension", "理解策略", "支持复杂句理解"),
                syntaxHit("SYN-E-001", "expression", "表达策略", "支持句式扩展")
        ));

        assertTrue(context.contains("【句法理解相关建议】"));
        assertTrue(context.contains("【句法表达相关建议】"));
    }

    @Test
    public void buildSyntaxContext_shouldHandleSingleSubModuleOnly() {
        String context = builder.build(Collections.singletonList(
                syntaxHit("SYN-C-001", "comprehension", "理解策略", "支持复杂句理解")
        ));

        assertTrue(context.contains("【句法理解相关建议】"));
        assertTrue(context.contains("[1] 理解策略"));
    }

    @Test
    public void buildSyntaxContext_shouldTrimAndDeduplicate() {
        String context = builder.build(Arrays.asList(
                syntaxHit("SYN-C-001", "comprehension", "理解策略", repeat("内容", 30)),
                syntaxHit("SYN-C-001", "comprehension", "理解策略", repeat("内容", 30))
        ), 20);

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

    private String repeat(String text, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }
}
