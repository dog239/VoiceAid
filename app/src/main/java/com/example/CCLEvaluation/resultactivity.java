package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import adapter.resultadapter;
import bean.a;
import bean.audio;
import bean.e;
import bean.evaluation;
import bean.pn;
import bean.pst;
import bean.re;
import bean.rg;
import bean.s;
import utils.AudioPlayer;
import utils.ImageUrls;
import utils.dataManager;
import utils.ResultContext;

public class resultactivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private resultadapter adapter;
    private View view;
    private TextView tv2;
    private LinearLayout table;
    private ArrayList<evaluation> evaluations;
    private Button back;
    private TextView[] tvs = new TextView[21];
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result11);
        recyclerView = findViewById(R.id.recyclerview);
        back = findViewById(R.id.back);
        tv2 = findViewById(R.id.tv_2);
        table = findViewById(R.id.table);
        view = findViewById(R.id.line);
        String[] charc = ImageUrls.A_characs;
        Resources res = getResources();
        for(int k=0; k < charc.length;++k){
            int start = charc[k].indexOf('/');
            int end = charc[k].lastIndexOf('/');
            String extractedString = charc[k].substring(start + 1, end).toUpperCase();
            tvs[k] = findViewById(res.getIdentifier(extractedString,"id",getPackageName()));
        }
        try {
            initData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        back.setOnClickListener(this);

    }
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
            //横向
            setContentView(R.layout.activity_ev_menu);
        } else {
            //竖向
            setContentView(R.layout.activity_ev_menu);
        }
    }

    /**
     * 需要加载该Activity展示的对象，例如A, E, NWR, PN, PST, RE, RG, S;
     * 其加载方式：
     * 1.手动初始化
     * 2.sharePreference
     * 3.文件
     * 4.SQLLite数据库
     * 5.从网络服务器获取
     */
    private void initData() throws Exception {
        ResultContext.getInstance().setContext(this);

        evaluations = new ArrayList<evaluation>();
        //示例，该结果展示PST对象，手动初始化
        /*
            evaluations.add(new PST(0,0));
            evaluations.add(new PST(1,99));
            evaluations.add(new PST(2,99));
            evaluations.add(new PST(3,99));
            evaluations.add(new PST(4,99));
            evaluations.add(new PST(5,99));
            evaluations.add(new PST(6,99));
            evaluations.add(new PST(7,99));
         */

        //获取上个页面传来的信息，即展示哪个结果页面
        Intent intent = getIntent();
        String fName = intent.getStringExtra("fName");
        String format = intent.getStringExtra("format");
        JSONObject data = dataManager.getInstance().loadData(fName);
        if(format==null)
            return;
        if(format.equals("A")){
            String[] characs = ImageUrls.A_characs;
            int[] score = new int[21];
            tv2.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("A");
            if(jsonArray.length()>0){
                view.setVisibility(View.VISIBLE);
                table.setVisibility(View.VISIBLE);
            }
            evaluations.add(new a(0,null,null,null,null,null,null));//首行
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    if(!object.getString("target_tone1").equals("")){
                        String originalString = object.getString("target_tone1");
                        for(int j=0;j<characs.length;j++){
                            if (characs[j].equals(originalString)){
                                score[j]++;
                            }
                        }
                    }
                    if(!object.getString("target_tone2").equals("")){
                        String originalString = object.getString("target_tone2");
                        for(int j=0;j<characs.length;j++){
                            if (characs[j].equals(originalString)){
                                score[j]++;
                            }
                        }
                    }

                    evaluations.add(new a(i+1,object.getString("target"),object.getString("progress"),
                            object.getString("target_tone1"), object.getString("target_tone2"),new audio(object.getString("audioPath")),
                            object.getString("time")));
                }
                else {
                    evaluations.add(new a(i+1,object.getString("target"),null,null,null,null,null));
                }
            }
            int num[] = ImageUrls.A_nums;
            if(jsonArray.length()>0){
                for(int i=0;i<num.length;++i){
                    double lenthe = num[i];
                    double scoree = (score[i]/lenthe)*100;
                    String stre = String.format("%.2f%%",scoree);
                    tvs[i].setText(stre);
                    if(scoree>=67){
                        tvs[i].setBackgroundResource(R.drawable.right_button);
                    }else if(scoree>=34) {
                        tvs[i].setBackgroundResource(R.drawable.justsoso_button);
                    }else {
                        tvs[i].setBackgroundResource(R.drawable.wrong_button);
                    }

                }
            }

        }
        else if (format.equals("E")) {
            double counte = 0;
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("E");
            evaluations.add(new e(0, null, null, null, null));//首行
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    if (object.getBoolean("result")) {
                        counte++;
                    }
                    evaluations.add(new e(i + 1, object.getString("target"), object.getBoolean("result"), new audio(object.getString("audioPath")), object.getString("time")));
                } else {
                    evaluations.add(new e(i + 1, object.getString("target"), null, null, null));
                }
            }
            double lenthe = jsonArray.length();
            double scoree = (counte / lenthe) * 100;
            String stre = String.format("%.2f%%", scoree);
            tv2.setText("本题正确率为：" + stre);

        }else if(format.equals("PN")){
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("PN");
            evaluations.add(new pn(0,null,null,null));//首行
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    evaluations.add(new pn(i+1,object.getInt("score"),new audio(object.getString("audioPath")),object.getString("time")));
                }
                else {
                    evaluations.add(new pn(i+1,null,null,null));
                }
            }
        } else if(format.equals("PST")){
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("PST");
            evaluations.add(new pst(0,null,null,null));//首行
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    evaluations.add(new pst(i+1,object.getInt("score"),new audio(object.getString("audioPath")),object.getString("time")));
                }
                else {
                    evaluations.add(new pst(i+1,null,null,null));
                }
            }
        } else if (format.equals("RE")) {
            double countre=0;
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("RE");
            evaluations.add(new re(0,null,null,null,-1,null,null,null));//首行
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    if(object.getBoolean("result")){
                        countre++;
                    }
                    evaluations.add(new re(i+1,object.getString("target"),object.getString("targetC"),
                            object.getString("select"),
                            object.getInt("select_num"),object.getBoolean("result"),new audio(object.getString("audioPath")),object.getString("time")));
                }
                else {
                    evaluations.add(new re(i+1,object.getString("target"),object.getString("targetC"),null,-1,null,null,null));
                }
            }
            double lenthre = jsonArray.length();
            double scorere = (countre/lenthre)*100;
            String strre = String.format("%.2f%%",scorere);
            tv2.setText("本题正确率为："+strre);
        }
        else if(format.equals("RG")){
            double countre=0;
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("RG");
            evaluations.add(new rg(0,null,null,null,null,-1,null,null));//首行
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    if(object.getBoolean("result")){
                        countre++;
                    }
                    evaluations.add(new rg(i+1,object.getString("question"),object.getString("right_option"),object.getString("answer"),
                            object.getBoolean("result"),object.getInt("score"),new audio(object.getString("audioPath")),object.getString("time")));
                }
                else {
                    evaluations.add(new rg(i+1,object.getString("question"),object.getString("right_option"),null,null,null,null,null));
                }
            }
            double lenthre = jsonArray.length();
            double scorere = (countre/lenthre)*100;
            String strre = String.format("%.2f%%",scorere);
            tv2.setText("本题正确率为："+strre);

        }
        else if(format.equals("S")){
            double countre=0;
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("S");
            evaluations.add(new s(0,null,null,null,null,null));//首行
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("result")&&!object.isNull("result")){
                    if(object.getBoolean("result")){
                        countre++;
                    }
                    evaluations.add(new s(i+1,object.getString("question"),object.getString("answer"),object.getBoolean("result"),new audio(object.getString("audioPath")),object.getString("time")));
                }
                else {
                    evaluations.add(new s(i+1,object.getString("question"),object.getString("answer"),null,null,null));
                }
            }
            double lenthre = jsonArray.length();
            double scorere = (countre/lenthre)*100;
            String strre = String.format("%.2f%%",scorere);
            tv2.setText("本题正确率为："+strre);
        }
        else {

        }
        adapter = new resultadapter(this, evaluations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



    }

    @Override
    protected void onDestroy() {
        //单例player重新初始化
        AudioPlayer.getInstance().setPlayPos(-1);
        AudioPlayer.getInstance().stop();
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back){
            AudioPlayer.getInstance().setPlayPos(-1);
            AudioPlayer.getInstance().stop();
            finish();
        }
    }
}