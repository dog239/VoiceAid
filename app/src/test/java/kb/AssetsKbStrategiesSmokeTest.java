package kb;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssetsKbStrategiesSmokeTest {

    private static final String RELATIVE_ASSET_PATH = "app/src/main/assets/kb/strategies.jsonl";
    private static final int MIN_EXPECTED_LINES = 20;

    @Test
    public void mainStrategiesJsonl_isNonEmptyAndJsonValidAndCoversCorePairs() throws Exception {
        Path file = resolveMainStrategiesPath();
        assertNotNull("main assets strategies.jsonl should exist", file);
        assertTrue("main assets strategies.jsonl should exist", Files.exists(file));

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertNotNull(lines);
        assertTrue("strategies.jsonl should contain at least " + MIN_EXPECTED_LINES + " lines", lines.size() >= MIN_EXPECTED_LINES);

        KbValidator.ParsedResult parsed = KbValidator.validateAndParseLines(lines);
        KbValidator.ValidationResult result = parsed.result;
        assertTrue("KB validation must pass, errors: " + result.errors, result.ok);
        assertTrue("KB validation errors must be empty", result.errors.isEmpty());

        List<KbStrategy> items = parsed.items;
        assertTrue("parsed items should not be empty when validation passes", !items.isEmpty());

        Set<String> pairs = new LinkedHashSet<>();
        for (KbStrategy item : items) {
            if (item == null || item.skillTags == null) {
                continue;
            }
            for (String tag : item.skillTags) {
                if (item.module != null && !item.module.trim().isEmpty()
                        && tag != null && !tag.trim().isEmpty()) {
                    String module = item.module.trim();
                    pairs.add(module + "|" + tag);
                }
            }
        }

        assertTrue("missing pair A|error_type", pairs.contains("A|error_type"));
        assertTrue("missing pair A|phonology_process", pairs.contains("A|phonology_process"));
        assertTrue("missing pair E|training_needed", pairs.contains("E|training_needed"));
        assertTrue("missing pair S|training_needed", pairs.contains("S|training_needed"));
        assertTrue("missing pair NWR|nonword_repetition", pairs.contains("NWR|nonword_repetition"));
        assertTrue("missing pair PST|low_score or PN|low_score",
                pairs.contains("PST|low_score") || pairs.contains("PN|low_score"));
    }

    private static Path resolveMainStrategiesPath() {
        Path first = Paths.get(RELATIVE_ASSET_PATH).toAbsolutePath().normalize();
        if (Files.exists(first)) {
            return first;
        }

        Path second = Paths.get("src/main/assets/kb/strategies.jsonl").toAbsolutePath().normalize();
        if (Files.exists(second)) {
            return second;
        }
        return first;
    }
}
