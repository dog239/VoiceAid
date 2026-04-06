package utils.netService;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

public class EvaluationService {
    private final ApiConfig apiConfig;
    private final RequestExecutor executor;

    public EvaluationService(ApiConfig apiConfig, RequestExecutor executor) {
        this.apiConfig = apiConfig;
        this.executor = executor;
    }

    public void uploadEvaluation(String uid, String childUser, ResultCallback<String> callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("childUser", childUser)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/evaluations")
                .post(requestBody)
                .build();
        executor.executeJson(request, json -> String.valueOf(json.get("childUserID")), callback);
    }

    public void getEvaluations(String uid, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/evaluations")
                .get()
                .build();
        executor.executeJson(request, json -> ResponseParsers.objectString(json, "evaluations", "[]"), callback);
    }

    public void getEvaluationsLimit(String uid, int start, int num, ResultCallback<String> callback) {
        if (start < 0 || num < 1) {
            callback.onError(new ApiException("分页参数不合法"));
            return;
        }
        getEvaluations(uid, new ResultCallback<String>() {
            @Override
            public void onSuccess(String evaluations) {
                try {
                    JSONArray source = new JSONArray(evaluations);
                    JSONArray page = new JSONArray();
                    int endExclusive = Math.min(source.length(), start + num);
                    for (int i = start; i < endExclusive; i++) {
                        page.put(source.get(i));
                    }
                    callback.onSuccess(page.toString());
                } catch (Exception e) {
                    callback.onError(new ApiException("解析服务器数据错误！", e));
                }
            }

            @Override
            public void onError(ApiException exception) {
                callback.onError(exception);
            }
        });
    }

    public void getEvaluation(String uid, String childUserID, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/evaluations/" + childUserID)
                .get()
                .build();
        executor.executeJson(request, json -> ResponseParsers.objectString(json, "evaluation", "{}"), callback);
    }

    public void getEvaluationIDs(String uid, ResultCallback<String> callback) {
        getEvaluations(uid, new ResultCallback<String>() {
            @Override
            public void onSuccess(String evaluations) {
                try {
                    JSONArray source = new JSONArray(evaluations);
                    JSONArray ids = new JSONArray();
                    for (int i = 0; i < source.length(); i++) {
                        JSONObject evaluation = source.optJSONObject(i);
                        if (evaluation != null && evaluation.has("ID")) {
                            ids.put(evaluation.get("ID"));
                        }
                    }
                    callback.onSuccess(ids.toString());
                } catch (Exception e) {
                    callback.onError(new ApiException("解析服务器数据错误！", e));
                }
            }

            @Override
            public void onError(ApiException exception) {
                callback.onError(exception);
            }
        });
    }

    public void deleteEvaluations(String uid, VoidCallback callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/evaluations")
                .delete()
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void deleteEvaluation(String uid, String childUserID, VoidCallback callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/evaluations/" + childUserID)
                .delete()
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void updateEvaluation(String uid, String childUserID, String childUser, VoidCallback callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("childUser", childUser)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/evaluations/" + childUserID)
                .put(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }
}
