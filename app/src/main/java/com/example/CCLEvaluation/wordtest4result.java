package com.example.CCLEvaluation;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import utils.AudioPlayer;
import utils.dataManager;
import utils.ResultContext;


public class wordtest4result extends AppCompatActivity  {
    private View[] audio = new View[6];
    private TextView tv2;

    private boolean table[][] = new boolean[6][6];

    private CharSequence[][] tests ={{"把","丹","召","歹","库","尚"},
            {"商","楷","到","尬","展","铺"},
            {"帮","债","看","赌","刹","搞"},
            {"战","搭","树","稿","掰","抗"},
            {"嘎","少","棒","苦","胆","摘"},
            {"稍","嘎","办","污","康","歹"}};

    private String[] question = new String[6];
    private Boolean[] results = new Boolean[6];
    private TextView checkTextViews[] = new TextView[6];
    private TextView checkTextViewTime;

    private MediaPlayer[] mediaPlayers = new MediaPlayer[6];
    private int currentPlayer = -1;
    private ArrayList<bean.evaluation> evaluations;
    private String audioPath;
    private String fName;

    private JSONArray jsonArray;
    private JSONObject data;
    private JSONObject evaluation;
    private JSONObject object;
    private Button buttonBack;
    private int right_number = 0;
    private int answer_number = 0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_test4_result);
        audio[0] = findViewById(R.id.iv_w1_210);
        audio[1] = findViewById(R.id.iv_w1_310);
        audio[2] = findViewById(R.id.iv_w1_410);
        audio[3] = findViewById(R.id.iv_w1_510);
        audio[4] = findViewById(R.id.iv_w1_610);
        buttonBack = findViewById(R.id.btn_w1back);
        tv2 = findViewById(R.id.tv_2);
        ResultContext.getInstance().setContext(this);
        Intent intent = getIntent();
        fName = intent.getStringExtra("fName");
        evaluations = new ArrayList<bean.evaluation>();

        try {
            setTestAndAudio(true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayer.getInstance().setPlayPos(-1);
                AudioPlayer.getInstance().stop();
                finish();
            }
        });
    }


    private void setTestAndAudio(boolean first)throws JSONException{
        try {
            double countnwr=0;
            data = dataManager.getInstance().loadData(fName);
            evaluation = data.getJSONObject("evaluations");
            jsonArray = evaluation.getJSONArray("NWR");
            if(jsonArray.length()!=0){
                for(int i=0;i<jsonArray.length();i++) {
                    object = jsonArray.getJSONObject(i);
                    for (int j = 0; j < 6; ++j) {
                        if(object.has("results" + (j + 1)) && !object.isNull("results" + (j + 1))){
                            if(object.getBoolean("results" + (j + 1))){
                                countnwr++;
                            }
                            results[j] = object.getBoolean("results" + (j + 1));
                        }else{
                            results[j] = false;
                        }
                    }
                    try {
                        setTextOnly(i+2);
                        if(first){
                            if(object.has("audioPath") && !object.isNull("audioPath")){
                                audioPath = object.getString("audioPath");
                                if(!audioPath.equals("null")){
                                    setAudioOnly(i+2,audioPath);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("qqqqq",String.valueOf(audio[i]));
                }
                double lenthe = jsonArray.length()*6;
                double scoree = (countnwr/lenthe)*100;
                String stre = String.format("%.2f%%",scoree);
                tv2.setText("本题正确率为："+stre);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void setTextOnly(int i) throws JSONException {
        int j=i-2;

        Resources res = getResources();
        for(int k=0; k<6;++k){
            checkTextViews[k] = findViewById(res.getIdentifier("tv_w1_"+String.valueOf(i)+String.valueOf(k+3),"id",getPackageName()));
        }
        Log.d("ssss",String.valueOf(i));
        checkTextViewTime = findViewById(res.getIdentifier("tv_w1_"+String.valueOf(i)+"9","id",getPackageName()));
        String timeString;
        if(object.has("time") && !object.isNull("time")){
            timeString = object.getString("time");
        }else {
            timeString = "";
        }

        checkTextViewTime.setText(timeString);
        answer_number = answer_number + 1;
        int num_correct=0;
        for(int k=0; k<6;++k){
            Boolean answersBool = results[k];
            if(answersBool){
                checkTextViews[k].setBackgroundResource(R.drawable.block);
                table[j][k] = true;
                num_correct = num_correct + 1;
            }else{
                checkTextViews[k].setBackgroundResource(R.drawable.table);
                table[j][k] = false;
            }
        }
    }
    private void setAudioOnly(int i,String audioPath) throws JSONException{
        int j=i-2;
        Resources res = getResources();
        audio[j] = findViewById(res.getIdentifier("iv_w1_"+String.valueOf(i)+"10","id",getPackageName()));
        ((TextView)audio[j]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
        ((TextView)audio[j]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
        AudioPlayer.getInstance().addIcon((TextView)audio[j]);
        audio[j].setOnClickListener(v -> AudioPlayer.getInstance().play(audioPath,j));
    }

    protected void onDestroy() {
        //单例player重新初始化
        AudioPlayer.getInstance().setPlayPos(-1);
        AudioPlayer.getInstance().stop();
        super.onDestroy();
    }
}