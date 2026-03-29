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
        String context = builder.build(Arrays.asList(hit("ART-001", "Substitution principle", "Stabilize initial-position target sounds first.")));

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
                hit("ART-001", "Title A", "Content A"),
                hit("ART-001", "Title A", "Content A")
        ));

        assertEquals(1, context.split("\\[1\\]").length - 1);
    }

    @Test
    public void build_shouldTrimLongContent() {
        String context = builder.build(Arrays.asList(hit("ART-002", "Title B", repeat("LongContent", 20))), 20);

        assertTrue(context.contains("..."));
    }

    private RagHit hit(String id, String title, String content) {
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

    private String repeat(String text, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }
}
