package com.example.CCLEvaluation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.mainactivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import login.Loginactivity;
import login.registeractivity;
import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.Netinteractutils;
import utils.dialogUtils;

public class deleteactivity extends AppCompatActivity {

    private EditText tel_or_email;
    private EditText capcha;
    private Button Get_capcha;
    private Button Sure;
    private Button Back;
    private String Uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        tel_or_email = findViewById(R.id.et_tel_or_email);
        capcha = findViewById(R.id.et_capcha);
        Get_capcha = findViewById(R.id.btn_getcapcha);
        Sure = findViewById(R.id.btn_sure);
        Back = findViewById(R.id.btn_back);
        Uid = getIntent().getStringExtra("Uid");


        Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bind = tel_or_email.getText().toString();
                String cap = capcha.getText().toString();
                if(bind.isEmpty()||cap.isEmpty()){
                    Toast.makeText(deleteactivity.this,"还有内容未填写！",Toast.LENGTH_SHORT).show();
                }else{
                    dialogUtils.showDialog(deleteactivity.this, "提示信息", "注销后，该账户及其测评都将被删除，是否注销？",
                            "确认", () -> {
                                SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("Uid", null);
                                editor.putBoolean("isLoggedIn", false);
                                editor.apply();
                                Netinteractutils.getInstance(deleteactivity.this).logoffUser(Uid,bind,cap);
                                Intent intent = new Intent(deleteactivity.this, Loginactivity.class);
                                startActivity(intent);
                                finish();
                            }, "取消", null);


                    //Toast.makeText(RegisterActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(deleteactivity.this, mainactivity.class);
                intent.putExtra("Uid", Uid);
                intent.putExtra("isTest", false);
                startActivity(intent);
                finish();
            }
        });


        Get_capcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // 在每个间隔时间内更新 TextView 显示的剩余时间
                        long seconds = millisUntilFinished / 1000;
                        Get_capcha.setText(String.valueOf(seconds) + "s后重试");
                    }

                    @Override
                    public void onFinish() {
                        // 倒计时结束后的操作
                        Get_capcha.setEnabled(true);
                        Get_capcha.setTextColor(Color.BLACK);
                        Get_capcha.setText("获取验证码");

                    }
                };
                String bind = tel_or_email.getText().toString();
                if(bind.isEmpty()){
                    Toast.makeText(deleteactivity.this,"未填写手机号/邮箱！",Toast.LENGTH_SHORT).show();
                }else{
                    Netinteractutils.getInstance(deleteactivity.this).getCaptcha("3",bind);
                    Get_capcha.setEnabled(false);
                    countDownTimer.start();
                }
            }
        });
    }
}