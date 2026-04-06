package utils.netService;

public interface ResultCallback<T> {
    void onSuccess(T result);

    void onError(ApiException exception);
}
