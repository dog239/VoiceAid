package kb;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KbRepositoryFilterUnitTest {

    @Test
    public void query_filtersByModuleAndTag_caseInsensitive_andHonorsTopK() {
        List<KbStrategy> seed = Arrays.asList(
                new KbStrategy("1", "A", Arrays.asList("error_type", "x"), "t1", "B1", "text1", "dn1"),
                new KbStrategy("2", "A", Arrays.asList("Error_Type"), "t2", "B1", "text2", "dn2"),
                new KbStrategy("3", "VOCAB", Arrays.asList("EXPRESSION"), "t3", "B1", "text3", "dn3"),
                new KbStrategy("4", "A", Arrays.asList("other"), "t4", "B1", "text4", "dn4")
        );

        AssetsKbRepository repository = new AssetsKbRepository(seed);

        List<KbStrategy> aErrorType = repository.query("a", "ERROR_TYPE", 10);
        assertEquals(2, aErrorType.size());
        assertEquals("1", aErrorType.get(0).id);
        assertEquals("2", aErrorType.get(1).id);

        List<KbStrategy> vocabExpression = repository.query("vocab", "expression", 10);
        assertEquals(1, vocabExpression.size());
        assertEquals("3", vocabExpression.get(0).id);

        List<KbStrategy> top1 = repository.query("A", "error_type", 1);
        assertEquals(1, top1.size());
        assertEquals("1", top1.get(0).id);

        assertTrue(repository.query(null, "error_type", 3).isEmpty());
        assertTrue(repository.query("A", "", 3).isEmpty());
        assertTrue(repository.query("A", "error_type", 0).isEmpty());
    }
}
