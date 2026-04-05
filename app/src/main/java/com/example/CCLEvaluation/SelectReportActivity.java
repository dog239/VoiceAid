package com.example.CCLEvaluation;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bean.AssessmentModule;
import utils.ImageUrls;
import utils.OverallInterventionReportBuilder;
import utils.dataManager;
import utils.net.NetService;
import utils.net.NetServiceProvider;

public class SelectReportActivity extends AppCompatActivity {

    private RecyclerView reportRecycler;
    private AssessmentReportAdapter adapter;
    private List<AssessmentModule> reportList = new ArrayList<>();
    private String fName;
    private String uid;
    private String childUser;
    private JSONObject data;
    private MaterialButton btnViewOverall;
    private MaterialButton btnExportReport;
    private MaterialButton btnUploadReport;
    private static final int REQUEST_EXPORT_ASSESSMENT_PDF = 3101;
    private final List<ExportOption> pendingExports = new ArrayList<>();
    private ExportOption currentExport;
    private NetService netService;

    private static class ExportOption {
        final String moduleType;
        final String label;
        final boolean ready;

        ExportOption(String moduleType, String label, boolean ready) {
            this.moduleType = moduleType;
            this.label = label;
            this.ready = ready;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_report);

        initViews();
        initData();
    }

    private void initViews() {
        reportRecycler = findViewById(R.id.recycler_view);
        reportRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AssessmentReportAdapter(reportList, this::onReportClick);
        reportRecycler.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SelectReportActivity.this, history.showchildinformation.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            finish();
        });

        btnViewOverall = findViewById(R.id.btn_view_overall);
        btnViewOverall.setOnClickListener(v -> onOverallReportClick());

        btnExportReport = findViewById(R.id.btn_export_report);
        if (btnExportReport != null) {
            btnExportReport.setOnClickListener(v -> onExportReportClick());
        }

        btnUploadReport = findViewById(R.id.btn_upload_report);
        if (btnUploadReport != null) {
            btnUploadReport.setOnClickListener(v -> {
                Intent intent = new Intent(SelectReportActivity.this, PdfUploadActivity.class);
                intent.putExtra("Uid", uid);
                intent.putExtra("childID", childUser);
                startActivity(intent);
            });
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_reports);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_reports) {
            return true;
        } else if (item.getItemId() == R.id.nav_modules) {
            Intent intent = new Intent(this, AssessmentModulesActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        } else if (item.getItemId() == R.id.nav_profile) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void initData() {
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
        boolean isPrivate = getIntent().getBooleanExtra("private", false);
        netService = NetServiceProvider.get(this);

        // Load data to check plan status
        refreshData();

        // Fetch permissions to populate list
        if (isPrivate || uid == null || uid.isEmpty()) {
            updateReportList(null);
        } else {
            netService.setModuleCallback(module -> updateReportList(module));
            netService.getModule(uid);

            // Fallback init
            updateReportList(null);
        }
    }

    private void refreshData() {
        try {
            if (fName != null) {
                data = dataManager.getInstance().loadData(fName);
                updateOverallButtonState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOverallButtonState() {
        if (OverallInterventionReportBuilder.isReady(data)) {
            btnViewOverall.setText("查看总体干预报告");
            btnViewOverall.setEnabled(true);
            btnViewOverall.setBackgroundTintList(getColorStateList(R.color.teal_700));
            btnViewOverall.setTextColor(getColor(R.color.white));
            btnViewOverall.setIconTint(getColorStateList(R.color.white));
        } else {
            disableOverallButton();
        }
    }
    
    private void disableOverallButton() {
        btnViewOverall.setText("查看总体干预报告");
        btnViewOverall.setEnabled(false);
        btnViewOverall.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2E8F0")));
        btnViewOverall.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
        btnViewOverall.setIconTint(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#94A3B8")));
    }

    private void updateReportList(String moduleJson) {
        String E = "1", A = "1", RG = "1", PL = "1";
        
        if (moduleJson != null && !moduleJson.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(moduleJson);
                E = jsonObject.optString("E", "1");
                A = jsonObject.optString("A", "1");
                RG = jsonObject.optString("RG", "1");
                PL = jsonObject.optString("PL", "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        reportList.clear();

        if ("1".equals(A)) {
            AssessmentModule m = new AssessmentModule("A", "构音评估", "发音准确性与清晰度报告", "", android.R.drawable.ic_btn_speak_now);
            m.setCompleted(checkCompletion("A", ImageUrls.A_imageUrls.length)); 
            m.setLastTestDate(getTestDate("A"));
            m.setReportGenerated(checkReportGenerated("A"));
            reportList.add(m);
        }
        
        if ("1".equals(PL)) {
            AssessmentModule m = new AssessmentModule("PL", "前语言能力", "前语言沟通技能报告", "", android.R.drawable.ic_menu_agenda);
            m.setCompleted(checkCompletion("PL", ImageUrls.PL_SKILLS.length));
            m.setLastTestDate(getTestDate("PL"));
            m.setReportGenerated(checkReportGenerated("PL"));
            reportList.add(m);
        }

        if ("1".equals(E)) {
            AssessmentModule m = new AssessmentModule("E", "词汇能力", "词汇理解与表达报告", "", android.R.drawable.ic_menu_sort_by_size);
            m.setCompleted(isVocabularyReportReady());
            m.setLastTestDate(getTestDate("E"));
            m.setReportGenerated(checkReportGenerated("E"));
            reportList.add(m);
        }
        
        if ("1".equals(RG)) {
            AssessmentModule m = new AssessmentModule("RG", "句法能力", "句法理解与表达报告", "", android.R.drawable.ic_menu_edit);
            m.setCompleted(checkSyntaxCompletion()); 
            m.setLastTestDate(getTestDate("RG"));
            m.setReportGenerated(checkReportGenerated("RG"));
            reportList.add(m);
        }
        
        AssessmentModule m = new AssessmentModule("SOCIAL", "社交能力", "社交互动技能报告", "", android.R.drawable.ic_menu_myplaces);
        m.setCompleted(checkSocialCompletion());
        m.setLastTestDate(getTestDate("SOCIAL"));
        m.setReportGenerated(checkReportGenerated("SOCIAL"));
        reportList.add(m);

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private boolean checkCompletion(String key, int targetLength) {
        if (data == null) return false;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return false;
        
        if ("PL".equals(key)) {
            int required = targetLength > 0 ? targetLength : ImageUrls.PL_SKILLS.length;
            int bestAnswered = 0;
            bestAnswered = Math.max(bestAnswered, countAnsweredUniqueByNum(evaluations.optJSONArray("PL"), required));
            bestAnswered = Math.max(bestAnswered, countAnsweredUniqueByNum(evaluations.optJSONArray("PL_A"), required));
            bestAnswered = Math.max(bestAnswered, countAnsweredUniqueByNum(evaluations.optJSONArray("PL_B"), required));
            return bestAnswered >= required;
        }
        
        if ("SOCIAL".equals(key)) {
            int totalAnswered = 0;
            // 检查主 SOCIAL 数组
            JSONArray mainArray = evaluations.optJSONArray(key);
            if (mainArray != null) {
                for (int i = 0; i < mainArray.length(); i++) {
                    try {
                        if (mainArray.getJSONObject(i).has("time") && !mainArray.getJSONObject(i).isNull("time"))
                            totalAnswered++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 检查每个组的数组
            for (int i = 1; i <= 6; i++) {
                JSONArray groupArray = evaluations.optJSONArray(key + i);
                if (groupArray != null) {
                    for (int j = 0; j < groupArray.length(); j++) {
                        try {
                            if (groupArray.getJSONObject(j).has("time") && !groupArray.getJSONObject(j).isNull("time"))
                                totalAnswered++;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return totalAnswered >= targetLength;
        }

        JSONArray array = evaluations.optJSONArray(key);
        if (array == null) return false;

        int len = 0;
        for (int i = 0; i < array.length(); i++) {
            try {
                if (array.getJSONObject(i).has("time") && !array.getJSONObject(i).isNull("time"))
                    len++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return len >= targetLength;
    }

    private int countAnsweredUniqueByNum(JSONArray array, int maxNum) {
        if (array == null || array.length() == 0 || maxNum <= 0) return 0;
        java.util.HashSet<Integer> answeredNums = new java.util.HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null || !obj.has("time") || obj.isNull("time")) {
                continue;
            }
            int num = obj.optInt("num", -1);
            if (num >= 1 && num <= maxNum) {
                answeredNums.add(num);
            }
        }
        return answeredNums.size();
    }

    private String getTestDate(String key) {
        // Retrieve date from last item in array? Or from 'info' object?
        // evmenuactivity doesn't show date on buttons.
        // Assuming we can get today's date or stored date.
        // For now returning placeholder or actual logic if available.
        // Let's use current date format if available in JSON.
        return "2023-10-25"; // Placeholder, needs actual date logic implementation
    }
    
    private boolean checkReportGenerated(String key) {
        if (data == null) return false;
        
        String moduleType = "";
        switch (key) {
            case "A": moduleType = "articulation"; break;
            case "PL": moduleType = "prelinguistic"; break;
            case "E": moduleType = "vocabulary"; break;
            case "RG": moduleType = "syntax"; break;
            case "SOCIAL": moduleType = "social"; break;
            default: return false;
        }
        
        JSONObject guide = utils.ModuleReportHelper.loadModuleInterventionGuide(data, moduleType);
        return guide != null && guide.length() > 0;
    }

    private void onReportClick(AssessmentModule module) {
        if ("RG".equals(module.getId())) {
            showSyntaxReportSelectionDialog();
            return;
        }

        if ("E".equals(module.getId()) && !isVocabularyReportReady()) {
            Toast.makeText(this, "请先完成词汇理解和词汇表达后再查看报告", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, resultactivity.class);
        intent.putExtra("fName", fName);
        intent.putExtra("format", module.getId());
        startActivity(intent);
    }

    private void showSyntaxReportSelectionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择句法报告类型");
        String[] options = {"句法理解测试报告", "句法表达测试报告"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // 句法理解
                checkAndOpenSyntaxReport("RG");
            } else {
                // 句法表达
                checkAndOpenSyntaxReport("SE");
            }
        });
        builder.show();
    }

    private void checkAndOpenSyntaxReport(String format) {
        if (data == null || !data.has("evaluations")) {
            Toast.makeText(this, "暂无评估数据", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject evaluations = data.optJSONObject("evaluations");
        boolean hasCompletedAnyGroup = false;

        // 检查是否有任何一组题已经完成 (RG1-RG4 或 SE1-SE4)
        for (int i = 1; i <= 4; i++) {
            String key = format + i;
            JSONArray jsonArray = evaluations.optJSONArray(key);
            if (jsonArray != null && jsonArray.length() > 0) {
                // 检查该组是否所有题目都已完成
                boolean allQuestionsCompleted = true;
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject object = jsonArray.optJSONObject(j);
                    if (object == null || !object.has("time") || object.isNull("time")) {
                        allQuestionsCompleted = false;
                        break;
                    }
                }
                if (allQuestionsCompleted) {
                    hasCompletedAnyGroup = true;
                    break;
                }
            }
        }

        if (hasCompletedAnyGroup) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", format);
            startActivity(intent);
        } else {
            String typeName = "RG".equals(format) ? "句法理解" : "句法表达";
            Toast.makeText(this, "请先完成至少一组" + typeName + "测试题目再查看报告", Toast.LENGTH_SHORT).show();
        }
    }

    private void onOverallReportClick() {
        Intent intent = new Intent(this, TreatmentPlanActivity.class);
        intent.putExtra("fName", fName);
        intent.putExtra(TreatmentPlanActivity.EXTRA_REPORT_MODE,
                OverallInterventionReportBuilder.REPORT_MODE_OVERALL_INTERVENTION);
        startActivity(intent);
    }

    private void onExportReportClick() {
        refreshData();
        if (data == null) {
            Toast.makeText(this, "暂无评估数据", Toast.LENGTH_SHORT).show();
            return;
        }
        List<ExportOption> options = buildExportOptions();
        if (options.isEmpty()) {
            Toast.makeText(this, "暂无可导出的测评模块", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[options.size()];
        boolean[] checked = new boolean[options.size()];
        for (int i = 0; i < options.size(); i++) {
            ExportOption option = options.get(i);
            labels[i] = option.ready ? option.label : option.label + "（未完成）";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择要导出的测评报告");
        builder.setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
        builder.setPositiveButton("导出", (dialog, which) -> startExportSelections(options, checked));
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private List<ExportOption> buildExportOptions() {
        List<ExportOption> options = new ArrayList<>();
        options.add(new ExportOption("articulation", "构音评估报告",
                checkCompletion("A", ImageUrls.A_imageUrls.length)));
        options.add(new ExportOption("prelinguistic", "前语言评估报告",
                checkCompletion("PL", ImageUrls.PL_SKILLS.length)));
        options.add(new ExportOption("vocabulary", "词汇评估报告", isVocabularyReportReady()));
        options.add(new ExportOption("syntax_comprehension", "句法理解评估报告", checkSyntaxComprehensionCompletion()));
        options.add(new ExportOption("syntax_expression", "句法表达评估报告", checkSyntaxExpressionCompletion()));
        options.add(new ExportOption("social", "社交评估报告", checkSocialCompletion()));
        return options;
    }

    private void startExportSelections(List<ExportOption> options, boolean[] checked) {
        pendingExports.clear();
        boolean hasNotReady = false;
        for (int i = 0; i < options.size(); i++) {
            if (!checked[i]) {
                continue;
            }
            ExportOption option = options.get(i);
            if (option.ready) {
                pendingExports.add(option);
            } else {
                hasNotReady = true;
            }
        }
        if (pendingExports.isEmpty()) {
            Toast.makeText(this, hasNotReady ? "所选模块尚未完成" : "未选择导出模块", Toast.LENGTH_SHORT).show();
            return;
        }
        exportNextModule();
    }

    private void exportNextModule() {
        if (pendingExports.isEmpty()) {
            currentExport = null;
            Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
            return;
        }
        currentExport = pendingExports.remove(0);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, buildAssessmentPdfFileName(data, currentExport));
        startActivityForResult(intent, REQUEST_EXPORT_ASSESSMENT_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        if (requestCode == REQUEST_EXPORT_ASSESSMENT_PDF) {
            if (resultCode != RESULT_OK || dataIntent == null) {
                pendingExports.clear();
                currentExport = null;
                return;
            }
            Uri uri = dataIntent.getData();
            if (uri == null || currentExport == null) {
                Toast.makeText(this, "未获取保存位置", Toast.LENGTH_SHORT).show();
                pendingExports.clear();
                currentExport = null;
                return;
            }
            try {
                ContentResolver resolver = getContentResolver();
                OutputStream outputStream = resolver.openOutputStream(uri);
                if (outputStream == null) {
                    throw new IllegalStateException("无法打开输出流");
                }
                new PdfGenerator(this);
                PdfGenerator.generateEvaluationPdf(outputStream, fName, currentExport.moduleType);
                outputStream.flush();
                outputStream.close();
                exportNextModule();
            } catch (Exception e) {
                Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                pendingExports.clear();
                currentExport = null;
            }
        }
    }

    private String buildAssessmentPdfFileName(JSONObject data, ExportOption option) {
        String name = "未命名";
        String date = "";
        if (data != null) {
            JSONObject info = data.optJSONObject("info");
            if (info != null) {
                name = info.optString("name", name);
                date = info.optString("testDate", "");
            }
        }
        if (date == null || date.trim().isEmpty()) {
            date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        }
        String raw = name + "_" + option.label + "_" + date + ".pdf";
        return raw.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private boolean isVocabularyReportReady() {
        return checkCompletion("E", 7) && checkCompletion("EV", 7);
    }
    
    private boolean checkSocialCompletion() {
        if (data == null) return false;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return false;
        
        // 检查是否有任何一组社交能力测试已经完成
        for (int i = 1; i <= 6; i++) {
            String socialKey = "SOCIAL" + i;
            JSONArray socialArray = evaluations.optJSONArray(socialKey);
            if (socialArray != null && socialArray.length() > 0) {
                // 检查该组是否有完成的题目
                for (int j = 0; j < socialArray.length(); j++) {
                    JSONObject item = socialArray.optJSONObject(j);
                    if (item != null && item.has("time") && !item.isNull("time")) {
                        return true;
                    }
                }
            }
        }
        
        // 检查主 SOCIAL 数组
        JSONArray mainArray = evaluations.optJSONArray("SOCIAL");
        if (mainArray != null && mainArray.length() > 0) {
            for (int i = 0; i < mainArray.length(); i++) {
                JSONObject item = mainArray.optJSONObject(i);
                if (item != null && item.has("time") && !item.isNull("time")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean checkSyntaxCompletion() {
        return checkSyntaxComprehensionCompletion() && checkSyntaxExpressionCompletion();
    }

    private boolean checkSyntaxComprehensionCompletion() {
        if (data == null) return false;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return false;

        for (int group = 1; group <= 4; group++) {
            JSONArray groupArray = evaluations.optJSONArray("RG" + group);
            if (groupArray != null && groupArray.length() > 0) {
                int completedCount = 0;
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject obj = groupArray.optJSONObject(i);
                    if (obj != null && obj.has("time") && !obj.isNull("time")) {
                        completedCount++;
                    }
                }
                if (completedCount == groupArray.length()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSyntaxExpressionCompletion() {
        if (data == null) return false;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return false;

        for (int group = 1; group <= 4; group++) {
            JSONArray groupArray = evaluations.optJSONArray("SE" + group);
            if (groupArray != null && groupArray.length() > 0) {
                int completedCount = 0;
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject obj = groupArray.optJSONObject(i);
                    if (obj != null && obj.has("time") && !obj.isNull("time")) {
                        completedCount++;
                    }
                }
                if (completedCount == groupArray.length()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}
