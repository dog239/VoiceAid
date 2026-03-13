package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utils.dataManager;

public class SocialResultActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv3;
    private Button back;
    private String fName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_social_result);

        back = findViewById(R.id.back);
        tv3 = findViewById(R.id.tv_3);

        back.setOnClickListener(this);

        // 获取传递的数据
        Intent intent = getIntent();
        fName = intent.getStringExtra("fName");

        try {
            if (fName == null) {
                Toast.makeText(this, "缺少必要参数", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            initData();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "加载评估数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 题目详情数据结构
    private static class QuestionDetail {
        int num;
        String ability;
        String focus;
        String content;
        Integer score;
        int groupNumber;

        QuestionDetail(int num, String ability, String focus, String content, Integer score, int groupNumber) {
            this.num = num;
            this.ability = ability;
            this.focus = focus;
            this.content = content;
            this.score = score;
            this.groupNumber = groupNumber;
        }
    }

    private void initData() throws Exception {
        JSONObject data = dataManager.getInstance().loadData(fName);
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) {
            evaluations = new JSONObject();
        }

        // 检查是否有任何一组题已经完成
        boolean hasCompletedAnyGroup = false;
        JSONArray socialArray = evaluations.optJSONArray("SOCIAL");
        if (socialArray != null && socialArray.length() > 0) {
            for (int i = 0; i < socialArray.length(); i++) {
                JSONObject object = socialArray.getJSONObject(i);
                if (object.has("score") && !object.isNull("score")) {
                    hasCompletedAnyGroup = true;
                    break;
                }
            }
        }

        // 如果没有完成的组，弹出提示并结束活动
        if (!hasCompletedAnyGroup) {
            Toast.makeText(this, "请先完成一组题目再生成报告", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 按组分类统计结果
        Map<Integer, ArrayList<QuestionDetail>> groupQuestionDetails = new HashMap<>();
        ArrayList<String> weaknessList = new ArrayList<>();
        ArrayList<String> inProgressList = new ArrayList<>();
        int totalScore = 0;
        int completedQuestions = 0;

        // 加载所有组的数据，但只显示已完成的组的题目
        for (int group = 1; group <= 6; group++) {
            ArrayList<QuestionDetail> groupDetails = new ArrayList<>();
            boolean hasCompletedQuestions = false;

            for (int i = 0; i < 10; i++) {
                int questionIndex = (group - 1) * 10 + i;
                if (socialArray != null && questionIndex < socialArray.length()) {
                    JSONObject object = socialArray.getJSONObject(questionIndex);
                    if (object.has("score") && !object.isNull("score")) {
                        hasCompletedQuestions = true;
                        completedQuestions++;
                        
                        // 收集题目详情
                        int num = object.optInt("num", questionIndex + 1);
                        String ability = object.optString("ability", "");
                        String focus = object.optString("focus", "");
                        String content = object.optString("content", "");
                        Integer score = object.getInt("score");
                        totalScore += score;

                        // 收集需要重点关注和进一步发展的能力
                        if (score == 0 && !weaknessList.contains(focus)) {
                            weaknessList.add(focus);
                        } else if (score == 1 && !inProgressList.contains(focus)) {
                            inProgressList.add(focus);
                        }

                        groupDetails.add(new QuestionDetail(num, ability, focus, content, score, group));
                    }
                }
            }

            if (hasCompletedQuestions) {
                groupQuestionDetails.put(group, groupDetails);
            }
        }

        // 填充题目详情表格
        fillQuestionDetailTable(groupQuestionDetails);

        // 生成评估建议
        generateEvaluationSuggestion(totalScore, completedQuestions, weaknessList, inProgressList);

        // 保存评估报告
        saveEvaluationReport(data, totalScore, weaknessList, inProgressList);
    }

    private void fillQuestionDetailTable(Map<Integer, ArrayList<QuestionDetail>> groupQuestionDetails) {
        TableLayout tableLayout = findViewById(R.id.question_detail_table);
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1); // 保留表头

        if (groupQuestionDetails.isEmpty()) {
            // 添加空数据提示
            TableRow row = new TableRow(this);
            row.setBackgroundColor(getResources().getColor(android.R.color.white));

            TextView emptyText = new TextView(this);
            emptyText.setText("暂无题目数据");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(12, 12, 12, 12);
            emptyText.setTextSize(14);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            params.span = 4;
            emptyText.setLayoutParams(params);

            row.addView(emptyText);
            tableLayout.addView(row);
            return;
        }

        // 按组号从小到大显示
        for (int group = 1; group <= 6; group++) {
            ArrayList<QuestionDetail> groupDetails = groupQuestionDetails.get(group);
            if (groupDetails != null && !groupDetails.isEmpty()) {
                // 添加分组标题
                TableRow groupHeaderRow = new TableRow(this);
                groupHeaderRow.setBackgroundColor(0xFFE0E0E0);

                TextView groupHeaderText = new TextView(this);
                groupHeaderText.setText("第" + group + "组");
                groupHeaderText.setGravity(Gravity.CENTER);
                groupHeaderText.setPadding(12, 12, 12, 12);
                groupHeaderText.setTextSize(16);
                groupHeaderText.setTextColor(getResources().getColor(android.R.color.black));
                TableRow.LayoutParams headerParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                headerParams.span = 4;
                groupHeaderText.setLayoutParams(headerParams);

                groupHeaderRow.addView(groupHeaderText);
                tableLayout.addView(groupHeaderRow);

                // 添加题目数据行
                for (int i = 0; i < groupDetails.size(); i++) {
                    QuestionDetail detail = groupDetails.get(i);
                    TableRow row = new TableRow(this);
                    row.setBackgroundColor(i % 2 == 0 ? getResources().getColor(android.R.color.white) : 0xF9F9F9);

                    // 题目序号
                    TextView numText = new TextView(this);
                    numText.setText(String.valueOf(detail.num));
                    numText.setGravity(Gravity.CENTER);
                    numText.setPadding(12, 12, 12, 12);
                    numText.setTextSize(16);
                    numText.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(numText);

                    // 社交能力考查点
                    TextView abilityText = new TextView(this);
                    abilityText.setText(detail.ability);
                    abilityText.setGravity(Gravity.CENTER);
                    abilityText.setPadding(12, 12, 12, 12);
                    abilityText.setTextSize(16);
                    abilityText.setTextColor(getResources().getColor(android.R.color.black));
                    abilityText.setSingleLine(false);
                    abilityText.setMaxWidth(200);
                    abilityText.setMaxLines(3);
                    row.addView(abilityText);

                    // 题目内容
                    TextView contentText = new TextView(this);
                    StringBuilder contentBuilder = new StringBuilder(detail.content);
                    // 在选择0或者1时在题目内容后面加个括号填进考查点
                    if (detail.score != null && detail.score < 2) {
                        contentBuilder.append(" (").append(detail.focus).append(")");
                    }
                    contentText.setText(contentBuilder.toString());
                    contentText.setGravity(Gravity.CENTER);
                    contentText.setPadding(12, 12, 12, 12);
                    contentText.setTextSize(16);
                    contentText.setTextColor(getResources().getColor(android.R.color.black));
                    contentText.setSingleLine(false);
                    contentText.setMaxWidth(350);
                    contentText.setMaxLines(3);
                    row.addView(contentText);

                    // 得分
                    TextView scoreText = new TextView(this);
                    scoreText.setText(String.valueOf(detail.score));
                    scoreText.setGravity(Gravity.CENTER);
                    scoreText.setPadding(12, 12, 12, 12);
                    scoreText.setTextSize(16);
                    scoreText.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(scoreText);

                    tableLayout.addView(row);
                }
            }
        }
    }

    private void generateEvaluationSuggestion(int totalScore, int completedQuestions, ArrayList<String> weaknessList, ArrayList<String> inProgressList) {
        // 计算总体正确率
        double accuracy = 0;
        if (completedQuestions > 0) {
            accuracy = (double) totalScore / (completedQuestions * 2);
        }

        // 生成总体评估
        String overallEvaluation;
        if (accuracy >= 0.6) {
            overallEvaluation = "1. 从整体上来说，孩子的社交能力较好，基本达标。";
        } else {
            overallEvaluation = "2. 从整体上来说，孩子的社交能力还有待进一步发展，尚未达标。";
        }

        // 构建评估建议文本
        StringBuilder suggestionBuilder = new StringBuilder();
        suggestionBuilder.append("通过对儿童社交能力的评估（家长试卷）结果如下：\n\n");
        suggestionBuilder.append(overallEvaluation + "\n\n");
        suggestionBuilder.append("评价准则如下：根据测试结果显示：\n");
        suggestionBuilder.append("选1：如果大于等于6/10，基本达标。\n");
        suggestionBuilder.append("选2：小于6/10，则尚未达标。\n\n");
        suggestionBuilder.append("总分：" + totalScore + "/" + (completedQuestions * 2) + "\n");

        tv3.setText(suggestionBuilder.toString());

        // 显示需要重点关注的能力
        LinearLayout weaknessLayout = findViewById(R.id.weakness_list);
        weaknessLayout.removeAllViews();
        if (!weaknessList.isEmpty()) {
            for (int i = 0; i < weaknessList.size(); i++) {
                TextView textView = new TextView(this);
                textView.setText((i + 1) + ". " + weaknessList.get(i));
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                textView.setSingleLine(false);
                weaknessLayout.addView(textView);
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText("暂无需要重点关注的能力");
            textView.setTextSize(16);
            textView.setPadding(5, 5, 5, 5);
            weaknessLayout.addView(textView);
        }

        // 显示需要进一步发展的能力
        LinearLayout inProgressLayout = findViewById(R.id.in_progress_list);
        inProgressLayout.removeAllViews();
        if (!inProgressList.isEmpty()) {
            for (int i = 0; i < inProgressList.size(); i++) {
                TextView textView = new TextView(this);
                textView.setText((i + 1) + ". " + inProgressList.get(i));
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                textView.setSingleLine(false);
                inProgressLayout.addView(textView);
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText("暂无需要进一步发展的能力");
            textView.setTextSize(16);
            textView.setPadding(5, 5, 5, 5);
            inProgressLayout.addView(textView);
        }
    }

    private void saveEvaluationReport(JSONObject data, int totalScore, ArrayList<String> weaknessList, ArrayList<String> inProgressList) {
        try {
            JSONObject report = new JSONObject();
            report.put("totalScore", totalScore);
            report.put("weaknesses", new JSONArray(weaknessList));
            report.put("inProgress", new JSONArray(inProgressList));
            
            JSONObject evaluations = data.optJSONObject("evaluations");
            if (evaluations == null) {
                evaluations = new JSONObject();
                data.put("evaluations", evaluations);
            }
            evaluations.put("socialReport", report);
            
            // 保存数据
            dataManager.getInstance().saveData(fName, data);
            Toast.makeText(this, "已保存社交能力评估报告", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存社交能力评估报告失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        }
    }
}
