package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.ModuleInterventionGuideSchema;
import utils.ModuleReportHelper;
import utils.ReportDisplayFallbackHelper;
import utils.dataManager;

public class InterventionPlanActivity extends AppCompatActivity {
    private String fName;
    private String moduleType;

    private TextView tvHeaderTitle;
    private TextView tvModuleTitle;

    private TextView tvOverallSummary;
    private TextView tvMastered;
    private TextView tvNotMasteredOverview;
    private TextView tvFocus;
    private TextView tvUnstable;
    private TextView tvSmartGoalText;
    private TextView tvSmartMeta;
    private TextView tvSmartGoalSupport;
    private TextView tvHomeGuidance;
    private TextView tvNotesForTherapist;

    private View layoutSmartMeta;

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
                btnShare.setOnClickListener(v ->
                        Toast.makeText(this, "分享功能待接入", Toast.LENGTH_SHORT).show());
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

        tvOverallSummary = findViewById(R.id.tv_overall_summary);
        tvMastered = findViewById(R.id.tv_mastered);
        tvNotMasteredOverview = findViewById(R.id.tv_not_mastered_overview);
        tvFocus = findViewById(R.id.tv_focus);
        tvUnstable = findViewById(R.id.tv_unstable);
        tvSmartGoalText = findViewById(R.id.tv_smart_goal_text);
        tvSmartMeta = findViewById(R.id.tv_smart_meta);
        tvSmartGoalSupport = findViewById(R.id.tv_smart_goal_support);
        tvHomeGuidance = findViewById(R.id.tv_home_guidance);
        tvNotesForTherapist = findViewById(R.id.tv_notes_for_therapist);

        layoutSmartMeta = findViewById(R.id.layout_smart_meta);

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
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("干预报告");
        }
        if (tvModuleTitle != null) {
            tvModuleTitle.setText(title);
        }

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
        String overallSummary = guide.optString("overallSummary", "");
        String notMasteredOverview = guide.optString("notMasteredOverview", "");

        if (etOverallSummary != null) {
            etOverallSummary.setText(overallSummary);
        }
        if (tvOverallSummary != null) {
            tvOverallSummary.setText(ReportDisplayFallbackHelper.getDisplayTextOrFallback(
                    overallSummary, ReportDisplayFallbackHelper.FIELD_OVERALL_SUMMARY));
        }

        JSONArray mastered = guide.optJSONArray("mastered");
        if (etMastered != null) {
            etMastered.setText(joinLines(mastered));
        }
        if (tvMastered != null) {
            tvMastered.setText(formatBulletList(mastered, ReportDisplayFallbackHelper.FIELD_MASTERED));
        }

        if (etNotMasteredOverview != null) {
            etNotMasteredOverview.setText(notMasteredOverview);
        }
        if (tvNotMasteredOverview != null) {
            tvNotMasteredOverview.setText(ReportDisplayFallbackHelper.getDisplayTextOrFallback(
                    notMasteredOverview, ReportDisplayFallbackHelper.FIELD_NOT_MASTERED_OVERVIEW));
        }

        JSONArray focus = guide.optJSONArray("focus");
        if (etFocus != null) {
            etFocus.setText(joinLines(focus));
        }
        if (tvFocus != null) {
            tvFocus.setText(formatBulletList(focus, ReportDisplayFallbackHelper.FIELD_FOCUS));
        }

        JSONArray unstable = guide.optJSONArray("unstable");
        if (etUnstable != null) {
            etUnstable.setText(joinLines(unstable));
        }
        if (tvUnstable != null) {
            tvUnstable.setText(formatBulletList(unstable, ReportDisplayFallbackHelper.FIELD_UNSTABLE));
        }

        JSONObject smartGoal = guide.optJSONObject("smartGoal");
        if (smartGoal == null) {
            smartGoal = new JSONObject();
        }
        String smartText = smartGoal.optString("text", "");
        int cycleWeeks = smartGoal.optInt("cycleWeeks", 8);
        double accuracyThreshold = smartGoal.optDouble("accuracyThreshold", 0.8d);
        JSONArray support = smartGoal.optJSONArray("support");

        if (etSmartGoalText != null) {
            etSmartGoalText.setText(smartText);
        }
        if (tvSmartGoalText != null) {
            tvSmartGoalText.setText(ReportDisplayFallbackHelper.getDisplayTextOrFallback(
                    smartText, ReportDisplayFallbackHelper.FIELD_SMART_GOAL_TEXT));
        }

        if (etSmartGoalCycleWeeks != null) {
            etSmartGoalCycleWeeks.setText(String.valueOf(cycleWeeks));
        }
        if (etSmartGoalAccuracy != null) {
            etSmartGoalAccuracy.setText(String.valueOf(accuracyThreshold));
        }
        if (etSmartGoalSupport != null) {
            etSmartGoalSupport.setText(joinLines(support));
        }

        if (tvSmartMeta != null) {
            tvSmartMeta.setText("周期：" + cycleWeeks + " 周  ·  达标阈值：" + accuracyThreshold);
        }
        if (tvSmartGoalSupport != null) {
            tvSmartGoalSupport.setText(ReportDisplayFallbackHelper.hasDisplayItems(support)
                    ? formatBulletList(support, null)
                    : "");
        }

        JSONArray home = guide.optJSONArray("homeGuidance");
        if (etHomeGuidance != null) {
            etHomeGuidance.setText(joinLines(home));
        }
        if (tvHomeGuidance != null) {
            tvHomeGuidance.setText(formatBulletList(home, ReportDisplayFallbackHelper.FIELD_HOME_GUIDANCE));
        }

        JSONArray notes = guide.optJSONArray("notesForTherapist");
        if (etNotesForTherapist != null) {
            etNotesForTherapist.setText(joinLines(notes));
        }
        if (tvNotesForTherapist != null) {
            tvNotesForTherapist.setText(ReportDisplayFallbackHelper.hasDisplayItems(notes)
                    ? formatBulletList(notes, null)
                    : "");
        }
    }

    private void switchEditMode(boolean enable) {
        editMode = enable;

        if (!enable) {
            bindGuide(cachedGuide);
        }

        setVisibility(etOverallSummary, enable);
        setVisibility(etMastered, enable);
        setVisibility(etNotMasteredOverview, enable);
        setVisibility(etFocus, enable);
        setVisibility(etUnstable, enable);
        setVisibility(etSmartGoalText, enable);
        setVisibility(etSmartGoalCycleWeeks, enable);
        setVisibility(etSmartGoalAccuracy, enable);
        setVisibility(etSmartGoalSupport, enable);
        setVisibility(etHomeGuidance, enable);
        setVisibility(etNotesForTherapist, enable);

        setVisibility(tvOverallSummary, !enable);
        setVisibility(tvMastered, !enable);
        setVisibility(tvNotMasteredOverview, !enable);
        setVisibility(tvFocus, !enable);
        setVisibility(tvUnstable, !enable);
        setVisibility(tvSmartGoalText, !enable);
        setVisibility(tvSmartMeta, !enable);
        setVisibility(tvSmartGoalSupport, !enable);
        setVisibility(tvHomeGuidance, !enable);
        setVisibility(tvNotesForTherapist, !enable);

        setVisibility(layoutSmartMeta, enable);

        if (btnEdit != null) {
            btnEdit.setText(enable ? "取消" : "编辑");
        }
    }

    private void setVisibility(View view, boolean visible) {
        if (view == null) {
            return;
        }
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
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

    private String textOf(EditText et) {
        return et == null ? "" : et.getText().toString();
    }

    private int parseInt(String val, int def) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private double parseDouble(String val, double def) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private CharSequence formatBulletList(JSONArray array, String fieldKey) {
        if (!ReportDisplayFallbackHelper.hasDisplayItems(array)) {
            return fieldKey == null ? "" : ReportDisplayFallbackHelper.getFallback(fieldKey);
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int i = 0; i < array.length(); i++) {
            String text = array.optString(i, "");
            if (text == null) {
                continue;
            }
            text = text.trim();
            if (text.isEmpty()) {
                continue;
            }
            int start = sb.length();
            sb.append(text);
            int end = sb.length();
            sb.setSpan(new BulletSpan(24), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append('\n');
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb;
    }
}
