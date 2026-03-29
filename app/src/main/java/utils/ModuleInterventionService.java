package utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import utils.rag.module.AssetModuleKnowledgeRepository;
import utils.rag.module.ModuleKnowledgeRepository;
import utils.rag.module.ModuleRagConfig;
import utils.rag.module.ModuleRagContextBuilder;
import utils.rag.module.ModuleRagQueryBuilder;
import utils.rag.module.ModuleRagRetriever;
import utils.rag.module.RagHit;
import utils.rag.module.RagQuery;

public class ModuleInterventionService {
    private static final String TAG = "ModuleInterventionSvc";

    private final ModuleKnowledgeRepository knowledgeRepository;
    private final ModuleRagRetriever ragRetriever;
    private final ModuleRagContextBuilder ragContextBuilder;
    private final boolean enableModuleRag;

    public interface Callback {
        void onSuccess(JSONObject interventionGuide);

        void onError(String errorMessage);
    }

    public ModuleInterventionService() {
        this(resolveAppContext(),
                new ModuleRagRetriever(),
                new ModuleRagContextBuilder(),
                ModuleRagConfig.ENABLE_MODULE_RAG);
    }

    ModuleInterventionService(Context context,
                              ModuleRagRetriever ragRetriever,
                              ModuleRagContextBuilder ragContextBuilder,
                              boolean enableModuleRag) {
        this(new AssetModuleKnowledgeRepository(context),
                ragRetriever,
                ragContextBuilder,
                enableModuleRag);
    }

    ModuleInterventionService(ModuleKnowledgeRepository knowledgeRepository,
                              ModuleRagRetriever ragRetriever,
                              ModuleRagContextBuilder ragContextBuilder,
                              boolean enableModuleRag) {
        this.knowledgeRepository = knowledgeRepository;
        this.ragRetriever = ragRetriever == null ? new ModuleRagRetriever() : ragRetriever;
        this.ragContextBuilder = ragContextBuilder == null ? new ModuleRagContextBuilder() : ragContextBuilder;
        this.enableModuleRag = enableModuleRag;
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
            String userPrompt = buildPromptWithRagFallback(childData, safeModuleType);
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

    private String buildPromptWithRagFallback(JSONObject childData, String moduleType) throws JSONException {
        boolean ragEnabled = enableModuleRag && ModuleRagConfig.isEnabledFor(moduleType);
        if (!ragEnabled) {
            Log.i(TAG, "moduleType=" + moduleType + " ragEnabled=false fallback=legacy");
            return ModuleInterventionPromptBuilder.buildUserPrompt(moduleType, childData);
        }

        try {
            JSONObject moduleInput = ModuleInterventionPromptBuilder.buildModuleInput(moduleType, childData);
            RagQuery query = ModuleRagQueryBuilder.build(moduleType, moduleInput);
            Log.i(TAG, "moduleType=" + moduleType + " ragEnabled=true query=" + query.toJson());

            List<RagHit> hits = ragRetriever.retrieve(
                    query,
                    knowledgeRepository == null ? new ArrayList<>() : knowledgeRepository.load(moduleType),
                    ModuleRagConfig.DEFAULT_TOP_K
            );
            Log.i(TAG, "moduleType=" + moduleType
                    + " ragEnabled=true hitCount=" + hits.size()
                    + " hitDocIds=" + collectDocIds(hits));

            String ragContext = ragContextBuilder.build(hits);
            if (ragContext.trim().isEmpty()) {
                Log.i(TAG, "moduleType=" + moduleType + " ragEnabled=true fallback=empty_hits");
                return ModuleInterventionPromptBuilder.buildUserPrompt(moduleType, childData);
            }
            Log.i(TAG, "moduleType=" + moduleType + " ragEnabled=true fallback=false");
            return ModuleInterventionPromptBuilder.buildUserPromptWithRag(moduleType, childData, ragContext);
        } catch (Exception e) {
            Log.w(TAG, "moduleType=" + moduleType + " ragEnabled=true fallback=exception", e);
            return ModuleInterventionPromptBuilder.buildUserPrompt(moduleType, childData);
        }
    }

    private List<String> collectDocIds(List<RagHit> hits) {
        List<String> out = new ArrayList<>();
        if (hits == null) {
            return out;
        }
        for (RagHit hit : hits) {
            if (hit != null && hit.doc != null && hit.doc.id != null && !hit.doc.id.trim().isEmpty()) {
                out.add(hit.doc.id.trim());
            }
        }
        return out;
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
        if (raw.has("moduleType") || raw.has("overallSummary") || raw.has("smartGoal")) {
            return raw;
        }
        return raw;
    }

    private static Context resolveAppContext() {
        Context context = ResultContext.getInstance().getContext();
        return context == null ? null : context.getApplicationContext();
    }
}
