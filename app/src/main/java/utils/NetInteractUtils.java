package utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 需要在xml下新建network.xml，如下：
 * <?xml version="1.0" encoding="utf-8"?>
 * <network-security-config>
 *     <base-config cleartextTrafficPermitted="true" />
 * </network-security-config>
 *在Manifest中添加：
 *android:networkSecurityConfig="@xml/network"
 *需添加网络权限
 * <uses-permission android:name="android.permission.INTERNET" />
 */

/**
 * http://43.143.206.86/ccle
 * 以下为NetInteractUtils 2.3版本, 实现用户注销，管理员删除用户等......持续更新
 * 添加管理员获取所有普通用户id(除了管理员), 修改删除用户需要管理员验证
 * 获取全部测评分页，用户模块选择
 */

public class NetInteractUtils {
    //定义服务器IP接口
    private final String url = "http://49.233.107.121/ccle";
    private Activity context;//网络交互页面
    private NetInteractUtils() {}


    private static volatile NetInteractUtils instance;

    /**
     * 网络交互单例模式，需带上下文
     *
     * @param context
     * @return
     */

    public static NetInteractUtils getInstance(Activity context) {
        if (instance == null) {
            synchronized (NetInteractUtils.class) {
                if (instance == null) {
                    instance = new NetInteractUtils();
                    instance.context = context;
                }
            }
        }
        return instance;
    }

    /**
     * UI刷新监听，可添加view网络刷新效果
     */
    public interface UiRefreshListener {
        /**
         * 刷新状态，网络加载效果
         * @param isPlaying 设置为True，需要打开加载，否则关闭
         */
        void refreshUI(Boolean isPlaying);
    }
    //UI刷新监听
    private UiRefreshListener listener;
    public void setListener(UiRefreshListener listener) {
        this.listener = listener;
    }

    //防止在非主线程刷新UI或无listener实例
    private void uiThreadRefresh(Boolean isPlaying) {
        if (listener != null)
            context.runOnUiThread(() -> {
                listener.refreshUI(isPlaying);
            });
    }

    //所有错误提示信息
    private void showToast(String msg) {
        context.runOnUiThread(() -> {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 登录回调函数（外部接口）
     */
    public interface LoginCallback {
        /**
         * 登录成功，返回参数如下
         * @param uid
         * @param username
         */
        void onLoginResult(String uid, String username);
    }

    LoginCallback loginCallback;

    public void setLoginCallback(LoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    /**
     * 上传测评回调函数（外部接口）
     */
    public interface UploadEvaluationCallback{
        /**
         * 上传测评成功，返回测评者ID（儿童）
         * @param childUserID
         */
        void onUploadEvaluationResult(String childUserID) throws Exception;
    }
    UploadEvaluationCallback uploadEvaluationCallback;
    public void setUploadEvaluationCallback(UploadEvaluationCallback uploadEvaluationCallback) {
        this.uploadEvaluationCallback = uploadEvaluationCallback;
    }

    /**
     * 获取某个测评的回调函数（外部接口）
     */
    public interface EvaluationCallback{
        /**
         * 获取测评成功，只有一个
         * @param evaluation
         * "evaluation":
         *     {
         *       "ID": "ChildUser ID",
         *       "Info": "ChildUser Info",
         *       "Evaluations": "ChildUser Evaluations",
         *       "UserID": "User ID",
         *       "Time": "Timestamp"
         *     }
         *     ***返回字符串没有包含"evaluation":{},已经通过json转换取出
         *     则字符串为
         *     "ID": "ChildUser ID",
         *     "Info": "ChildUser Info",
         *     "Evaluations": "ChildUser Evaluations",
         *     "UserID": "User ID",
         *     "Time": "Timestamp"
         */
        void onEvaluationResult(String evaluation) throws JSONException;
    }
    EvaluationCallback evaluationCallback;
    public void setEvaluationCallback(EvaluationCallback evaluationCallback) {
        this.evaluationCallback = evaluationCallback;
    }


    /**
     * 获取所有测评回调函数（外部接口）
     */
    public interface EvaluationsCallback{
        /**
         * 获取测评成功
         * @param evaluations
         * "evaluations": [
         *     {
         *       "ID": "ChildUser ID",
         *       "Info": "ChildUser Info",
         *       "Evaluations": "ChildUser Evaluations",
         *       "UserID": "User ID",
         *       "Time": "Timestamp"
         *     }
         *      {
         *       "ID": "ChildUser ID",
         *       "Info": "ChildUser Info",
         *       "Evaluations": "ChildUser Evaluations",
         *       "UserID": "User ID",
         *       "Time": "Timestamp"
         *     }
         *     ·····
         *     ***返回字符串没有包含"evaluations":[],已经通过json转换取出，即内层
         *     则字符串为
         *     {
         *     "ID": "ChildUser ID",
         *     "Info": "ChildUser Info",
         *     "Evaluations": "ChildUser Evaluations",
         *     "UserID": "User ID",
         *     "Time": "Timestamp"
         *     }
         */
        void onEvaluationsResult(String evaluations);
    }
    EvaluationsCallback evaluationsCallback;
    public void setEvaluationsCallback(EvaluationsCallback evaluationsCallback) {
        this.evaluationsCallback = evaluationsCallback;
    }

    /**
     * 获取所有测评ID回调函数（外部接口）
     */
    public interface EvaluationIDsCallback{
        /**
         * 获取测评ID成功
         * @param evaluationIDs
         * "evaluationIDs": [101, 102, 103, ...]
         *  ***返回字符串没有包含"evaluationIDs":[],已经通过json转换取出，即内层
         */
        void onEvaluationIDsResult(String evaluationIDs) throws Exception;
    }
    EvaluationIDsCallback evaluationIDsCallback;
    public void setEvaluationIDsCallback(EvaluationIDsCallback evaluationIDsCallback) {
        this.evaluationIDsCallback = evaluationIDsCallback;
    }

    public interface UserIDsCallback{
        /**
         * 获取用户ID成功
         * @param userIDs
         * "UserIDs": [101, 102, 103, ...]
         *  ***返回字符串没有包含"UserIDs":[],已经通过json转换取出，即内层
         */
        void onUserIDsResult(String userIDs);
    }
    UserIDsCallback userIDsCallback;
    public void setUserIDsCallback(UserIDsCallback userIDsCallback) {
        this.userIDsCallback = userIDsCallback;
    }

    public interface UserInfoCallback{
        /**
         * 获取用户信息成功
         * @param user
         * {
         *     "ID":用户uid
         *     "Username":用户名
         *     "PassWord":用户密码（加密）
         *     "Bind":用户绑定账号（手机号或邮箱）
         *     "Time":注册时间
         * }
         *
         */
        void onUserInfoResult(String user) throws Exception;
    }
    UserInfoCallback userInfoCallback;
    public void setUserInfoCallback(UserInfoCallback userInfoCallback) {
        this.userInfoCallback = userInfoCallback;
    }



    public interface AudioCallback{
        /**
         * @param audio Base64编码格式的语音
         *              可以这样解码：
         *              byte[] audioData = Base64.decode(audio, Base64.DEFAULT)（安卓库）
         *                              或Base64.getDecoder().decode(audio)（java库）;
         */
        void onAudioResult(String audio);
    }
    AudioCallback audioCallback;
    public void setAudioCallback(AudioCallback audioCallback) {
        this.audioCallback = audioCallback;
    }



    public interface ModuleCallback{
        /**
         * @param module
         *
         * {
         *     A:1,
         *     ......
         * }
         *
         */
        void onModuleResult(String module) throws JSONException;
    }
    ModuleCallback moduleCallback;
    public void setModuleCallback(ModuleCallback moduleCallback) {
        this.moduleCallback= moduleCallback;
    }

    /**
     * 一般OKHttp请求回调函数，即无返回结果
     * @param request
     * @param client
     */
    private void newCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    if (responseData == null || responseData.trim().isEmpty()) {
                        // 如果响应为空，忽略错误
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (!response.isSuccessful()) {//失败
                        String message = jsonResponse.optString("message", "未知错误");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    // showToast("解析服务器数据错误！");
                    // 忽略JSON解析错误，避免干扰用户
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 登录OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void loginCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String username = jsonResponse.getString("username");
                        String uid = jsonResponse.getString("uid");
                        if (loginCallback != null) {
                            context.runOnUiThread(() -> {
                                loginCallback.onLoginResult(uid, username);
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.getString("message");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 上传测评OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void uploadEvaluationCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String childUserID = jsonResponse.getString("childUserID");
                        if (uploadEvaluationCallback != null) {
                            context.runOnUiThread(() -> {
                                try {
                                    uploadEvaluationCallback.onUploadEvaluationResult(childUserID);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.getString("message");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 获得所有测评OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void evaluationsCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    if (responseData == null || responseData.trim().isEmpty()) {
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String evaluations = jsonResponse.optString("evaluations");
                        if (evaluationsCallback != null) {
                            context.runOnUiThread(() -> {
                                evaluationsCallback.onEvaluationsResult(evaluations);
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.optString("message", "未知错误");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    // showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 获得某个测评OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void evaluationCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String evaluation = jsonResponse.getString("evaluation");
                        if (evaluationCallback != null) {
                            context.runOnUiThread(() -> {
                                try {
                                    evaluationCallback.onEvaluationResult(evaluation);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.getString("message");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 获得所有测评ID的OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void evaluationIDsCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String evaluationIDs = jsonResponse.getString("evaluationIDs");
                        if (evaluationIDsCallback != null) {
                            context.runOnUiThread(() -> {
                                try {
                                    evaluationIDsCallback.onEvaluationIDsResult(evaluationIDs);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.getString("message");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }


    /**
     * 获得所有普通用户ID的OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void userIDsCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    if (responseData == null || responseData.trim().isEmpty()) {
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String userIDs = jsonResponse.optString("uids");
                        if (userIDsCallback != null) {
                            context.runOnUiThread(() -> {
                                try {
                                    userIDsCallback.onUserIDsResult(userIDs);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.optString("message", "未知错误");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    // showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }


    /**
     * 获得所有普通用户信息的OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void userInfoCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    if (responseData == null || responseData.trim().isEmpty()) {
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String user = jsonResponse.optString("user");
                        if (userInfoCallback != null) {
                            context.runOnUiThread(() -> {
                                try {
                                    userInfoCallback.onUserInfoResult(user);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.optString("message", "未知错误");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    // showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 获得录音OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void audioCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String audio = jsonResponse.getString("audio");
                        if (audioCallback != null) {
                            context.runOnUiThread(() -> {
                                audioCallback.onAudioResult(audio);
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.getString("message");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast("解析服务器数据错误！");
                }
                uiThreadRefresh(false);
            }
        });
    }

    /**
     * 获得模块OKHttp请求回调函数
     * @param request
     * @param client
     */
    private void moduleCall(Request request, OkHttpClient client){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {// 请求失败的回调处理
                showToast("错误！"+e.getMessage());
                uiThreadRefresh(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    if (response.isSuccessful()) {//返回码为200
                        String module = jsonResponse.getString("module");
                        if (moduleCallback != null) {
                            context.runOnUiThread(() -> {
                                try {
                                    moduleCallback.onModuleResult(module);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else {//返回码为其他，发生错误
                        String message = jsonResponse.getString("message");
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast("解析服务器数据错误！");
                    Log.d("NetInteractUtils", "JSON解析错误");
                }
                uiThreadRefresh(false);
            }
        });
    }


    /**
     * 密码登录
     *
     * @param username
     * @param password
     */
    public void loginWithPassword(String username, String password) {
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(url + "/login")
                .post(requestBody)
                .build();
        loginCall(request,client);

    }

    /**
     * 验证码登录
     * @param bind
     * @param captcha
     */
    public void loginWithCaptcha(String bind, String captcha) {
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .build();

        Request request = new Request.Builder()
                .url(url + "/login_captcha")
                .post(requestBody)
                .build();

        loginCall(request,client);
    }

    /**
     * 获取验证码
     * @param code 0：登录验证码，1：注册验证码，2：修改密码验证码，3:注销账号，其他不合法
     * @param contact 手机号或邮箱二选一
     */
    public void getCaptcha(String code, String contact) {
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("code", code)
                .add("contact", contact)
                .build();

        Request request = new Request.Builder()
                .url(url + "/get_captcha")
                .post(requestBody)
                .build();

        newCall(request,client);
    }

    /**
     * 修改密码
     * @param bind
     * @param captcha
     * @param password
     * @param password_confirm
     */
    public void changePassword(String bind, String captcha, String password, String password_confirm) {
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .add("password", password)
                .add("password_confirm", password_confirm)
                .build();

        Request request = new Request.Builder()
                .url(url + "/change_password")
                .post(requestBody)
                .build();

        newCall(request,client);
    }

    /**
     * 注册账号
     * @param bind
     * @param captcha
     * @param username
     * @param password
     * @param password_confirm
     */
    public void register(String bind, String captcha, String username, String password,
                         String password_confirm) {
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .add("username", username)
                .add("password", password)
                .add("password_confirm", password_confirm)
                .build();

        Request request = new Request.Builder()
                .url(url + "/register")
                .post(requestBody)
                .build();

        newCall(request, client);
    }

    /**
     * 上传测评
     * @param uid
     * @param childUser
     */
    public void uploadEvaluation(String uid, String childUser){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", uid)
                .add("childUser", childUser)
                .build();

        Request request = new Request.Builder()
                .url(url + "/upload_evaluations")
                .post(requestBody)
                .build();
       uploadEvaluationCall(request, client);
    }

    /**
     * 获取该用户的所有测评
     * @param uid
     */
    @Deprecated
    public void getEvaluations(String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_evaluations/"+uid)
                .get()
                .build();
        evaluationsCall(request, client);
    }


    /**
     * 分页获取该用户的所有测评
     * @param uid
     * @param start 不能为负数
     * @param num 大于0
     */
    public void getEvaluationsLimit(String uid, int start, int num){
        if(start<0 || num <1)
            return;
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_evaluations_limit/"+uid+"/"+start+"/"+num)
                .get()
                .build();
        evaluationsCall(request, client);
    }

    /**
     * 获取该用户的某个测评
     * @param uid
     */
    public void getEvaluation(String uid, String childUserID){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_evaluation/"+uid+"/"+childUserID)
                .get()
                .build();
        evaluationCall(request, client);
    }

    /**
     * 获取该用户的所有测评ID
     * @param uid
     */
    public void getEvaluationIDs(String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_evaluation_ids/"+uid)
                .get()
                .build();
        evaluationIDsCall(request, client);
    }

    /**
     * 获取所有普通用户的ID
     * @param adminUid
     */
    public void getUserIDs(String adminUid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_uids/"+adminUid)
                .get()
                .build();
        userIDsCall(request, client);
    }

    /**
     * 获取用户的信息
     * @param uid
     */
    public void getUserInfo(String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_user_info/"+uid)
                .get()
                .build();
        userInfoCall(request, client);
    }


    /**
     * 删除该用户的所有测评
     * @param uid
     */
    public void deleteEvaluations(String admin, String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/delete_evaluations/" + admin + "/" + uid)
                .delete()
                .build();
        newCall(request, client);//调用一般回调
    }

    /**
     * 删除该用户的某个测评
     * @param uid
     */
    public void deleteEvaluation(String uid, String childUserID){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/delete_evaluation/" + uid + "/" + childUserID)
                .delete()
                .build();
        newCall(request, client);//调用一般回调
    }

    /**
     * 管理员删除某用户
     * @param admin uid
     */
    public void deleteUserAdmin(String admin, String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/delete_user_admin/" + admin + "/" + uid)
                .delete()
                .build();
        newCall(request, client);//调用一般回调
    }

    /**
     * 用户注销账号
     * @param uid
     */
    public void logoffUser(String uid, String bind, String captcha){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("bind", bind)
                .add("captcha", captcha)
                .add("uid", uid)
                .build();

        Request request = new Request.Builder()
                .url(url + "/logoff_user")
                .post(requestBody)
                .build();
        newCall(request, client);
    }



    /**
     * 更新测评
     * @param uid
     * @param childUser
     */
    public void updateEvaluation(String uid, String childUserID, String childUser){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // 设置连接超时
                .readTimeout(30, TimeUnit.SECONDS)     // 设置读取超时
                .writeTimeout(30, TimeUnit.SECONDS)    // 设置写入超时
                .build();
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", uid)
                .add("childUserID", childUserID)
                .add("childUser", childUser)
                .build();

        Request request = new Request.Builder()
                .url(url + "/update_evaluation")
                .post(requestBody)
                .build();
        newCall(request, client);
    }

    /**
     * 上传录音
     * @param uid
     * @param childUserID
     * @param title
     * @param num
     * @param audioPath 文件路径
     */
    public void uploadAudio(String uid, String childUserID, String title, String num, String audioPath){
        uiThreadRefresh(true);
        File audio = new File(audioPath);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uid", uid)
                .addFormDataPart("childUserID", childUserID)
                .addFormDataPart("title", title)
                .addFormDataPart("num", num)
                .addFormDataPart("audio", audio.getName(),
                        RequestBody.create(audio, MediaType.parse("audio/amr")))
                .build();

        Request request = new Request.Builder()
                .url(url + "/upload_audio")
                .post(requestBody)
                .build();
        newCall(request, client);
    }

    /**
     * 获取录音
     * @param uid  用户ID
     * @param childUserID 测评儿童的ID
     * @param title 题目 例如：A,E
     * @param num 题号
     */
    public void getAudio(String uid, String childUserID, String title, String num) {
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        String getString = uid + "/" + childUserID + "/" + title + "/" + num;
        Request request = new Request.Builder()
                .url(url + "/get_audio/" + getString)
                .get()
                .build();
        audioCall(request, client);
    }


    /**
     * 上传模块
     * @param uid
     * @param module
     * 例如：
     * {
     *     "A":1,
     *     "S":0,
     *     ......
     * }
     * 可以自定义，但是必须是整数，比如 1：已开通，0：未开通
     * 或者只上传开通的模块，未开通的后端默认0
     */
    public void createModule(String uid, JSONObject module){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", uid)
                .add("module", module.toString())
                .build();

        Request request = new Request.Builder()
                .url(url + "/create_module")
                .post(requestBody)
                .build();
        newCall(request, client);
    }

    /**
     * 清除用户开通的模块
     * @param admin
     * @param uid
     */
    public void deleteModule(String admin, String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/delete_module_admin/" + admin + "/" + uid)
                .delete()
                .build();
        newCall(request, client);//调用一般回调
    }


    /**
     * 更新模块
     * @param uid
     * @param module
     * 例如：
     * {
     *     "A":1,
     *     "S":0,
     *     ......
     * }
     * 可以自定义，但是必须是整数，比如 1：已开通，0：未开通
     * 只更新上传的模块，其余不变
     */
    public void updateModule(String uid, JSONObject module){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", uid)
                .add("module", module.toString())
                .build();

        Request request = new Request.Builder()
                .url(url + "/update_module")
                .post(requestBody)
                .build();
        newCall(request, client);
    }

    /**
     * 获取模块
     * @param uid
     */
    public void getModule(String uid){
        uiThreadRefresh(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url + "/get_module/" + uid)
                .get()
                .build();
        moduleCall(request, client);
    }

}
