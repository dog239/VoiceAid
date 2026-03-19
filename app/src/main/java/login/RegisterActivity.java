package login;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.choosemoduleactivity;

import org.json.JSONException;
import org.json.JSONObject;

import utils.NetInteractUtils;

public class RegisterActivity extends AppCompatActivity{
    private TextView tv1;
    private EditText tel_or_email;
    private EditText capcha;
    private EditText regName;
    private EditText regCode;
    private EditText regCheck;
    private Button Get_capcha;
    private Button Sure;
    private Button Back;

    private Boolean isChangeCode = false;
    private LinearLayout chooseTable;
    private CheckBox[] checkBoxes = new CheckBox[4];
    private Boolean[] chooseWhat = new Boolean[4];
    private String Uid;

    // 添加变量保存注册信息，用于在登录成功后创建模块
    private String savedBind;
    private String savedCap;
    private String savedUsername;
    private String savedPassword;
    private String savedCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        tv1 = findViewById(R.id.tv_1);
        tel_or_email = findViewById(R.id.et_tel_or_email);
        capcha = findViewById(R.id.et_capcha);
        regName = findViewById(R.id.et_regname);
        regCode = findViewById(R.id.et_regcode);
        regCheck = findViewById(R.id.et_regcheck);
        Get_capcha = findViewById(R.id.btn_getcapcha);
        Sure = findViewById(R.id.btn_sure);
        chooseTable = findViewById(R.id.chooseTable);
        checkBoxes[0] = findViewById(R.id.word);
        checkBoxes[1] = findViewById(R.id.pronounciation);
        checkBoxes[2] = findViewById(R.id.grammar);
        checkBoxes[3] = findViewById(R.id.narrator);
        Back = findViewById(R.id.btn_back);
        for(int i=0;i<4;++i){
            chooseWhat[i] = false;
        }

        NetInteractUtils.getInstance(RegisterActivity.this).setLoginCallback(new NetInteractUtils.LoginCallback() {
            @Override
            public void onLoginResult(String uid, String username) {
                Uid = uid;

                // 只有在注册模式下且成功获取到UID时才创建模块
                if (!isChangeCode && uid != null && !uid.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        if(chooseWhat[0]){
                            jsonObject.put("E",1);
                            jsonObject.put("RE",1);
                            jsonObject.put("S",1);
                            jsonObject.put("NWR",1);
                        }else{
                            jsonObject.put("E",0);
                            jsonObject.put("RE",0);
                            jsonObject.put("S",0);
                            jsonObject.put("NWR",0);
                        }
                        if(chooseWhat[1]){
                            jsonObject.put("A",1);
                        }else{
                            jsonObject.put("A",0);
                        }
                        if(chooseWhat[2]){
                            jsonObject.put("RG",1);
                        }else{
                            jsonObject.put("RG",0);
                        }
                        if(chooseWhat[3]){
                            jsonObject.put("PST",1);
                            jsonObject.put("PN",1);
                        }else{
                            jsonObject.put("PST",0);
                            jsonObject.put("PN",0);
                        }
                        Log.d("10086",jsonObject.toString());
                        NetInteractUtils.getInstance(RegisterActivity.this).createModule(Uid,jsonObject);

                        // 模块创建完成后跳转到登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(RegisterActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        NetInteractUtils.getInstance(RegisterActivity.this).setRegisterCallback(() -> {
            if (savedUsername != null && savedPassword != null) {
                NetInteractUtils.getInstance(RegisterActivity.this)
                        .loginWithPassword(savedUsername, savedPassword);
            }
        });

        isChangeCode = getIntent().getBooleanExtra("ischangeCode", false);

        if(isChangeCode){
            tv1.setText("修改密码");
            regName.setVisibility(View.GONE);
            regCode.setHint("修改密码");
            regCheck.setHint("再次确认修改密码");
            chooseTable.setVisibility(View.GONE);
        }else{
            tv1.setText("平台注册");
            regCode.setHint("密码");
            regCheck.setHint("再次确认密码");
            regName.setVisibility(View.VISIBLE);
            chooseTable.setVisibility(View.VISIBLE);
        }

        Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bind = tel_or_email.getText().toString();
                String cap = capcha.getText().toString();
                String username = "";
                if(!isChangeCode){
                    username = regName.getText().toString();
                }
                String password = regCode.getText().toString();
                String check = regCheck.getText().toString();

                if(!isChangeCode){
                    if(bind.isEmpty()||cap.isEmpty()||username.isEmpty()||password.isEmpty()||check.isEmpty()){
                        Toast.makeText(RegisterActivity.this,"还有内容未填写！",Toast.LENGTH_SHORT).show();
                    }else if(password.equals(check)){
                        // 保存注册信息
                        savedBind = bind;
                        savedCap = cap;
                        savedUsername = username;
                        savedPassword = password;
                        savedCheck = check;

                        // 先注册，注册成功后再登录
                        NetInteractUtils.getInstance(RegisterActivity.this).register(bind,cap,username,password,check);

                        // 注意：这里不再立即跳转，等待登录成功回调
                    }else{
                        Toast.makeText(RegisterActivity.this,"密码确认有误！",Toast.LENGTH_SHORT).show();
                        regCode.setText("");
                        regCheck.setText("");
                    }
                }else{
                    if(bind.isEmpty()||cap.isEmpty()||password.isEmpty()||check.isEmpty()){
                        Toast.makeText(RegisterActivity.this,"还有内容未填写！",Toast.LENGTH_SHORT).show();
                    }else if(password.equals(check)){
                        NetInteractUtils.getInstance(RegisterActivity.this).changePassword(bind,cap,password,check);
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(RegisterActivity.this,"密码修改成功！",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this,"密码确认有误！",Toast.LENGTH_SHORT).show();
                        regCode.setText("");
                        regCheck.setText("");
                    }
                }
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
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
                    Toast.makeText(RegisterActivity.this,"未填写手机号/邮箱！",Toast.LENGTH_SHORT).show();
                }else{
                    // 添加本地调试模式
                    boolean useLocalDebug = false;
                    if (useLocalDebug) {
                        // 本地调试模式，模拟验证码发送成功
                        Toast.makeText(RegisterActivity.this,"验证码已发送（调试模式）",Toast.LENGTH_SHORT).show();
                        Get_capcha.setEnabled(false);
                        countDownTimer.start();
                    } else {
                        // 网络请求模式，连接服务器发送验证码
                        NetInteractUtils.getInstance(RegisterActivity.this).getCaptcha("1",bind);
                        Get_capcha.setEnabled(false);
                        countDownTimer.start();
                    }
                }
            }
        });

        // 复选框监听器保持不变
        checkBoxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    chooseWhat[0] = true;
                }else{
                    chooseWhat[0] = false;
                }
            }
        });

        checkBoxes[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    chooseWhat[1] = true;
                }else{
                    chooseWhat[1] = false;
                }
            }
        });

        checkBoxes[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    chooseWhat[2] = true;
                }else{
                    chooseWhat[2] = false;
                }
            }
        });

        checkBoxes[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    chooseWhat[3] = true;
                }else{
                    chooseWhat[3] = false;
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}
