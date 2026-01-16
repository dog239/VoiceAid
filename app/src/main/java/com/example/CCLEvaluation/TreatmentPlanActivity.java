package com.example.CCLEvaluation;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.widget.Button;
import android.widget.Toast;

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

import utils.dataManager;

public class TreatmentPlanActivity extends AppCompatActivity {
    private static final int REQUEST_EXPORT_PDF = 3001;
    private String fName;
    private final List<PlanUiItem> items = new ArrayList<>();
    private TreatmentPlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_plan);

        fName = getIntent().getStringExtra("fName");

        String planJson = getIntent().getStringExtra("planJsonString");
        if (planJson == null || planJson.trim().isEmpty()) {
            planJson = getIntent().getStringExtra("planJson");
        }

        String savedPlan = loadPlanFromFile(fName);
        if (savedPlan != null && !savedPlan.trim().isEmpty()) {
            planJson = savedPlan;
        }

        JSONObject planObject = parsePlan(planJson);
        items.clear();
        items.addAll(buildUiItems(planObject));

        RecyclerView recyclerView = findViewById(R.id.rv_plan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TreatmentPlanAdapter(items, new TreatmentPlanAdapter.ListActionListener() {
            @Override
            public void onAddRow(String listPath, int position) {
                addListRow(listPath, position);
            }

            @Override
            public void onRemoveRow(PlanUiItem.ListItem item, int position) {
                removeListRow(item, position);
            }
        });
        recyclerView.setAdapter(adapter);

        Button exportButton = findViewById(R.id.btn_export_plan_pdf);
        exportButton.setOnClickListener(v -> exportPlanPdf());

        Button saveButton = findViewById(R.id.btn_save_plan);
        saveButton.setOnClickListener(v -> savePlan());
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
            Integer ageMonths = parseAgeMonths(getKeyValue("case_summary.age_months"));
            if (ageMonths == null) {
                Toast.makeText(this, "\u5e74\u9f84\u683c\u5f0f\u4e0d\u6b63\u786e\uff0c\u8bf7\u8f93\u5165\u5982\uff1a\u0033\u5c81\u0032\u6708\u3001\u0033\u5c81\u3001\u0036\u6708\u6216\u7eaf\u6570\u5b57", Toast.LENGTH_LONG).show();
                return;
            }
            JSONObject plan = buildPlanJsonFromItems(ageMonths);
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

    private void addListRow(String listPath, int position) {
        PlanUiItem.ListItem newItem = new PlanUiItem.ListItem(listPath, 0, "");
        items.add(position, newItem);
        refreshListIndexes(listPath);
        adapter.notifyItemInserted(position);
    }

    private void removeListRow(PlanUiItem.ListItem item, int position) {
        items.remove(position);
        refreshListIndexes(item.listPath);
        adapter.notifyItemRemoved(position);
    }

    private void refreshListIndexes(String listPath) {
        int index = 0;
        for (PlanUiItem item : items) {
            if (item instanceof PlanUiItem.ListItem) {
                PlanUiItem.ListItem listItem = (PlanUiItem.ListItem) item;
                if (listPath.equals(listItem.listPath)) {
                    listItem.index = index++;
                }
            }
        }
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

        JSONObject caseSummary = plan.optJSONObject("case_summary");
        int ageMonths = caseSummary == null ? 0 : caseSummary.optInt("age_months", 0);
        String chiefComplaint = caseSummary == null ? "" : caseSummary.optString("chief_complaint", "");

        list.add(new PlanUiItem.SectionHeader("\u4e2a\u6848\u6458\u8981", 0));
        list.add(new PlanUiItem.KeyValue("case_summary.age_months", "\u5e74\u9f84", formatAgeMonths(ageMonths), InputType.TYPE_CLASS_TEXT));
        list.add(new PlanUiItem.KeyValue("case_summary.chief_complaint", "\u4e3b\u8bc9", chiefComplaint, InputType.TYPE_CLASS_TEXT));

        addListGroup(list, "\u5173\u952e\u53d1\u73b0", "case_summary.key_findings",
                getStringList(caseSummary, "key_findings"), 1);
        addListGroup(list, "\u7591\u4f3c\u8bca\u65ad", "case_summary.suspected_diagnosis",
                getStringList(caseSummary, "suspected_diagnosis"), 1);
        addListGroup(list, "\u98ce\u9669\u63d0\u793a", "case_summary.risk_flags",
                getStringList(caseSummary, "risk_flags"), 1);

        JSONObject overallGoals = plan.optJSONObject("overall_goals");
        list.add(new PlanUiItem.SectionHeader("\u603b\u4f53\u76ee\u6807", 0));
        addListGroup(list, "\u0034\u5468\u5185\u76ee\u6807", "overall_goals.within_4_weeks",
                getStringList(overallGoals, "within_4_weeks"), 1);
        addListGroup(list, "\u0031\u0032\u5468\u5185\u76ee\u6807", "overall_goals.within_12_weeks",
                getStringList(overallGoals, "within_12_weeks"), 1);
        addListGroup(list, "\u0032\u0034\u5468\u5185\u76ee\u6807", "overall_goals.within_24_weeks",
                getStringList(overallGoals, "within_24_weeks"), 1);

        JSONObject modulePlan = plan.optJSONObject("module_plan");
        list.add(new PlanUiItem.SectionHeader("\u6a21\u5757\u8ba1\u5212", 0));
        addModuleSection(list, "\u8bed\u97f3", "speech_sound",
                modulePlan == null ? null : modulePlan.optJSONObject("speech_sound"), true);
        addModuleSection(list, "\u8bed\u524d", "prelinguistic",
                modulePlan == null ? null : modulePlan.optJSONObject("prelinguistic"), false);
        addModuleSection(list, "\u8bcd\u6c47", "vocabulary",
                modulePlan == null ? null : modulePlan.optJSONObject("vocabulary"), false);
        addModuleSection(list, "\u53e5\u6cd5", "syntax",
                modulePlan == null ? null : modulePlan.optJSONObject("syntax"), false);
        addModuleSection(list, "\u793e\u4ea4", "social_pragmatics",
                modulePlan == null ? null : modulePlan.optJSONObject("social_pragmatics"), false);

        JSONObject schedule = plan.optJSONObject("schedule_recommendation");
        int sessionsPerWeek = schedule == null ? 0 : schedule.optInt("sessions_per_week", 0);
        int minutesPerSession = schedule == null ? 0 : schedule.optInt("minutes_per_session", 0);
        int reviewWeeks = schedule == null ? 0 : schedule.optInt("review_in_weeks", 0);

        list.add(new PlanUiItem.SectionHeader("\u9891\u6b21\u5efa\u8bae", 0));
        list.add(new PlanUiItem.KeyValue("schedule_recommendation.sessions_per_week", "\u6bcf\u5468\u6b21\u6570", String.valueOf(sessionsPerWeek), InputType.TYPE_CLASS_NUMBER));
        list.add(new PlanUiItem.KeyValue("schedule_recommendation.minutes_per_session", "\u6bcf\u6b21\u65f6\u957f\u0028\u5206\u949f\u0029", String.valueOf(minutesPerSession), InputType.TYPE_CLASS_NUMBER));
        list.add(new PlanUiItem.KeyValue("schedule_recommendation.review_in_weeks", "\u590d\u8bc4\u5468\u671f\u0028\u5468\u0029", String.valueOf(reviewWeeks), InputType.TYPE_CLASS_NUMBER));

        list.add(new PlanUiItem.SectionHeader("\u6cbb\u7597\u5e08\u5907\u6ce8", 0));
        addListGroup(list, null, "notes_for_therapist", getStringList(plan, "notes_for_therapist"), 1);

        list.add(new PlanUiItem.SectionHeader("\u5bb6\u957f\u5efa\u8bae", 0));
        addListGroup(list, null, "notes_for_parents", getStringList(plan, "notes_for_parents"), 1);

        return list;
    }

    private void addModuleSection(List<PlanUiItem> list, String label, String moduleKey, JSONObject moduleObj, boolean speech) {
        list.add(new PlanUiItem.SectionHeader(label, 1));
        addListGroup(list, "\u76ee\u6807", "module_plan." + moduleKey + ".targets",
                getStringList(moduleObj, "targets"), 2);
        if (speech) {
            List<String> methods = getStringList(moduleObj, "methods");
            if (methods.isEmpty()) {
                methods = getStringList(moduleObj, "activities");
            }
            addListGroup(list, "\u65b9\u6cd5", "module_plan." + moduleKey + ".methods", methods, 2);
        } else {
            List<String> activities = getStringList(moduleObj, "activities");
            if (activities.isEmpty()) {
                activities = getStringList(moduleObj, "methods");
            }
            addListGroup(list, "\u6d3b\u52a8", "module_plan." + moduleKey + ".activities", activities, 2);
        }
        addListGroup(list, "\u5bb6\u5ead\u7ec3\u4e60", "module_plan." + moduleKey + ".home_practice",
                getStringList(moduleObj, "home_practice"), 2);
        addListGroup(list, "\u6307\u6807", "module_plan." + moduleKey + ".metrics",
                getStringList(moduleObj, "metrics"), 2);
    }

    private void addListGroup(List<PlanUiItem> list, String title, String listPath, List<String> values, int level) {
        if (title != null && !title.trim().isEmpty()) {
            list.add(new PlanUiItem.SectionHeader(title, level));
        }
        List<String> safeValues = new ArrayList<>(values);
        if (safeValues.isEmpty()) {
            safeValues.add("");
        }
        int index = 0;
        for (String value : safeValues) {
            list.add(new PlanUiItem.ListItem(listPath, index++, value));
        }
        list.add(new PlanUiItem.AddButton(listPath, "\u65b0\u589e\u4e00\u884c"));
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

    private JSONObject buildPlanJsonFromItems(int ageMonths) throws JSONException {
        Map<String, String> keyValues = new HashMap<>();
        Map<String, List<String>> listValues = new LinkedHashMap<>();

        for (PlanUiItem item : items) {
            if (item instanceof PlanUiItem.KeyValue) {
                PlanUiItem.KeyValue kv = (PlanUiItem.KeyValue) item;
                keyValues.put(kv.keyPath, safeText(kv.value));
            } else if (item instanceof PlanUiItem.ListItem) {
                PlanUiItem.ListItem li = (PlanUiItem.ListItem) item;
                listValues.computeIfAbsent(li.listPath, k -> new ArrayList<>()).add(safeText(li.value));
            }
        }

        JSONObject plan = new JSONObject();

        JSONObject caseSummary = new JSONObject();
        caseSummary.put("age_months", ageMonths);
        caseSummary.put("chief_complaint", safeText(keyValues.get("case_summary.chief_complaint")));
        caseSummary.put("key_findings", toArray(listValues.get("case_summary.key_findings")));
        caseSummary.put("suspected_diagnosis", toArray(listValues.get("case_summary.suspected_diagnosis")));
        caseSummary.put("risk_flags", toArray(listValues.get("case_summary.risk_flags")));
        plan.put("case_summary", caseSummary);

        JSONObject overallGoals = new JSONObject();
        overallGoals.put("within_4_weeks", toArray(listValues.get("overall_goals.within_4_weeks")));
        overallGoals.put("within_12_weeks", toArray(listValues.get("overall_goals.within_12_weeks")));
        overallGoals.put("within_24_weeks", toArray(listValues.get("overall_goals.within_24_weeks")));
        plan.put("overall_goals", overallGoals);

        JSONObject modulePlan = new JSONObject();
        modulePlan.put("speech_sound", buildModulePlan(listValues, "module_plan.speech_sound", true));
        modulePlan.put("prelinguistic", buildModulePlan(listValues, "module_plan.prelinguistic", false));
        modulePlan.put("vocabulary", buildModulePlan(listValues, "module_plan.vocabulary", false));
        modulePlan.put("syntax", buildModulePlan(listValues, "module_plan.syntax", false));
        modulePlan.put("social_pragmatics", buildModulePlan(listValues, "module_plan.social_pragmatics", false));
        plan.put("module_plan", modulePlan);

        JSONObject schedule = new JSONObject();
        schedule.put("sessions_per_week", parseInt(keyValues.get("schedule_recommendation.sessions_per_week")));
        schedule.put("minutes_per_session", parseInt(keyValues.get("schedule_recommendation.minutes_per_session")));
        schedule.put("review_in_weeks", parseInt(keyValues.get("schedule_recommendation.review_in_weeks")));
        plan.put("schedule_recommendation", schedule);

        plan.put("notes_for_therapist", toArray(listValues.get("notes_for_therapist")));
        plan.put("notes_for_parents", toArray(listValues.get("notes_for_parents")));

        return plan;
    }

    private JSONObject buildModulePlan(Map<String, List<String>> listValues, String base, boolean speech) throws JSONException {
        JSONObject module = new JSONObject();
        module.put("targets", toArray(listValues.get(base + ".targets")));
        if (speech) {
            module.put("methods", toArray(listValues.get(base + ".methods")));
        } else {
            module.put("activities", toArray(listValues.get(base + ".activities")));
        }
        module.put("home_practice", toArray(listValues.get(base + ".home_practice")));
        module.put("metrics", toArray(listValues.get(base + ".metrics")));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXPORT_PDF && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) {
                Toast.makeText(this, "\u672a\u83b7\u53d6\u4fdd\u5b58\u4f4d\u7f6e", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject childJson = dataManager.getInstance().loadData(fName);
                JSONObject plan = childJson.optJSONObject("treatmentPlan");
                if (plan == null) {
                    Toast.makeText(this, "\u8bf7\u5148\u4fdd\u5b58\u6cbb\u7597\u65b9\u6848\u518d\u5bfc\u51fa", Toast.LENGTH_LONG).show();
                    return;
                }
                PdfGenerator.writeTreatmentPlanPdf(this, uri, childJson);
                Toast.makeText(this, "\u5bfc\u51fa\u6210\u529f", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "\u5bfc\u51fa\u5931\u8d25: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getKeyValue(String keyPath) {
        for (PlanUiItem item : items) {
            if (item instanceof PlanUiItem.KeyValue) {
                PlanUiItem.KeyValue kv = (PlanUiItem.KeyValue) item;
                if (keyPath.equals(kv.keyPath)) {
                    return kv.value;
                }
            }
        }
        return "";
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
}
