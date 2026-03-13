package com.example.CCLEvaluation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import history.allhistorylist;
import history.historylist;
import history.privatehistorylist;
import login.LoginActivity;
import utils.NetInteractUtils;
import utils.dialogUtils;
import utils.dirpath;
import utils.Ifileinter;
import utils.permissionutils;
import utils.sdcard;
import audiotest.Audiocheck;
import utils.testcontext;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout newTest;
    private LinearLayout oldTest1;
    private LinearLayout oldTest2;
    private LinearLayout voiceCheck;
    private LinearLayout logout;
    private LinearLayout allTests;
    private LinearLayout changeModule;
    private LinearLayout delete;
    private TextView newTestno;
    private TextView oldTest1no;
    private TextView oldTest2no;
    private TextView voiceCheckno;
    private TextView logoutno;
    private TextView allTestsno;
    private TextView changeModuleNo;
    private TextView deleteno;
    private String Uid;
    private Boolean isTest = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newTest = findViewById(R.id.btn_new1);
        oldTest1 = findViewById(R.id.btn_old1);
        oldTest2 = findViewById(R.id.btn_old2);
        voiceCheck = findViewById(R.id.btn_record);
        logout = findViewById(R.id.btn_logout);
        allTests = findViewById(R.id.btn_all);
        changeModule = findViewById(R.id.btn_changeModule);
        delete = findViewById(R.id.btn_delete);

        newTestno = findViewById(R.id.new1_no);
        oldTest1no = findViewById(R.id.old1_no);
        oldTest2no = findViewById(R.id.old2_no);
        voiceCheckno = findViewById(R.id.record_no);
        logoutno = findViewById(R.id.logout_no);
        allTestsno = findViewById(R.id.all_no);
        changeModuleNo = findViewById(R.id.changeModule_no);
        deleteno = findViewById(R.id.delete_no);

        newTest.setOnClickListener(this);
        oldTest1.setOnClickListener(this);
        oldTest2.setOnClickListener(this);
        voiceCheck.setOnClickListener(this);
        logout.setOnClickListener(this);
        allTests.setOnClickListener(this);
        changeModule.setOnClickListener(this);
        delete.setOnClickListener(this);

        Uid = getIntent().getStringExtra("Uid");
        isTest = getIntent().getBooleanExtra("isTest",true);
        if(isTest){
            newTestno.setText("1");
            oldTest1no.setText("2");
            oldTest2no.setText("3");
            allTestsno.setText("4");
            voiceCheck.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            logout.setVisibility(View.GONE);
            changeModule.setVisibility(View.GONE);
        }else{
            newTest.setVisibility(View.GONE);
            oldTest1.setVisibility(View.GONE);
            oldTest2.setVisibility(View.GONE);
            allTests.setVisibility(View.GONE);
            voiceCheckno.setText("1");
            changeModuleNo.setText("2");
            logoutno.setText("3");
            deleteno.setText("4");
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_new1) {
            Intent intent = new Intent(MainActivity.this, childinfoactivity.class);
            intent.putExtra("Uid", Uid);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.btn_old1) {
            Intent intent = new Intent(MainActivity.this, historylist.class);
            intent.putExtra("Uid", Uid);
            intent.putExtra("old1",true);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.btn_old2) {
            Intent intent = new Intent(MainActivity.this, privatehistorylist.class);
            intent.putExtra("Uid", Uid);
            startActivity(intent);
            finish();
        }else if (v.getId() == R.id.btn_all) {
            Intent intent = new Intent(MainActivity.this, allhistorylist.class);
            intent.putExtra("Uid", Uid);
            startActivity(intent);
            finish();
        }else if (v.getId() == R.id.btn_record) {
            Intent intent = new Intent(MainActivity.this, Audiocheck.class);
            intent.putExtra("Uid", Uid);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.btn_logout) {
            dialogUtils.showDialog(this, "提示信息", "确认要退出登录吗？",
                    "确认", () -> {
                        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("Uid", null);
                        editor.putBoolean("isLoggedIn", false);
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }, "取消", null);
        } else if (v.getId() == R.id.btn_changeModule){
            Intent intent = new Intent(MainActivity.this, choosemoduleactivity.class);
            intent.putExtra("Uid", Uid);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.btn_delete){
            Intent intent = new Intent(MainActivity.this, deleteactivity.class);
            intent.putExtra("Uid", Uid);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }



}
