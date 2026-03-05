package kb;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KbMetaLoader {
    private static final Pattern KB_VERSION_PATTERN = Pattern.compile("\\\"kb_version\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private static final Pattern BUILD_TIME_PATTERN = Pattern.compile("\\\"build_time\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");

    private KbMetaLoader() {
    }

    public static KbMeta loadFromMainAssets() {
        Path first = Paths.get("app/src/main/assets/kb/kb_meta.json").toAbsolutePath().normalize();
        if (Files.exists(first)) {
            return load(first);
        }

        Path second = Paths.get("src/main/assets/kb/kb_meta.json").toAbsolutePath().normalize();
        if (Files.exists(second)) {
            return load(second);
        }
        return KbMeta.EMPTY;
    }

    public static KbMeta loadFromAssets(Context context) {
        if (context == null) {
            return KbMeta.EMPTY;
        }
        try (InputStream inputStream = context.getAssets().open("kb/kb_meta.json")) {
            String content = readUtf8(inputStream);
            return parseContent(content);
        } catch (Exception ignored) {
            return KbMeta.EMPTY;
        }
    }

    public static KbMeta load(Path path) {
        if (path == null) {
            return KbMeta.EMPTY;
        }
        try {
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return KbMeta.EMPTY;
            }
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return parseContent(content);
        } catch (Exception ignored) {
            return KbMeta.EMPTY;
        }
    }

    private static KbMeta parseContent(String content) {
        String kbVersion = matchString(content, KB_VERSION_PATTERN);
        String buildTime = matchString(content, BUILD_TIME_PATTERN);
        if (kbVersion.isEmpty() || buildTime.isEmpty()) {
            return KbMeta.EMPTY;
        }
        return new KbMeta(kbVersion, buildTime);
    }

    private static String readUtf8(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        byte[] buffer = new byte[1024];
        int read;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((read = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private static String matchString(String content, Pattern pattern) {
        if (content == null || pattern == null) {
            return "";
        }
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return "";
        }
        String value = matcher.group(1);
        return value == null ? "" : value.trim();
    }
}
