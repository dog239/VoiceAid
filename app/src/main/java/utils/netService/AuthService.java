package utils.netService;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AuthService {
    private final ApiConfig apiConfig;
    private final RequestExecutor executor;

    public AuthService(ApiConfig apiConfig, RequestExecutor executor) {
        this.apiConfig = apiConfig;
        this.executor = executor;
    }

    public void loginWithPassword(String username, String password, ResultCallback<LoginResult> callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/login")
                .post(requestBody)
                .build();
        executor.executeJson(request,
                json -> new LoginResult(String.valueOf(json.get("uid")), json.getString("username")),
                callback);
    }

    public void loginWithCaptcha(String bind, String captcha, ResultCallback<LoginResult> callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/login_captcha")
                .post(requestBody)
                .build();
        executor.executeJson(request,
                json -> new LoginResult(String.valueOf(json.get("uid")), json.getString("username")),
                callback);
    }

    public void getCaptcha(String code, String contact, VoidCallback callback) {
        String purpose = RequestData.captchaPurpose(code);
        if (purpose == null) {
            callback.onError(new ApiException("验证码用途不合法"));
            return;
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("purpose", purpose)
                .add("contact", contact)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/captchas")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void changePassword(String bind, String captcha, String password, String passwordConfirm, VoidCallback callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .add("password", password)
                .add("password_confirm", passwordConfirm)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/change_password")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void register(String bind, String captcha, String username, String password, String passwordConfirm, VoidCallback callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .add("username", username)
                .add("password", password)
                .add("password_confirm", passwordConfirm)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/register")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void logoffUser(String uid, String bind, String captcha, VoidCallback callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .add("uid", uid)
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/logoff_user")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }
}
