package com.example.CCLEvaluation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bean.evaluation;
import utils.AudioRecorder;
import utils.dataManager;
import utils.dialogUtils;
import utils.dirpath;
import utils.ImageUrls;
import utils.LlmPlanService;
import utils.NetInteractUtils;
import utils.TreatmentPromptBuilder;

public class evmenuactivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private ConstraintLayout cl;
    private TextView pl;
    private Button Word1, Word2;
    private Button WordResult1, WordResult2;
    private Button Pronunciation;
    private Button PronunciationResult;
    private Button Grammar;
    private Button GrammarResult;
    private Button PrelinguisticTest, PrelinguisticResult;
    private Button SocialTest, SocialResult;
    private Button Upload, Download, Pdf, Plan, PlanView;
    private JSONObject data;
    private String fName;
    private String childUser;
    private String uid;
    private LinearLayout wd;
    private LinearLayout plModule;
    private LinearLayout pn;
    private LinearLayout gm;
    private static final int MY_PERMISSIONS_REQUEST_READ_OR_WRITE_EXTERNAL_STORAGE = 1;
    private static final long PLAN_HINT_DELAY_MS = 45000L;

    private Map<String, Map<String, String>> audioPaths;
    private final Handler planHandler = new Handler(Looper.getMainLooper());
    private Runnable planHintRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ev_menu);
        cl = findViewById(R.id.cl2);
        pl = findViewById(R.id.pleasewait);
        Word1 = findViewById(R.id.btn_wordTest1);
        WordResult1 = findViewById(R.id.btn_wordResult1);
        Word2 = findViewById(R.id.btn_wordTest2);
        WordResult2 = findViewById(R.id.btn_wordResult2);

        Pronunciation = findViewById(R.id.btn_pronunciationTest);
        PronunciationResult = findViewById(R.id.btn_pronunciationResult);
        Grammar = findViewById(R.id.btn_grammarTest);
        GrammarResult = findViewById(R.id.btn_grammarResult);
        PrelinguisticTest = findViewById(R.id.btn_prelinguisticTest);
        PrelinguisticResult = findViewById(R.id.btn_prelinguisticResult);
        SocialTest = findViewById(R.id.btn_socialTest);
        SocialResult = findViewById(R.id.btn_socialResult);
        Upload = findViewById(R.id.btn_upload);
        Download = findViewById(R.id.btn_download);
        Pdf = findViewById(R.id.btn_pdf);
        Plan = findViewById(R.id.btn_plan);
        PlanView = findViewById(R.id.btn_plan_view);
        wd = findViewById(R.id.wd);
        plModule = findViewById(R.id.pl_module);
        pn = findViewById(R.id.pn);
        gm = findViewById(R.id.gm);
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
//        if(childUser != null){
//            Download.setVisibility(View.VISIBLE);
//        }
        NetInteractUtils.getInstance(evmenuactivity.this).setModuleCallback(new NetInteractUtils.ModuleCallback() {
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
                String PL = jsonObject.optString("PL", "0");

                if(E.equals("1") && RE.equals("1") && S.equals("1") && NWR.equals("1")){
                    wd.setVisibility(View.VISIBLE);
                }else{
                    wd.setVisibility(View.GONE);
                }
                if(A.equals("1")){
                    pn.setVisibility(View.VISIBLE);
                }else{
                    pn.setVisibility(View.GONE);
                }
                if(RG.equals("1")){
                    gm.setVisibility(View.VISIBLE);
                }else{
                    gm.setVisibility(View.GONE);
                }

                if(PL.equals("1")){
                    plModule.setVisibility(View.VISIBLE);
                }else{
                    plModule.setVisibility(View.GONE);
                }


            }
        });

        NetInteractUtils.getInstance(evmenuactivity.this).getModule(uid);


        try {
            initData();
        } catch (Exception e) {
            Toast.makeText(this, "数据加载失败！", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
        Word1.setOnClickListener(this);
        Word2.setOnClickListener(this);
        Pronunciation.setOnClickListener(this);
        Grammar.setOnClickListener(this);
        WordResult1.setOnClickListener(this);
        WordResult2.setOnClickListener(this);
        PronunciationResult.setOnClickListener(this);
        GrammarResult.setOnClickListener(this);
        PrelinguisticTest.setOnClickListener(this);
        PrelinguisticResult.setOnClickListener(this);
        SocialTest.setOnClickListener(this);
        SocialResult.setOnClickListener(this);
        Upload.setOnClickListener(this);
        Download.setOnClickListener(this);
        Pdf.setOnClickListener(this);
        Plan.setOnClickListener(this);
        PlanView.setOnClickListener(this);







//        Typeface typeface = Typeface.createFromAsset(getAssets(),"font/iconfont.ttf");
//        pl.setTypeface(typeface);
//
//        Netinteractutils.getInstance(this).setListener(new Netinteractutils.UiRefreshListener() {
//            /**
//             * @param isPlaying 是否需要打开等待动画中
//             */
//            @Override
//            public void refreshUI(Boolean isPlaying) {
//
//                if (isPlaying == true){//加载动画
//                    cl.setVisibility(View.VISIBLE);
//                }
//                else {//关闭动画
//                    cl.setVisibility(View.GONE);
//                }
//
//            }
//        });


        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writePDF(uri,fName);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            data = dataManager.getInstance().loadData(fName);
            completeDetails(data.getJSONObject("evaluations"));
        } catch (Exception e) {
            Toast.makeText(this, "数据加载失败！", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }
    @Override
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

    NetInteractUtils.UploadEvaluationCallback uploadEvaluationCallback = new NetInteractUtils.UploadEvaluationCallback() {
        /**
         * 如果上传成功，则会执行该回调，否则会在前后文this中弹出Toast提示错误
         * @param childUserID 后端返回的本次上传的编号，或者说这个儿童用户的测评id
         */
        @Override
        public void onUploadEvaluationResult(String childUserID) throws Exception {
            childUser = childUserID;
            Toast.makeText(evmenuactivity.this, "上传成功！childUserID:" + childUserID, Toast.LENGTH_SHORT).show();
            uploadAudioInParallel(uid, childUser, data.getJSONObject("evaluations"));

        }
    };
    NetInteractUtils.AudioCallback audioCallback = new NetInteractUtils.AudioCallback() {

        @Override
        public void onAudioResult(String audio) {
            try {
                //Toast.makeText(EvMenuActivity.this,audio,Toast.LENGTH_LONG).show();
                Log.d("fffff",audio);
                JSONObject audioData = new JSONObject(audio);
                String title = audioData.getString("Title");
                String num = audioData.getString("Num");
                Log.d("7654321",title+"__"+num);
                String audioByte = audioData.getString("Audio");
                byte[] decodeAudio;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    decodeAudio = Base64.getDecoder().decode(audioByte);
                } else {
                    decodeAudio = android.util.Base64.decode(audioByte, android.util.Base64.DEFAULT);
                }
                File file = new File(audioPaths.get(title).get(num));
                if (!file.getParentFile().exists()) {//父目录文件夹不存在
                    File dir = new File(dirpath.PATH_FETCH_DIR_AUDIO);
                    dir.mkdirs();
                }
                if (!file.exists())
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decodeAudio);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_wordTest1) {
            // 词汇能力评估（包含词汇表达和词汇理解）
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "E");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordTest2) {
            // 词汇理解测试
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "EV");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_pronunciationTest) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "A");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_grammarTest) {
            Intent intent = new Intent(this, SyntaxAbilityEvaluationActivity.class);
            intent.putExtra("fName", fName);
            startActivity(intent);

        } else if (v.getId() == R.id.btn_wordResult1) {
            // 词汇能力评估结果（包含词汇表达和词汇理解结果）
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "E");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordResult2) {
            // 词汇理解测试结果
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "EV");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_pronunciationResult) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "A");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_grammarResult) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "RG");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_prelinguisticTest) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "PL");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_prelinguisticResult) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "PL");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_socialTest) {
            Intent intent = new Intent(this, SocialGroupSelectActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_socialResult) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "SOCIAL");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_upload) {
            dialogUtils.showDialog(this, "提示信息", "您确定要上传该儿童的测评信息吗？",
                    "确认", () -> {
                        if (childUser != null) {
                            NetInteractUtils.getInstance(this).deleteEvaluation(uid, childUser);
                            childUser = null;//防止用户连点，例如两次删除，后面一次会有已删除的提示，而这是上传，不应该提示
                        }
                        data = dataManager.getInstance().loadData(fName);//必须重新加载，可能会更新
                        NetInteractUtils.getInstance(this).uploadEvaluation(uid, data.toString());

                        Intent intent = new Intent(evmenuactivity.this, MainActivity.class);
                        intent.putExtra("Uid",uid);
                        startActivity(intent);
                        finish();


                    }, "取消", null);
        } else if (v.getId() == R.id.btn_plan) {
            generateTreatmentPlan();
        } else if (v.getId() == R.id.btn_plan_view) {
            openLocalTreatmentPlan();
        } else if (v.getId() == R.id.btn_pdf) {
            if (ContextCompat.checkSelfPermission(evmenuactivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // 请求权限
                ActivityCompat.requestPermissions(evmenuactivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_OR_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(evmenuactivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // 请求权限
                ActivityCompat.requestPermissions(evmenuactivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_OR_WRITE_EXTERNAL_STORAGE);
            }

            try {
                JSONObject childData = dataManager.getInstance().loadData(fName);
                JSONObject info = childData.optJSONObject("info");
                String patientName = info != null ? info.optString("name", "") : "";
                String safeFileName = getSafeFileName(patientName, "未命名") + "_测评报告.pdf";
                createPDFWithSAF(safeFileName);
            } catch (Exception e) {
                createPDFWithSAF("测评报告.pdf");
            }

        }else if(v.getId() == R.id.btn_download){
            try {
                data = dataManager.getInstance().loadData(fName);
                JSONObject evaluations = data.getJSONObject("evaluations");
                Iterator<String> it = evaluations.keys();
                while (it.hasNext()) {
                    String title = it.next();
                    JSONArray currentArray = evaluations.getJSONArray(title);
                    for (int i = 0; i < currentArray.length(); i++) {
                        JSONObject element = currentArray.getJSONObject(i);
                        int num = element.getInt("num");
                        String audioPath = element.getString("audioPath");
                        if (audioPath != null && !audioPath.equals("null")) {
                                String destpath = dirpath.PATH_FETCH_DIR_AUDIO+"/"+ System.currentTimeMillis()+".amr";
                                copyFile(audioPath, destpath);
                                element.put("audioPath",destpath);
                        }
                    }
                }
                String fName = dataManager.getInstance().saveData(System.currentTimeMillis() + ".json", data);
                dataManager.getInstance().createIndex(fName,"Index");
                Intent intent = new Intent(evmenuactivity.this, MainActivity.class);
                intent.putExtra("Uid",uid);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getSafeFileName(String name, String defaultName) {
        if (name == null || name.trim().isEmpty()) {
            name = defaultName;
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private void generateTreatmentPlan() {
        JSONObject latestData;
        try {
            latestData = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            setLoading(false, null);
            Toast.makeText(this, "读取测评数据失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // concurrent plan generation
        setLoading(true, "正在并发生成干预方案(速度提升300%)...");
        schedulePlanHint();

        // build 6 parallel prompts
        Map<String, String> prompts;
        try {
            prompts = TreatmentPromptBuilder.buildConcurrentPrompts(latestData);
        } catch (JSONException e) {
            setLoading(false, null);
            Toast.makeText(this, "构建任务失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // call concurrent service
        LlmPlanService service = new LlmPlanService();
        service.generateTreatmentPlanConcurrent(prompts, new LlmPlanService.PlanCallback() {
            @Override
            public void onSuccess(JSONObject plan) {
                // merged full JSON already
                runOnUiThread(() -> {
                    clearPlanHint();
                    setLoading(false, null);
                    openTreatmentPlanActivity(plan.toString());
                });
            }

            @Override
            public void onError(String errorMessage) {
                // only invoked if tasks never started
                runOnUiThread(() -> {
                    clearPlanHint();
                    setLoading(false, null);
                    Toast.makeText(evmenuactivity.this, "生成失败: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean isLoading, String message) {
        if (isLoading) {
            if (message != null) {
                pl.setText(message);
            }
            cl.setVisibility(View.VISIBLE);
        } else {
            cl.setVisibility(View.GONE);
        }
    }

    private void schedulePlanHint() {
        clearPlanHint();
        planHintRunnable = () -> Toast.makeText(this,
                "报告生成中，请稍候",
                Toast.LENGTH_LONG).show();
        planHandler.postDelayed(planHintRunnable, PLAN_HINT_DELAY_MS);
    }

    private void clearPlanHint() {
        if (planHintRunnable != null) {
            planHandler.removeCallbacks(planHintRunnable);
            planHintRunnable = null;
        }
    }

    private void openTreatmentPlanActivity(String planJson) {
        Intent intent = new Intent(this, TreatmentPlanActivity.class);
        if (planJson != null) {
            intent.putExtra("planJsonString", planJson);
        }
        intent.putExtra("fName", fName);
        startActivity(intent);
    }

    private void openLocalTreatmentPlan() {
        try {
            JSONObject latestData = dataManager.getInstance().loadData(fName);
            if (latestData.optJSONObject("treatmentPlan") == null) {
                Toast.makeText(this, "暂无已保存的干预方案，请先生成", Toast.LENGTH_SHORT).show();
                return;
            }
            openTreatmentPlanActivity(null);
        } catch (Exception e) {
            Toast.makeText(this, "读取测评数据失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPDFWithSAF(String name) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, name);
        createDocumentLauncher.launch(intent);
    }

    private void writePDF(Uri uri,String fname) {
        try {
            ContentResolver contentResolver = getContentResolver();
            OutputStream outputStream = contentResolver.openOutputStream(uri);
            if (outputStream != null) {
                PdfGenerator util = new PdfGenerator(this);
                util.generatePdf(outputStream,fname);
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openPdfFile() {
        String PDF_FILE_PATH = this.getFilesDir()+"/file.pdf";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(PDF_FILE_PATH), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initData() throws Exception {
        //设置回调函数
        NetInteractUtils.getInstance(this).setUploadEvaluationCallback(uploadEvaluationCallback);
        NetInteractUtils.getInstance(this).setAudioCallback(audioCallback);

        //获取信息并展示
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
        data = dataManager.getInstance().loadData(fName);
        JSONObject evaluations = data.optJSONObject("evaluations");
        if (evaluations == null) {
            evaluations = new JSONObject();
            data.put("evaluations", evaluations);
        }
        completeDetails(evaluations);


        if (childUser != null) {//是从网络下载的，需要继续获取录音
            audioPaths = new HashMap<String, Map<String, String>>();
            Iterator<String> items = evaluations.keys();
            while (items.hasNext()) {
                HashMap<String, String> temp = new HashMap<String, String>();
                String title = items.next();
                JSONArray currentItem = evaluations.getJSONArray(title);
                for (int i = 0; i < currentItem.length(); i++) {
                    JSONObject element = currentItem.getJSONObject(i);
                    String num = element.getString("num");
                    String audioPath = element.getString("audioPath");
                    if (audioPath != null && !audioPath.equals("null")) {
                        temp.put(num, audioPath);
                    }
                }
                audioPaths.put(title, temp);
            }
            //开一个子线程去完成语音下载
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, Map<String, String>> outerEntry : audioPaths.entrySet()) {
                        String title = outerEntry.getKey();
                        Map<String, String> innerMap = outerEntry.getValue();
                        for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {
                            String num = innerEntry.getKey();
                            NetInteractUtils.getInstance(evmenuactivity.this).getAudio(uid, childUser, title, num);
                        }
                    }
                }
            });
            thread.start();
        }
    }


    private void completeDetails(JSONObject evaluations) throws JSONException {
        // 安全获取各数组字段，如果不存在则创建新的JSONArray
        JSONArray E_array = evaluations.optJSONArray("E");
        if (E_array == null) {
            E_array = new JSONArray();
            evaluations.put("E", E_array);
        }
        showDetails(Word1, E_array, "词汇能力评估", 7);
        
        JSONArray EV_array = evaluations.optJSONArray("EV");
        if (EV_array == null) {
            EV_array = new JSONArray();
            evaluations.put("EV", EV_array);
        }
        showDetails(Word2, EV_array, "词汇理解测试", 7);
        
        JSONArray NWR_array = evaluations.optJSONArray("NWR");
        if (NWR_array == null) {
            NWR_array = new JSONArray();
            evaluations.put("NWR", NWR_array);
        }

        
        JSONArray A_array = evaluations.optJSONArray("A");
        if (A_array == null) {
            A_array = new JSONArray();
            evaluations.put("A", A_array);
        }
        showDetails(Pronunciation, A_array, evaluation.A, ImageUrls.A_imageUrls.length);
        
        JSONArray RG_array = evaluations.optJSONArray("RG");
        if (RG_array == null) {
            RG_array = new JSONArray();
            evaluations.put("RG", RG_array);
        }
        showDetails(Grammar, RG_array, evaluation.RG, ImageUrls.RG_hints.length);
        

        
        // 确保SOCIAL数组存在
        JSONArray SOCIAL_array = evaluations.optJSONArray("SOCIAL");
        if (SOCIAL_array == null) {
            SOCIAL_array = new JSONArray();
            evaluations.put("SOCIAL", SOCIAL_array);
        }
        showDetails(SocialTest, SOCIAL_array, "社交能力评估", ImageUrls.SOCIAL_abilities.length);
        

    }

    private void showDetails(Button btn, JSONArray array, String ev, int length) throws JSONException {
        int len = 0;
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).has("time") && !array.getJSONObject(i).isNull("time"))
                len++;
        }
        if (len >= length) {
            btn.setEnabled(false);
            btn.setText(ev + "(已完成)");
        }
        // 删除统计标签，只显示题目名称
        btn.setText(ev);
    }


    private void uploadAudioInParallel(String Uid, String childUser, JSONObject evaluations) throws JSONException {
        ExecutorService executorService = Executors.newFixedThreadPool(4); // 创建一个线程池，指定线程数量

        Iterator<String> it = evaluations.keys();
        while (it.hasNext()) {
            String title = it.next();
            JSONArray currentArray = evaluations.getJSONArray(title);
            for (int i = 0; i < currentArray.length(); i++) {
                JSONObject element = currentArray.getJSONObject(i);
                int num = element.getInt("num");
                String audioPath = element.getString("audioPath");
                if (audioPath != null && !audioPath.equals("null")) {
                    Runnable uploadTask = () -> {
                        try {
                            NetInteractUtils.getInstance(evmenuactivity.this)
                                    .uploadAudio(Uid, childUser, title, String.valueOf(num), audioPath);
                        } catch (Exception e) {
                            e.printStackTrace(); // 处理上传异常
                        }
                    };
                    executorService.submit(uploadTask); // 提交任务到线程池
                }
            }
        }

        executorService.shutdown(); // 关闭线程池
    }

    public static void copyFile(String sourcePath, String destPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);

        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourcePath);
        }

        // 创建目标文件夹，如果它不存在
        File destDir = destFile.getParentFile();
        if (destDir != null && !destDir.exists()) {
            destDir.mkdirs();
        }

        // 使用文件输入输出流进行文件复制
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    @Override
    protected void onDestroy() {
        clearPlanHint();
        super.onDestroy();
    }

}
