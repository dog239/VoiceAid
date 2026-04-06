package utils.netService;

import okhttp3.Request;

import org.json.JSONObject;

public class UserService {
    private final ApiConfig apiConfig;
    private final RequestExecutor executor;

    public UserService(ApiConfig apiConfig, RequestExecutor executor) {
        this.apiConfig = apiConfig;
        this.executor = executor;
    }

    public void getUserIDs(String adminUid, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/admins/" + adminUid + "/users")
                .get()
                .build();
        executor.executeJson(request, json -> ResponseParsers.firstArray(json, "uids", "userIDs", "UserIDs"), callback);
    }

    public void getAdminStatus(String uid, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/admin-status")
                .get()
                .build();
        executor.executeJson(request, json -> new JSONObject()
                .put("uid", json.opt("uid"))
                .put("isAdmin", json.optBoolean("isAdmin"))
                .toString(), callback);
    }

    public void getUserInfo(String uid, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/info")
                .get()
                .build();
        executor.executeJson(request, json -> json.getJSONObject("user").toString(), callback);
    }

    public void deleteUserAdmin(String adminUid, String uid, VoidCallback callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/admins/" + adminUid + "/users/" + uid)
                .delete()
                .build();
        executor.executeWithoutResult(request, callback);
    }
}
