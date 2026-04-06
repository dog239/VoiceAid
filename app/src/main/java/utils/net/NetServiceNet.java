package utils.net;

import android.app.Activity;
import android.widget.Toast;

import org.json.JSONObject;

import utils.netService.NetInteractUtils;

/**
 * utils/NetServiceNet.java 将接口调用转发到 utils.netService.NetInteractUtils，后续对方完成时仅需维护这里。
 */
public class NetServiceNet implements NetService {
    private final NetInteractUtils net;

    public NetServiceNet(Activity activity) {
        this.net = NetInteractUtils.getInstance(activity);
        this.net.setUiRunner(activity::runOnUiThread);
        this.net.setMessageHandler(message -> activity.runOnUiThread(
                () -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()));
    }

    @Override
    public void setLoginCallback(LoginCallback loginCallback) {
        net.setLoginCallback((uid, username) -> loginCallback.onLoginResult(uid, username));
    }

    @Override
    public void setRegisterCallback(RegisterCallback registerCallback) {
        net.setRegisterCallback(registerCallback::onRegisterResult);
    }

    @Override
    public void setListener(UiRefreshListener listener) {
        net.setListener(listener::refreshUI);
    }

    @Override
    public void setUploadEvaluationCallback(UploadEvaluationCallback uploadEvaluationCallback) {
        net.setUploadEvaluationCallback(childUserID -> uploadEvaluationCallback.onUploadEvaluationResult(childUserID));
    }

    @Override
    public void setEvaluationCallback(EvaluationCallback evaluationCallback) {
        net.setEvaluationCallback(evaluation -> evaluationCallback.onEvaluationResult(evaluation));
    }

    @Override
    public void setEvaluationsCallback(EvaluationsCallback evaluationsCallback) {
        net.setEvaluationsCallback(evaluationsCallback::onEvaluationsResult);
    }

    @Override
    public void setEvaluationIDsCallback(EvaluationIDsCallback evaluationIDsCallback) {
        net.setEvaluationIDsCallback(evaluationIDs -> evaluationIDsCallback.onEvaluationIDsResult(evaluationIDs));
    }

    @Override
    public void setUserIDsCallback(UserIDsCallback userIDsCallback) {
        net.setUserIDsCallback(userIDsCallback::onUserIDsResult);
    }

    @Override
    public void setUserInfoCallback(UserInfoCallback userInfoCallback) {
        net.setUserInfoCallback(user -> userInfoCallback.onUserInfoResult(user));
    }

    @Override
    public void setAudioCallback(AudioCallback audioCallback) {
        net.setAudioCallback(audioCallback::onAudioResult);
    }

    @Override
    public void setModuleCallback(ModuleCallback moduleCallback) {
        net.setModuleCallback(module -> moduleCallback.onModuleResult(module));
    }

    @Override
    public void loginWithPassword(String username, String password) {
        net.loginWithPassword(username, password);
    }

    @Override
    public void loginWithCaptcha(String bind, String captcha) {
        net.loginWithCaptcha(bind, captcha);
    }

    @Override
    public void getCaptcha(String code, String contact) {
        net.getCaptcha(code, contact);
    }

    @Override
    public void changePassword(String bind, String captcha, String password, String passwordConfirm) {
        net.changePassword(bind, captcha, password, passwordConfirm);
    }

    @Override
    public void register(String bind, String captcha, String username, String password, String passwordConfirm) {
        net.register(bind, captcha, username, password, passwordConfirm);
    }

    @Override
    public void uploadEvaluation(String uid, String childUser) {
        net.uploadEvaluation(uid, childUser);
    }

    @Override
    public void getEvaluations(String uid) {
        net.getEvaluations(uid);
    }

    @Override
    public void getEvaluationsLimit(String uid, int start, int num) {
        net.getEvaluationsLimit(uid, start, num);
    }

    @Override
    public void getEvaluation(String uid, String childUserID) {
        net.getEvaluation(uid, childUserID);
    }

    @Override
    public void getEvaluationIDs(String uid) {
        net.getEvaluationIDs(uid);
    }

    @Override
    public void getUserIDs(String adminUid) {
        net.getUserIDs(adminUid);
    }

    @Override
    public void getUserInfo(String uid) {
        net.getUserInfo(uid);
    }

    @Override
    public void deleteEvaluations(String admin, String uid) {
        net.deleteEvaluations(admin, uid);
    }

    @Override
    public void deleteEvaluation(String uid, String childUserID) {
        net.deleteEvaluation(uid, childUserID);
    }

    @Override
    public void deleteUserAdmin(String admin, String uid) {
        net.deleteUserAdmin(admin, uid);
    }

    @Override
    public void logoffUser(String uid, String bind, String captcha) {
        net.logoffUser(uid, bind, captcha);
    }

    @Override
    public void updateEvaluation(String uid, String childUserID, String childUser) {
        net.updateEvaluation(uid, childUserID, childUser);
    }

    @Override
    public void uploadAudio(String uid, String childUserID, String title, String num, String audioPath) {
        net.uploadAudio(uid, childUserID, title, num, audioPath);
    }

    @Override
    public void getAudio(String uid, String childUserID, String title, String num) {
        net.getAudio(uid, childUserID, title, num);
    }

    @Override
    public void createModule(String uid, JSONObject module) {
        net.createModule(uid, module);
    }

    @Override
    public void deleteModule(String admin, String uid) {
        net.deleteModule(admin, uid);
    }

    @Override
    public void updateModule(String uid, JSONObject module) {
        net.updateModule(uid, module);
    }

    @Override
    public void getModule(String uid) {
        net.getModule(uid);
    }

    @Override
    public void uploadPdf(String uid, String childUserID, String moduleType, String pdfPath) {
        net.uploadPdf(uid, childUserID, moduleType, pdfPath);
    }
}
