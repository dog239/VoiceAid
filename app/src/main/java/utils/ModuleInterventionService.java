package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModuleInterventionService {

    public interface Callback {
        void onSuccess(JSONObject interventionGuide);

        void onError(String errorMessage);
    }

    public void generate(JSONObject childData, String moduleType, Callback callback) {
        if (callback == null) {
            return;
        }
        if (childData == null) {
            callback.onError("缺少测评数据");
            return;
        }
        final String safeModuleType = ModuleReportHelper.normalizeModuleType(moduleType);
        final String moduleTitle = ModuleReportHelper.moduleTitle(safeModuleType);
        final JSONArray subtypes = ModuleReportHelper.defaultSubtypes(safeModuleType);
        try {
            String systemPrompt = ModuleInterventionPromptBuilder.buildSystemPrompt();
            String userPrompt = ModuleInterventionPromptBuilder.buildUserPrompt(safeModuleType, childData);
            new LlmPlanService().generateTreatmentPlan(systemPrompt, userPrompt, new LlmPlanService.PlanCallback() {
                @Override
                public void onSuccess(JSONObject plan) {
                    try {
                        JSONObject rawGuide = extractGuide(plan);
                        JSONObject normalized = ModuleInterventionGuideSchema.normalize(
                                rawGuide, safeModuleType, moduleTitle, subtypes);
                        callback.onSuccess(normalized);
                    } catch (Exception e) {
                        callback.onError("解析模块干预报告失败: " + e.getMessage());
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
        } catch (JSONException e) {
            callback.onError("构建模块干预请求失败: " + e.getMessage());
        }
    }

    private JSONObject extractGuide(JSONObject raw) throws JSONException {
        if (raw == null) {
            return new JSONObject();
        }
        JSONObject guide = raw.optJSONObject("interventionGuide");
        if (guide != null) {
            return guide;
        }
        guide = raw.optJSONObject("intervention_guide");
        if (guide != null) {
            return guide;
        }
        // If model already returned the guide root object directly.
        if (raw.has("moduleType") || raw.has("overallSummary") || raw.has("smartGoal")) {
            return raw;
        }
        return raw;
    }
}

