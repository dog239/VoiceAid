package utils.netService;

import okhttp3.*;

import org.json.JSONObject;

import java.io.IOException;

public class RequestExecutor {
    public interface JsonHandler<T> {
        T handle(JSONObject jsonObject) throws Exception;
    }

    public interface BinaryHandler<T> {
        T handle(byte[] bytes) throws Exception;
    }

    private final OkHttpClient client;

    public RequestExecutor(OkHttpClient client) {
        this.client = client;
    }

    public void executeWithoutResult(Request request, VoidCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(new ApiException("错误！" + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = readBody(response);
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                        return;
                    }
                    callback.onError(buildException(response.code(), body));
                } finally {
                    response.close();
                }
            }
        });
    }

    public <T> void executeJson(Request request, JsonHandler<T> handler, ResultCallback<T> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(new ApiException("错误！" + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = readBody(response);
                    if (!response.isSuccessful()) {
                        callback.onError(buildException(response.code(), body));
                        return;
                    }
                    JSONObject jsonObject = body.isEmpty() ? new JSONObject() : new JSONObject(body);
                    T result = handler.handle(jsonObject);
                    callback.onSuccess(result);
                } catch (ApiException e) {
                    callback.onError(e);
                } catch (Exception e) {
                    callback.onError(new ApiException("解析服务器数据错误！", e));
                } finally {
                    response.close();
                }
            }
        });
    }

    public <T> void executeAudio(Request request, JsonHandler<T> jsonHandler, BinaryHandler<T> binaryHandler, ResultCallback<T> callback) {
        executeBinary(request, jsonHandler, binaryHandler, callback);
    }

    public <T> void executeBinary(Request request, JsonHandler<T> jsonHandler, BinaryHandler<T> binaryHandler, ResultCallback<T> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(new ApiException("错误！" + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    callback.onError(new ApiException("解析服务器数据错误！"));
                    response.close();
                    return;
                }
                try {
                    MediaType contentType = responseBody.contentType();
                    boolean isJson = contentType != null && contentType.subtype() != null
                            && contentType.subtype().toLowerCase().contains("json");
                    if (!response.isSuccessful()) {
                        if (isJson) {
                            callback.onError(buildException(response.code(), responseBody.string()));
                        } else {
                            callback.onError(new ApiException(response.code(), "请求失败，状态码：" + response.code()));
                        }
                        return;
                    }
                    if (isJson) {
                        T result = jsonHandler.handle(new JSONObject(responseBody.string()));
                        callback.onSuccess(result);
                    } else {
                        T result = binaryHandler.handle(responseBody.bytes());
                        callback.onSuccess(result);
                    }
                } catch (ApiException e) {
                    callback.onError(e);
                } catch (Exception e) {
                    callback.onError(new ApiException("解析服务器数据错误！", e));
                } finally {
                    response.close();
                }
            }
        });
    }

    private String readBody(Response response) throws IOException {
        ResponseBody body = response.body();
        return body == null ? "" : body.string();
    }

    private ApiException buildException(int code, String body) {
        try {
            JSONObject jsonObject = body == null || body.trim().isEmpty() ? new JSONObject() : new JSONObject(body);
            return new ApiException(code, ResponseParsers.message(jsonObject));
        } catch (Exception ignored) {
            return new ApiException(code, "请求失败，状态码：" + code);
        }
    }
}
