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

import utils.AudioPlayer;
import utils.dataManager;

public class SyntaxResultActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv3;
    private Button back;
    private String fName;
    private String format; // RG 或 SE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_syntax_result);

        back = findViewById(R.id.back);
        tv3 = findViewById(R.id.tv_3);

        back.setOnClickListener(this);

        // 获取传递的数据
        Intent intent = getIntent();
        fName = intent.getStringExtra("fName");
        format = intent.getStringExtra("format");
        // 提取format的前两个字符（RG或SE）
        if (format != null && format.length() >= 2) {
            format = format.substring(0, 2);
        }

        // 打印调试信息
        System.out.println("DEBUG: fName = " + fName);
        System.out.println("DEBUG: format = " + format);
        System.out.println("DEBUG: format length = " + (format != null ? format.length() : "null"));
        try {
            if (fName == null || format == null) {
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
        String question;
        boolean result;
        String audioPath;
        String testStage;
        String testLanguage;

        QuestionDetail(int num, String question, boolean result, String audioPath, String testStage, String testLanguage) {
            this.num = num;
            this.question = question;
            this.result = result;
            this.audioPath = audioPath;
            this.testStage = testStage;
            this.testLanguage = testLanguage;
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
        for (int i = 1; i <= 4; i++) {
            String key = format + i;
            JSONArray jsonArray = evaluations.optJSONArray(key);
            if (jsonArray != null && jsonArray.length() > 0) {
                // 检查该组是否所有题目都已完成
                boolean allQuestionsCompleted = true;
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject object = jsonArray.getJSONObject(j);
                    if (!object.has("time") || object.isNull("time")) {
                        allQuestionsCompleted = false;
                        break;
                    }
                }
                if (allQuestionsCompleted) {
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

        // 按测试点分类统计结果
        Map<String, Integer> testPointCorrectCount = new HashMap<>();
        Map<String, Integer> testPointTotalCount = new HashMap<>();
        ArrayList<QuestionDetail> questionDetails = new ArrayList<>();

        // 计算总题目数和总正确数
        int totalCorrect = 0;
        int totalQuestions = 0;

        // 加载所有组的数据，但只显示已完成的组的题目
        for (int i = 1; i <= 4; i++) {
            String key = format + i;
            JSONArray jsonArray = evaluations.optJSONArray(key);
            if (jsonArray != null && jsonArray.length() > 0) {
                // 检查该组是否所有题目都已完成
                boolean allQuestionsCompleted = true;
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject object = jsonArray.getJSONObject(j);
                    if (!object.has("result") || object.isNull("result")) {
                        allQuestionsCompleted = false;
                        break;
                    }
                }
                
                // 只处理已完成的组
                if (allQuestionsCompleted) {
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject object = jsonArray.getJSONObject(j);
                        
                        // 题目有结果，因为组已完成
                        String question = object.optString("question", "");

                        // 收集题目详情
                        int num = object.optInt("num", j + 1);
                        String audioPath = object.optString("audioPath", "");
                        String testStage = String.valueOf(i); // 测试阶段直接使用组号
                        String testLanguage = getTestLanguage(testStage, num); // 根据测试阶段和题目编号确定考查点
                        boolean result = object.getBoolean("result");
                        
                        // 更新统计数据
                        String testPoint = testLanguage; // 使用测试语言作为测试点

                        if (!testPoint.isEmpty()) {
                            testPointTotalCount.put(testPoint, testPointTotalCount.getOrDefault(testPoint, 0) + 1);
                            if (result) {
                                testPointCorrectCount.put(testPoint, testPointCorrectCount.getOrDefault(testPoint, 0) + 1);
                                totalCorrect++;
                            }

                            System.out.println("DEBUG: After update - testPointTotalCount: " + testPointTotalCount);
                            System.out.println("DEBUG: After update - testPointCorrectCount: " + testPointCorrectCount);
                        } else {
                            System.out.println("DEBUG: testPoint is empty for question num=" + num);
                        }
                        totalQuestions++;
                        
                        questionDetails.add(new QuestionDetail(num, question, result, audioPath, testStage, testPoint));
                    }
                }
            } else {
                System.out.println("DEBUG: Group " + i + " has no items");
            }
        }

        System.out.println("DEBUG: Final testPointTotalCount: " + testPointTotalCount);
        System.out.println("DEBUG: Final testPointCorrectCount: " + testPointCorrectCount);

        // 填充题目详情表格
        fillQuestionDetailTable(questionDetails);

        // 计算总体正确率
        double overallAccuracy = totalQuestions > 0 ? (double) totalCorrect / totalQuestions * 100 : 0;

        // 打印调试信息
        System.out.println("DEBUG: Before generateEvaluationSuggestion");
        System.out.println("DEBUG: testPointTotalCount = " + testPointTotalCount);
        System.out.println("DEBUG: testPointCorrectCount = " + testPointCorrectCount);
        System.out.println("DEBUG: totalQuestions = " + totalQuestions);
        System.out.println("DEBUG: totalCorrect = " + totalCorrect);
        System.out.println("DEBUG: overallAccuracy = " + overallAccuracy);

        // 生成评估建议
        generateEvaluationSuggestion(data, overallAccuracy, testPointCorrectCount, testPointTotalCount, totalCorrect, totalQuestions);

        // 显示测试点分类结果
        displayTestPointClassification(testPointCorrectCount, testPointTotalCount);
    }

    private String getTestPointType(String question) {
        // 根据题目内容判断测试点类型
        question = question.toLowerCase();
        if (question.contains("因为") || question.contains("所以") || question.contains("如果") || question.contains("就")) {
            return "复杂句";
        } else if (question.contains("和") || question.contains("与") || question.contains("或者") || question.contains("还是")) {
            return "复合句";
        } else {
            return "简单句";
        }
    }

    private void generateEvaluationSuggestion(JSONObject data, double overallAccuracy, Map<String, Integer> testPointCorrectCount, Map<String, Integer> testPointTotalCount, int totalCorrect, int totalQuestions) {
        // 生成总体评估
        String overallEvaluation1;
        String overallEvaluation2;
        if (format.equals("RG")) {
            overallEvaluation1 = "从整体上来说，孩子的句法理解能力较好，基本达标，符合该年龄段孩子语言发育水平。";
            overallEvaluation2 = "从整体上来说，孩子的句法理解能力还有待进一步发展，尚未达标。";
        } else {
            overallEvaluation1 = "从整体上来说，孩子的句法表达能力较好，基本达标，符合该年龄段孩子语言发育水平。";
            overallEvaluation2 = "从整体上来说，孩子的句法表达能力还有待进一步发展，尚未达标。";
        }

        // 确定使用哪个评估结果
        boolean useFirstEvaluation;
        if (totalQuestions >= 15) {
            // 题目总数达到15题，使用10/15的标准
            useFirstEvaluation = totalCorrect >= 10;
        } else {
            // 题目总数不足15题，使用正确率标准
            useFirstEvaluation = overallAccuracy >= 66.7;
        }

        // 识别需要重点关注的能力和不稳定的能力
        ArrayList<String> weaknessList = new ArrayList<>();
        ArrayList<String> unstableList = new ArrayList<>();

        // 手动统计篇章理解能力的正确数和总数
        int discourseCorrect = 0;
        int discourseTotal = 0;

        // 遍历所有测试点
        for (String testPoint : testPointTotalCount.keySet()) {
            int correct = testPointCorrectCount.getOrDefault(testPoint, 0);
            int total = testPointTotalCount.get(testPoint);

            // 处理篇章理解能力
            if (testPoint.equals("篇章理解能力")) {
                discourseCorrect = correct;
                discourseTotal = total;
            } 
            // 处理其他测试点
            else {
                if (correct == 0 && !weaknessList.contains(testPoint)) {
                    weaknessList.add(testPoint);
                } else if (correct <= total / 3 && !unstableList.contains(testPoint)) {
                    unstableList.add(testPoint);
                }
            }
        }

        // 特殊处理篇章理解能力（6小问）
        if (discourseTotal > 0) {
            if (discourseCorrect == 0 && !weaknessList.contains("篇章理解能力")) {
                weaknessList.add("篇章理解能力");
            } else if (discourseCorrect <= 2 && !unstableList.contains("篇章理解能力")) {
                unstableList.add("篇章理解能力");
            }
        }

        // 保存评估结果到数据中
        try {
            JSONObject report = new JSONObject();
            report.put("weaknesses", new JSONArray(weaknessList));
            report.put("unstable", new JSONArray(unstableList));
            report.put("overallAccuracy", overallAccuracy);
            report.put("totalCorrect", totalCorrect);
            report.put("totalQuestions", totalQuestions);
            
            JSONObject evaluations = data.optJSONObject("evaluations");
            if (evaluations == null) {
                evaluations = new JSONObject();
                data.put("evaluations", evaluations);
            }
            evaluations.put("syntaxReport", report);
            
            // 保存数据
            dataManager.getInstance().saveData(fName, data);
            System.out.println("DEBUG: 评估报告已保存");
        } catch (Exception e) {
            System.out.println("DEBUG: 保存评估报告失败: " + e.getMessage());
            e.printStackTrace();
        }

        // 打印最终结果
        System.out.println("DEBUG: Final weaknessList: " + weaknessList);
        System.out.println("DEBUG: Final unstableList: " + unstableList);

        // 构建评估建议文本
        StringBuilder suggestionBuilder = new StringBuilder();
        suggestionBuilder.append("通过儿童").append(format.equals("RG") ? "句法理解" : "句法表达").append("能力的评估，本次评估结果如下：\n\n");
        suggestionBuilder.append("● " + (useFirstEvaluation ? overallEvaluation1 : overallEvaluation2) + "\n\n");

        tv3.setText(suggestionBuilder.toString());

        // 显示需要重点关注的能力
        LinearLayout weaknessLayout = findViewById(R.id.weakness_list);

        if (weaknessLayout != null) {
            // 强制移除所有子视图，包括默认的硬编码内容
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
        }

        // 显示不稳定的能力
        LinearLayout unstableLayout = findViewById(R.id.in_progress_list);

        if (unstableLayout != null) {
            // 强制移除所有子视图，包括默认的硬编码内容
            unstableLayout.removeAllViews();
            if (!unstableList.isEmpty()) {
                for (int i = 0; i < unstableList.size(); i++) {
                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ". " + unstableList.get(i));
                    textView.setTextSize(16);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setSingleLine(false);
                    unstableLayout.addView(textView);
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("暂无不稳定的能力");
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                unstableLayout.addView(textView);
            }
        }
    }

    private void displayTestPointClassification(Map<String, Integer> correctCount, Map<String, Integer> totalCount) {
        // 测试点分类表格已移除，仅在后台记录数据用于评估建议
        // 该方法保留但不执行任何UI操作
    }

    private String getAbilityLevel(double accuracy) {
        if (accuracy == 0) {
            return "需要关注";
        } else if (accuracy < 66.7) {
            return "不稳定";
        } else {
            return "稳定";
        }
    }

    private String getTestStage(int groupNumber, int questionNumber) {
        // 根据组别和题目编号确定测试阶段
        // 这里可以根据实际需求进行调整
        return String.valueOf(groupNumber);
    }

    private String getTestLanguage(String testStage, int questionNumber) {
        // 根据测试阶段和题目编号确定对应测试语
        int stage = Integer.parseInt(testStage);
        
        System.out.println("DEBUG: getTestLanguage - format: " + format + ", stage: " + stage + ", questionNumber: " + questionNumber);
        
        // 句法理解部分
        if (format != null && format.equals("RG")) {
            switch (stage) {
                case 1:
                    // 第一组：
                    // 1-3题（测试语法点：主谓结构）
                    // 4-6（动宾结构）
                    // 7-9（主谓宾）
                    // 10-12（否定句）
                    // 13-15（一般疑问句）
                    // 16-18（特殊疑问句）
                    // 19-21（形容词+名词）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "主谓结构";
                        case 4:
                        case 5:
                        case 6:
                            return "动宾结构";
                        case 7:
                        case 8:
                        case 9:
                            return "主谓宾";
                        case 10:
                        case 11:
                        case 12:
                            return "否定句";
                        case 13:
                        case 14:
                        case 15:
                            return "一般疑问句";
                        case 16:
                        case 17:
                        case 18:
                            return "特殊疑问句";
                        case 19:
                        case 20:
                        case 21:
                            return "形容词+名词";
                        default:
                            return "";
                    }
                case 2:
                    // 第二组：
                    // 1-3（多重修饰）
                    // 4-6（双宾结构）
                    // 7-9（是不是问句）
                    // 10-12（地点疑问句）
                    // 13-15（副词都）
                    // 16-18（语序）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "多重修饰";
                        case 4:
                        case 5:
                        case 6:
                            return "双宾结构";
                        case 7:
                        case 8:
                        case 9:
                            return "是不是问句";
                        case 10:
                        case 11:
                        case 12:
                            return "地点疑问句";
                        case 13:
                        case 14:
                        case 15:
                            return "副词都";
                        case 16:
                        case 17:
                        case 18:
                            return "语序";
                        default:
                            return "";
                    }
                case 3:
                    // 第三组：
                    // 1-3（被动句）
                    // 4-6（比较句）
                    // 7-9（了（完成体））
                    // 10-12（因果复句）
                    // 13-15（转折句）
                    // 16-18（时间顺序句）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "被动句";
                        case 4:
                        case 5:
                        case 6:
                            return "比较句";
                        case 7:
                        case 8:
                        case 9:
                            return "了（完成体）";
                        case 10:
                        case 11:
                        case 12:
                            return "因果复句";
                        case 13:
                        case 14:
                        case 15:
                            return "转折句";
                        case 16:
                        case 17:
                        case 18:
                            return "时间顺序句";
                        default:
                            return "";
                    }
                case 4:
                    // 第四组：
                    // 1-3（着（持续体））
                    // 4-6（双重否定）
                    // 7-9（条件句（排除））
                    // 10-15（篇章理解能力，共6小问）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "着（持续体）";
                        case 4:
                        case 5:
                        case 6:
                            return "双重否定";
                        case 7:
                        case 8:
                        case 9:
                            return "条件句（排除）";
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            return "篇章理解能力";
                        default:
                            return "";
                    }
                default:
                    return "";
            }
        }
        // 句法表达部分

        else if (format != null && format.equals("SE")) {

            switch (stage) {
                case 1:
                    // 第一组：
                    // 1-3（主谓结构）
                    // 4-6（动宾结构）
                    // 7-9（形容词+名词）
                    // 10-12（主谓宾）
                    // 13-15（否定句）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "主谓结构";
                        case 4:
                        case 5:
                        case 6:
                            return "动宾结构";
                        case 7:
                        case 8:
                        case 9:
                            return "形容词+名词";
                        case 10:
                        case 11:
                        case 12:
                            return "主谓宾";
                        case 13:
                        case 14:
                        case 15:
                            return "否定句";
                        default:
                            return "";
                    }
                case 2:
                    // 第二组：
                    // 1-3（双宾结构）
                    // 4-6（一般疑问句）
                    // 7-9（特殊疑问句）
                    // 10-12（地点/方位）
                    // 13-15（多重修饰）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "双宾结构";
                        case 4:
                        case 5:
                        case 6:
                            return "一般疑问句";
                        case 7:
                        case 8:
                        case 9:
                            return "特殊疑问句";
                        case 10:
                        case 11:
                        case 12:
                            return "地点/方位";
                        case 13:
                        case 14:
                        case 15:
                            return "多重修饰";
                        default:
                            return "";
                    }
                case 3:
                    // 第三组：
                    // 1-3（被动句）
                    // 4-6（了（完成体））
                    // 7-9（副词"都"）
                    // 10-12（语序）
                    // 13-15（时间顺序句）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "被动句";
                        case 4:
                        case 5:
                        case 6:
                            return "了（完成体）";
                        case 7:
                        case 8:
                        case 9:
                            return "副词都";
                        case 10:
                        case 11:
                        case 12:
                            return "语序";
                        case 13:
                        case 14:
                        case 15:
                            return "时间顺序句";
                        default:
                            return "";
                    }
                case 4:
                    // 第四组：
                    // 1-3（因果复句）
                    // 4-6（转折句）
                    // 7-9（正在/在/着（进行体））
                    // 10-12（假设条件句）
                    // 13-15（比较句）
                    // 16（看图讲故事）
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "因果复句";
                        case 4:
                        case 5:
                        case 6:
                            return "转折句";
                        case 7:
                        case 8:
                        case 9:
                            return "正在/在/着（进行体）";
                        case 10:
                        case 11:
                        case 12:
                            return "假设条件句";
                        case 13:
                        case 14:
                        case 15:
                            return "比较句";
                        case 16:
                            return "看图讲故事";
                        default:
                            return "";
                    }
                default:
                    return "";
            }
        }
        else {

            System.out.println("DEBUG: getTestLanguage - format is null or not RG/SE: " + format);
            return "";
        }
    }
    
    private String getManualTestPoint(int group, int questionNumber) {
        // 手动根据组别和题目编号确定测试点，不依赖format变量
        switch (group) {
            case 1:
                switch (questionNumber) {
                    case 1:
                    case 2:
                    case 3:
                        return "主谓结构";
                    case 4:
                    case 5:
                    case 6:
                        return "动宾结构";
                    case 7:
                    case 8:
                    case 9:
                        return "主谓宾";
                    case 10:
                    case 11:
                    case 12:
                        return "否定句";
                    case 13:
                    case 14:
                    case 15:
                        return "一般疑问句";
                    case 16:
                    case 17:
                    case 18:
                        return "特殊疑问句";
                    case 19:
                    case 20:
                    case 21:
                        return "形容词+名词";
                    default:
                        return "";
                }
            case 2:
                switch (questionNumber) {
                    case 1:
                    case 2:
                    case 3:
                        return "多重修饰";
                    case 4:
                    case 5:
                    case 6:
                        return "双宾结构";
                    case 7:
                    case 8:
                    case 9:
                        return "是不是问句";
                    case 10:
                    case 11:
                    case 12:
                        return "地点疑问句";
                    case 13:
                    case 14:
                    case 15:
                        return "副词都";
                    case 16:
                    case 17:
                    case 18:
                        return "语序";
                    default:
                        return "";
                }
            case 3:
                switch (questionNumber) {
                    case 1:
                    case 2:
                    case 3:
                        return "被动句";
                    case 4:
                    case 5:
                    case 6:
                        return "比较句";
                    case 7:
                    case 8:
                    case 9:
                        return "了（完成体）";
                    case 10:
                    case 11:
                    case 12:
                        return "因果复句";
                    case 13:
                    case 14:
                    case 15:
                        return "转折句";
                    case 16:
                    case 17:
                    case 18:
                        return "时间顺序句";
                    default:
                        return "";
                }
            case 4:
                switch (questionNumber) {
                    case 1:
                    case 2:
                    case 3:
                        return "着（持续体）";
                    case 4:
                    case 5:
                    case 6:
                        return "双重否定";
                    case 7:
                    case 8:
                    case 9:
                        return "条件句（排除）";
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        return "篇章理解能力";
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    private void fillQuestionDetailTable(ArrayList<QuestionDetail> questionDetails) {
        TableLayout tableLayout = findViewById(R.id.question_detail_table);
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1); // 保留表头

        if (questionDetails.isEmpty()) {
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
            params.span = 5;
            emptyText.setLayoutParams(params);

            row.addView(emptyText);
            tableLayout.addView(row);
            return;
        }

        // 添加题目数据行
        for (int i = 0; i < questionDetails.size(); i++) {
            final int index = i;
            QuestionDetail detail = questionDetails.get(i);
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? getResources().getColor(android.R.color.white) : 0xF9F9F9);

            // 测试阶段
            TextView stageText = new TextView(this);
            stageText.setText(detail.testStage);
            stageText.setGravity(Gravity.CENTER);
            stageText.setPadding(8, 8, 8, 8);
            stageText.setTextSize(14);
            row.addView(stageText);

            // 题号
            TextView numText = new TextView(this);
            numText.setText(String.valueOf(detail.num));
            numText.setGravity(Gravity.CENTER);
            numText.setPadding(8, 8, 8, 8);
            numText.setTextSize(14);
            row.addView(numText);

            // 例句
            TextView questionText = new TextView(this);
            questionText.setText(detail.question);
            questionText.setGravity(Gravity.CENTER);
            questionText.setPadding(8, 8, 8, 8);
            questionText.setTextSize(14);
            questionText.setSingleLine(false);
            questionText.setMaxWidth(300); // 设置最大宽度，确保文本不会过长
            row.addView(questionText);

            // 测试结果
            TextView resultText = new TextView(this);
            resultText.setText(detail.result ? "正确" : "错误");
            resultText.setTextColor(detail.result ? getResources().getColor(android.R.color.holo_green_dark) : getResources().getColor(android.R.color.holo_red_dark));
            resultText.setGravity(Gravity.CENTER);
            resultText.setPadding(8, 8, 8, 8);
            resultText.setTextSize(14);
            row.addView(resultText);

            // 为句法表达添加录音播放按钮
            if (format.equals("SE") && !detail.audioPath.isEmpty()) {
                Button playButton = new Button(this);
                playButton.setText("播放录音");
                playButton.setTextSize(12);
                playButton.setPadding(4, 4, 4, 4);
                playButton.setOnClickListener(v -> {
                    try {
                        // 直接使用MediaPlayer播放，避免使用AudioPlayer的audioIcons列表
                        android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
                        mediaPlayer.setDataSource(detail.audioPath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        
                        // 播放完成后释放资源
                        mediaPlayer.setOnCompletionListener(mp -> {
                            mp.release();
                        });
                    } catch (Exception e) {
                        Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
                row.addView(playButton);
            }

            // 对应测试语法点
            TextView languageText = new TextView(this);
            languageText.setText(detail.testLanguage);
            languageText.setGravity(Gravity.CENTER);
            languageText.setPadding(8, 8, 8, 8);
            languageText.setTextSize(14);
            row.addView(languageText);

            tableLayout.addView(row);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        }
    }
}
