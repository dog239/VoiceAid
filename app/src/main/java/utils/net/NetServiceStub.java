package utils.net;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.UUID;

/**
 * utils/NetServiceStub.java 提供本地假实现，含 UI 线程回调与轻微延迟，保持时序接近真实网络。
 */
public class NetServiceStub implements NetService {
    private static final long STUB_DELAY_MS = 300;

    private final Activity activity;
    private final Handler mainHandler;
    private LoginCallback loginCallback;
    private RegisterCallback registerCallback;
    private AudioCallback audioCallback;
    private ModuleCallback moduleCallback;
    private UploadEvaluationCallback uploadEvaluationCallback;
    private EvaluationCallback evaluationCallback;
    private EvaluationsCallback evaluationsCallback;
    private EvaluationIDsCallback evaluationIDsCallback;
    private UserIDsCallback userIDsCallback;
    private UserInfoCallback userInfoCallback;
    private UiRefreshListener uiRefreshListener;

    public NetServiceStub(Activity activity) {
        this.activity = activity;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setLoginCallback(LoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    @Override
    public void setRegisterCallback(RegisterCallback registerCallback) {
        this.registerCallback = registerCallback;
    }

    @Override
    public void setUploadEvaluationCallback(UploadEvaluationCallback uploadEvaluationCallback) {
        this.uploadEvaluationCallback = uploadEvaluationCallback;
    }

    @Override
    public void setEvaluationCallback(EvaluationCallback evaluationCallback) {
        this.evaluationCallback = evaluationCallback;
    }

    @Override
    public void setEvaluationsCallback(EvaluationsCallback evaluationsCallback) {
        this.evaluationsCallback = evaluationsCallback;
    }

    @Override
    public void setEvaluationIDsCallback(EvaluationIDsCallback evaluationIDsCallback) {
        this.evaluationIDsCallback = evaluationIDsCallback;
    }

    @Override
    public void setUserIDsCallback(UserIDsCallback userIDsCallback) {
        this.userIDsCallback = userIDsCallback;
    }

    @Override
    public void setUserInfoCallback(UserInfoCallback userInfoCallback) {
        this.userInfoCallback = userInfoCallback;
    }

    @Override
    public void setAudioCallback(AudioCallback audioCallback) {
        this.audioCallback = audioCallback;
    }

    @Override
    public void setModuleCallback(ModuleCallback moduleCallback) {
        this.moduleCallback = moduleCallback;
    }

    @Override
    public void setListener(UiRefreshListener listener) {
        this.uiRefreshListener = listener;
    }

    @Override
    public void loginWithPassword(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showToast("还有信息未填写");
            return;
        }
        post(() -> {
            if (loginCallback != null) {
                loginCallback.onLoginResult(fakeUid(), username.trim());
            }
        });
    }

    @Override
    public void loginWithCaptcha(String bind, String captcha) {
        if (bind == null || bind.trim().isEmpty() || captcha == null || captcha.trim().isEmpty()) {
            showToast("还有信息未填写");
            return;
        }
        post(() -> {
            if (loginCallback != null) {
                loginCallback.onLoginResult(fakeUid(), bind.trim());
            }
        });
    }

    @Override
    public void getCaptcha(String code, String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            showToast("未填写手机号/邮箱！");
            return;
        }
        post(() -> showToast("验证码已发送（调试模式）"));
    }

    @Override
    public void changePassword(String bind, String captcha, String password, String passwordConfirm) {
        if (bind == null || bind.trim().isEmpty() || captcha == null || captcha.trim().isEmpty()) {
            showToast("还有内容未填写！");
            return;
        }
        if (password == null || passwordConfirm == null || !password.equals(passwordConfirm)) {
            showToast("密码确认有误！");
            return;
        }
        post(() -> showToast("密码修改成功！"));
    }

    @Override
    public void register(String bind, String captcha, String username, String password, String passwordConfirm) {
        if (bind == null || bind.trim().isEmpty() || captcha == null || captcha.trim().isEmpty()) {
            showToast("还有内容未填写！");
            return;
        }
        if (username == null || username.trim().isEmpty() || password == null || passwordConfirm == null) {
            showToast("还有内容未填写！");
            return;
        }
        if (!password.equals(passwordConfirm)) {
            showToast("密码确认有误！");
            return;
        }
        post(() -> {
            if (registerCallback != null) {
                registerCallback.onRegisterResult();
            }
        });
    }

    @Override
    public void uploadEvaluation(String uid, String childUser) {
        post(() -> {
            if (uploadEvaluationCallback != null) {
                try {
                    uploadEvaluationCallback.onUploadEvaluationResult("local_child_001");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void getEvaluations(String uid) {
        post(() -> {
            if (evaluationsCallback != null) {
                evaluationsCallback.onEvaluationsResult("[]");
            }
        });
    }

    @Override
    public void getEvaluationsLimit(String uid, int start, int num) {
        getEvaluations(uid);
    }

    @Override
    public void getEvaluation(String uid, String childUserID) {
        post(() -> {
            if (evaluationCallback != null) {
                try {
                    evaluationCallback.onEvaluationResult("{}");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void getEvaluationIDs(String uid) {
        post(() -> {
            if (evaluationIDsCallback != null) {
                try {
                    evaluationIDsCallback.onEvaluationIDsResult("[]");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void getUserIDs(String adminUid) {
        post(() -> {
            if (userIDsCallback != null) {
                userIDsCallback.onUserIDsResult("[]");
            }
        });
    }

    @Override
    public void getUserInfo(String uid) {
        post(() -> {
            if (userInfoCallback != null) {
                try {
                    JSONObject user = new JSONObject();
                    user.put("ID", uid == null ? "" : uid);
                    user.put("Username", "local_user");
                    user.put("PassWord", "");
                    user.put("Bind", "");
                    user.put("Time", "");
                    userInfoCallback.onUserInfoResult(user.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void deleteEvaluations(String admin, String uid) {
        post(() -> showToast("已删除测评（调试模式）"));
    }

    @Override
    public void deleteEvaluation(String uid, String childUserID) {
        post(() -> showToast("已删除测评（调试模式）"));
    }

    @Override
    public void deleteUserAdmin(String admin, String uid) {
        post(() -> showToast("已删除用户（调试模式）"));
    }

    @Override
    public void logoffUser(String uid, String bind, String captcha) {
        post(() -> showToast("已注销账号（调试模式）"));
    }

    @Override
    public void updateEvaluation(String uid, String childUserID, String childUser) {
        post(() -> showToast("已更新测评（调试模式）"));
    }

    @Override
    public void uploadAudio(String uid, String childUserID, String title, String num, String audioPath) {
        post(() -> showToast("已上传音频（调试模式）"));
    }

    @Override
    public void getAudio(String uid, String childUserID, String title, String num) {
        post(() -> {
            if (audioCallback != null) {
                audioCallback.onAudioResult("");
            }
        });
    }

    @Override
    public void createModule(String uid, JSONObject module) {
        post(() -> showToast("模块已创建（调试模式）"));
    }

    @Override
    public void deleteModule(String admin, String uid) {
        post(() -> showToast("模块已删除（调试模式）"));
    }

    @Override
    public void updateModule(String uid, JSONObject module) {
        post(() -> showToast("模块已更新（调试模式）"));
    }

    @Override
    public void getModule(String uid) {
        post(() -> {
            if (moduleCallback != null) {
                try {
                    moduleCallback.onModuleResult("{}");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void uploadPdf(String uid, String childUserID, String moduleType, String pdfPath) {
        post(() -> showToast("已上传PDF（调试模式）"));
    }

    private void post(Runnable runnable) {
        if (uiRefreshListener != null) {
            uiRefreshListener.refreshUI(true);
        }
        mainHandler.postDelayed(() -> {
            try {
                runnable.run();
            } finally {
                if (uiRefreshListener != null) {
                    uiRefreshListener.refreshUI(false);
                }
            }
        }, STUB_DELAY_MS);
    }

    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
    }

    private String fakeUid() {
        return "local_" + UUID.randomUUID().toString().replace("-", "");
    }
}
