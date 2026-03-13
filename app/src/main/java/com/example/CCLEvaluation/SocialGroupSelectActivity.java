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

public class SocialGroupSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private Button group1Button, group2Button, group3Button, group4Button, group5Button, group6Button;
    private String fName;
    private String uid;
    private String childID;
    private boolean hasCompletedAnyGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_group_select);

        // 初始化UI组件
        group1Button = findViewById(R.id.btn_group1);
        group2Button = findViewById(R.id.btn_group2);
        group3Button = findViewById(R.id.btn_group3);
        group4Button = findViewById(R.id.btn_group4);
        group5Button = findViewById(R.id.btn_group5);
        group6Button = findViewById(R.id.btn_group6);

        // 设置点击监听器
        group1Button.setOnClickListener(this);
        group2Button.setOnClickListener(this);
        group3Button.setOnClickListener(this);
        group4Button.setOnClickListener(this);
        group5Button.setOnClickListener(this);
        group6Button.setOnClickListener(this);

        // 获取传递过来的参数
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childID = getIntent().getStringExtra("childID");
        int currentGroup = getIntent().getIntExtra("currentGroup", 0);

        // 检查是否已经有完成的组别
        checkCompletedGroups();

        // 如果currentGroup参数存在，说明是从testactivity跳转过来的，分数不够12分，需要限制只能选择上一组的题目
        if (currentGroup > 1) {
            // 禁用除了上一组之外的所有按钮
            disableAllButtons();
            // 启用上一组的按钮
            int previousGroup = currentGroup - 1;
            switch (previousGroup) {
                case 1:
                    group1Button.setEnabled(true);
                    break;
                case 2:
                    group2Button.setEnabled(true);
                    break;
                case 3:
                    group3Button.setEnabled(true);
                    break;
                case 4:
                    group4Button.setEnabled(true);
                    break;
                case 5:
                    group5Button.setEnabled(true);
                    break;
                case 6:
                    group6Button.setEnabled(true);
                    break;
            }
            // 弹出提示，说明只能选择上一组的题目
            Toast.makeText(this, "只能选择第" + previousGroup + "组的题目", Toast.LENGTH_LONG).show();
        }
    }

    private void checkCompletedGroups() {
        try {
            if (fName == null || fName.isEmpty()) {
                return;
            }
            JSONObject data = dataManager.getInstance().loadData(fName);
            if (data != null && data.has("evaluations")) {
                JSONObject evaluations = data.getJSONObject("evaluations");
                JSONArray socialArray = evaluations.optJSONArray("SOCIAL");
                if (socialArray != null && socialArray.length() > 0) {
                    // 检查是否有完成的题目
                    for (int i = 0; i < socialArray.length(); i++) {
                        JSONObject item = socialArray.getJSONObject(i);
                        if (item.has("score") && !item.isNull("score")) {
                            hasCompletedAnyGroup = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableAllButtons() {
        group1Button.setEnabled(false);
        group2Button.setEnabled(false);
        group3Button.setEnabled(false);
        group4Button.setEnabled(false);
        group5Button.setEnabled(false);
        group6Button.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int groupNumber = 0;

        // 根据点击的按钮确定分组编号
        if (v.getId() == R.id.btn_group1) {
            groupNumber = 1;
        } else if (v.getId() == R.id.btn_group2) {
            groupNumber = 2;
        } else if (v.getId() == R.id.btn_group3) {
            groupNumber = 3;
        } else if (v.getId() == R.id.btn_group4) {
            groupNumber = 4;
        } else if (v.getId() == R.id.btn_group5) {
            groupNumber = 5;
        } else if (v.getId() == R.id.btn_group6) {
            groupNumber = 6;
        }

        if (groupNumber > 0) {
            // 启动测试活动，并传递分组信息
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "SOCIAL");
            intent.putExtra("groupNumber", groupNumber);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childID);
            startActivity(intent);
            // 结束当前活动，避免多个活动实例堆积
            finish();
        }
    }
}
