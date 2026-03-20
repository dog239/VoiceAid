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
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private EditText vowelErrorList;
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
    private View generatePlanButton;
    private View viewPlanButton;
    private String cachedPlanJsonString;
    private static final String FORMAT_PL = "11";
    private static final String MODULE_PL = "PL";
    private String plSummaryTextValue;
    private boolean isPrelinguisticResult;
    private boolean isArticulationResult;
    private boolean isSocialResult;
    private TextView tv3;
    
    // 题目详情数据结构
    private static class QuestionDetail {
        int num;
        String questionNumber;
        String questionText;
        String correctOption;
        String selectedOption;
        boolean result;
        String time;
        String grammarPoint;
        int groupNumber;

        QuestionDetail(int num, String questionNumber, String questionText, String correctOption, 
                      String selectedOption, boolean result, String time, String grammarPoint, int groupNumber) {
            this.num = num;
            this.questionNumber = questionNumber;
            this.questionText = questionText;
            this.correctOption = correctOption;
            this.selectedOption = selectedOption;
            this.result = result;
            this.time = time;
            this.grammarPoint = grammarPoint;
            this.groupNumber = groupNumber;
        }
    }
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
    private final int[] vowelIds = new int[]{
            R.id.vowel_a, R.id.vowel_o, R.id.vowel_e, R.id.vowel_i,
            R.id.vowel_u, R.id.vowel_v, R.id.vowel_er, R.id.vowel_ai,
            R.id.vowel_ei, R.id.vowel_ao, R.id.vowel_ou, R.id.vowel_ia,
            R.id.vowel_ie, R.id.vowel_iao, R.id.vowel_iou, R.id.vowel_ua,
            R.id.vowel_uo, R.id.vowel_uai, R.id.vowel_uei, R.id.vowel_ue,
            R.id.vowel_an, R.id.vowel_en, R.id.vowel_ang, R.id.vowel_eng,
            R.id.vowel_ian, R.id.vowel_in, R.id.vowel_iang, R.id.vowel_ing,
            R.id.vowel_uan, R.id.vowel_uen, R.id.vowel_uang, R.id.vowel_ong,
            R.id.vowel_uan_umlaut, R.id.vowel_un, R.id.vowel_iong
    };
    private final String[] vowelLabels = new String[]{
            "a", "o", "e", "i",
            "u", "ü", "er", "ai",
            "ei", "ao", "ou", "ia",
            "ie", "iao", "iou", "ua",
            "uo", "uai", "uei", "üe",
            "an", "en", "ang", "eng",
            "ian", "in", "iang", "ing",
            "uan", "uen", "uang", "ong",
            "üan", "ün", "iong"
    };

    // --- Begin: Articulation stat helpers and recompute logic ---
    private List<a> collectArticulationItems() {
        List<a> items = new ArrayList<>();
        if (evaluations == null) return items;
        for (evaluation eval : evaluations) {
            if (eval instanceof a) {
                a item = (a) eval;
                if (item.getNum() > 0) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    private void recomputeArticulationStats() {
        if (!isArticulationResult) return;
        List<a> aItems = collectArticulationItems();
        int[] correctInitial = new int[initialLabels.length];
        int[] totalInitial = new int[initialLabels.length];
        int[] correctVowel = new int[vowelLabels.length];
        int[] totalVowel = new int[vowelLabels.length];
        int[] errorVowel = new int[vowelLabels.length];
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
                String tgtVowel = normalizeVowel(vowelString(tgt));
                if (!tgtVowel.isEmpty()) {
                    int pos = vowelIndexOf(tgtVowel);
                    if (pos >= 0) {
                        totalVowel[pos]++;
                        String ansVowel = normalizeVowel(vowelString(ans));
                        if (tgtVowel.equals(ansVowel)) {
                            correctVowel[pos]++;
                        } else {
                            errorVowel[pos]++;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < initialIds.length && i < initialLabels.length; i++) {
            EditText et = findViewById(initialIds[i]);
            if (et != null) {
                int total = totalInitial[i];
                int correct = correctInitial[i];
                et.setText(total > 0 ? (correct + "/" + total) : "0/0");
            }
        }
        for (int i = 0; i < vowelIds.length && i < vowelLabels.length; i++) {
            EditText et = findViewById(vowelIds[i]);
            if (et != null) {
                int total = totalVowel[i];
                int correct = correctVowel[i];
                et.setText(total > 0 ? (correct + "/" + total) : "0/0");
            }
        }
        if (vowelErrorList != null) {
            List<String> errorItems = new ArrayList<>();
            for (int i = 0; i < vowelLabels.length; i++) {
                if (errorVowel[i] > 0) {
                    errorItems.add(vowelLabels[i]);
                }
            }
            vowelErrorList.setText(joinText(errorItems, "、"));
        }
        double initialRate = 0d;
        if (initialAccuracy != null) {
            int sumTotal = 0, sumCorrect = 0;
            for (int i = 0; i < totalInitial.length; i++) { sumTotal += totalInitial[i]; sumCorrect += correctInitial[i]; }
            initialRate = sumTotal > 0 ? (sumCorrect * 1.0d / sumTotal) : 0d;
            String rateStr = sumTotal > 0 ? String.format(Locale.getDefault(), "%.2f%%", (initialRate * 100.0d)) : "0%";
            initialAccuracy.setText(rateStr);
        }
        if (speechClarity != null) {
            String level;
            if (initialRate >= 0.85d) level = "轻度";
            else if (initialRate >= 0.65d) level = "轻中度";
            else if (initialRate >= 0.50d) level = "中重度";
            else level = "重度";
            speechClarity.setText(level);
        }
    }
    // --- End: Articulation stat helpers and recompute logic ---

    private JSONArray getEvaluationArray(JSONObject data, String key) {
        if (data == null) {
            return new JSONArray();
        }
        JSONObject evaluationsObject = data.optJSONObject("evaluations");
        if (evaluationsObject == null) {
            return new JSONArray();
        }
        JSONArray array = evaluationsObject.optJSONArray(key);
        return array != null ? array : new JSONArray();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result11);
        recyclerView = findViewById(R.id.recyclerview);
        back = findViewById(R.id.back);
        ImageView btnBackNew = findViewById(R.id.btn_back_new);
        if (btnBackNew != null) {
            btnBackNew.setOnClickListener(this);
        }
        generatePlanButton = findViewById(R.id.btn_generate_plan);
        if (generatePlanButton != null) {
            generatePlanButton.setOnClickListener(this);
        }
        viewPlanButton = findViewById(R.id.btn_view_plan);
        if (viewPlanButton != null) {
            viewPlanButton.setOnClickListener(this);
        }
        tv2 = findViewById(R.id.tv_2);
        tv3 = findViewById(R.id.tv_3);
        extraAResults = findViewById(R.id.extra_a_results);
        extraASuggestions = findViewById(R.id.extra_a_suggestions);
        initialAccuracy = findViewById(R.id.extra_a_initial_accuracy);
        speechClarity = findViewById(R.id.extra_a_speech_clarity);
        vowelErrorList = findViewById(R.id.vowel_error_list);
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
        
        // 检查是否已有干预报告，如果有，启用查看按钮
        String checkModuleType = safeFormat;
        if (isPrelinguisticResult) {
            checkModuleType = "prelinguistic";
        } else if (isArticulationResult) {
            checkModuleType = "articulation";
        } else if (isSocialResult) {
            checkModuleType = "social";
        } else if ("E".equals(safeFormat) || "EV".equals(safeFormat)) {
            checkModuleType = "vocabulary";
        }
        
        if (viewPlanButton != null) {
            JSONObject guide = utils.ModuleReportHelper.loadModuleInterventionGuide(data, checkModuleType);
            if (guide != null && guide.length() > 0) {
                viewPlanButton.setEnabled(true);
                viewPlanButton.setAlpha(1f);
            } else {
                // 如果没有报告，也可以选择禁用按钮或者保持可点击但点击时提示
                // 这里选择禁用以明确状态
                // viewPlanButton.setEnabled(false);
                // viewPlanButton.setAlpha(0.5f);
                // 为了符合用户习惯，也可以不禁用，点击时提示“请先生成”，维持现状
            }
        }

        if(isArticulation){
            tv2.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray("A");
            if (extraAResults != null) extraAResults.setVisibility(View.VISIBLE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.VISIBLE);
            evaluations.add(new a(0, (List<a.CharacterPhonology>) null, null, null, null)); // 首行
            // 构音记录去重：按题号保留最后一次写入，避免历史重复append导致1-53重复显示。
            HashMap<Integer, a> uniqueByNum = new HashMap<>();
            int maxNum = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                a item = a.fromJson(object);
                int num = item.getNum();
                if (num <= 0) {
                    continue;
                }
                uniqueByNum.put(num, item);
                if (num > maxNum) {
                    maxNum = num;
                }
            }
            List<a> aItems = new ArrayList<>();
            for (int num = 1; num <= maxNum; num++) {
                a item = uniqueByNum.get(num);
                if (item != null) {
                    aItems.add(item);
                    evaluations.add(item);
                }
            }
            recomputeArticulationStats();
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
            
            // 隐藏硬编码的需要重点关注的能力标题
            View weaknessTitle = findViewById(R.id.weakness_title);
            if (weaknessTitle != null) {
                weaknessTitle.setVisibility(View.GONE);
            }
            // 隐藏硬编码的不稳定的能力标题
            View inProgressTitle = findViewById(R.id.in_progress_title);
            if (inProgressTitle != null) {
                inProgressTitle.setVisibility(View.GONE);
            }
        }
        else if (safeFormat.equals("E")) {
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            
            // 加载词汇表达（E）的结果
            double counte = 0;
            JSONArray eArray = getEvaluationArray(data, "E");
            
            // 加载词汇理解（EV）的结果
            double countev = 0;
            JSONArray evArray = getEvaluationArray(data, "EV");
            
            // 计算各测试点的得分情况
            HashMap<String, Integer> eTestPointScores = new HashMap<>();
            HashMap<String, Integer> evTestPointScores = new HashMap<>();
            
            // 初始化测试点得分
            eTestPointScores.put("名词", 0);
            eTestPointScores.put("动词", 0);
            eTestPointScores.put("形容词", 0);
            eTestPointScores.put("分类名词（名词上位词）", 0);
            
            evTestPointScores.put("名词", 0);
            evTestPointScores.put("动词", 0);
            evTestPointScores.put("形容词", 0);
            evTestPointScores.put("分类名词（名词上位词）", 0);
            
            // 计算词汇表达的得分
            for (int i = 0; i < eArray.length(); i++) {
                JSONObject object = eArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    if (object.getBoolean("result")) {
                        counte++;
                        // 根据题目编号确定测试点
                        String testPoint = "";
                        int num = i + 1;
                        switch (num) {
                            case 1:
                            case 2:
                                testPoint = "名词";
                                break;
                            case 3:
                            case 4:
                                testPoint = "动词";
                                break;
                            case 5:
                            case 6:
                                testPoint = "形容词";
                                break;
                            case 7:
                                testPoint = "分类名词（名词上位词）";
                                break;
                        }
                        eTestPointScores.put(testPoint, eTestPointScores.get(testPoint) + 1);
                    }
                }
            }
            
            // 计算词汇理解的得分
            for (int i = 0; i < evArray.length(); i++) {
                JSONObject object = evArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    if (object.getBoolean("result")) {
                        countev++;
                        // 根据题目编号确定测试点
                        String testPoint = "";
                        int num = i + 1;
                        switch (num) {
                            case 1:
                            case 2:
                                testPoint = "名词";
                                break;
                            case 3:
                            case 4:
                                testPoint = "动词";
                                break;
                            case 5:
                            case 6:
                                testPoint = "形容词";
                                break;
                            case 7:
                                testPoint = "分类名词（名词上位词）";
                                break;
                        }
                        evTestPointScores.put(testPoint, evTestPointScores.get(testPoint) + 1);
                    }
                }
            }
            
            // 计算每个测试点的总得分（词汇表达 + 词汇理解）
            HashMap<String, Integer> totalTestPointScores = new HashMap<>();
            totalTestPointScores.put("名词", eTestPointScores.get("名词") + evTestPointScores.get("名词"));
            totalTestPointScores.put("动词", eTestPointScores.get("动词") + evTestPointScores.get("动词"));
            totalTestPointScores.put("形容词", eTestPointScores.get("形容词") + evTestPointScores.get("形容词"));
            totalTestPointScores.put("分类名词（名词上位词）", eTestPointScores.get("分类名词（名词上位词）") + evTestPointScores.get("分类名词（名词上位词）"));
            
            // 确定掌握较好和不够好的测试点
            String goodAt = "";
            String needImprove = "";
            
            // 检查是否所有测试点都全对
            boolean allGood = true;
            // 检查是否所有测试点都全错
            boolean allBad = true;
            
            for (String testPoint : totalTestPointScores.keySet()) {
                int score = totalTestPointScores.get(testPoint);
                if (testPoint.equals("分类名词（名词上位词）")) {
                    // 分类概念名词只有2道题（1道词汇表达 + 1道词汇理解）
                    if (score >= 2) {
                        if (goodAt.isEmpty()) {
                            goodAt = testPoint;
                        } else {
                            goodAt += "、" + testPoint;
                        }
                        allBad = false;
                    } else if (score <= 0) {
                        if (needImprove.isEmpty()) {
                            needImprove = testPoint;
                        } else {
                            needImprove += "、" + testPoint;
                        }
                        allGood = false;
                    } else {
                        allGood = false;
                        allBad = false;
                    }
                } else {
                    // 其他测试点有4道题（2道词汇表达 + 2道词汇理解）
                    if (score >= 4) {
                        if (goodAt.isEmpty()) {
                            goodAt = testPoint;
                        } else {
                            goodAt += "、" + testPoint;
                        }
                        allBad = false;
                    } else if (score <= 0) {
                        if (needImprove.isEmpty()) {
                            needImprove = testPoint;
                        } else {
                            needImprove += "、" + testPoint;
                        }
                        allGood = false;
                    } else {
                        allGood = false;
                        allBad = false;
                    }
                }
            }
            
            // 处理特殊情况
            if (allGood) {
                // 所有测试点都全对
                goodAt = "名词、动词、形容词、分类名词（名词上位词）";
                needImprove = "无";
            } else if (allBad) {
                // 所有测试点都全错
                goodAt = "无";
                needImprove = "名词、动词、形容词、分类名词（名词上位词）";
            } else if (goodAt.isEmpty() && needImprove.isEmpty()) {
                // 没有任何测试点全对或全错
                goodAt = "无";
                needImprove = "无";
            }
            
            // 添加词汇理解表格的表头行
            evaluations.add(new ev(0, "序号", null, "结果"));//首行
            for (int i = 0; i < evArray.length(); i++) {
                JSONObject object = evArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    evaluations.add(new ev(i + 1, object.getString("target"), object.getBoolean("result"), object.getString("time")));
                } else {
                    evaluations.add(new ev(i + 1, object.getString("target"), null, null));
                }
            }
            // 添加词汇理解结果总分
            evaluations.add(new ev(-1, "词汇理解结果", null, String.valueOf((int) countev) + "/" + String.valueOf(evArray.length())));
            
            // 添加词汇表达表格的表头行
            evaluations.add(new e(0, "序号", "测试点", "目标词", "结果", "答题时长", "录音"));//首行
            for (int i = 0; i < eArray.length(); i++) {
                JSONObject object = eArray.getJSONObject(i);
                if (object.has("result") && !object.isNull("result")) {
                    evaluations.add(new e(i + 1, object.getString("target"), object.getBoolean("result"), new audio(object.getString("audioPath")), object.getString("time")));
                } else {
                    evaluations.add(new e(i + 1, object.getString("target"), null, null, null));
                }
            }
            // 添加词汇表达结果总分
            evaluations.add(new e(-1, "词汇表达结果", null, null, String.valueOf((int) counte) + "/" + String.valueOf(eArray.length())));
            
            // 生成评估建议
            StringBuilder suggestion = new StringBuilder();
            double evAccuracy = evArray.length() > 0 ? (countev / evArray.length()) * 100 : 0;
            double eAccuracy = eArray.length() > 0 ? (counte / eArray.length()) * 100 : 0;
            suggestion.append("（二）评估结果\n\n");
            suggestion.append("词汇理解正确率：").append(String.format("%.2f%%", evAccuracy)).append("\n");
            suggestion.append("词汇表达正确率：").append(String.format("%.2f%%", eAccuracy)).append("\n\n");
            suggestion.append("（三）评估建议\n\n");
            suggestion.append("词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达，根据测评结果显示，孩子");
            if (!goodAt.isEmpty()) {
                suggestion.append(goodAt);
            } else {
                suggestion.append("各测试点");
            }
            suggestion.append("掌握较好，但");
            if (!needImprove.isEmpty()) {
                suggestion.append(needImprove);
            } else {
                suggestion.append("各测试点");
            }
            suggestion.append("掌握得不够好，需要针对性的学习。");
            
            tv2.setText(suggestion.toString());
            
            // 隐藏默认的评估建议部分，因为词汇模块有自己的评估建议格式
            LinearLayout weaknessLayout = findViewById(R.id.weakness_list);
            if (weaknessLayout != null) {
                weaknessLayout.setVisibility(View.GONE);
            }
            LinearLayout inProgressLayout = findViewById(R.id.in_progress_list);
            if (inProgressLayout != null) {
                inProgressLayout.setVisibility(View.GONE);
            }
            // 隐藏评估建议标题和相关文本
            TextView evaluationText = findViewById(R.id.tv_3);
            if (evaluationText != null) {
                evaluationText.setVisibility(View.GONE);
            }
            // 隐藏硬编码的需要重点关注的能力标题
            View weaknessTitle = findViewById(R.id.weakness_title);
            if (weaknessTitle != null) {
                weaknessTitle.setVisibility(View.GONE);
            }
            // 隐藏硬编码的不稳定的能力标题
            View inProgressTitle = findViewById(R.id.in_progress_title);
            if (inProgressTitle != null) {
                inProgressTitle.setVisibility(View.GONE);
            }

        } else if (safeFormat.equals("EV")) {
            // 重定向到E格式，因为我们在E格式中已经处理了EV的结果
            Intent evIntent = new Intent(this, resultactivity.class);
            evIntent.putExtra("fName", fName);
            evIntent.putExtra("format", "E");
            startActivity(evIntent);
            finish();

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
            // 强制横屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            
            // 初始化数据结构
            double countre=0;
            int completedCount = 0;
            
            // 存储每个语法点的得分情况
            HashMap<String, Integer> grammarPointScores = new HashMap<>();
            HashMap<String, Integer> grammarPointTotal = new HashMap<>();
            
            // 存储题目详情，按组分类
            Map<Integer, ArrayList<QuestionDetail>> groupQuestionDetails = new HashMap<>();
            
            // 定义语法点映射
            String[][] grammarPoints = {
                // 第一组
                {"主谓结构", "动宾结构", "主谓宾", "否定句", "一般疑问句", "特殊疑问句", "形容词+名词"},
                // 第二组
                {"多重修饰", "双宾结构", "是不是问句", "地点疑问句", "副词都", "语序"},
                // 第三组
                {"被动句", "比较句", "了（完成体）", "因果复句", "转折句", "时间顺序句"},
                // 第四组
                {"着（持续体）", "双重否定", "条件句（排除）", "篇章理解能力"}
            };
            
            // 初始化语法点得分和总分
            for (int i = 0; i < grammarPoints.length; i++) {
                for (String point : grammarPoints[i]) {
                    grammarPointScores.put(point, 0);
                    grammarPointTotal.put(point, 0);
                }
            }
            
            // 加载数据
            JSONArray jsonArray = null;
            JSONObject evaluationsObj = data.getJSONObject("evaluations");
            // 打印日志，查看evaluationsObj的内容
            System.out.println("RG - evaluationsObj: " + evaluationsObj.toString());
            for (int i = 1; i <= 4; i++) {
                String rgKey = "RG" + i;
                System.out.println("RG - Checking key: " + rgKey);
                if (evaluationsObj.has(rgKey)) {
                    JSONArray groupArray = evaluationsObj.getJSONArray(rgKey);
                    System.out.println("RG - Group " + i + " has " + groupArray.length() + " items");
                    if (groupArray.length() > 0) {
                        if (jsonArray == null) {
                            jsonArray = groupArray;
                        } else {
                            for (int j = 0; j < groupArray.length(); j++) {
                                jsonArray.put(groupArray.get(j));
                            }
                        }
                    }
                }
            }
            if (jsonArray == null) {
                jsonArray = evaluationsObj.optJSONArray("RG");
                System.out.println("RG - Old RG array has " + (jsonArray != null ? jsonArray.length() : 0) + " items");
                if (jsonArray == null) {
                    jsonArray = new JSONArray();
                }
            }
            System.out.println("RG - Final jsonArray has " + jsonArray.length() + " items");
            
            // 定义题目数据 - 按照新的格式重新组织
            // 第一组
            String[] group1Questions = {
                "指导语：小狗跑", "指导语：爷爷坐", "指导语：小鸟飞",
                "指导语：切香蕉", "指导语：吃苹果", "指导语：擦桌子",
                "指导语：哥哥搭积木", "指导语：爷爷看报纸", "指导语：兔子拔萝卜",
                "指导语：不能吃", "指导语：没有跑", "指导语：不会飞",
                "指导语：这是苹果么？", "指导语：妹妹睡觉了么？", "指导语：天黑了吗？",
                "指导语：谁在画兔子？", "指导语：爸爸在干什么？", "指导语：去哪里看医生？",
                "指导语：大苹果", "指导语：黄香蕉", "指导语：圆圆的饼干"
            };
            String[] group1GrammarPoints = {
                "主谓结构", "主谓结构", "主谓结构",
                "动宾结构", "动宾结构", "动宾结构",
                "主谓宾", "主谓宾", "主谓宾",
                "否定句", "否定句", "否定句",
                "一般疑问句", "一般疑问句", "一般疑问句",
                "特殊疑问句", "特殊疑问句", "特殊疑问句",
                "形容词+名词", "形容词+名词", "形容词+名词"
            };
            
            // 第二组
            String[] group2Questions = {
                "指导语：大红苹果", "指导语：白色斑点狗", "指导语：小的圆形饼干",
                "指导语：给妈妈包包", "指导语：喂小猫牛奶", "指导语：送姐姐花",
                "指导语：苹果是不是红的？", "指导语：姐姐戴没戴帽子？", "指导语：飞机是不是交通工具？",
                "指导语：去哪里看大象？", "指导语：在哪里挖沙子？", "指导语：小猫在哪里？",
                "指导语：他们都戴帽子（3个男孩，每个人都戴帽子）", "指导语：小朋友都有苹果（三个小孩，每个人手里都有一个苹果）", "指导语：气球都飞走了（图片：一个小孩，手里没有气球，三个气球都飞走了）",
                "指导语：哥哥推姐姐（荡秋千）", "指导语：爷爷送小朋友礼物", "指导语：狗追猫"
            };
            String[] group2GrammarPoints = {
                "多重修饰", "多重修饰", "多重修饰",
                "双宾结构", "双宾结构", "双宾结构",
                "是不是问句", "是不是问句", "是不是问句",
                "地点疑问句", "地点疑问句", "地点疑问句",
                "副词都", "副词都", "副词都",
                "语序", "语序", "语序"
            };
            
            // 第三组
            String[] group3Questions = {
                "指导语：弟弟被妈妈抱", "指导语：爸爸被小狗追", "指导语：猫咪被大象推",
                "指导语：哥哥比妹妹跑得快。", "指导语：叔叔比阿姨胖", "指导语：红球比绿球大",
                "指导语：哪个是妹妹吃完了？（碗空了，妹妹放下勺子，嘴边有饭渍）", "指导语：花开了（花朵盛开）", "指导语：门关了",
                "指导语：因为下雨，所以打伞", "指导语：因为小明生病了，所以没上学", "指导语：因为天冷了，所以穿棉袄。",
                "指导语：哥哥想去玩，但是下雨了", "指导语：小朋友想吃蛋糕，但是没有了。", "指导语：他想踢足球，但是他腿受伤了。",
                "指导语：指令先揉揉肚子，再挥挥手", "先洗手，再吃饭。", "先刷牙，再睡觉"
            };
            String[] group3GrammarPoints = {
                "被动句", "被动句", "被动句",
                "比较句", "比较句", "比较句",
                "了（完成体）", "了（完成体）", "了（完成体）",
                "因果复句", "因果复句", "因果复句",
                "转折句", "转折句", "转折句",
                "时间顺序句", "时间顺序句", "时间顺序句"
            };
            
            // 第四组
            String[] group4Questions = {
                "指导语：电视开着", "指导语：姐姐跳着绳", "指导语：叔叔唱着歌",
                "指导语：宝宝没有不睡觉", "指导语：小明没有不喜欢吃苹果", "指导语：小明不是不想去学校",
                "指导语：除非洗手，否则不能吃饭。提问：小男孩能吃饭么？", "指导语：除非天气很冷，否则小明都去游泳。提问：小明能去游泳么？", "指导语：除非收拾好玩具，否则不能看电视。提问：可以看电视么？"
            };
            String[] group4GrammarPoints = {
                "着（持续体）", "着（持续体）", "着（持续体）",
                "双重否定", "双重否定", "双重否定",
                "条件句（排除）", "条件句（排除）", "条件句（排除）"
            };
            
            // 添加表头
            evaluations.add(new rg(0, "测试阶段", "题  号", "例  句", "正确选项", "被选选项", "", null, null, "", "对应测试语法点"));
            
            // 按组处理数据，确保顺序正确
            for (int group = 1; group <= 4; group++) {
                ArrayList<QuestionDetail> groupDetails = new ArrayList<>();
                boolean hasCompletedQuestions = false;
                
                // 根据组号获取对应的数据
                String rgKey = "RG" + group;
                JSONArray groupArray = evaluationsObj.optJSONArray(rgKey);
                
                if (groupArray != null && groupArray.length() > 0) {
                    hasCompletedQuestions = true;
                    
                    // 处理该组的题目
                    for (int i = 0; i < groupArray.length(); i++) {
                        JSONObject object = groupArray.getJSONObject(i);
                        if(object.has("time")&&!object.isNull("time")){
                            boolean result = object.getBoolean("result");
                            if(result){
                                countre++;
                            }
                            
                            // 确定题目所属的组和语法点
                            int questionNum = object.optInt("num", i+1);
                            String grammarPoint = "";
                            String questionText = "";
                            String questionNumber = "";
                            int groupQuestionIndex = 0;
                            
                            // 根据题目编号确定组内索引和语法点
                            switch (group) {
                                case 1:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group1Questions.length) {
                                        questionText = group1Questions[groupQuestionIndex];
                                        grammarPoint = group1GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf((groupQuestionIndex % 3) + 1);
                                    }
                                    break;
                                case 2:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group2Questions.length) {
                                        questionText = group2Questions[groupQuestionIndex];
                                        grammarPoint = group2GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf((groupQuestionIndex % 3) + 1);
                                    }
                                    break;
                                case 3:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group3Questions.length) {
                                        questionText = group3Questions[groupQuestionIndex];
                                        grammarPoint = group3GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf((groupQuestionIndex % 3) + 1);
                                    }
                                    break;
                                case 4:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group4Questions.length) {
                                        questionText = group4Questions[groupQuestionIndex];
                                        grammarPoint = group4GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf((groupQuestionIndex % 3) + 1);
                                    } else if (questionNum == 10) {
                                        // 篇章理解能力
                                        grammarPoint = "篇章理解能力";
                                        questionNumber = "10";
                                    }
                                    break;
                            }
                            
                            // 更新语法点得分
                            if (!grammarPoint.isEmpty() && !grammarPoint.equals("篇章理解能力")) {
                                grammarPointTotal.put(grammarPoint, grammarPointTotal.get(grammarPoint) + 1);
                                if (result) {
                                    grammarPointScores.put(grammarPoint, grammarPointScores.get(grammarPoint) + 1);
                                }
                            } else if (grammarPoint.equals("篇章理解能力")) {
                                // 处理篇章理解能力的得分
                                // 检查是否有子问题结果
                                JSONArray subResults = object.optJSONArray("subQuestionResults");
                                if (subResults != null) {
                                    int subCorrect = 0;
                                    int subTotal = subResults.length();
                                    for (int j = 0; j < subResults.length(); j++) {
                                        if (subResults.optBoolean(j)) {
                                            subCorrect++;
                                        }
                                    }
                                    grammarPointTotal.put(grammarPoint, subTotal);
                                    grammarPointScores.put(grammarPoint, subCorrect);
                                } else {
                                    // 如果没有子问题结果，使用主问题结果
                                    grammarPointTotal.put(grammarPoint, 6); // 6个小问
                                    grammarPointScores.put(grammarPoint, result ? 6 : 0);
                                }
                            }
                            
                            // 添加到题目详情列表
                            if (!grammarPoint.equals("篇章理解能力")) {
                                // 尝试从right_option字段获取正确选项
                                String correctOption = object.optString("right_option", "");
                                // 尝试从answer字段获取被选选项
                                String selectedOption = object.optString("answer", "");
                                String time = object.optString("time", "");
                                
                                groupDetails.add(new QuestionDetail(questionNum, questionNumber, questionText, 
                                        correctOption, selectedOption, result, time, grammarPoint, group));
                                completedCount++;
                            }
                        }
                    }
                }
                
                // 如果该组有完成的题目，添加到映射中
                if (hasCompletedQuestions && !groupDetails.isEmpty()) {
                    groupQuestionDetails.put(group, groupDetails);
                }
            }
            

            
            // 清空之前的评估结果
            evaluations.clear();
            
            // 添加表头
            evaluations.add(new rg(0, "测试阶段", "题  号", "例  句", "正确选项", "被选选项", "测试结果", null, null, "", "对应测试语法点"));
            
            // 定义题目数据 - 按照新的格式重新组织
            // 第一组
            String[] rgGroup1Questions = {
                "指导语：小狗跑", "指导语：爷爷坐", "指导语：小鸟飞",
                "指导语：切香蕉", "指导语：吃苹果", "指导语：擦桌子",
                "指导语：哥哥搭积木", "指导语：爷爷看报纸", "指导语：兔子拔萝卜",
                "指导语：不能吃", "指导语：没有跑", "指导语：不会飞",
                "指导语：这是苹果么？", "指导语：妹妹睡觉了么？", "指导语：天黑了吗？",
                "指导语：谁在画兔子？", "指导语：爸爸在干什么？", "指导语：去哪里看医生？",
                "指导语：大苹果", "指导语：黄香蕉", "指导语：圆圆的饼干"
            };
            String[] rgGroup1GrammarPoints = {
                "主谓结构", "主谓结构", "主谓结构",
                "动宾结构", "动宾结构", "动宾结构",
                "主谓宾", "主谓宾", "主谓宾",
                "否定句", "否定句", "否定句",
                "一般疑问句", "一般疑问句", "一般疑问句",
                "特殊疑问句", "特殊疑问句", "特殊疑问句",
                "形容词+名词", "形容词+名词", "形容词+名词"
            };
            
            // 第二组
            String[] rgGroup2Questions = {
                "指导语：大红苹果", "指导语：白色斑点狗", "指导语：小的圆形饼干",
                "指导语：给妈妈包包", "指导语：喂小猫牛奶", "指导语：送姐姐花",
                "指导语：苹果是不是红的？", "指导语：姐姐戴没戴帽子？", "指导语：飞机是不是交通工具？",
                "指导语：去哪里看大象？", "指导语：在哪里挖沙子？", "指导语：小猫在哪里？",
                "指导语：他们都戴帽子（3个男孩，每个人都戴帽子）", "指导语：小朋友都有苹果（三个小孩，每个人手里都有一个苹果）", "指导语：气球都飞走了（图片：一个小孩，手里没有气球，三个气球都飞走了）",
                "指导语：哥哥推姐姐（荡秋千）", "指导语：爷爷送小朋友礼物", "指导语：狗追猫"
            };
            String[] rgGroup2GrammarPoints = {
                "多重修饰", "多重修饰", "多重修饰",
                "双宾结构", "双宾结构", "双宾结构",
                "是不是问句", "是不是问句", "是不是问句",
                "地点疑问句", "地点疑问句", "地点疑问句",
                "副词都", "副词都", "副词都",
                "语序", "语序", "语序"
            };
            
            // 第三组
            String[] rgGroup3Questions = {
                "指导语：弟弟被妈妈抱", "指导语：爸爸被小狗追", "指导语：猫咪被大象推",
                "指导语：哥哥比妹妹跑得快。", "指导语：叔叔比阿姨胖", "指导语：红球比绿球大",
                "指导语：哪个是妹妹吃完了？（碗空了，妹妹放下勺子，嘴边有饭渍）", "指导语：花开了（花朵盛开）", "指导语：门关了",
                "指导语：因为下雨，所以打伞", "指导语：因为小明生病了，所以没上学", "指导语：因为天冷了，所以穿棉袄。",
                "指导语：哥哥想去玩，但是下雨了", "指导语：小朋友想吃蛋糕，但是没有了。", "指导语：他想踢足球，但是他腿受伤了。",
                "指导语：指令先揉揉肚子，再挥挥手", "先洗手，再吃饭。", "先刷牙，再睡觉"
            };
            String[] rgGroup3GrammarPoints = {
                "被动句", "被动句", "被动句",
                "比较句", "比较句", "比较句",
                "了（完成体）", "了（完成体）", "了（完成体）",
                "因果复句", "因果复句", "因果复句",
                "转折句", "转折句", "转折句",
                "时间顺序句", "时间顺序句", "时间顺序句"
            };
            
            // 第四组
            String[] rgGroup4Questions = {
                "指导语：电视开着", "指导语：姐姐跳着绳", "指导语：叔叔唱着歌",
                "指导语：宝宝没有不睡觉", "指导语：小明没有不喜欢吃苹果", "指导语：小明不是不想去学校",
                "指导语：除非洗手，否则不能吃饭。提问：小男孩能吃饭么？", "指导语：除非天气很冷，否则小明都去游泳。提问：小明能去游泳么？", "指导语：除非收拾好玩具，否则不能看电视。提问：可以看电视么？"
            };
            String[] rgGroup4GrammarPoints = {
                "着（持续体）", "着（持续体）", "着（持续体）",
                "双重否定", "双重否定", "双重否定",
                "条件句（排除）", "条件句（排除）", "条件句（排除）"
            };
            
            // 按组处理数据，确保顺序正确
            for (int group = 1; group <= 4; group++) {
                // 根据组号获取对应的数据
                String rgKey = "RG" + group;
                JSONArray groupArray = evaluationsObj.optJSONArray(rgKey);
                
                // 只有当该组有数据时才处理
                if (groupArray != null && groupArray.length() > 0) {
                    // 添加组标题
                    evaluations.add(new rg(-3, "", "第" + group + "组", "", "", "", "", null, null, "", ""));
                    
                    // 处理该组的题目
                    String[] questions = null;
                    String[] rgGrammarPoints = null;
                    
                    switch (group) {
                        case 1:
                            questions = rgGroup1Questions;
                            rgGrammarPoints = rgGroup1GrammarPoints;
                            break;
                        case 2:
                            questions = rgGroup2Questions;
                            rgGrammarPoints = rgGroup2GrammarPoints;
                            break;
                        case 3:
                            questions = rgGroup3Questions;
                            rgGrammarPoints = rgGroup3GrammarPoints;
                            break;
                        case 4:
                            questions = rgGroup4Questions;
                            rgGrammarPoints = rgGroup4GrammarPoints;
                            break;
                    }
                    
                    if (questions != null && rgGrammarPoints != null) {
                        for (int i = 0; i < questions.length; i++) {
                            String questionText = questions[i];
                            String grammarPoint = rgGrammarPoints[i];
                            
                            // 计算题目编号，按照1-10的顺序显示
                            int questionNumber = i + 1;
                            
                            // 尝试从分组数组中加载数据
                            boolean result = false;
                            boolean hasData = false;
                            String correctOption = "";
                            String selectedOption = "";
                            String time = "";
                            
                            // 搜索分组数组中对应题号的记录
                            for (int j = 0; j < groupArray.length(); j++) {
                                Object arrayItem = groupArray.opt(j);
                                if (arrayItem instanceof JSONObject) {
                                    JSONObject itemObj = (JSONObject) arrayItem;
                                    int itemNum = itemObj.optInt("num", 0);
                                    // 匹配题号
                                    if (itemNum == (i + 1)) {
                                        hasData = true;
                                        // 尝试从right_option字段获取正确选项
                                        correctOption = itemObj.optString("right_option", "");
                                        // 尝试从answer字段获取被选选项
                                        selectedOption = itemObj.optString("answer", "");
                                        time = itemObj.optString("time", "");
                                        
                                        // 计算结果：如果被选选项与正确选项相同，则为正确
                                        if (!correctOption.isEmpty() && !selectedOption.isEmpty()) {
                                            // 处理RIGHT/WRONG类型的正确选项
                                            if (correctOption.equals("RIGHT")) {
                                                // 正确按钮的题目，被选选项为RIGHT则正确
                                                result = selectedOption.equals("RIGHT");
                                            } else if (correctOption.equals("WRONG")) {
                                                // 错误按钮的题目，被选选项为WRONG则正确
                                                result = selectedOption.equals("WRONG");
                                            } else {
                                                // 其他类型的题目，直接比较选项
                                                result = correctOption.equals(selectedOption);
                                            }
                                        } else {
                                            // 如果没有正确选项或被选选项，使用原始result字段
                                            result = itemObj.optBoolean("result", false);
                                        }
                                        break;
                                    }
                                }
                            }
                            
                            // 如果没有找到正确选项，尝试根据题目编号和组别设置默认正确选项
                            if (correctOption.isEmpty()) {
                                correctOption = getCorrectOptionForQuestion(group, i + 1);
                            }
                            
                            // 添加题目数据
                            evaluations.add(new rg(i + 1, String.valueOf(group), String.valueOf(questionNumber), 
                                    questionText, correctOption, selectedOption, 
                                    hasData ? (result ? "正确" : "错误") : "", null, null, time, grammarPoint));
                        }
                        
                        // 处理第四组的篇章理解能力
                        if (group == 4) {
                            // 尝试加载第十题的数据
                            boolean hasPassageData = false;
                            boolean passageResult = false;
                            String passageCorrectOption = "RIGHT";
                            String passageSelectedOption = "";
                            
                            String passageTime = "";
                            // 加载主题目数据（题号为10）
                            for (int j = 0; j < groupArray.length(); j++) {
                                Object arrayItem = groupArray.opt(j);
                                if (arrayItem instanceof JSONObject) {
                                    JSONObject itemObj = (JSONObject) arrayItem;
                                    int itemNum = itemObj.optInt("num", 0);
                                    if (itemNum == 10) {
                                        hasPassageData = true;
                                        passageSelectedOption = itemObj.optString("answer", "");
                                        passageResult = passageSelectedOption.equals(passageCorrectOption);
                                        passageTime = itemObj.optString("time", "");
                                        break;
                                    }
                                }
                            }
                            
                            // 加载小问题数据（题号为10.1, 10.2等）
                            boolean[] subQuestionResults = new boolean[6];
                            String[] subQuestionAnswers = new String[6];
                            int subCorrect = 0;
                            int subTotal = 6; // 6个小问
                            // 遍历所有子问题，尝试多种匹配方式
                            for (int j = 0; j < groupArray.length(); j++) {
                                Object arrayItem = groupArray.opt(j);
                                if (arrayItem instanceof JSONObject) {
                                    JSONObject itemObj = (JSONObject) arrayItem;
                                    
                                    // 检查是否是子问题（通过检查question字段是否包含小问题编号）
                                    String question = itemObj.optString("question", "");
                                    if (question.startsWith("1.") || question.startsWith("2.") || question.startsWith("3.") || 
                                        question.startsWith("4.") || question.startsWith("5.") || question.startsWith("6.")) {
                                        // 根据问题文本确定子问题索引
                                        int tempSubIndex = -1;
                                        if (question.startsWith("1.")) tempSubIndex = 0;
                                        else if (question.startsWith("2.")) tempSubIndex = 1;
                                        else if (question.startsWith("3.")) tempSubIndex = 2;
                                        else if (question.startsWith("4.")) tempSubIndex = 3;
                                        else if (question.startsWith("5.")) tempSubIndex = 4;
                                        else if (question.startsWith("6.")) tempSubIndex = 5;
                                        
                                        if (tempSubIndex >= 0 && tempSubIndex < 6) {
                                            subQuestionResults[tempSubIndex] = itemObj.optBoolean("result", false);
                                            subQuestionAnswers[tempSubIndex] = itemObj.optString("answer", "");
                                            if (subQuestionResults[tempSubIndex]) {
                                                subCorrect++;
                                            }
                                        }
                                    }
                                    
                                    // 也检查是否是子问题（通过检查num字段是否为小数）
                                    double itemNum = itemObj.optDouble("num", 0);
                                    if (itemNum >= 10.1 && itemNum <= 10.6) {
                                        int tempSubIndex = (int)((itemNum - 10) * 10) - 1;
                                        if (tempSubIndex >= 0 && tempSubIndex < 6) {
                                            subQuestionResults[tempSubIndex] = itemObj.optBoolean("result", false);
                                            subQuestionAnswers[tempSubIndex] = itemObj.optString("answer", "");
                                            if (subQuestionResults[tempSubIndex]) {
                                                subCorrect++;
                                            }
                                        }
                                    }
                                    
                                    // 也检查是否是子问题（通过检查num字段是否为整数101-106）
                                    int intItemNum = itemObj.optInt("num", 0);
                                    if (intItemNum >= 101 && intItemNum <= 106) {
                                        int tempSubIndex = intItemNum - 101;
                                        if (tempSubIndex >= 0 && tempSubIndex < 6) {
                                            subQuestionResults[tempSubIndex] = itemObj.optBoolean("result", false);
                                            subQuestionAnswers[tempSubIndex] = itemObj.optString("answer", "");
                                            if (subQuestionResults[tempSubIndex]) {
                                                subCorrect++;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // 更新篇章理解能力的得分
                            grammarPointTotal.put("篇章理解能力", subTotal);
                            grammarPointScores.put("篇章理解能力", subCorrect);
                            
                            // 添加篇章理解能力的题目
                            evaluations.add(new rg(10, String.valueOf(group), "10", "「朵朵的生日是在什么时候过的呀？」（今天晚上）", 
                                    passageCorrectOption, hasPassageData ? passageSelectedOption : "", 
                                    hasPassageData ? (passageResult ? "正确" : "错误") : "", null, null, passageTime, "篇章理解能力"));
                            
                            // 添加小问题，显示各自的答案和结果，但不显示时间
                            String[] subQuestions = {
                                "「是在什么地方过生日的呀？」（朵朵的家里）",
                                "「谁给朵朵过生日呀？」（全家人）",
                                "「一开始发生了什么让朵朵不开心的事呀？」（妈妈发现忘买生日蛋糕上的蜡烛了）",
                                "「爸爸想了什么办法呀？」（找来彩色小蜡笔，插在蛋糕上当蜡烛）",
                                "「最后朵朵的生日过得怎么样呀？」（朵朵笑了，全家人唱生日歌，她过得特别开心）"
                            };
                            
                            for (int i = 0; i < subQuestions.length; i++) {
                                boolean subResult = false;
                                String subAnswer = "";
                                if (i < subQuestionResults.length) {
                                    subResult = subQuestionResults[i];
                                    subAnswer = subQuestionAnswers[i];
                                    // 计算子问题的得分
                                    if (subResult) {
                                        countre++;
                                    }
                                    completedCount++;
                                }
                                // 小问题不显示时间，只显示空字符串
                                evaluations.add(new rg(10, String.valueOf(group), "", subQuestions[i], 
                                        "RIGHT", subAnswer, subResult ? "正确" : "错误", null, null, "", "篇章理解能力"));
                            }
                        }
                    }
                }
            }
            
            // 生成评估建议
            generateRGEvaluationSuggestion(countre, completedCount, grammarPointScores, grammarPointTotal);

        } else if(safeFormat.equals("SE")){
            // 强制横屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);
            
            // 初始化数据结构
            double countre=0;
            int completedCount = 0;
            
            // 存储每个语法点的得分情况
            HashMap<String, Integer> grammarPointScores = new HashMap<>();
            HashMap<String, Integer> grammarPointTotal = new HashMap<>();
            
            // 存储题目详情，按组分类
            Map<Integer, ArrayList<QuestionDetail>> groupQuestionDetails = new HashMap<>();
            
            // 定义题目数据 - 按照新的格式重新组织
            // 第一组
            String[] group1Questions = {
                "猫咪跑", "爸爸喝", "妹妹哭",
                "洗手", "刷牙", "吹泡泡",
                "蓝色的袜子", "小橙子", "脏脏的裤子",
                "哥哥骑自行车", "奶奶擦桌子", "熊猫看书",
                "袜子不能吃", "女孩手里没有汽车，空着手", "小猪不会爬树。"
            };
            String[] group1GrammarPoints = {
                "主谓结构", "主谓结构", "主谓结构",
                "动宾结构", "动宾结构", "动宾结构",
                "形容词+名词", "形容词+名词", "形容词+名词",
                "主谓宾", "主谓宾", "主谓宾",
                "否定句", "否定句", "否定句"
            };
            
            // 第二组
            String[] group2Questions = {
                "给妹妹礼物", "喂小狗骨头", "给弟弟汽车",
                "爸爸买蛋糕了么？", "我能玩机器人么？", "外面下雨了么？",
                "盒子里是什么？/里面是什么？", "谁的球？/谁干的？/谁把窗户打破了？", "小汽车在哪里？",
                "小狗在椅子下面。", "蛋糕在盒子里。", "兔子在猴子中间。",
                "小白狗", "黄色小熊上衣", "紫色三角形积木"
            };
            String[] group2GrammarPoints = {
                "双宾结构", "双宾结构", "双宾结构",
                "一般疑问句", "一般疑问句", "一般疑问句",
                "特殊疑问句", "特殊疑问句", "特殊疑问句",
                "地点/方位", "地点/方位", "地点/方位",
                "多重修饰", "多重修饰", "多重修饰"
            };
            
            // 第三组
            String[] group3Questions = {
                "竹子被熊猫吃了。", "妹妹追小狗。", "宝宝被妈妈抱着/宝宝被妈妈抱在怀里。",
                "妹妹吃完了。", "弟弟拼完了。/弟弟拼图拼完了。", "叔叔画完了。",
                "房间里的灯都亮着。", "小朋友都背着书包。", "小朋友都在跑步。",
                "妈妈抱宝宝。", "爸爸推奶奶。/爸爸推着坐轮椅的奶奶。", "兔子追乌龟。",
                "小朋友先刷牙再睡觉。", "小朋友先洗手再吃饭。", "阿姨先换鞋再进屋。"
            };
            String[] group3GrammarPoints = {
                "被动句", "被动句", "被动句",
                "了（完成体）", "了（完成体）", "了（完成体）",
                "副词\"都\"", "副词\"都\"", "副词\"都\"",
                "语序", "语序", "语序",
                "时间顺序句", "时间顺序句", "时间顺序句"
            };
            
            // 第四组
            String[] group4Questions = {
                "因为摔倒了，所以小朋友哭了。", "因为下雨了，所以打着伞。", "因为天气冷，所以打喷嚏了。",
                "小狗想进屋，但是门关着", "小朋友想骑自行车，但是车坏了", "小朋友想吃饼干，但是他够不着",
                "哥哥正在喝果汁", "小朋友正在吹气球", "爷爷正在钓鱼",
                "如果天气很热，就吹风扇", "如果下雨了，就打伞", "如果太阳出来，雪人就会化",
                "男孩比女孩高。/女孩子比男孩子矮。", "男孩的花比女孩的花多。/女孩的花比男孩子的花少。", "红铅笔比蓝铅笔长。/蓝铅笔比红铅笔短。",
                "一个小女孩在糖果店买了很多糖果。\n小女孩回到家，坐在沙发上，开心地吃着糖果。\n夜晚，小女孩躺在床上，突然捂着脸，牙齿开始痛。\n第二天，小女孩来诊所看医生，牙医给她检查了牙齿。\n小女孩以后再也不敢吃糖了。"
            };
            String[] group4GrammarPoints = {
                "因果复句", "因果复句", "因果复句",
                "转折句", "转折句", "转折句",
                "正在/在/着（进行体）", "正在/在/着（进行体）", "正在/在/着（进行体）",
                "假设条件句", "假设条件句", "假设条件句",
                "比较句", "比较句", "比较句",
                "看图讲故事"
            };
            
            // 初始化语法点得分和总分
            String[] allGrammarPoints = {
                "主谓结构", "动宾结构", "形容词+名词", "主谓宾", "否定句",
                "双宾结构", "一般疑问句", "特殊疑问句", "地点/方位", "多重修饰",
                "被动句", "了（完成体）", "副词\"都\"", "语序", "时间顺序句",
                "因果复句", "转折句", "正在/在/着（进行体）", "假设条件句", "比较句", "看图讲故事"
            };
            for (String point : allGrammarPoints) {
                grammarPointScores.put(point, 0);
                grammarPointTotal.put(point, 0);
            }
            
            // 加载数据
            JSONArray jsonArray = null;
            JSONObject evaluationsObj = data.getJSONObject("evaluations");
            // 打印日志，查看evaluationsObj的内容
            System.out.println("SE - evaluationsObj: " + evaluationsObj.toString());
            for (int i = 1; i <= 4; i++) {
                String seKey = "SE" + i;
                System.out.println("SE - Checking key: " + seKey);
                if (evaluationsObj.has(seKey)) {
                    JSONArray groupArray = evaluationsObj.getJSONArray(seKey);
                    System.out.println("SE - Group " + i + " has " + groupArray.length() + " items");
                    if (groupArray.length() > 0) {
                        if (jsonArray == null) {
                            jsonArray = groupArray;
                        } else {
                            for (int j = 0; j < groupArray.length(); j++) {
                                jsonArray.put(groupArray.get(j));
                            }
                        }
                    }
                }
            }
            if (jsonArray == null) {
                jsonArray = evaluationsObj.optJSONArray("SE");
                System.out.println("SE - Old SE array has " + (jsonArray != null ? jsonArray.length() : 0) + " items");
                if (jsonArray == null) {
                    jsonArray = new JSONArray();
                }
            }
            System.out.println("SE - Final jsonArray has " + jsonArray.length() + " items");
            
            // 强制横屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            
            // 添加表头
            evaluations.add(new se(0, "测试阶段", "题号", "例句", "正确选项", "被选选项", "", null, null, "", "对应测试语法点"));
            
            // 按组处理数据，确保顺序正确
            for (int group = 1; group <= 4; group++) {
                ArrayList<QuestionDetail> groupDetails = new ArrayList<>();
                boolean hasCompletedQuestions = false;
                
                // 根据组号获取对应的数据
                String seKey = "SE" + group;
                JSONArray groupArray = evaluationsObj.optJSONArray(seKey);
                
                if (groupArray != null && groupArray.length() > 0) {
                    hasCompletedQuestions = true;
                    
                    // 处理该组的题目
                    for (int i = 0; i < groupArray.length(); i++) {
                        JSONObject object = groupArray.getJSONObject(i);
                        if(object.has("time")&&!object.isNull("time")){
                            boolean result = object.getBoolean("result");
                            if(result){
                                countre++;
                            }
                            
                            // 确定题目所属的组和语法点
                            int questionNum = object.optInt("num", i+1);
                            String grammarPoint = "";
                            String questionText = "";
                            String questionNumber = "";
                            int groupQuestionIndex = 0;
                            
                            // 根据题目编号确定组内索引和语法点
                            switch (group) {
                                case 1:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group1Questions.length) {
                                        questionText = group1Questions[groupQuestionIndex];
                                        grammarPoint = group1GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf(groupQuestionIndex + 1);
                                    }
                                    break;
                                case 2:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group2Questions.length) {
                                        questionText = group2Questions[groupQuestionIndex];
                                        grammarPoint = group2GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf(groupQuestionIndex + 1);
                                    }
                                    break;
                                case 3:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group3Questions.length) {
                                        questionText = group3Questions[groupQuestionIndex];
                                        grammarPoint = group3GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf(groupQuestionIndex + 1);
                                    }
                                    break;
                                case 4:
                                    groupQuestionIndex = questionNum - 1;
                                    if (groupQuestionIndex >= 0 && groupQuestionIndex < group4Questions.length) {
                                        questionText = group4Questions[groupQuestionIndex];
                                        grammarPoint = group4GrammarPoints[groupQuestionIndex];
                                        questionNumber = String.valueOf(groupQuestionIndex + 1);
                                    }
                                    break;
                            }
                            
                            // 更新语法点得分
                            if (!grammarPoint.isEmpty()) {
                                grammarPointTotal.put(grammarPoint, grammarPointTotal.get(grammarPoint) + 1);
                                if (result) {
                                    grammarPointScores.put(grammarPoint, grammarPointScores.get(grammarPoint) + 1);
                                }
                            }
                            
                            // 添加到题目详情列表
                            String correctOption = object.optString("correctOption", "");
                            String selectedOption = object.optString("selectedOption", "");
                            String time = object.optString("time", "");
                            
                            groupDetails.add(new QuestionDetail(questionNum, questionNumber, questionText, 
                                    correctOption, selectedOption, result, time, grammarPoint, group));
                            completedCount++;
                        }
                    }
                }
                
                // 如果该组有完成的题目，添加到映射中
                if (hasCompletedQuestions && !groupDetails.isEmpty()) {
                    groupQuestionDetails.put(group, groupDetails);
                }
                
                // 如果该组有完成的题目，添加一个空行作为分隔
                if (hasCompletedQuestions) {
                    evaluations.add(new se(-1, "", "", "", "", "", "", null, null, "", ""));
                }
            }
            

            
            // 清空之前的评估结果
            evaluations.clear();
            
            // 添加表头
            evaluations.add(new se(0, "测试阶段", "题  号", "例  句", "正确选项", "被选选项", "测试结果", null, null, "答题时间", "对应测试语法点"));
            
            // 定义题目数据 - 按照新的格式重新组织
            // 第一组
            String[] seGroup1Questions = {
                "猫咪跑", "爸爸喝", "妹妹哭",
                "洗手", "刷牙", "吹泡泡",
                "蓝色的袜子", "小橙子", "脏脏的裤子",
                "哥哥骑自行车", "奶奶擦桌子", "熊猫看书",
                "袜子不能吃", "女孩手里没有汽车，空着手", "小猪不会爬树。"
            };
            String[] seGroup1GrammarPoints = {
                "主谓结构", "主谓结构", "主谓结构",
                "动宾结构", "动宾结构", "动宾结构",
                "形容词+名词", "形容词+名词", "形容词+名词",
                "主谓宾", "主谓宾", "主谓宾",
                "否定句", "否定句", "否定句"
            };
            
            // 第二组
            String[] seGroup2Questions = {
                "给妹妹礼物", "喂小狗骨头", "给弟弟汽车",
                "爸爸买蛋糕了么？", "我能玩机器人么？", "外面下雨了么？",
                "盒子里是什么？/里面是什么？", "谁的球？/谁干的？/谁把窗户打破了？", "小汽车在哪里？",
                "小狗在椅子下面。", "蛋糕在盒子里。", "兔子在猴子中间。",
                "小白狗", "黄色小熊上衣", "紫色三角形积木"
            };
            String[] seGroup2GrammarPoints = {
                "双宾结构", "双宾结构", "双宾结构",
                "一般疑问句", "一般疑问句", "一般疑问句",
                "特殊疑问句", "特殊疑问句", "特殊疑问句",
                "地点/方位", "地点/方位", "地点/方位",
                "多重修饰", "多重修饰", "多重修饰"
            };
            
            // 第三组
            String[] seGroup3Questions = {
                "竹子被熊猫吃了。", "妹妹追小狗。", "宝宝被妈妈抱着/宝宝被妈妈抱在怀里。",
                "妹妹吃完了。", "弟弟拼完了。/弟弟拼图拼完了。", "叔叔画完了。",
                "房间里的灯都亮着。", "小朋友都背着书包。", "小朋友都在跑步。",
                "妈妈抱宝宝。", "爸爸推奶奶。/爸爸推着坐轮椅的奶奶。", "兔子追乌龟。",
                "小朋友先刷牙再睡觉。", "小朋友先洗手再吃饭。", "阿姨先换鞋再进屋。"
            };
            String[] seGroup3GrammarPoints = {
                "被动句", "被动句", "被动句",
                "了（完成体）", "了（完成体）", "了（完成体）",
                "副词\"都\"", "副词\"都\"", "副词\"都\"",
                "语序", "语序", "语序",
                "时间顺序句", "时间顺序句", "时间顺序句"
            };
            
            // 第四组
            String[] seGroup4Questions = {
                "因为摔倒了，所以小朋友哭了。", "因为下雨了，所以打着伞。", "因为天气冷，所以打喷嚏了。",
                "小狗想进屋，但是门关着", "小朋友想骑自行车，但是车坏了", "小朋友想吃饼干，但是他够不着",
                "哥哥正在喝果汁", "小朋友正在吹气球", "爷爷正在钓鱼",
                "如果天气很热，就吹风扇", "如果下雨了，就打伞", "如果太阳出来，雪人就会化",
                "男孩比女孩高。/女孩子比男孩子矮。", "男孩的花比女孩的花多。/女孩的花比男孩子的花少。", "红铅笔比蓝铅笔长。/蓝铅笔比红铅笔短。",
                "一个小女孩在糖果店买了很多糖果。\n小女孩回到家，坐在沙发上，开心地吃着糖果。\n夜晚，小女孩躺在床上，突然捂着脸，牙齿开始痛。\n第二天，小女孩来诊所看医生，牙医给她检查了牙齿。\n小女孩以后再也不敢吃糖了。"
            };
            String[] seGroup4GrammarPoints = {
                "因果复句", "因果复句", "因果复句",
                "转折句", "转折句", "转折句",
                "正在/在/着（进行体）", "正在/在/着（进行体）", "正在/在/着（进行体）",
                "假设条件句", "假设条件句", "假设条件句",
                "比较句", "比较句", "比较句",
                "看图讲故事"
            };
            
            // 按组处理数据，确保顺序正确
            for (int group = 1; group <= 4; group++) {
                // 根据组号获取对应的数据
                String seKey = "SE" + group;
                JSONArray groupArray = evaluationsObj.optJSONArray(seKey);
                
                // 只有当该组有数据时才处理
                if (groupArray != null && groupArray.length() > 0) {
                    // 添加组标题
                    evaluations.add(new se(-3, "", "第" + group + "组", "", "", "", "", null, null, "", ""));
                    
                    // 处理该组的题目
                    String[] questions = null;
                    String[] grammarPoints = null;
                    
                    switch (group) {
                        case 1:
                            questions = seGroup1Questions;
                            grammarPoints = seGroup1GrammarPoints;
                            break;
                        case 2:
                            questions = seGroup2Questions;
                            grammarPoints = seGroup2GrammarPoints;
                            break;
                        case 3:
                            questions = seGroup3Questions;
                            grammarPoints = seGroup3GrammarPoints;
                            break;
                        case 4:
                            questions = seGroup4Questions;
                            grammarPoints = seGroup4GrammarPoints;
                            break;
                    }
                    
                    if (questions != null && grammarPoints != null) {
                        for (int i = 0; i < questions.length; i++) {
                            String questionText = questions[i];
                            String grammarPoint = grammarPoints[i];
                            
                            // 计算题目编号，按照1-16的顺序显示
                            int questionNumber = i + 1;
                            
                            // 尝试从分组数组中加载数据
                            boolean result = false;
                            boolean hasData = false;
                            String correctOption = "";
                            String selectedOption = "";
                            String time = "";
                            
                            // 搜索分组数组中对应题号的记录
                            JSONObject itemObj = null;
                            for (int j = 0; j < groupArray.length(); j++) {
                                Object arrayItem = groupArray.opt(j);
                                if (arrayItem instanceof JSONObject) {
                                    itemObj = (JSONObject) arrayItem;
                                    int itemNum = itemObj.optInt("num", 0);
                                    // 匹配题号
                                    if (itemNum == (i + 1)) {
                                        hasData = true;
                                        // 尝试从right_option字段获取正确选项
                                        correctOption = itemObj.optString("right_option", "");
                                        // 尝试从answer字段获取被选选项
                                        selectedOption = itemObj.optString("answer", "");
                                        time = itemObj.optString("time", "");
                                        
                                        // 计算结果：如果被选选项与正确选项相同，则为正确
                                        if (!correctOption.isEmpty() && !selectedOption.isEmpty()) {
                                            // 处理RIGHT/WRONG类型的正确选项
                                            if (correctOption.equals("RIGHT")) {
                                                // 正确按钮的题目，被选选项为RIGHT则正确
                                                result = selectedOption.equals("RIGHT");
                                            } else if (correctOption.equals("WRONG")) {
                                                // 错误按钮的题目，被选选项为WRONG则正确
                                                result = selectedOption.equals("WRONG");
                                            } else {
                                                // 其他类型的题目，直接比较选项
                                                result = correctOption.equals(selectedOption);
                                            }
                                        } else {
                                            // 如果没有正确选项或被选选项，使用原始result字段
                                            result = itemObj.optBoolean("result", false);
                                        }
                                        break;
                                    }
                                }
                            }
                            
                            // 如果没有找到正确选项，尝试根据题目编号和组别设置默认正确选项
                            if (correctOption.isEmpty()) {
                                correctOption = getCorrectOptionForQuestion(group, i + 1);
                            }
                            
                            // 加载录音数据
                            bean.audio audio = null;
                            if (hasData && itemObj != null) {
                                String audioPath = itemObj.optString("audioPath", "");
                                if (!audioPath.isEmpty()) {
                                    audio = new bean.audio(audioPath);
                                }
                            }
                            
                            // 添加题目数据
                            evaluations.add(new se(i + 1, String.valueOf(group), String.valueOf(questionNumber), 
                                    questionText, correctOption, selectedOption, 
                                    hasData ? (result ? "正确" : "错误") : "", null, audio, time, grammarPoint));
                        }
                    }
                }
            }
            
            // 生成评估建议
            generateSEEvaluationSuggestion(countre, completedCount, grammarPointScores, grammarPointTotal);

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

            evaluations.add(new pl(0, null, null, null, null, null, null));//首行
            List<pl> plItems = new ArrayList<>();
            JSONObject report = ModuleReportHelper.loadPrelinguisticReport(data);
            String scene = report != null ? report.optString("scene", "") : "";
            if (scene == null || scene.trim().isEmpty()) {
                scene = "A";
            }
            JSONObject evaluationsObject = data.getJSONObject("evaluations");
            String plKey = "PL_" + scene;
            JSONArray jsonArray = evaluationsObject.optJSONArray(plKey);
            if (jsonArray == null) {
                jsonArray = evaluationsObject.optJSONArray(MODULE_PL);
            }
            if (jsonArray == null) {
                jsonArray = new JSONArray();
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
            
            // 隐藏硬编码的需要重点关注的能力标题
            View weaknessTitle = findViewById(R.id.weakness_title);
            if (weaknessTitle != null) {
                weaknessTitle.setVisibility(View.GONE);
            }
            // 隐藏硬编码的不稳定的能力标题
            View inProgressTitle = findViewById(R.id.in_progress_title);
            if (inProgressTitle != null) {
                inProgressTitle.setVisibility(View.GONE);
            }
        } else if (safeFormat.equals("SOCIAL")) {
            // 强制横屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (extraAResults != null) extraAResults.setVisibility(View.GONE);
            if (extraASuggestions != null) extraASuggestions.setVisibility(View.GONE);


            // 清空之前的评估结果
            evaluations.clear();
            
            // 定义变量
            int totalScore = 0;
            int completedQuestions = 0;
            List<String> weaknesses = new ArrayList<>();
            List<String> inProgress = new ArrayList<>();
            
            try {
                // 检查数据是否存在
                if (data == null) {
                    Toast.makeText(this, "数据加载失败：数据为空！", Toast.LENGTH_SHORT).show();
                    evaluations.add(new social(-1, "错误", "数据为空", "", null, null, null, null));
                    if (tv2 != null) {
                        tv2.setText("（二）评估建议\n\n数据加载失败：数据为空！");
                    }
                    return;
                }
                
                JSONObject evaluationsObj = data.optJSONObject("evaluations");
                if (evaluationsObj == null) {
                    Toast.makeText(this, "数据加载失败：评估数据为空！", Toast.LENGTH_SHORT).show();
                    evaluations.add(new social(-1, "错误", "评估数据为空", "", null, null, null, null));
                    if (tv2 != null) {
                        tv2.setText("（二）评估建议\n\n数据加载失败：评估数据为空！");
                    }
                    return;
                }
                
                // 添加表头
                evaluations.add(new social(0, "", "", "", null, null, null, null));
                
                // 创建一个列表来存储社交能力评估项目
                List<social> socialItems = new ArrayList<>();
                
                // 定义题目数据
                String[][] groupData = {
                    // 第一组
                    {
                        "1", "轮流互动", "1. 孩子在和大人玩耍时，会轮流发出声音或做动作\n例，你拍手，孩子也拍手；或者你说\"啊\"，孩子也回应。",
                        "2", "互动期待", "2. 在互动中，孩子会看向你，好像在等你回应\n例，玩耍时，孩子会看看你，好像在确认\"你看到了吗\"。",
                        "3", "回应性共同关注", "3. 当你指着某样东西或看向某处时，孩子会顺着你的指向一起看\n例，你指小狗或玩具车，孩子也转头看过去。",
                        "4", "意图性沟通", "4. 孩子会用手势、声音或者拉人来表达需求\n例，想要东西时，会指、拉大人、发出声音引起注意。",
                        "5", "发起共同关注", "5. 孩子会把自己感兴趣的东西拿给你看\n例，孩子会举起玩具给你看或者递给你，而不只是自己玩。",
                        "6", "行为模仿", "6. 看到你或别人做动作时，孩子会模仿\n例，别的小朋友拍手、挥手，孩子也会跟着做。",
                        "7", "情绪觉察", "7. 孩子对他人的情绪有基本反应\n例，别的孩子哭了，孩子会停下来看看，或者看向大人。",
                        "8", "早期语音/词汇模仿", "8. 孩子会模仿你说的词语或简单表达\n例，大人说\"车车\"\"汪汪\"，孩子会跟着说或发声模仿。",
                        "9", "早期同伴注意与互动萌芽", "9. 和其他孩子在一起时，会有短暂互动\n例，和别的小朋友你看我、我看你，笑一下、做个动作。",
                        "10", "社交动机", "10. 当你停止互动时，孩子会表现出想继续玩的样子\n例，孩子不想结束游戏时，会拉你、看你、发声表示\"还要\"。"
                    },
                    // 第二组
                    {
                        "11", "常规性的社交互动行为", "11. 孩子会用手势或简单语言向人打招呼或说再见\n例，孩子会说\"你好\"\"拜拜\"，或者挥手。",
                        "12", "对非语言指令的理解", "12. 当大人拍手、招手或示意时，孩子能理解并做出简单回应\n例，大人招手示意孩子过来，孩子会看向大人并且走过去。",
                        "13", "对语言指令的理解", "13. 当你提出简单要求或指令时，孩子能理解并尝试去做\n例，能听懂\"给我\"\"拿过来\"等指令并照做。",
                        "14", "规则性轮流互动", "14. 孩子能参与简单轮流游戏\n例，比如你扔球给孩子，孩子会接球再扔回去。",
                        "15", "意图的明确表达", "15. 孩子会表达想玩或不想玩的意愿\n例，会说\"我要\"\"不要\"\"不想玩\"，或者用动作表示。",
                        "16", "自我情绪觉察与表达", "16. 孩子会用简单方式表达基本感受（开心、生气、害怕等）\n例，如说\"开心\"\"我不高兴\"，或者皱眉、拍手表示情绪。",
                        "17", "发起与回应社交互动", "17. 孩子会尝试做简单邀请或回应别人邀请\n例，会拉人或说\"来\"，尝试让别人一起玩；或者有人说\"一起玩吧\"，孩子会跟过去或加入。",
                        "18", "社交注视与互动", "18. 和别人互动时，孩子会注视对方的脸并做出反应\n例，说话或玩耍时会看着对方，模仿表情或动作。",
                        "19", "早期假装游戏中的社交参与", "19.孩子能在成人引导下参与具有简单情境的假装互动游戏。\n例，大人假装给娃娃喂饭、打电话或睡觉时，孩子会模仿相关动作，或用声音、词语进行回应，与大人形成简单互动。",
                        "20", "早期个人经历叙述", "20. 在大人的支持下，孩子能讲述简单经历或片段\n例，比如\"公园\"\"滑梯\"\"妈妈\"，能说出自己做过的事情或看到的东西。"
                    },
                    // 第三组
                    {
                        "21", "同伴互动中的分享与轮流", "21. 孩子能与同伴一起玩玩具或游戏，并理解轮流概念\n例，知道\"轮到我 / 轮到你\"，能把玩具给别人再拿回来玩。",
                        "22", "规则意识与简单合作", "22. 孩子在游戏中能遵守基本规则并与他人合作\n例，玩游戏时，知道要按规则来，比如不能一直自己玩、不让别人玩。",
                        "23", "社交主动性及合作规划", "23. 孩子会主动与同伴交流想法和计划活动\n例，一起搭积木，一起玩过家家，会说\"我们一起搭房子\"。",
                        "24", "情绪识别与早期共情能力", "24. 孩子会看出别人明显的情绪并作出简单回应\n例，看到别人难过，会停下来看看，或者问\"你怎么了？\"",
                        "25", "情绪归因与初级推理能力", "25. 在成人提问下，孩子能说出别人为什么会有这种情绪\n例，大人问\"他为什么哭了？\"，孩子回答\"他摔了\"或\"玩具坏了\"。",
                        "26", "冲突情境下的自我表达与求助", "26. 孩子能够尝试用语言解决小冲突，但可能需要成人帮助\n例，发生争抢时，会说\"这是我的\"\"我先玩\"，但还需要大人帮忙。",
                        "27", "集体情境下的社会参与行为调节", "27. 孩子在集体活动中能保持注意并参与\n例，上课、集体游戏时，能跟着做，不会马上走开。",
                        "28", "社交情境中的语言表达", "28. 孩子会用完整句子描述自己在做什么或想什么\n例，会说类似\"我在画画\"\"我有新车\"来表达想法。",
                        "29", "寻求帮助", "29. 当互动中出现不明白或困难时，孩子能主动向大人或同伴寻求帮助\n例，会说\"这个怎么弄？\"、\"我不会\"，寻求帮助。",
                        "30", "初步心智解读", "30. 孩子能初步理解别人想法或兴趣，并作出回应\n例，孩子玩积木时，别的小朋友走过来靠近他，孩子会说\"一起玩吧\"，或把积木递给同伴。"
                    },
                    // 第四组
                    {
                        "31", "合作性假装游戏能力", "31. 孩子在游戏中分配或者接受角色\n例，玩\"过家家\"或者\"买东西\"时，会说谁当什么、怎么玩。",
                        "32", "情绪识别与共情能力", "32. 孩子能理解并尊重他人的表情和情绪\n例，知道别人不开心时，不能继续闹或抢。",
                        "33", "对话的轮流与规则意识", "33. 孩子能理解并按照社交模式轮流说话\n例，知道别人说完再说，不总是插话。",
                        "34", "维持同伴互动与游戏的能力", "34. 孩子与同伴能持续进行互动游戏（约5–10分钟）\n例，能和同一个孩子一起玩一会儿，而不是马上换。",
                        "35", "叙事中的情感理解与推理", "35. 孩子能在讲故事或游戏中理解角色情绪或行为\n例，看绘本时看到小朋友哭了，会说\"他摔倒了，哭了。\"",
                        "36", "心智理论萌发", "36. 孩子能初步理解别人观点可能与自己不同\n例，看到同伴选择不同游戏，会说\"他想玩这个，我不想玩\"。",
                        "37", "情境理解与行为调节", "37. 孩子能够根据不同场合调整行为\n例，在教室、图书馆会安静，在操场可以更活跃。",
                        "38", "在支持下解决冲突", "38. 在出现分歧时，孩子在成人支持下能够提出解决办法\n例，发生争抢或意见不一致时，会尝试换玩法、轮流或请老师帮助。",
                        "39", "社交发起与组织能力", "39. 孩子会邀请同伴加入游戏并解释玩法\n例，会说\"一起玩吧，这样玩\"。",
                        "40", "群体会话能力", "40. 孩子在群体互动中能表达自己意见并倾听他人\n例，能说自己的想法，也会听别人说。"
                    },
                    // 第五组
                    {
                        "41", "规则意识与自控力", "41. 孩子能在多人游戏中理解并坚持规则\n例，玩桌游、集体游戏时，能遵守规则，不随意破坏或插队。",
                        "42", "维持对话的能力", "42. 孩子能与同伴进行较长的社交对话\n例，和同伴你一句我一句地聊一会儿，比如讨论玩具或刚做过的事情。",
                        "43", "亲社会行为与共情能力", "43. 孩子能在游戏或活动中主动帮助或支持同伴\n例，看到小朋友做不到某件事时，会主动说\"我来帮你\"或者示范怎么做，比如帮同伴搭积木、整理玩具、一起完成任务。",
                        "44", "高级对话规则", "44. 孩子能在讨论中适当地轮流发言、等待与倾听\n例，别人说话时能认真听，轮到自己再说话，不随意打断别人。",
                        "45", "复杂情绪感知与回应", "45. 孩子能体会并回应较复杂情绪\n例，知道别人\"失望\"\"不好意思\"\"兴奋\"，能说\"没关系、太棒了等\"等来回应别人。",
                        "46", "目标导向的合作", "46. 孩子能与同伴合作完成有目标的任务\n例，一起搭积木、做手工或合作游戏时，共同完成任务。",
                        "47", "初步心智理论", "47. 孩子能理解别人可能和自己想的不一样\n例，回答\"如果你是他，你会怎么做？\"时，能理解对方的想法可能不同于自己。",
                        "48", "社会问题解决能力", "48. 孩子能主动尝试解决问题或提出建议\n例，遇到游戏困难，能说\"我们换个办法\"或提出新玩法。",
                        "49", "意图理解与归因", "49. 孩子能理解并推测别人行为背后的原因或意图\n例，看到同伴拿走玩具，会说\"他想玩这个，不是故意的\"。",
                        "50", "复杂语言组织与规划能力", "50. 孩子能表达复杂愿望、计划或合作想法\n例，说\"我们周末先去公园玩滑梯，然后去吃冰淇淋\"，或者讨论合作完成某件事。"
                    },
                    // 第六组
                    {
                        "51", "规则内化与社会责任感", "51. 孩子能在多人活动中遵守并维护共同规则\n例，玩桌游、排队或集体游戏时，知道规则并提醒或帮助其他小朋友遵守。",
                        "52", "社交互动的自主管理", "52. 孩子能主动开始、维持并结束社交互动\n例，会主动找小朋友玩，玩了一会儿会自己说\"我们休息一下\"或\"下次再玩\"。",
                        "53", "有组织的合作能力", "53. 孩子能与同伴分工、协作完成较复杂的任务\n例，一起搭积木、做小组手工或完成拼图，知道分工、协作完成目标。",
                        "54", "沟通修复", "54. 在对话中出现没听懂或误解时，孩子能主动澄清或者解释。\n例，孩子提出玩某项游戏，但其他小朋友表现得很困惑，孩子会主动用更详细或者更具体地说法来进行解释。",
                        "55", "高级情绪理解及共情", "55. 孩子能理解并回应他人较复杂或矛盾的情绪\n例，知道别人\"生气又害怕\"\"高兴又有点紧张\"，能说\"没关系，我帮你\"或安慰。",
                        "56", "社交推理能力", "56. 孩子能理解并使用言外之意\n例，理解别人说\"你真慢呀\"可能是提醒快一点",
                        "57", "社会性问题解决与策略", "57. 孩子在面对社交问题时，能比较不同做法并选择较合适的方案\n例，玩具争抢或意见不同，能说\"我们轮流玩\"或者\"我先做这个，你先玩那个\"。",
                        "58", "情境化的自我控制", "58. 孩子能在集体情境中自我调节行为\n例，上课或排队时，能控制冲动、等待轮到自己，不打扰别人。",
                        "59", "高阶心智理论", "59. 孩子能预测自己言行对他人的影响\n例，说\"如果我这样说，朋友可能会生气/开心\"，并据此调整自己的行为。",
                        "60", "协商性沟通与群体决策", "60. 孩子能清楚表达计划、理由，并与他人协商达成一致\n例，说\"我们先一起搭积木，然后去玩滑梯，这样大家都能玩到\"，并和小朋友商量最后达成一致意见。"
                    }
                };
                
                // 按组处理题目
                
                for (int group = 0; group < groupData.length; group++) {
                    boolean hasCompletedQuestions = false;
                    String[] groupQuestions = groupData[group];
                    
                    // 尝试从分组数组中加载数据
                    String socialKey = "SOCIAL" + (group + 1);
                    JSONArray groupArray = evaluationsObj.optJSONArray(socialKey);
                    
                    // 如果该组有数据，添加组标题
                    if (groupArray != null && groupArray.length() > 0) {
                        evaluations.add(new social(-3, "第" + (group + 1) + "组", "", "", null, null, null, null));
                    }
                    
                    // 处理该组的题目
                    for (int i = 0; i < groupQuestions.length; i += 3) {
                        try {
                            if (i + 2 >= groupQuestions.length) {
                                // 防止数组越界
                                break;
                            }
                            
                            String questionNumber = groupQuestions[i];
                            String ability = groupQuestions[i + 1];
                            String content = groupQuestions[i + 2];
                            
                            int num = Integer.parseInt(questionNumber);
                            social item = new social(num, ability, ability, content, null, null, null, null);
                            
                            // 尝试从分组数组中加载数据
                            Integer score = null;
                            
                            if (groupArray != null && groupArray.length() > 0) {
                                // 尝试两种索引计算方式，以兼容不同版本的数据结构
                                // 方式1：按组内顺序索引（新版本）
                                int groupIndex = i / 3;
                                if (groupIndex >= 0 && groupIndex < groupArray.length()) {
                                    Object arrayItem = groupArray.opt(groupIndex);
                                    if (arrayItem instanceof JSONObject) {
                                        JSONObject itemObj = (JSONObject) arrayItem;
                                        score = itemObj.optInt("score", -1);
                                        if (score != -1) {
                                            item.setScore(score);
                                            item.setTime(itemObj.optString("time", null));
                                        }
                                    }
                                }
                                
                                // 如果方式1没有找到分数，尝试方式2：按题目编号索引（旧版本）
                                if (score == null || score == -1) {
                                    int startIndex = group * 10;
                                    int index = num - startIndex - 1;
                                    if (index >= 0 && index < groupArray.length()) {
                                        Object arrayItem = groupArray.opt(index);
                                        if (arrayItem instanceof JSONObject) {
                                            JSONObject itemObj = (JSONObject) arrayItem;
                                            score = itemObj.optInt("score", -1);
                                            if (score != -1) {
                                                item.setScore(score);
                                                item.setTime(itemObj.optString("time", null));
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // 检查是否有得分
                            if (item.getScore() != null) {
                                hasCompletedQuestions = true;
                                completedQuestions++;
                                totalScore += item.getScore();
                                
                                // 收集需要重点关注和进一步发展的能力
                                if (item.getScore() == 1 && !inProgress.contains(ability)) {
                                    inProgress.add(ability);
                                } else if (item.getScore() == 0 && !weaknesses.contains(ability)) {
                                    weaknesses.add(ability);
                                }
                            }
                            
                            // 只添加有得分的题目
                            if (item.getScore() != null) {
                                evaluations.add(item);
                                socialItems.add(item);
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            // 添加错误提示
                            evaluations.add(new social(-1, "错误", "题目编号格式错误", "", null, null, null, null));
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 添加错误提示
                            evaluations.add(new social(-1, "错误", "加载题目失败", "", null, null, null, null));
                        }
                    }
                    
                    // 如果该组有完成的题目，添加一个空行作为分隔
                    if (hasCompletedQuestions) {
                        evaluations.add(new social(-1, "", "", "", null, null, null, null));
                    }
                }
                
                // 检查是否有数据
                if (completedQuestions == 0) {
                    Toast.makeText(this, "未找到社交能力评估数据！", Toast.LENGTH_SHORT).show();
                    evaluations.add(new social(-1, "提示", "未找到社交能力评估数据", "", null, null, null, null));
                    if (tv2 != null) {
                        tv2.setText("（二）评估建议\n\n未找到社交能力评估数据！");
                    }
                    return;
                }
                
                // 计算总体评估结果
                double accuracy = (double) totalScore / (completedQuestions * 2);
                
                // 生成评估建议
                StringBuilder detailedAssessment = new StringBuilder();
                detailedAssessment.append("（二）评估建议\n\n");
                detailedAssessment.append("通过对儿童社交能力的评估（家长试卷）结果如下：\n\n");
                
                // 根据正确率选择评估结果
                if (accuracy >= 0.6) {
                    detailedAssessment.append("1.从整体上来说，孩子的社交能力较好，基本达标。\n\n");
                } else {
                    detailedAssessment.append("2.从整体上来说，孩子的社交能力还有待进一步发展，尚未达标。\n\n");
                }
                
                // 显示需要重点关注的能力
                detailedAssessment.append("1.需要重点关注：\n");
                if (!weaknesses.isEmpty()) {
                    for (int i = 0; i < weaknesses.size(); i++) {
                        detailedAssessment.append((i + 1)).append(". ").append(weaknesses.get(i)).append("\n");
                    }
                } else {
                    detailedAssessment.append("无\n");
                }
                detailedAssessment.append("\n");
                
                // 显示需要进一步发展的能力
                detailedAssessment.append("2.有些社交能力已经表现出来，但是不够经常，我们可以多去发展以下能力：\n");
                if (!inProgress.isEmpty()) {
                    for (int i = 0; i < inProgress.size(); i++) {
                        detailedAssessment.append((i + 1)).append(". ").append(inProgress.get(i)).append("\n");
                    }
                } else {
                    detailedAssessment.append("无\n");
                }
                detailedAssessment.append("\n");
                
                if (tv2 != null) {
                    tv2.setText(detailedAssessment.toString());
                }


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "加载社交能力报告失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                // 添加一个错误提示项
                evaluations.add(new social(-1, "加载失败", "请重新尝试", "", null, null, null, null));
                if (tv2 != null) {
                    tv2.setText("（二）评估建议\n\n加载社交能力报告失败：" + e.getMessage());
                }
            }
            
            // 隐藏硬编码的需要重点关注的能力标题
            View weaknessTitle = findViewById(R.id.weakness_title);
            if (weaknessTitle != null) {
                weaknessTitle.setVisibility(View.GONE);
            }
            // 隐藏硬编码的不稳定的能力标题
            View inProgressTitle = findViewById(R.id.in_progress_title);
            if (inProgressTitle != null) {
                inProgressTitle.setVisibility(View.GONE);
            }
            
            // 计算总体评估结果
            double accuracy = (double) totalScore / (completedQuestions * 2);
            
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
                    detailedAssessment.append((i + 1)).append(". ").append(weaknesses.get(i)).append("\n");
                }
            } else {
                detailedAssessment.append("无\n");
            }
            detailedAssessment.append("\n");
            
            // 显示需要进一步发展的能力
            detailedAssessment.append("2.有些社交能力已经表现出来，但是不够经常，我们可以多去发展以下能力：\n");
            if (!inProgress.isEmpty()) {
                for (int i = 0; i < inProgress.size(); i++) {
                    detailedAssessment.append((i + 1)).append(". ").append(inProgress.get(i)).append("\n");
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
        adapter = new resultadapter(this, evaluations, this::recomputeArticulationStats);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setItemPrefetchEnabled(true);
        layoutManager.setInitialPrefetchItemCount(6);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 20);
        recyclerView.setAdapter(adapter);

        // 隐藏返回按钮上面的"（三）评估建议"标题
        findAndHideEvaluationSuggestionTitle(findViewById(android.R.id.content));

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
        AudioPlayer.getInstance().setPlayPos(-1);
        AudioPlayer.getInstance().stop();
        savePrelinguisticReport();
        saveArticulationReport();
        saveSocialReport();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back || v.getId() == R.id.btn_back_new){
            AudioPlayer.getInstance().setPlayPos(-1);
            AudioPlayer.getInstance().stop();
            savePrelinguisticReport();
            saveArticulationReport();
            saveSocialReport();
            finish();
        } else if (v.getId() == R.id.btn_generate_plan) {
            generateTreatmentPlan();
        } else if (v.getId() == R.id.btn_view_plan) {
            openTreatmentPlanActivity();
        } else if (v.getId() == R.id.pl_save_button) {
            savePrelinguisticReport();
            finish();
        }
    }

    private void generateTreatmentPlan() {
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到评估数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 解析当前模块
        String format = getIntent().getStringExtra("format");
        String moduleType = format;
        if (isPrelinguisticResult) {
            moduleType = "prelinguistic";
        } else if (isArticulationResult) {
            moduleType = "articulation";
        } else if (isSocialResult) {
            moduleType = "social";
        } else if ("E".equals(format) || "EV".equals(format)) {
            moduleType = "vocabulary";
        }

        JSONObject data;
        try {
            data = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            Toast.makeText(this, "读取评估数据失败", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "正在生成单模块干预报告...", Toast.LENGTH_SHORT).show();
        String finalModuleType = moduleType;
        new utils.ModuleInterventionService().generate(data, finalModuleType, new utils.ModuleInterventionService.Callback() {
            @Override
            public void onSuccess(JSONObject interventionGuide) {
                runOnUiThread(() -> {
                    try {
                        utils.ModuleReportHelper.saveModuleInterventionGuide(data, finalModuleType, interventionGuide);
                        dataManager.getInstance().saveChildJson(fName, data);
                        
                        if (viewPlanButton != null) {
                            viewPlanButton.setEnabled(true);
                            viewPlanButton.setAlpha(1f);
                        }
                        
                        Intent intent = new Intent(resultactivity.this, InterventionPlanActivity.class);
                        intent.putExtra("fName", fName);
                        intent.putExtra("moduleType", finalModuleType);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(resultactivity.this, "保存报告失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(resultactivity.this,
                        "生成失败: " + (errorMessage == null ? "" : errorMessage),
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private void openTreatmentPlanActivity() {
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到评估数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String format = getIntent().getStringExtra("format");
        String moduleType = format;
        if (isPrelinguisticResult) {
            moduleType = "prelinguistic";
        } else if (isArticulationResult) {
            moduleType = "articulation";
        } else if (isSocialResult) {
            moduleType = "social";
        } else if ("E".equals(format) || "EV".equals(format)) {
            moduleType = "vocabulary";
        }
        
        JSONObject data;
        try {
            data = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            Toast.makeText(this, "读取评估数据失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        JSONObject guide = utils.ModuleReportHelper.loadModuleInterventionGuide(data, moduleType);
        if (guide == null || guide.length() == 0) {
            Toast.makeText(this, "请先生成干预报告", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, InterventionPlanActivity.class);
        intent.putExtra("fName", fName);
        intent.putExtra("moduleType", moduleType);
        startActivity(intent);
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

            // 同步构音记录表：用当前可编辑结果覆盖持久化数组，避免旧数据未更新。
            JSONArray aArray = new JSONArray();
            evaluations.put("A", aArray);
            if (this.evaluations != null) {
                for (evaluation eval : this.evaluations) {
                    if (eval instanceof a) {
                        a item = (a) eval;
                        if (item.getNum() > 0) {
                            item.toJson(evaluations);
                        }
                    }
                }
            }

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
            evaluations.put("initial_accuracy", initialText);
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
            dataManager.getInstance().saveChildJson(fName, data);
            Toast.makeText(this, "已保存社交能力评估报告", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存社交能力评估报告失败", Toast.LENGTH_SHORT).show();
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

    private int vowelIndexOf(String vowel) {
        for (int i = 0; i < vowelLabels.length; i++) {
            if (vowelLabels[i].equalsIgnoreCase(vowel)) return i;
        }
        return -1;
    }

    private String normalizeVowel(String vowel) {
        String value = safeLower(vowel);
        if (value.isEmpty()) {
            return value;
        }
        return value.replace("v", "ü");
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
                        // 只隐藏标题，不隐藏后面的评估内容
                        textView.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            // 捕获任何异常，确保方法不会导致崩溃
            e.printStackTrace();
        }
    }

    
    // 填充RG题目详情表格
    private void fillRGQuestionDetailTable(Map<Integer, ArrayList<QuestionDetail>> groupQuestionDetails) {
        TableLayout tableLayout = findViewById(R.id.question_detail_table);
        if (tableLayout == null) {
            return;
        }
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1); // 保留表头
        
        // 修改表头
        TableRow headerRow = (TableRow) tableLayout.getChildAt(0);
        headerRow.removeAllViews();
        
        // 测试阶段
        TextView phaseText = new TextView(this);
        phaseText.setText("测试阶段");
        phaseText.setGravity(Gravity.CENTER);
        phaseText.setPadding(8, 8, 8, 8);
        phaseText.setTextSize(14);
        phaseText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(phaseText);
        
        // 题号
        TextView numText = new TextView(this);
        numText.setText("题  号");
        numText.setGravity(Gravity.CENTER);
        numText.setPadding(8, 8, 8, 8);
        numText.setTextSize(14);
        numText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(numText);
        
        // 例句
        TextView questionText = new TextView(this);
        questionText.setText("例  句");
        questionText.setGravity(Gravity.CENTER);
        questionText.setPadding(8, 8, 8, 8);
        questionText.setTextSize(14);
        questionText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(questionText);
        
        // 正确选项
        TextView correctOptionText = new TextView(this);
        correctOptionText.setText("正确选项");
        correctOptionText.setGravity(Gravity.CENTER);
        correctOptionText.setPadding(8, 8, 8, 8);
        correctOptionText.setTextSize(14);
        correctOptionText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(correctOptionText);
        
        // 被选选项
        TextView selectedOptionText = new TextView(this);
        selectedOptionText.setText("被选选项");
        selectedOptionText.setGravity(Gravity.CENTER);
        selectedOptionText.setPadding(8, 8, 8, 8);
        selectedOptionText.setTextSize(14);
        selectedOptionText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(selectedOptionText);
        
        // 测试结果
        TextView resultText = new TextView(this);
        resultText.setText("测试结果");
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(8, 8, 8, 8);
        resultText.setTextSize(14);
        resultText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(resultText);
        
        // 答题时间
        TextView timeText = new TextView(this);
        timeText.setText("答题时间");
        timeText.setGravity(Gravity.CENTER);
        timeText.setPadding(8, 8, 8, 8);
        timeText.setTextSize(14);
        timeText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(timeText);
        
        // 对应测试语法点
        TextView grammarPointText = new TextView(this);
        grammarPointText.setText("对应测试语法点");
        grammarPointText.setGravity(Gravity.CENTER);
        grammarPointText.setPadding(8, 8, 8, 8);
        grammarPointText.setTextSize(14);
        grammarPointText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(grammarPointText);

        if (groupQuestionDetails.isEmpty()) {
            // 添加空数据提示
            TableRow row = new TableRow(this);
            row.setBackgroundColor(getResources().getColor(android.R.color.white));

            TextView emptyText = new TextView(this);
            emptyText.setText("暂无题目数据");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(12, 12, 12, 12);
            emptyText.setTextSize(14);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            params.span = 8;
            emptyText.setLayoutParams(params);

            row.addView(emptyText);
            tableLayout.addView(row);
            return;
        }

        // 按组号从小到大显示，只显示有数据的组
        for (int group : groupQuestionDetails.keySet()) {
            ArrayList<QuestionDetail> groupDetails = groupQuestionDetails.get(group);
            if (groupDetails != null && !groupDetails.isEmpty()) {
                // 添加分组标题
                TableRow groupHeaderRow = new TableRow(this);
                groupHeaderRow.setBackgroundColor(0xFFE0E0E0);

                TextView groupHeaderText = new TextView(this);
                groupHeaderText.setText("第" + group + "组");
                groupHeaderText.setGravity(Gravity.CENTER);
                groupHeaderText.setPadding(12, 12, 12, 12);
                groupHeaderText.setTextSize(16);
                groupHeaderText.setTextColor(getResources().getColor(android.R.color.black));
                TableRow.LayoutParams headerParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                headerParams.span = 8;
                groupHeaderText.setLayoutParams(headerParams);

                groupHeaderRow.addView(groupHeaderText);
                tableLayout.addView(groupHeaderRow);

                // 添加题目数据行
                for (int i = 0; i < groupDetails.size(); i++) {
                    QuestionDetail detail = groupDetails.get(i);
                    TableRow row = new TableRow(this);
                    row.setBackgroundColor(i % 2 == 0 ? getResources().getColor(android.R.color.white) : 0xF9F9F9);

                    // 测试阶段
                    TextView phaseCell = new TextView(this);
                    phaseCell.setText(String.valueOf(detail.groupNumber));
                    phaseCell.setGravity(Gravity.CENTER);
                    phaseCell.setPadding(8, 8, 8, 8);
                    phaseCell.setTextSize(14);
                    phaseCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(phaseCell);

                    // 题号
                    TextView numCell = new TextView(this);
                    numCell.setText(detail.questionNumber);
                    numCell.setGravity(Gravity.CENTER);
                    numCell.setPadding(8, 8, 8, 8);
                    numCell.setTextSize(14);
                    numCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(numCell);

                    // 例句
                    TextView questionCell = new TextView(this);
                    questionCell.setText(detail.questionText);
                    questionCell.setGravity(Gravity.CENTER);
                    questionCell.setPadding(8, 8, 8, 8);
                    questionCell.setTextSize(14);
                    questionCell.setTextColor(getResources().getColor(android.R.color.black));
                    questionCell.setSingleLine(false);
                    questionCell.setMaxWidth(200);
                    questionCell.setMaxLines(3);
                    row.addView(questionCell);

                    // 正确选项
                    TextView correctOptionCell = new TextView(this);
                    correctOptionCell.setText(detail.correctOption);
                    correctOptionCell.setGravity(Gravity.CENTER);
                    correctOptionCell.setPadding(8, 8, 8, 8);
                    correctOptionCell.setTextSize(14);
                    correctOptionCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(correctOptionCell);

                    // 被选选项
                    TextView selectedOptionCell = new TextView(this);
                    selectedOptionCell.setText(detail.selectedOption);
                    selectedOptionCell.setGravity(Gravity.CENTER);
                    selectedOptionCell.setPadding(8, 8, 8, 8);
                    selectedOptionCell.setTextSize(14);
                    selectedOptionCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(selectedOptionCell);

                    // 测试结果
                    TextView resultCell = new TextView(this);
                    resultCell.setText(detail.result ? "正确" : "错误");
                    resultCell.setGravity(Gravity.CENTER);
                    resultCell.setPadding(8, 8, 8, 8);
                    resultCell.setTextSize(14);
                    resultCell.setTextColor(detail.result ? getResources().getColor(android.R.color.holo_green_dark) : getResources().getColor(android.R.color.holo_red_dark));
                    row.addView(resultCell);

                    // 答题时间
                    TextView timeCell = new TextView(this);
                    timeCell.setText(detail.time);
                    timeCell.setGravity(Gravity.CENTER);
                    timeCell.setPadding(8, 8, 8, 8);
                    timeCell.setTextSize(14);
                    timeCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(timeCell);

                    // 对应测试语法点
                    TextView grammarPointCell = new TextView(this);
                    grammarPointCell.setText(detail.grammarPoint);
                    grammarPointCell.setGravity(Gravity.CENTER);
                    grammarPointCell.setPadding(8, 8, 8, 8);
                    grammarPointCell.setTextSize(14);
                    grammarPointCell.setTextColor(getResources().getColor(android.R.color.black));
                    grammarPointCell.setSingleLine(false);
                    grammarPointCell.setMaxWidth(150);
                    grammarPointCell.setMaxLines(2);
                    row.addView(grammarPointCell);

                    tableLayout.addView(row);
                }
                
                // 处理第四组的篇章理解能力
                if (group == 4) {
                    // 添加篇章理解能力的题目
                    TableRow passageRow = new TableRow(this);
                    passageRow.setBackgroundColor(0xF9F9F9);
                    
                    TextView emptyCell = new TextView(this);
                    emptyCell.setPadding(8, 8, 8, 8);
                    passageRow.addView(emptyCell);
                    
                    TextView emptyCell2 = new TextView(this);
                    emptyCell2.setPadding(8, 8, 8, 8);
                    passageRow.addView(emptyCell2);
                    
                    TextView passageText = new TextView(this);
                    passageText.setText("「朵朵的生日是在什么时候过的呀？」（今天晚上）");
                    passageText.setGravity(Gravity.CENTER);
                    passageText.setPadding(8, 8, 8, 8);
                    passageText.setTextSize(14);
                    passageText.setTextColor(getResources().getColor(android.R.color.black));
                    passageText.setSingleLine(false);
                    passageText.setMaxWidth(200);
                    passageText.setMaxLines(3);
                    passageRow.addView(passageText);
                    
                    for (int i = 0; i < 5; i++) {
                        TextView empty = new TextView(this);
                        empty.setPadding(8, 8, 8, 8);
                        passageRow.addView(empty);
                    }
                    
                    tableLayout.addView(passageRow);
                    
                    // 添加其他篇章理解问题
                    String[] passageQuestions = {
                        "「是在什么地方过生日的呀？」（朵朵的家里）",
                        "「谁给朵朵过生日呀？」（全家人）",
                        "「一开始发生了什么让朵朵不开心的事呀？」（妈妈发现忘买生日蛋糕上的蜡烛了）",
                        "「爸爸想了什么办法呀？」（找来彩色小蜡笔，插在蛋糕上当蜡烛）",
                        "「最后朵朵的生日过得怎么样呀？」（朵朵笑了，全家人唱生日歌，她过得特别开心）"
                    };
                    
                    for (String question : passageQuestions) {
                        TableRow row = new TableRow(this);
                        row.setBackgroundColor(getResources().getColor(android.R.color.white));
                        
                        for (int i = 0; i < 8; i++) {
                            TextView cell = new TextView(this);
                            if (i == 2) {
                                cell.setText(question);
                                cell.setGravity(Gravity.CENTER);
                                cell.setSingleLine(false);
                                cell.setMaxWidth(200);
                                cell.setMaxLines(3);
                            }
                            cell.setPadding(8, 8, 8, 8);
                            cell.setTextSize(14);
                            cell.setTextColor(getResources().getColor(android.R.color.black));
                            row.addView(cell);
                        }
                        
                        tableLayout.addView(row);
                    }
                }
            }
        }
    }
    
    // 填充SE题目详情表格
    private void fillSEQuestionDetailTable(Map<Integer, ArrayList<QuestionDetail>> groupQuestionDetails) {
        TableLayout tableLayout = findViewById(R.id.question_detail_table);
        if (tableLayout == null) {
            return;
        }
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1); // 保留表头
        
        // 修改表头
        TableRow headerRow = (TableRow) tableLayout.getChildAt(0);
        headerRow.removeAllViews();
        
        // 测试阶段
        TextView phaseText = new TextView(this);
        phaseText.setText("测试阶段");
        phaseText.setGravity(Gravity.CENTER);
        phaseText.setPadding(8, 8, 8, 8);
        phaseText.setTextSize(14);
        phaseText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(phaseText);
        
        // 题号
        TextView numText = new TextView(this);
        numText.setText("题  号");
        numText.setGravity(Gravity.CENTER);
        numText.setPadding(8, 8, 8, 8);
        numText.setTextSize(14);
        numText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(numText);
        
        // 例句
        TextView questionText = new TextView(this);
        questionText.setText("例  句");
        questionText.setGravity(Gravity.CENTER);
        questionText.setPadding(8, 8, 8, 8);
        questionText.setTextSize(14);
        questionText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(questionText);
        
        // 正确选项
        TextView correctOptionText = new TextView(this);
        correctOptionText.setText("正确选项");
        correctOptionText.setGravity(Gravity.CENTER);
        correctOptionText.setPadding(8, 8, 8, 8);
        correctOptionText.setTextSize(14);
        correctOptionText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(correctOptionText);
        
        // 被选选项
        TextView selectedOptionText = new TextView(this);
        selectedOptionText.setText("被选选项");
        selectedOptionText.setGravity(Gravity.CENTER);
        selectedOptionText.setPadding(8, 8, 8, 8);
        selectedOptionText.setTextSize(14);
        selectedOptionText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(selectedOptionText);
        
        // 测试结果
        TextView resultText = new TextView(this);
        resultText.setText("测试结果");
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(8, 8, 8, 8);
        resultText.setTextSize(14);
        resultText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(resultText);
        
        // 答题时间
        TextView timeText = new TextView(this);
        timeText.setText("答题时间");
        timeText.setGravity(Gravity.CENTER);
        timeText.setPadding(8, 8, 8, 8);
        timeText.setTextSize(14);
        timeText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(timeText);
        
        // 对应测试语法点
        TextView grammarPointText = new TextView(this);
        grammarPointText.setText("对应测试语法点");
        grammarPointText.setGravity(Gravity.CENTER);
        grammarPointText.setPadding(8, 8, 8, 8);
        grammarPointText.setTextSize(14);
        grammarPointText.setTextColor(getResources().getColor(android.R.color.white));
        headerRow.addView(grammarPointText);

        if (groupQuestionDetails.isEmpty()) {
            // 添加空数据提示
            TableRow row = new TableRow(this);
            row.setBackgroundColor(getResources().getColor(android.R.color.white));

            TextView emptyText = new TextView(this);
            emptyText.setText("暂无题目数据");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(12, 12, 12, 12);
            emptyText.setTextSize(14);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            params.span = 8;
            emptyText.setLayoutParams(params);

            row.addView(emptyText);
            tableLayout.addView(row);
            return;
        }

        // 按组号从小到大显示，只显示有数据的组
        for (int group : groupQuestionDetails.keySet()) {
            ArrayList<QuestionDetail> groupDetails = groupQuestionDetails.get(group);
            if (groupDetails != null && !groupDetails.isEmpty()) {
                // 添加分组标题
                TableRow groupHeaderRow = new TableRow(this);
                groupHeaderRow.setBackgroundColor(0xFFE0E0E0);

                TextView groupHeaderText = new TextView(this);
                groupHeaderText.setText("第" + group + "组");
                groupHeaderText.setGravity(Gravity.CENTER);
                groupHeaderText.setPadding(12, 12, 12, 12);
                groupHeaderText.setTextSize(16);
                groupHeaderText.setTextColor(getResources().getColor(android.R.color.black));
                TableRow.LayoutParams headerParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                headerParams.span = 8;
                groupHeaderText.setLayoutParams(headerParams);

                groupHeaderRow.addView(groupHeaderText);
                tableLayout.addView(groupHeaderRow);

                // 添加题目数据行
                for (int i = 0; i < groupDetails.size(); i++) {
                    QuestionDetail detail = groupDetails.get(i);
                    TableRow row = new TableRow(this);
                    row.setBackgroundColor(i % 2 == 0 ? getResources().getColor(android.R.color.white) : 0xF9F9F9);

                    // 测试阶段
                    TextView phaseCell = new TextView(this);
                    phaseCell.setText(String.valueOf(detail.groupNumber));
                    phaseCell.setGravity(Gravity.CENTER);
                    phaseCell.setPadding(8, 8, 8, 8);
                    phaseCell.setTextSize(14);
                    phaseCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(phaseCell);

                    // 题号
                    TextView numCell = new TextView(this);
                    numCell.setText(detail.questionNumber);
                    numCell.setGravity(Gravity.CENTER);
                    numCell.setPadding(8, 8, 8, 8);
                    numCell.setTextSize(14);
                    numCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(numCell);

                    // 例句
                    TextView questionCell = new TextView(this);
                    questionCell.setText(detail.questionText);
                    questionCell.setGravity(Gravity.CENTER);
                    questionCell.setPadding(8, 8, 8, 8);
                    questionCell.setTextSize(14);
                    questionCell.setTextColor(getResources().getColor(android.R.color.black));
                    questionCell.setSingleLine(false);
                    questionCell.setMaxWidth(200);
                    questionCell.setMaxLines(3);
                    row.addView(questionCell);

                    // 正确选项
                    TextView correctOptionCell = new TextView(this);
                    correctOptionCell.setText(detail.correctOption);
                    correctOptionCell.setGravity(Gravity.CENTER);
                    correctOptionCell.setPadding(8, 8, 8, 8);
                    correctOptionCell.setTextSize(14);
                    correctOptionCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(correctOptionCell);

                    // 被选选项
                    TextView selectedOptionCell = new TextView(this);
                    selectedOptionCell.setText(detail.selectedOption);
                    selectedOptionCell.setGravity(Gravity.CENTER);
                    selectedOptionCell.setPadding(8, 8, 8, 8);
                    selectedOptionCell.setTextSize(14);
                    selectedOptionCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(selectedOptionCell);

                    // 测试结果
                    TextView resultCell = new TextView(this);
                    resultCell.setText(detail.result ? "正确" : "错误");
                    resultCell.setGravity(Gravity.CENTER);
                    resultCell.setPadding(8, 8, 8, 8);
                    resultCell.setTextSize(14);
                    resultCell.setTextColor(detail.result ? getResources().getColor(android.R.color.holo_green_dark) : getResources().getColor(android.R.color.holo_red_dark));
                    row.addView(resultCell);

                    // 答题时间
                    TextView timeCell = new TextView(this);
                    timeCell.setText(detail.time);
                    timeCell.setGravity(Gravity.CENTER);
                    timeCell.setPadding(8, 8, 8, 8);
                    timeCell.setTextSize(14);
                    timeCell.setTextColor(getResources().getColor(android.R.color.black));
                    row.addView(timeCell);

                    // 对应测试语法点
                    TextView grammarPointCell = new TextView(this);
                    grammarPointCell.setText(detail.grammarPoint);
                    grammarPointCell.setGravity(Gravity.CENTER);
                    grammarPointCell.setPadding(8, 8, 8, 8);
                    grammarPointCell.setTextSize(14);
                    grammarPointCell.setTextColor(getResources().getColor(android.R.color.black));
                    grammarPointCell.setSingleLine(false);
                    grammarPointCell.setMaxWidth(150);
                    grammarPointCell.setMaxLines(2);
                    row.addView(grammarPointCell);

                    tableLayout.addView(row);
                }
            }
        }
    }
    
    // 生成RG评估建议
    private void generateRGEvaluationSuggestion(double correctCount, int totalCount, 
                                              HashMap<String, Integer> grammarPointScores, 
                                              HashMap<String, Integer> grammarPointTotal) {
        // 计算总体正确率
        double accuracy = 0;
        if (totalCount > 0) {
            accuracy = correctCount / totalCount;
        }

        // 生成总体评估
        String overallEvaluation;
        if (correctCount >= 10 && totalCount >= 15) {
            overallEvaluation = "1. 从整体上来说，孩子的句法理解能力较好，基本达标，符合该年龄段孩子语言发育水平。";
        } else {
            overallEvaluation = "2. 从整体上来说，孩子的句法理解能力还有待进一步发展，尚未达标。";
        }

        // 构建评估建议文本
        StringBuilder suggestionBuilder = new StringBuilder();
        suggestionBuilder.append("评估建议\n\n");
        suggestionBuilder.append("通过儿童句法理解能力的评估，本次评估结果如下：\n\n");
        suggestionBuilder.append(overallEvaluation + "\n\n");
        suggestionBuilder.append("1.【需要重点关注的能力】\n");
        suggestionBuilder.append("这些语言能力还没有发展出来，需要重点关注。\n\n");
        
        // 收集需要重点关注的能力
        ArrayList<String> weaknessList = new ArrayList<>();
        for (String point : grammarPointScores.keySet()) {
            int score = grammarPointScores.get(point);
            int total = grammarPointTotal.get(point);
            if (total > 0 && score == 0) {
                weaknessList.add(point);
            }
        }

        // 收集不稳定的能力
        ArrayList<String> inProgressList = new ArrayList<>();
        for (String point : grammarPointScores.keySet()) {
            int score = grammarPointScores.get(point);
            int total = grammarPointTotal.get(point);
            if (total > 0 && score > 0 && score < total) {
                if (total == 3 && score == 1) {
                    inProgressList.add(point);
                } else if (total == 6 && (score == 1 || score == 2)) {
                    inProgressList.add(point);
                }
            }
        }

        // 显示需要重点关注的能力
        if (!weaknessList.isEmpty()) {
            for (int i = 0; i < weaknessList.size(); i++) {
                suggestionBuilder.append((i + 1) + ". " + weaknessList.get(i) + "\n");
            }
        } else {
            suggestionBuilder.append("暂无需要重点关注的能力\n");
        }
        suggestionBuilder.append("\n\n\n\n");
        
        suggestionBuilder.append("2.【不稳定的能力】\n");
        suggestionBuilder.append("有些语言能力已经发展出来，但是不够稳定，我们可以多去引导，在家多做语言示范。\n\n\n");
        
        // 显示不稳定的能力
        if (!inProgressList.isEmpty()) {
            for (int i = 0; i < inProgressList.size(); i++) {
                suggestionBuilder.append((i + 1) + ". " + inProgressList.get(i) + "\n");
            }
        } else {
            suggestionBuilder.append("暂无不稳定的能力\n");
        }
        suggestionBuilder.append("\n");

        // 显示评估建议总体部分
        TextView evaluationText = findViewById(R.id.tv_3);
        if (evaluationText != null) {
            evaluationText.setVisibility(View.VISIBLE);
            evaluationText.setText("通过儿童句法理解能力的评估，本次评估结果如下：\n\n" + overallEvaluation);
        }

        // 显示需要重点关注的能力
        LinearLayout weaknessLayout = findViewById(R.id.weakness_list);
        if (weaknessLayout != null) {
            weaknessLayout.removeAllViews();
            if (!weaknessList.isEmpty()) {
                for (int i = 0; i < weaknessList.size(); i++) {
                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ". " + weaknessList.get(i));
                    textView.setTextSize(16);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setSingleLine(false);
                    weaknessLayout.addView(textView);
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("暂无需要重点关注的能力");
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                weaknessLayout.addView(textView);
            }
        }

        // 显示不稳定的能力
        LinearLayout inProgressLayout = findViewById(R.id.in_progress_list);
        if (inProgressLayout != null) {
            inProgressLayout.removeAllViews();
            if (!inProgressList.isEmpty()) {
                for (int i = 0; i < inProgressList.size(); i++) {
                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ". " + inProgressList.get(i));
                    textView.setTextSize(16);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setSingleLine(false);
                    inProgressLayout.addView(textView);
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("暂无不稳定的能力");
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                inProgressLayout.addView(textView);
            }
        }
    }
    
    // 生成SE评估建议
    private void generateSEEvaluationSuggestion(double correctCount, int totalCount, 
                                              HashMap<String, Integer> grammarPointScores, 
                                              HashMap<String, Integer> grammarPointTotal) {
        // 计算总体正确率
        double accuracy = 0;
        if (totalCount > 0) {
            accuracy = correctCount / totalCount;
        }

        // 生成总体评估
        String overallEvaluation;
        if (correctCount >= 10 && totalCount >= 15) {
            overallEvaluation = "1. 从整体上来说，孩子的句法表达能力较好，基本达标，符合该年龄段孩子语言发育水平。";
        } else {
            overallEvaluation = "2. 从整体上来说，孩子的句法表达能力还有待进一步发展，尚未达标。";
        }

        // 收集需要重点关注的能力
        ArrayList<String> weaknessList = new ArrayList<>();
        for (String point : grammarPointScores.keySet()) {
            int score = grammarPointScores.get(point);
            int total = grammarPointTotal.get(point);
            if (total > 0 && score == 0) {
                weaknessList.add(point);
            }
        }

        // 收集不稳定的能力
        ArrayList<String> inProgressList = new ArrayList<>();
        for (String point : grammarPointScores.keySet()) {
            int score = grammarPointScores.get(point);
            int total = grammarPointTotal.get(point);
            if (total > 0 && score > 0 && score < total) {
                if (total == 3 && score == 1) {
                    inProgressList.add(point);
                }
            }
        }

        // 构建评估建议文本
        StringBuilder suggestionBuilder = new StringBuilder();
        suggestionBuilder.append("评估建议\n\n");
        suggestionBuilder.append("通过儿童句法表达能力的评估，本次评估结果如下：\n\n");
        suggestionBuilder.append(overallEvaluation + "\n\n");
        suggestionBuilder.append("1.【需要重点关注的能力】\n");
        suggestionBuilder.append("这些语言能力还没有发展出来，需要重点关注。\n\n");
        
        // 显示需要重点关注的能力
        if (!weaknessList.isEmpty()) {
            for (int i = 0; i < weaknessList.size(); i++) {
                suggestionBuilder.append((i + 1) + ". " + weaknessList.get(i) + "\n");
            }
        } else {
            suggestionBuilder.append("暂无需要重点关注的能力\n");
        }
        suggestionBuilder.append("\n\n\n\n");
        
        suggestionBuilder.append("2.【不稳定的能力】\n");
        suggestionBuilder.append("有些语言能力已经发展出来，但是不够稳定，我们可以多去引导，在家多做语言示范。\n\n\n");
        
        // 显示不稳定的能力
        if (!inProgressList.isEmpty()) {
            for (int i = 0; i < inProgressList.size(); i++) {
                suggestionBuilder.append((i + 1) + ". " + inProgressList.get(i) + "\n");
            }
        } else {
            suggestionBuilder.append("暂无不稳定的能力\n");
        }
        suggestionBuilder.append("\n");

        // 显示评估建议总体部分
        TextView evaluationText = findViewById(R.id.tv_3);
        if (evaluationText != null) {
            evaluationText.setVisibility(View.VISIBLE);
            evaluationText.setText("通过儿童句法表达能力的评估，本次评估结果如下：\n\n" + overallEvaluation);
        }

        // 显示需要重点关注的能力
        LinearLayout weaknessLayout = findViewById(R.id.weakness_list);
        if (weaknessLayout != null) {
            weaknessLayout.removeAllViews();
            if (!weaknessList.isEmpty()) {
                for (int i = 0; i < weaknessList.size(); i++) {
                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ". " + weaknessList.get(i));
                    textView.setTextSize(16);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setSingleLine(false);
                    weaknessLayout.addView(textView);
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("暂无需要重点关注的能力");
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                weaknessLayout.addView(textView);
            }
        }

        // 显示不稳定的能力
        LinearLayout inProgressLayout = findViewById(R.id.in_progress_list);
        if (inProgressLayout != null) {
            inProgressLayout.removeAllViews();
            if (!inProgressList.isEmpty()) {
                for (int i = 0; i < inProgressList.size(); i++) {
                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ". " + inProgressList.get(i));
                    textView.setTextSize(16);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setSingleLine(false);
                    inProgressLayout.addView(textView);
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("暂无不稳定的能力");
                textView.setTextSize(16);
                textView.setPadding(5, 5, 5, 5);
                inProgressLayout.addView(textView);
            }
        }
    }
    
    // 获取题目对应的正确选项
    private String getCorrectOptionForQuestion(int group, int questionNumber) {
        // 根据组别和题目编号返回正确选项
        if (group == 1) {
            // 第一组正确选项
            switch (questionNumber) {
                case 1: return "B"; // 小狗跑
                case 2: return "C"; // 爷爷坐
                case 3: return "A"; // 小鸟飞
                case 4: return "C"; // 切香蕉
                case 5: return "A"; // 吃苹果
                case 6: return "B"; // 擦桌子
                case 7: return "A"; // 哥哥搭积木
                case 8: return "C"; // 爷爷看报纸
                case 9: return "A"; // 兔子拔萝卜
                case 10: return "B"; // 不能吃
                case 11: return "B"; // 没有跑
                case 12: return "A"; // 不会飞
                case 13: return "RIGHT"; // 这是苹果么？
                case 14: return "RIGHT"; // 妹妹睡觉了么？
                case 15: return "RIGHT"; // 天黑了吗？
                case 16: return "B"; // 谁在画兔子？
                case 17: return "C"; // 爸爸在干什么？
                case 18: return "C"; // 去哪里看医生？
                case 19: return "A"; // 大苹果
                case 20: return "B"; // 黄香蕉
                case 21: return "A"; // 圆圆的饼干
                default: return "";
            }
        } else if (group == 2) {
            // 第二组正确选项
            switch (questionNumber) {
                case 1: return "C"; // 大红苹果
                case 2: return "B"; // 白色斑点狗
                case 3: return "D"; // 小的圆形饼干
                case 4: return "A"; // 给妈妈包包
                case 5: return "B"; // 喂小猫牛奶
                case 6: return "A"; // 送姐姐花
                case 7: return "RIGHT"; // 苹果是不是红的？
                case 8: return "RIGHT"; // 姐姐戴没戴帽子？
                case 9: return "RIGHT"; // 飞机是不是交通工具？
                case 10: return "B"; // 去哪里看大象？
                case 11: return "D"; // 在哪里挖沙子？
                case 12: return "E"; // 小猫在哪里？
                case 13: return "A"; // 他们都戴帽子
                case 14: return "C"; // 小朋友都有苹果
                case 15: return "B"; // 气球都飞走了
                case 16: return "A"; // 哥哥推姐姐
                case 17: return "B"; // 爷爷送小朋友礼物
                case 18: return "D"; // 狗追猫
                default: return "";
            }
        } else if (group == 3) {
            // 第三组正确选项
            switch (questionNumber) {
                case 1: return "D"; // 弟弟被妈妈抱
                case 2: return "C"; // 爸爸被小狗追
                case 3: return "B"; // 猫咪被大象推
                case 4: return "C"; // 哥哥比妹妹跑得快
                case 5: return "A"; // 叔叔比阿姨胖
                case 6: return "B"; // 红球比绿球大
                case 7: return "D"; // 哪个是妹妹吃完了？
                case 8: return "B"; // 花开了
                case 9: return "A"; // 门关了
                case 10: return "B"; // 因为下雨，所以打伞
                case 11: return "B"; // 因为小明生病了，所以没上学
                case 12: return "A"; // 因为天冷了，所以穿棉袄
                case 13: return "A"; // 哥哥想去玩，但是下雨了
                case 14: return "A"; // 小朋友想吃蛋糕，但是没有了
                case 15: return "A"; // 他想踢足球，但是他腿受伤了
                case 16: return "61,62"; // 先揉揉肚子，再挥挥手
                case 17: return "65,67"; // 先洗手，再吃饭
                case 18: return "71,69"; // 先刷牙，再睡觉
                default: return "";
            }
        } else if (group == 4) {
            // 第四组正确选项
            switch (questionNumber) {
                case 1: return "A"; // 电视开着
                case 2: return "B"; // 姐姐跳着绳
                case 3: return "A"; // 叔叔唱着歌
                case 4: return "A"; // 宝宝没有不睡觉
                case 5: return "A"; // 小明没有不喜欢吃苹果
                case 6: return "A"; // 小明不是不想去学校
                case 7: return "RIGHT"; // 除非洗手，否则不能吃饭
                case 8: return "RIGHT"; // 除非天气很冷，否则小明都去游泳
                case 9: return "RIGHT"; // 除非收拾好玩具，否则不能看电视
                case 10: return "RIGHT"; // 篇章理解能力
                default: return "";
            }
        }
        return "";
    }



}
