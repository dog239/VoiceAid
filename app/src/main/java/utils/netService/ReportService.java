package utils.netService;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;

public class ReportService {
    private static final MediaType PDF_MEDIA_TYPE = MediaType.parse("application/pdf");

    private final ApiConfig apiConfig;
    private final RequestExecutor executor;

    public ReportService(ApiConfig apiConfig, RequestExecutor executor) {
        this.apiConfig = apiConfig;
        this.executor = executor;
    }

    public void uploadReport(String uid, String childUserID, String moduleType, String pdfPath, VoidCallback callback) {
        File pdf = validateFile(pdfPath, callback);
        if (pdf == null) {
            return;
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("moduleType", moduleType == null ? "" : moduleType)
                .addFormDataPart("report", pdf.getName(), RequestBody.create(pdf, PDF_MEDIA_TYPE))
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/reports")
                .post(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void getReports(String uid, String childUserID, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/reports")
                .get()
                .build();
        executor.executeJson(request, json -> ResponseParsers.objectString(json, "reports", "[]"), callback);
    }

    public void getReport(String uid, String childUserID, String moduleType, ResultCallback<String> callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/reports/" + moduleType)
                .get()
                .build();
        executor.executeBinary(request,
                json -> {
                    String report = ResponseParsers.extractBinaryFromJson(json, "report", "pdf");
                    if (ResponseParsers.isBlank(report)) {
                        throw new ApiException("解析服务器数据错误！");
                    }
                    return report;
                },
                ResponseParsers::encodeBinary,
                callback);
    }

    public void updateReport(String uid, String childUserID, String moduleType, String pdfPath, VoidCallback callback) {
        File pdf = validateFile(pdfPath, callback);
        if (pdf == null) {
            return;
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("report", pdf.getName(), RequestBody.create(pdf, PDF_MEDIA_TYPE))
                .build();
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/reports/" + moduleType)
                .put(requestBody)
                .build();
        executor.executeWithoutResult(request, callback);
    }

    public void deleteReport(String uid, String childUserID, String moduleType, VoidCallback callback) {
        Request request = new Request.Builder()
                .url(apiConfig.getBaseUrl() + "/users/" + uid + "/children/" + childUserID + "/reports/" + moduleType)
                .delete()
                .build();
        executor.executeWithoutResult(request, callback);
    }

    private File validateFile(String path, VoidCallback callback) {
        File pdf = new File(path);
        if (!pdf.exists()) {
            callback.onError(new ApiException("PDF文件不存在"));
            return null;
        }
        return pdf;
    }
}
