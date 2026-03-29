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
        String context = builder.build(Arrays.asList(hit("ART-001", "构音替代错误干预原则", "先稳定词首，再扩展到短语。")));

        assertTrue(context.contains("【检索参考知识】"));
        assertTrue(context.contains("[1] 构音替代错误干预原则"));
    }

    @Test
    public void build_shouldReturnEmpty_whenHitsEmpty() {
        assertEquals("", builder.build(Collections.<RagHit>emptyList()));
    }

    @Test
    public void build_shouldDeduplicateHits() {
        String context = builder.build(Arrays.asList(
                hit("ART-001", "标题A", "内容A"),
                hit("ART-001", "标题A", "内容A")
        ));

        assertEquals(1, context.split("\\[1\\]").length - 1);
    }

    @Test
    public void build_shouldTrimLongContent() {
        String context = builder.build(Arrays.asList(hit("ART-002", "标题B", repeat("长内容", 80))), 20);

        assertTrue(context.contains("..."));
    }

    private RagHit hit(String id, String title, String content) {
        return new RagHit(
                new KnowledgeDoc(id, "articulation", title, content, Collections.singletonList("替代"), Collections.singletonList("k"), 1, "test"),
                1.0d,
                Collections.singletonList("替代")
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
