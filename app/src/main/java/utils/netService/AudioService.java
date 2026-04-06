package utils.netService;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;

public class AudioService {
    private static final MediaType DEFAULT_AUDIO_MEDIA_TYPE = MediaType.parse("audio/amr");

    private final ApiConfig apiConfig;
    private final RequestExecutor executor;

    public AudioService(ApiConfig apiConfig, RequestExecutor executor) {
        this.apiConfig = apiConfig;
        this.executor = executor;
    }

    public void uploadAudio(String uid, String childUserID, String title, String num, String audioPath, VoidCallback callback) {
        File audio = validateFile(audioPath, "音频文件不存在", callback);
        if (audio == null) {
            return;
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("moduleType", title)
                .addFormDataPart("num", num)
                .addFormDataPart("audio", audio.getName(), RequestBody.create(audio, DEFAULT_AUDIO_MEDIA_TYPE))
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/audios")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void getAudios(String uid, String childUserID, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/audios")
                .get()
                .build();
        executor.executeJson(request, json -> ResponseParsers.objectString(json, "audios", "[]"), callback);
    }

    public void getAudio(String uid, String childUserID, String title, String num, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/audios/" + title + "/" + num)
                .get()
                .build();
        executor.executeBinary(request,
                json -> {
                    String audio = ResponseParsers.extractAudioFromJson(json);
                    if (ResponseParsers.isBlank(audio)) {
                        throw new ApiException("解析服务器数据错误！");
                    }
                    return audio;
                },
                ResponseParsers::encodeBinary,
                callback);
    }

    public void updateAudio(String uid, String childUserID, String moduleType, String num, String audioPath, VoidCallback callback) {
        File audio = validateFile(audioPath, "音频文件不存在", callback);
        if (audio == null) {
            return;
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio", audio.getName(), RequestBody.create(audio, DEFAULT_AUDIO_MEDIA_TYPE))
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/audios/" + moduleType + "/" + num)
                .put(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void deleteAudio(String uid, String childUserID, String moduleType, String num, VoidCallback callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/audios/" + moduleType + "/" + num)
                .delete()
                .build();
        executor.executeWithoutResult(request, callback);
    }

    private File validateFile(String path, String message, VoidCallback callback) {
        File file = new File(path);
        if (!file.exists()) {
            callback.onError(new ApiException(message));
            return null;
        }
        return file;
    }
}
