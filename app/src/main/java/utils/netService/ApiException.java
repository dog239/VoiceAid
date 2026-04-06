package utils.netService;

public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(String message) {
        this(-1, message);
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.code = -1;
    }

    public int getCode() {
        return code;
    }
}
