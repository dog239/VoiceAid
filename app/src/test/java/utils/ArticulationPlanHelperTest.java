package utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArticulationPlanHelperTest {
    @Test
    public void buildDefaultArticulation_hasRequiredFields() throws Exception {
        JSONObject articulation = ArticulationPlanHelper.buildDefaultArticulation(null);
        assertNotNull(articulation);

        JSONObject overall = articulation.optJSONObject("overall_summary");
        assertNotNull(overall);
        assertTrue(overall.optString("text", "").length() > 0);

        JSONObject mastered = articulation.optJSONObject("mastered");
        assertNotNull(mastered);
        JSONArray masteredItems = mastered.optJSONArray("items");
        assertNotNull(masteredItems);
        assertTrue(masteredItems.length() >= 1);

        JSONObject smartGoal = articulation.optJSONObject("smart_goal");
        assertNotNull(smartGoal);
        assertEquals("8-10", smartGoal.optString("cycle_weeks"));
        assertEquals(0.80, smartGoal.optDouble("accuracy_threshold"), 0.001);

        JSONObject homeGuidance = articulation.optJSONObject("home_guidance");
        assertNotNull(homeGuidance);
        JSONArray guidanceItems = homeGuidance.optJSONArray("items");
        assertNotNull(guidanceItems);
        assertTrue(guidanceItems.length() >= 2);
    }

    @Test
    public void buildSmartGoalText_usesPlaceholderWhenTargetsMissing() throws Exception {
        JSONObject articulation = ArticulationPlanHelper.buildDefaultArticulation(null);
        JSONObject smartGoal = articulation.optJSONObject("smart_goal");
        assertNotNull(smartGoal);
        smartGoal.put("target_sounds", new JSONArray());
        smartGoal.put("accuracy_threshold", 0.80);

        String text = ArticulationPlanHelper.buildSmartGoalText(smartGoal);
        assertTrue(text.contains("80%"));
        assertTrue(text.contains("目标辅音（以评估结果为准）"));
    }
}
