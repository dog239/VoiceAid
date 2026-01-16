package com.example.CCLEvaluation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import utils.dataManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class childinfoactivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSave;
    private Button buttonDelete;
    private EditText textName;
    private EditText textClass;
    private EditText textNumber;
    private EditText textBirth;
    private EditText textTestTime;
    private EditText textTestPlace;
    private EditText textTester;
    private SimpleDateFormat dateFormat;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_info);
        buttonSave = findViewById(R.id.btn_save);
        buttonDelete = findViewById(R.id.btn_delete);

        textName = findViewById(R.id.et_name);
        textClass = findViewById(R.id.et_class);
        textNumber = findViewById(R.id.et_number);
        textBirth = findViewById(R.id.et_birth);
        textTestTime = findViewById(R.id.et_test_time);
        textTestPlace = findViewById(R.id.et_place);
        textTester = findViewById(R.id.et_tester);

        // 创建一个 SimpleDateFormat 对象，用于格式化日期时间
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // 使用 SimpleDateFormat 格式化日期时间，并将其设置给 TextView
        textTestTime.setText(dateFormat.format(new Date()));

        buttonSave.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save){
            String tNa = textName.getText().toString();
            String tC = textClass.getText().toString();
            String tNu = textNumber.getText().toString();
            String tB = textBirth.getText().toString();
            String tTT = textTestTime.getText().toString();
            String tTP = textTestPlace.getText().toString();
            String tT = textTester.getText().toString();
            if(tNa.isEmpty()||tNu.isEmpty()||tB.isEmpty()||tC.isEmpty()||tTP.isEmpty()||tT.isEmpty()){
                Toast.makeText(this,"还有内容未填写！",Toast.LENGTH_SHORT).show();
            }else{
                try {
                    Intent intent = new Intent(childinfoactivity.this, evmenuactivity.class);
                    JSONObject data = dataManager.getInstance().createData(tNa, tC, tNu, tB, tTT, tTP, tT);
                    String fName = dataManager.getInstance().saveData(tNa + System.currentTimeMillis() + ".json", data);
                    dataManager.getInstance().createIndex(fName,"Index");
                    intent.putExtra("fName", fName);
                    String Uid = getIntent().getStringExtra("Uid");
                    intent.putExtra("Uid", Uid);
                    intent.putExtra("private",true);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this,"信息写入失败！",Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }

        } else if (v.getId()==R.id.btn_delete) {
            textName.setText("");
            textClass.setText("");
            textNumber.setText("");
            textBirth.setText("");
            textTestTime.setText("");
            textTestPlace.setText("");
            textTester.setText("");
            Toast.makeText(this,"信息已清空！",Toast.LENGTH_SHORT).show();
            textTestTime.setText(dateFormat.format(new Date()));
        }

    }
}
