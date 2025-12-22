package login;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.CCLEvaluation.mainactivity;
import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.startActivity;

import utils.ImageUrls;
import utils.Netinteractutils;

public class Loginactivity extends AppCompatActivity{
    //声明控件
    private Button mBtnLogin;
    private Button mBtnRegister;
    private Button mBtnCaptcha;
    private EditText mEtet1;
    private Boolean iscaptcha;
    private EditText mEtet2;
    private EditText mEtet3;
    private TextView changeCode;
    private TextView captchaLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //找到控件
        mBtnLogin = findViewById(R.id.btn_loginrr);
        mBtnRegister = findViewById(R.id.btn_regs);
        mEtet1 = findViewById(R.id.et_1);
        mEtet2 = findViewById(R.id.et_2);
        mEtet3 = findViewById(R.id.et_3);
        mBtnCaptcha = findViewById(R.id.btn_getcapcha);
        changeCode = findViewById(R.id.changeCode);
        captchaLogin = findViewById(R.id.capchaLogin);
        iscaptcha = false;
        //页面初始化
        mEtet1.setHint("用户名");
       // mEtet3.setHint("密码");
        mEtet2.setVisibility(View.GONE);
        mBtnCaptcha.setVisibility(View.GONE);
        captchaLogin.setText(getString(R.string.captchaLogin));
        //重写验证码登陆回调函数
        Netinteractutils.getInstance(Loginactivity.this).setLoginCallback(new Netinteractutils.LoginCallback() {
            @Override
            public void onLoginResult(String uid, String username) {
                Intent intent = new Intent(Loginactivity.this, startActivity.class);
                SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Uid", uid);
                editor.putBoolean("isLoggedIn", true);
                editor.apply();
                startActivity(intent);
                Toast.makeText(Loginactivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                finish();
            }
        });



        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        String Uid = preferences.getString("Uid",null);
        if (isLoggedIn && Uid != null) {
            // Directly go to Main Activity
            Intent intent = new Intent(Loginactivity.this, startActivity.class);
            startActivity(intent);
            finish();
        }
        //匹配对应的用户名和密码才能登录
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //需要获取输入的用户名和密码
                String username;
                String password;
                if(!iscaptcha){
                    username = mEtet1.getText().toString();
                    password = mEtet3.getText().toString();
                }else{
                    username = mEtet1.getText().toString();
                    password = mEtet2.getText().toString();
                }

                if(username.equals("")||password.equals("")){
                    Toast.makeText(Loginactivity.this,"还有信息未填写",Toast.LENGTH_SHORT).show();
                }else{
                    if(!iscaptcha){
                        Netinteractutils.getInstance(Loginactivity.this).loginWithPassword(username,password);
                    }else{
                        Netinteractutils.getInstance(Loginactivity.this).loginWithCaptcha(username,password);
                    }
                }

            }
        });
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(Loginactivity.this, registeractivity.class);
                intent.putExtra("ischangeCode",false);
                startActivity(intent);
            }
        });


        changeCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(Loginactivity.this, registeractivity.class);
                intent.putExtra("ischangeCode",true);
                startActivity(intent);
            }
        });

        captchaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!iscaptcha){
                    iscaptcha = true;
                    mBtnCaptcha.setVisibility(View.VISIBLE);
                    mEtet1.setHint("手机号/邮箱");
                    mEtet2.setVisibility(View.VISIBLE);
                    //mEtet2.setHint("验证码");
                    mEtet3.setVisibility(View.GONE);
                    captchaLogin.setText(getString(R.string.codeLogin));

                }else{
                    iscaptcha = false;
                    mEtet1.setHint("用户名");
                    mEtet2.setVisibility(View.GONE);
                    mEtet3.setVisibility(View.VISIBLE);
                   // mEtet3.setHint("密码");
                    mBtnCaptcha.setVisibility(View.GONE);
                    captchaLogin.setText(getString(R.string.captchaLogin));
                }

            }
        });
        mBtnCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // 在每个间隔时间内更新 TextView 显示的剩余时间
                        long seconds = millisUntilFinished / 1000;
                        mBtnCaptcha.setText(String.valueOf(seconds) + "s后重试");
                    }

                    @Override
                    public void onFinish() {
                        // 倒计时结束后的操作
                        mBtnCaptcha.setEnabled(true);
                        mBtnCaptcha.setTextColor(Color.BLACK);
                        mBtnCaptcha.setText("获取验证码");

                    }
                };
                String bind = mEtet1.getText().toString();
                if(bind.isEmpty()){
                    Toast.makeText(Loginactivity.this,"未填写手机号/邮箱！",Toast.LENGTH_SHORT).show();
                }else{
                    Netinteractutils.getInstance(Loginactivity.this).getCaptcha("0",bind);
                    mBtnCaptcha.setEnabled(false);
                    countDownTimer.start();
                }
            }
        });








    }


}

    
