package utils.netService;

public class ApiConfig {
    public static final String DEFAULT_BASE_URL = "http://123.57.104.101:8080/";

    private final String baseUrl;

    public ApiConfig() {
        this(DEFAULT_BASE_URL);
    }

    public ApiConfig(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("baseUrl不能为空");
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
