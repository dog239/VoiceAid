package login;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.startActivity;

import utils.IflytekEvaluator;
import utils.net.NetService;
import utils.net.NetServiceProvider;

public class LoginActivity extends AppCompatActivity{
    //声明控件
    private Button mBtnLogin;
    private Button mBtnRegister;
    private Button mBtnCaptcha;
    private EditText mEtUsername;
    private boolean isCaptcha;
    private EditText mEtCaptcha;
    private EditText mEtPassword;
    private TextView changeCode;
    private TextView captchaLogin;
    private NetService netService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        IflytekEvaluator.init(this, getString(R.string.app_id));
        //找到控件
        mBtnLogin = findViewById(R.id.btn_loginrr);
        mBtnRegister = findViewById(R.id.btn_regs);
        mEtUsername = findViewById(R.id.et_1);
        mEtCaptcha = findViewById(R.id.et_2);
        mEtPassword = findViewById(R.id.et_3);
        mBtnCaptcha = findViewById(R.id.btn_getcapcha);
        changeCode = findViewById(R.id.changeCode);
        captchaLogin = findViewById(R.id.capchaLogin);
        isCaptcha = false;
        netService = NetServiceProvider.get(this);
        //页面初始化
        mEtUsername.setHint("用户名");
        mEtCaptcha.setVisibility(View.GONE);
        mBtnCaptcha.setVisibility(View.GONE);
        captchaLogin.setText(getString(R.string.captchaLogin));
        //重写验证码登陆回调函数
        netService.setLoginCallback((uid, username) -> {
            Intent intent = new Intent(LoginActivity.this, startActivity.class);
            SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Uid", uid);
            editor.putString("Username", username);
            editor.putBoolean("isLoggedIn", true);
            editor.apply();
            startActivity(intent);
            Toast.makeText(LoginActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
            finish();
        });

        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        String Uid = preferences.getString("Uid",null);
        if (isLoggedIn && Uid != null) {
            // Directly go to Main Activity
            Intent intent = new Intent(LoginActivity.this, startActivity.class);
            startActivity(intent);
            finish();
        }
        //匹配对应的用户名和密码才能登录
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String username;
                String password;
                if(!isCaptcha){
                    username = mEtUsername.getText().toString();
                    password = mEtPassword.getText().toString();
                }else{
                    username = mEtUsername.getText().toString();
                    password = mEtCaptcha.getText().toString();
                }

                if(username.equals("")||password.equals("")){
                    Toast.makeText(LoginActivity.this,"还有信息未填写",Toast.LENGTH_SHORT).show();
                }else{
                    if(!isCaptcha){
                        netService.loginWithPassword(username,password);
                    }else{
                        netService.loginWithCaptcha(username,password);
                    }
                }
            }
        });
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("ischangeCode",false);
                startActivity(intent);
            }
        });

        changeCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("ischangeCode",true);
                startActivity(intent);
            }
        });

        captchaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isCaptcha){
                    isCaptcha = true;
                    mBtnCaptcha.setVisibility(View.VISIBLE);
                    mEtUsername.setHint("手机号/邮箱");
                    mEtCaptcha.setVisibility(View.VISIBLE);
                    mEtPassword.setVisibility(View.GONE);
                    captchaLogin.setText(getString(R.string.codeLogin));
                }else{
                    isCaptcha = false;
                    mEtUsername.setHint("用户名");
                    mEtCaptcha.setVisibility(View.GONE);
                    mEtPassword.setVisibility(View.VISIBLE);
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
                        long seconds = millisUntilFinished / 1000;
                        mBtnCaptcha.setText(String.valueOf(seconds) + "s后重试");
                    }
                    @Override
                    public void onFinish() {
                        mBtnCaptcha.setEnabled(true);
                        mBtnCaptcha.setTextColor(Color.BLACK);
                        mBtnCaptcha.setText("获取验证码");
                    }
                };
                String bind = mEtUsername.getText().toString();
                if(bind.isEmpty()){
                    Toast.makeText(LoginActivity.this,"未填写手机号/邮箱！",Toast.LENGTH_SHORT).show();
                }else{
                    netService.getCaptcha("0",bind);
                    mBtnCaptcha.setEnabled(false);
                    countDownTimer.start();
                }
            }
        });
    }
}
