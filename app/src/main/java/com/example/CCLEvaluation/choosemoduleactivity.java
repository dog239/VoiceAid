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

import audiotest.Audiocheckresult;
import login.Loginactivity;
import login.registeractivity;
import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.Netinteractutils;
import utils.dialogUtils;

public class choosemoduleactivity extends AppCompatActivity {


    private Button Sure1;
    private Button Back1;
    private CheckBox[] checkBoxes = new CheckBox[4];
    private Boolean[] chooseWhat = new Boolean[4];
    private String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosemodule);
        Sure1 = findViewById(R.id.btn_sure1);
        Back1 = findViewById(R.id.btn_back1);
        checkBoxes[0] = findViewById(R.id.word);
        checkBoxes[1] = findViewById(R.id.pronounciation);
        checkBoxes[2] = findViewById(R.id.grammar);
        checkBoxes[3] = findViewById(R.id.narrator);
        checkBoxes[0].setVisibility(View.INVISIBLE);
        checkBoxes[1].setVisibility(View.INVISIBLE);
        checkBoxes[2].setVisibility(View.INVISIBLE);
        checkBoxes[3].setVisibility(View.INVISIBLE);

        Uid = getIntent().getStringExtra("Uid");
//        Toast.makeText(choosemoduleactivity.this,Uid,Toast.LENGTH_SHORT).show();
        Netinteractutils.getInstance(choosemoduleactivity.this).setModuleCallback(new Netinteractutils.ModuleCallback() {
            @Override
            public void onModuleResult(String module) throws JSONException {
                JSONObject jsonObject = new JSONObject(module);
                String E = jsonObject.getString("E");
                String RE = jsonObject.getString("RE");
                String S = jsonObject.getString("S");
                String NWR = jsonObject.getString("NWR");
                String A = jsonObject.getString("A");
                String RG = jsonObject.getString("RG");
                String PN = jsonObject.getString("PN");
                String PST = jsonObject.getString("PST");

                if(E.equals("1") && RE.equals("1") && S.equals("1") && NWR.equals("1")){
                    chooseWhat[0] = true;
                    checkBoxes[0].setChecked(true);
                    checkBoxes[0].setVisibility(View.VISIBLE);
                }else{
                    chooseWhat[0] = false;
                    checkBoxes[0].setChecked(false);
                    checkBoxes[0].setVisibility(View.VISIBLE);
                }
                if(A.equals("1")){
                    chooseWhat[1] = true;
                    checkBoxes[1].setChecked(true);
                    checkBoxes[1].setVisibility(View.VISIBLE);
                }else{
                    chooseWhat[1] = false;
                    checkBoxes[1].setChecked(false);
                    checkBoxes[1].setVisibility(View.VISIBLE);
                }
                if(RG.equals("1")){
                    chooseWhat[2] = true;
                    checkBoxes[2].setChecked(true);
                    checkBoxes[2].setVisibility(View.VISIBLE);
                }else{
                    chooseWhat[2] = false;
                    checkBoxes[2].setChecked(false);
                    checkBoxes[2].setVisibility(View.VISIBLE);
                }
                if(PN.equals("1") && PST.equals("1")){
                    chooseWhat[3] = true;
                    checkBoxes[3].setChecked(true);
                    checkBoxes[3].setVisibility(View.VISIBLE);
                }else{
                    chooseWhat[3] = false;
                    checkBoxes[3].setChecked(false);
                    checkBoxes[3].setVisibility(View.VISIBLE);
                }


            }
        });

        Netinteractutils.getInstance(choosemoduleactivity.this).getModule(Uid);


        checkBoxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
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
                // 将RadioButton的选中状态存储到SharedPreferences中
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
                // 将RadioButton的选中状态存储到SharedPreferences中
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
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    chooseWhat[3] = true;
                }else{
                    chooseWhat[3] = false;
                }
            }
        });


        Sure1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Netinteractutils.getInstance(choosemoduleactivity.this).updateModule(Uid,jsonObject);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Intent intent;
                intent = new Intent(choosemoduleactivity.this, mainactivity.class);
                intent.putExtra("isTest", false);
                intent.putExtra("Uid", Uid);
                startActivity(intent);
                finish();
            }
        });
        Back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(choosemoduleactivity.this, mainactivity.class);
                intent.putExtra("Uid", Uid);
                intent.putExtra("isTest", false);
                startActivity(intent);
                finish();
            }
        });
    }
}