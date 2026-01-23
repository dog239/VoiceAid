package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import adapter.CustomViewPager;
import adapter.testpageradapter;
import bean.audio;
import bean.e;
import bean.a;
import bean.re;
import bean.rg;
import bean.s;
import bean.pst;
import bean.nwr;
import bean.pn;
import bean.pl;
import bean.evaluation;
import utils.AudioRecorder;
import utils.Chinesenumbers;
import utils.allquestionlistener;
import utils.dataManager;
import utils.dialogUtils;
import utils.ImageUrls;
import utils.ModuleReportHelper;
import utils.testcontext;

public class testactivity extends AppCompatActivity implements View.OnClickListener {
    private CustomViewPager viewPager;
    private testpageradapter adapter;
    private String fName;
    private ArrayList<evaluation> evTemp;
    private TextView exit, counter, timer;
    private String format;
    private String scene;
    private String moduleKey;
    private static final String FORMAT_PL = "11";
    private static final String MODULE_PL = "PL";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        viewPager = findViewById(R.id.viewpager);
        exit = findViewById(R.id.btn_exit);
        counter = findViewById(R.id.counter);
        timer = findViewById(R.id.timer);

        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CurrentQuestion", null);
        editor.apply();


        //必须加载数据
        try {
            initData();
        } catch (Exception e) {
            Toast.makeText(this,"数据加载失败！",Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

        exit.setOnClickListener(this);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int currentPage = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
                if (adapter.getAllowSwipe()){
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(adapter.getAllowSwipe())
                    currentPage = position;
                Log.d("zhxj7034",String.valueOf(currentPage));
                timer.setText(testcontext.getInstance().getEvaluations().get(position).getTime()==null?
                        "00:00": testcontext.getInstance().getEvaluations().get(position).getTime());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 当页面切换完成，检测是否需要跳回到当前页
                if (state == ViewPager.SCROLL_STATE_IDLE && !adapter.getAllowSwipe()) {
                    Log.d("zhxj7034jump",String.valueOf(currentPage));
                    viewPager.setCurrentItem(currentPage, true);
                }
            }
        });
    }



    private void initData() throws Exception {
        testcontext.getInstance().setContext(this);
        testcontext.getInstance().setViewPager(viewPager);

        //获取上个页面传来的信息，即测试哪种题目
        Intent intent = getIntent();
        format = intent.getStringExtra("format");
        moduleKey = intent.getStringExtra("moduleKey");
        scene = intent.getStringExtra("scene");
        if (scene == null || scene.trim().isEmpty()) {
            scene = "A";
        }
        fName = intent.getStringExtra("fName");
        JSONObject evaluations = dataManager.getInstance().loadData(fName).getJSONObject("evaluations");
        String resolvedKey = moduleKey != null ? moduleKey : format;
        if (resolvedKey == null)
            return;
        if (resolvedKey.equals("A")) {
            boolean useNewA = ImageUrls.useNewAPhonology();
            if (useNewA) {
                ImageUrls.initAPhonologyLexicon();
            }
            String[] imageUrls = useNewA ? ImageUrls.A_newImageUrls : ImageUrls.A_imageUrls;
            String[] imageUrlsC = useNewA ? ImageUrls.A_newImageUrlsC : ImageUrls.A_imageUrlsC;
            String[][] target_tone = ImageUrls.A_proAns;
            int lenth = imageUrls.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray A = evaluations.getJSONArray("A");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
            ArrayList<String[]> R_id2 = useNewA ? null : new ArrayList<String[]>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (useNewA) {
                if (A.length() == 0) {
                    for (int i = 1; i <= lenth; i++) {
                        a a1 = new a(i, imageUrlsC[i - 1], null, null, null, null, null);
                        a1.setTargetWord(ImageUrls.toList(ImageUrls.A_targetWord[i - 1]));
                        a1.setAllQuestionListener(allquestioncallback);
                        evTemp.add(a1);
                        R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                    }
                } else {
                    for (int i = 1; i <= lenth; i++) {
                        a a2 = a.fromJson(A.getJSONObject(i - 1));
                        if (a2.getTargetWord() == null) {
                            a2.setTargetWord(ImageUrls.toList(ImageUrls.A_targetWord[i - 1]));
                        }
                        if (a2.getTarget() == null) {
                            a2.setTarget(imageUrlsC[i - 1]);
                        }
                        a2.setAllQuestionListener(allquestioncallback);
                        evTemp.add(a2);
                        R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                    }
                }
                testcontext.getInstance().setEvaluations(evTemp);
                adapter = new testpageradapter(R.layout.pronounciation_test1, R_id, Tb, evTemp, null, null, imageUrlsC, counter, timer);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            } else {
                if (A.length() == 0) {
                    for (int i = 1; i <= lenth; i++) {
                        a a1 = new a(i, imageUrlsC[i - 1], null, target_tone[i - 1][0], target_tone[i - 1][1], null, null);
                        a1.setAllQuestionListener(allquestioncallback);
                        evTemp.add(a1);
                        R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                        R_id2.add(target_tone[i - 1]);
                    }
                } else {
                    String progress;
                    String targettone1;
                    String targettone2;
                    audio audio;
                    String time;
                    for (int i = 1; i <= lenth; i++) {
                        if (A.getJSONObject(i - 1).has("time") && !A.getJSONObject(i - 1).isNull("time") && !A.getJSONObject(i - 1).getString("time").equals("null")) {
                            progress = A.getJSONObject(i - 1).getString("progress");
                            if (A.getJSONObject(i - 1).has("target_tone1") && !A.getJSONObject(i - 1).isNull("target_tone1")) {
                                targettone1 = A.getJSONObject(i - 1).getString("target_tone1");
                            } else {
                                targettone1 = "";
                            }

                            if (A.getJSONObject(i - 1).has("target_tone2") && !A.getJSONObject(i - 1).isNull("target_tone2")) {
                                targettone2 = A.getJSONObject(i - 1).getString("target_tone2");
                            } else {
                                targettone2 = "";
                            }
                            audio = new audio(A.getJSONObject(i - 1).getString("audioPath"));
                            time = A.getJSONObject(i - 1).getString("time");
                        } else {
                            progress = null;
                            targettone1 = null;
                            targettone2 = null;
                            audio = null;
                            time = null;
                        }
                        a a2 = new a(i, imageUrlsC[i - 1], progress, targettone1, targettone2, audio, time);
                        a2.setAllQuestionListener(allquestioncallback);
                        evTemp.add(a2);
                        R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                        R_id2.add(target_tone[i - 1]);
                    }
                }
                testcontext.getInstance().setEvaluations(evTemp);
                adapter = new testpageradapter(R.layout.pronounciation_test1, R_id, Tb, evTemp, null, R_id2, imageUrlsC, counter, timer);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }

        } else if (resolvedKey.equals("E")) {

            String[] imageUrls = ImageUrls.E_imageUrls;
            String[] imageUrlsC = ImageUrls.E_imageUrlsC;
            int lenth = imageUrls.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray E = evaluations.getJSONArray("E");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (E.length()==0){//E为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    e e1 = new e(i, imageUrlsC[i - 1], null, null, null);
                    e1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(e1);
                    R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                }
            }
            else {//非空，则完成了部分题目
                Boolean result;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (E.getJSONObject(i-1).has("result") && !E.getJSONObject(i-1).isNull("result")) {
                        result = E.getJSONObject(i-1).getBoolean("result");
                        audio = new audio(E.getJSONObject(i-1).getString("audioPath"));
                        time = E.getJSONObject(i-1).getString("time");
                    }
                    else{
                        result = null;
                        audio = null;
                        time = null;
                    }
                    e e2 = new e(i, imageUrlsC[i - 1], result, audio, time);
                    e2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(e2);
                    R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.word_test1, R_id, Tb, evTemp, null,null,null,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals("NWR")) {
            String[][] words = ImageUrls.NWR_characs;
            String[][] wordsC = ImageUrls.NWR_characsC;
            int lenth  = words.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray NWR = evaluations.getJSONArray("NWR");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<String []>R_id2 = new ArrayList<String[]>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);
            if (NWR.length()==0){//E为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    Boolean[] ans = new Boolean[6];
                    for(int j=0;j<6;++j){
                        ans[j]=false;
                    }
                    nwr nwr1 = new nwr(i, wordsC[i-1], ans, null, null);
                    nwr1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(nwr1);
                    R_id2.add(words[i-1]);
                }
            }
            else {//非空，则完成了部分题目

                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (NWR.getJSONObject(i-1).has("time") && !NWR.getJSONObject(i-1).isNull("time")) {
                        Boolean[] ans = new Boolean[6];
                        for(int j=0;j<6;++j){
                            ans[j] = NWR.getJSONObject(i-1).getBoolean("results"+(j+1));
                        }
                        audio = new audio(NWR.getJSONObject(i-1).getString("audioPath"));
                        time = NWR.getJSONObject(i-1).getString("time");
                        nwr nwr2 = new nwr(i, wordsC[i-1], ans, audio, time);
                        nwr2.setAllQuestionListener(allquestioncallback);
                        evTemp.add(nwr2);
                    }else{
                        Boolean[] ans = new Boolean[6];
                        for(int j=0;j<6;++j){
                            ans[j]=false;
                        }
                        audio = null;
                        time = null;
                        nwr nwr3 = new nwr(i, wordsC[i-1], ans, audio, time);
                        nwr3.setAllQuestionListener(allquestioncallback);
                        evTemp.add(nwr3);
                    }
                    R_id2.add(words[i-1]);
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.word_test4, null, Tb, evTemp, null,R_id2,null,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals("PN")) {

            String[] imageUrls = ImageUrls.PN_imageUrls;
            String[] pnHints = ImageUrls.PN_hints;
            int lenth = imageUrls.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray PN = evaluations.getJSONArray("PN");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
            ArrayList<String []> R_id2 = new ArrayList<String []>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (PN.length()==0){//E为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {

                    pn pn1 = new pn(i,0,null,null);
                    pn1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(pn1);
                    R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));

                    String[] hint = new String[1];
                    for(int j=0;j<1;++j){
                        hint[j] = getString(getResources().getIdentifier(pnHints[i-1], "string", getPackageName()));
                    }
                    R_id2.add(hint);
                }
            }
            else {//非空，则完成了部分题目
                audio audio;
                String time;
                int score;
                for (int i = 1; i <= lenth; i++) {
                    if (PN.getJSONObject(i-1).has("time") && !PN.getJSONObject(i-1).isNull("time")) {
                        audio = new audio(PN.getJSONObject(i-1).getString("audioPath"));
                        time = PN.getJSONObject(i-1).getString("time");
                        score = PN.getJSONObject(i-1).getInt("score");
                    }
                    else{
                        audio = null;
                        time = null;
                        score = 0;
                    }
                    pn pn2 =  new pn(i, score, audio, time);
                    pn2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(pn2);
                    String[] hint = new String[1];
                    hint[0] = getString(getResources().getIdentifier(pnHints[i-1], "string", getPackageName()));
                    R_id2.add(hint);
                    R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);
            adapter = new testpageradapter(R.layout.narrate_tests, R_id, Tb, evTemp, null,R_id2,null,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals("PST")) {
            String[] imageUrls = ImageUrls.PST_imageUrls;
            //String[][] pstHints = ImageUrls.PST_hints;
            int lenth = imageUrls.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray PST = evaluations.getJSONArray("PST");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
           // ArrayList<String []> R_id2 = new ArrayList<String []>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (PST.length()==0){//E为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    pst pst1 = new pst(i,0,null,null);
                    pst1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(pst1);
                    R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                }
            }
            else {//非空，则完成了部分题目
                audio audio;
                String time;
                int score;
                for (int i = 1; i <= lenth; i++) {
                    if (PST.getJSONObject(i-1).has("time") && !PST.getJSONObject(i-1).isNull("time")) {
                        audio = new audio(PST.getJSONObject(i-1).getString("audioPath"));
                        time = PST.getJSONObject(i-1).getString("time");
                        score = PST.getJSONObject(i-1).getInt("score");
                    }
                    else{
                        audio = null;
                        time = null;
                        score = 0;
                    }
                    pst pst2 = new pst(i, score, audio, time);
                    pst2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(pst2);
//                    String[] hint = new String[4];
//                    for(int j=0;j<4;++j){
//                        hint[j] = getString(getResources().getIdentifier(pstHints[i-1][j], "string", getPackageName()));
//                    }
//                    R_id2.add(hint);
                    R_id.add(getResources().getIdentifier(imageUrls[i - 1], "drawable", getPackageName()));
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);
            adapter = new testpageradapter(R.layout.narrate_tests, R_id, Tb, evTemp, null,null,null,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());
        } else if (resolvedKey.equals("RE")) {
            String[] imageUrls = ImageUrls.RE_imageUrls;
            String[] imageUrlsC = ImageUrls.RE_imageUrlsC;
            int[][] turn = ImageUrls.RE_turn;
            int lenth = imageUrls.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray RE = evaluations.getJSONArray("RE");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer []> R_id = new ArrayList<Integer[]>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (RE.length()==0){//RE为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    re re1 = new re(i, imageUrls[i - 1], imageUrlsC[i - 1],null, -1, null,null,null);
                    re1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(re1);
                    Integer[] imageUrl = new Integer[6];
                    for(int j=0;j<6;++j){
                        imageUrl[j] = getResources().getIdentifier(imageUrls[turn[i-1][j]], "drawable", getPackageName());
                        Log.d("imageUrl",imageUrls[turn[i-1][j]]);
                    }
                    R_id.add(imageUrl);
                }
            }else {//非空，则完成了部分题目
                Boolean result;
                String select;
                int select_num;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (RE.getJSONObject(i-1).has("result") && !RE.getJSONObject(i-1).isNull("result")) {
                        result = RE.getJSONObject(i-1).getBoolean("result");
                        select = RE.getJSONObject(i-1).getString("select");
                        select_num = RE.getJSONObject(i-1).getInt("select_num");
                        audio = new audio(RE.getJSONObject(i-1).getString("audioPath"));
                        time = RE.getJSONObject(i-1).getString("time");
                    }
                    else{
                        result = null;
                        select = null;
                        select_num = -1;
                        audio = null;
                        time = null;
                    }
                    re re2 = new re(i, imageUrls[i - 1], imageUrlsC[i - 1],select, select_num, result, audio, time);
                    re2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(re2);
                    Integer[] imageUrl = new Integer[6];
                    for(int j=0;j<6;++j){
                        imageUrl[j] = getResources().getIdentifier(imageUrls[turn[i-1][j]], "drawable", getPackageName());
                    }
                    R_id.add(imageUrl);
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.word_test2, null, Tb, evTemp, R_id,null,null,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());
        } else if (resolvedKey.equals("RG")) {

            String[] imageUrlsC = ImageUrls.RG_hints;
            String[] ans = ImageUrls.RG_Ans;
            int lenth = imageUrlsC.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray RG = evaluations.getJSONArray("RG");
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (RG.length()==0){//RE为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    rg rg1 = new rg(i, imageUrlsC[i - 1], ans[i - 1],null, null,-1, null,null);
                    rg1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(rg1);
                    R_id.add(getResources().getIdentifier("g"+String.valueOf(i), "drawable", getPackageName()));
                }
            }else {//非空，则完成了部分题目
                String answer;
                Boolean result;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (RG.getJSONObject(i-1).has("time") && !RG.getJSONObject(i-1).isNull("time")) {
                        result = RG.getJSONObject(i-1).getBoolean("result");
                        answer = RG.getJSONObject(i-1).getString("answer");
                        audio = new audio(RG.getJSONObject(i-1).getString("audioPath"));
                        time = RG.getJSONObject(i-1).getString("time");
                    }
                    else{
                        answer = null;
                        result = null;
                        audio = null;
                        time = null;
                    }
                    rg rg2 = new rg(i, imageUrlsC[i - 1], ans[i - 1],answer, result, -1, audio, time);
                    rg2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(rg2);
                    R_id.add(getResources().getIdentifier("g"+String.valueOf(i), "drawable", getPackageName()));
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.grammar_test1, R_id, Tb, evTemp,null,null,imageUrlsC,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals("S")) {
            String[] sWords = ImageUrls.S_words;
            String[] sWordsAns = ImageUrls.S_wordsAns;
            int lenth = sWords.length;
            testcontext.getInstance().setLengths(lenth);
            JSONArray S = evaluations.getJSONArray("S");
            evTemp = new ArrayList<evaluation>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (S.length()==0){//E为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    s s1 = new s(i, sWords[i - 1], sWordsAns[i - 1], null, null, null);
                    s1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(s1);
                }
            }
            else {//非空，则完成了部分题目
                Boolean result;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (S.getJSONObject(i-1).has("result") && !S.getJSONObject(i-1).isNull("result")) {
                        result = S.getJSONObject(i-1).getBoolean("result");
                        audio = new audio(S.getJSONObject(i-1).getString("audioPath"));
                        time = S.getJSONObject(i-1).getString("time");
                    }
                    else{
                        result = null;
                        audio = null;
                        time = null;
                    }
                    s s2 = new s(i,  sWords[i - 1],sWordsAns[i - 1], result, audio, time);
                    s2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(s2);
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.word_test3, null, Tb, evTemp, null,null,sWords,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals(MODULE_PL) || FORMAT_PL.equals(resolvedKey) || FORMAT_PL.equals(format)) {
            String[] skills = ImageUrls.PL_SKILLS;
            String[] prompts = "B".equals(scene) ? ImageUrls.PL_PROMPTS_B : ImageUrls.PL_PROMPTS_A;
            String[] imageNames = "B".equals(scene) ? ImageUrls.PL_IMAGES_B : ImageUrls.PL_IMAGES_A;
            int length = skills.length;
            testcontext.getInstance().setLengths(length);
            JSONArray PL = evaluations.optJSONArray("PL");
            if (PL == null) {
                PL = new JSONArray();
                evaluations.put("PL", PL);
            }
            evTemp = new ArrayList<evaluation>(length);

            ArrayList<Integer> imageIds = new ArrayList<Integer>(length);
            boolean hasImage = false;
            for (int i = 0; i < length; i++) {
                int resId = 0;
                if (imageNames != null && i < imageNames.length) {
                    String name = imageNames[i];
                    if (name != null && !name.trim().isEmpty()) {
                        resId = getResources().getIdentifier(name, "drawable", getPackageName());
                    }
                }
                imageIds.add(resId);
                if (resId != 0) {
                    hasImage = true;
                }
            }
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(length);

            for (int i = 1; i <= length; i++) {
                pl item;
                if (PL.length() >= i) {
                    item = pl.fromJson(PL.getJSONObject(i - 1));
                } else {
                    item = new pl(i, null, null, null, null, null, null);
                }
                if (item.getSkill() == null || item.getSkill().trim().isEmpty()) {
                    item.setSkill(skills[i - 1]);
                }
                item.setPrompt(prompts[i - 1]);
                item.setAllQuestionListener(allquestioncallback);
                evTemp.add(item);
            }

            testcontext.getInstance().setEvaluations(evTemp);
            adapter = new testpageradapter(R.layout.word_test1, hasImage ? imageIds : null, Tb, evTemp, null, null, null, counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else {

        }
    }

    @Override
    public void onBackPressed() {
        if(testcontext.getInstance().getCount()< testcontext.getInstance().getLengths())
            dialogUtils.showDialog(this, "提示信息", "您尚未完成测评，是否退出？", "是", () ->{
                performCleanup();
                finish();
            }, "否", null);
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        //录音未结束，中断录音
        if(AudioRecorder.getInstance().getAlive()!=null&& AudioRecorder.getInstance().getAlive()){
            int i = viewPager.getCurrentItem();
            SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            String CurrentQuestion = preferences.getString("CurrentQuestion", null);
            int CurrentQustionNumber = preferences.getInt("CurrentQustionNumber",-1);
            if(CurrentQuestion != null && CurrentQustionNumber != -1){
                JSONObject data = null;
                try {
                    data = dataManager.getInstance().loadData(fName);
                    JSONObject evaluations = data.getJSONObject("evaluations");
                    evaluations.getJSONArray(CurrentQuestion).get(CurrentQustionNumber);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            }
            AudioRecorder.getInstance().interruptRecorder();
        }

        //释放单例
        testcontext.getInstance().release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_exit){
            if(testcontext.getInstance().getCount()< testcontext.getInstance().getLengths())
                dialogUtils.showDialog(this, "提示信息", "您尚未完成测评，是否退出？", "是", ()->{
                    performCleanup();
                    finish();
                }, "否", null);
            else {
                performCleanup();
                finish();
            }
        }
    }

    public void performCleanup(){
        //结果写入内存
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject evaluations = data.getJSONObject("evaluations");

            // 创建一个新的空的 JSONArray
            JSONArray emptyArray = new JSONArray();

            String resolvedKey = moduleKey != null ? moduleKey : format;
            boolean isPrelinguistic = MODULE_PL.equals(resolvedKey) || FORMAT_PL.equals(resolvedKey) || FORMAT_PL.equals(format);
            if(resolvedKey!=null){
                if (resolvedKey.equals("A")) {
                    evaluations.put("A", emptyArray);
                }
                else if (resolvedKey.equals("E")) {
                    evaluations.put("E", emptyArray);
                }
                else if (resolvedKey.equals("NWR")) {
                    evaluations.put("NWR", emptyArray);

                } else if (resolvedKey.equals("PN")) {
                    evaluations.put("PN", emptyArray);

                } else if (resolvedKey.equals("PST")) {
                    evaluations.put("PST", emptyArray);

                } else if (resolvedKey.equals("RE")) {
                    evaluations.put("RE", emptyArray);

                } else if (resolvedKey.equals("RG")) {
                    evaluations.put("RG", emptyArray);

                } else if (resolvedKey.equals("S")) {
                    evaluations.put("S", emptyArray);

                } else if (isPrelinguistic) {
                    evaluations.put("PL", emptyArray);

                } else {

                }
                for(int i = 0; i< evTemp.size(); i++){
                    evTemp.get(i).toJson(evaluations);
                }
                if (isPrelinguistic) {
                    ArrayList<String> strengths = new ArrayList<>();
                    ArrayList<String> weaknesses = new ArrayList<>();
                    int totalScore = 0;
                    for (evaluation evaluation : evTemp) {
                        if (!(evaluation instanceof pl)) {
                            continue;
                        }
                        pl item = (pl) evaluation;
                        int score = item.getScore() == null ? 0 : item.getScore();
                        if (score == 1) {
                            strengths.add(item.getSkill());
                            totalScore++;
                        } else {
                            weaknesses.add(item.getSkill());
                        }
                    }
                    JSONObject report = ModuleReportHelper.buildPrelinguisticReport(scene, totalScore, strengths, weaknesses);
                    ModuleReportHelper.savePrelinguisticReport(data, report);
                }
                dataManager.getInstance().saveData(fName,data);
            }

        } catch (Exception e) {
            Toast.makeText(this,"保存失败！",Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }


    utils.allquestionlistener allquestioncallback = new allquestionlistener(){
        @Override
        public void onAllQuestionComplete() {
            performCleanup();
        }
    };


}
