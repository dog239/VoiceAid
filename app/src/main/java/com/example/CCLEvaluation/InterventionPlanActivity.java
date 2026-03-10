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
        setContentView(R.layout.activity_intervention_plan);

        fName = getIntent().getStringExtra("fName");
        moduleType = ModuleReportHelper.normalizeModuleType(getIntent().getStringExtra("moduleType"));

        bindViews();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> switchEditMode(!editMode));
        btnSave.setOnClickListener(v -> saveGuide(false));
        btnConfirm.setOnClickListener(v -> saveGuide(true));
        btnShare.setOnClickListener(v -> Toast.makeText(this, "分享功能待接入", Toast.LENGTH_SHORT).show());

        loadGuide();
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
        tvHeaderTitle.setText("干预报告");
        tvModuleTitle.setText(title);
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
        etOverallSummary.setText(guide.optString("overallSummary", ""));
        etMastered.setText(joinLines(guide.optJSONArray("mastered")));
        etNotMasteredOverview.setText(guide.optString("notMasteredOverview", ""));
        etFocus.setText(joinLines(guide.optJSONArray("focus")));
        etUnstable.setText(joinLines(guide.optJSONArray("unstable")));

        JSONObject smartGoal = guide.optJSONObject("smartGoal");
        if (smartGoal == null) {
            smartGoal = new JSONObject();
        }
        etSmartGoalText.setText(smartGoal.optString("text", ""));
        etSmartGoalCycleWeeks.setText(String.valueOf(smartGoal.optInt("cycleWeeks", 8)));
        etSmartGoalAccuracy.setText(String.valueOf(smartGoal.optDouble("accuracyThreshold", 0.8d)));
        etSmartGoalSupport.setText(joinLines(smartGoal.optJSONArray("support")));

        etHomeGuidance.setText(joinLines(guide.optJSONArray("homeGuidance")));
        etNotesForTherapist.setText(joinLines(guide.optJSONArray("notesForTherapist")));
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
        source.put("mastered", toArray(etMastered));
        source.put("notMasteredOverview", textOf(etNotMasteredOverview));
        source.put("focus", toArray(etFocus));
        source.put("unstable", toArray(etUnstable));
        source.put("homeGuidance", toArray(etHomeGuidance));
        source.put("notesForTherapist", toArray(etNotesForTherapist));

        JSONObject smartGoal = source.optJSONObject("smartGoal");
        if (smartGoal == null) {
            smartGoal = new JSONObject();
        }
        smartGoal.put("text", textOf(etSmartGoalText));
        smartGoal.put("cycleWeeks", parseInt(etSmartGoalCycleWeeks, 8));
        smartGoal.put("accuracyThreshold", parseDouble(etSmartGoalAccuracy, 0.8d));
        smartGoal.put("support", toArray(etSmartGoalSupport));
        source.put("smartGoal", smartGoal);
        return ModuleInterventionGuideSchema.normalize(
                source,
                moduleType,
                ModuleReportHelper.moduleTitle(moduleType),
                ModuleReportHelper.defaultSubtypes(moduleType));
    }

    private JSONArray toArray(EditText editText) {
        JSONArray array = new JSONArray();
        List<String> lines = splitLines(textOf(editText));
        for (String line : lines) {
            array.put(line);
        }
        return array;
    }

    private List<String> splitLines(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        String[] parts = text.split("\\r?\\n");
        for (String part : parts) {
            String line = part == null ? "" : part.trim();
            if (!line.isEmpty()) {
                out.add(line);
            }
        }
        return out;
    }

    private String joinLines(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            String item = array.optString(i, "").trim();
            if (item.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(item);
        }
        return builder.toString();
    }

    private String textOf(EditText editText) {
        return editText == null || editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private int parseInt(EditText editText, int fallback) {
        try {
            return Integer.parseInt(textOf(editText));
        } catch (Exception e) {
            return fallback;
        }
    }

    private double parseDouble(EditText editText, double fallback) {
        try {
            double value = Double.parseDouble(textOf(editText));
            if (value <= 0 || value > 1.0d) {
                return fallback;
            }
            return value;
        } catch (Exception e) {
            return fallback;
        }
    }
}

