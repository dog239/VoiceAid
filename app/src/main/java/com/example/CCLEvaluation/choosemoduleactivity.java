package com.example.CCLEvaluation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import utils.net.NetService;
import utils.net.NetServiceProvider;

public class choosemoduleactivity extends AppCompatActivity {


    private Button Sure1;
    private Button Back1;
    private CheckBox[] checkBoxes = new CheckBox[6];
    private Boolean[] chooseWhat = new Boolean[6];
    private String Uid;
    private NetService netService;

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
        checkBoxes[4] = findViewById(R.id.prelinguistic);
        checkBoxes[5] = findViewById(R.id.social);
        checkBoxes[0].setVisibility(View.INVISIBLE);
        checkBoxes[1].setVisibility(View.INVISIBLE);
        checkBoxes[2].setVisibility(View.INVISIBLE);
        checkBoxes[3].setVisibility(View.INVISIBLE);
        checkBoxes[4].setVisibility(View.INVISIBLE);
        checkBoxes[5].setVisibility(View.INVISIBLE);

        Uid = getIntent().getStringExtra("Uid");
        netService = NetServiceProvider.get(this);

        /**
         * 字段约定如下
         * A 构音
         * PL 前语言
         * E 词汇
         * SE 句法表达
         * RG 句法理解
         * SOCIAL 社交
         */
        netService.setModuleCallback(module -> {
            JSONObject jsonObject = new JSONObject(module);
            String E = jsonObject.optString("E", "0");
            String A = jsonObject.optString("A", "0");
            String SE = jsonObject.optString("SE", "0");
            String RG = jsonObject.optString("RG", "0");
            String PL = jsonObject.optString("PL", "0");
            String SOCIAL = jsonObject.optString("SOCIAL", "0");

            chooseWhat[0] = "1".equals(E);
            checkBoxes[0].setChecked(chooseWhat[0]);
            checkBoxes[0].setVisibility(View.VISIBLE);

            chooseWhat[1] = "1".equals(A);
            checkBoxes[1].setChecked(chooseWhat[1]);
            checkBoxes[1].setVisibility(View.VISIBLE);

            chooseWhat[2] = "1".equals(SE);
            checkBoxes[2].setChecked(chooseWhat[2]);
            checkBoxes[2].setVisibility(View.VISIBLE);

            chooseWhat[3] = "1".equals(RG);
            checkBoxes[3].setChecked(chooseWhat[3]);
            checkBoxes[3].setVisibility(View.VISIBLE);

            chooseWhat[4] = "1".equals(PL);
            checkBoxes[4].setChecked(chooseWhat[4]);
            checkBoxes[4].setVisibility(View.VISIBLE);

            chooseWhat[5] = "1".equals(SOCIAL);
            checkBoxes[5].setChecked(chooseWhat[5]);
            checkBoxes[5].setVisibility(View.VISIBLE);
        });

        netService.getModule(Uid);


        checkBoxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhat[0] = isChecked;
            }
        });

        checkBoxes[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhat[1] = isChecked;
            }
        });
        checkBoxes[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhat[2] = isChecked;
            }
        });
        checkBoxes[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhat[3] = isChecked;
            }
        });
        checkBoxes[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhat[4] = isChecked;
            }
        });
        checkBoxes[5].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhat[5] = isChecked;
            }
        });


        Sure1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("E", chooseWhat[0] ? 1 : 0);
                    jsonObject.put("A", chooseWhat[1] ? 1 : 0);
                    jsonObject.put("SE", chooseWhat[2] ? 1 : 0);
                    jsonObject.put("RG", chooseWhat[3] ? 1 : 0);
                    jsonObject.put("PL", chooseWhat[4] ? 1 : 0);
                    jsonObject.put("SOCIAL", chooseWhat[5] ? 1 : 0);
                    Log.d("10086",jsonObject.toString());
                    SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                    preferences.edit().putString("module_json", jsonObject.toString()).apply();
                    netService.updateModule(Uid,jsonObject);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Intent intent;
                intent = new Intent(choosemoduleactivity.this, MainActivity.class);
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
                intent = new Intent(choosemoduleactivity.this, MainActivity.class);
                intent.putExtra("Uid", Uid);
                intent.putExtra("isTest", false);
                startActivity(intent);
                finish();
            }
        });
    }
}
