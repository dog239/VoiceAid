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

public class SyntaxComprehensionGroupSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGroup1;
    private Button btnGroup2;
    private Button btnGroup3;
    private Button btnGroup4;
    private String fName;
    private boolean hasCompletedAnyGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syntax_comprehension_group_select);

        btnGroup1 = findViewById(R.id.btn_group1);
        btnGroup2 = findViewById(R.id.btn_group2);
        btnGroup3 = findViewById(R.id.btn_group3);
        btnGroup4 = findViewById(R.id.btn_group4);

        btnGroup1.setOnClickListener(this);
        btnGroup2.setOnClickListener(this);
        btnGroup3.setOnClickListener(this);
        btnGroup4.setOnClickListener(this);

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
                    JSONArray rgArray = evaluations.optJSONArray("RG" + i);
                    if (rgArray != null && rgArray.length() > 0) {
                        // 检查该组是否有完成的题目
                        boolean groupCompleted = false;
                        for (int j = 0; j < rgArray.length(); j++) {
                            JSONObject item = rgArray.getJSONObject(j);
                            if (item.has("time") && !item.isNull("time")) {
                                groupCompleted = true;
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
        switch (completedGroup) {
            case 1:
                btnGroup2.setEnabled(false);
                btnGroup3.setEnabled(false);
                btnGroup4.setEnabled(false);
                break;
            case 2:
                btnGroup1.setEnabled(false);
                btnGroup3.setEnabled(false);
                btnGroup4.setEnabled(false);
                break;
            case 3:
                btnGroup1.setEnabled(false);
                btnGroup2.setEnabled(false);
                btnGroup4.setEnabled(false);
                break;
            case 4:
                btnGroup1.setEnabled(false);
                btnGroup2.setEnabled(false);
                btnGroup3.setEnabled(false);
                break;
        }
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
            // 启动测试活动，传递组别信息
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "RG");
            intent.putExtra("groupNumber", groupNumber);
            startActivity(intent);
        }
    }
}
