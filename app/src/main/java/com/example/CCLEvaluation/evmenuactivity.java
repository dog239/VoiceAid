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
import utils.ArticulationPlanHelper;
import utils.dataManager;
import utils.dialogUtils;
import utils.dirpath;
import utils.ImageUrls;
import utils.LlmPlanService;
import utils.ModuleReportHelper;
import utils.Netinteractutils;
import utils.TreatmentPromptBuilder;

public class evmenuactivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private ConstraintLayout cl;
    private TextView pl;
    private Button Word1, Word2, Word3, Word4;
    private Button WordResult1, WordResult2, WordResult3, WordResult4;
    private Button Pronunciation;
    private Button PronunciationResult;
    private Button Grammar;
    private Button GrammarResult;
    private Button Narrate1, Narrate2;
    private Button NarrateResult1, NarrateResult2;
    private Button Upload, Download, Pdf, Plan, PlanView;
    private JSONObject data;
    private String fName;
    private String childUser;
    private String uid;
    private LinearLayout wd;
    private LinearLayout pn;
    private LinearLayout gm;
    private LinearLayout nr;
    private static final int MY_PERMISSIONS_REQUEST_READ_OR_WRITE_EXTERNAL_STORAGE = 1;

    private Map<String, Map<String, String>> audioPaths;

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
        Word3 = findViewById(R.id.btn_wordTest3);
        WordResult3 = findViewById(R.id.btn_wordResult3);
        Word4 = findViewById(R.id.btn_wordTest4);
        WordResult4 = findViewById(R.id.btn_wordResult4);
        Pronunciation = findViewById(R.id.btn_pronunciationTest);
        PronunciationResult = findViewById(R.id.btn_pronunciationResult);
        Grammar = findViewById(R.id.btn_grammarTest);
        GrammarResult = findViewById(R.id.btn_grammarResult);
        Narrate1 = findViewById(R.id.btn_narrateTest1);
        NarrateResult1 = findViewById(R.id.btn_narrateResult1);
        Narrate2 = findViewById(R.id.btn_narrateTest2);
        NarrateResult2 = findViewById(R.id.btn_narrateResult2);
        Upload = findViewById(R.id.btn_upload);
        Download = findViewById(R.id.btn_download);
        Pdf = findViewById(R.id.btn_pdf);
        Plan = findViewById(R.id.btn_plan);
        PlanView = findViewById(R.id.btn_plan_view);
        wd = findViewById(R.id.wd);
        pn = findViewById(R.id.pn);
        gm = findViewById(R.id.gm);
        nr = findViewById(R.id.nr);
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
//        if(childUser != null){
//            Download.setVisibility(View.VISIBLE);
//        }
        Netinteractutils.getInstance(evmenuactivity.this).setModuleCallback(new Netinteractutils.ModuleCallback() {
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
                if(PN.equals("1") && PST.equals("1")){
                    nr.setVisibility(View.VISIBLE);
                }else{
                    nr.setVisibility(View.GONE);
                }


            }
        });

        Netinteractutils.getInstance(evmenuactivity.this).getModule(uid);


        try {
            initData();
        } catch (Exception e) {
            Toast.makeText(this, "数据加载失败，请重试", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
        Word1.setOnClickListener(this);
        Word2.setOnClickListener(this);
        Word3.setOnClickListener(this);
        Word4.setOnClickListener(this);
        Pronunciation.setOnClickListener(this);
        Grammar.setOnClickListener(this);
        Narrate1.setOnClickListener(this);
        Narrate2.setOnClickListener(this);
        WordResult1.setOnClickListener(this);
        WordResult2.setOnClickListener(this);
        WordResult3.setOnClickListener(this);
        WordResult4.setOnClickListener(this);
        PronunciationResult.setOnClickListener(this);
        GrammarResult.setOnClickListener(this);
        NarrateResult1.setOnClickListener(this);
        NarrateResult2.setOnClickListener(this);
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
//             * @param isPlaying Whether to show loading animation
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
            Toast.makeText(this, "数据加载失败，请重试", Toast.LENGTH_SHORT).show();
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

    Netinteractutils.UploadEvaluationCallback uploadEvaluationCallback = new Netinteractutils.UploadEvaluationCallback() {
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
    Netinteractutils.AudioCallback audioCallback = new Netinteractutils.AudioCallback() {

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
                if (!file.getParentFile().exists()) {// Parent directory missing
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
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "E");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordTest2) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "RE");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordTest3) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "S");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordTest4) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "NWR");
            startActivity(intent);

        } else if (v.getId() == R.id.btn_pronunciationTest) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "A");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_grammarTest) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "RG");
            startActivity(intent);

        } else if (v.getId() == R.id.btn_narrateTest1) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "PST");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_narrateTest2) {
            Intent intent = new Intent(this, testactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "PN");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordResult1) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "E");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordResult2) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "RE");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordResult3) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "S");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_wordResult4) {
            Intent intent = new Intent(this, wordtest4result.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "NWR");
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
        } else if (v.getId() == R.id.btn_narrateResult1) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "PST");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_narrateResult2) {
            Intent intent = new Intent(this, resultactivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("format", "PN");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_upload) {
            dialogUtils.showDialog(this, "提示信息", "您确定要上传该儿童的测评信息吗？",
                    "确认", () -> {
                        if (childUser != null) {
                            Netinteractutils.getInstance(this).deleteEvaluation(uid, childUser);
                            childUser = null;
                        }
                        data = dataManager.getInstance().loadData(fName);//必须重新加载，可能会更新
                        Netinteractutils.getInstance(this).uploadEvaluation(uid, data.toString());

                        Intent intent = new Intent(evmenuactivity.this, mainactivity.class);
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
                Intent intent = new Intent(evmenuactivity.this, mainactivity.class);
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

        JSONObject evaluations = latestData.optJSONObject("evaluations");
        JSONArray evaluationsA = evaluations == null ? null : evaluations.optJSONArray("A");

        setLoading(true, "正在生成干预方案...");
        String systemPrompt = getString(R.string.treatment_plan_system_prompt);

        String userPrompt;
        try {
            userPrompt = TreatmentPromptBuilder.buildUserPrompt(latestData);
        } catch (JSONException e) {
            setLoading(false, null);
            Toast.makeText(this, "生成提示词失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        LlmPlanService service = new LlmPlanService();
        JSONArray finalEvaluationsA = evaluationsA;
        JSONObject finalEvaluations = evaluations;
        service.generateTreatmentPlan(systemPrompt, userPrompt, new LlmPlanService.PlanCallback() {
            @Override
            public void onSuccess(JSONObject plan) {
                ArticulationPlanHelper.ensureArticulation(plan, finalEvaluationsA);
                ArticulationPlanHelper.applyArticulationReport(plan, latestData, finalEvaluationsA);
                ModuleReportHelper.applyModuleFindings(plan, finalEvaluations);
                String pretty;
                try {
                    pretty = plan.toString(2);
                } catch (JSONException e) {
                    pretty = plan.toString();
                }
                boolean saved = false;
                String saveError = null;
                if (fName != null && !fName.trim().isEmpty()) {
                    try {
                        JSONObject data = dataManager.getInstance().loadData(fName);
                        data.put("treatmentPlan", plan);
                        dataManager.getInstance().saveChildJson(fName, data);
                        saved = true;
                    } catch (Exception e) {
                        saveError = e.getMessage();
                    }
                } else {
                    saveError = "未找到个案信息";
                }
                String finalPretty = pretty;
                boolean finalSaved = saved;
                String finalSaveError = saveError;
                runOnUiThread(() -> {
                    setLoading(false, null);
                    if (!finalSaved && finalSaveError != null && !finalSaveError.trim().isEmpty()) {
                        Toast.makeText(evmenuactivity.this, "保存失败: " + finalSaveError, Toast.LENGTH_LONG).show();
                        openTreatmentPlanActivity(finalPretty, true);
                        return;
                    }
                    openTreatmentPlanActivity(null);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false, null);
                    Toast.makeText(evmenuactivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

    private void openTreatmentPlanActivity(String planJson) {
        openTreatmentPlanActivity(planJson, false);
    }

    private void openTreatmentPlanActivity(String planJson, boolean preferIncomingPlan) {
        Intent intent = new Intent(this, TreatmentPlanActivity.class);
        if (planJson != null) {
            intent.putExtra("planJsonString", planJson);
        }
        intent.putExtra("preferIncomingPlan", preferIncomingPlan);
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
        Netinteractutils.getInstance(this).setUploadEvaluationCallback(uploadEvaluationCallback);
        Netinteractutils.getInstance(this).setAudioCallback(audioCallback);

        // Load data and render
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
        data = dataManager.getInstance().loadData(fName);
        JSONObject evaluations = data.getJSONObject("evaluations");
        completeDetails(evaluations);


        if (childUser != null) {// Downloaded from network; continue fetching audio
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
            // Spawn a worker thread to download audio
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, Map<String, String>> outerEntry : audioPaths.entrySet()) {
                        String title = outerEntry.getKey();
                        Map<String, String> innerMap = outerEntry.getValue();
                        for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {
                            String num = innerEntry.getKey();
                            Netinteractutils.getInstance(evmenuactivity.this).getAudio(uid, childUser, title, num);
                        }
                    }
                }
            });
            thread.start();
        }
    }


    private void completeDetails(JSONObject evaluations) throws JSONException {
        showDetails(Word1, evaluations.getJSONArray("E"), evaluation.E, ImageUrls.E_imageUrls.length);
        showDetails(Word2, evaluations.getJSONArray("RE"), evaluation.RE, ImageUrls.RE_imageUrls.length);
        showDetails(Word3, evaluations.getJSONArray("S"), evaluation.S, ImageUrls.S_words.length);
        showDetails(Word4, evaluations.getJSONArray("NWR"), evaluation.NWR, ImageUrls.NWR_characs.length);
        showDetails(Pronunciation, evaluations.getJSONArray("A"), evaluation.A, ImageUrls.getAImageCount());
        showDetails(Grammar, evaluations.getJSONArray("RG"), evaluation.RG, ImageUrls.RG_hints.length);
        showDetails(Narrate1, evaluations.getJSONArray("PST"), evaluation.PST, ImageUrls.PST_imageUrls.length);
        showDetails(Narrate2, evaluations.getJSONArray("PN"), evaluation.PN, ImageUrls.PN_hints.length);
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
        } else if (len > 0) {
            btn.setText(ev + "(完成题目" + len + "/" + length + ")");
        }
    }


    private void uploadAudioInParallel(String Uid, String childUser, JSONObject evaluations) throws JSONException {
        ExecutorService executorService = Executors.newFixedThreadPool(4); // Create a thread pool with fixed size

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
                            Netinteractutils.getInstance(evmenuactivity.this)
                                    .uploadAudio(Uid, childUser, title, String.valueOf(num), audioPath);
                        } catch (Exception e) {
                            e.printStackTrace(); // 处理上传异常
                        }
                    };
                    executorService.submit(uploadTask); // 提交任务到线程池
                }
            }
        }

        executorService.shutdown(); // 关闭线程?
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

        // Copy file with streams
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }


}
