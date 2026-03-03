package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SocialGroupSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private Button group1Button, group2Button, group3Button, group4Button, group5Button, group6Button;
    private String fName;
    private String uid;
    private String childID;

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
