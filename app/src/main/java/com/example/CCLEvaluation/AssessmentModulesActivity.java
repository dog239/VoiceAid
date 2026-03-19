package com.example.CCLEvaluation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bean.AssessmentModule;
import bean.evaluation;
import utils.ImageUrls;
import utils.NetInteractUtils;
import utils.dataManager;

public class AssessmentModulesActivity extends AppCompatActivity {

    private RecyclerView modulesRecycler;
    private AssessmentModuleAdapter adapter;
    private List<AssessmentModule> moduleList = new ArrayList<>();
    private String fName;
    private String uid;
    private String childUser;
    private JSONObject data;
    private TextView patientName;
    private TextView patientAge; // Reusing this for ID or Age
    private TextView patientId;
    private String lastModuleJson;
    private boolean isPrivateMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_modules);

        initViews();
        initData();
    }

    private void initViews() {
        modulesRecycler = findViewById(R.id.modules_recycler);
        modulesRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AssessmentModuleAdapter(moduleList, this::onModuleClick);
        modulesRecycler.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AssessmentModulesActivity.this, history.showchildinformation.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            finish();
        });
        
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_modules); // Set current item
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_modules) {
            return true;
        } else if (item.getItemId() == R.id.nav_reports) {
            Intent intent = new Intent(this, SelectReportActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            overridePendingTransition(0, 0); // Seamless transition
            return true;
        } else if (item.getItemId() == R.id.nav_profile) {
            // Jump to User Profile
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
        isPrivateMode = getIntent().getBooleanExtra("private", false);

        // Load data from file
        try {
            data = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
        }

        // Fetch permissions/modules from backend
        if (isPrivateMode || uid == null || uid.isEmpty()) {
            // Private mode or no UID: show all modules by default
            lastModuleJson = null;
            updateModulesList(null); 
        } else {
            NetInteractUtils.getInstance(this).setModuleCallback(new NetInteractUtils.ModuleCallback() {
                @Override
                public void onModuleResult(String module) throws JSONException {
                    lastModuleJson = module;
                    updateModulesList(module);
                }
            });
            NetInteractUtils.getInstance(this).getModule(uid);
            
            // Fallback: if network is slow/fails, show defaults after delay? 
            // Or just init with defaults and let network update it.
            updateModulesList(null);
        }
    }

    private void updateModulesList(String moduleJson) {
        String E = "1", A = "1", RG = "1", PL = "1"; // Default to enabled
        
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

        moduleList.clear();

        // 1. 构音 (A)
        if ("1".equals(A)) {
            AssessmentModule m = new AssessmentModule("A", "构音评估", "评估语音放置和发音准确性", "10 分钟", android.R.drawable.ic_btn_speak_now);
            int progressStatus = getModuleProgressStatus("A", ImageUrls.A_imageUrls.length);
            m.setProgressStatus(progressStatus);
            m.setCompleted(progressStatus == AssessmentModule.STATUS_COMPLETED);
            moduleList.add(m);
        }

        // 2. 前语言 (PL)
        if ("1".equals(PL)) {
            AssessmentModule m = new AssessmentModule("PL", "前语言能力", "评估前语言沟通技能", "15 分钟", android.R.drawable.ic_menu_agenda);
            int progressStatus = getModuleProgressStatus("PL", ImageUrls.PL_SKILLS.length);
            m.setProgressStatus(progressStatus);
            m.setCompleted(progressStatus == AssessmentModule.STATUS_COMPLETED);
            moduleList.add(m);
        }

        // 3. 词汇-表达 (E)
        if ("1".equals(E)) {
            AssessmentModule m = new AssessmentModule("E", "词汇能力-表达", "评估词汇表达能力", "15 分钟", android.R.drawable.ic_menu_sort_by_size);
            m.setCompleted(checkCompletion("E", 7)); 
            moduleList.add(m);
        }

        // 4. 词汇-理解 (EV)
        if ("1".equals(E)) { 
            AssessmentModule m = new AssessmentModule("EV", "词汇能力-理解", "评估词汇理解能力", "15 分钟", android.R.drawable.ic_menu_search);
            m.setCompleted(checkCompletion("EV", 7)); 
            moduleList.add(m);
        }

        // 5. 句法 (RG)
        if ("1".equals(RG)) {
            AssessmentModule m = new AssessmentModule("RG", "句法能力", "评估句法理解与表达", "20 分钟", android.R.drawable.ic_menu_edit);
            // 检查句法理解和句法表达是否都至少完成了一组
            boolean syntaxCompleted = checkSyntaxCompletion();
            m.setCompleted(syntaxCompleted);
            moduleList.add(m);
        }

        // 6. 社交 (Social) - Always added
        AssessmentModule m = new AssessmentModule("SOCIAL", "社交能力", "评估社交互动技能", "15 分钟", android.R.drawable.ic_menu_myplaces);
        m.setCompleted(checkCompletion("SOCIAL", 7)); // Assuming 7 based on earlier snippet or default
        moduleList.add(m);

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private int getModuleProgressStatus(String key, int targetLength) {
        if (data == null) return AssessmentModule.STATUS_NOT_STARTED;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return AssessmentModule.STATUS_NOT_STARTED;
        if ("PL".equals(key)) {
            int required = targetLength > 0 ? targetLength : ImageUrls.PL_SKILLS.length;
            int bestAnswered = 0;
            JSONArray legacy = evaluations.optJSONArray("PL");
            bestAnswered = Math.max(bestAnswered, countAnsweredUniqueByNum(legacy, required));
            JSONArray sceneA = evaluations.optJSONArray("PL_A");
            bestAnswered = Math.max(bestAnswered, countAnsweredUniqueByNum(sceneA, required));
            JSONArray sceneB = evaluations.optJSONArray("PL_B");
            bestAnswered = Math.max(bestAnswered, countAnsweredUniqueByNum(sceneB, required));
            if (bestAnswered >= required) return AssessmentModule.STATUS_COMPLETED;
            return bestAnswered > 0 ? AssessmentModule.STATUS_IN_PROGRESS : AssessmentModule.STATUS_NOT_STARTED;
        }
        if ("SOCIAL".equals(key)) {
            int totalAnswered = 0;
            // 检查主 SOCIAL 数组
            JSONArray mainArray = evaluations.optJSONArray(key);
            if (mainArray != null) {
                for (int i = 0; i < mainArray.length(); i++) {
                    JSONObject obj = mainArray.optJSONObject(i);
                    if (obj != null && obj.has("time") && !obj.isNull("time")) {
                        totalAnswered++;
                    }
                }
            }
            // 检查每个组的数组
            for (int i = 1; i <= 6; i++) {
                JSONArray groupArray = evaluations.optJSONArray(key + i);
                if (groupArray != null) {
                    for (int j = 0; j < groupArray.length(); j++) {
                        JSONObject obj = groupArray.optJSONObject(j);
                        if (obj != null && obj.has("time") && !obj.isNull("time")) {
                            totalAnswered++;
                        }
                    }
                }
            }
            if (targetLength > 0 && totalAnswered >= targetLength) {
                return AssessmentModule.STATUS_COMPLETED;
            }
            return totalAnswered > 0 ? AssessmentModule.STATUS_IN_PROGRESS : AssessmentModule.STATUS_NOT_STARTED;
        }
        JSONArray array = evaluations.optJSONArray(key);
        if (array == null || array.length() == 0) return AssessmentModule.STATUS_NOT_STARTED;

        int answered = 0;
        if ("A".equals(key) && targetLength > 0) {
            java.util.HashSet<Integer> answeredNums = new java.util.HashSet<>();
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj == null || !obj.has("time") || obj.isNull("time")) {
                        continue;
                    }
                    int num = obj.optInt("num", -1);
                    if (num >= 1 && num <= targetLength) {
                        answeredNums.add(num);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            answered = answeredNums.size();
        } else {
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj != null && obj.has("time") && !obj.isNull("time")) {
                        answered++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (targetLength > 0 && answered >= targetLength) {
            return AssessmentModule.STATUS_COMPLETED;
        }
        return answered > 0 ? AssessmentModule.STATUS_IN_PROGRESS : AssessmentModule.STATUS_NOT_STARTED;
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

    private boolean checkCompletion(String key, int targetLength) {
        return getModuleProgressStatus(key, targetLength) == AssessmentModule.STATUS_COMPLETED;
    }
    
    // Overload for array check (A, RG)
    private boolean checkCompletion(String key, String[] targetArray) {
        return checkCompletion(key, targetArray != null ? targetArray.length : 0);
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

    private void onModuleClick(AssessmentModule module) {
        Intent intent = null;
        switch (module.getId()) {
            case "A": // 构音
                intent = new Intent(this, testactivity.class);
                intent.putExtra("format", "A");
                break;
            case "PL": // 前语言
                intent = new Intent(this, PrelinguisticSceneSelectActivity.class);
                break;
            case "E": // 词汇表达
                intent = new Intent(this, testactivity.class);
                intent.putExtra("format", "E");
                break;
            case "EV": // 词汇理解
                intent = new Intent(this, testactivity.class);
                intent.putExtra("format", "EV");
                break;
            case "RG": // 句法
                intent = new Intent(this, SyntaxAbilityEvaluationActivity.class);
                break;
            case "SOCIAL": // 社交
                intent = new Intent(this, SocialGroupSelectActivity.class);
                intent.putExtra("Uid", uid);
                intent.putExtra("childID", childUser);
                break;
        }

        if (intent != null) {
            intent.putExtra("fName", fName);
            startActivity(intent);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload data to check completion status updates
        try {
            if (fName != null) {
                data = dataManager.getInstance().loadData(fName);
                updateModulesList(lastModuleJson);
                if (!isPrivateMode && uid != null && !uid.isEmpty()) {
                    NetInteractUtils.getInstance(this).setModuleCallback(new NetInteractUtils.ModuleCallback() {
                        @Override
                        public void onModuleResult(String module) throws JSONException {
                            lastModuleJson = module;
                            updateModulesList(module);
                        }
                    });
                    NetInteractUtils.getInstance(this).getModule(uid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
