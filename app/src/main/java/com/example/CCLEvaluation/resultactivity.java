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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private TextView tv2;
    private ArrayList<evaluation> evaluations;
    private Button back;
    private LinearLayout extraAResults;
    private LinearLayout extraASuggestions;
    private EditText vowelAccuracy;
    private EditText initialAccuracy;
    private EditText speechClarity;
    private final int[] initialIds = new int[]{
            R.id.initial_b, R.id.initial_p, R.id.initial_m, R.id.initial_f,
            R.id.initial_d, R.id.initial_t, R.id.initial_n, R.id.initial_l,
            R.id.initial_g, R.id.initial_k, R.id.initial_h, R.id.initial_j,
            R.id.initial_q, R.id.initial_x, R.id.initial_zh, R.id.initial_ch,
            R.id.initial_sh, R.id.initial_r, R.id.initial_z, R.id.initial_c
    };
    private final String[] initialLabels = new String[]{
            "b", "p", "m", "f",
            "d", "t", "n", "l",
            "g", "k", "h", "j",
            "q", "x", "zh", "ch",
            "sh", "r", "z", "c"
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result11);
        recyclerView = findViewById(R.id.recyclerview);
        back = findViewById(R.id.back);
        tv2 = findViewById(R.id.tv_2);
        extraAResults = findViewById(R.id.extra_a_results);
        extraASuggestions = findViewById(R.id.extra_a_suggestions);
        vowelAccuracy = findViewById(R.id.extra_a_vowel_accuracy);
        initialAccuracy = findViewById(R.id.extra_a_initial_accuracy);
        speechClarity = findViewById(R.id.extra_a_speech_clarity);
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
            tv2.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("A");
            if (extraAResults != null) extraAResults.setVisibility(View.VISIBLE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.VISIBLE);
            evaluations.add(new a(0, (List<a.CharacterPhonology>) null, null, null, null)); // 首行
            // 逐题读取
            List<a> aItems = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                a item = a.fromJson(object);
                aItems.add(item);
                evaluations.add(item);
            }
            // 统计声母正确率：每个声母独立 “正确/总”
            int[] correctInitial = new int[initialLabels.length];
            int[] totalInitial = new int[initialLabels.length];
            int vowelCorrect = 0;
            int vowelTotal = 0;
            for (a item : aItems) {
                List<a.CharacterPhonology> targets = item.getTargetWord();
                List<a.CharacterPhonology> answers = item.getAnswerPhonology();
                if (targets == null) continue;
                for (int idx = 0; idx < targets.size(); idx++) {
                    a.CharacterPhonology tgt = targets.get(idx);
                    a.CharacterPhonology ans = (answers != null && idx < answers.size()) ? answers.get(idx) : null;
                    String tgtInitial = safeLower(initialOf(tgt));
                    String ansInitial = safeLower(initialOf(ans));
                    if (!tgtInitial.isEmpty()) {
                        int pos = initialIndexOf(tgtInitial);
                        if (pos >= 0) {
                            totalInitial[pos]++;
                            if (tgtInitial.equals(ansInitial)) correctInitial[pos]++;
                        }
                    }
                    String tgtVowel = vowelString(tgt);
                    if (!tgtVowel.isEmpty()) {
                        vowelTotal++;
                        if (tgtVowel.equals(vowelString(ans))) vowelCorrect++;
                    }
                }
            }
            // 填充声母表格
            for (int i = 0; i < initialIds.length && i < initialLabels.length; i++) {
                EditText et = findViewById(initialIds[i]);
                if (et != null) {
                    int total = totalInitial[i];
                    int correct = correctInitial[i];
                    et.setText(total > 0 ? (correct + "/" + total) : "0/0");
                }
            }
            // 填充总体声母正确率
            double initialRate = 0d;
            if (initialAccuracy != null) {
                int sumTotal = 0, sumCorrect = 0;
                for (int i = 0; i < totalInitial.length; i++) { sumTotal += totalInitial[i]; sumCorrect += correctInitial[i]; }
                initialRate = sumTotal > 0 ? (sumCorrect * 1.0d / sumTotal) : 0d;
                String rateStr = sumTotal > 0 ? String.format(Locale.getDefault(), "%.2f%%", (initialRate * 100.0d)) : "0%";
                initialAccuracy.setText(rateStr);
            }
            // 填充韵母正确率
            if (vowelAccuracy != null) {
                String rate = vowelTotal > 0 ? String.format(Locale.getDefault(), "%.2f%%", (vowelCorrect * 100.0f / vowelTotal)) : "0%";
                vowelAccuracy.setText(rate);
            }
            // 根据声母总正确率填写语音清晰度等级
            if (speechClarity != null) {
                String level;
                if (initialRate >= 0.85d) level = "轻度";
                else if (initialRate >= 0.65d) level = "轻中度";
                else if (initialRate >= 0.50d) level = "中重度";
                else level = "重度";
                speechClarity.setText(level);
            }
        }
        else if (format.equals("E")) {
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
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

    private String initialOf(a.CharacterPhonology cp) {
        if (cp == null || cp.phonology == null) return "";
        return cp.phonology.initial == null ? "" : cp.phonology.initial.trim();
    }

    private String vowelString(a.CharacterPhonology cp) {
        if (cp == null || cp.phonology == null) return "";
        String m = cp.phonology.medial == null ? "" : cp.phonology.medial.trim();
        String n = cp.phonology.nucleus == null ? "" : cp.phonology.nucleus.trim();
        String c = cp.phonology.coda == null ? "" : cp.phonology.coda.trim();
        String res = m + n + c;
        return res.trim();
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.getDefault());
    }

    private int initialIndexOf(String ini) {
        for (int i = 0; i < initialLabels.length; i++) {
            if (initialLabels[i].equalsIgnoreCase(ini)) return i;
        }
        return -1;
    }

}