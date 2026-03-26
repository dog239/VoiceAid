package com.example.CCLEvaluation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bean.AssessmentModule;
import utils.ImageUrls;
import utils.NetInteractUtils;
import utils.OverallInterventionReportBuilder;
import utils.ReportPipeline;
import utils.dataManager;

public class SelectReportActivity extends AppCompatActivity {

    private RecyclerView reportRecycler;
    private AssessmentReportAdapter adapter;
    private List<AssessmentModule> reportList = new ArrayList<>();
    private String fName;
    private String uid;
    private String childUser;
    private JSONObject data;
    private MaterialButton btnViewOverall;

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

        // Load data to check plan status
        refreshData();

        // Fetch permissions to populate list
        if (isPrivate || uid == null || uid.isEmpty()) {
            updateReportList(null);
        } else {
            NetInteractUtils.getInstance(this).setModuleCallback(new NetInteractUtils.ModuleCallback() {
                @Override
                public void onModuleResult(String module) throws JSONException {
                    updateReportList(module);
                }
            });
            NetInteractUtils.getInstance(this).getModule(uid);
            
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
        if (data == null) return false;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return false;
        
        boolean hasCompletedRG = false;
        boolean hasCompletedSE = false;
        
        // 检查句法理解（RG）是否至少完成了一组
        for (int group = 1; group <= 4; group++) {
            JSONArray groupArray = evaluations.optJSONArray("RG" + group);
            if (groupArray != null && groupArray.length() > 0) {
                int completedCount = 0;
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject obj = groupArray.optJSONObject(i);
                    if (obj != null && obj.has("result") && !obj.isNull("result")) {
                        completedCount++;
                    }
                }
                if (completedCount == groupArray.length()) {
                    hasCompletedRG = true;
                    break;
                }
            }
        }
        
        // 检查句法表达（SE）是否至少完成了一组
        for (int group = 1; group <= 4; group++) {
            JSONArray groupArray = evaluations.optJSONArray("SE" + group);
            if (groupArray != null && groupArray.length() > 0) {
                int completedCount = 0;
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject obj = groupArray.optJSONObject(i);
                    if (obj != null && obj.has("result") && !obj.isNull("result")) {
                        completedCount++;
                    }
                }
                if (completedCount == groupArray.length()) {
                    hasCompletedSE = true;
                    break;
                }
            }
        }
        
        // 只有当句法理解和句法表达都至少完成了一组时，才认为句法能力模块已完成
        return hasCompletedRG && hasCompletedSE;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}
