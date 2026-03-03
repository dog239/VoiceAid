package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.dataManager;

public class SyntaxExpressionGroupSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGroup1;
    private Button btnGroup2;
    private Button btnGroup3;
    private Button btnGroup4;
    private Button btnEvaluationReport;
    private String fName;
    private boolean hasCompletedAnyGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syntax_expression_group_select);

        btnGroup1 = findViewById(R.id.btn_group1);
        btnGroup2 = findViewById(R.id.btn_group2);
        btnGroup3 = findViewById(R.id.btn_group3);
        btnGroup4 = findViewById(R.id.btn_group4);
        btnEvaluationReport = findViewById(R.id.btn_evaluation_report);

        btnGroup1.setOnClickListener(this);
        btnGroup2.setOnClickListener(this);
        btnGroup3.setOnClickListener(this);
        btnGroup4.setOnClickListener(this);
        btnEvaluationReport.setOnClickListener(this);

        fName = getIntent().getStringExtra("fName");

        // 检查是否已经有完成的组别
        checkCompletedGroups();
    }

    private void checkCompletedGroups() {
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            if (data != null && data.has("evaluations")) {
                JSONObject evaluations = data.getJSONObject("evaluations");
                for (int i = 1; i <= 4; i++) {
                    JSONArray seArray = evaluations.optJSONArray("SE" + i);
                    if (seArray != null && seArray.length() > 0) {
                        // 检查该组是否所有题目都已完成
                        boolean groupCompleted = true;
                        for (int j = 0; j < seArray.length(); j++) {
                            JSONObject item = seArray.getJSONObject(j);
                            if (!item.has("time") || item.isNull("time")) {
                                groupCompleted = false;
                                break;
                            }
                        }
                        if (groupCompleted) {
                            hasCompletedAnyGroup = true;
                            // 禁用其他组的按钮
                            disableOtherGroupButtons(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableOtherGroupButtons(int completedGroup) {
        // 不再禁用按钮，而是在onClick方法中处理
        // 这样用户点击其他组时会弹出提示，而不是没有反应
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回该页面时，重新检查是否有完成的组别
        hasCompletedAnyGroup = false;
        checkCompletedGroups();
    }

    @Override
    public void onClick(View v) {
        int groupNumber = 0;
        if (v.getId() == R.id.btn_group1) {
            groupNumber = 1;
        } else if (v.getId() == R.id.btn_group2) {
            groupNumber = 2;
        } else if (v.getId() == R.id.btn_group3) {
            groupNumber = 3;
        } else if (v.getId() == R.id.btn_group4) {
            groupNumber = 4;
        }

        if (groupNumber > 0) {
            if (hasCompletedAnyGroup) {
                // 如果已经完成了任何一组，弹出提示
                Toast.makeText(this, "您已经完成了一组测试，不能再选择其他组别进行测试", Toast.LENGTH_SHORT).show();
            } else {
                // 启动测试活动，传递组别信息
                Intent intent = new Intent(this, testactivity.class);
                intent.putExtra("fName", fName);
                intent.putExtra("format", "SE");
                intent.putExtra("groupNumber", groupNumber);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.btn_evaluation_report) {
            // 启动句法表达评估报告
            Intent intent = new Intent(this, SyntaxResultActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "SE"); // 显示句法表达结果
            startActivity(intent);
        }
    }
}
