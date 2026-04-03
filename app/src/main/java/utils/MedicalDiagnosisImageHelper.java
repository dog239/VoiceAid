package utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MedicalDiagnosisImageHelper {

    public static final String FIELD_MEDICAL_DOCUMENTS = "medicalDocuments";
    private static final String IMAGE_DIR = "medical_diagnosis";

    private MedicalDiagnosisImageHelper() {
    }

    public static JSONArray optMedicalDocuments(JSONObject backgroundInfo) {
        if (backgroundInfo == null) {
            return new JSONArray();
        }
        JSONArray array = backgroundInfo.optJSONArray(FIELD_MEDICAL_DOCUMENTS);
        return array == null ? new JSONArray() : array;
    }

    public static List<JSONObject> toObjectList(JSONArray array) {
        List<JSONObject> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) {
                result.add(cloneObject(item));
            }
        }
        return result;
    }

    public static List<JSONObject> cloneList(List<JSONObject> source) {
        List<JSONObject> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (JSONObject item : source) {
            result.add(cloneObject(item));
        }
        return result;
    }

    public static JSONArray toJsonArray(List<JSONObject> items) {
        JSONArray array = new JSONArray();
        if (items == null) {
            return array;
        }
        for (JSONObject item : items) {
            if (hasImage(item)) {
                array.put(cloneObject(item));
            }
        }
        return array;
    }

    public static JSONObject cloneObject(JSONObject source) {
        if (source == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(source.toString());
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    public static File createImageFile(Context context, String caseIdHint, String extension) throws IOException {
        File dir = resolveImageDir(context);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create medical diagnosis image directory.");
        }
        String cleanExt = sanitizeExtension(extension);
        String prefix = sanitizeFileName(caseIdHint);
        if (prefix.isEmpty()) {
            prefix = "medical_diagnosis";
        }
        String fileName = prefix + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + cleanExt;
        return new File(dir, fileName);
    }

    public static JSONObject copyImageToAppStorage(Context context, Uri sourceUri, String caseIdHint) throws IOException, JSONException {
        if (context == null || sourceUri == null) {
            throw new IOException("Missing image source.");
        }
        ContentResolver resolver = context.getContentResolver();
        String mimeType = resolver.getType(sourceUri);
        File targetFile = createImageFile(context, caseIdHint, guessExtension(mimeType));
        try (InputStream input = resolver.openInputStream(sourceUri);
             OutputStream output = new FileOutputStream(targetFile)) {
            if (input == null) {
                throw new IOException("Unable to open selected image.");
            }
            copyStream(input, output);
        }
        return buildImageMetadata(targetFile, mimeType, "");
    }

    public static JSONObject buildImageMetadata(File file, String mimeType, String remoteUrl) throws JSONException {
        JSONObject image = new JSONObject();
        if (file == null) {
            return image;
        }
        String safeMimeType = TextUtils.isEmpty(mimeType) ? guessMimeType(file.getName()) : mimeType;
        image.put("localPath", file.getAbsolutePath());
        image.put("remoteUrl", remoteUrl == null ? "" : remoteUrl.trim());
        image.put("fileName", file.getName());
        image.put("mimeType", safeMimeType);
        image.put("lastUpdated", System.currentTimeMillis());
        return image;
    }

    public static boolean hasImage(JSONObject image) {
        if (image == null) {
            return false;
        }
        String localPath = image.optString("localPath", "").trim();
        String remoteUrl = image.optString("remoteUrl", "").trim();
        return !localPath.isEmpty() || !remoteUrl.isEmpty();
    }

    public static String getLocalPath(JSONObject image) {
        return image == null ? "" : image.optString("localPath", "").trim();
    }

    public static String getFileName(JSONObject image) {
        return image == null ? "" : image.optString("fileName", "").trim();
    }

    public static void deleteImageFile(JSONObject image) {
        deleteFileQuietly(new File(getLocalPath(image)));
    }

    public static void deleteFileQuietly(File file) {
        if (file == null) {
            return;
        }
        try {
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private static File resolveImageDir(Context context) {
        File base = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (base == null) {
            base = context.getFilesDir();
        }
        return new File(base, IMAGE_DIR);
    }

    private static String guessExtension(String mimeType) {
        if (!TextUtils.isEmpty(mimeType)) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!TextUtils.isEmpty(extension)) {
                return "." + extension;
            }
        }
        return ".jpg";
    }

    private static String guessMimeType(String fileName) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (!TextUtils.isEmpty(extension)) {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.getDefault()));
            if (!TextUtils.isEmpty(mime)) {
                return mime;
            }
        }
        return "image/jpeg";
    }

    private static String sanitizeExtension(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return ".jpg";
        }
        return extension.startsWith(".") ? extension : "." + extension;
    }

    private static String sanitizeFileName(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        output.flush();
    }
}
