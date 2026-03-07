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
import bean.se;
import bean.pst;
import bean.nwr;
import bean.pn;
import bean.pl;
import bean.social;
import bean.ev;
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
            // 限制为7道题
            int lenth = Math.min(imageUrls.length, 7);
            testcontext.getInstance().setLengths(lenth);
            JSONArray E = evaluations.optJSONArray("E");
            if (E == null) {
                E = new JSONArray();
                evaluations.put("E", E);
            }
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(imageUrls.length);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            // 加载所有图片资源
            for (int i = 0; i < imageUrls.length; i++) {
                int resId = 0;
                if (i < imageUrls.length) {
                    resId = getResources().getIdentifier(imageUrls[i], "drawable", getPackageName());
                }
                R_id.add(resId);
            }

            if (E.length()==0){//E为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    // 确保索引不越界
                    String target = (i - 1 < imageUrlsC.length) ? imageUrlsC[i - 1] : "";
                    e e1 = new e(i, target, null, null, null);
                    e1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(e1);
                }
            }
            else {//非空，则完成了部分题目
                Boolean result;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (i - 1 < E.length() && E.getJSONObject(i-1).has("result") && !E.getJSONObject(i-1).isNull("result")) {
                        result = E.getJSONObject(i-1).getBoolean("result");
                        audio = new audio(E.getJSONObject(i-1).getString("audioPath"));
                        time = E.getJSONObject(i-1).getString("time");
                    }
                    else{
                        result = null;
                        audio = null;
                        time = null;
                    }
                    // 确保索引不越界
                    String target = (i - 1 < imageUrlsC.length) ? imageUrlsC[i - 1] : "";
                    e e2 = new e(i, target, result, audio, time);
                    e2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(e2);
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.word_test1, R_id, Tb, evTemp, null,null,null,counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals("EV")) {

            String[] imageUrls = ImageUrls.EV_imageUrls;
            String[] imageUrlsC = ImageUrls.EV_imageUrlsC;
            // 限制为7道题
            int lenth = Math.min(imageUrls.length, 7);
            testcontext.getInstance().setLengths(lenth);
            JSONArray EV = evaluations.optJSONArray("EV");
            if (EV == null) {
                EV = new JSONArray();
                evaluations.put("EV", EV);
            }
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>();
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            // 加载所有需要的图片
            for (int i = 0; i < imageUrls.length; i++) {
                int resId = getResources().getIdentifier(imageUrls[i], "drawable", getPackageName());
                R_id.add(resId);
            }

            if (EV.length()==0){//EV为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    // 确保索引不越界
                    String target = (i - 1 < imageUrlsC.length) ? imageUrlsC[i - 1] : "";
                    ev ev1 = new ev(i, target, null, null);
                    ev1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(ev1);
                }
            }
            else {//非空，则完成了部分题目
                Boolean result;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (i - 1 < EV.length() && EV.getJSONObject(i-1).has("result") && !EV.getJSONObject(i-1).isNull("result")) {
                        result = EV.getJSONObject(i-1).getBoolean("result");
                        time = EV.getJSONObject(i-1).getString("time");
                    }
                    else{
                        result = null;
                        time = null;
                    }
                    // 确保索引不越界
                    String target = (i - 1 < imageUrlsC.length) ? imageUrlsC[i - 1] : "";
                    ev ev2 = new ev(i, target, result, time);
                    ev2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(ev2);
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
            // 重置测评状态，确保切换分组时不继承之前的状态
            testcontext.getInstance().resetEvaluationState();
            
            // 获取分组信息，默认为第1组
            int groupNumber = getIntent().getIntExtra("groupNumber", 1);
            // 确保分组编号在有效范围内
            if (groupNumber < 1) groupNumber = 1;
            if (groupNumber > 4) groupNumber = 4;
            
            // 存储分组信息，供后续使用
            testcontext.getInstance().setGroupNumber(groupNumber);
            
            // 根据组别加载对应的句法理解数据
            String[] hints;
            String[][] images;
            String[] answers;
            
            switch (groupNumber) {
                case 4:
                    hints = ImageUrls.SYNTAX_COMPREHENSION_GROUP4_hints;
                    images = ImageUrls.SYNTAX_COMPREHENSION_GROUP4_images;
                    answers = ImageUrls.SYNTAX_COMPREHENSION_GROUP4_answers;
                    break;
                case 3:
                    hints = ImageUrls.SYNTAX_COMPREHENSION_GROUP3_hints;
                    images = ImageUrls.SYNTAX_COMPREHENSION_GROUP3_images;
                    answers = ImageUrls.SYNTAX_COMPREHENSION_GROUP3_answers;
                    break;
                case 2:
                    hints = ImageUrls.SYNTAX_COMPREHENSION_GROUP2_hints;
                    images = ImageUrls.SYNTAX_COMPREHENSION_GROUP2_images;
                    answers = ImageUrls.SYNTAX_COMPREHENSION_GROUP2_answers;
                    break;
                case 1:
                default:
                    hints = ImageUrls.SYNTAX_COMPREHENSION_GROUP1_hints;
                    images = ImageUrls.SYNTAX_COMPREHENSION_GROUP1_images;
                    answers = ImageUrls.SYNTAX_COMPREHENSION_GROUP1_answers;
                    break;
            }
            
            int lenth = hints.length;
            testcontext.getInstance().setLengths(lenth);
            // 为每个组使用单独的JSONArray存储数据
            String rgKey = "RG" + groupNumber;
            JSONArray RG = evaluations.optJSONArray(rgKey);
            if (RG == null) {
                RG = new JSONArray();
                evaluations.put(rgKey, RG);
            }
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (RG.length()==0){//RG为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    rg rg1 = new rg(i, hints[i - 1], answers[i - 1], null, null, -1, null, null);
                    rg1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(rg1);
                    
                    // 根据题目类型加载不同的图片
                    String[] questionImages = images[i - 1];
                    if (questionImages != null && questionImages.length > 0) {
                        // 加载第一张图片作为代表
                        int resId = getResources().getIdentifier(questionImages[0], "drawable", getPackageName());
                        R_id.add(resId);
                    } else {
                        R_id.add(0);
                    }
                }
            }else {//非空，则完成了部分题目
                String answer;
                Boolean result;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (i - 1 < RG.length() && RG.getJSONObject(i-1).has("result") && !RG.getJSONObject(i-1).isNull("result")) {
                        result = RG.getJSONObject(i-1).getBoolean("result");
                        answer = RG.getJSONObject(i-1).getString("answer");
                        audio = new audio(RG.getJSONObject(i-1).getString("audioPath"));
                        time = RG.getJSONObject(i-1).getString("time");
                    } else {
                        answer = null;
                        result = null;
                        audio = null;
                        time = null;
                    }
                    se se2 = new se(i, hints[i - 1], answers[i - 1], answer, result, -1, audio, time);
                    se2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(se2);
                    
                    // 根据题目类型加载不同的图片
                    String[] questionImages = images[i - 1];
                    if (questionImages != null && questionImages.length > 0) {
                        int resId = getResources().getIdentifier(questionImages[0], "drawable", getPackageName());
                        R_id.add(resId);
                    } else {
                        R_id.add(0);
                    }
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.grammar_test1, R_id, Tb, evTemp, null, null, hints, counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (resolvedKey.equals("SE")) {
            // 重置测评状态，确保切换分组时不继承之前的状态
            testcontext.getInstance().resetEvaluationState();
            
            // 获取分组信息，默认为第1组
            int groupNumber = getIntent().getIntExtra("groupNumber", 1);
            // 确保分组编号在有效范围内
            if (groupNumber < 1) groupNumber = 1;
            if (groupNumber > 4) groupNumber = 4;
            
            // 存储分组信息，供后续使用
            testcontext.getInstance().setGroupNumber(groupNumber);
            
            // 根据组别加载对应的句法表达数据
            String[] hints;
            String[][] images;
            String[] answers;
            
            switch (groupNumber) {
                case 4:
                    hints = ImageUrls.SYNTAX_EXPRESSION_GROUP4_hints;
                    images = ImageUrls.SYNTAX_EXPRESSION_GROUP4_images;
                    answers = ImageUrls.SYNTAX_EXPRESSION_GROUP4_answers;
                    break;
                case 3:
                    hints = ImageUrls.SYNTAX_EXPRESSION_GROUP3_hints;
                    images = ImageUrls.SYNTAX_EXPRESSION_GROUP3_images;
                    answers = ImageUrls.SYNTAX_EXPRESSION_GROUP3_answers;
                    break;
                case 2:
                    hints = ImageUrls.SYNTAX_EXPRESSION_GROUP2_hints;
                    images = ImageUrls.SYNTAX_EXPRESSION_GROUP2_images;
                    answers = ImageUrls.SYNTAX_EXPRESSION_GROUP2_answers;
                    break;
                case 1:
                default:
                    hints = ImageUrls.SYNTAX_EXPRESSION_GROUP1_hints;
                    images = ImageUrls.SYNTAX_EXPRESSION_GROUP1_images;
                    answers = ImageUrls.SYNTAX_EXPRESSION_GROUP1_answers;
                    break;
            }
            
            int lenth = hints.length;
            testcontext.getInstance().setLengths(lenth);
            // 为每个组使用单独的JSONArray存储数据
            String seKey = "SE" + groupNumber;
            JSONArray SE = evaluations.optJSONArray(seKey);
            if (SE == null) {
                SE = new JSONArray();
                evaluations.put(seKey, SE);
            }
            evTemp = new ArrayList<evaluation>(lenth);

            ArrayList<Integer> R_id = new ArrayList<Integer>(lenth);
            String[] Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

            if (SE.length()==0){//SE为空，即尚未答题
                for (int i = 1; i <= lenth; i++) {
                    se se1 = new se(i, hints[i - 1], answers[i - 1], null, null, -1, null, null);
                    se1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(se1);
                    
                    // 根据题目类型加载不同的图片
                    String[] questionImages = images[i - 1];
                    if (questionImages != null && questionImages.length > 0) {
                        // 加载第一张图片作为代表
                        int resId = getResources().getIdentifier(questionImages[0], "drawable", getPackageName());
                        R_id.add(resId);
                    } else {
                        R_id.add(0);
                    }
                }
            }else {//非空，则完成了部分题目
                String answer;
                Boolean result;
                audio audio;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (i - 1 < SE.length() && SE.getJSONObject(i-1).has("result") && !SE.getJSONObject(i-1).isNull("result")) {
                        result = SE.getJSONObject(i-1).getBoolean("result");
                        answer = SE.getJSONObject(i-1).getString("answer");
                        audio = new audio(SE.getJSONObject(i-1).getString("audioPath"));
                        time = SE.getJSONObject(i-1).getString("time");
                    }
                    else{
                        answer = null;
                        result = null;
                        audio = null;
                        time = null;
                    }
                    se se2 = new se(i, hints[i - 1], answers[i - 1], answer, result, -1, audio, time);
                    se2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(se2);
                    
                    // 根据题目类型加载不同的图片
                    String[] questionImages = images[i - 1];
                    if (questionImages != null && questionImages.length > 0) {
                        int resId = getResources().getIdentifier(questionImages[0], "drawable", getPackageName());
                        R_id.add(resId);
                    } else {
                        R_id.add(0);
                    }
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.grammar_test1, R_id, Tb, evTemp, null, null, hints, counter, timer);
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

        } else if (resolvedKey.equals("SOCIAL")) {
            // 重置测评状态，确保切换分组时不继承之前的状态
            testcontext.getInstance().resetEvaluationState();
            
            // 获取分组信息，默认为第1组
            int groupNumber = getIntent().getIntExtra("groupNumber", 1);
            // 确保分组编号在有效范围内
            if (groupNumber < 1) groupNumber = 1;
            if (groupNumber > 6) groupNumber = 6;
            
            // 计算该分组的题目范围（每组10题）
            int startIndex = (groupNumber - 1) * 10;
            int endIndex = startIndex + 10;
            int groupLength = endIndex - startIndex;
            
            String[] abilities = ImageUrls.SOCIAL_abilities;
            String[] focuses = ImageUrls.SOCIAL_focuses;
            String[] contents = ImageUrls.SOCIAL_contents;
            
            testcontext.getInstance().setLengths(groupLength);
            JSONArray SOCIAL = evaluations.optJSONArray("SOCIAL");
            if (SOCIAL == null) {
                SOCIAL = new JSONArray();
                evaluations.put("SOCIAL", SOCIAL);
            }
            evTemp = new ArrayList<evaluation>(groupLength);

            String[] Tb = Chinesenumbers.generateChineseNumbersArray(groupLength);

            for (int i = startIndex; i < endIndex; i++) {
                int questionNumber = i + 1;
                social item;
                if (SOCIAL.length() >= questionNumber) {
                    item = social.fromJson(SOCIAL.getJSONObject(questionNumber - 1));
                } else {
                    item = new social(questionNumber, null, null, null, null, null, null, null);
                }
                if (item.getAbility() == null || item.getAbility().trim().isEmpty()) {
                    item.setAbility(abilities[i]);
                }
                if (item.getFocus() == null || item.getFocus().trim().isEmpty()) {
                    item.setFocus(focuses[i]);
                }
                if (item.getContent() == null || item.getContent().trim().isEmpty()) {
                    item.setContent(contents[i]);
                }
                item.setAllQuestionListener(allquestioncallback);
                evTemp.add(item);
            }

            // 存储分组信息，供后续使用
            testcontext.getInstance().setGroupNumber(groupNumber);
            
            testcontext.getInstance().setEvaluations(evTemp);
            adapter = new testpageradapter(R.layout.word_test1, null, Tb, evTemp, null, null, null, counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else {

        }
    }

    @Override
    public void onBackPressed() {
        if(testcontext.getInstance().getCount()< testcontext.getInstance().getLengths())
            dialogUtils.showDialog(this, "提示信息", "您尚未完成测评，是否退出？", "是", () ->{
                // 重置测评状态，确保重新选择组别时题目个数从一开始
                testcontext.getInstance().resetEvaluationState();
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
                    // 重置测评状态，确保重新选择组别时题目个数从一开始
                    testcontext.getInstance().resetEvaluationState();
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
                else if (resolvedKey.equals("EV")) {
                    evaluations.put("EV", emptyArray);
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
                    // 使用组特定的键来清空数组
                    int groupNumber = testcontext.getInstance().getGroupNumber();
                    String rgKey = "RG" + groupNumber;
                    evaluations.put(rgKey, emptyArray);

                } else if (resolvedKey.equals("SE")) {
                    // 使用组特定的键来清空数组
                    int groupNumber = testcontext.getInstance().getGroupNumber();
                    String seKey = "SE" + groupNumber;
                    evaluations.put(seKey, emptyArray);

                } else if (resolvedKey.equals("S")) {
                    evaluations.put("S", emptyArray);

                } else if (isPrelinguistic) {
                    evaluations.put("PL", emptyArray);

                } else if (resolvedKey.equals("SOCIAL")) {
                    // 不要创建空数组覆盖，保留之前的结果
                    if (!evaluations.has("SOCIAL")) {
                        evaluations.put("SOCIAL", new JSONArray());
                    }

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
                } else if (resolvedKey.equals("SOCIAL")) {
                    ArrayList<String> strengths = new ArrayList<>();
                    ArrayList<String> weaknesses = new ArrayList<>();
                    ArrayList<String> inProgress = new ArrayList<>();
                    int totalScore = 0;
                    for (evaluation evaluation : evTemp) {
                        if (!(evaluation instanceof social)) {
                            continue;
                        }
                        social item = (social) evaluation;
                        int score = item.getScore() == null ? 0 : item.getScore();
                        totalScore += score;
                        if (score == 2) {
                            strengths.add(item.getAbility());
                        } else if (score == 1) {
                            inProgress.add(item.getAbility());
                        } else {
                            weaknesses.add(item.getAbility());
                        }
                    }
                    JSONObject report = ModuleReportHelper.buildSocialReport(totalScore, strengths, inProgress, weaknesses);
                    ModuleReportHelper.saveSocialReport(data, report);
                    
                    // 判断是否需要跳转到前一组
                    int currentGroup = testcontext.getInstance().getGroupNumber();
                    if (totalScore < 12 && currentGroup > 1) {
                        // 总分不够12分，跳转到前一组
                        int previousGroup = currentGroup - 1;
                        Intent intent = new Intent(this, testactivity.class);
                        intent.putExtra("fName", fName);
                        intent.putExtra("format", "SOCIAL");
                        intent.putExtra("groupNumber", previousGroup);
                        intent.putExtra("Uid", getIntent().getStringExtra("Uid"));
                        intent.putExtra("childID", getIntent().getStringExtra("childID"));
                        startActivity(intent);
                        // 结束当前活动，避免多个活动实例堆积
                        finish();
                        return;
                    } else {
                        // 总分达到12分或已经是第一组，完成评估
                        // 不做特殊处理，保持当前逻辑
                    }
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
