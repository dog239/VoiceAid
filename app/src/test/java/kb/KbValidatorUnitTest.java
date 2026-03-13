package kb;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KbValidatorUnitTest {

    @Test
    public void duplicateId_shouldFail() {
        List<String> lines = Arrays.asList(
                "{\"id\":\"same\",\"module\":\"A\",\"skill_tag\":[\"error_type\"],\"text\":\"t1\"}",
                "{\"id\":\"same\",\"module\":\"E\",\"skill_tag\":[\"training_needed\"],\"text\":\"t2\"}"
        );

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "duplicate id");
    }

    @Test
    public void missingRequiredField_shouldFail() {
        List<String> lines = Arrays.asList(
                "{\"id\":\"kb-1\",\"module\":\"A\",\"skill_tag\":[\"error_type\"]}",
                "{\"id\":\"kb-2\",\"skill_tag\":[\"training_needed\"],\"text\":\"content\"}"
        );

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "field=text");
        assertContains(result.errors, "field=module");
    }

    @Test
    public void invalidModule_shouldFail() {
        List<String> lines = Collections.singletonList(
                "{\"id\":\"kb-1\",\"module\":\"XYZ\",\"skill_tag\":[\"error_type\"],\"text\":\"content\"}"
        );

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "invalid module");
    }

    @Test
    public void skillTagNotArray_shouldFail() {
        List<String> lines = Collections.singletonList(
                "{\"id\":\"kb-1\",\"module\":\"A\",\"skill_tag\":\"a\",\"text\":\"content\"}"
        );

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "skill_tag must be an array");
    }

    @Test
    public void skillTagSizeOutOfRange_shouldFail() {
        List<String> emptyTags = Collections.singletonList(
                "{\"id\":\"kb-1\",\"module\":\"A\",\"skill_tag\":[],\"text\":\"content\"}"
        );
        KbValidator.ValidationResult emptyResult = KbValidator.validateLines(emptyTags);
        assertFalse(emptyResult.ok);
        assertContains(emptyResult.errors, "size must be between 1 and 3");

        List<String> tooManyTags = Collections.singletonList(
                "{\"id\":\"kb-2\",\"module\":\"A\",\"skill_tag\":[\"a\",\"b\",\"c\",\"d\"],\"text\":\"content\"}"
        );
        KbValidator.ValidationResult tooManyResult = KbValidator.validateLines(tooManyTags);
        assertFalse(tooManyResult.ok);
        assertContains(tooManyResult.errors, "size must be between 1 and 3");
    }

    @Test
    public void invalidJsonLine_shouldFail() {
        List<String> lines = Collections.singletonList("{bad json");

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "line 1");
        assertContains(result.errors, "invalid JSON");
    }

    @Test
    public void invalidOptionalLevel_shouldFail() {
        List<String> lines = Collections.singletonList(
                "{\"id\":\"kb-1\",\"module\":\"A\",\"skill_tag\":[\"error_type\"],\"text\":\"content\",\"level\":\"hard\"}"
        );

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "invalid level");
    }

    @Test
    public void lowercaseModule_shouldPassAfterNormalization() {
        List<String> lines = Collections.singletonList(
                "{\"id\":\" kb-1 \",\"module\":\" a \",\"skill_tag\":[\"error_type\"],\"text\":\"content\"}"
        );

        KbValidator.ParsedResult parsed = KbValidator.validateAndParseLines(lines);
        assertTrue(parsed.result.ok);
        assertTrue(parsed.result.errors.isEmpty());
        assertTrue(!parsed.items.isEmpty());
        assertTrue("A".equals(parsed.items.get(0).module));
        assertTrue("kb-1".equals(parsed.items.get(0).id));
    }

    @Test
    public void duplicateSkillTags_shouldDedupAndPassWhenRangeValid() {
        List<String> lines = Collections.singletonList(
                "{\"id\":\"kb-1\",\"module\":\"A\",\"skill_tag\":[\"a\",\"a\"],\"text\":\"content\"}"
        );

        KbValidator.ParsedResult parsed = KbValidator.validateAndParseLines(lines);
        assertTrue(parsed.result.ok);
        assertTrue(parsed.result.errors.isEmpty());
        assertTrue(parsed.items.size() == 1);
        assertTrue(parsed.items.get(0).skillTags.size() == 1);
        assertTrue("a".equals(parsed.items.get(0).skillTags.get(0)));
    }

    @Test
    public void duplicateSkillTags_shouldFailWhenUniqueCountOutOfRange() {
        List<String> lines = Collections.singletonList(
                "{\"id\":\"kb-2\",\"module\":\"A\",\"skill_tag\":[\"a\",\"b\",\"c\",\"d\",\"d\"],\"text\":\"content\"}"
        );

        KbValidator.ValidationResult result = KbValidator.validateLines(lines);
        assertFalse(result.ok);
        assertContains(result.errors, "size must be between 1 and 3");
    }

    private static void assertContains(List<String> errors, String keyword) {
        for (String error : errors) {
            if (error != null && error.contains(keyword)) {
                return;
            }
        }
        throw new AssertionError("expected errors to contain keyword: " + keyword + ", actual: " + errors);
    }
}
