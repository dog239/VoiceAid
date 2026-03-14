package com.example.CCLEvaluation;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import utils.ArticulationPlanHelper;
import utils.ModuleInterventionGuideSchema;
import utils.ModuleReportHelper;
import utils.dataManager;

public class TreatmentPlanActivity extends AppCompatActivity {
    private static final int REQUEST_EXPORT_PDF = 3001;
    private static final String[] DEFAULT_SPEECH_STAGE_NAMES = {
            "\u9636\u6bb51\uff1a\u542c\u8fa8\u4e0e\u6ce8\u610f",
            "\u9636\u6bb52\uff1a\u6a21\u4eff\u4e0e\u63a7\u5236",
            "\u9636\u6bb53\uff1a\u8bcd/\u77ed\u8bed\u5c42\u7ea7",
            "\u9636\u6bb54\uff1a\u65e5\u5e38\u6cdb\u5316"
    };
    private static final String CARD_BASE_INFO = "\u57fa\u7840\u4fe1\u606f";
    private static final String CARD_SPEECH_SOUND = "\u8bed\u97f3/\u6784\u97f3";
    private static final String CARD_PRELINGUISTIC = "\u524d\u8bed\u8a00";
    private static final String CARD_VOCABULARY = "\u8bcd\u6c47";
    private static final String CARD_SYNTAX = "\u53e5\u6cd5";
    private static final String CARD_SOCIAL = "\u793e\u4f1a\u4ea4\u5f80";
    private static final String CARD_SCHEDULE = "\u9891\u6b21\u5efa\u8bae";
    private static final String CARD_NOTES_THERAPIST = "\u6cbb\u7597\u5e08\u5907\u6ce8";
    private static final String CARD_NOTES_PARENTS = "\u5bb6\u957f\u5efa\u8bae";
    private static final String SECTION_FINDINGS = "\u8bca\u65ad\u53d1\u73b0";
    private static final String SECTION_TEST_RESULTS = "\u6d4b\u8bd5\u7ed3\u679c";
    private static final String SECTION_PLAN = "\u5e72\u9884\u8ba1\u5212";
    private static final String SECTION_STAGES = "\u9636\u6bb5\u8bad\u7ec3";
    private static final String SECTION_ARTICULATION_OVERALL = "\u8bc4\u4f30\u7ed3\u679c\uff08\u6574\u4f53\uff09";
    private static final String SECTION_ARTICULATION_MASTERED = "\u672c\u6b21\u8bc4\u4f30\u4e2d\u5df2\u638c\u63e1\u7684\u80fd\u529b";
    private static final String SECTION_ARTICULATION_NOT_MASTERED = "\u672a\u638c\u63e1\u80fd\u529b\u7684\u6574\u4f53\u8bf4\u660e";
    private static final String SECTION_ARTICULATION_FOCUS = "\u9700\u8981\u91cd\u70b9\u5173\u6ce8\u7684\u80fd\u529b";
    private static final String SECTION_ARTICULATION_UNSTABLE = "\u4e0d\u7a33\u5b9a\u7684\u80fd\u529b";
    private static final String SECTION_ARTICULATION_SMART = "\u5e72\u9884\u76ee\u6807\uff08SMART\uff09";
    private static final String SECTION_ARTICULATION_HOME = "\u5bb6\u5ead\u5e72\u9884\u6307\u5bfc\u5efa\u8bae";
    private static final String LABEL_OVERALL_SUMMARY = "\u6574\u4f53\u7ed3\u8bba";
    private static final String LABEL_MASTERED_INTRO = "\u8bf4\u660e";
    private static final String LABEL_NOT_MASTERED = "\u6574\u4f53\u8bf4\u660e";
    private static final String LABEL_FOCUS_NOTE = "\u8865\u5145\u8bf4\u660e";
    private static final String LABEL_SMART_CYCLE = "\u5468\u671f(\u5468)";
    private static final String LABEL_SMART_LEVEL = "\u8bad\u7ec3\u5c42\u7ea7";
    private static final String LABEL_SMART_ACCURACY = "\u8fbe\u6807\u6807\u51c6(0-1)";
    private static final String LABEL_SMART_TARGET_SOUNDS = "\u76ee\u6807\u97f3";
    private static final String LABEL_SMART_SUPPORT = "\u63d0\u793a\u652f\u6301";
    private static final String LABEL_SMART_TEXT = "SMART\u76ee\u6807\u63cf\u8ff0";
    private static final String PLACEHOLDER_TEXT = "\uff08\u6839\u636e\u8bc4\u4f30\u60c5\u51b5\u5f85\u5b9a\uff09";
    private String fName;
    private JSONObject currentPlan;
    private final List<PlanUiItem> items = new ArrayList<>();
    private final List<String> speechStageNames = new ArrayList<>();
    private TreatmentPlanAdapter adapter;

    private JSONObject mChildData;

    private View overallContainer;
    private View planActions;
    private View rvPlan;
    private Button btnLeft;
    private Button btnRight;
    private View btnOverallEdit;
    private TextView tvTitle;
    private TextView tvDate;
    private TextView tvOverallSubtitle;
    private TextView tvOverallStatus;

    private boolean overallEditMode = false;
    private final Map<String, ModuleBlock> moduleBlocks = new HashMap<>();
    private final Map<String, JSONObject> cachedGuides = new HashMap<>();

    private static final String[] OVERALL_MODULES = {"articulation", "syntax", "vocabulary", "social", "prelinguistic"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_plan);

        fName = getIntent().getStringExtra("fName");

        tvTitle = findViewById(R.id.tv_title);
        tvDate = findViewById(R.id.tv_date);
        overallContainer = findViewById(R.id.overall_container);
        planActions = findViewById(R.id.layout_plan_actions);
        rvPlan = findViewById(R.id.rv_plan);
        btnLeft = findViewById(R.id.btn_export_plan_pdf);
        btnRight = findViewById(R.id.btn_save_plan);
        btnOverallEdit = findViewById(R.id.btn_overall_edit);
        tvOverallSubtitle = findViewById(R.id.tv_overall_subtitle);
        tvOverallStatus = findViewById(R.id.tv_overall_status);

        String planJson = getIntent().getStringExtra("planJsonString");
        if (planJson == null || planJson.trim().isEmpty()) {
            planJson = getIntent().getStringExtra("planJson");
        }
        boolean preferIncomingPlan = getIntent().getBooleanExtra("preferIncomingPlan", false);

        if (!preferIncomingPlan) {
            String savedPlan = loadPlanFromFile(fName);
            if (savedPlan != null && !savedPlan.trim().isEmpty()) {
                planJson = savedPlan;
            }
        }

        JSONObject planObject = parsePlan(planJson);
        mChildData = null;
        JSONObject evaluations = null;
        JSONArray evaluationsA = null;
        if (fName != null && !fName.trim().isEmpty()) {
            try {
                mChildData = dataManager.getInstance().loadData(fName);
                evaluations = mChildData.optJSONObject("evaluations");
                evaluationsA = evaluations == null ? null : evaluations.optJSONArray("A");
            } catch (Exception ignored) {
            }
        }

        if (shouldShowOverallIntervention(mChildData)) {
            initOverallInterventionUi();
            return;
        }
        currentPlan = ArticulationPlanHelper.ensureArticulation(planObject, evaluationsA);
        if (mChildData != null) {
            ArticulationPlanHelper.applyArticulationReport(currentPlan, mChildData, evaluationsA);
        }
        ModuleReportHelper.applyModuleFindings(currentPlan, evaluations);
        items.clear();
        items.addAll(buildUiItems(currentPlan));

        RecyclerView recyclerView = findViewById(R.id.rv_plan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TreatmentPlanAdapter(items);
        recyclerView.setAdapter(adapter);

        Button exportButton = findViewById(R.id.btn_export_plan_pdf);
        exportButton.setOnClickListener(v -> exportPlanPdf());

        Button saveButton = findViewById(R.id.btn_save_plan);
        saveButton.setOnClickListener(v -> savePlan());
    }

    private boolean shouldShowOverallIntervention(JSONObject childData) {
        if (childData == null) {
            return false;
        }
        for (String module : OVERALL_MODULES) {
            JSONObject guide = ModuleReportHelper.loadModuleInterventionGuide(childData, module);
            if (guide == null) {
                return false;
            }
            String summary = guide.optString("overallSummary", "");
            if (summary == null || summary.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void initOverallInterventionUi() {
        if (tvTitle != null) {
            tvTitle.setText("总体干预报告");
        }
        if (tvDate != null) {
            tvDate.setText(" ");
        }
        if (overallContainer != null) {
            overallContainer.setVisibility(View.VISIBLE);
        }
        if (rvPlan != null) {
            rvPlan.setVisibility(View.GONE);
        }

        bindOverallModuleBlocks();
        loadOverallGuides();
        bindOverallHeader();
        bindOverallViews();
        setOverallEditMode(false);

        if (btnOverallEdit != null) {
            btnOverallEdit.setOnClickListener(v -> setOverallEditMode(!overallEditMode));
        }

        if (planActions != null) {
            planActions.setVisibility(View.VISIBLE);
        }
        if (btnLeft != null) {
            btnLeft.setText("保存修改");
            btnLeft.setOnClickListener(v -> saveOverallGuides(false));
        }
        if (btnRight != null) {
            btnRight.setText("确认并保存");
            btnRight.setOnClickListener(v -> saveOverallGuides(true));
        }
    }

    private void bindOverallModuleBlocks() {
        moduleBlocks.clear();
        moduleBlocks.put("articulation", new ModuleBlock(
                findViewById(R.id.tv_articulation_overall),
                findViewById(R.id.tv_articulation_focus),
                findViewById(R.id.tv_articulation_goal),
                findViewById(R.id.tv_articulation_home),
                findViewById(R.id.et_articulation_overall),
                findViewById(R.id.et_articulation_focus),
                findViewById(R.id.et_articulation_goal),
                findViewById(R.id.et_articulation_home)
        ));
        moduleBlocks.put("syntax", new ModuleBlock(
                findViewById(R.id.tv_syntax_overall),
                findViewById(R.id.tv_syntax_focus),
                findViewById(R.id.tv_syntax_goal),
                findViewById(R.id.tv_syntax_home),
                findViewById(R.id.et_syntax_overall),
                findViewById(R.id.et_syntax_focus),
                findViewById(R.id.et_syntax_goal),
                findViewById(R.id.et_syntax_home)
        ));
        moduleBlocks.put("vocabulary", new ModuleBlock(
                findViewById(R.id.tv_vocabulary_overall),
                findViewById(R.id.tv_vocabulary_focus),
                findViewById(R.id.tv_vocabulary_goal),
                findViewById(R.id.tv_vocabulary_home),
                findViewById(R.id.et_vocabulary_overall),
                findViewById(R.id.et_vocabulary_focus),
                findViewById(R.id.et_vocabulary_goal),
                findViewById(R.id.et_vocabulary_home)
        ));
        moduleBlocks.put("social", new ModuleBlock(
                findViewById(R.id.tv_social_overall),
                findViewById(R.id.tv_social_focus),
                findViewById(R.id.tv_social_goal),
                findViewById(R.id.tv_social_home),
                findViewById(R.id.et_social_overall),
                findViewById(R.id.et_social_focus),
                findViewById(R.id.et_social_goal),
                findViewById(R.id.et_social_home)
        ));
        moduleBlocks.put("prelinguistic", new ModuleBlock(
                findViewById(R.id.tv_prelinguistic_overall),
                findViewById(R.id.tv_prelinguistic_focus),
                findViewById(R.id.tv_prelinguistic_goal),
                findViewById(R.id.tv_prelinguistic_home),
                findViewById(R.id.et_prelinguistic_overall),
                findViewById(R.id.et_prelinguistic_focus),
                findViewById(R.id.et_prelinguistic_goal),
                findViewById(R.id.et_prelinguistic_home)
        ));
    }

    private void loadOverallGuides() {
        cachedGuides.clear();
        if (mChildData == null) {
            return;
        }
        for (String module : OVERALL_MODULES) {
            JSONObject guide = ModuleReportHelper.loadModuleInterventionGuide(mChildData, module);
            try {
                guide = ModuleInterventionGuideSchema.normalize(
                        guide,
                        module,
                        ModuleReportHelper.moduleTitle(module),
                        ModuleReportHelper.defaultSubtypes(module));
            } catch (Exception e) {
                guide = new JSONObject();
            }
            cachedGuides.put(module, guide);
        }
    }

    private void bindOverallHeader() {
        if (tvOverallSubtitle != null) {
            tvOverallSubtitle.setText("包含模块：构音 / 句法 / 词汇 / 社交 / 前语言");
        }
        if (tvOverallStatus != null) {
            tvOverallStatus.setText("阅读态可快速浏览要点；编辑后可逐模块保存。 ");
        }
    }

    private void bindOverallViews() {
        for (String module : OVERALL_MODULES) {
            ModuleBlock block = moduleBlocks.get(module);
            JSONObject guide = cachedGuides.get(module);
            if (block == null || guide == null) {
                continue;
            }
            String overall = guide.optString("overallSummary", "");
            JSONArray focus = guide.optJSONArray("focus");
            JSONObject smart = guide.optJSONObject("smartGoal");
            String goal = smart == null ? "" : smart.optString("text", "");
            JSONArray home = guide.optJSONArray("homeGuidance");

            if (block.tvOverall != null) {
                block.tvOverall.setText(safeOrPlaceholder(overall));
            }
            if (block.tvFocus != null) {
                block.tvFocus.setText(formatBulletList(focus));
            }
            if (block.tvGoal != null) {
                block.tvGoal.setText(safeOrPlaceholder(goal));
            }
            if (block.tvHome != null) {
                block.tvHome.setText(formatBulletList(home));
            }

            if (block.etOverall != null) {
                block.etOverall.setText(overall);
            }
            if (block.etFocus != null) {
                block.etFocus.setText(joinLines(focus));
            }
            if (block.etGoal != null) {
                block.etGoal.setText(goal);
            }
            if (block.etHome != null) {
                block.etHome.setText(joinLines(home));
            }
        }
    }

    private void setOverallEditMode(boolean enable) {
        overallEditMode = enable;
        if (btnOverallEdit instanceof TextView) {
            ((TextView) btnOverallEdit).setText(enable ? "取消" : "编辑");
        }
        for (String module : OVERALL_MODULES) {
            ModuleBlock block = moduleBlocks.get(module);
            if (block == null) {
                continue;
            }
            block.setEditMode(enable);
        }
        if (!enable) {
            bindOverallViews();
        }
    }

    private void saveOverallGuides(boolean reviewed) {
        if (mChildData == null || fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到个案信息", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            for (String module : OVERALL_MODULES) {
                JSONObject cached = cachedGuides.get(module);
                ModuleBlock block = moduleBlocks.get(module);
                if (cached == null || block == null) {
                    continue;
                }
                JSONObject guide = new JSONObject(cached.toString());
                guide.put("overallSummary", safeText(block.etOverall == null ? "" : block.etOverall.getText().toString()));
                guide.put("focus", arrayFromText(block.etFocus == null ? "" : block.etFocus.getText().toString()));

                JSONObject smart = guide.optJSONObject("smartGoal");
                if (smart == null) {
                    smart = new JSONObject();
                }
                smart.put("text", safeText(block.etGoal == null ? "" : block.etGoal.getText().toString()));
                guide.put("smartGoal", smart);
                guide.put("homeGuidance", arrayFromText(block.etHome == null ? "" : block.etHome.getText().toString()));

                JSONObject meta = guide.optJSONObject("meta");
                if (meta == null) {
                    meta = new JSONObject();
                    guide.put("meta", meta);
                }
                if (reviewed) {
                    meta.put("reviewStatus", "reviewed");
                    meta.put("reviewedByTherapist", true);
                }

                guide = ModuleInterventionGuideSchema.normalize(
                        guide,
                        module,
                        ModuleReportHelper.moduleTitle(module),
                        ModuleReportHelper.defaultSubtypes(module));

                ModuleReportHelper.saveModuleInterventionGuide(mChildData, module, guide);
                cachedGuides.put(module, guide);
            }
            dataManager.getInstance().saveChildJson(fName, mChildData);
            setOverallEditMode(false);
            Toast.makeText(this, reviewed ? "已确认并保存" : "已保存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private CharSequence formatBulletList(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "—";
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int shown = 0;
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
            shown++;
        }
        if (shown == 0) {
            return "—";
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb;
    }

    private String joinLines(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            String line = array.optString(i, "");
            if (line == null) {
                continue;
            }
            line = line.trim();
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

    private JSONArray arrayFromText(String text) {
        JSONArray array = new JSONArray();
        if (text == null || text.trim().isEmpty()) {
            return array;
        }
        String[] lines = text.split("\\n");
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                array.put(line.trim());
            }
        }
        return array;
    }

    private String safeOrPlaceholder(String value) {
        String text = value == null ? "" : value.trim();
        return text.isEmpty() ? "—" : text;
    }

    private static final class ModuleBlock {
        final TextView tvOverall;
        final TextView tvFocus;
        final TextView tvGoal;
        final TextView tvHome;
        final EditText etOverall;
        final EditText etFocus;
        final EditText etGoal;
        final EditText etHome;

        ModuleBlock(TextView tvOverall,
                    TextView tvFocus,
                    TextView tvGoal,
                    TextView tvHome,
                    EditText etOverall,
                    EditText etFocus,
                    EditText etGoal,
                    EditText etHome) {
            this.tvOverall = tvOverall;
            this.tvFocus = tvFocus;
            this.tvGoal = tvGoal;
            this.tvHome = tvHome;
            this.etOverall = etOverall;
            this.etFocus = etFocus;
            this.etGoal = etGoal;
            this.etHome = etHome;
        }

        void setEditMode(boolean enable) {
            setVisibility(tvOverall, !enable);
            setVisibility(tvFocus, !enable);
            setVisibility(tvGoal, !enable);
            setVisibility(tvHome, !enable);

            setVisibility(etOverall, enable);
            setVisibility(etFocus, enable);
            setVisibility(etGoal, enable);
            setVisibility(etHome, enable);
        }

        private void setVisibility(View v, boolean visible) {
            if (v == null) {
                return;
            }
            v.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private String loadPlanFromFile(String fName) {
        if (fName == null || fName.trim().isEmpty()) {
            return null;
        }
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject plan = data.optJSONObject("treatmentPlan");
            if (plan != null) {
                return plan.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void savePlan() {
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "\u672a\u627e\u5230\u4e2a\u6848\u4fe1\u606f", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String rawAge = getKeyValue("case_summary.age_months");
            Integer ageMonths = null;
            if (rawAge != null && !rawAge.trim().isEmpty()) {
                ageMonths = parseAgeMonths(rawAge);
                if (ageMonths == null) {
                    Toast.makeText(this, "\u5e74\u9f84\u683c\u5f0f\u4e0d\u6b63\u786e\uff0c\u8bf7\u8f93\u5165\u5982\uff1a\u0033\u5c81\u0032\u6708\u3001\u0033\u5c81\u3001\u0036\u6708\u6216\u7eaf\u6570\u5b57", Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                ageMonths = getExistingAgeMonths();
            }
            JSONObject plan = buildPlanJsonFromItems(ageMonths == null ? 0 : ageMonths);
            JSONObject data = dataManager.getInstance().loadData(fName);
            data.put("treatmentPlan", plan);
            dataManager.getInstance().saveChildJson(fName, data);
            Toast.makeText(this, "\u5df2\u4fdd\u5b58", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "\u4fdd\u5b58\u5931\u8d25: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportPlanPdf() {
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "\u672a\u627e\u5230\u4e2a\u6848\u4fe1\u606f", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject data;
        try {
            data = dataManager.getInstance().loadData(fName);
            JSONObject plan = data.optJSONObject("treatmentPlan");
            if (plan == null) {
                Toast.makeText(this, "\u8bf7\u5148\u4fdd\u5b58\u6cbb\u7597\u65b9\u6848\u518d\u5bfc\u51fa", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "\u8bfb\u53d6\u4e2a\u6848\u5931\u8d25: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String safeName = buildPlanPdfFileName(data);
        intent.putExtra(Intent.EXTRA_TITLE, safeName + "_\u6cbb\u7597\u65b9\u6848.pdf");
        startActivityForResult(intent, REQUEST_EXPORT_PDF);
    }

    private String buildPlanPdfFileName(JSONObject data) {
        String name = "";
        if (data != null) {
            JSONObject info = data.optJSONObject("info");
            if (info != null) {
                name = info.optString("name", "");
            }
        }
        if (name == null || name.trim().isEmpty()) {
            name = "\u672a\u547d\u540d";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private JSONObject parsePlan(String planJson) {
        if (planJson == null || planJson.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(planJson);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private List<PlanUiItem> buildUiItems(JSONObject plan) {
        List<PlanUiItem> list = new ArrayList<>();
        speechStageNames.clear();

        // 1. 个人信息模块
        list.add(new PlanUiItem.SectionDivider("个人信息"));
        list.add(buildPersonalInfoCard());

        // 2. 干预指导模块
        list.add(new PlanUiItem.SectionDivider("干预指导"));

        JSONObject caseSummary = plan.optJSONObject("case_summary");
        List<String> keyFindings = getStringList(caseSummary, "key_findings");

        JSONObject modulePlan = plan.optJSONObject("module_plan");
        addModuleCard(list, CARD_SPEECH_SOUND, "speech_sound",
                modulePlan == null ? null : modulePlan.optJSONObject("speech_sound"), true, keyFindings);
        addModuleCard(list, CARD_PRELINGUISTIC, "prelinguistic",
                modulePlan == null ? null : modulePlan.optJSONObject("prelinguistic"), false, keyFindings);
        addModuleCard(list, CARD_VOCABULARY, "vocabulary",
                modulePlan == null ? null : modulePlan.optJSONObject("vocabulary"), false, keyFindings);
        addModuleCard(list, CARD_SYNTAX, "syntax",
                modulePlan == null ? null : modulePlan.optJSONObject("syntax"), false, keyFindings);
        addModuleCard(list, CARD_SOCIAL, "social_pragmatics",
                modulePlan == null ? null : modulePlan.optJSONObject("social_pragmatics"), false, keyFindings);

        return list;
    }

    private PlanUiItem.ModuleCard buildPersonalInfoCard() {
        List<PlanUiItem> children = new ArrayList<>();
        JSONObject info = mChildData == null ? null : mChildData.optJSONObject("info");
        
        String name = info == null ? "" : info.optString("name", "");
        String gender = "";
        if (info != null && info.has("gender")) {
            int g = info.optInt("gender");
            gender = g == 0 ? "男" : (g == 1 ? "女" : "");
        }
        String birthday = info == null ? "" : info.optString("birthday", "");
        String address = info == null ? "" : info.optString("address", "");
        
        // 计算年龄
        int ageMonths = getExistingAgeMonths();
        String ageStr = formatAgeMonths(ageMonths);

        children.add(new PlanUiItem.KeyValue("info.name", "姓名", name, InputType.TYPE_CLASS_TEXT));
        children.add(new PlanUiItem.KeyValue("info.gender", "性别", gender, InputType.TYPE_CLASS_TEXT));
        children.add(new PlanUiItem.KeyValue("info.birthday", "出生日期", birthday, InputType.TYPE_DATETIME_VARIATION_DATE));
        children.add(new PlanUiItem.KeyValue("case_summary.age_months", "年龄", ageStr, InputType.TYPE_CLASS_TEXT));
        children.add(new PlanUiItem.KeyValue("info.address", "家庭住址", address, InputType.TYPE_CLASS_TEXT));

        String contact = info == null ? "" : info.optString("contact", "");
        if (!contact.isEmpty()) {
            children.add(new PlanUiItem.KeyValue("info.contact", "联系方式", contact, InputType.TYPE_CLASS_PHONE));
        }

        return new PlanUiItem.ModuleCard("基本信息", "base_info", children);
    }

    private void addModuleCard(List<PlanUiItem> list, String label, String moduleKey, JSONObject moduleObj,
                               boolean speech, List<String> caseFindings) {
        list.add(new PlanUiItem.ModuleCard(label, moduleKey, buildModuleChildren(moduleKey, moduleObj, speech, caseFindings)));
    }

    private List<PlanUiItem> buildModuleChildren(String moduleKey, JSONObject moduleObj, boolean speech, List<String> caseFindings) {
        List<PlanUiItem> children = new ArrayList<>();
        
        // 1. 测试结果 (InfoBox)
        List<PlanUiItem> findingsItems = new ArrayList<>();
        String findingsTitle = speech ? SECTION_TEST_RESULTS : SECTION_FINDINGS;
        List<String> moduleFindings = getStringList(moduleObj, "key_findings");
        
        String findingPath = "module_plan." + moduleKey + ".key_findings";
        if (moduleFindings.isEmpty()) {
             findingsItems.add(new PlanUiItem.ListItem(findingPath, 0, ""));
        } else {
             for (int i = 0; i < moduleFindings.size(); i++) {
                 findingsItems.add(new PlanUiItem.ListItem(findingPath, i, moduleFindings.get(i)));
             }
        }
        findingsItems.add(new PlanUiItem.AddButton(findingPath, "新增一条结果"));
        children.add(new PlanUiItem.InfoBox(findingsTitle, findingsItems));

        // 2. 干预计划内容
        if (speech && hasArticulationPlan(moduleObj)) {
            addSpeechArticulationPlan(children, moduleObj);
        } else {
            addListGroupToChildren(children, "目标", "module_plan." + moduleKey + ".targets",
                    getStringList(moduleObj, "targets"));
            
            List<String> methods = getStringList(moduleObj, "methods");
            if (methods.isEmpty()) methods = getStringList(moduleObj, "activities");
            addListGroupToChildren(children, "活动/方法", "module_plan." + moduleKey + ".methods", methods);
            
            addListGroupToChildren(children, "家庭练习", "module_plan." + moduleKey + ".home_practice",
                    getStringList(moduleObj, "home_practice"));
        }
        return children;
    }

    private void addSpeechArticulationPlan(List<PlanUiItem> children, JSONObject moduleObj) {
        if (moduleObj == null) return;
        JSONObject articulation = moduleObj.optJSONObject("articulation");
        if (articulation == null) return;
        String base = "module_plan.speech_sound.articulation";

        JSONObject overall = articulation.optJSONObject("overall_summary");
        children.add(new PlanUiItem.SectionHeader(SECTION_ARTICULATION_OVERALL, 2));
        children.add(new PlanUiItem.KeyValue(base + ".overall_summary.text", LABEL_OVERALL_SUMMARY,
                overall == null ? "" : overall.optString("text", ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));

        JSONObject mastered = articulation.optJSONObject("mastered");
        children.add(new PlanUiItem.SectionHeader(SECTION_ARTICULATION_MASTERED, 2));
        children.add(new PlanUiItem.KeyValue(base + ".mastered.intro", LABEL_MASTERED_INTRO,
                mastered == null ? "" : mastered.optString("intro", ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
        addListGroupToChildren(children, null, base + ".mastered.items", getStringList(mastered, "items"));

        JSONObject notMastered = articulation.optJSONObject("not_mastered_overview");
        children.add(new PlanUiItem.SectionHeader(SECTION_ARTICULATION_NOT_MASTERED, 2));
        children.add(new PlanUiItem.KeyValue(base + ".not_mastered_overview.text", LABEL_NOT_MASTERED,
                notMastered == null ? "" : notMastered.optString("text", ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));

        JSONObject focus = articulation.optJSONObject("focus");
        children.add(new PlanUiItem.SectionHeader(safeTitle(focus == null ? "" : focus.optString("title", ""),
                SECTION_ARTICULATION_FOCUS), 2));
        addArticulationListGroupToChildren(children, null, base + ".focus.items", getStringList(focus, "items"));
        children.add(new PlanUiItem.KeyValue(base + ".focus.note", LABEL_FOCUS_NOTE,
                focus == null ? "" : focus.optString("note", ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));

        JSONObject unstable = articulation.optJSONObject("unstable");
        children.add(new PlanUiItem.SectionHeader(safeTitle(unstable == null ? "" : unstable.optString("title", ""),
                SECTION_ARTICULATION_UNSTABLE), 2));
        addArticulationListGroupToChildren(children, null, base + ".unstable.items", getStringList(unstable, "items"));

        JSONObject smartGoal = articulation.optJSONObject("smart_goal");
        children.add(new PlanUiItem.SectionHeader(SECTION_ARTICULATION_SMART, 2));
        children.add(new PlanUiItem.KeyValue(base + ".smart_goal.cycle_weeks", LABEL_SMART_CYCLE,
                smartGoal == null ? "" : smartGoal.optString("cycle_weeks", ""), InputType.TYPE_CLASS_TEXT));
        children.add(new PlanUiItem.KeyValue(base + ".smart_goal.level", LABEL_SMART_LEVEL,
                smartGoal == null ? "" : smartGoal.optString("level", ""), InputType.TYPE_CLASS_TEXT));
        
        String accuracyValue = "";
        if (smartGoal != null && smartGoal.has("accuracy_threshold")) {
            accuracyValue = String.valueOf(smartGoal.optDouble("accuracy_threshold", 0.8));
        }
        children.add(new PlanUiItem.KeyValue(base + ".smart_goal.accuracy_threshold", LABEL_SMART_ACCURACY,
                accuracyValue, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));

        addListGroupToChildren(children, LABEL_SMART_TARGET_SOUNDS, base + ".smart_goal.target_sounds",
                getStringList(smartGoal, "target_sounds"));
        addListGroupToChildren(children, LABEL_SMART_SUPPORT, base + ".smart_goal.support",
                getStringList(smartGoal, "support"));

        String smartText = smartGoal == null ? "" : smartGoal.optString("text", "");
        if (smartText.isEmpty() && smartGoal != null) {
            smartText = ArticulationPlanHelper.buildSmartGoalText(smartGoal);
        }
        children.add(new PlanUiItem.KeyValue(base + ".smart_goal.text", LABEL_SMART_TEXT,
                smartText, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));

        JSONObject homeGuidance = articulation.optJSONObject("home_guidance");
        children.add(new PlanUiItem.SectionHeader(SECTION_ARTICULATION_HOME, 2));
        addArticulationListGroupToChildren(children, null, base + ".home_guidance.items", getStringList(homeGuidance, "items"));
    }

    private void addListGroupToChildren(List<PlanUiItem> children, String title, String listPath, List<String> values) {
        if (title != null && !title.trim().isEmpty()) {
            children.add(new PlanUiItem.SectionHeader(title, 2));
        }
        List<String> safeValues = new ArrayList<>(values);
        if (safeValues.isEmpty()) {
            safeValues.add("");
        }
        int index = 0;
        for (String value : safeValues) {
            children.add(new PlanUiItem.ListItem(listPath, index++, value));
        }
        children.add(new PlanUiItem.AddButton(listPath, "新增一行"));
    }

    private void addArticulationListGroupToChildren(List<PlanUiItem> children, String title, String listPath, List<String> values) {
        if (title != null && !title.trim().isEmpty()) {
            children.add(new PlanUiItem.SectionHeader(title, 2));
        }
        List<String> safeValues = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                String text = safeText(value);
                if (!text.isEmpty()) {
                    safeValues.add(text);
                }
            }
        }
        if (safeValues.isEmpty()) {
            safeValues.add(ArticulationPlanHelper.MISSING_DETAIL_HINT);
        }
        int index = 0;
        for (String value : safeValues) {
            children.add(new PlanUiItem.ListItem(listPath, index++, value));
        }
        children.add(new PlanUiItem.AddButton(listPath, "新增一行"));
    }

    private boolean hasArticulationPlan(JSONObject moduleObj) {
        return moduleObj != null && moduleObj.optJSONObject("articulation") != null;
    }

    private String safeTitle(String title, String fallback) {
        String value = safeText(title);
        return value.isEmpty() ? fallback : value;
    }

    private List<String> getStringList(JSONObject obj, String key) {
        List<String> result = new ArrayList<>();
        if (obj == null || key == null) {
            return result;
        }
        JSONArray arr = obj.optJSONArray(key);
        if (arr == null) {
            return result;
        }
        for (int i = 0; i < arr.length(); i++) {
            String value = arr.optString(i, "");
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> sanitizeArticulationList(List<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            String text = safeText(value);
            if (text.isEmpty() || ArticulationPlanHelper.MISSING_DETAIL_HINT.equals(text)) {
                continue;
            }
            result.add(text);
        }
        return result;
    }

    private JSONObject getCurrentSpeechPlan() {
        if (currentPlan == null) {
            return null;
        }
        JSONObject modulePlan = currentPlan.optJSONObject("module_plan");
        return modulePlan == null ? null : modulePlan.optJSONObject("speech_sound");
    }

    private JSONObject getCurrentModule(String key) {
        if (currentPlan == null || key == null) {
            return null;
        }
        JSONObject modulePlan = currentPlan.optJSONObject("module_plan");
        return modulePlan == null ? null : modulePlan.optJSONObject(key);
    }

    private JSONObject ensureObject(JSONObject parent, String key) throws JSONException {
        JSONObject obj = parent.optJSONObject(key);
        if (obj == null) {
            obj = new JSONObject();
            parent.put(key, obj);
        }
        return obj;
    }

    private JSONArray resolveList(Map<String, List<String>> listValues, String path, JSONObject existingSpeech, String key) throws JSONException {
        if (listValues.containsKey(path)) {
            return toArray(listValues.get(path));
        }
        if (existingSpeech != null) {
            JSONArray existing = existingSpeech.optJSONArray(key);
            if (existing != null) {
                return new JSONArray(existing.toString());
            }
        }
        return new JSONArray();
    }

    private JSONArray resolveStages(Map<String, List<String>> listValues, String base, JSONObject existingSpeech) throws JSONException {
        if (hasStageEdits(listValues, base + ".stages[")) {
            return buildSpeechStages(listValues, base, existingSpeech);
        }
        if (existingSpeech != null) {
            JSONArray existing = existingSpeech.optJSONArray("stages");
            if (existing != null) {
                return new JSONArray(existing.toString());
            }
        }
        return buildSpeechStages(listValues, base, existingSpeech);
    }

    private boolean hasStageEdits(Map<String, List<String>> listValues, String prefix) {
        for (String key : listValues.keySet()) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private JSONArray buildSpeechStages(Map<String, List<String>> listValues, String base, JSONObject existingSpeech) throws JSONException {
        JSONArray stages = new JSONArray();
        JSONArray existingStages = existingSpeech == null ? null : existingSpeech.optJSONArray("stages");
        int stageCount = Math.max(speechStageNames.size(), DEFAULT_SPEECH_STAGE_NAMES.length);
        for (int i = 0; i < stageCount; i++) {
            String name = i < speechStageNames.size() ? speechStageNames.get(i) : "";
            if (name == null || name.trim().isEmpty()) {
                name = i < DEFAULT_SPEECH_STAGE_NAMES.length ? DEFAULT_SPEECH_STAGE_NAMES[i] : "\u9636\u6bb5" + (i + 1);
            }
            JSONObject stage = new JSONObject();
            stage.put("name", name);
            String stageBase = base + ".stages[" + i + "]";
            JSONObject existingStage = existingStages == null ? null : existingStages.optJSONObject(i);
            stage.put("focus", resolveList(listValues, stageBase + ".focus", existingStage, "focus"));
            stage.put("activities", resolveList(listValues, stageBase + ".activities", existingStage, "activities"));
            stage.put("home_practice", resolveList(listValues, stageBase + ".home_practice", existingStage, "home_practice"));
            stage.put("metrics", resolveList(listValues, stageBase + ".metrics", existingStage, "metrics"));
            stages.put(stage);
        }
        return stages;
    }

    private JSONObject buildPlanJsonFromItems(int ageMonths) throws JSONException {
        Map<String, String> keyValues = new HashMap<>();
        Map<String, List<String>> listValues = new LinkedHashMap<>();

        // Recursively collect values
        collectValues(items, keyValues, listValues);

        JSONObject plan = new JSONObject();

        JSONObject existingSummary = currentPlan == null ? null : currentPlan.optJSONObject("case_summary");
        JSONObject caseSummary = new JSONObject();
        caseSummary.put("age_months", ageMonths);
        caseSummary.put("chief_complaint", safeText(keyValues.get("case_summary.chief_complaint")));
        caseSummary.put("key_findings", resolveArray(listValues, "case_summary.key_findings",
                existingSummary == null ? null : existingSummary.optJSONArray("key_findings")));
        caseSummary.put("suspected_diagnosis", resolveArray(listValues, "case_summary.suspected_diagnosis",
                existingSummary == null ? null : existingSummary.optJSONArray("suspected_diagnosis")));
        caseSummary.put("risk_flags", resolveArray(listValues, "case_summary.risk_flags",
                existingSummary == null ? null : existingSummary.optJSONArray("risk_flags")));
        plan.put("case_summary", caseSummary);

        JSONObject overallGoals = new JSONObject();
        overallGoals.put("within_4_weeks", toArray(listValues.get("overall_goals.within_4_weeks")));
        overallGoals.put("within_12_weeks", toArray(listValues.get("overall_goals.within_12_weeks")));
        overallGoals.put("within_24_weeks", toArray(listValues.get("overall_goals.within_24_weeks")));
        plan.put("overall_goals", overallGoals);

        JSONObject modulePlan = new JSONObject();
        modulePlan.put("speech_sound", buildSpeechModulePlan(listValues, keyValues));
        modulePlan.put("prelinguistic", buildModulePlan(listValues, "module_plan.prelinguistic", getCurrentModule("prelinguistic")));
        modulePlan.put("vocabulary", buildModulePlan(listValues, "module_plan.vocabulary", getCurrentModule("vocabulary")));
        modulePlan.put("syntax", buildModulePlan(listValues, "module_plan.syntax", getCurrentModule("syntax")));
        modulePlan.put("social_pragmatics", buildModulePlan(listValues, "module_plan.social_pragmatics", getCurrentModule("social_pragmatics")));
        plan.put("module_plan", modulePlan);

        JSONObject existingSchedule = currentPlan == null ? null : currentPlan.optJSONObject("schedule_recommendation");
        JSONObject schedule = new JSONObject();
        schedule.put("sessions_per_week", resolveInt(keyValues, "schedule_recommendation.sessions_per_week",
                existingSchedule == null ? 0 : existingSchedule.optInt("sessions_per_week", 0)));
        schedule.put("minutes_per_session", resolveInt(keyValues, "schedule_recommendation.minutes_per_session",
                existingSchedule == null ? 0 : existingSchedule.optInt("minutes_per_session", 0)));
        schedule.put("review_in_weeks", resolveInt(keyValues, "schedule_recommendation.review_in_weeks",
                existingSchedule == null ? 0 : existingSchedule.optInt("review_in_weeks", 0)));
        plan.put("schedule_recommendation", schedule);

        JSONArray existingTherapist = currentPlan == null ? null : currentPlan.optJSONArray("notes_for_therapist");
        JSONArray existingParents = currentPlan == null ? null : currentPlan.optJSONArray("notes_for_parents");
        plan.put("notes_for_therapist", resolveArray(listValues, "notes_for_therapist", existingTherapist));
        plan.put("notes_for_parents", resolveArray(listValues, "notes_for_parents", existingParents));

        return plan;
    }

    private void collectValues(List<PlanUiItem> items, Map<String, String> keyValues, Map<String, List<String>> listValues) {
        if (items == null) return;
        for (PlanUiItem item : items) {
            if (item instanceof PlanUiItem.KeyValue) {
                PlanUiItem.KeyValue kv = (PlanUiItem.KeyValue) item;
                keyValues.put(kv.keyPath, safeText(kv.value));
            } else if (item instanceof PlanUiItem.ListItem) {
                PlanUiItem.ListItem li = (PlanUiItem.ListItem) item;
                listValues.computeIfAbsent(li.listPath, k -> new ArrayList<>()).add(safeText(li.value));
            } else if (item instanceof PlanUiItem.ModuleCard) {
                collectValues(((PlanUiItem.ModuleCard) item).children, keyValues, listValues);
            } else if (item instanceof PlanUiItem.InfoBox) {
                collectValues(((PlanUiItem.InfoBox) item).children, keyValues, listValues);
            }
        }
    }

    private String findKeyValue(List<PlanUiItem> items, String keyPath) {
        if (items == null) return "";
        for (PlanUiItem item : items) {
            if (item instanceof PlanUiItem.KeyValue) {
                PlanUiItem.KeyValue kv = (PlanUiItem.KeyValue) item;
                if (keyPath.equals(kv.keyPath)) {
                    return kv.value;
                }
            } else if (item instanceof PlanUiItem.ModuleCard) {
                String val = findKeyValue(((PlanUiItem.ModuleCard) item).children, keyPath);
                if (!val.isEmpty()) return val;
            } else if (item instanceof PlanUiItem.InfoBox) {
                String val = findKeyValue(((PlanUiItem.InfoBox) item).children, keyPath);
                if (!val.isEmpty()) return val;
            }
        }
        return "";
    }

    private JSONObject buildSpeechModulePlan(Map<String, List<String>> listValues, Map<String, String> keyValues) throws JSONException {
        String base = "module_plan.speech_sound";
        JSONObject module = new JSONObject();
        JSONObject existingSpeech = getCurrentSpeechPlan();
        module.put("key_findings", resolveList(listValues, base + ".key_findings", existingSpeech, "key_findings"));
        module.put("targets", resolveList(listValues, base + ".targets", existingSpeech, "targets"));
        module.put("methods", resolveList(listValues, base + ".methods", existingSpeech, "methods"));
        module.put("sample_activities", resolveList(listValues, base + ".sample_activities", existingSpeech, "sample_activities"));
        module.put("home_practice", resolveList(listValues, base + ".home_practice", existingSpeech, "home_practice"));
        module.put("metrics", resolveList(listValues, base + ".metrics", existingSpeech, "metrics"));
        module.put("stages", resolveStages(listValues, base, existingSpeech));

        if (existingSpeech != null) {
            JSONObject existingArticulation = existingSpeech.optJSONObject("articulation");
            if (existingArticulation != null) {
                module.put("articulation", new JSONObject(existingArticulation.toString()));
            }
        }
        JSONObject articulation = ArticulationPlanHelper.ensureSpeechSoundArticulation(module, null);
        if (articulation != null) {
            String articulationBase = base + ".articulation";
            JSONObject overall = ensureObject(articulation, "overall_summary");
            String overallText = safeText(keyValues.get(articulationBase + ".overall_summary.text"));
            if (!overallText.isEmpty()) {
                overall.put("text", overallText);
            }
            String overallLevel = safeText(keyValues.get(articulationBase + ".overall_summary.level"));
            if (!overallLevel.isEmpty()) {
                overall.put("level", overallLevel);
            }

            JSONObject mastered = ensureObject(articulation, "mastered");
            String masteredIntro = safeText(keyValues.get(articulationBase + ".mastered.intro"));
            if (!masteredIntro.isEmpty()) {
                mastered.put("intro", masteredIntro);
            }
            mastered.put("items", toArray(listValues.get(articulationBase + ".mastered.items")));

            JSONObject notMastered = ensureObject(articulation, "not_mastered_overview");
            String notMasteredText = safeText(keyValues.get(articulationBase + ".not_mastered_overview.text"));
            if (!notMasteredText.isEmpty()) {
                notMastered.put("text", notMasteredText);
            }

            JSONObject focus = ensureObject(articulation, "focus");
            String focusTitle = safeText(keyValues.get(articulationBase + ".focus.title"));
            if (!focusTitle.isEmpty()) {
                focus.put("title", focusTitle);
            }
            focus.put("items", toArray(sanitizeArticulationList(listValues.get(articulationBase + ".focus.items"))));
            if (keyValues.containsKey(articulationBase + ".focus.note")) {
                focus.put("note", safeText(keyValues.get(articulationBase + ".focus.note")));
            }

            JSONObject unstable = ensureObject(articulation, "unstable");
            String unstableTitle = safeText(keyValues.get(articulationBase + ".unstable.title"));
            if (!unstableTitle.isEmpty()) {
                unstable.put("title", unstableTitle);
            }
            unstable.put("items", toArray(sanitizeArticulationList(listValues.get(articulationBase + ".unstable.items"))));

            JSONObject smartGoal = ensureObject(articulation, "smart_goal");
            String cycleWeeks = safeText(keyValues.get(articulationBase + ".smart_goal.cycle_weeks"));
            if (!cycleWeeks.isEmpty()) {
                smartGoal.put("cycle_weeks", cycleWeeks);
            }
            String level = safeText(keyValues.get(articulationBase + ".smart_goal.level"));
            if (!level.isEmpty()) {
                smartGoal.put("level", level);
            }
            double accuracy = parseDouble(keyValues.get(articulationBase + ".smart_goal.accuracy_threshold"), -1);
            if (accuracy > 0) {
                smartGoal.put("accuracy_threshold", accuracy);
            }
            JSONArray targetSounds = toArray(listValues.get(articulationBase + ".smart_goal.target_sounds"));
            smartGoal.put("target_sounds", targetSounds);
            JSONArray support = toArray(listValues.get(articulationBase + ".smart_goal.support"));
            if (support.length() == 0) {
                JSONArray existingSupport = smartGoal.optJSONArray("support");
                support = existingSupport == null ? support : existingSupport;
            }
            if (support.length() > 0) {
                smartGoal.put("support", support);
            }
            smartGoal.put("text", ArticulationPlanHelper.buildSmartGoalText(smartGoal));

            JSONObject homeGuidance = ensureObject(articulation, "home_guidance");
            List<String> guidanceItems = sanitizeArticulationList(listValues.get(articulationBase + ".home_guidance.items"));
            if (!guidanceItems.isEmpty()) {
                homeGuidance.put("items", toArray(guidanceItems));
            }
        }
        return module;
    }

    private JSONObject buildModulePlan(Map<String, List<String>> listValues, String base, JSONObject existingModule) throws JSONException {
        JSONObject module = new JSONObject();
        module.put("key_findings", resolveList(listValues, base + ".key_findings", existingModule, "key_findings"));
        module.put("targets", resolveList(listValues, base + ".targets", existingModule, "targets"));
        module.put("activities", resolveList(listValues, base + ".activities", existingModule, "activities"));
        module.put("home_practice", resolveList(listValues, base + ".home_practice", existingModule, "home_practice"));
        module.put("metrics", resolveList(listValues, base + ".metrics", existingModule, "metrics"));
        return module;
    }

    private JSONArray toArray(List<String> values) {
        JSONArray arr = new JSONArray();
        if (values == null) {
            return arr;
        }
        for (String value : values) {
            String text = safeText(value);
            if (!text.isEmpty()) {
                arr.put(text);
            }
        }
        return arr;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseInt(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private int resolveInt(Map<String, String> keyValues, String keyPath, int existingValue) {
        if (keyValues == null || keyPath == null || !keyValues.containsKey(keyPath)) {
            return existingValue;
        }
        return parseInt(keyValues.get(keyPath));
    }

    private JSONArray resolveArray(Map<String, List<String>> listValues, String keyPath, JSONArray existingValue) {
        if (listValues != null && keyPath != null && listValues.containsKey(keyPath)) {
            return toArray(listValues.get(keyPath));
        }
        if (existingValue != null) {
            try {
                return new JSONArray(existingValue.toString());
            } catch (JSONException ignored) {
                return new JSONArray();
            }
        }
        return new JSONArray();
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXPORT_PDF && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) {
                Toast.makeText(this, "未获取保存位置", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject childJson = dataManager.getInstance().loadData(fName);
                JSONObject plan = childJson.optJSONObject("treatmentPlan");
                if (plan == null) {
                    Toast.makeText(this, "请先保存治疗方案再导出", Toast.LENGTH_LONG).show();
                    return;
                }
                PdfGenerator.writeTreatmentPlanPdf(this, uri, childJson);
                Toast.makeText(this, "导出成功", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getKeyValue(String keyPath) {
        return findKeyValue(items, keyPath);
    }

    private String formatAgeMonths(int ageMonths) {
        if (ageMonths <= 0) {
            return "0\u6708";
        }
        int years = ageMonths / 12;
        int months = ageMonths % 12;
        if (years > 0 && months > 0) {
            return years + "\u5c81" + months + "\u6708";
        }
        if (years > 0) {
            return years + "\u5c81";
        }
        return months + "\u6708";
    }

    private Integer parseAgeMonths(String input) {
        if (input == null) {
            return 0;
        }
        String text = input.trim();
        if (text.isEmpty()) {
            return 0;
        }
        text = text.replace(" ", "").replace("\u3000", "");
        try {
            if (text.matches("^\\d+$")) {
                return Integer.parseInt(text);
            }
            if (text.matches("^\\d+\\u6708$")) {
                return Integer.parseInt(text.replace("\u6708", ""));
            }
            if (text.matches("^\\d+\\u5c81$")) {
                int years = Integer.parseInt(text.replace("\u5c81", ""));
                return years * 12;
            }
            if (text.matches("^(\\d+)\\u5c81(\\d+)\\u6708$")) {
                String[] parts = text.replace("\u5c81", ",").replace("\u6708", "").split(",");
                int years = Integer.parseInt(parts[0]);
                int months = Integer.parseInt(parts[1]);
                return years * 12 + months;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private int getExistingAgeMonths() {
        if (currentPlan == null) {
            return 0;
        }
        JSONObject summary = currentPlan.optJSONObject("case_summary");
        return summary == null ? 0 : summary.optInt("age_months", 0);
    }
}
