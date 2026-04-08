package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;

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
import utils.net.NetService;
import utils.net.NetServiceProvider;

public class testactivity extends AppCompatActivity implements View.OnClickListener {
    private CustomViewPager viewPager;
    private testpageradapter adapter;
    private String fName;
    private ArrayList<evaluation> evTemp;
    private TextView exit, counter, timer;
    private String format;
    private String scene;
    private String moduleKey;
    private String resolvedKey;
    private NetService netService;
    private String uid;
    private String childUser;
    private boolean isServerBacked;
    private static final String FORMAT_PL = "11";
    private static final String MODULE_PL = "PL";
    
    // 所有题型的监听器
    private utils.allquestionlistener allquestioncallback = new allquestionlistener(){
        @Override
        public void onAllQuestionComplete() {
            // 完成整个组的测试后，清除保存的位置
            if (fName != null) {
                SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("CurrentQuestion");
                editor.apply();
            }
            performCleanup(true);
            // 不再直接关闭活动，而是让performCleanup方法来处理后续的逻辑
        }
    };

    private NetService netService;
    private String uid;
    private String childUserId;
    private boolean uploadRequested;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        viewPager = findViewById(R.id.viewpager);
        // 减少预加载页面数量，只预加载相邻的一个页面
        viewPager.setOffscreenPageLimit(1);
        exit = findViewById(R.id.btn_exit);
        counter = findViewById(R.id.counter);
        timer = findViewById(R.id.timer);

        uid = getIntent().getStringExtra("Uid");
        childUserId = getIntent().getStringExtra("childID");
        netService = NetServiceProvider.get(this);
        if (netService != null) {
            netService.setUploadEvaluationCallback(childUserID -> childUserId = childUserID);
        }

        // 显示加载界面
        View loadingView = findViewById(R.id.loading_view);
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        
        // 隐藏ViewPager，直到数据加载完成
        viewPager.setVisibility(View.GONE);
        
        // 直接在主线程中执行initData，因为它包含UI操作
        try {
            // 执行数据加载和UI初始化
            initData();
            
            // 隐藏加载界面
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }
            
            // 显示ViewPager
            viewPager.setVisibility(View.VISIBLE);
            
            // 检查是否是社交模块或前语言模块，如果是则隐藏计时器
            if (this.resolvedKey != null && (this.resolvedKey.equals("SOCIAL") || MODULE_PL.equals(this.resolvedKey) || FORMAT_PL.equals(this.resolvedKey) || FORMAT_PL.equals(format))) {
                if (timer != null) {
                    timer.setVisibility(View.GONE);
                }
            }
            
            // 数据加载成功，显示提示
            Toast.makeText(this, "数据加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "数据加载失败！", Toast.LENGTH_SHORT).show();
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
                // 先更新当前页面位置
                if(adapter.getAllowSwipe())
                    currentPage = position;
                Log.d("zhxj7034",String.valueOf(currentPage));
                // 停止当前计时器
                AudioRecorder.getInstance().stopRecorder();
                // 停止当前题目的计时器
                if (testcontext.getInstance().getEvaluations() != null && position < testcontext.getInstance().getEvaluations().size()) {
                    evaluation currentEval = testcontext.getInstance().getEvaluations().get(position);
                    currentEval.stopTimer();
                }
                // 重置计时器为0，确保每一题都从0开始计时
                timer.setText("00:00");
                // 对于词汇理解（EV）模块，重新启动计时器
                if (testactivity.this.resolvedKey != null && testactivity.this.resolvedKey.equals("EV")) {
                    // 由于ViewPager预加载机制，需要确保计时器在页面实际显示时重新启动
                    // 这里我们通过延迟执行来确保计时器在页面完全显示后启动
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 重新创建计时器并启动
                            if (testcontext.getInstance().getEvaluations() != null && position < testcontext.getInstance().getEvaluations().size()) {
                                evaluation currentEval = testcontext.getInstance().getEvaluations().get(position);
                                // 这里我们需要重新启动计时器
                                // 由于ev类的计时器是在test方法中启动的，我们需要确保它在页面显示时重新启动
                                // 但由于test方法已经执行过，我们需要通过其他方式启动计时器
                                // 这里我们直接在Activity中启动一个临时计时器
                                final Handler tempHandler = new Handler();
                                final long startTime = System.currentTimeMillis();
                                tempHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        long currentTime = System.currentTimeMillis();
                                        long elapsedTime = currentTime - startTime;
                                        int minutes = (int) (elapsedTime / 60000);
                                        int seconds = (int) ((elapsedTime % 60000) / 1000);
                                        String timeString = String.format("%02d:%02d", minutes, seconds);
                                        timer.setText(timeString);
                                        // 检查页面是否仍然是当前页面
                                        if (viewPager.getCurrentItem() == position) {
                                            tempHandler.postDelayed(this, 1000);
                                        } else {
                                            tempHandler.removeCallbacks(this);
                                        }
                                    }
                                }, 1000);
                            }
                        }
                    }, 100);
                }
                // 更新进度显示
                String key = testactivity.this.resolvedKey;
                if (key != null && key.equals("SOCIAL")) {
                    // 对于SOCIAL模块，直接基于当前位置计算进度
                    int completedCount = position + 1;
                    int groupLength = 10; // 固定每组10题
                    counter.setText(completedCount + "/" + groupLength);
                } else {
                    // 其他模块使用原来的方式
                    counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
                }
                // 保存当前位置到 SharedPreferences，为每个模块使用不同的键
            String currentQuestionKey = "CurrentQuestion";
            // 如果是 RG、SE、SOCIAL、E 或 EV 类型，添加模块信息到键中
            if (key != null && (key.equals("RG") || key.equals("SE") || key.equals("SOCIAL") || key.equals("E") || key.equals("EV"))) {
                Integer groupNumber = testcontext.getInstance().getGroupNumber();
                if (groupNumber != null) {
                    currentQuestionKey = "CurrentQuestion_" + key + groupNumber;
                } else {
                    currentQuestionKey = "CurrentQuestion_" + key;
                }
            }
                // 只在位置变化时保存，减少IO操作
                SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
                String lastSavedPosition = preferences.getString(currentQuestionKey, null);
                if (lastSavedPosition == null || !lastSavedPosition.equals(String.valueOf(position))) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(currentQuestionKey, String.valueOf(position));
                    editor.apply(); // 使用apply()异步保存，避免阻塞主线程
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 当页面切换完成，检测是否需要跳回到当前页
                if (state == ViewPager.SCROLL_STATE_IDLE && !testcontext.getInstance().getAllowSwipe()) {
                    Log.d("zhxj7034jump",String.valueOf(currentPage));
                    viewPager.setCurrentItem(currentPage, true);
                }
            }
        });
    }



    private void initData() throws Exception {
        // 初始化testcontext，确保每次测评都有独立的状态
        testcontext.getInstance().setContext(this);
        testcontext.getInstance().setViewPager(viewPager);
        testcontext.getInstance().setModuleKey(this.resolvedKey);

        //获取上个页面传来的信息，即测试哪种题目
        Intent intent = getIntent();
        format = intent.getStringExtra("format");
        moduleKey = intent.getStringExtra("moduleKey");
        scene = intent.getStringExtra("scene");
        if (scene == null || scene.trim().isEmpty()) {
            scene = "A";
        }
        fName = intent.getStringExtra("fName");
        uid = intent.getStringExtra("Uid");
        childUser = intent.getStringExtra("childID");
        SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        if (uid == null || uid.isEmpty()) {
            uid = loginPrefs.getString("Uid", null);
        }
        // 如果上游没透传 childID，就尝试从当前文件名映射恢复
        if (fName != null && (childUser == null || childUser.isEmpty())) {
            childUser = loginPrefs.getString("childID_" + fName, null);
        }
        isServerBacked = childUser != null && !childUser.isEmpty();
        netService = (uid != null && !uid.isEmpty()) ? NetServiceProvider.get(this) : null;
        JSONObject data = dataManager.getInstance().loadData(fName);
        JSONObject evaluations;
        if (data.has("evaluations")) {
            evaluations = data.getJSONObject("evaluations");
        } else {
            // 如果没有evaluations字段，创建一个新的
            evaluations = new JSONObject();
            try {
                // 为所有模块创建空的JSONArray
                evaluations.put("A", new JSONArray());
                evaluations.put("E", new JSONArray());
                evaluations.put("NWR", new JSONArray());
                evaluations.put("PN", new JSONArray());
                evaluations.put("PL", new JSONArray());
                evaluations.put("PST", new JSONArray());
                evaluations.put("RE", new JSONArray());
                evaluations.put("RG", new JSONArray());
                evaluations.put("SE", new JSONArray());
                evaluations.put("S", new JSONArray());
                evaluations.put("SOCIAL", new JSONArray());
                // 为每个社交能力测试组创建独立的JSONArray
                for (int i = 1; i <= 6; i++) {
                    evaluations.put("SOCIAL" + i, new JSONArray());
                }
                // 为每个句法理解测试组创建独立的JSONArray
                for (int i = 1; i <= 4; i++) {
                    evaluations.put("RG" + i, new JSONArray());
                }
                // 为每个句法表达测试组创建独立的JSONArray
                for (int i = 1; i <= 4; i++) {
                    evaluations.put("SE" + i, new JSONArray());
                }
                // 保存回数据文件
                data.put("evaluations", evaluations);
                dataManager.getInstance().saveChildJson(fName, data);
            } catch (JSONException e) {
                e.printStackTrace();
                // 如果创建失败，使用空的JSONObject
                evaluations = new JSONObject();
            }
        }
        this.resolvedKey = moduleKey != null ? moduleKey : format;
            if (this.resolvedKey == null)
                return;
        if (this.resolvedKey.equals("A")) {
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
                  // 从SharedPreferences读取之前的位置
                  SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                  String savedPosition = preferences.getString("CurrentQuestion", null);
                  if (savedPosition != null) {
                      try {
                          int position = Integer.parseInt(savedPosition);
                          if (position >= 0 && position < adapter.getCount()) {
                              viewPager.setCurrentItem(position);
                          } else {
                              viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                          }
                      } catch (NumberFormatException e) {
                          viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                      }
                  } else {
                      viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                  }
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
                  // 从SharedPreferences读取之前的位置
                  SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                  String savedPosition = preferences.getString("CurrentQuestion", null);
                  if (savedPosition != null) {
                      try {
                          int position = Integer.parseInt(savedPosition);
                          if (position >= 0 && position < adapter.getCount()) {
                              viewPager.setCurrentItem(position);
                          } else {
                              viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                          }
                      } catch (NumberFormatException e) {
                          viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                      }
                  } else {
                      viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                  }
            }

        } else if (this.resolvedKey.equals("E")) {

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
                    e e1 = new e(i, target, null, null);
                    e1.setAllQuestionListener(allquestioncallback);
                    evTemp.add(e1);
                }
            }
            else {//非空，则完成了部分题目
                Boolean result;
                String time;
                for (int i = 1; i <= lenth; i++) {
                    if (i - 1 < E.length() && E.getJSONObject(i-1).has("result") && !E.getJSONObject(i-1).isNull("result")) {
                        result = E.getJSONObject(i-1).getBoolean("result");
                        time = E.getJSONObject(i-1).getString("time");
                    }
                    else{
                        result = null;
                        time = null;
                    }
                    // 确保索引不越界
                    String target = (i - 1 < imageUrlsC.length) ? imageUrlsC[i - 1] : "";
                    e e2 = new e(i, target, result, time);
                    e2.setAllQuestionListener(allquestioncallback);
                    evTemp.add(e2);
                }
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.word_test1, R_id, Tb, evTemp, null,null,null,counter, timer);
            viewPager.setAdapter(adapter);
            // 计算已完成的题目数量
            int completedCount = testcontext.getInstance().searchOne();
            // 更新进度显示
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String currentQuestionKey = "CurrentQuestion_" + this.resolvedKey;
            String savedPosition = preferences.getString(currentQuestionKey, null);
            // 检查是否是新的测评（没有保存的位置且没有完成的题目）
            if (savedPosition == null && testcontext.getInstance().getCount() == 0) {
                // 新的测评，从第一题开始
                viewPager.setCurrentItem(0);
            } else if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        // 保存的位置无效，从第一个未完成的题目开始
                        viewPager.setCurrentItem(completedCount);
                    }
                } catch (NumberFormatException e) {
                    // 保存的位置格式错误，从第一个未完成的题目开始
                    viewPager.setCurrentItem(completedCount);
                }
            } else {
                // 有完成的题目但没有保存的位置，从第一个未完成的题目开始
                viewPager.setCurrentItem(completedCount);
            }

        } else if (this.resolvedKey.equals("EV")) {

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
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String currentQuestionKey = "CurrentQuestion_" + this.resolvedKey;
            String savedPosition = preferences.getString(currentQuestionKey, null);
            if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }

        } else if (this.resolvedKey.equals("NWR")) {
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
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String savedPosition = preferences.getString("CurrentQuestion", null);
            if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }

        } else if (this.resolvedKey.equals("PN")) {

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
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String savedPosition = preferences.getString("CurrentQuestion", null);
            if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }

        } else if (this.resolvedKey.equals("PST")) {
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
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            String savedPosition = preferences.getString("CurrentQuestion", null);
            if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }
        } else if (this.resolvedKey.equals("RE")) {
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
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            String savedPosition = preferences.getString("CurrentQuestion", null);
            if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }
        } else if (this.resolvedKey.equals("RG")) {
            // 获取分组信息，默认为第1组
            int groupNumber = getIntent().getIntExtra("groupNumber", 1);
            // 确保分组编号在有效范围内
            if (groupNumber < 1) groupNumber = 1;
            if (groupNumber > 4) groupNumber = 4;
            
            // 存储分组信息，供后续使用
            testcontext.getInstance().setGroupNumber(groupNumber);
            
            // 根据组别加载对应的句法理解数据
            String[] hints = null;
            String[][] images = null;
            String[] answers = null;
            ArrayList<Integer> R_id = new ArrayList<Integer>();
            String[] Tb = null;
            
            try {
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
                
                if (hints == null || answers == null || images == null) {
                    Toast.makeText(this, "加载数据失败！", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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

                R_id = new ArrayList<Integer>(lenth);
                Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

                if (RG.length()==0){//RG为空，即尚未答题
                    for (int i = 1; i <= lenth; i++) {
                        if (i - 1 < hints.length && i - 1 < answers.length && i - 1 < images.length) {
                            rg rg1 = new rg(i, "", String.valueOf(i), hints[i - 1], "", answers[i - 1], "", null, null, null, "");
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
                    }
                }else {//非空，则完成了部分题目
                    String answer;
                    Boolean result;
                    audio audio;
                    String time;
                    for (int i = 1; i <= lenth; i++) {
                        if (i - 1 < hints.length && i - 1 < answers.length && i - 1 < images.length) {
                            if (i - 1 < RG.length() && RG.getJSONObject(i-1).has("result") && !RG.getJSONObject(i-1).isNull("result")) {
                                result = RG.getJSONObject(i-1).getBoolean("result");
                                answer = RG.getJSONObject(i-1).getString("answer");
                                if (RG.getJSONObject(i-1).has("audioPath") && !RG.getJSONObject(i-1).isNull("audioPath")) {
                                    audio = new audio(RG.getJSONObject(i-1).getString("audioPath"));
                                } else {
                                    audio = null;
                                }
                                time = RG.getJSONObject(i-1).getString("time");
                            } else {
                                answer = null;
                                result = null;
                                audio = null;
                                time = null;
                            }
                            rg rg2 = new rg(i, "", String.valueOf(i), hints[i - 1], "", answer, result != null ? result.toString() : "", null, audio, time, "");
                            rg2.setAllQuestionListener(allquestioncallback);
                            evTemp.add(rg2);
                            
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
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "加载数据失败！", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.grammar_test1, R_id, Tb, evTemp, null, null, hints, counter, timer);
            viewPager.setAdapter(adapter);
            // 计算已完成的题目数量
            testcontext.getInstance().searchOne();
            // 更新进度显示
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String currentQuestionKey = "CurrentQuestion_RG" + groupNumber;
            String savedPosition = preferences.getString(currentQuestionKey, null);
            // 检查是否是新的测评（没有保存的位置且没有完成的题目）
            if (savedPosition == null && testcontext.getInstance().getCount() == 0) {
                // 新的测评，从第一题开始
                viewPager.setCurrentItem(0);
            } else if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        // 保存的位置无效，从第一个未完成的题目开始
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    // 保存的位置格式错误，从第一个未完成的题目开始
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                // 有完成的题目但没有保存的位置，从第一个未完成的题目开始
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }

        } else if (this.resolvedKey.equals("SE")) {
            // 获取分组信息，默认为第1组
            int groupNumber = getIntent().getIntExtra("groupNumber", 1);
            // 确保分组编号在有效范围内
            if (groupNumber < 1) groupNumber = 1;
            if (groupNumber > 4) groupNumber = 4;
            
            // 存储分组信息，供后续使用
            testcontext.getInstance().setGroupNumber(groupNumber);
            
            // 根据组别加载对应的句法表达数据
            String[] hints = null;
            String[][] images = null;
            String[] answers = null;
            ArrayList<Integer> R_id = new ArrayList<Integer>();
            String[] Tb = null;
            
            try {
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
                
                if (hints == null || answers == null || images == null) {
                    Toast.makeText(this, "加载数据失败！", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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

                R_id = new ArrayList<Integer>(lenth);
                Tb = Chinesenumbers.generateChineseNumbersArray(lenth);

                // 初始化题目计数器，用于实际测试题目的编号
                int questionCount = 1;
                
                if (SE.length()==0){//SE为空，即尚未答题
                    for (int i = 1; i <= lenth; i++) {
                        if (i - 1 < hints.length && i - 1 < answers.length && i - 1 < images.length) {
                            // 检查是否为示例题目
                            boolean isExample = false;
                            if (groupNumber == 1) {
                                // 第一组示例题目位置
                                // 示例题目应该是位置0、4、11（对应questionNumber 1、5、12）
                                // 但根据用户的要求，这些题目应该是真实题目，所以不标记为示例
                                isExample = false;
                            } else if (groupNumber == 2) {
                                // 第二组示例题目位置
                                isExample = (i == 4 || i == 8 || i == 12);
                            } else if (groupNumber == 4) {
                                // 第四组示例题目位置
                                isExample = (i == 4 || i == 8 || i == 12 || i == 16);
                            }
                            
                            // 为实际测试题目分配题号，示例题目不占用题号
                            String questionNum = isExample ? "示例" : String.valueOf(questionCount);
                            if (!isExample) {
                                questionCount++;
                            }
                            
                            se se1 = new se(i, "第" + groupNumber + "组", questionNum, hints[i - 1], answers[i - 1], null, null, -1, null, null, "");
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
                    }
                }else {//非空，则完成了部分题目
                    String answer;
                    Boolean result;
                    audio audio;
                    String time;
                    String group;
                    String questionNum;
                    String grammarPoint;
                    for (int i = 1; i <= lenth; i++) {
                        if (i - 1 < hints.length && i - 1 < answers.length && i - 1 < images.length) {
                            // 检查是否为示例题目
                            boolean isExample = false;
                            if (groupNumber == 1) {
                                // 第一组示例题目位置
                                // 示例题目应该是位置0、4、11（对应questionNumber 1、5、12）
                                // 但根据用户的要求，这些题目应该是真实题目，所以不标记为示例
                                isExample = false;
                            } else if (groupNumber == 2) {
                                // 第二组示例题目位置
                                isExample = (i == 4 || i == 8 || i == 12);
                            } else if (groupNumber == 4) {
                                // 第四组示例题目位置
                                isExample = (i == 4 || i == 8 || i == 12 || i == 16);
                            }
                            
                            // 为实际测试题目分配题号，示例题目不占用题号
                            if (!isExample) {
                                questionNum = String.valueOf(questionCount);
                                questionCount++;
                            } else {
                                questionNum = "示例";
                            }
                            
                            if (i - 1 < SE.length() && SE.getJSONObject(i-1).has("result") && !SE.getJSONObject(i-1).isNull("result")) {
                                result = SE.getJSONObject(i-1).getBoolean("result");
                                answer = SE.getJSONObject(i-1).getString("answer");
                                if (SE.getJSONObject(i-1).has("audioPath") && !SE.getJSONObject(i-1).isNull("audioPath")) {
                                    audio = new audio(SE.getJSONObject(i-1).getString("audioPath"));
                                } else {
                                    audio = null;
                                }
                                time = SE.getJSONObject(i-1).getString("time");
                                // 读取新增的字段
                                group = SE.getJSONObject(i-1).optString("group", "第" + groupNumber + "组");
                                // 使用计算出的questionNum，而不是从JSON中读取
                                grammarPoint = SE.getJSONObject(i-1).optString("grammarPoint", "");
                            }
                            else{
                                answer = null;
                                result = null;
                                audio = null;
                                time = null;
                                group = "第" + groupNumber + "组";
                                // 使用计算出的questionNum
                                grammarPoint = "";
                            }
                            se se2 = new se(i, group, questionNum, hints[i - 1], answers[i - 1], answer, result != null ? (result ? "正确" : "错误") : null, -1, audio, time, grammarPoint);
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
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "加载数据失败！", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            testcontext.getInstance().setEvaluations(evTemp);

            adapter = new testpageradapter(R.layout.grammar_test1, R_id, Tb, evTemp, null, null, hints, counter, timer);
            viewPager.setAdapter(adapter);
            // 计算已完成的题目数量
            testcontext.getInstance().searchOne();
            // 更新进度显示
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String currentQuestionKey = "CurrentQuestion_SE" + groupNumber;
            String savedPosition = preferences.getString(currentQuestionKey, null);
            // 检查是否是新的测评（没有保存的位置且没有完成的题目）
            if (savedPosition == null && testcontext.getInstance().getCount() == 0) {
                // 新的测评，从第一题开始
                viewPager.setCurrentItem(0);
            } else if (savedPosition != null) {
                try {
                    int position = Integer.parseInt(savedPosition);
                    if (position >= 0 && position < adapter.getCount()) {
                        viewPager.setCurrentItem(position);
                    } else {
                        // 保存的位置无效，从第一个未完成的题目开始
                        viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                    }
                } catch (NumberFormatException e) {
                    // 保存的位置格式错误，从第一个未完成的题目开始
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne());
                }
            } else {
                // 有完成的题目但没有保存的位置，从第一个未完成的题目开始
                viewPager.setCurrentItem(testcontext.getInstance().searchOne());
            }

        } else if (this.resolvedKey.equals("S")) {
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

        } else if (this.resolvedKey.equals(MODULE_PL) || FORMAT_PL.equals(this.resolvedKey) || FORMAT_PL.equals(format)) {
            // 重置测评状态，确保切换场景时不继承之前的状态
            testcontext.getInstance().resetEvaluationState();
            
            // 设置场景信息到testcontext
            testcontext.getInstance().setScene(scene);
            
            String[] skills = ImageUrls.PL_SKILLS;
            String[] prompts = "B".equals(scene) ? ImageUrls.PL_PROMPTS_B : ImageUrls.PL_PROMPTS_A;
            String[] imageNames = "B".equals(scene) ? ImageUrls.PL_IMAGES_B : ImageUrls.PL_IMAGES_A;
            int length = skills.length;
            testcontext.getInstance().setLengths(length);
            // 为不同场景使用不同的JSONArray键
            String plKey = "PL_" + scene;
            JSONArray PL = evaluations.optJSONArray(plKey);
            if (PL == null) {
                PL = new JSONArray();
                evaluations.put(plKey, PL);
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
            adapter = new testpageradapter(R.layout.word_test1, null, Tb, evTemp, null, null, null, counter, timer);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(testcontext.getInstance().searchOne());

        } else if (this.resolvedKey.equals("SOCIAL")) {
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
            // 固定使用10作为每组题目数量
            int groupLength = 10;
            
            // 直接定义完整的社交能力评估数据
            String[][] groupData = {
                // 第一组
                {
                    "轮流互动",
                    "轮流互动",
                    "孩子在和大人玩耍时，会轮流发出声音或做动作\n例，你拍手，孩子也拍手；或者你说\"啊\"，孩子也回应。"
                },
                {
                    "互动期待",
                    "互动期待",
                    "在互动中，孩子会看向你，好像在等你回应\n例，玩耍时，孩子会看看你，好像在确认\"你看到了吗\"。"
                },
                {
                    "回应性共同关注",
                    "回应性共同关注",
                    "当你指着某样东西或看向某处时，孩子会顺着你的指向一起看\n例，你指小狗或玩具车，孩子也转头看过去。"
                },
                {
                    "意图性沟通",
                    "意图性沟通",
                    "孩子会用手势、声音或者拉人来表达需求\n例，想要东西时，会指、拉大人、发出声音引起注意。"
                },
                {
                    "发起共同关注",
                    "发起共同关注",
                    "孩子会把自己感兴趣的东西拿给你看\n例，孩子会举起玩具给你看或者递给你，而不只是自己玩。"
                },
                {
                    "行为模仿",
                    "行为模仿",
                    "看到你或别人做动作时，孩子会模仿\n例，别的小朋友拍手、挥手，孩子也会跟着做。"
                },
                {
                    "情绪觉察",
                    "情绪觉察",
                    "孩子对他人的情绪有基本反应\n例，别的孩子哭了，孩子会停下来看看，或者看向大人。"
                },
                {
                    "早期语音/词汇模仿",
                    "早期语音/词汇模仿",
                    "孩子会模仿你说的词语或简单表达\n例，大人说\"车车\"\"汪汪\"，孩子会跟着说或发声模仿。"
                },
                {
                    "早期同伴注意与互动萌芽",
                    "早期同伴注意与互动萌芽",
                    "和其他孩子在一起时，会有短暂互动\n例，和别的小朋友你看我、我看你，笑一下、做个动作。"
                },
                {
                    "社交动机",
                    "社交动机",
                    "当你停止互动时，孩子会表现出想继续玩的样子\n例，孩子不想结束游戏时，会拉你、看你、发声表示\"还要\"。"
                },
                // 第二组
                {
                    "常规性的社交互动行为",
                    "常规性的社交互动行为",
                    "孩子会用手势或简单语言向人打招呼或说再见\n例，孩子会说\"你好\"\"拜拜\"，或者挥手。"
                },
                {
                    "对非语言指令的理解",
                    "对非语言指令的理解",
                    "当大人拍手、招手或示意时，孩子能理解并做出简单回应\n例，大人招手示意孩子过来，孩子会看向大人并且走过去。"
                },
                {
                    "对语言指令的理解",
                    "对语言指令的理解",
                    "当你提出简单要求或指令时，孩子能理解并尝试去做\n例，能听懂\"给我\"\"拿过来\"等指令并照做。"
                },
                {
                    "规则性轮流互动",
                    "规则性轮流互动",
                    "孩子能参与简单轮流游戏\n例，比如你扔球给孩子，孩子会接球再扔回去。"
                },
                {
                    "意图的明确表达",
                    "意图的明确表达",
                    "孩子会表达想玩或不想玩的意愿\n例，会说\"我要\"\"不要\"\"不想玩\"，或者用动作表示。"
                },
                {
                    "自我情绪觉察与表达",
                    "自我情绪觉察与表达",
                    "孩子会用简单方式表达基本感受（开心、生气、害怕等）\n例，如说\"开心\"\"我不高兴\"，或者皱眉、拍手表示情绪。"
                },
                {
                    "发起与回应社交互动",
                    "发起与回应社交互动",
                    "孩子会尝试做简单邀请或回应别人邀请\n例，会拉人或说\"来\"，尝试让别人一起玩；或者有人说\"一起玩吧\"，孩子会跟过去或加入。"
                },
                {
                    "社交注视与互动",
                    "社交注视与互动",
                    "和别人互动时，孩子会注视对方的脸并做出反应\n例，说话或玩耍时会看着对方，模仿表情或动作。"
                },
                {
                    "早期假装游戏中的社交参与",
                    "早期假装游戏中的社交参与",
                    "孩子能在成人引导下参与具有简单情境的假装互动游戏\n例，大人假装给娃娃喂饭、打电话或睡觉时，孩子会模仿相关动作，或用声音、词语进行回应，与大人形成简单互动。"
                },
                {
                    "早期个人经历叙述",
                    "早期个人经历叙述",
                    "在大人的支持下，孩子能讲述简单经历或片段\n例，比如\"公园\"\"滑梯\"\"妈妈\"，能说出自己做过的事情或看到的东西。"
                },
                // 第三组
                {
                    "同伴互动中的分享与轮流",
                    "同伴互动中的分享与轮流",
                    "孩子能与同伴一起玩玩具或游戏，并理解轮流概念\n例，知道\"轮到我 / 轮到你\"，能把玩具给别人再拿回来玩。"
                },
                {
                    "规则意识与简单合作",
                    "规则意识与简单合作",
                    "孩子在游戏中能遵守基本规则并与他人合作\n例，玩游戏时，知道要按规则来，比如不能一直自己玩、不让别人玩。"
                },
                {
                    "社交主动性及合作规划",
                    "社交主动性及合作规划",
                    "孩子会主动与同伴交流想法和计划活动\n例，一起搭积木，一起玩过家家，会说\"我们一起搭房子\"。"
                },
                {
                    "情绪识别与早期共情能力",
                    "情绪识别与早期共情能力",
                    "孩子会看出别人明显的情绪并作出简单回应\n例，看到别人难过，会停下来看看，或者问\"你怎么了？\"。"
                },
                {
                    "情绪归因与初级推理能力",
                    "情绪归因与初级推理能力",
                    "在成人提问下，孩子能说出别人为什么会有这种情绪\n例，大人问\"他为什么哭了？\"，孩子回答\"他摔了\"或\"玩具坏了\"。"
                },
                {
                    "冲突情境下的自我表达与求助",
                    "冲突情境下的自我表达与求助",
                    "孩子能够尝试用语言解决小冲突，但可能需要成人帮助\n例，发生争抢时，会说\"这是我的\"\"我先玩\"，但还需要大人帮忙。"
                },
                {
                    "集体情境下的社会参与行为调节",
                    "集体情境下的社会参与行为调节",
                    "孩子在集体活动中能保持注意并参与\n例，上课、集体游戏时，能跟着做，不会马上走开。"
                },
                {
                    "社交情境中的语言表达",
                    "社交情境中的语言表达",
                    "孩子会用完整句子描述自己在做什么或想什么\n例，会说类似\"我在画画\"\"我有新车\"来表达想法。"
                },
                {
                    "寻求帮助",
                    "寻求帮助",
                    "当互动中出现不明白或困难时，孩子能主动向大人或同伴寻求帮助\n例，会说\"这个怎么弄？\"、\"我不会\"，寻求帮助。"
                },
                {
                    "初步心智解读",
                    "初步心智解读",
                    "孩子能初步理解别人想法或兴趣，并作出回应\n例，孩子玩积木时，别的小朋友走过来靠近他，孩子会说\"一起玩吧\"，或把积木递给同伴。"
                },
                // 第四组
                {
                    "合作性假装游戏能力",
                    "合作性假装游戏能力",
                    "孩子在游戏中分配或者接受角色\n例，玩\"过家家\"或者\"买东西\"时，会说谁当什么、怎么玩。"
                },
                {
                    "情绪识别与共情能力",
                    "情绪识别与共情能力",
                    "孩子能理解并尊重他人的表情和情绪\n例，知道别人不开心时，不能继续闹或抢。"
                },
                {
                    "对话的轮流与规则意识",
                    "对话的轮流与规则意识",
                    "孩子能理解并按照社交模式轮流说话\n例，知道别人说完再说，不总是插话。"
                },
                {
                    "维持同伴互动与游戏的能力",
                    "维持同伴互动与游戏的能力",
                    "孩子能与同伴维持互动与游戏\n例，和朋友一起玩玩具、跑、追，能玩一会儿，不会很快离开。"
                },
                {
                    "叙事中的情感理解与推理",
                    "叙事中的情感理解与推理",
                    "孩子能理解故事中的人物感受\n例，听故事时，能知道故事里的人是开心、难过还是生气。"
                },
                {
                    "心智理论萌发",
                    "心智理论萌发",
                    "孩子开始理解别人的想法可能和自己不同\n例，知道别人可能喜欢自己不喜欢的东西，或想要自己不想给的玩具。"
                },
                {
                    "情境理解与行为调节",
                    "情境理解与行为调节",
                    "孩子能理解社交规则并调整行为\n例，知道在不同场合（如吃饭、上课）应该怎么做，会安静、排队等。"
                },
                {
                    "在支持下解决冲突",
                    "在支持下解决冲突",
                    "孩子能在大人的帮助下解决冲突\n例，争抢时，大人提醒后会分享或轮流玩。"
                },
                {
                    "社交发起与组织能力",
                    "社交发起与组织能力",
                    "孩子会主动邀请他人一起玩\n例，看到别的小朋友，会说\"来玩\"或拉他们一起。"
                },
                {
                    "群体会话能力",
                    "群体会话能力",
                    "孩子能加入3人以上的群体会话\n例，在聚会或课堂讨论中，能轮流发言、回应他人。"
                },
                // 第五组
                {
                    "规则意识与自控力",
                    "规则意识与自控力",
                    "孩子能遵守集体规则\n例，上课、排队、游戏时，能按规则做，不需要频繁提醒。"
                },
                {
                    "维持对话的能力",
                    "维持对话的能力",
                    "孩子能维持一段对话\n例，能回答问题，也会主动提问，对话能持续几个回合。"
                },
                {
                    "亲社会行为与共情能力",
                    "亲社会行为与共情能力",
                    "孩子会关心别人\n例，看到别人受伤或难过，会安慰、帮忙拿东西等。"
                },
                {
                    "高级对话规则",
                    "高级对话规则",
                    "孩子能理解并使用礼貌用语\n例，会说\"请\"\"谢谢\"\"对不起\"等。"
                },
                {
                    "复杂情绪感知与回应",
                    "复杂情绪感知与回应",
                    "孩子能理解复杂情绪（如尴尬、嫉妒、自豪等）\n例，知道别人为什么会尴尬，或自己什么时候会感到自豪。"
                },
                {
                    "目标导向的合作",
                    "目标导向的合作",
                    "孩子能与同伴合作完成任务\n例，一起搭积木、做手工，分工合作。"
                },
                {
                    "初步心智理论",
                    "初步心智理论",
                    "孩子能理解别人的想法和感受\n例，能猜测别人可能在想什么，或为什么会那样做。"
                },
                {
                    "社会问题解决能力",
                    "社会问题解决能力",
                    "孩子能自己解决简单问题\n例，东西够不到时，会搬椅子、找工具，而不是马上找大人。"
                },
                {
                    "意图理解与归因",
                    "意图理解与归因",
                    "孩子能理解别人的意图\n例，知道别人是不小心碰到自己，还是故意的。"
                },
                {
                    "复杂语言组织与规划能力",
                    "复杂语言组织与规划能力",
                    "孩子能组织语言，表达复杂想法\n例，能说长句子，描述过去的事情，或讲简单故事。"
                },
                // 第六组
                {
                    "规则内化与社会责任感",
                    "规则内化与社会责任感",
                    "孩子能自觉遵守规则\n例，即使没有大人在场，也会按规则做，如不闯红灯、不拿别人东西。"
                },
                {
                    "社交互动的自主管理",
                    "社交互动的自主管理",
                    "孩子能自己开始和结束社交互动\n例，会主动交朋友，离开时会说再见。"
                },
                {
                    "有组织的合作能力",
                    "有组织的合作能力",
                    "孩子能组织或参与小组活动\n例，带领朋友玩游戏，分配角色，制定简单规则。"
                },
                {
                    "沟通修复",
                    "沟通修复",
                    "孩子能在沟通中修复误解\n例，别人没听懂时，会换说法、解释，直到对方明白。"
                },
                {
                    "高级情绪理解及共情能力",
                    "高级情绪理解及共情能力",
                    "孩子能理解他人复杂的情绪状态\n例，知道别人可能同时有两种情绪，如又开心又难过。"
                },
                {
                    "社交推理能力",
                    "社交推理能力",
                    "孩子能根据社交情境调整行为\n例，在不同场合（如家里、学校、公共场合）表现得体。"
                },
                {
                    "社会性问题解决与策略",
                    "社会性问题解决与策略",
                    "孩子能解决更复杂的社交问题\n例，能协商、妥协，找到大家都满意的解决方案。"
                },
                {
                    "情境化的自我控制",
                    "情境化的自我控制",
                    "孩子能在诱惑或冲突面前控制自己\n例，看到喜欢的玩具，不会马上抢，会等待或商量。"
                },
                {
                    "高阶心智理论",
                    "高阶心智理论",
                    "孩子能理解他人的信念和意图\n例，知道别人可能有错误的信念，或隐藏自己的真实意图。"
                },
                {
                    "协商性沟通与群体决策",
                    "协商性沟通与群体决策",
                    "孩子能参与集体决策\n例，和朋友一起决定玩什么游戏，能提出建议并接受多数决定。"
                }
            };
            
            testcontext.getInstance().setLengths(groupLength);
            evTemp = new ArrayList<evaluation>(groupLength);

            String[] Tb = Chinesenumbers.generateChineseNumbersArray(groupLength);

            // 为每个组使用单独的JSONArray存储数据
            String socialKey = "SOCIAL" + groupNumber;
            JSONArray socialArray = evaluations.optJSONArray(socialKey);
            if (socialArray == null) {
                socialArray = new JSONArray();
                evaluations.put(socialKey, socialArray);
            }
            
            for (int i = startIndex; i < endIndex; i++) {
                int questionNumber = i + 1;
                int groupIndex = i - startIndex;
                String ability = "";
                String focus = "";
                String content = "";
                Integer score = null;
                String observation = null;
                String time = null;
                String audioPath = null;
                
                // 确保索引不超出数组范围
                if (i >= 0 && i < groupData.length) {
                    ability = groupData[i][0];
                    focus = groupData[i][1];
                    content = groupData[i][2];
                }
                
                // 尝试从JSONArray中加载已有的数据
                if (groupIndex < socialArray.length()) {
                    JSONObject itemObj = socialArray.optJSONObject(groupIndex);
                    if (itemObj != null) {
                        score = itemObj.optInt("score", -1);
                        if (score == -1) score = null;
                        observation = itemObj.optString("observation", null);
                        time = itemObj.optString("time", null);
                        audioPath = itemObj.optString("audioPath", null);
                    }
                }
                
                // 创建新的social对象
                social item = new social(questionNumber, ability, focus, content, score, observation, time, audioPath);
                item.setAllQuestionListener(allquestioncallback);
                evTemp.add(item);
            }

            // 存储分组信息，供后续使用
            testcontext.getInstance().setGroupNumber(groupNumber);
            
            testcontext.getInstance().setEvaluations(evTemp);
            adapter = new testpageradapter(R.layout.word_test1, null, Tb, evTemp, null, null, null, counter, timer);
            viewPager.setAdapter(adapter);
            
            // 从SharedPreferences读取之前的位置
            SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
            String currentQuestionKey = "CurrentQuestion_SOCIAL" + groupNumber;
            String savedPosition = preferences.getString(currentQuestionKey, null);
            int position = 0;
            if (savedPosition != null) {
                try {
                    position = Integer.parseInt(savedPosition);
                    if (position < 0 || position >= adapter.getCount()) {
                        // 保存的位置无效，从第一题开始
                        position = 0;
                    }
                } catch (NumberFormatException e) {
                    // 保存的位置格式错误，从第一题开始
                    position = 0;
                }
            } else {
                // 没有保存的位置，从第一题开始
                position = 0;
            }
            
            // 强制更新counter的显示，确保显示正确的进度
            if (counter != null) {
                int completedCount = position + 1;
                counter.setText(completedCount + "/" + groupLength);
            }
            
            // 跳转到指定位置
            viewPager.setCurrentItem(position);
            
            // 延迟执行，确保页面切换完成后再更新计数器
            viewPager.post(new Runnable() {
                @Override
                public void run() {
                    if (counter != null) {
                        int currentPosition = viewPager.getCurrentItem();
                        int completedCount = currentPosition + 1;
                        counter.setText(completedCount + "/" + groupLength);
                    }
                }
            });

        } else {

        }
    }

    @Override
    public void onBackPressed() {
        Integer count = testcontext.getInstance().getCount();
        Integer lengths = testcontext.getInstance().getLengths();
        int countValue = count != null ? count : 0;
        int lengthsValue = lengths != null && lengths > 0 ? lengths : 10;
        
        if(countValue < lengthsValue)
                dialogUtils.showDialog(this, "提示信息", "您尚未完成测评，是否退出？", "是", () -> {
                    // 保存当前位置
                    SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    String currentQuestionKey = "CurrentQuestion";
                    // 如果是 RG、SE 或 SOCIAL 类型，添加组别信息到键中
                    String key = testactivity.this.resolvedKey;
                    if (key != null && (key.equals("RG") || key.equals("SE") || key.equals("SOCIAL"))) {
                        Integer groupNumber = testcontext.getInstance().getGroupNumber();
                        if (groupNumber != null) {
                            currentQuestionKey = "CurrentQuestion_" + key + groupNumber;
                        }
                    }
                    editor.putString(currentQuestionKey, String.valueOf(viewPager.getCurrentItem()));
                    editor.apply();
                    performCleanup(false);
                    
                    // 直接使用finish()返回上一个Activity，遵循正常的返回栈顺序
                    finish();
                }, "否", null);
        else {
            // 完成了所有题目，执行自动跳转逻辑
            performCleanup(true);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        //录音未结束，中断录音
        if(AudioRecorder.getInstance().getAlive()){
            AudioRecorder.getInstance().interruptRecorder();
        }

        // 清理ViewPager相关资源
        if (viewPager != null) {
            viewPager.setAdapter(null);
            viewPager.clearOnPageChangeListeners();
        }

        // 清理适配器资源
        if (adapter != null) {
            adapter = null;
        }

        // 清理数据集合
        if (evTemp != null) {
            evTemp.clear();
            evTemp = null;
        }

        // 释放单例
        testcontext.getInstance().release();
        
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_exit){
            if(testcontext.getInstance().getCount()< testcontext.getInstance().getLengths())
                dialogUtils.showDialog(this, "提示信息", "您尚未完成测评，是否退出？", "是", () -> {
                    // 保存当前位置
                    SharedPreferences preferences = getSharedPreferences("login_prefs_" + fName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    String currentQuestionKey = "CurrentQuestion";
                    // 如果是 RG、SE 或 SOCIAL 类型，添加组别信息到键中
                    String key = testactivity.this.resolvedKey;
                    if (key != null && (key.equals("RG") || key.equals("SE") || key.equals("SOCIAL"))) {
                        Integer groupNumber = testcontext.getInstance().getGroupNumber();
                        if (groupNumber != null) {
                            currentQuestionKey = "CurrentQuestion_" + key + groupNumber;
                        }
                    }
                    editor.putString(currentQuestionKey, String.valueOf(viewPager.getCurrentItem()));
                    editor.apply();
                    performCleanup(false);
                    
                    // 直接使用finish()返回上一个Activity，遵循正常的返回栈顺序
                    finish();
                }, "否", null);
            else {
                // 完成了所有题目，执行自动跳转逻辑
                performCleanup(true);
                finish();
            }
        }
    }

    public void performCleanup(boolean shouldNavigate){
        // 清理资源
        AudioRecorder.getInstance().stopRecorder();
        
        final String key = this.resolvedKey != null ? this.resolvedKey : (moduleKey != null ? moduleKey : format);
        final int currentGroup = testcontext.getInstance().getGroupNumber() != null ? testcontext.getInstance().getGroupNumber() : 1;
        final boolean finalShouldNavigate = shouldNavigate;
        final ArrayList<evaluation> finalEvTemp = evTemp;
        final String finalFName = fName;
        final String finalUid = uid;
        final String finalChildUser = childUser;
        final NetService finalNetService = netService;
        
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                int totalScore = 0;
                boolean saveSuccess = false;
                int resultCode = -4;
                String dataJson = null;

                try {
                    if (finalFName == null) {
                        resultCode = -1;
                    } else {
                        // 加载数据，添加异常处理
                        JSONObject data = null;
                        try {
                            data = dataManager.getInstance().loadData(finalFName);
                            if (data == null) {
                                resultCode = -2;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            resultCode = -3;
                        }
                        
                        if (resultCode == -4 && data != null) {
                            JSONObject evaluations = data.optJSONObject("evaluations");
                            if (evaluations == null) {
                                evaluations = new JSONObject();
                                data.put("evaluations", evaluations);
                            }

                            boolean isPrelinguistic = MODULE_PL.equals(key) || FORMAT_PL.equals(key) || FORMAT_PL.equals(format);
                            if(key!=null && finalEvTemp != null && !finalEvTemp.isEmpty()){
                                // 构音结果去重写入：每次完成模块先清空再写回，避免重复append导致结果重复或丢失。
                                if ("A".equals(key)) {
                                    evaluations.put("A", new JSONArray());
                                } else if ("E".equals(key)) {
                                    evaluations.put("E", new JSONArray());
                                } else if ("EV".equals(key)) {
                                    evaluations.put("EV", new JSONArray());
                                } else if ("SE".equals(key)) {
                                    // 对于SE模块，清空对应组的JSONArray
                                    String seKey = "SE" + currentGroup;
                                    evaluations.put(seKey, new JSONArray());
                                }
                                
                                // 直接写入数据，不再清空数组，保留之前的进度
                                for(int i = 0; i< finalEvTemp.size(); i++){
                                    try {
                                        if (finalEvTemp.get(i) != null) {
                                            finalEvTemp.get(i).toJson(evaluations);
                                        }
                                    } catch (Exception e) {
                                        // 忽略单个题目写入错误，继续处理其他题目
                                        e.printStackTrace();
                                    }
                                }
                                
                                // 保存数据，确保在计算总分之前数据已经保存
                                try {
                                    dataManager.getInstance().saveData(finalFName, data);
                                    saveSuccess = true;
                                    dataJson = data.toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    saveSuccess = false;
                                }

                                // 保存本地成功后，同步到服务端（并上传该模块产生的音频）
                                if (saveSuccess
                                        && finalNetService != null
                                        && finalUid != null
                                        && !finalUid.isEmpty()) {
                                    JSONObject evaluationsToUpload = data.optJSONObject("evaluations");
                                    if (evaluationsToUpload == null) {
                                        evaluationsToUpload = new JSONObject();
                                    }
                                    // lambda 回调需要捕获“effectively final”变量
                                    final JSONObject evaluationsToUploadFinal = evaluationsToUpload;

                                    if (finalChildUser != null && !finalChildUser.isEmpty()) {
                                        // 已有 childUserID：直接更新
                                        finalNetService.updateEvaluation(finalUid, finalChildUser, data.toString());
                                        uploadAudioIfPresent(finalNetService, finalUid, finalChildUser, evaluationsToUploadFinal);
                                    } else {
                                        // 没有 childUserID：创建一条并拿到返回的 childUserID，再继续上传音频
                                        finalNetService.setUploadEvaluationCallback(childUserID -> {
                                            try {
                                                if (childUserID != null && !childUserID.isEmpty()) {
                                                    SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                                                    loginPrefs.edit().putString("childID_" + finalFName, childUserID).apply();
                                                    childUser = childUserID;
                                                    isServerBacked = true;

                                                    uploadAudioIfPresent(finalNetService, finalUid, childUserID, evaluationsToUploadFinal);
                                                }
                                            } catch (Exception ignored) {
                                            }
                                        });
                                        finalNetService.uploadEvaluation(finalUid, data.toString());
                                    }
                                }
                                
                                // 计算当前组的总分（仅适用于SOCIAL）
                                if (key.equals("SOCIAL")) {
                                    for (evaluation evaluation : finalEvTemp) {
                                        try {
                                            if (evaluation == null || !(evaluation instanceof social)) {
                                                continue;
                                            }
                                            social item = (social) evaluation;
                                            int score = item.getScore() == null ? 0 : item.getScore();
                                            totalScore += score;
                                        } catch (Exception e) {
                                            // 忽略单个题目分数计算错误，继续处理其他题目
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                
                                // 确保总分计算正确，至少为0
                                if (totalScore < 0) {
                                    totalScore = 0;
                                }
                            }
                            resultCode = saveSuccess ? totalScore : -5;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resultCode = -4;
                }
                
                final int finalResult = resultCode;
                final int finalTotalScore = totalScore;
                final String finalDataJson = dataJson;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResult == -1) {
                            Toast.makeText(testactivity.this, "数据加载失败！", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (finalResult == -2) {
                            Toast.makeText(testactivity.this, "加载数据失败！", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (finalResult == -3) {
                            Toast.makeText(testactivity.this, "加载数据失败！", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (finalResult == -4 || finalResult == -5) {
                            Toast.makeText(testactivity.this, "保存数据失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 退出模块时做一次上传（有账号时）
                        uploadEvaluationOnExit(finalDataJson);

                        // 所有题目完成后，生成模块报告
                        if (finalShouldNavigate) {
                            // 显示测评已完成提示
                            Toast.makeText(testactivity.this, "测评已完成！", Toast.LENGTH_SHORT).show();
                            
                            // 延迟跳转，让用户有时间看到提示
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (key.equals("SOCIAL")) {
                                        // 社交能力评估的特殊逻辑
                                        if (finalTotalScore < 12 && currentGroup != 1) {
                                            // 总分小于12且不是第一组，跳转到社交能力评估选择界面，限制只能选上一组
                                            Intent intent = new Intent(testactivity.this, SocialGroupSelectActivity.class);
                                            intent.putExtra("fName", finalFName);
                                            intent.putExtra("Uid", getIntent().getStringExtra("Uid"));
                                            intent.putExtra("childID", getIntent().getStringExtra("childID"));
                                            intent.putExtra("currentGroup", currentGroup);
                                            startActivity(intent);
                                        } else {
                                            // 总分大于等于12，或者是第一组，跳转到评估模块选择界面
                                            Intent intent = new Intent(testactivity.this, AssessmentModulesActivity.class);
                                            intent.putExtra("fName", finalFName);
                                            intent.putExtra("Uid", getIntent().getStringExtra("Uid"));
                                            intent.putExtra("childID", getIntent().getStringExtra("childID"));
                                            startActivity(intent);
                                        }
                                    } else {
                                        // 其他模块的跳转逻辑
                                        generateModuleReport();
                                    }
                                }
                            }, 1000); // 1秒延迟
                        }
                    }
                });
            }
        }).start();
    }

    private void uploadEvaluationOnExit(String dataJson) {
        if (uploadRequested || dataJson == null || dataJson.isEmpty()) {
            return;
        }
        if (uid == null || uid.trim().isEmpty() || netService == null) {
            return;
        }
        uploadRequested = true;
        if (childUserId != null && !childUserId.trim().isEmpty()) {
            netService.updateEvaluation(uid, childUserId, dataJson);
        } else {
            netService.uploadEvaluation(uid, dataJson);
        }
    }

    private void generateModuleReport() {
        try {
            // 检查模块的完成情况
            boolean isRGCompleted = checkModuleCompletion("RG");
            boolean isSECompleted = checkModuleCompletion("SE");
            boolean isECompleted = checkModuleCompletion("E");
            boolean isEVCompleted = checkModuleCompletion("EV");
            
            // 对于词汇表达（E）和词汇理解（EV）模块，直接跳转到评估模块选择界面
            if ("E".equals(resolvedKey) || "EV".equals(resolvedKey)) {
                Intent intent = new Intent(this, AssessmentModulesActivity.class);
                intent.putExtra("fName", fName);
                intent.putExtra("Uid", uid);
                intent.putExtra("childID", childUser);
                startActivity(intent);
                return;
            }
            
            // 根据完成情况决定跳转目标
            if (isRGCompleted && isSECompleted) {
                // 两个模块都完成了，跳转到图三（评估模块选择界面）
                Intent intent = new Intent(this, AssessmentModulesActivity.class);
                intent.putExtra("fName", fName);
                intent.putExtra("Uid", uid);
                intent.putExtra("childID", childUser);
                startActivity(intent);
            } else if (isRGCompleted || isSECompleted) {
                // 只有一个模块完成了，跳转到图二（句法能力评估选择界面）
                Intent intent = new Intent(this, SyntaxAbilityEvaluationActivity.class);
                intent.putExtra("fName", fName);
                intent.putExtra("Uid", uid);
                intent.putExtra("childID", childUser);
                startActivity(intent);
            } else {
                // 两个模块都没完成，跳转到对应模块的组别选择界面
                if ("RG".equals(resolvedKey)) {
                    Intent intent = new Intent(this, SyntaxComprehensionGroupSelectActivity.class);
                    intent.putExtra("fName", fName);
                    intent.putExtra("Uid", uid);
                    intent.putExtra("childID", childUser);
                    startActivity(intent);
                } else if ("SE".equals(resolvedKey)) {
                    Intent intent = new Intent(this, SyntaxExpressionGroupSelectActivity.class);
                    intent.putExtra("fName", fName);
                    intent.putExtra("Uid", uid);
                    intent.putExtra("childID", childUser);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 异常发生时，跳转到评估模块选择界面
            Intent intent = new Intent(this, AssessmentModulesActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
        }
    }

    private void uploadAudioIfPresent(NetService service, String uid, String childUserID, JSONObject evaluations) {
        if (service == null || uid == null || uid.isEmpty() || childUserID == null || childUserID.isEmpty() || evaluations == null) {
            return;
        }
        try {
            Iterator<String> it = evaluations.keys();
            while (it.hasNext()) {
                String moduleType = it.next(); // 与 evmenuactivity 约定一致
                JSONArray currentArray = evaluations.optJSONArray(moduleType);
                if (currentArray == null) {
                    continue;
                }
                for (int i = 0; i < currentArray.length(); i++) {
                    JSONObject element = currentArray.optJSONObject(i);
                    if (element == null) {
                        continue;
                    }
                    String audioPath = element.optString("audioPath", null);
                    if (audioPath == null || audioPath.trim().isEmpty() || "null".equals(audioPath)) {
                        continue;
                    }
                    audioPath = audioPath.trim();
                    if (!new File(audioPath).isFile()) {
                        // JSON 里可能残留旧路径或该题本无需录音；无本地文件则不上传，避免误报「音频文件不存在」
                        continue;
                    }
                    int num = element.optInt("num", -1);
                    if (num <= 0) {
                        continue;
                    }
                    service.uploadAudio(uid, childUserID, moduleType, String.valueOf(num), audioPath);
                }
            }
        } catch (Exception ignored) {
            // 上传失败交给 NetService 内部回调/Toast 处理
        }
    }
    
    private boolean checkModuleCompletion(String moduleKey) {
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            if (data != null && data.has("evaluations")) {
                JSONObject evaluations = data.getJSONObject("evaluations");
                
                // 对于词汇表达（E）和词汇理解（EV）模块，直接检查对应的JSONArray
                if ("E".equals(moduleKey) || "EV".equals(moduleKey)) {
                    JSONArray moduleArray = evaluations.optJSONArray(moduleKey);
                    if (moduleArray != null && moduleArray.length() > 0) {
                        // 检查该模块是否所有题目都已完成
                        boolean moduleCompleted = true;
                        for (int j = 0; j < moduleArray.length(); j++) {
                            try {
                                JSONObject item = moduleArray.getJSONObject(j);
                                if (!item.has("time") || item.isNull("time")) {
                                    moduleCompleted = false;
                                    break;
                                }
                            } catch (Exception e) {
                                // 单个题目检查失败，继续检查下一个
                                e.printStackTrace();
                                moduleCompleted = false;
                                break;
                            }
                        }
                        if (moduleCompleted) {
                            return true;
                        }
                    }
                } else {
                    // 对于其他模块（如RG、SE），检查每个组
                    for (int i = 1; i <= 4; i++) {
                        try {
                            JSONArray moduleArray = evaluations.optJSONArray(moduleKey + i);
                            if (moduleArray != null && moduleArray.length() > 0) {
                                // 检查该组是否所有题目都已完成
                                boolean groupCompleted = true;
                                for (int j = 0; j < moduleArray.length(); j++) {
                                    try {
                                        JSONObject item = moduleArray.getJSONObject(j);
                                        if (!item.has("time") || item.isNull("time")) {
                                            groupCompleted = false;
                                            break;
                                        }
                                    } catch (Exception e) {
                                        // 单个题目检查失败，继续检查下一个组
                                        e.printStackTrace();
                                        groupCompleted = false;
                                        break;
                                    }
                                }
                                if (groupCompleted) {
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            // 单个组检查失败，继续检查下一个组
                            e.printStackTrace();
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}


