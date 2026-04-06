package utils.netService;

import android.os.Build;

import okhttp3.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Consumer;

public class NetInteractUtils {
    private final AuthService authService;
    private final EvaluationService evaluationService;
    private final UserService userService;
    private final AudioService audioService;
    private final ModuleService moduleService;
    private final ReportService reportService;

    private UiRunner uiRunner = UiRunner.direct();
    private MessageHandler messageHandler = message -> { };
    private UiRefreshListener uiRefreshListener;

    private LoginCallback loginCallback;
    private UploadEvaluationCallback uploadEvaluationCallback;
    private EvaluationCallback evaluationCallback;
    private EvaluationsCallback evaluationsCallback;
    private EvaluationIDsCallback evaluationIDsCallback;
    private UserIDsCallback userIDsCallback;
    private UserInfoCallback userInfoCallback;
    private AudioCallback audioCallback;
    private AudiosCallback audiosCallback;
    private ModuleCallback moduleCallback;
    private RegisterCallback registerCallback;
    private ReportsCallback reportsCallback;
    private ReportCallback reportCallback;
    private AdminStatusCallback adminStatusCallback;

    private static volatile NetInteractUtils instance;

    public NetInteractUtils() {
        this(new ApiConfig(), HttpClientFactory.createDefault());
    }

    public NetInteractUtils(ApiConfig apiConfig, OkHttpClient client) {
        RequestExecutor executor = new RequestExecutor(client);
        this.authService = new AuthService(apiConfig, executor);
        this.evaluationService = new EvaluationService(apiConfig, executor);
        this.userService = new UserService(apiConfig, executor);
        this.audioService = new AudioService(apiConfig, executor);
        this.moduleService = new ModuleService(apiConfig, executor);
        this.reportService = new ReportService(apiConfig, executor);
    }

    public static NetInteractUtils getInstance() {
        if (instance == null) {
            synchronized (NetInteractUtils.class) {
                if (instance == null) {
                    instance = new NetInteractUtils();
                }
            }
        }
        return instance;
    }

    public static NetInteractUtils getInstance(Object ignoredContext) {
        return getInstance();
    }

    public void setUiRunner(UiRunner uiRunner) {
        this.uiRunner = uiRunner == null ? UiRunner.direct() : uiRunner;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler == null ? message -> { } : messageHandler;
    }

    public interface UiRefreshListener {
        void refreshUI(Boolean isPlaying);
    }

    public interface MessageHandler {
        void showMessage(String message);
    }

    public interface UiRunner {
        void run(Runnable runnable);

        static UiRunner direct() {
            return Runnable::run;
        }
    }

    public interface LoginCallback {
        void onLoginResult(String uid, String username);
    }

    public interface UploadEvaluationCallback {
        void onUploadEvaluationResult(String childUserID) throws Exception;
    }

    public interface EvaluationCallback {
        void onEvaluationResult(String evaluation) throws JSONException;
    }

    public interface EvaluationsCallback {
        void onEvaluationsResult(String evaluations);
    }

    public interface EvaluationIDsCallback {
        void onEvaluationIDsResult(String evaluationIDs) throws Exception;
    }

    public interface UserIDsCallback {
        void onUserIDsResult(String userIDs);
    }

    public interface UserInfoCallback {
        void onUserInfoResult(String user) throws Exception;
    }

    public interface AudioCallback {
        void onAudioResult(String audio);
    }

    public interface AudiosCallback {
        void onAudiosResult(String audios);
    }

    public interface ReportsCallback {
        void onReportsResult(String reports);
    }

    public interface ReportCallback {
        void onReportResult(String report);
    }

    public interface AdminStatusCallback {
        void onAdminStatusResult(String adminStatus);
    }

    public interface ModuleCallback {
        void onModuleResult(String module) throws JSONException;
    }

    public interface RegisterCallback {
        void onRegisterResult();
    }

    public void setListener(UiRefreshListener listener) {
        this.uiRefreshListener = listener;
    }

    public void setLoginCallback(LoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    public void setUploadEvaluationCallback(UploadEvaluationCallback uploadEvaluationCallback) {
        this.uploadEvaluationCallback = uploadEvaluationCallback;
    }

    public void setEvaluationCallback(EvaluationCallback evaluationCallback) {
        this.evaluationCallback = evaluationCallback;
    }

    public void setEvaluationsCallback(EvaluationsCallback evaluationsCallback) {
        this.evaluationsCallback = evaluationsCallback;
    }

    public void setEvaluationIDsCallback(EvaluationIDsCallback evaluationIDsCallback) {
        this.evaluationIDsCallback = evaluationIDsCallback;
    }

    public void setUserIDsCallback(UserIDsCallback userIDsCallback) {
        this.userIDsCallback = userIDsCallback;
    }

    public void setUserInfoCallback(UserInfoCallback userInfoCallback) {
        this.userInfoCallback = userInfoCallback;
    }

    public void setAudioCallback(AudioCallback audioCallback) {
        this.audioCallback = audioCallback;
    }

    public void setAudiosCallback(AudiosCallback audiosCallback) {
        this.audiosCallback = audiosCallback;
    }

    public void setReportsCallback(ReportsCallback reportsCallback) {
        this.reportsCallback = reportsCallback;
    }

    public void setReportCallback(ReportCallback reportCallback) {
        this.reportCallback = reportCallback;
    }

    public void setAdminStatusCallback(AdminStatusCallback adminStatusCallback) {
        this.adminStatusCallback = adminStatusCallback;
    }

    public void setModuleCallback(ModuleCallback moduleCallback) {
        this.moduleCallback = moduleCallback;
    }

    public void setRegisterCallback(RegisterCallback registerCallback) {
        this.registerCallback = registerCallback;
    }

    public void loginWithPassword(String username, String password) {
        runWithLoading(done -> authService.loginWithPassword(username, password, new ResultCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                deliver(() -> {
                    if (loginCallback != null) {
                        loginCallback.onLoginResult(result.getUid(), result.getUsername());
                    }
                });
                done.run();
            }

            @Override
            public void onError(ApiException exception) {
                handleError(exception);
                done.run();
            }
        }));
    }

    public void loginWithCaptcha(String bind, String captcha) {
        runWithLoading(done -> authService.loginWithCaptcha(bind, captcha, new ResultCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                deliver(() -> {
                    if (loginCallback != null) {
                        loginCallback.onLoginResult(result.getUid(), result.getUsername());
                    }
                });
                done.run();
            }

            @Override
            public void onError(ApiException exception) {
                handleError(exception);
                done.run();
            }
        }));
    }

    public void getCaptcha(String code, String contact) {
        runWithLoading(done -> authService.getCaptcha(code, contact, voidCallback(done, null)));
    }

    public void changePassword(String bind, String captcha, String password, String passwordConfirm) {
        runWithLoading(done -> authService.changePassword(bind, captcha, password, passwordConfirm, voidCallback(done, null)));
    }

    public void register(String bind, String captcha, String username, String password, String passwordConfirm) {
        runWithLoading(done -> authService.register(bind, captcha, username, password, passwordConfirm, voidCallback(done, () -> {
            if (registerCallback != null) {
                registerCallback.onRegisterResult();
            }
        })));
    }

    public void uploadEvaluation(String uid, String childUser) {
        runWithLoading(done -> evaluationService.uploadEvaluation(uid, childUser, callback(done, childUserID -> {
            if (uploadEvaluationCallback != null) {
                uploadEvaluationCallback.onUploadEvaluationResult(childUserID);
            }
        })));
    }

    @Deprecated
    public void getEvaluations(String uid) {
        runWithLoading(done -> evaluationService.getEvaluations(uid, callback(done, evaluations -> {
            if (evaluationsCallback != null) {
                evaluationsCallback.onEvaluationsResult(evaluations);
            }
        })));
    }

    public void getEvaluationsLimit(String uid, int start, int num) {
        runWithLoading(done -> evaluationService.getEvaluationsLimit(uid, start, num, callback(done, evaluations -> {
            if (evaluationsCallback != null) {
                evaluationsCallback.onEvaluationsResult(evaluations);
            }
        })));
    }

    public void getEvaluation(String uid, String childUserID) {
        runWithLoading(done -> evaluationService.getEvaluation(uid, childUserID, callback(done, evaluation -> {
            if (evaluationCallback != null) {
                evaluationCallback.onEvaluationResult(evaluation);
            }
        })));
    }

    public void getEvaluationIDs(String uid) {
        runWithLoading(done -> evaluationService.getEvaluationIDs(uid, callback(done, ids -> {
            if (evaluationIDsCallback != null) {
                evaluationIDsCallback.onEvaluationIDsResult(ids);
            }
        })));
    }

    public void getUserIDs(String adminUid) {
        runWithLoading(done -> userService.getUserIDs(adminUid, callback(done, ids -> {
            if (userIDsCallback != null) {
                userIDsCallback.onUserIDsResult(ids);
            }
        })));
    }

    public void getAdminStatus(String uid) {
        runWithLoading(done -> userService.getAdminStatus(uid, callback(done, status -> {
            if (adminStatusCallback != null) {
                adminStatusCallback.onAdminStatusResult(status);
            }
        })));
    }

    public void getUserInfo(String uid) {
        runWithLoading(done -> userService.getUserInfo(uid, callback(done, user -> {
            if (userInfoCallback != null) {
                userInfoCallback.onUserInfoResult(user);
            }
        })));
    }

    public void deleteEvaluations(String admin, String uid) {
        runWithLoading(done -> evaluationService.deleteEvaluations(uid, voidCallback(done, null)));
    }

    public void deleteEvaluation(String uid, String childUserID) {
        runWithLoading(done -> evaluationService.deleteEvaluation(uid, childUserID, voidCallback(done, null)));
    }

    public void deleteUserAdmin(String admin, String uid) {
        runWithLoading(done -> userService.deleteUserAdmin(admin, uid, voidCallback(done, null)));
    }

    public void logoffUser(String uid, String bind, String captcha) {
        runWithLoading(done -> authService.logoffUser(uid, bind, captcha, voidCallback(done, null)));
    }

    public void updateEvaluation(String uid, String childUserID, String childUser) {
        runWithLoading(done -> evaluationService.updateEvaluation(uid, childUserID, childUser, voidCallback(done, null)));
    }

    public void uploadAudio(String uid, String childUserID, String title, String num, String audioPath) {
        runWithLoading(done -> audioService.uploadAudio(uid, childUserID, title, num, audioPath, voidCallback(done, null)));
    }

    public void getAudios(String uid, String childUserID) {
        runWithLoading(done -> audioService.getAudios(uid, childUserID, callback(done, audios -> {
            if (audiosCallback != null) {
                audiosCallback.onAudiosResult(audios);
            }
        })));
    }

    public void getAudio(String uid, String childUserID, String title, String num) {
        runWithLoading(done -> audioService.getAudio(uid, childUserID, title, num, callback(done, audio -> {
            if (audioCallback != null) {
                audioCallback.onAudioResult(audio);
            }
        })));
    }

    public void updateAudio(String uid, String childUserID, String title, String num, String audioPath) {
        runWithLoading(done -> audioService.updateAudio(uid, childUserID, title, num, audioPath, voidCallback(done, null)));
    }

    public void deleteAudio(String uid, String childUserID, String title, String num) {
        runWithLoading(done -> audioService.deleteAudio(uid, childUserID, title, num, voidCallback(done, null)));
    }

    public void createModule(String uid, JSONObject module) {
        runWithLoading(done -> moduleService.createModule(uid, module, voidCallback(done, null)));
    }

    public void deleteModule(String admin, String uid) {
        runWithLoading(done -> moduleService.deleteModule(admin, uid, voidCallback(done, null)));
    }

    public void updateModule(String uid, JSONObject module) {
        runWithLoading(done -> moduleService.updateModule(uid, module, voidCallback(done, null)));
    }

    public void getModule(String uid) {
        runWithLoading(done -> moduleService.getModule(uid, callback(done, module -> {
            if (moduleCallback != null) {
                moduleCallback.onModuleResult(module);
            }
        })));
    }

    public void uploadPdf(String uid, String childUserID, String moduleType, String pdfPath) {
        runWithLoading(done -> moduleService.uploadPdf(uid, childUserID, moduleType, pdfPath, voidCallback(done, null)));
    }

    public void getReports(String uid, String childUserID) {
        runWithLoading(done -> reportService.getReports(uid, childUserID, callback(done, reports -> {
            if (reportsCallback != null) {
                reportsCallback.onReportsResult(reports);
            }
        })));
    }

    public void getReport(String uid, String childUserID, String moduleType) {
        runWithLoading(done -> reportService.getReport(uid, childUserID, moduleType, callback(done, report -> {
            if (reportCallback != null) {
                reportCallback.onReportResult(report);
            }
        })));
    }

    public void updateReport(String uid, String childUserID, String moduleType, String pdfPath) {
        runWithLoading(done -> reportService.updateReport(uid, childUserID, moduleType, pdfPath, voidCallback(done, null)));
    }

    public void deleteReport(String uid, String childUserID, String moduleType) {
        runWithLoading(done -> reportService.deleteReport(uid, childUserID, moduleType, voidCallback(done, null)));
    }

    private <T> ResultCallback<T> callback(Runnable done, ThrowingConsumer<T> onSuccess) {
        return new ResultCallback<T>() {
            @Override
            public void onSuccess(T result) {
                deliverSafely(() -> onSuccess.accept(result));
                done.run();
            }

            @Override
            public void onError(ApiException exception) {
                handleError(exception);
                done.run();
            }
        };
    }

    private VoidCallback voidCallback(Runnable done, Runnable onSuccess) {
        return new VoidCallback() {
            @Override
            public void onSuccess() {
                deliver(() -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
                done.run();
            }

            @Override
            public void onError(ApiException exception) {
                handleError(exception);
                done.run();
            }
        };
    }

    private void runWithLoading(Consumer<Runnable> action) {
        refresh(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            action.accept(() -> refresh(false));
        }
    }

    private void refresh(boolean playing) {
        if (uiRefreshListener != null) {
            deliver(() -> uiRefreshListener.refreshUI(playing));
        }
    }

    private void handleError(ApiException exception) {
        deliver(() -> messageHandler.showMessage(exception.getMessage()));
    }

    private void deliver(Runnable runnable) {
        uiRunner.run(runnable);
    }

    private void deliverSafely(ThrowingRunnable runnable) {
        uiRunner.run(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T value) throws Exception;
    }
}
