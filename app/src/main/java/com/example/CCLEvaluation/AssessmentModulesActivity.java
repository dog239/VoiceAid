package com.example.CCLEvaluation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import utils.dataManager;
import utils.net.NetService;
import utils.net.NetServiceProvider;

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
    private NetService netService;

    private String getCachedModuleJson() {
        try {
            SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            String cached = preferences.getString("module_json", null);
            if (cached == null) return null;
            cached = cached.trim();
            return cached.isEmpty() ? null : cached;
        } catch (Exception ignored) {
            return null;
        }
    }

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
            // 模块页属于流程页，返回应遵循正常返回栈
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0); // Seamless transition
            return true;
        } else if (item.getItemId() == R.id.nav_profile) {
            // Jump to User Profile
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void initData() {
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
        isPrivateMode = getIntent().getBooleanExtra("private", false);
        netService = NetServiceProvider.get(this);

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
            // Render cached module selection immediately (prevents first-enter showing "all modules")
            String cached = getCachedModuleJson();
            if (cached != null) {
                lastModuleJson = cached;
                updateModulesList(cached);
            } else {
                updateModulesList(null);
            }

            netService.setModuleCallback(module -> {
                lastModuleJson = module;
                updateModulesList(module);
            });
            netService.getModule(uid);
        }
    }

    private void updateModulesList(String moduleJson) {
        // In non-private mode, if backend selection hasn't been loaded yet (moduleJson is null/empty),
        // do not fall back to enabling all modules. Otherwise the UI will briefly show everything
        // until the next refresh/callback.
        if ((moduleJson == null || moduleJson.isEmpty())
                && !(isPrivateMode || uid == null || uid.isEmpty())) {
            moduleList.clear();
            runOnUiThread(() -> adapter.notifyDataSetChanged());
            return;
        }

        ModuleFlags flags = resolveModuleFlags(moduleJson);

        moduleList.clear();

        // 1. 构音 (A)
        if (flags.enablePronunciation) {
            AssessmentModule m1 = new AssessmentModule("A", "构音评估", "评估语音放置和发音准确性", "10 分钟", android.R.drawable.ic_btn_speak_now);
            int progressStatusA = getModuleProgressStatus("A", ImageUrls.A_imageUrls.length);
            m1.setProgressStatus(progressStatusA);
            m1.setCompleted(progressStatusA == AssessmentModule.STATUS_COMPLETED);
            moduleList.add(m1);
        }

        // 2. 前语言 (PL)
        if (flags.enablePrelinguistic) {
            AssessmentModule m2 = new AssessmentModule("PL", "前语言能力", "评估前语言沟通技能", "15 分钟", android.R.drawable.ic_menu_agenda);
            int progressStatusPL = getModuleProgressStatus("PL", ImageUrls.PL_SKILLS.length);
            m2.setProgressStatus(progressStatusPL);
            m2.setCompleted(progressStatusPL == AssessmentModule.STATUS_COMPLETED);
            moduleList.add(m2);
        }

        // 3. 词汇-表达 (E)
        if (flags.enableWord) {
            AssessmentModule m3 = new AssessmentModule("E", "词汇能力-表达", "评估词汇表达能力", "15 分钟", android.R.drawable.ic_menu_sort_by_size);
            m3.setCompleted(checkCompletion("E", 7));
            moduleList.add(m3);

            // 4. 词汇-理解 (EV)
            AssessmentModule m4 = new AssessmentModule("EV", "词汇能力-理解", "评估词汇理解能力", "15 分钟", android.R.drawable.ic_menu_search);
            m4.setCompleted(checkCompletion("EV", 7));
            moduleList.add(m4);
        }

        // 5. 句法 (RG)
        if (flags.enableGrammar) {
            AssessmentModule m5 = new AssessmentModule("RG", "句法能力", "评估句法理解与表达", "20 分钟", android.R.drawable.ic_menu_edit);
            boolean syntaxCompleted = checkSyntaxCompletion();
            m5.setCompleted(syntaxCompleted);
            moduleList.add(m5);
        }

        // 6. 社交 (Social)
        if (flags.enableSocial) {
            AssessmentModule m6 = new AssessmentModule("SOCIAL", "社交能力", "评估社交互动技能", "15 分钟", android.R.drawable.ic_menu_myplaces);
            m6.setCompleted(checkCompletion("SOCIAL", 7));
            moduleList.add(m6);
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private static class ModuleFlags {
        boolean enableWord = true;
        boolean enablePronunciation = true;
        boolean enableGrammar = true;
        boolean enablePrelinguistic = true;
        boolean enableSocial = true;
    }

    private ModuleFlags resolveModuleFlags(String moduleJson) {
        ModuleFlags flags = new ModuleFlags();
        if (moduleJson == null || moduleJson.isEmpty()) {
            return flags;
        }
        try {
            JSONObject jsonObject = new JSONObject(moduleJson);
            flags.enableWord = "1".equals(jsonObject.optString("E", "1"));
            flags.enablePronunciation = "1".equals(jsonObject.optString("A", "1"));
            String rg = jsonObject.optString("RG", "1");
            String se = jsonObject.optString("SE", "1");
            flags.enableGrammar = "1".equals(rg) || "1".equals(se);
            flags.enablePrelinguistic = "1".equals(jsonObject.optString("PL", "1"));
            flags.enableSocial = "1".equals(jsonObject.optString("SOCIAL", "1"));
        } catch (JSONException e) {
            return flags;
        }
        return flags;
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
            JSONArray mainArray = evaluations.optJSONArray(key);
            if (mainArray != null) {
                for (int i = 0; i < mainArray.length(); i++) {
                    JSONObject obj = mainArray.optJSONObject(i);
                    if (obj != null && obj.has("time") && !obj.isNull("time")) {
                        totalAnswered++;
                    }
                }
            }
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
                JSONObject obj = array.optJSONObject(i);
                if (obj == null || !obj.has("time") || obj.isNull("time")) {
                    continue;
                }
                int num = obj.optInt("num", -1);
                if (num >= 1 && num <= targetLength) {
                    answeredNums.add(num);
                }
            }
            answered = answeredNums.size();
        } else {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null && obj.has("time") && !obj.isNull("time")) {
                    answered++;
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

    private boolean checkSyntaxCompletion() {
        if (data == null) return false;
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) return false;

        boolean hasCompletedRG = false;
        boolean hasCompletedSE = false;
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
                    hasCompletedRG = true;
                    break;
                }
            }
        }
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
                    hasCompletedSE = true;
                    break;
                }
            }
        }
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
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
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
                if (!isPrivateMode && uid != null && !uid.isEmpty()) {
                    // Ensure we at least apply cached selection on returning to this page.
                    String cached = getCachedModuleJson();
                    if (cached != null) {
                        lastModuleJson = cached;
                    }
                }
                updateModulesList(lastModuleJson);
                if (!isPrivateMode && uid != null && !uid.isEmpty()) {
                    netService.setModuleCallback(module -> {
                        lastModuleJson = module;
                        updateModulesList(module);
                    });
                    netService.getModule(uid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
