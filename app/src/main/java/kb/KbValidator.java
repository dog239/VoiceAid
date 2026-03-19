package kb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class KbValidator {

    private static final Set<String> ALLOWED_MODULES = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList("A", "E", "RE", "S", "RG", "NWR", "PN", "PST", "PL", "SOCIAL"))
    );

    private static final Set<String> ALLOWED_LEVELS = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList("basic", "intermediate", "advanced"))
    );

    private KbValidator() {
    }

    public static ValidationResult validateFile(Path file) throws IOException {
        if (file == null) {
            return new ValidationResult(false,
                    Collections.singletonList("line 0: file is null"),
                    0,
                    0);
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        return validateLines(lines);
    }

    public static ValidationResult validateLines(List<String> jsonlLines) {
        return validateAndParse(jsonlLines, false).result;
    }

    public static ParsedResult validateAndParseLines(List<String> jsonlLines) {
        return validateAndParse(jsonlLines, true);
    }

    public static List<KbStrategy> parseValidItems(Path file) throws IOException {
        if (file == null) {
            return Collections.emptyList();
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        return parseValidItems(lines);
    }

    public static List<KbStrategy> parseValidItems(List<String> jsonlLines) {
        ParsedResult parsedResult = validateAndParseLines(jsonlLines);
        if (!parsedResult.result.ok) {
            return Collections.emptyList();
        }
        return parsedResult.items;
    }

    private static ParsedResult validateAndParse(List<String> lines, boolean buildItems) {
        if (lines == null) {
            ValidationResult result = new ValidationResult(false,
                    Collections.singletonList("line 0: jsonl lines are null"),
                    0,
                    0);
            return new ParsedResult(result, Collections.<KbStrategy>emptyList());
        }

        List<String> errors = new ArrayList<>();
        List<KbStrategy> items = buildItems ? new ArrayList<KbStrategy>() : Collections.<KbStrategy>emptyList();
        int validCount = 0;
        int lineCount = lines.size();
        Map<String, Integer> idToFirstLine = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            int lineNo = i + 1;
            String raw = lines.get(i);
            String trimmed = raw == null ? "" : raw.trim();
            boolean lineOk = true;

            if (trimmed.isEmpty()) {
                errors.add(formatError(lineNo, "blank line is not allowed", null));
                continue;
            }

            Map<String, Object> object;
            try {
                object = new JsonParser(trimmed).parseObjectLine();
            } catch (Exception e) {
                errors.add(formatError(lineNo, "invalid JSON object", null));
                continue;
            }

            String id = normalizeId(readRequiredString(object, "id", lineNo, errors));
            if (id.isEmpty()) {
                lineOk = false;
            } else {
                Integer firstLine = idToFirstLine.get(id);
                if (firstLine != null) {
                    errors.add(formatError(lineNo,
                            "duplicate id '" + id + "' (first seen at line " + firstLine + ")",
                            "id"));
                    lineOk = false;
                } else {
                    idToFirstLine.put(id, lineNo);
                }
            }

            String rawModule = readRequiredString(object, "module", lineNo, errors);
            String module = normalizeModule(rawModule);
            if (module.isEmpty()) {
                lineOk = false;
            } else if (!ALLOWED_MODULES.contains(module)) {
                errors.add(formatError(lineNo,
                        "invalid module '" + rawModule + "' -> '" + module + "'",
                        "module"));
                lineOk = false;
            }

            String text = readRequiredString(object, "text", lineNo, errors);
            if (text.isEmpty()) {
                lineOk = false;
            }

            List<String> skillTags = readSkillTags(object, lineNo, errors);
            if (skillTags == null) {
                lineOk = false;
            }

            if (object.containsKey("level") && object.get("level") != null) {
                Object levelObj = object.get("level");
                if (!(levelObj instanceof String)) {
                    errors.add(formatError(lineNo, "level must be a string", "level"));
                    lineOk = false;
                } else {
                    String level = ((String) levelObj).trim().toLowerCase(Locale.ROOT);
                    if (!level.isEmpty() && !ALLOWED_LEVELS.contains(level)) {
                        errors.add(formatError(lineNo, "invalid level '" + levelObj + "'", "level"));
                        lineOk = false;
                    }
                }
            }

            if (object.containsKey("do_not") && object.get("do_not") != null) {
                Object doNotObj = object.get("do_not");
                if (!(doNotObj instanceof String)) {
                    errors.add(formatError(lineNo, "do_not must be a string", "do_not"));
                    lineOk = false;
                }
            }

            if (lineOk) {
                validCount++;
                if (buildItems) {
                    items.add(toKbStrategy(object, id, module, text, skillTags));
                }
            }
        }

        List<KbStrategy> finalItems = buildItems
                ? Collections.unmodifiableList(items)
                : Collections.<KbStrategy>emptyList();

        ValidationResult result = new ValidationResult(errors.isEmpty(),
                Collections.unmodifiableList(errors),
                lineCount,
                validCount);
        return new ParsedResult(result, finalItems);
    }

    private static KbStrategy toKbStrategy(Map<String, Object> object,
                                           String id,
                                           String module,
                                           String text,
                                           List<String> tags) {
        String title = stringOrEmpty(object.get("title"));
        String level = stringOrEmpty(object.get("level"));
        String doNot = stringOrEmpty(object.get("do_not"));
        return new KbStrategy(id, module, tags, title, level, text, doNot);
    }

    private static List<String> readSkillTags(Map<String, Object> object, int lineNo, List<String> errors) {
        if (!object.containsKey("skill_tag") || object.get("skill_tag") == null) {
            errors.add(formatError(lineNo, "missing required field", "skill_tag"));
            return null;
        }

        Object raw = object.get("skill_tag");
        if (!(raw instanceof List)) {
            errors.add(formatError(lineNo, "skill_tag must be an array", "skill_tag"));
            return null;
        }

        List<?> array = (List<?>) raw;
        LinkedHashSet<String> dedup = new LinkedHashSet<>();
        for (int i = 0; i < array.size(); i++) {
            Object tagObj = array.get(i);
            if (!(tagObj instanceof String) || ((String) tagObj).trim().isEmpty()) {
                errors.add(formatError(lineNo, "skill_tag[" + i + "] must be a non-empty string", "skill_tag"));
                return null;
            }
            dedup.add(((String) tagObj).trim());
        }

        if (dedup.size() < 1 || dedup.size() > 3) {
            errors.add(formatError(lineNo, "skill_tag size must be between 1 and 3", "skill_tag"));
            return null;
        }
        return new ArrayList<>(dedup);
    }

    private static String readRequiredString(Map<String, Object> object, String field, int lineNo, List<String> errors) {
        if (!object.containsKey(field) || object.get(field) == null) {
            errors.add(formatError(lineNo, "missing required field", field));
            return "";
        }
        Object value = object.get(field);
        if (!(value instanceof String)) {
            errors.add(formatError(lineNo, "field must be a string", field));
            return "";
        }
        String text = ((String) value).trim();
        if (text.isEmpty()) {
            errors.add(formatError(lineNo, "field must not be empty", field));
            return "";
        }
        return text;
    }

    private static String formatError(int lineNo, String reason, String field) {
        if (field == null || field.isEmpty()) {
            return "line " + lineNo + ": " + reason;
        }
        return "line " + lineNo + ": " + reason + " [field=" + field + "]";
    }

    private static String stringOrEmpty(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim();
    }

    private static String normalizeModule(String module) {
        return module == null ? "" : module.trim().toUpperCase(Locale.ROOT);
    }

    private static final class JsonParser {
        private final String text;
        private int index;

        JsonParser(String text) {
            this.text = text == null ? "" : text;
        }

        Map<String, Object> parseObjectLine() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                throw new IllegalArgumentException("trailing content");
            }
            if (!(value instanceof Map)) {
                throw new IllegalArgumentException("not json object");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> object = (Map<String, Object>) value;
            return object;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("unexpected end");
            }
            char c = text.charAt(index);
            if (c == '{') {
                return parseObject();
            }
            if (c == '[') {
                return parseArray();
            }
            if (c == '"') {
                return parseString();
            }
            if (c == 't') {
                expectLiteral("true");
                return Boolean.TRUE;
            }
            if (c == 'f') {
                expectLiteral("false");
                return Boolean.FALSE;
            }
            if (c == 'n') {
                expectLiteral("null");
                return null;
            }
            if (c == '-' || isDigit(c)) {
                return parseNumber();
            }
            throw new IllegalArgumentException("unexpected token");
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> object = new HashMap<>();
            expectChar('{');
            skipWhitespace();
            if (peek('}')) {
                index++;
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expectChar(':');
                skipWhitespace();
                Object value = parseValue();
                object.put(key, value);
                skipWhitespace();
                if (peek(',')) {
                    index++;
                    continue;
                }
                if (peek('}')) {
                    index++;
                    break;
                }
                throw new IllegalArgumentException("object not closed");
            }
            return object;
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<>();
            expectChar('[');
            skipWhitespace();
            if (peek(']')) {
                index++;
                return array;
            }
            while (true) {
                skipWhitespace();
                array.add(parseValue());
                skipWhitespace();
                if (peek(',')) {
                    index++;
                    continue;
                }
                if (peek(']')) {
                    index++;
                    break;
                }
                throw new IllegalArgumentException("array not closed");
            }
            return array;
        }

        private String parseString() {
            expectChar('"');
            StringBuilder sb = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (index >= text.length()) {
                        throw new IllegalArgumentException("bad escape");
                    }
                    char esc = text.charAt(index++);
                    switch (esc) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(esc);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            sb.append(parseUnicodeHex());
                            break;
                        default:
                            throw new IllegalArgumentException("bad escape");
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("unterminated string");
        }

        private char parseUnicodeHex() {
            if (index + 4 > text.length()) {
                throw new IllegalArgumentException("invalid unicode");
            }
            int code = 0;
            for (int i = 0; i < 4; i++) {
                char ch = text.charAt(index++);
                int digit = Character.digit(ch, 16);
                if (digit < 0) {
                    throw new IllegalArgumentException("invalid unicode");
                }
                code = (code << 4) + digit;
            }
            return (char) code;
        }

        private Number parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            if (peek('0')) {
                index++;
            } else {
                readDigits();
            }
            if (peek('.')) {
                index++;
                readDigits();
            }
            if (peek('e') || peek('E')) {
                index++;
                if (peek('+') || peek('-')) {
                    index++;
                }
                readDigits();
            }
            String number = text.substring(start, index);
            try {
                if (number.contains(".") || number.contains("e") || number.contains("E")) {
                    return Double.parseDouble(number);
                }
                return Long.parseLong(number);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid number");
            }
        }

        private void readDigits() {
            if (index >= text.length() || !isDigit(text.charAt(index))) {
                throw new IllegalArgumentException("invalid number");
            }
            while (index < text.length() && isDigit(text.charAt(index))) {
                index++;
            }
        }

        private void expectLiteral(String literal) {
            if (!text.startsWith(literal, index)) {
                throw new IllegalArgumentException("invalid literal");
            }
            index += literal.length();
        }

        private void expectChar(char c) {
            if (index >= text.length() || text.charAt(index) != c) {
                throw new IllegalArgumentException("expected char");
            }
            index++;
        }

        private boolean peek(char c) {
            return index < text.length() && text.charAt(index) == c;
        }

        private void skipWhitespace() {
            while (index < text.length()) {
                char c = text.charAt(index);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    index++;
                    continue;
                }
                break;
            }
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }
    }

    public static final class ParsedResult {
        public final ValidationResult result;
        public final List<KbStrategy> items;

        ParsedResult(ValidationResult result, List<KbStrategy> items) {
            this.result = result;
            this.items = items == null ? Collections.<KbStrategy>emptyList() : items;
        }
    }

    public static final class ValidationResult {
        public final boolean ok;
        public final List<String> errors;
        public final int lineCount;
        public final int validCount;

        ValidationResult(boolean ok, List<String> errors, int lineCount, int validCount) {
            this.ok = ok;
            this.errors = errors == null ? Collections.<String>emptyList() : errors;
            this.lineCount = lineCount;
            this.validCount = validCount;
        }
    }
}
