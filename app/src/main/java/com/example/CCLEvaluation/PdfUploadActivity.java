package com.example.CCLEvaluation;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import utils.NetInteractUtils;

/**
 * PDF上传界面
 * 允许用户选择一个PDF文件并上传到服务器，关联特定的用户和儿童信息
 */
public class PdfUploadActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_PDF = 6101;

    private String uid;
    private String childUser;
    private Uri selectedUri;
    private File stagedFile;

    private TextView txtSelectedFile;
    private Spinner spinnerModule;

    private final String[] moduleKeys = new String[] { "A", "PL", "E", "SE", "RG", "SOCIAL", "overall", "child_info" };
    private final String[] moduleLabels = new String[] { "构音测试报告", "前语言测试报告", "词汇能力测试报告", "句法表达测试报告", "句法理解测试报告", "社交能力测试报告", "总体干预报告", "儿童信息报告" };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_upload);

        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");

        txtSelectedFile = findViewById(R.id.txt_selected_pdf);
        spinnerModule = findViewById(R.id.spinner_module);
        Button btnPick = findViewById(R.id.btn_pick_pdf);
        Button btnUpload = findViewById(R.id.btn_upload_pdf);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, moduleLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModule.setAdapter(adapter);

        btnPick.setOnClickListener(v -> openPdfPicker());
        btnUpload.setOnClickListener(v -> uploadSelectedPdf());
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_PICK_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PDF && resultCode == RESULT_OK && data != null) {
            selectedUri = data.getData();
            String name = getDisplayName(selectedUri);
            if (name == null || name.trim().isEmpty()) {
                name = "已选择PDF";
            }
            txtSelectedFile.setText(name);
        }
    }

    private void uploadSelectedPdf() {
        if (uid == null || uid.trim().isEmpty() || childUser == null || childUser.trim().isEmpty()) {
            Toast.makeText(this, "缺少用户或儿童信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedUri == null) {
            Toast.makeText(this, "请先选择PDF文件", Toast.LENGTH_SHORT).show();
            return;
        }
        String moduleType = moduleKeys[spinnerModule.getSelectedItemPosition()];
        try {
            stagedFile = stagePdf(selectedUri, moduleType);
        } catch (Exception e) {
            Toast.makeText(this, "PDF准备失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        NetInteractUtils.getInstance(this).uploadPdf(uid, childUser, moduleType, stagedFile.getAbsolutePath());
        Toast.makeText(this, "已开始上传", Toast.LENGTH_SHORT).show();
    }

    private File stagePdf(Uri uri, String moduleType) throws Exception {
        File cacheDir = new File(getCacheDir(), "pdf_uploads");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        String name = getDisplayName(uri);
        if (name == null || name.trim().isEmpty()) {
            name = "report_" + moduleType + "_" + System.currentTimeMillis() + ".pdf";
        }
        File outFile = new File(cacheDir, sanitizeFileName(name));
        ContentResolver resolver = getContentResolver();
        try (InputStream input = resolver.openInputStream(uri);
             FileOutputStream output = new FileOutputStream(outFile)) {
            if (input == null) {
                throw new IllegalStateException("无法读取PDF文件");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        return outFile;
    }

    private String getDisplayName(Uri uri) {
        if (uri == null) {
            return null;
        }
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String path = uri.getPath();
        if (path == null) {
            return null;
        }
        int last = path.lastIndexOf('/');
        return last >= 0 ? path.substring(last + 1) : path;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
