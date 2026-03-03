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
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import adapter.resultadapter;
import bean.a;
import bean.audio;
import bean.e;
import bean.ev;
import bean.evaluation;
import bean.pn;
import bean.pst;
import bean.pl;
import bean.re;
import bean.rg;
import bean.s;
import bean.se;
import bean.social;
import utils.AudioPlayer;
import utils.ImageUrls;
import utils.ModuleReportHelper;
import utils.dataManager;
import utils.ResultContext;

public class resultactivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private resultadapter adapter;
    private TextView tv2;
    private ArrayList<evaluation> evaluations;
    private Button back;
    private String fName;
    private LinearLayout extraAResults;
    private LinearLayout extraASuggestions;
    private EditText vowelAccuracy;
    private EditText initialAccuracy;
    private EditText speechClarity;
    private CheckBox diagNormal;
    private CheckBox diagPhonology;
    private CheckBox diagTone;
    private CheckBox suggestNone;
    private CheckBox suggestFollow;
    private CheckBox suggestTrad;
    private CheckBox suggestPhono;
    private CheckBox suggestMore;
    private EditText followMonths;
    private EditText tradFreq;
    private EditText phonoFreq;
    private LinearLayout plSuggestions;
    private RadioGroup plOptionGroup;
    private Button plSaveButton;
    private static final String FORMAT_PL = "11";
    private static final String MODULE_PL = "PL";
    private String plSummaryTextValue;
    private boolean isPrelinguisticResult;
    private boolean isArticulationResult;
    private boolean isSocialResult;
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
        diagNormal = findViewById(R.id.extra_a_diag_normal);
        diagPhonology = findViewById(R.id.extra_a_diag_phonology);
        diagTone = findViewById(R.id.extra_a_diag_tone);
        suggestNone = findViewById(R.id.extra_a_suggest_none);
        suggestFollow = findViewById(R.id.extra_a_suggest_follow);
        suggestTrad = findViewById(R.id.extra_a_suggest_trad);
        suggestPhono = findViewById(R.id.extra_a_suggest_phono);
        suggestMore = findViewById(R.id.extra_a_suggest_more);
        followMonths = findViewById(R.id.extra_a_follow_months);
        tradFreq = findViewById(R.id.extra_a_trad_freq);
        phonoFreq = findViewById(R.id.extra_a_phono_freq);
        plSuggestions = findViewById(R.id.pl_suggestions);
        plOptionGroup = findViewById(R.id.pl_option_group);
        plSaveButton = findViewById(R.id.pl_save_button);
        try {
            initData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        back.setOnClickListener(this);
        if (plSaveButton != null) {
            plSaveButton.setOnClickListener(this);
        }

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
        if (plSuggestions != null) {
            plSuggestions.setVisibility(View.GONE);
        }
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
        String moduleKey = intent.getStringExtra("moduleKey");
        this.fName = fName;
        
        // 检查fName是否为空
        if (fName == null || fName.isEmpty()) {
            Toast.makeText(this, "文件名不能为空！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        JSONObject data = dataManager.getInstance().loadData(fName);
        if(format==null && moduleKey == null)
            return;
        String safeFormat = format == null ? "" : format;
        boolean isPrelinguistic = MODULE_PL.equals(moduleKey) || MODULE_PL.equals(format) || FORMAT_PL.equals(format);
        isPrelinguisticResult = isPrelinguistic;
        boolean isArticulation = "A".equals(safeFormat);
        isArticulationResult = isArticulation;
        boolean isSocial = "SOCIAL".equals(safeFormat);
        isSocialResult = isSocial;
        if(isArticulation){
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
            JSONObject evaluations = data.optJSONObject("evaluations");
            if (evaluations != null) {
                String savedClarity = safeText(evaluations.optString("speech_intelligibility", ""));
                if (savedClarity.isEmpty()) {
                    savedClarity = safeText(evaluations.optString("intelligibility_level", ""));
                }
                if (!savedClarity.isEmpty() && speechClarity != null) {
                    speechClarity.setText(savedClarity);
                }
            }
        }
        else if (safeFormat.equals("E")) {
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            
            // 词汇表达结果
            double counte = 0;
            JSONArray eArray = null;
            try {
                eArray = data.getJSONObject("evaluations").getJSONArray("E");
            } catch (Exception e) {
                eArray = new JSONArray();
            }
            
            // 添加词汇表达表头
            evaluations.add(new e(0, "目标词", null, null, null));//词汇表达首行
            for (int i = 0; i < eArray.length(); i++) {
                JSONObject object = eArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    if (object.getBoolean("result")) {
                        counte++;
                    }
                    // 检查是否有录音路径
                    if (object.has("audioPath") && !object.isNull("audioPath")) {
                        evaluations.add(new e(i + 1, object.getString("target"), object.getBoolean("result"), new audio(object.getString("audioPath")), object.getString("time")));
                    } else {
                        evaluations.add(new e(i + 1, object.getString("target"), object.getBoolean("result"), null, object.getString("time")));
                    }
                } else {
                    evaluations.add(new e(i + 1, object.getString("target"), null, null, null));
                }
            }
            double lenthe = eArray.length();
            double scoree = lenthe > 0 ? (counte / lenthe) * 100 : 0;
            String stre = String.format("%.2f%%", scoree);
            
            // 添加词汇理解表头
            evaluations.add(new ev(0, "目标词", null, null));//词汇理解首行
            double countev = 0;
            JSONArray evArray = null;
            try {
                evArray = data.getJSONObject("evaluations").getJSONArray("EV");
            } catch (Exception e) {
                evArray = new JSONArray();
            }
            for (int i = 0; i < evArray.length(); i++) {
                JSONObject object = evArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    if (object.getBoolean("result")) {
                        countev++;
                    }
                    evaluations.add(new ev(i + 1, object.getString("target"), object.getBoolean("result"), object.getString("time")));
                } else {
                    evaluations.add(new ev(i + 1, object.getString("target"), null, null));
                }
            }
            double lenthev = evArray.length();
            double scoreev = lenthev > 0 ? (countev / lenthev) * 100 : 0;
            String strev = String.format("%.2f%%", scoreev);
            
            // 合并显示结果
            tv2.setText("词汇表达正确率：" + stre + "\n词汇理解正确率：" + strev);
            
            // 添加评估建议
            TextView tv3 = findViewById(R.id.tv_3);
            if (tv3 != null) {
                tv3.setVisibility(View.VISIBLE);
                
                // 自动匹配评估建议
                if (scoree == 100 && scoreev == 100) {
                    tv3.setText("评估建议：词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子各类型词汇掌握较好，词汇表达和理解能力都达到了优秀水平。建议继续丰富孩子的词汇量，鼓励孩子使用更复杂的词汇和句子。");
                } else if (scoree == 0 && scoreev == 0) {
                    tv3.setText("评估建议：词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子各类型词汇掌握得不够好，需要针对性的学习。建议从基础词汇开始，通过实物、图片等直观方式帮助孩子理解和记忆词汇，逐步提高词汇量。");
                } else if (scoree >= 60 && scoreev >= 60) {
                    tv3.setText("评估建议：词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子各类型词汇掌握较好，建议继续扩大词汇量，注重词汇的实际应用，鼓励孩子在日常生活中多使用新学的词汇。");
                } else if (scoree >= 60 && scoreev < 60) {
                    tv3.setText("评估建议：词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子词汇表达能力较好，但理解能力有待提高，需要针对性的学习。建议通过多听、多看、多互动的方式，帮助孩子理解更多词汇的含义。");
                } else if (scoree < 60 && scoreev >= 60) {
                    tv3.setText("评估建议：词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子词汇理解能力较好，但表达能力有待提高，需要针对性的学习。建议多鼓励孩子开口说话，通过模仿、复述等方式，帮助孩子提高词汇表达能力。");
                } else {
                    tv3.setText("评估建议：词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子词汇能力发展还有待加强，需要针对性的学习。建议根据孩子的兴趣和生活经验，选择适合的词汇进行教学，注重词汇的趣味性和实用性。");
                }
            }

        } else if (safeFormat.equals("EV")) {
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            double countev = 0;
            JSONArray jsonArray = null;
            try {
                jsonArray = data.getJSONObject("evaluations").getJSONArray("EV");
            } catch (Exception e) {
                jsonArray = new JSONArray();
            }
            evaluations.add(new ev(0, null, null, null));//首行
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    if (object.getBoolean("result")) {
                        countev++;
                    }
                    evaluations.add(new ev(i + 1, object.getString("target"), object.getBoolean("result"), object.getString("time")));
                } else {
                    evaluations.add(new ev(i + 1, object.getString("target"), null, null));
                }
            }
            double lenthev = jsonArray.length();
            double scoreev = lenthev > 0 ? (countev / lenthev) * 100 : 0;
            String strev = String.format("%.2f%%", scoreev);
            tv2.setText("本题正确率为：" + strev);
            
            // 添加评估建议
            TextView tv3 = findViewById(R.id.tv_3);
            if (tv3 != null) {
                tv3.setVisibility(View.VISIBLE);
                if (scoreev >= 60) {
                    tv3.setText("评估建议：孩子的词汇理解能力较好，基本达标，建议继续扩大词汇量，注重词汇的实际应用。");
                } else {
                    tv3.setText("评估建议：孩子的词汇理解能力还有待提高，建议通过多听、多看、多互动的方式，帮助孩子理解更多词汇的含义。");
                }
            }

        }else if(safeFormat.equals("PN")){
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
        } else if(safeFormat.equals("PST")){
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
        } else if (safeFormat.equals("RE")) {
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
            
            // 添加评估建议
            TextView tv3 = findViewById(R.id.tv_3);
            if (tv3 != null) {
                tv3.setVisibility(View.VISIBLE);
                if (scorere >= 66.7) {
                    tv3.setText("评估建议：孩子的句法理解能力较好，基本达标，符合该年龄段孩子语言发育水平。");
                } else {
                    tv3.setText("评估建议：孩子的句法理解能力还有待进一步发展，尚未达标。");
                }
            }
        } else if(safeFormat.equals("RG")){
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            double countre=0;
            JSONArray jsonArray = null;
            // 检查是否存在分组特定的 JSONArray（如 "RG1", "RG2" 等）
            JSONObject evaluationsObj = data.getJSONObject("evaluations");
            for (int i = 1; i <= 4; i++) {
                String rgKey = "RG" + i;
                if (evaluationsObj.has(rgKey)) {
                    JSONArray groupArray = evaluationsObj.getJSONArray(rgKey);
                    if (groupArray.length() > 0) {
                        if (jsonArray == null) {
                            jsonArray = groupArray;
                        } else {
                            // 如果有多个分组数据，合并它们
                            for (int j = 0; j < groupArray.length(); j++) {
                                jsonArray.put(groupArray.get(j));
                            }
                        }
                    }
                }
            }
            // 如果没有找到分组特定的 JSONArray，则使用默认的 "RG" JSONArray
            if (jsonArray == null) {
                jsonArray = evaluationsObj.optJSONArray("RG");
                if (jsonArray == null) {
                    jsonArray = new JSONArray();
                }
            }
            evaluations.add(new rg(0,null,null,null,null,-1,null,null));//首行
            int completedCount = 0;
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    if(object.getBoolean("result")){
                        countre++;
                    }
                    // 检查是否有录音路径和分数
                    int score = object.has("score") && !object.isNull("score") ? object.getInt("score") : -1;
                    if (object.has("audioPath") && !object.isNull("audioPath")) {
                        evaluations.add(new rg(i+1,object.getString("question"),object.getString("right_option"),object.getString("answer"),
                                object.getBoolean("result"),score,new audio(object.getString("audioPath")),object.getString("time")));
                    } else {
                        evaluations.add(new rg(i+1,object.getString("question"),object.getString("right_option"),object.getString("answer"),
                                object.getBoolean("result"),score,null,object.getString("time")));
                    }
                    completedCount++;
                }
                // 只显示已经完成的题目，跳过未完成的题目
            }
            double lenthre = completedCount;
            double scorere = lenthre > 0 ? (countre/lenthre)*100 : 0;
            String strre = String.format("%.2f%%",scorere);
            tv2.setText("本题正确率为："+strre);

            // 添加评估建议
            TextView tv3 = findViewById(R.id.tv_3);
            if (tv3 != null) {
                tv3.setVisibility(View.VISIBLE);
                if (scorere >= 66.7) {
                    tv3.setText("评估建议：孩子的句法理解能力较好，基本达标，符合该年龄段孩子语言发育水平。");
                } else {
                    tv3.setText("评估建议：孩子的句法理解能力还有待进一步发展，尚未达标。");
                }
            }

        } else if(safeFormat.equals("SE")){
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            double countre=0;
            JSONArray jsonArray = null;
            // 检查是否存在分组特定的 JSONArray（如 "SE1", "SE2" 等）
            JSONObject evaluationsObj = data.getJSONObject("evaluations");
            for (int i = 1; i <= 4; i++) {
                String seKey = "SE" + i;
                if (evaluationsObj.has(seKey)) {
                    JSONArray groupArray = evaluationsObj.getJSONArray(seKey);
                    if (groupArray.length() > 0) {
                        if (jsonArray == null) {
                            jsonArray = groupArray;
                        } else {
                            // 如果有多个分组数据，合并它们
                            for (int j = 0; j < groupArray.length(); j++) {
                                jsonArray.put(groupArray.get(j));
                            }
                        }
                    }
                }
            }
            // 如果没有找到分组特定的 JSONArray，则使用默认的 "SE" JSONArray
            if (jsonArray == null) {
                jsonArray = evaluationsObj.optJSONArray("SE");
                if (jsonArray == null) {
                    jsonArray = new JSONArray();
                }
            }
            evaluations.add(new se(0,null,null,null,null,-1,null,null));//首行
            int completedCount = 0;
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.has("time")&&!object.isNull("time")){
                    if(object.getBoolean("result")){
                        countre++;
                    }
                    // 检查是否有录音路径和分数
                    int score = object.has("score") && !object.isNull("score") ? object.getInt("score") : -1;
                    if (object.has("audioPath") && !object.isNull("audioPath")) {
                        evaluations.add(new se(i+1,object.getString("question"),object.getString("right_option"),object.getString("answer"),
                                object.getBoolean("result"),score,new audio(object.getString("audioPath")),object.getString("time")));
                    } else {
                        evaluations.add(new se(i+1,object.getString("question"),object.getString("right_option"),object.getString("answer"),
                                object.getBoolean("result"),score,null,object.getString("time")));
                    }
                    completedCount++;
                }
                // 只显示已经完成的题目，跳过未完成的题目
            }
            double lenthre = completedCount;
            double scorere = lenthre > 0 ? (countre/lenthre)*100 : 0;
            String strre = String.format("%.2f%%",scorere);
            tv2.setText("本题正确率为："+strre);

            // 添加评估建议
            TextView tv3 = findViewById(R.id.tv_3);
            if (tv3 != null) {
                tv3.setVisibility(View.VISIBLE);
                if (scorere >= 66.7) {
                    tv3.setText("评估建议：孩子的句法表达能力较好，基本达标，符合该年龄段孩子语言发育水平。");
                } else {
                    tv3.setText("评估建议：孩子的句法表达能力还有待进一步发展，尚未达标。");
                }
            }

        } else if(safeFormat.equals("S")){
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
            double scorere = lenthre > 0 ? (countre/lenthre)*100 : 0;
            String strre = String.format("%.2f%%",scorere);
            tv2.setText("本题正确率为："+strre);
        }
        else if(isPrelinguistic){
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            if (plSuggestions != null) plSuggestions.setVisibility(View.VISIBLE);

            JSONArray jsonArray = data.getJSONObject("evaluations").optJSONArray(MODULE_PL);
            if (jsonArray == null) {
                jsonArray = new JSONArray();
            }
            evaluations.add(new pl(0, null, null, null, null, null, null));//首行
            List<pl> plItems = new ArrayList<>();
            JSONObject report = ModuleReportHelper.loadPrelinguisticReport(data);
            String scene = report != null ? report.optString("scene", "") : "";
            if (scene == null || scene.trim().isEmpty()) {
                scene = "A";
            }
            String[] prompts = "B".equals(scene) ? ImageUrls.PL_PROMPTS_B : ImageUrls.PL_PROMPTS_A;
            String[] skills = ImageUrls.PL_SKILLS;
            for(int i=0;i<skills.length;i++){
                pl item;
                if (jsonArray.length() > i) {
                    item = pl.fromJson(jsonArray.getJSONObject(i));
                } else {
                    item = new pl(i + 1, null, null, null, null, null, null);
                }
                if (item.getSkill() == null || item.getSkill().trim().isEmpty()) {
                    item.setSkill(skills[i]);
                }
                if (item.getPrompt() == null || item.getPrompt().trim().isEmpty()) {
                    item.setPrompt(prompts[i]);
                }
                plItems.add(item);
                evaluations.add(item);
            }
            int totalScore = 0;
            List<String> strengths = new ArrayList<>();
            List<String> weaknesses = new ArrayList<>();
            for (pl item : plItems) {
                int value = item.getScore() == null ? 0 : item.getScore();
                if (value == 1) {
                    totalScore++;
                    strengths.add(item.getSkill());
                } else {
                    weaknesses.add(item.getSkill());
                }
            }
            plSummaryTextValue = ModuleReportHelper.buildPrelinguisticSummaryText(strengths, weaknesses);
            tv2.setText(plSummaryTextValue);

            evaluations.add(new pl(-1, "总分", null, totalScore, null, null, null));

            int option = report != null ? report.optInt("suggestionOption", 1) : 1;
            String option1Text = ModuleReportHelper.getPrelinguisticSuggestionOptionText(1);
            String option2Text = ModuleReportHelper.getPrelinguisticSuggestionOptionText(2);
            JSONArray savedOptions = report != null ? report.optJSONArray("suggestionOptions") : null;
            if (savedOptions != null && savedOptions.length() >= 2) {
                String savedOption1 = safeText(savedOptions.optString(0, ""));
                String savedOption2 = safeText(savedOptions.optString(1, ""));
                if (!savedOption1.isEmpty()) {
                    option1Text = savedOption1;
                }
                if (!savedOption2.isEmpty()) {
                    option2Text = savedOption2;
                }
            }
            RadioButton option1Button = findViewById(R.id.pl_option1);
            RadioButton option2Button = findViewById(R.id.pl_option2);
            if (option1Button != null) {
                option1Button.setText(option1Text);
            }
            if (option2Button != null) {
                option2Button.setText(option2Text);
            }
            if (plOptionGroup != null) {
                plOptionGroup.setVisibility(View.VISIBLE);
                plOptionGroup.check(option == 2 ? R.id.pl_option2 : R.id.pl_option1);
            }

            if (plSaveButton != null) {
                plSaveButton.setVisibility(View.GONE);
            }
        } else if (safeFormat.equals("SOCIAL")) {
            // 强制横屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);

            JSONArray jsonArray = data.getJSONObject("evaluations").optJSONArray("SOCIAL");
            if (jsonArray == null) {
                jsonArray = new JSONArray();
            }
            
            // 清空之前的评估结果
            evaluations.clear();
            
            // 添加表头
            evaluations.add(new social(0, "题号", "社交能力", "考查点", null, null, null, null));
            
            List<social> socialItems = new ArrayList<>();
            
            String[] abilities = ImageUrls.SOCIAL_abilities;
            String[] focuses = ImageUrls.SOCIAL_focuses;
            String[] contents = ImageUrls.SOCIAL_contents;
            
            // 检查数组是否为null
            if (abilities == null || focuses == null || contents == null) {
                Toast.makeText(this, "数据加载失败：题目数据为空！", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 创建一个映射来存储已有的结果，避免重复
            HashMap<Integer, social> socialItemMap = new HashMap<>();
            
            // 首先加载已有的结果
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    social item = social.fromJson(jsonArray.getJSONObject(i));
                    int num = item.getNum();
                    if (num > 0) {
                        socialItemMap.put(num, item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // 按组处理题目，只显示已完成的组
            int totalScore = 0;
            int completedQuestions = 0;
            List<String> weaknesses = new ArrayList<>();
            List<String> inProgress = new ArrayList<>();
            
            // 按组号从小到大处理
            for (int group = 1; group <= 6; group++) {
                boolean hasCompletedQuestions = false;
                
                // 处理该组的10个题目
                for (int i = 0; i < 10; i++) {
                    int questionIndex = (group - 1) * 10 + i;
                    if (questionIndex >= abilities.length) break;
                    
                    int questionNumber = questionIndex + 1;
                    social item = socialItemMap.get(questionNumber);
                    
                    if (item == null) {
                        // 如果没有已有结果，创建新对象
                        item = new social(questionNumber, abilities[questionIndex], focuses[questionIndex], contents[questionIndex], null, null, null, null);
                    } else {
                        // 如果有已有结果，确保能力、考查点和题目内容正确
                        if (item.getAbility() == null || item.getAbility().trim().isEmpty()) {
                            item.setAbility(abilities[questionIndex]);
                        }
                        if (item.getFocus() == null || item.getFocus().trim().isEmpty()) {
                            item.setFocus(focuses[questionIndex]);
                        }
                        if (item.getContent() == null || item.getContent().trim().isEmpty()) {
                            item.setContent(contents[questionIndex]);
                        }
                    }
                    
                    socialItems.add(item);
                    
                    // 检查是否有得分
                    Integer score = item.getScore();
                    if (score != null) {
                        hasCompletedQuestions = true;
                        completedQuestions++;
                        totalScore += score;
                        
                        // 收集需要重点关注和进一步发展的能力
                        if (score == 1 && !inProgress.contains(item.getAbility())) {
                            inProgress.add(item.getAbility());
                        } else if (score == 0 && !weaknesses.contains(item.getAbility())) {
                            weaknesses.add(item.getAbility());
                        }
                        
                        // 添加到评估结果中 - 无论得分如何，只要完成了就显示
                        evaluations.add(item);
                    }
                }
                
                // 如果该组有完成的题目，添加一个空行作为分隔
                if (hasCompletedQuestions) {
                    evaluations.add(new social(-1, "", "", "", null, null, null, null));
                }
            }
            
            // 计算总体评估结果
            double accuracy = 0;
            if (completedQuestions > 0) {
                accuracy = (double) totalScore / (completedQuestions * 2);
            }
            
            // 生成评估建议
            StringBuilder detailedAssessment = new StringBuilder();
            detailedAssessment.append("（二）评估建议\n\n");
            detailedAssessment.append("通过对儿童社交能力的评估（家长试卷）结果如下：\n\n");
            
            // 根据正确率选择评估结果
            if (accuracy >= 0.6) {
                detailedAssessment.append("● 从整体上来说，孩子的社交能力较好，基本达标。\n\n");
            } else {
                detailedAssessment.append("● 从整体上来说，孩子的社交能力还有待进一步发展，尚未达标。\n\n");
            }
            
            // 显示需要重点关注的能力
            detailedAssessment.append("1.需要重点关注：\n");
            if (!weaknesses.isEmpty()) {
                for (int i = 0; i < weaknesses.size(); i++) {
                    detailedAssessment.append((i + 1)).append(". " ).append(weaknesses.get(i)).append("\n");
                }
            } else {
                detailedAssessment.append("无\n");
            }
            detailedAssessment.append("\n");
            
            // 显示需要进一步发展的能力
            detailedAssessment.append("2.有些社交能力已经表现出来，但是不够经常，我们可以多去发展以下能力：\n");
            if (!inProgress.isEmpty()) {
                for (int i = 0; i < inProgress.size(); i++) {
                    detailedAssessment.append((i + 1)).append(". " ).append(inProgress.get(i)).append("\n");
                }
            } else {
                detailedAssessment.append("无\n");
            }
            detailedAssessment.append("\n");
            
            if (tv2 != null) {
                tv2.setText(detailedAssessment.toString());
            }

            // 添加总分行
            evaluations.add(new social(-1, "总分", null, null, totalScore, null, null, null));
        }
        else {
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
        }
        adapter = new resultadapter(this, evaluations);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }



    }

    @Override
    protected void onDestroy() {
        //单例player重新初始化
        AudioPlayer.getInstance().setPlayPos(-1);
        AudioPlayer.getInstance().stop();
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        savePrelinguisticReport();
        saveArticulationReport();
        saveSocialReport();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back){
            AudioPlayer.getInstance().setPlayPos(-1);
            AudioPlayer.getInstance().stop();
            savePrelinguisticReport();
            saveArticulationReport();
            saveSocialReport();
            finish();
        } else if (v.getId() == R.id.pl_save_button) {
            savePrelinguisticReport();
            finish();
        }
    }

    private void savePrelinguisticReport() {
        if (!isPrelinguisticResult) {
            return;
        }
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到评估数据", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject data;
        try {
            data = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            Toast.makeText(this, "读取评估数据失败", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject report = ModuleReportHelper.loadPrelinguisticReport(data);
        if (report == null) {
            report = new JSONObject();
        }
        String scene = report.optString("scene", "A");

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        int totalScore = 0;
        if (evaluations != null) {
            for (evaluation evaluation : evaluations) {
                if (!(evaluation instanceof pl)) {
                    continue;
                }
                pl item = (pl) evaluation;
                String skill = item.getSkill();
                if (skill == null || skill.trim().isEmpty()) {
                    continue;
                }
                int value = item.getScore() == null ? 0 : item.getScore();
                if (value == 1) {
                    totalScore++;
                    strengths.add(skill);
                } else {
                    weaknesses.add(skill);
                }
            }
        }

        int option = plOptionGroup != null && plOptionGroup.getCheckedRadioButtonId() == R.id.pl_option2 ? 2 : 1;
        String option1Text = resolvePlOptionText(R.id.pl_option1,
                ModuleReportHelper.getPrelinguisticSuggestionOptionText(1));
        String option2Text = resolvePlOptionText(R.id.pl_option2,
                ModuleReportHelper.getPrelinguisticSuggestionOptionText(2));
        String summaryText = plSummaryTextValue != null ? plSummaryTextValue
                : ModuleReportHelper.buildPrelinguisticSummaryText(strengths, weaknesses);
        String suggestionText = option == 2 ? option2Text : option1Text;
        JSONArray suggestionOptions = new JSONArray();
        suggestionOptions.put(option1Text);
        suggestionOptions.put(option2Text);

        try {
            report.put("scene", scene);
            report.put("totalScore", totalScore);
            report.put("strengths", new JSONArray(strengths));
            report.put("weaknesses", new JSONArray(weaknesses));
            report.put("summaryText", summaryText);
            report.put("suggestionOption", option);
            report.put("suggestionOptions", suggestionOptions);
            report.put("suggestionText", suggestionText);
            ModuleReportHelper.savePrelinguisticReport(data, report);
            dataManager.getInstance().saveChildJson(fName, data);
            Toast.makeText(this, "已保存评估报告", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存评估报告失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveArticulationReport() {
        if (!isArticulationResult) {
            return;
        }
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到评估数据", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject data;
        try {
            data = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            Toast.makeText(this, "读取评估数据失败", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) {
            evaluations = new JSONObject();
        }
        try {
            data.put("evaluations", evaluations);
            String intelligibility = safeText(getEditTextValue(speechClarity));
            if (intelligibility.isEmpty()) {
                intelligibility = safeText(evaluations.optString("speech_intelligibility", ""));
                if (intelligibility.isEmpty()) {
                    intelligibility = safeText(evaluations.optString("intelligibility_level", ""));
                }
            }
            String diagnosis = buildDiagnosisText();
            if (!hasDiagnosisInput() && diagnosis.isEmpty()) {
                diagnosis = safeText(evaluations.optString("clinical_diagnosis", ""));
            }
            String suggestions = buildSuggestionText();
            if (!hasSuggestionInput() && suggestions.isEmpty()) {
                suggestions = safeText(evaluations.optString("assessment_suggestions", ""));
            }
            evaluations.put("speech_intelligibility", intelligibility);
            evaluations.put("clinical_diagnosis", diagnosis);
            evaluations.put("assessment_suggestions", suggestions);
            String initialText = safeText(getEditTextValue(initialAccuracy));
            if (initialText.isEmpty()) {
                initialText = safeText(evaluations.optString("initial_accuracy", ""));
            }
            String vowelText = safeText(getEditTextValue(vowelAccuracy));
            if (vowelText.isEmpty()) {
                vowelText = safeText(evaluations.optString("vowel_accuracy", ""));
            }
            evaluations.put("initial_accuracy", initialText);
            evaluations.put("vowel_accuracy", vowelText);
            dataManager.getInstance().saveChildJson(fName, data);
            Toast.makeText(this, "已保存评估报告", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存评估报告失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSocialReport() {
        if (!isSocialResult) {
            return;
        }
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到评估数据", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject data;
        try {
            data = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            Toast.makeText(this, "读取评估数据失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> strengths = new ArrayList<>();
        List<String> inProgress = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        int totalScore = 0;
        if (evaluations != null) {
            for (evaluation evaluation : evaluations) {
                if (!(evaluation instanceof social)) {
                    continue;
                }
                social item = (social) evaluation;
                // 跳过首行和总分行
                if (item.getNum() == 0 || item.getNum() == -1) {
                    continue;
                }
                String ability = item.getAbility();
                if (ability == null || ability.trim().isEmpty()) {
                    continue;
                }
                Integer score = item.getScore();
                if (score == null) {
                    continue;
                }
                int value = score;
                totalScore += value;
                if (value == 2) {
                    strengths.add(ability);
                } else if (value == 1) {
                    inProgress.add(ability);
                } else {
                    weaknesses.add(ability);
                }
            }
        }
        
        JSONObject report = ModuleReportHelper.buildSocialReport(totalScore, strengths, inProgress, weaknesses);
        try {
            ModuleReportHelper.saveSocialReport(data, report);
            // 直接使用saveData方法保存数据，而不是saveChildJson
            dataManager.getInstance().saveData(fName, data);
            Toast.makeText(this, "已保存社交能力评估报告", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存社交能力评估报告失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildDiagnosisText() {
        List<String> items = new ArrayList<>();
        addCheckedLabel(items, diagNormal);
        addCheckedLabel(items, diagPhonology);
        addCheckedLabel(items, diagTone);
        return joinText(items, "、");
    }

    private String buildSuggestionText() {
        List<String> items = new ArrayList<>();
        if (suggestNone != null && suggestNone.isChecked()) {
            addText(items, suggestNone.getText());
        }
        if (suggestFollow != null && suggestFollow.isChecked()) {
            String base = safeText(suggestFollow.getText() == null ? "" : suggestFollow.getText().toString());
            String months = safeText(getEditTextValue(followMonths));
            if (!months.isEmpty()) {
                base = base + months + "个月后复测";
            }
            if (!base.isEmpty()) {
                items.add(base);
            }
        }
        if (suggestTrad != null && suggestTrad.isChecked()) {
            String base = safeText(suggestTrad.getText() == null ? "" : suggestTrad.getText().toString());
            String freq = safeText(getEditTextValue(tradFreq));
            if (!freq.isEmpty()) {
                base = base + freq + "次/周";
            }
            if (!base.isEmpty()) {
                items.add(base);
            }
        }
        if (suggestPhono != null && suggestPhono.isChecked()) {
            String base = safeText(suggestPhono.getText() == null ? "" : suggestPhono.getText().toString());
            String freq = safeText(getEditTextValue(phonoFreq));
            if (!freq.isEmpty()) {
                base = base + freq + "次/周";
            }
            if (!base.isEmpty()) {
                items.add(base);
            }
        }
        if (suggestMore != null && suggestMore.isChecked()) {
            addText(items, suggestMore.getText());
        }
        return joinText(items, "；");
    }

    private boolean hasDiagnosisInput() {
        return isChecked(diagNormal) || isChecked(diagPhonology) || isChecked(diagTone);
    }

    private boolean hasSuggestionInput() {
        return isChecked(suggestNone)
                || isChecked(suggestFollow)
                || isChecked(suggestTrad)
                || isChecked(suggestPhono)
                || isChecked(suggestMore)
                || !safeText(getEditTextValue(followMonths)).isEmpty()
                || !safeText(getEditTextValue(tradFreq)).isEmpty()
                || !safeText(getEditTextValue(phonoFreq)).isEmpty();
    }

    private boolean isChecked(CheckBox box) {
        return box != null && box.isChecked();
    }

    private void addCheckedLabel(List<String> items, CheckBox box) {
        if (box != null && box.isChecked()) {
            addText(items, box.getText());
        }
    }

    private void addText(List<String> items, CharSequence text) {
        String value = safeText(text == null ? "" : text.toString());
        if (!value.isEmpty()) {
            items.add(value);
        }
    }

    private String joinText(List<String> items, String separator) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            String text = safeText(item);
            if (text.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(text);
        }
        return builder.toString();
    }

    private String resolvePlOptionText(int buttonId, String fallback) {
        RadioButton button = findViewById(buttonId);
        if (button == null) {
            return fallback;
        }
        String text = safeText(button.getText() == null ? "" : button.getText().toString());
        return text.isEmpty() ? fallback : text;
    }

    private String getEditTextValue(EditText editText) {
        if (editText == null) {
            return "";
        }
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
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

    /**
     * 查找并隐藏文本为"（三）评估建议"的TextView控件
     * @param view
     */
    private void findAndHideEvaluationSuggestionTitle(View view) {
        if (view == null) {
            return;
        }
        try {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    findAndHideEvaluationSuggestionTitle(child);
                }
            } else if (view instanceof TextView) {
                TextView textView = (TextView) view;
                CharSequence textCharSequence = textView.getText();
                if (textCharSequence != null) {
                    String text = textCharSequence.toString();
                    if (text.equals("（三）评估建议")) {
                        textView.setVisibility(View.GONE);
                        // 隐藏该TextView后面的所有兄弟View，直到遇到返回按钮
                        ViewParent parent = view.getParent();
                        if (parent instanceof ViewGroup) {
                            ViewGroup parentGroup = (ViewGroup) parent;
                            int index = parentGroup.indexOfChild(view);
                            if (index >= 0) {
                                for (int i = index + 1; i < parentGroup.getChildCount(); i++) {
                                    View sibling = parentGroup.getChildAt(i);
                                    if (sibling != null) {
                                        // 检查是否是返回按钮
                                        if (sibling.getId() == R.id.back) {
                                            break;
                                        }
                                        sibling.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 捕获任何异常，确保方法不会导致崩溃
            e.printStackTrace();
        }
    }

}
