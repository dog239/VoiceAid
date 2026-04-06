package utils.netService;

public interface VoidCallback {
    void onSuccess();

    void onError(ApiException exception);
}
