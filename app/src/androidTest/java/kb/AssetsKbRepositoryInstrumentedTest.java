package kb;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AssetsKbRepositoryInstrumentedTest {

    @Test
    public void test_read_androidTest_assets_and_skip_bad_line() {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetsKbRepository repository = new AssetsKbRepository(testContext);

        List<KbStrategy> aErrorTypeTop3 = repository.query("A", "error_type", 3);
        assertTrue(aErrorTypeTop3.size() > 0);
        assertTrue(aErrorTypeTop3.size() <= 3);
        for (KbStrategy strategy : aErrorTypeTop3) {
            assertEquals("A", strategy.module);
            assertTrue(containsIgnoreCase(strategy.skillTags, "error_type"));
        }

        assertTrue(repository.query(null, "error_type", 3).isEmpty());
        assertTrue(repository.query("A", "", 3).isEmpty());
        assertTrue(repository.query("A", "error_type", 0).isEmpty());

        List<KbStrategy> vocabExpression = repository.query("vocab", "EXPRESSION", 10);
        assertFalse(vocabExpression.isEmpty());
        for (KbStrategy strategy : vocabExpression) {
            assertEquals("VOCAB", strategy.module);
            assertTrue(containsIgnoreCase(strategy.skillTags, "expression"));
        }

        List<KbStrategy> aErrorTypeTop1 = repository.query("A", "error_type", 1);
        assertEquals(1, aErrorTypeTop1.size());

        repository.query("A", "error_type", 3);
        repository.query("VOCAB", "expression", 5);
        assertEquals(1, repository.getLoadCountForTest());
    }

    @Test
    public void test_read_main_assets_strategies_jsonl() {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AssetsKbRepository repository = new AssetsKbRepository(targetContext);

        List<KbStrategy> result = repository.query("A", "error_type", 1);
        Assume.assumeTrue(
                "main assets kb/strategies.jsonl should include at least one A+error_type record for strict non-empty assertion",
                !result.isEmpty()
        );
        assertEquals(1, result.size());
        assertEquals(1, repository.getLoadCountForTest());
    }

    private static boolean containsIgnoreCase(List<String> values, String expected) {
        if (values == null || expected == null) {
            return false;
        }
        for (String value : values) {
            if (expected.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
