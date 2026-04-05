package utils.net;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 通过 utils/NetService.java 抽出稳定接口，UI 只依赖接口，后续替换无需改 UI
 */
public interface NetService {
    interface UiRefreshListener {
        void refreshUI(Boolean isPlaying);
    }

    interface LoginCallback {
        void onLoginResult(String uid, String username);
    }

    interface RegisterCallback {
        void onRegisterResult();
    }

    interface UploadEvaluationCallback {
        void onUploadEvaluationResult(String childUserID) throws Exception;
    }

    interface EvaluationCallback {
        void onEvaluationResult(String evaluation) throws JSONException;
    }

    interface EvaluationsCallback {
        void onEvaluationsResult(String evaluations);
    }

    interface EvaluationIDsCallback {
        void onEvaluationIDsResult(String evaluationIDs) throws Exception;
    }

    interface UserIDsCallback {
        void onUserIDsResult(String userIDs);
    }

    interface UserInfoCallback {
        void onUserInfoResult(String user) throws Exception;
    }

    interface AudioCallback {
        void onAudioResult(String audio);
    }

    interface ModuleCallback {
        void onModuleResult(String module) throws JSONException;
    }

    void setLoginCallback(LoginCallback loginCallback);

    void setRegisterCallback(RegisterCallback registerCallback);

    void setListener(UiRefreshListener listener);

    void setUploadEvaluationCallback(UploadEvaluationCallback uploadEvaluationCallback);

    void setEvaluationCallback(EvaluationCallback evaluationCallback);

    void setEvaluationsCallback(EvaluationsCallback evaluationsCallback);

    void setEvaluationIDsCallback(EvaluationIDsCallback evaluationIDsCallback);

    void setUserIDsCallback(UserIDsCallback userIDsCallback);

    void setUserInfoCallback(UserInfoCallback userInfoCallback);

    void setAudioCallback(AudioCallback audioCallback);

    void setModuleCallback(ModuleCallback moduleCallback);

    void loginWithPassword(String username, String password);

    void loginWithCaptcha(String bind, String captcha);

    void getCaptcha(String code, String contact);

    void changePassword(String bind, String captcha, String password, String passwordConfirm);

    void register(String bind, String captcha, String username, String password, String passwordConfirm);

    void uploadEvaluation(String uid, String childUser);

    @Deprecated
    void getEvaluations(String uid);

    void getEvaluationsLimit(String uid, int start, int num);

    void getEvaluation(String uid, String childUserID);

    void getEvaluationIDs(String uid);

    void getUserIDs(String adminUid);

    void getUserInfo(String uid);

    void deleteEvaluations(String admin, String uid);

    void deleteEvaluation(String uid, String childUserID);

    void deleteUserAdmin(String admin, String uid);

    void logoffUser(String uid, String bind, String captcha);

    void updateEvaluation(String uid, String childUserID, String childUser);

    void uploadAudio(String uid, String childUserID, String title, String num, String audioPath);

    void getAudio(String uid, String childUserID, String title, String num);

    void createModule(String uid, JSONObject module);

    void deleteModule(String admin, String uid);

    void updateModule(String uid, JSONObject module);

    void getModule(String uid);

    void uploadPdf(String uid, String childUserID, String moduleType, String pdfPath);
}
