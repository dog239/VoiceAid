package utils.netService;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONObject;

public class ModuleService {
    private final ApiConfig apiConfig;
    private final RequestExecutor executor;
    private final ReportService reportService;

    public ModuleService(ApiConfig apiConfig, RequestExecutor executor) {
        this.apiConfig = apiConfig;
        this.executor = executor;
        this.reportService = new ReportService(apiConfig, executor);
    }

    public void createModule(String uid, JSONObject module, VoidCallback callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("module", RequestData.jsonString(module))
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/module")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void deleteModule(String admin, String uid, VoidCallback callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/admins/" + admin + "/users/" + uid + "/module")
                .delete()
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void updateModule(String uid, JSONObject module, VoidCallback callback) {
        RequestBody requestBody = new FormBody.Builder()
                .add("module", RequestData.jsonString(module))
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/module")
                .put(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void getModule(String uid, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/module")
                .get()
                .build();
        executor.executeJson(request, json -> json.getJSONObject("module").toString(), callback);
    }

    public void uploadPdf(String uid, String childUserID, String moduleType, String pdfPath, VoidCallback callback) {
        reportService.uploadReport(uid, childUserID, moduleType, pdfPath, callback);
    }
}
