package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import utils.ModuleInterventionGuideSchema;
import utils.ModuleReportHelper;
import utils.dataManager;

public class InterventionPlanActivity extends AppCompatActivity {
    private String fName;
    private String moduleType;

    private TextView tvHeaderTitle;
    private TextView tvModuleTitle;
    private EditText etOverallSummary;
    private EditText etMastered;
    private EditText etNotMasteredOverview;
    private EditText etFocus;
    private EditText etUnstable;
    private EditText etSmartGoalText;
    private EditText etSmartGoalCycleWeeks;
    private EditText etSmartGoalAccuracy;
    private EditText etSmartGoalSupport;
    private EditText etHomeGuidance;
    private EditText etNotesForTherapist;
    private MaterialButton btnEdit;
    private MaterialButton btnSave;
    private MaterialButton btnConfirm;
    private MaterialButton btnShare;

    private JSONObject cachedGuide;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_intervention_plan);

            fName = getIntent().getStringExtra("fName");
            moduleType = ModuleReportHelper.normalizeModuleType(getIntent().getStringExtra("moduleType"));

            bindViews();
            View btnBack = findViewById(R.id.btn_back);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> switchEditMode(!editMode));
            }
            if (btnSave != null) {
                btnSave.setOnClickListener(v -> saveGuide(false));
            }
            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> saveGuide(true));
            }
            if (btnShare != null) {
                btnShare.setOnClickListener(v -> Toast.makeText(this, "分享功能待接入", Toast.LENGTH_SHORT).show());
            }

            loadGuide();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "页面初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        tvModuleTitle = findViewById(R.id.tv_module_title);
        etOverallSummary = findViewById(R.id.et_overall_summary);
        etMastered = findViewById(R.id.et_mastered);
        etNotMasteredOverview = findViewById(R.id.et_not_mastered_overview);
        etFocus = findViewById(R.id.et_focus);
        etUnstable = findViewById(R.id.et_unstable);
        etSmartGoalText = findViewById(R.id.et_smart_goal_text);
        etSmartGoalCycleWeeks = findViewById(R.id.et_smart_goal_cycle_weeks);
        etSmartGoalAccuracy = findViewById(R.id.et_smart_goal_accuracy);
        etSmartGoalSupport = findViewById(R.id.et_smart_goal_support);
        etHomeGuidance = findViewById(R.id.et_home_guidance);
        etNotesForTherapist = findViewById(R.id.et_notes_for_therapist);
        btnEdit = findViewById(R.id.btn_edit);
        btnSave = findViewById(R.id.btn_save);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnShare = findViewById(R.id.btn_share);
    }

    private void loadGuide() {
        String title = ModuleReportHelper.moduleTitle(moduleType);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("干预报告");
        if (tvModuleTitle != null) tvModuleTitle.setText(title);

        JSONObject childData = loadChildData();
        JSONObject guide = ModuleReportHelper.loadModuleInterventionGuide(childData, moduleType);
        try {
            cachedGuide = ModuleInterventionGuideSchema.normalize(
                    guide, moduleType, title, ModuleReportHelper.defaultSubtypes(moduleType));
        } catch (Exception e) {
            cachedGuide = new JSONObject();
        }
        bindGuide(cachedGuide);
        switchEditMode(false);
    }

    private JSONObject loadChildData() {
        if (fName == null || fName.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            return dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private void bindGuide(JSONObject guide) {
        if (guide == null) {
            return;
        }
        if (etOverallSummary != null) etOverallSummary.setText(guide.optString("overallSummary", ""));
        if (etMastered != null) etMastered.setText(joinLines(guide.optJSONArray("mastered")));
        if (etNotMasteredOverview != null) etNotMasteredOverview.setText(guide.optString("notMasteredOverview", ""));
        if (etFocus != null) etFocus.setText(joinLines(guide.optJSONArray("focus")));
        if (etUnstable != null) etUnstable.setText(joinLines(guide.optJSONArray("unstable")));

        JSONObject smartGoal = guide.optJSONObject("smartGoal");
        if (smartGoal == null) {
            smartGoal = new JSONObject();
        }
        if (etSmartGoalText != null) etSmartGoalText.setText(smartGoal.optString("text", ""));
        if (etSmartGoalCycleWeeks != null) etSmartGoalCycleWeeks.setText(String.valueOf(smartGoal.optInt("cycleWeeks", 8)));
        if (etSmartGoalAccuracy != null) etSmartGoalAccuracy.setText(String.valueOf(smartGoal.optDouble("accuracyThreshold", 0.8d)));
        if (etSmartGoalSupport != null) etSmartGoalSupport.setText(joinLines(smartGoal.optJSONArray("support")));

        if (etHomeGuidance != null) etHomeGuidance.setText(joinLines(guide.optJSONArray("homeGuidance")));
        if (etNotesForTherapist != null) etNotesForTherapist.setText(joinLines(guide.optJSONArray("notesForTherapist")));
    }

    private void switchEditMode(boolean enable) {
        editMode = enable;
        setEditable(etOverallSummary, enable);
        setEditable(etMastered, enable);
        setEditable(etNotMasteredOverview, enable);
        setEditable(etFocus, enable);
        setEditable(etUnstable, enable);
        setEditable(etSmartGoalText, enable);
        setEditable(etSmartGoalCycleWeeks, enable);
        setEditable(etSmartGoalAccuracy, enable);
        setEditable(etSmartGoalSupport, enable);
        setEditable(etHomeGuidance, enable);
        setEditable(etNotesForTherapist, enable);
        btnEdit.setText(enable ? "退出编辑" : "编辑");
    }

    private void setEditable(EditText editText, boolean editable) {
        if (editText == null) {
            return;
        }
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        editText.setClickable(editable);
    }

    private void saveGuide(boolean reviewed) {
        JSONObject childData = loadChildData();
        try {
            JSONObject guide = buildGuideFromUi();
            JSONObject meta = guide.optJSONObject("meta");
            if (meta == null) {
                meta = new JSONObject();
                guide.put("meta", meta);
            }
            if (reviewed) {
                meta.put("reviewStatus", "reviewed");
                meta.put("reviewedByTherapist", true);
            } else if (!meta.has("reviewStatus")) {
                meta.put("reviewStatus", "draft");
                meta.put("reviewedByTherapist", false);
            }
            ModuleReportHelper.saveModuleInterventionGuide(childData, moduleType, guide);
            dataManager.getInstance().saveChildJson(fName, childData);
            cachedGuide = guide;
            switchEditMode(false);
            Toast.makeText(this, reviewed ? "已确认并保存" : "已保存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject buildGuideFromUi() throws JSONException {
        JSONObject source = cachedGuide == null ? new JSONObject() : new JSONObject(cachedGuide.toString());
        source.put("moduleType", moduleType);
        source.put("moduleTitle", ModuleReportHelper.moduleTitle(moduleType));
        source.put("subtypes", ModuleReportHelper.defaultSubtypes(moduleType));
        source.put("overallSummary", textOf(etOverallSummary));
        source.put("mastered", arrayFromText(textOf(etMastered)));
        source.put("notMasteredOverview", textOf(etNotMasteredOverview));
        source.put("focus", arrayFromText(textOf(etFocus)));
        source.put("unstable", arrayFromText(textOf(etUnstable)));
        source.put("homeGuidance", arrayFromText(textOf(etHomeGuidance)));
        source.put("notesForTherapist", arrayFromText(textOf(etNotesForTherapist)));

        JSONObject smartGoal = source.optJSONObject("smartGoal");
        if (smartGoal == null) {
            smartGoal = new JSONObject();
        }
        smartGoal.put("text", textOf(etSmartGoalText));
        smartGoal.put("cycleWeeks", parseInt(textOf(etSmartGoalCycleWeeks), 8));
        smartGoal.put("accuracyThreshold", parseDouble(textOf(etSmartGoalAccuracy), 0.8d));
        smartGoal.put("support", arrayFromText(textOf(etSmartGoalSupport)));
        source.put("smartGoal", smartGoal);
        
        // 确保 custom 字段被保留
        if (cachedGuide != null && cachedGuide.has("custom")) {
            source.put("custom", cachedGuide.getJSONObject("custom"));
        }

        return ModuleInterventionGuideSchema.normalize(
                source,
                moduleType,
                ModuleReportHelper.moduleTitle(moduleType),
                ModuleReportHelper.defaultSubtypes(moduleType));
    }

    private JSONArray arrayFromText(String text) {
        JSONArray array = new JSONArray();
        if (text == null || text.trim().isEmpty()) {
            return array;
        }
        String[] lines = text.split("\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                array.put(line.trim());
            }
        }
        return array;
    }

    private String joinLines(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            String line = array.optString(i, "").trim();
            if (line.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(line);
        }
        return sb.toString();
    }
    
    // Helper methods for parsing
    private String textOf(EditText et) {
        return et == null ? "" : et.getText().toString();
    }
    
    private int parseInt(String val, int def) {
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }
    
    private double parseDouble(String val, double def) {
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }
}
