package kb;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KbMetaLoaderUnitTest {

    @Test
    public void loadFromMainAssets_readsKbMetaJson() {
        KbMeta meta = KbMetaLoader.loadFromMainAssets();

        assertNotNull(meta);
        assertTrue(meta.kbVersion != null && !meta.kbVersion.trim().isEmpty());
        assertTrue(meta.buildTime != null && !meta.buildTime.trim().isEmpty());
    }

    @Test
    public void loadFromMainAssets_valuesMatchExpectedAsset() {
        KbMeta meta = KbMetaLoader.loadFromMainAssets();

        assertEquals("assets_main", meta.kbVersion);
        assertEquals("2026-03-05T18:30:00Z", meta.buildTime);
    }

    @Test
    public void load_missingFile_returnsEmptyWithoutCrash() {
        Path missing = Paths.get("app/src/main/assets/kb/not_exists_kb_meta.json").toAbsolutePath().normalize();

        KbMeta meta = KbMetaLoader.load(missing);

        assertNotNull(meta);
        assertEquals("", meta.kbVersion);
        assertEquals("", meta.buildTime);
    }

    @Test
    public void load_parseSupportsWhitespaceAndNewlines() throws Exception {
        Path temp = Files.createTempFile("kb_meta", ".json");
        try {
            String content = "{\n  \"kb_version\"  :  \"v_test\" ,\n\n  \"build_time\" : \"2026-03-05T00:00:00Z\"\n}";
            Files.write(temp, content.getBytes(StandardCharsets.UTF_8));

            KbMeta meta = KbMetaLoader.load(temp);

            assertEquals("v_test", meta.kbVersion);
            assertEquals("2026-03-05T00:00:00Z", meta.buildTime);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
