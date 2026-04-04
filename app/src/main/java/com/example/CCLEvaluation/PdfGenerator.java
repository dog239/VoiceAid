package com.example.CCLEvaluation;


import static com.google.android.material.internal.ContextUtils.getActivity;
import static com.itextpdf.text.BaseColor.BLUE;
import static com.itextpdf.text.BaseColor.LIGHT_GRAY;
//import static com.itextpdf.kernel.pdf.PdfName.BaseFont;
//import static com.itextpdf.kernel.pdf.PdfName.T;
//import static com.itextpdf.kernel.pdf.PdfName.staticNames;
import static utils.permissionutils.permissionUtils;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
//
//import com.itextpdf.io.font.PdfEncodings;
//import com.itextpdf.kernel.colors.Color;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.borders.Border;
//import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import java.io.IOException;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import bean.a;
import bean.audio;
import bean.e;
import bean.re;
import bean.rg;
import bean.s;
import utils.ImageUrls;
import utils.ArticulationPlanHelper;
import utils.MedicalDiagnosisImageHelper;
import utils.ModuleReportHelper;
import utils.dataManager;
import utils.permissionutils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
//import com.itextpdf.io.font.constants.StandardFonts;
//import com.itextpdf.kernel.colors.ColorConstants;
//import com.itextpdf.kernel.font.PdfFont;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Cell;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.property.HorizontalAlignment;
//import com.itextpdf.layout.property.TextAlignment;
//import com.itextpdf.layout.property.TransparentColor;
//import com.itextpdf.layout.property.UnitValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class PdfGenerator extends evmenuactivity{
     static private Context context;
    private static final String CHINESE_FONT_ASSET_PATH = "fonts/songsim.ttf";
    private static Typeface cachedChineseTypeface;
    private static BaseFont cachedChineseBaseFont;
    private static final ThreadLocal<JSONObject> evaluationDataOverride = new ThreadLocal<>();
    private static final ThreadLocal<String> evaluationModuleOverride = new ThreadLocal<>();




    public PdfGenerator(Context context) {
        this.context = context;
    }

    private static Context requireContext() {
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        return context;
    }

    private static String getStringRes(int resId) {
        return requireContext().getString(resId);
    }

    private static Typeface loadChineseTypeface(Context context) {
        if (cachedChineseTypeface != null) {
            return cachedChineseTypeface;
        }
        if (context == null) {
            return null;
        }
        try {
            cachedChineseTypeface = Typeface.createFromAsset(context.getAssets(), CHINESE_FONT_ASSET_PATH);
        } catch (Exception ignored) {
            cachedChineseTypeface = null;
        }
        return cachedChineseTypeface;
    }

    private static BaseFont loadChineseBaseFont(Context context) {
        if (cachedChineseBaseFont != null) {
            return cachedChineseBaseFont;
        }
        if (context == null) {
            return null;
        }
        InputStream input = null;
        try {
            input = context.getAssets().open(CHINESE_FONT_ASSET_PATH);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            byte[] fontData = output.toByteArray();
            cachedChineseBaseFont = BaseFont.createFont(
                    "songsim.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontData,
                    null
            );
        } catch (Exception ignored) {
            cachedChineseBaseFont = null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }
        return cachedChineseBaseFont;
    }

    private static BaseFont createFallbackBaseFont() {
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception ignored) {
            return null;
        }
    }

    // 根据组别和题目编号确定RG测试点
    private static int getRGTestPoint(int group, int num) {
        // 简单动词短语理解
        if ((group == 1 && num >= 1 && num <= 3) || 
            (group == 2 && num >= 1 && num <= 3) || 
            (group == 3 && num >= 1 && num <= 3) || 
            (group == 4 && num >= 1 && num <= 3)) {
            return 1;
        }
        // 简单名词短语理解
        if ((group == 1 && num >= 4 && num <= 6) || 
            (group == 2 && num >= 4 && num <= 6) || 
            (group == 3 && num >= 4 && num <= 6) || 
            (group == 4 && num >= 4 && num <= 6)) {
            return 2;
        }
        // 疑问句理解
        if ((group == 1 && num >= 13 && num <= 15) || 
            (group == 2 && num >= 7 && num <= 9) || 
            (group == 3 && num >= 10 && num <= 12) || 
            (group == 4 && num >= 7 && num <= 9)) {
            return 3;
        }
        // 否定句理解
        if ((group == 1 && num >= 10 && num <= 12) || 
            (group == 2 && num >= 13 && num <= 15) || 
            (group == 3 && num >= 7 && num <= 9) || 
            (group == 4 && num >= 4 && num <= 6)) {
            return 4;
        }
        // 比较句理解
        if ((group == 1 && num >= 4 && num <= 6) || 
            (group == 2 && num >= 16 && num <= 18) || 
            (group == 3 && num >= 4 && num <= 6) || 
            (group == 4 && num >= 10 && num <= 15)) {
            return 5;
        }
        // 复合句理解
        if ((group == 1 && num >= 7 && num <= 9) || 
            (group == 2 && num >= 10 && num <= 12) || 
            (group == 3 && num >= 13 && num <= 15) || 
            (group == 4 && num >= 10 && num <= 15)) {
            return 6;
        }
        return 0;
    }

    // 根据组别和题目编号确定SE测试点
    private static int getSETestPoint(int group, int num) {
        // 简单句表达
        if ((group == 1 && num >= 1 && num <= 5) || 
            (group == 2 && num >= 10 && num <= 15) || 
            (group == 3 && num >= 1 && num <= 3) || 
            (group == 4 && num >= 1 && num <= 3)) {
            return 1;
        }
        // 疑问句表达
        if ((group == 1 && num >= 13 && num <= 15) || 
            (group == 2 && num >= 4 && num <= 9) || 
            (group == 3 && num >= 10 && num <= 12) || 
            (group == 4 && num >= 4 && num <= 6)) {
            return 2;
        }
        // 因果句表达
        if ((group == 1 && num >= 10 && num <= 12) || 
            (group == 2 && num >= 1 && num <= 3) || 
            (group == 3 && num >= 13 && num <= 15) || 
            (group == 4 && num >= 1 && num <= 3)) {
            return 3;
        }
        // 条件句表达
        if ((group == 1 && num >= 7 && num <= 9) || 
            (group == 2 && num >= 10 && num <= 12) || 
            (group == 3 && num >= 7 && num <= 9) || 
            (group == 4 && num >= 10 && num <= 12)) {
            return 4;
        }
        // 比较句表达
        if ((group == 1 && num >= 4 && num <= 6) || 
            (group == 2 && num >= 13 && num <= 15) || 
            (group == 3 && num >= 4 && num <= 6) || 
            (group == 4 && num >= 13 && num <= 15)) {
            return 5;
        }
        // 复合句表达
        if ((group == 1 && num >= 1 && num <= 3) || 
            (group == 2 && num >= 10 && num <= 12) || 
            (group == 3 && num >= 16 && num <= 18) || 
            (group == 4 && num >= 7 && num <= 9)) {
            return 6;
        }
        return 0;
    }

    // 根据format、组别和题目编号确定测试语法点
    private static String getTestLanguage(String format, int group, int questionNumber) {
        // 句法理解部分
        if (format != null && format.equals("RG")) {
            switch (group) {
                case 1:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "主谓结构";
                        case 4:
                        case 5:
                        case 6:
                            return "动宾结构";
                        case 7:
                        case 8:
                        case 9:
                            return "主谓宾";
                        case 10:
                        case 11:
                        case 12:
                            return "否定句";
                        case 13:
                        case 14:
                        case 15:
                            return "一般疑问句";
                        case 16:
                        case 17:
                        case 18:
                            return "特殊疑问句";
                        case 19:
                        case 20:
                        case 21:
                            return "形容词+名词";
                        default:
                            return "";
                    }
                case 2:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "多重修饰";
                        case 4:
                        case 5:
                        case 6:
                            return "双宾结构";
                        case 7:
                        case 8:
                        case 9:
                            return "是不是问句";
                        case 10:
                        case 11:
                        case 12:
                            return "地点疑问句";
                        case 13:
                        case 14:
                        case 15:
                            return "副词都";
                        case 16:
                        case 17:
                        case 18:
                            return "语序";
                        default:
                            return "";
                    }
                case 3:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "被动句";
                        case 4:
                        case 5:
                        case 6:
                            return "比较句";
                        case 7:
                        case 8:
                        case 9:
                            return "了（完成体）";
                        case 10:
                        case 11:
                        case 12:
                            return "因果复句";
                        case 13:
                        case 14:
                        case 15:
                            return "转折句";
                        case 16:
                        case 17:
                        case 18:
                            return "时间顺序句";
                        default:
                            return "";
                    }
                case 4:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "着（持续体）";
                        case 4:
                        case 5:
                        case 6:
                            return "双重否定";
                        case 7:
                        case 8:
                        case 9:
                            return "条件句（排除）";
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            return "篇章理解能力";
                        default:
                            return "";
                    }
                default:
                    return "";
            }
        }
        // 句法表达部分
        else if (format != null && format.equals("SE")) {
            switch (group) {
                case 1:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "主谓结构";
                        case 4:
                        case 5:
                        case 6:
                            return "动宾结构";
                        case 7:
                        case 8:
                        case 9:
                            return "形容词+名词";
                        case 10:
                        case 11:
                        case 12:
                            return "主谓宾";
                        case 13:
                        case 14:
                        case 15:
                            return "否定句";
                        default:
                            return "";
                    }
                case 2:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "双宾结构";
                        case 4:
                        case 5:
                        case 6:
                            return "一般疑问句";
                        case 7:
                        case 8:
                        case 9:
                            return "特殊疑问句";
                        case 10:
                        case 11:
                        case 12:
                            return "地点/方位";
                        case 13:
                        case 14:
                        case 15:
                            return "多重修饰";
                        default:
                            return "";
                    }
                case 3:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "被动句";
                        case 4:
                        case 5:
                        case 6:
                            return "了（完成体）";
                        case 7:
                        case 8:
                        case 9:
                            return "副词都";
                        case 10:
                        case 11:
                        case 12:
                            return "语序";
                        case 13:
                        case 14:
                        case 15:
                            return "时间顺序句";
                        default:
                            return "";
                    }
                case 4:
                    switch (questionNumber) {
                        case 1:
                        case 2:
                        case 3:
                            return "因果复句";
                        case 4:
                        case 5:
                        case 6:
                            return "转折句";
                        case 7:
                        case 8:
                        case 9:
                            return "正在/在/着（进行体）";
                        case 10:
                        case 11:
                        case 12:
                            return "假设条件句";
                        case 13:
                        case 14:
                        case 15:
                            return "比较句";
                        case 16:
                            return "看图讲故事";
                        default:
                            return "";
                    }
                default:
                    return "";
            }
        }
        else {
            return "";
        }
    }


    @SuppressLint("NewApi")
    public static void generatePdf(OutputStream outputStream, String fname) {
        Document document = new Document();
        try {
            String[] scoreA =new String[21];
            String scoreE;
            String scorePNAVE;
            String[] scorePN = new String[3];
            String scorePSTAVE;
            String[] scorePST = new String[5];
            String scoreRE;
            String scoreRG;
            String scoreS;
            String scoreNWR;
            String name;
            String c;
            String serialNumber;
            String testData;
            String birthDate;
            String testLocation;
            String examiner;

            JSONObject data = evaluationDataOverride.get();
            if (data == null) {
                data = dataManager.getInstance().loadData(fname);
            }
            JSONObject info = data.optJSONObject("info");
            JSONObject evaluations = data.optJSONObject("evaluations");
            if (evaluations == null) {
                evaluations = new JSONObject();
            }

            String moduleKey = normalizeModuleKey(evaluationModuleOverride.get());
            boolean includeAll = moduleKey.isEmpty();
            boolean showArticulation = includeAll || "articulation".equals(moduleKey);
            boolean showPrelinguistic = includeAll || "prelinguistic".equals(moduleKey);
            boolean showVocabulary = includeAll || "vocabulary".equals(moduleKey);
            boolean showSyntax = includeAll || "syntax".equals(moduleKey) || "syntax_comprehension".equals(moduleKey) || "syntax_expression".equals(moduleKey);
            boolean showSocial = includeAll || "social".equals(moduleKey);

            name = info == null ? "" : info.optString("name", "");
            c = info == null ? "" : info.optString("class", "");
            serialNumber = info == null ? "" : info.optString("serialNumber", "");
            birthDate = info == null ? "" : info.optString("birthDate", "");
            testData = info == null ? "" : info.optString("testDate", "");
            testLocation = info == null ? "" : info.optString("testLocation", "");
            examiner = info == null ? "" : info.optString("examiner", "");

//  A
            String[] characs = ImageUrls.A_characs;
            int[] score = new int[21];
            JSONArray jsonArrayA = evaluations.optJSONArray("A");
            if (jsonArrayA == null) {
                jsonArrayA = new JSONArray();
            }
            int num[] = ImageUrls.A_nums;
            if (jsonArrayA.length() == 0) {
                for (int i = 0; i < num.length; ++i) {
                    scoreA[i] = "   ";
                }
            } else {
                for (int i = 0; i < jsonArrayA.length(); i++) {
                    JSONObject object = jsonArrayA.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    String time = object.optString("time", "");
                    if (!time.isEmpty() && !"null".equals(time)) {
                        String tone1 = object.optString("target_tone1", "");
                        if (!tone1.isEmpty()) {
                            for (int j = 0; j < characs.length; j++) {
                                if (characs[j].equals(tone1)) {
                                    score[j]++;
                                }
                            }
                        }
                        String tone2 = object.optString("target_tone2", "");
                        if (!tone2.isEmpty()) {
                            for (int j = 0; j < characs.length; j++) {
                                if (characs[j].equals(tone2)) {
                                    score[j]++;
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < num.length; ++i) {
                    double lentha = num[i];
                    double scorea = lentha == 0 ? 0 : (score[i] / lentha) * 100;
                    scoreA[i] = String.format("%.2f%%", scorea);
                }
            }
//        E
            double counte = 0;
            JSONArray jsonArrayE = evaluations.optJSONArray("E");
            if (jsonArrayE == null) {
                jsonArrayE = new JSONArray();
            }
            for (int i = 0; i < jsonArrayE.length(); i++) {
                JSONObject object = jsonArrayE.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                if (object.has("result") && !object.isNull("result")) {
                    if (object.optBoolean("result", false)) {
                        counte++;
                    }
                }
            }
            double lenthe = jsonArrayE.length();
            if (lenthe == 0) {
                scoreE = " ";
            } else {
                double scoree = (counte / lenthe) * 100;
                scoreE = String.format("%.2f%%", scoree);
            }


//RE (使用EV模块数据，词汇理解测试)
            double countre = 0;
            JSONArray jsonArrayRE = evaluations.optJSONArray("EV");
            if (jsonArrayRE == null) {
                jsonArrayRE = new JSONArray();
            }
            for (int i = 0; i < jsonArrayRE.length(); i++) {
                JSONObject object = jsonArrayRE.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                if (object.has("result") && !object.isNull("result")) {
                    if (object.optBoolean("result", false)) {
                        countre++;
                    }
                }
            }
            double lenthre = jsonArrayRE.length();
            if (lenthre == 0) {
                scoreRE = " ";
            } else {
                double scorere = (countre / lenthre) * 100;
                scoreRE = String.format("%.2f%%", scorere);
            }
//        RG
            double countrg = 0;
            double lenthrg = 0;
            // 检查所有RG组的数据
            for (int group = 1; group <= 4; group++) {
                JSONArray jsonArrayRG = evaluations.optJSONArray("RG" + group);
                if (jsonArrayRG != null) {
                    for (int i = 0; i < jsonArrayRG.length(); i++) {
                        JSONObject object = jsonArrayRG.optJSONObject(i);
                        if (object == null) {
                            continue;
                        }
                        String time = object.optString("time", "");
                        if (!time.isEmpty() && !"null".equals(time)) {
                            if (object.optBoolean("result", false)) {
                                countrg++;
                            }
                            lenthrg++;
                        }
                    }
                }
            }
            if (lenthrg == 0) {
                scoreRG = " ";
            } else {
                double scorerg = (countrg / lenthrg) * 100;
                scoreRG = String.format("%.2f%%", scorerg);
            }

//        SE (句法表达)
            double countse = 0;
            double lenthse = 0;
            // 检查所有SE组的数据
            for (int group = 1; group <= 4; group++) {
                JSONArray jsonArraySE = evaluations.optJSONArray("SE" + group);
                if (jsonArraySE != null) {
                    for (int i = 0; i < jsonArraySE.length(); i++) {
                        JSONObject object = jsonArraySE.optJSONObject(i);
                        if (object == null) {
                            continue;
                        }
                        String time = object.optString("time", "");
                        if (!time.isEmpty() && !"null".equals(time)) {
                            if (object.optBoolean("result", false)) {
                                countse++;
                            }
                            lenthse++;
                        }
                    }
                }
            }
            String scoreSE;
            if (lenthse == 0) {
                scoreSE = " ";
            } else {
                double scorese = (countse / lenthse) * 100;
                scoreSE = String.format("%.2f%%", scorese);
            }
            // 综合RG和SE的结果
            String scoreSyntax;
            if (lenthrg + lenthse == 0) {
                scoreSyntax = " ";
            } else {
                double totalSyntaxScore = ((countrg + countse) / (lenthrg + lenthse)) * 100;
                scoreSyntax = String.format("%.2f%%", totalSyntaxScore);
            }

//S
            double counts = 0;
            JSONArray jsonArrayS = evaluations.optJSONArray("S");
            if (jsonArrayS == null) {
                jsonArrayS = new JSONArray();
            }
            for (int i = 0; i < jsonArrayS.length(); i++) {
                JSONObject object = jsonArrayS.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                if (object.has("result") && !object.isNull("result")) {
                    if (object.optBoolean("result", false)) {
                        counts++;
                    }
                }
            }
            double lenths = jsonArrayS.length();
            if (lenths == 0) {
                scoreS = " ";
            } else {
                double scores = (counts / lenths) * 100;
                scoreS = String.format("%.2f%%", scores);
            }


//NWR
            double countnwr = 0;
            JSONArray jsonArrayNWR = evaluations.optJSONArray("NWR");
            if (jsonArrayNWR == null) {
                jsonArrayNWR = new JSONArray();
            }
            for (int i = 0; i < jsonArrayNWR.length(); i++) {
                JSONObject object = jsonArrayNWR.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                for (int j = 0; j < 6; ++j) {
                    String key = "results" + (j + 1);
                    if (object.has(key) && !object.isNull(key)) {
                        if (object.optBoolean(key, false)) {
                            countnwr++;
                        }
                    }
                }
            }
            double lenthnwr = jsonArrayNWR.length() * 6;
            if (lenthnwr == 0) {
                scoreNWR = " ";
            } else {
                double scorenwr = (countnwr / lenthnwr) * 100;
                scoreNWR = String.format("%.2f%%", scorenwr);
            }

//PST
            double countpst = 0;
            double lenthpst = 0;
            JSONArray jsonArrayPST = evaluations.optJSONArray("PST");
            if (jsonArrayPST == null) {
                jsonArrayPST = new JSONArray();
            }
            if (jsonArrayPST.length() == 0) {
                Arrays.fill(scorePST, " ");
            } else {
                for (int i = 0; i < jsonArrayPST.length(); i++) {
                    JSONObject object = jsonArrayPST.optJSONObject(i);
                    if (object == null) {
                        scorePST[i] = "";
                        continue;
                    }
                    String time = object.optString("time", "");
                    if (!time.isEmpty() && !"null".equals(time)) {
                        int sc = object.optInt("score", 0);
                        scorePST[i] = String.valueOf(sc);
                        countpst += sc;
                        lenthpst++;
                    } else {
                        scorePST[i] = "";
                    }
                }
            }

            if (lenthpst != jsonArrayPST.length()) {
                scorePSTAVE = " ";
            } else {
                double scorepst = countpst / lenthpst;
                scorePSTAVE = String.format("%.2f", scorepst);
            }


//PN
            double countpn = 0;
            double lenthpn = 0;
            JSONArray jsonArrayPN = evaluations.optJSONArray("PN");
            if (jsonArrayPN == null) {
                jsonArrayPN = new JSONArray();
            }
            if (jsonArrayPN.length() == 0) {
                Arrays.fill(scorePN, " ");
            } else {
                for (int i = 0; i < jsonArrayPN.length(); i++) {
                    JSONObject object = jsonArrayPN.optJSONObject(i);
                    if (object == null) {
                        scorePN[i] = "";
                        continue;
                    }
                    String time = object.optString("time", "");
                    if (!time.isEmpty() && !"null".equals(time)) {
                        int sc = object.optInt("score", 0);
                        scorePN[i] = String.valueOf(sc);
                        countpn += sc;
                        lenthpn++;
                    } else {
                        scorePN[i] = "";
                    }
                }
            }

            if(lenthpn != jsonArrayPN.length()){
                scorePNAVE = " ";
            }else{
                double scorepn = countpn/lenthpn;
                scorePNAVE = String.format("%.2f",scorepn);
            }


            PdfWriter.getInstance(document, outputStream);
            document.open();
            // 最大宽度
            Font simsun = FontFactory.getFont("assets/fonts/songsim.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
            Font simsunBold = new com.itextpdf.text.Font(simsun.getBaseFont(), 10, Font.BOLD);
            Font simsunBig = new com.itextpdf.text.Font(simsun.getBaseFont(), 18, Font.BOLD);
            Font simsunSmall = new com.itextpdf.text.Font(simsun.getBaseFont(), 8, Font.BOLD);

            //设置内容
            Paragraph paragraph = new Paragraph("学前儿童汉语测评报告",simsunBig);
            paragraph.setAlignment(1);
            //引用字体
            document.add(paragraph);



            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(40f);
            table.setSpacingAfter(15f);
            addHeaderCell(table,"儿童信息",simsunBold,6);
            addHeaderCell(table,"姓名",simsun,1);
            addDataCell(table,name,simsun,5);
            addHeaderCell(table,"班级",simsun,1);
            addDataCell(table,c,simsun,2);
            addHeaderCell(table,"序号",simsun,1);
            addDataCell(table,serialNumber,simsun,2);
            addHeaderCell(table,"出生日期",simsun,1);
            addDataCell(table,birthDate,simsun,2);
            addHeaderCell(table,"测试日期",simsun,1);
            addDataCell(table,testData,simsun,2);
            addHeaderCell(table,"测试地点",simsun,1);
            addDataCell(table,testLocation,simsun,2);
            addHeaderCell(table,"测试员",simsun,1);
            addDataCell(table,examiner,simsun,2);
            document.add(table);



            // 创建表格，设置宽度
            PdfPTable table21 = new PdfPTable(4);
            table21.setWidthPercentage(100);

            table21.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addHeaderCell(table21, "1", simsun,1);
            addHeaderCell(table21, "2", simsun,1);
            addHeaderCell(table21, "3", simsun,1);
            addHeaderCell(table21, "平均值", simsun,1);
            for (String scorepn : scorePN) {
                addDataCell(table21, scorepn, simsun,1);
            }
            addDataCell(table21, scorePNAVE, simsun,1);
            // 创建表格，设置宽度
            PdfPTable table22 = new PdfPTable(6);
            table22.setWidthPercentage(100);
            table22.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            // 添加标题单元格
            addHeaderCell(table22, "1", simsun,1);
            addHeaderCell(table22, "2", simsun,1);
            addHeaderCell(table22, "3", simsun,1);
            addHeaderCell(table22, "4", simsun,1);
            addHeaderCell(table22, "5", simsun,1);
            addHeaderCell(table22, "平均值", simsunSmall,1);
            for (String scorepst : scorePST) {
                addDataCell(table22, scorepst, simsun,1);
            }
            addDataCell(table22, scorePSTAVE, simsun,1);


            PdfPTable table2 = new PdfPTable(6);
            table2.setWidthPercentage(100);
            table2.setSpacingBefore(15f);
            table2.setSpacingAfter(15f);

            addHeaderCell(table2,"测评结果",simsunBold,6);
            addHeaderCell(table2,"词汇表达（E）",simsun,1);
            addDataCell(table2,scoreE,simsun,2);
            addHeaderCell(table2,"词汇理解（RE）",simsun,1);
            addDataCell(table2,scoreRE,simsun,2);
            addHeaderCell(table2,"词义（S）",simsun,1);
            addDataCell(table2,scoreS,simsun,2);
            addHeaderCell(table2,"非词复述（NWR）",simsun,1);
            addDataCell(table2,scoreNWR,simsun,2);
            addHeaderCell(table2,"语法理解（RG）",simsun,1);
            addDataCell(table2,scoreRG,simsun,2);
            addHeaderCell(table2,"句法表达（SE）",simsun,1);
            addDataCell(table2,scoreSE,simsun,2);
            addHeaderCell(table2,"句法能力综合",simsun,1);
            addDataCell(table2,scoreSyntax,simsun,2);
            addHeaderCell2(table2,"看图说故事（PST）",simsun,1, LIGHT_GRAY,30f);
            addTableCell(table2,table22,2,30f);
            addHeaderCell2(table2,"个人生活经验（PN）",simsun,1, LIGHT_GRAY,30f);
            addTableCell(table2,table21,5,30f);
            if (includeAll) {
                document.add(table2);
            }

            JSONObject plReport = ModuleReportHelper.loadPrelinguisticReport(data);
            String plScene = plReport != null ? plReport.optString("scene", "") : "";
            int plTotalScore = plReport != null ? plReport.optInt("totalScore", -1) : -1;
            String plSummaryText = plReport != null ? plReport.optString("summaryText", "") : "";
            int plSuggestionOption = plReport != null ? plReport.optInt("suggestionOption", 1) : 1;
            JSONArray plSuggestionOptions = plReport != null ? plReport.optJSONArray("suggestionOptions") : null;
            String plOption1Text = ModuleReportHelper.getPrelinguisticSuggestionOptionText(1);
            String plOption2Text = ModuleReportHelper.getPrelinguisticSuggestionOptionText(2);
            if (plSuggestionOptions != null && plSuggestionOptions.length() >= 2) {
                String savedOption1 = plSuggestionOptions.optString(0, "").trim();
                String savedOption2 = plSuggestionOptions.optString(1, "").trim();
                if (!savedOption1.isEmpty()) {
                    plOption1Text = savedOption1;
                }
                if (!savedOption2.isEmpty()) {
                    plOption2Text = savedOption2;
                }
            }
            List<String> plStrengths = new ArrayList<>();
            List<String> plWeaknesses = new ArrayList<>();
            int computedScore = 0;
            JSONArray plArray = evaluations.optJSONArray("PL");
            if (plArray != null) {
                for (int i = 0; i < plArray.length(); i++) {
                    JSONObject item = plArray.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    int scoreValue = item.has("score") && !item.isNull("score") ? item.optInt("score", 0) : 0;
                    String skill = item.optString("skill", "");
                    if (scoreValue == 1) {
                        computedScore++;
                        if (!skill.trim().isEmpty()) {
                            plStrengths.add(skill);
                        }
                    } else if (!skill.trim().isEmpty()) {
                        plWeaknesses.add(skill);
                    }
                }
            }
            if (plTotalScore < 0) {
                plTotalScore = computedScore;
            }
            if (plSummaryText == null || plSummaryText.trim().isEmpty()) {
                plSummaryText = ModuleReportHelper.buildPrelinguisticSummaryText(plStrengths, plWeaknesses);
            }
            if (plScene == null || plScene.trim().isEmpty()) {
                plScene = "未选择";
            }
            String plSceneLabel;
            if ("A".equals(plScene)) {
                plSceneLabel = "A 吹泡泡场景";
            } else if ("B".equals(plScene)) {
                plSceneLabel = "B 玩球场景";
            } else {
                plSceneLabel = plScene;
            }
            // 词汇能力评估结果
            if (showVocabulary) {
                document.add(new Paragraph("三、词汇能力评估", simsunBold));
                document.add(new Paragraph("（一）记录表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 1. 词汇理解结果表格
                document.add(new Paragraph("(1) 词汇理解结果", simsunBold));
                document.add(new Paragraph(" ", simsun));

                PdfPTable tableRE = new PdfPTable(5);
                tableRE.setWidthPercentage(100);
                tableRE.setSpacingBefore(10f);
                tableRE.setSpacingAfter(15f);

                // 设置列宽比例
                float[] columnWidthsRE = {1f, 2f, 2f, 1.5f, 1.5f};
                tableRE.setWidths(columnWidthsRE);

                // 添加表头
                addHeaderCell(tableRE, "序号", simsun, 1);
                addHeaderCell(tableRE, "测试点", simsun, 1);
                addHeaderCell(tableRE, "目标词", simsun, 1);
                addHeaderCell(tableRE, "结果", simsun, 1);
                addHeaderCell(tableRE, "答题时长", simsun, 1);

                // 词汇理解测试点和目标词
                String[] reTestPoints = {"名词", "名词", "动词", "动词", "形容词", "分类名词（名词上位词）", "形容词"};
                String[] reTargets = {"勺子", "眼睛", "跑", "吃", "红", "动物", "大"};

                int reCorrect = 0;
                for (int i = 0; i < reTestPoints.length; i++) {
                    addDataCell(tableRE, String.valueOf(i + 1), simsun, 1);
                    addDataCell(tableRE, reTestPoints[i], simsun, 1);
                    addDataCell(tableRE, reTargets[i], simsun, 1);

                    String result = "错误";
                    String time = "00:00";

                    if (i < jsonArrayRE.length()) {
                        JSONObject reObject = jsonArrayRE.optJSONObject(i);
                        if (reObject != null) {
                            if (reObject.has("result") && !reObject.isNull("result")) {
                                if (reObject.optBoolean("result", false)) {
                                    result = "正确";
                                    reCorrect++;
                                }
                            }
                            if (reObject.has("time") && !reObject.isNull("time")) {
                                time = reObject.optString("time", "00:00");
                            }
                        }
                    }

                    addDataCell(tableRE, result, simsun, 1);
                    addDataCell(tableRE, time, simsun, 1);
                }
                
                // 添加词汇理解结果汇总
                addDataCell(tableRE, "", simsun, 2);
                addDataCell(tableRE, "词汇理解结果", simsun, 2);
                addDataCell(tableRE, reCorrect + "/" + reTestPoints.length, simsun, 1);

                document.add(tableRE);
                document.add(new Paragraph(" ", simsun));

                // 2. 词汇表达结果表格
                document.add(new Paragraph("(2) 词汇表达结果", simsunBold));
                document.add(new Paragraph(" ", simsun));

                PdfPTable tableE = new PdfPTable(5);
                tableE.setWidthPercentage(100);
                tableE.setSpacingBefore(10f);
                tableE.setSpacingAfter(15f);

                // 设置列宽比例
                float[] columnWidthsE = {1f, 2f, 2f, 1.5f, 1.5f};
                tableE.setWidths(columnWidthsE);

                // 添加表头
                addHeaderCell(tableE, "序号", simsun, 1);
                addHeaderCell(tableE, "测试点", simsun, 1);
                addHeaderCell(tableE, "目标词", simsun, 1);
                addHeaderCell(tableE, "结果", simsun, 1);
                addHeaderCell(tableE, "答题时长", simsun, 1);

                // 词汇表达测试点和目标词
                String[] eTestPoints = {"名词", "名词", "动词", "动词", "形容词", "分类名词（名词上位词）", "形容词"};
                String[] eTargets = {"杯子", "耳朵", "睡觉", "喝水", "蓝色", "水果", "长"};

                int eCorrect = 0;
                for (int i = 0; i < eTestPoints.length; i++) {
                    addDataCell(tableE, String.valueOf(i + 1), simsun, 1);
                    addDataCell(tableE, eTestPoints[i], simsun, 1);
                    addDataCell(tableE, eTargets[i], simsun, 1);

                    String result = "错误";
                    String time = "00:00";

                    if (i < jsonArrayE.length()) {
                        JSONObject eObject = jsonArrayE.optJSONObject(i);
                        if (eObject != null) {
                            if (eObject.has("result") && !eObject.isNull("result")) {
                                if (eObject.optBoolean("result", false)) {
                                    result = "正确";
                                    eCorrect++;
                                }
                            }
                            if (eObject.has("time") && !eObject.isNull("time")) {
                                time = eObject.optString("time", "00:00");
                            }
                        }
                    }

                    addDataCell(tableE, result, simsun, 1);
                    addDataCell(tableE, time, simsun, 1);
                }

                // 添加词汇表达结果汇总
                addDataCell(tableE, "", simsun, 2);
                addDataCell(tableE, "词汇表达结果", simsun, 2);
                addDataCell(tableE, eCorrect + "/" + eTestPoints.length, simsun, 1);

                document.add(tableE);
                document.add(new Paragraph(" ", simsun));

                // （二）评估结果
                document.add(new Paragraph("（二）评估结果", simsunBold));
                double reRate = reTestPoints.length > 0 ? (double)reCorrect / reTestPoints.length * 100 : 0;
                double eRate = eTestPoints.length > 0 ? (double)eCorrect / eTestPoints.length * 100 : 0;
                document.add(new Paragraph("词汇理解正确率：" + String.format("%.2f%%", reRate), simsun));
                document.add(new Paragraph("词汇表达正确率：" + String.format("%.2f%%", eRate), simsun));
                document.add(new Paragraph(" ", simsun));

                // （三）评估建议
                document.add(new Paragraph("（三）评估建议", simsunBold));
                document.add(new Paragraph("词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达。根据测评结果显示，", simsun));

                // 统计各词类的正确率
                // 分类名词：索引5（词汇理解）和索引5（词汇表达）
                int categoryTotal = 2;
                int categoryCorrect = 0;
                if (jsonArrayRE.length() > 5) {
                    JSONObject obj = jsonArrayRE.optJSONObject(5);
                    if (obj != null && obj.optBoolean("result", false)) categoryCorrect++;
                }
                if (jsonArrayE.length() > 5) {
                    JSONObject obj = jsonArrayE.optJSONObject(5);
                    if (obj != null && obj.optBoolean("result", false)) categoryCorrect++;
                }

                // 形容词：索引4,6（词汇理解）和索引4,6（词汇表达）
                int adjTotal = 4;
                int adjCorrect = 0;
                if (jsonArrayRE.length() > 4) {
                    JSONObject obj = jsonArrayRE.optJSONObject(4);
                    if (obj != null && obj.optBoolean("result", false)) adjCorrect++;
                }
                if (jsonArrayRE.length() > 6) {
                    JSONObject obj = jsonArrayRE.optJSONObject(6);
                    if (obj != null && obj.optBoolean("result", false)) adjCorrect++;
                }
                if (jsonArrayE.length() > 4) {
                    JSONObject obj = jsonArrayE.optJSONObject(4);
                    if (obj != null && obj.optBoolean("result", false)) adjCorrect++;
                }
                if (jsonArrayE.length() > 6) {
                    JSONObject obj = jsonArrayE.optJSONObject(6);
                    if (obj != null && obj.optBoolean("result", false)) adjCorrect++;
                }

                // 动词：索引2,3（词汇理解）和索引2,3（词汇表达）
                int verbTotal = 4;
                int verbCorrect = 0;
                if (jsonArrayRE.length() > 2) {
                    JSONObject obj = jsonArrayRE.optJSONObject(2);
                    if (obj != null && obj.optBoolean("result", false)) verbCorrect++;
                }
                if (jsonArrayRE.length() > 3) {
                    JSONObject obj = jsonArrayRE.optJSONObject(3);
                    if (obj != null && obj.optBoolean("result", false)) verbCorrect++;
                }
                if (jsonArrayE.length() > 2) {
                    JSONObject obj = jsonArrayE.optJSONObject(2);
                    if (obj != null && obj.optBoolean("result", false)) verbCorrect++;
                }
                if (jsonArrayE.length() > 3) {
                    JSONObject obj = jsonArrayE.optJSONObject(3);
                    if (obj != null && obj.optBoolean("result", false)) verbCorrect++;
                }

                // 名词：索引0,1（词汇理解）和索引0,1（词汇表达）
                int nounTotal = 4;
                int nounCorrect = 0;
                if (jsonArrayRE.length() > 0) {
                    JSONObject obj = jsonArrayRE.optJSONObject(0);
                    if (obj != null && obj.optBoolean("result", false)) nounCorrect++;
                }
                if (jsonArrayRE.length() > 1) {
                    JSONObject obj = jsonArrayRE.optJSONObject(1);
                    if (obj != null && obj.optBoolean("result", false)) nounCorrect++;
                }
                if (jsonArrayE.length() > 0) {
                    JSONObject obj = jsonArrayE.optJSONObject(0);
                    if (obj != null && obj.optBoolean("result", false)) nounCorrect++;
                }
                if (jsonArrayE.length() > 1) {
                    JSONObject obj = jsonArrayE.optJSONObject(1);
                    if (obj != null && obj.optBoolean("result", false)) nounCorrect++;
                }

                // 生成与App显示完全一致的评估建议文本
                StringBuilder suggestionText = new StringBuilder();
                suggestionText.append("孩子");

                // 按App显示的顺序：分类名词、形容词、动词
                boolean hasGoodWords = false;
                if (categoryCorrect >= categoryTotal / 2.0) {
                    suggestionText.append("分类名词（名词上位词）");
                    hasGoodWords = true;
                }

                if (adjCorrect >= adjTotal / 2.0) {
                    if (hasGoodWords) suggestionText.append("、");
                    suggestionText.append("形容词");
                    hasGoodWords = true;
                }

                if (verbCorrect >= verbTotal / 2.0) {
                    if (hasGoodWords) suggestionText.append("、");
                    suggestionText.append("动词");
                    hasGoodWords = true;
                }

                if (nounCorrect >= nounTotal / 2.0) {
                    if (hasGoodWords) suggestionText.append("、");
                    suggestionText.append("名词");
                    hasGoodWords = true;
                }

                if (hasGoodWords) {
                    suggestionText.append("掌握较好");
                }

                // 添加"但各测试点掌握得不够好，需要针对性的学习"，与App显示一致
                suggestionText.append("，但各测试点掌握得不够好，需要针对性的学习");

                suggestionText.append("。");

                document.add(new Paragraph(suggestionText.toString(), simsun));


            }

            if (showPrelinguistic) {
                document.add(new Paragraph("二、前语言能力测试模块（模块二）", simsunBold));
                document.add(new Paragraph("场景：" + plSceneLabel, simsun));
                document.add(new Paragraph("得分：" + plTotalScore + "/10", simsun));

                // （一）记录表
                document.add(new Paragraph("（一）记录表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 创建前语言能力测试点表格
                PdfPTable tablePL = new PdfPTable(3);
                tablePL.setWidthPercentage(100);
                tablePL.setSpacingBefore(10f);
                tablePL.setSpacingAfter(15f);

                // 添加表头
                addHeaderCell(tablePL, "序号", simsun, 1);
                addHeaderCell(tablePL, "技能", simsun, 1);
                addHeaderCell(tablePL, "得分", simsun, 1);

                // 加载前语言能力评估数据
                JSONArray plDataArray = null;
                if (evaluations.has("PL_A")) {
                    plDataArray = evaluations.optJSONArray("PL_A");
                } else if (evaluations.has("PL_B")) {
                    plDataArray = evaluations.optJSONArray("PL_B");
                } else if (evaluations.has("PL")) {
                    plDataArray = evaluations.optJSONArray("PL");
                }

                // 技能列表
                String[] plSkills = ImageUrls.PL_SKILLS;
                String[] plPrompts = "B".equals(plSceneLabel) ? ImageUrls.PL_PROMPTS_B : ImageUrls.PL_PROMPTS_A;

                // 添加技能数据行
                for (int i = 0; i < plSkills.length; i++) {
                    int plScore = 0;
                    if (plDataArray != null && i < plDataArray.length()) {
                        JSONObject item = plDataArray.optJSONObject(i);
                        if (item != null) {
                            plScore = item.optInt("score", 0);
                        }
                    }
                    addDataCell(tablePL, String.valueOf(i + 1), simsun, 1);
                    addDataCell(tablePL, plSkills[i], simsun, 1);
                    addDataCell(tablePL, String.valueOf(plScore), simsun, 1);
                }

                // 添加总分行
                PdfPCell totalCell = new PdfPCell(new Phrase("总分", simsunBold));
                totalCell.setColspan(2);
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                totalCell.setFixedHeight(22.5f);
                tablePL.addCell(totalCell);

                PdfPCell totalScoreCell = new PdfPCell(new Phrase(plTotalScore + "/10", simsun));
                totalScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                totalScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                totalScoreCell.setFixedHeight(22.5f);
                tablePL.addCell(totalScoreCell);

                document.add(tablePL);

                // （二）前语言能力评估结果
                document.add(new Paragraph("（二）前语言能力评估结果", simsunBold));
                document.add(new Paragraph(plSummaryText, simsun));

                // （三）评估建议
                document.add(new Paragraph("（三）评估建议", simsunBold));
                // 只显示勾选的选项
                if (plSuggestionOption == 1) {
                    document.add(new Paragraph("● " + plOption1Text, simsun));
                } else if (plSuggestionOption == 2) {
                    document.add(new Paragraph("● " + plOption2Text, simsun));
                }
                document.add(new Paragraph(" ", simsun));
            }

            // 句法能力评估结果
            if (showSyntax) {
                document.add(new Paragraph("四、句法能力评估", simsunBold));
                document.add(new Paragraph("（一）记录表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 创建详细的题目明细表格
                PdfPTable tableSyntax = new PdfPTable(5);
                tableSyntax.setWidthPercentage(100);
                tableSyntax.setSpacingBefore(10f);
                tableSyntax.setSpacingAfter(15f);

                // 添加表头
                addHeaderCell(tableSyntax, "测试组别", simsun, 1);
                addHeaderCell(tableSyntax, "题号", simsun, 1);
                addHeaderCell(tableSyntax, "例句", simsun, 1);
                addHeaderCell(tableSyntax, "测试结果", simsun, 1);
                addHeaderCell(tableSyntax, "对应测试语法点", simsun, 1);

                // 加载RG（句法理解）和SE（句法表达）数据
                // 遍历所有组的数据，与SyntaxResultActivity保持一致
                Map<Integer, Integer> rgCorrectCount = new HashMap<>();
                Map<Integer, Integer> seCorrectCount = new HashMap<>();
                Map<Integer, Integer> rgTotalCount = new HashMap<>();
                Map<Integer, Integer> seTotalCount = new HashMap<>();

                // 初始化计数映射
                for (int i = 1; i <= 6; i++) {
                    rgCorrectCount.put(i, 0);
                    rgTotalCount.put(i, 0);
                }
                for (int i = 1; i <= 6; i++) {
                    seCorrectCount.put(i, 0);
                    seTotalCount.put(i, 0);
                }

                // 收集题目详情
                ArrayList<JSONObject> questionDetails = new ArrayList<>();

                // 根据模块类型决定处理哪些数据
                if ("syntax_comprehension".equals(moduleKey) || "syntax".equals(moduleKey)) {
                    // 处理RG组数据
                    for (int group = 1; group <= 4; group++) {
                        JSONArray jsonArrayRG = evaluations.optJSONArray("RG" + group);
                        if (jsonArrayRG != null) {
                            for (int j = 0; j < jsonArrayRG.length(); j++) {
                                JSONObject item = jsonArrayRG.optJSONObject(j);
                                if (item != null) {
                                    String time = item.optString("time", "");
                                    // 即使没有time字段也处理，因为App显示了所有题目
                                    int questionNum = j + 1; // 使用连续的编号
                                    int testPoint = getRGTestPoint(group, questionNum);
                                    if (testPoint > 0 && testPoint <= 6) {
                                        rgTotalCount.put(testPoint, rgTotalCount.get(testPoint) + 1);
                                        if (item.optBoolean("result", false)) {
                                            rgCorrectCount.put(testPoint, rgCorrectCount.get(testPoint) + 1);
                                        }
                                    }
                                    // 添加题目详情
                                    JSONObject detail = new JSONObject();
                                    try {
                                        detail.put("group", group);
                                        detail.put("num", questionNum);
                                        detail.put("question", item.optString("question", ""));
                                        detail.put("result", item.optBoolean("result", false));
                                        detail.put("testLanguage", getTestLanguage("RG", group, questionNum));
                                        questionDetails.add(detail);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }

                if ("syntax_expression".equals(moduleKey) || "syntax".equals(moduleKey)) {
                    // 处理SE组数据
                    for (int group = 1; group <= 4; group++) {
                        JSONArray jsonArraySE = evaluations.optJSONArray("SE" + group);
                        if (jsonArraySE != null) {
                            for (int j = 0; j < jsonArraySE.length(); j++) {
                                JSONObject item = jsonArraySE.optJSONObject(j);
                                if (item != null) {
                                    String time = item.optString("time", "");
                                    // 即使没有time字段也处理，因为App显示了所有题目
                                    int questionNum = j + 1; // 使用连续的编号
                                    int testPoint = getSETestPoint(group, questionNum);
                                    if (testPoint > 0 && testPoint <= 6) {
                                        seTotalCount.put(testPoint, seTotalCount.get(testPoint) + 1);
                                        if (item.optBoolean("result", false)) {
                                            seCorrectCount.put(testPoint, seCorrectCount.get(testPoint) + 1);
                                        }
                                    }
                                    // 添加题目详情
                                    JSONObject detail = new JSONObject();
                                    try {
                                        detail.put("group", group);
                                        detail.put("num", questionNum);
                                        detail.put("question", item.optString("question", ""));
                                        detail.put("result", item.optBoolean("result", false));
                                        detail.put("testLanguage", getTestLanguage("SE", group, questionNum));
                                        questionDetails.add(detail);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }

                // 计算每个测试点的正确率
                ArrayList<String> weaknessList = new ArrayList<>();
                ArrayList<String> inProgressList = new ArrayList<>();

                // 按测试点名称统计正确数和总数
                Map<String, Integer> testPointCorrectCount = new HashMap<>();
                Map<String, Integer> testPointTotalCount = new HashMap<>();

                // 显示题目详情并统计数据
                for (JSONObject detail : questionDetails) {
                    try {
                        int group = detail.getInt("group");
                        int questionNum = detail.getInt("num");
                        String question = detail.getString("question");
                        boolean result = detail.getBoolean("result");
                        String testLanguage = detail.getString("testLanguage");

                        addDataCell(tableSyntax, String.valueOf(group), simsun, 1);
                        addDataCell(tableSyntax, String.valueOf(questionNum), simsun, 1);
                        addDataCell(tableSyntax, question, simsun, 1);
                        addDataCell(tableSyntax, result ? "正确" : "错误", simsun, 1);
                        addDataCell(tableSyntax, testLanguage, simsun, 1);

                        // 统计测试点数据
                        if (!testLanguage.isEmpty()) {
                            testPointTotalCount.put(testLanguage, testPointTotalCount.getOrDefault(testLanguage, 0) + 1);
                            if (result) {
                                testPointCorrectCount.put(testLanguage, testPointCorrectCount.getOrDefault(testLanguage, 0) + 1);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                document.add(tableSyntax);

                // 计算需要重点关注的能力和不稳定的能力
                // 手动统计篇章理解能力的正确数和总数
                int discourseCorrect = 0;
                int discourseTotal = 0;

                // 遍历所有测试点
                for (String testPoint : testPointTotalCount.keySet()) {
                    int correct = testPointCorrectCount.getOrDefault(testPoint, 0);
                    int total = testPointTotalCount.get(testPoint);

                    // 处理篇章理解能力
                    if (testPoint.equals("篇章理解能力")) {
                        discourseCorrect = correct;
                        discourseTotal = total;
                    } 
                    // 处理其他测试点
                    else {
                        if (correct == 0 && !weaknessList.contains(testPoint)) {
                            weaknessList.add(testPoint);
                        } else if (correct <= total / 3 && !inProgressList.contains(testPoint)) {
                            inProgressList.add(testPoint);
                        }
                    }
                }

                // 特殊处理篇章理解能力（6小问）
                if (discourseTotal > 0) {
                    if (discourseCorrect == 0 && !weaknessList.contains("篇章理解能力")) {
                        weaknessList.add("篇章理解能力");
                    } else if (discourseCorrect <= 2 && !inProgressList.contains("篇章理解能力")) {
                        inProgressList.add("篇章理解能力");
                    }
                }

                // 根据模块类型显示相应的结果
                if ("syntax_comprehension".equals(moduleKey)) {
                    document.add(new Paragraph("（二）句法理解能力结果", simsunBold));
                    document.add(new Paragraph("测试结果：" + scoreRG, simsun));
                    document.add(new Paragraph(" ", simsun));
                } else if ("syntax_expression".equals(moduleKey)) {
                    document.add(new Paragraph("（二）句法表达能力结果", simsunBold));
                    document.add(new Paragraph("测试结果：" + scoreSE, simsun));
                    document.add(new Paragraph(" ", simsun));
                } else {
                    document.add(new Paragraph("（二）句法理解能力结果", simsunBold));
                    document.add(new Paragraph("测试结果：" + scoreRG, simsun));
                    document.add(new Paragraph(" ", simsun));

                    document.add(new Paragraph("（三）句法表达能力结果", simsunBold));
                    document.add(new Paragraph("测试结果：" + scoreSE, simsun));
                    document.add(new Paragraph(" ", simsun));

                    document.add(new Paragraph("（四）句法能力综合评估", simsunBold));
                    document.add(new Paragraph("综合结果：" + scoreSyntax, simsun));
                    document.add(new Paragraph(" ", simsun));
                }

                document.add(new Paragraph("（五）评估建议", simsunBold));
                if ("syntax_comprehension".equals(moduleKey)) {
                    document.add(new Paragraph("通过儿童句法理解能力的评估，本次评估结果如下：", simsun));
                } else if ("syntax_expression".equals(moduleKey)) {
                    document.add(new Paragraph("通过儿童句法表达能力的评估，本次评估结果如下：", simsun));
                } else {
                    document.add(new Paragraph("通过儿童句法理解与表达能力的评估，本次评估结果如下：", simsun));
                }
                document.add(new Paragraph(" ", simsun));

                if (lenthrg + lenthse > 0) {
                    double totalSyntaxScore = 0;
                    String abilityType = "句法理解能力";
                    if ("syntax_comprehension".equals(moduleKey)) {
                        totalSyntaxScore = lenthrg > 0 ? (countrg / lenthrg) * 100 : 0;
                        abilityType = "句法理解能力";
                    } else if ("syntax_expression".equals(moduleKey)) {
                        totalSyntaxScore = lenthse > 0 ? (countse / lenthse) * 100 : 0;
                        abilityType = "句法表达能力";
                    } else {
                        totalSyntaxScore = ((countrg + countse) / (lenthrg + lenthse)) * 100;
                        abilityType = "句法能力";
                    }
                    if (totalSyntaxScore >= 66.7) { // 10/15 ≈ 66.7%
                        document.add(new Paragraph("● 从整体上来说，孩子的" + abilityType + "较好，基本达标，符合该年龄段孩子语言发育水平。", simsun));
                    } else {
                        document.add(new Paragraph("● 从整体上来说，孩子的" + abilityType + "还有待进一步发展，尚未达标。", simsun));
                    }
                    document.add(new Paragraph(" ", simsun));

                    // 显示需要重点关注的能力
                    document.add(new Paragraph("1. 【需要重点关注的能力】", simsun));
                    if (!weaknessList.isEmpty()) {
                        for (String ability : weaknessList) {
                            document.add(new Paragraph("   - " + ability, simsun));
                        }
                    } else {
                        document.add(new Paragraph("   暂无需要重点关注的能力", simsun));
                    }
                    document.add(new Paragraph(" ", simsun));

                    // 显示不稳定的能力
                    document.add(new Paragraph("2. 【不稳定的能力】", simsun));
                    if (!inProgressList.isEmpty()) {
                        for (String ability : inProgressList) {
                            document.add(new Paragraph("   - " + ability, simsun));
                        }
                    } else {
                        document.add(new Paragraph("   暂无不稳定的能力", simsun));
                    }
                    document.add(new Paragraph(" ", simsun));
                }


            }

            // 社交能力评估结果
            if (showSocial) {
                document.add(new Paragraph("五、社交能力评估", simsunBold));
                document.add(new Paragraph("（一）记录表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 读取社交能力评估数据
                JSONArray socialArray = evaluations.optJSONArray("SOCIAL");
                // 同时读取SOCIAL1到SOCIAL6数组
                JSONArray socialArray1 = evaluations.optJSONArray("SOCIAL1");
                JSONArray socialArray2 = evaluations.optJSONArray("SOCIAL2");
                JSONArray socialArray3 = evaluations.optJSONArray("SOCIAL3");
                JSONArray socialArray4 = evaluations.optJSONArray("SOCIAL4");
                JSONArray socialArray5 = evaluations.optJSONArray("SOCIAL5");
                JSONArray socialArray6 = evaluations.optJSONArray("SOCIAL6");

                // 检查是否有任何社交能力数据
                boolean hasSocialData = (socialArray != null && socialArray.length() > 0) ||
                        (socialArray1 != null && socialArray1.length() > 0) ||
                        (socialArray2 != null && socialArray2.length() > 0) ||
                        (socialArray3 != null && socialArray3.length() > 0) ||
                        (socialArray4 != null && socialArray4.length() > 0) ||
                        (socialArray5 != null && socialArray5.length() > 0) ||
                        (socialArray6 != null && socialArray6.length() > 0);

                if (hasSocialData) {
                    // 按组分类统计结果
                    Map<Integer, ArrayList<JSONObject>> groupSocialData = new HashMap<>();
                    ArrayList<String> weaknessList = new ArrayList<>();
                    ArrayList<String> inProgressList = new ArrayList<>();
                    int socialTotalScore = 0;
                    int socialCompletedQuestions = 0;

                    // 加载所有组的数据，但只显示已完成的组的题目
                    for (int group = 1; group <= 6; group++) {
                        ArrayList<JSONObject> groupDetails = new ArrayList<>();
                        boolean hasCompletedQuestions = false;

                        // 根据组号选择对应的数组
                        JSONArray currentArray = null;
                        switch (group) {
                            case 1:
                                currentArray = socialArray1;
                                break;
                            case 2:
                                currentArray = socialArray2;
                                break;
                            case 3:
                                currentArray = socialArray3;
                                break;
                            case 4:
                                currentArray = socialArray4;
                                break;
                            case 5:
                                currentArray = socialArray5;
                                break;
                            case 6:
                                currentArray = socialArray6;
                                break;
                            default:
                                currentArray = socialArray;
                        }

                        // 如果当前数组为空，尝试使用SOCIAL数组
                        if (currentArray == null || currentArray.length() == 0) {
                            currentArray = socialArray;
                        }

                        if (currentArray != null) {
                            for (int i = 0; i < currentArray.length(); i++) {
                                JSONObject object = currentArray.optJSONObject(i);
                                if (object != null && object.has("score") && !object.isNull("score")) {
                                    hasCompletedQuestions = true;
                                    socialCompletedQuestions++;
                                    socialTotalScore += object.optInt("score", 0);

                                    String focus = object.optString("focus", "");
                                    int socialScore = object.optInt("score", 0);
                                    if (socialScore == 0 && !weaknessList.contains(focus)) {
                                        weaknessList.add(focus);
                                    } else if (socialScore == 1 && !inProgressList.contains(focus)) {
                                        inProgressList.add(focus);
                                    }

                                    groupDetails.add(object);
                                }
                            }
                        }

                        if (hasCompletedQuestions) {
                            groupSocialData.put(group, groupDetails);
                        }
                    }

                    // 创建社交能力测试点表格
                    PdfPTable tableSocial = new PdfPTable(4);
                    tableSocial.setWidthPercentage(100);
                    tableSocial.setSpacingBefore(10f);
                    tableSocial.setSpacingAfter(15f);

                    // 设置列宽比例
                    float[] socialColumnWidths = {1f, 2f, 3f, 1f};
                    tableSocial.setWidths(socialColumnWidths);

                    // 添加表头
                    addHeaderCell(tableSocial, "序号", simsun, 1);
                    addHeaderCell(tableSocial, "考查点", simsun, 1);
                    addHeaderCell(tableSocial, "题目内容", simsun, 1);
                    addHeaderCell(tableSocial, "得分", simsun, 1);

                    // 按组号从小到大显示
                    for (int group = 1; group <= 6; group++) {
                        ArrayList<JSONObject> groupDetails = groupSocialData.get(group);
                        if (groupDetails != null && !groupDetails.isEmpty()) {
                            // 添加分组标题
                            PdfPCell groupHeaderCell = new PdfPCell(new Phrase("第" + group + "组", simsunBold));
                            groupHeaderCell.setColspan(4);
                            groupHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            groupHeaderCell.setBackgroundColor(LIGHT_GRAY);
                            groupHeaderCell.setFixedHeight(22.5f);
                            tableSocial.addCell(groupHeaderCell);

                            // 添加题目数据行
                            for (JSONObject object : groupDetails) {
                                if (object == null) {
                                    continue;
                                }
                                int socialNum = object.optInt("num", 0);
                                String ability = object.optString("ability", "");
                                String focus = object.optString("focus", "");
                                String content = object.optString("content", "");
                                int socialScore = object.optInt("score", 0);

                                // 题目内容，在选择0或者1时在题目内容后面加个括号填进考查点
                                StringBuilder contentBuilder = new StringBuilder(content);
                                if (socialScore < 2 && !focus.isEmpty()) {
                                    contentBuilder.append(" (").append(focus).append(")");
                                }

                                addDataCell(tableSocial, String.valueOf(socialNum), simsun, 1);
                                addDataCell(tableSocial, ability, simsun, 1);
                                addDataCell(tableSocial, contentBuilder.toString(), simsun, 1);
                                addDataCell(tableSocial, String.valueOf(socialScore), simsun, 1);
                            }
                        }
                    }
                    document.add(tableSocial);

                    document.add(new Paragraph("（二）评估结果", simsunBold));
                    document.add(new Paragraph(" ", simsun));

                    // 计算总体正确率
                    double accuracy = 0;
                    if (socialCompletedQuestions > 0) {
                        accuracy = (double) socialTotalScore / (socialCompletedQuestions * 2);
                    }

                    // 生成总体评估
                    String overallEvaluation;
                    if (accuracy >= 0.6) {
                        overallEvaluation = "● 从整体上来说，孩子的社交能力较好，基本达标。";
                    } else {
                        overallEvaluation = "● 从整体上来说，孩子的社交能力还有待进一步发展，尚未达标。";
                    }

                    document.add(new Paragraph("通过对儿童社交能力的评估（家长试卷）结果如下：", simsun));
                    document.add(new Paragraph(" ", simsun));
                    document.add(new Paragraph(overallEvaluation, simsun));
                    document.add(new Paragraph(" ", simsun));
                    document.add(new Paragraph("总分：" + socialTotalScore + "/" + (socialCompletedQuestions * 2), simsun));
                    document.add(new Paragraph(" ", simsun));

                    document.add(new Paragraph("（三）需要重点关注的能力", simsunBold));
                    if (!weaknessList.isEmpty()) {
                        for (int i = 0; i < weaknessList.size(); i++) {
                            document.add(new Paragraph((i + 1) + ". " + weaknessList.get(i), simsun));
                        }
                    } else {
                        document.add(new Paragraph("暂无需要重点关注的能力", simsun));
                    }
                    document.add(new Paragraph(" ", simsun));

                    document.add(new Paragraph("（四）需要多发展的能力", simsunBold));
                    if (!inProgressList.isEmpty()) {
                        for (int i = 0; i < inProgressList.size(); i++) {
                            document.add(new Paragraph((i + 1) + ". " + inProgressList.get(i), simsun));
                        }
                    } else {
                        document.add(new Paragraph("暂无需要多发展的能力", simsun));
                    }
                    document.add(new Paragraph(" ", simsun));
                }
            }

            if (showArticulation) {
                document.add(new Paragraph("一、构音评估", simsunBold));
                document.add(new Paragraph("（一）记录表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 创建构音评估表格，与界面显示一致
                PdfPTable tableArticulation = new PdfPTable(12);
                tableArticulation.setWidthPercentage(100);
                tableArticulation.setSpacingBefore(10f);
                tableArticulation.setSpacingAfter(15f);

                // 设置表格列宽，确保所有字段都能完整显示
                float[] columnWidths = {0.5f, 1f, 1f, 0.8f, 1f, 0.8f, 0.8f, 0.8f, 1f, 1f, 1f, 0.8f};
                tableArticulation.setWidths(columnWidths);

                // 添加表头
                addHeaderCell(tableArticulation, "序号", simsun, 1);
                addHeaderCell(tableArticulation, "词汇", simsun, 1);
                addHeaderCell(tableArticulation, "拼音", simsun, 1);
                addHeaderCell(tableArticulation, "声母", simsun, 1);
                addHeaderCell(tableArticulation, "韵母", simsun, 1);
                addHeaderCell(tableArticulation, "韵头", simsun, 1);
                addHeaderCell(tableArticulation, "韵腹", simsun, 1);
                addHeaderCell(tableArticulation, "韵尾", simsun, 1);
                addHeaderCell(tableArticulation, "错误类型", simsun, 1);
                addHeaderCell(tableArticulation, "音系历程", simsun, 1);
                addHeaderCell(tableArticulation, "是否可诱发", simsun, 1);
                addHeaderCell(tableArticulation, "录音", simsun, 1);

                // 加载构音评估数据
                JSONArray aArrayData = evaluations.optJSONArray("A");
                if (aArrayData != null && aArrayData.length() > 0) {
                    // 按原始顺序添加数据行，使用词条序号
                    for (int i = 0; i < aArrayData.length(); i++) {
                        JSONObject object = aArrayData.optJSONObject(i);
                        if (object == null) continue;

                        // 提取数据
                        String word = object.optString("target", "");
                        String pinyin = object.optString("targetPinyin", "");
                        // 如果targetPinyin为空，尝试从ImageUrls获取拼音
                        if (pinyin.isEmpty() && !word.isEmpty()) {
                            pinyin = utils.ImageUrls.getAPinyin(word);
                        }

                        // 从targetWord中提取声母、韵母等信息
                        JSONArray targetWord = object.optJSONArray("targetWord");
                        String initial = "", vowel = "", vowelHead = "", vowelBody = "", vowelTail = "";
                        if (targetWord != null && targetWord.length() > 0) {
                            JSONObject charPhonology = targetWord.optJSONObject(0);
                            if (charPhonology != null) {
                                JSONObject phonology = charPhonology.optJSONObject("phonology");
                                if (phonology != null) {
                                    initial = phonology.optString("initial", "");
                                    vowelHead = phonology.optString("medial", ""); // 韵头
                                    vowelBody = phonology.optString("nucleus", ""); // 韵腹
                                    vowelTail = phonology.optString("coda", ""); // 韵尾
                                    // 构建完整韵母
                                    vowel = vowelHead + vowelBody + vowelTail;
                                }
                            }
                        }

                        // 提取错误类型、音系历程和是否可诱发
                        String errorType = object.optString("errorType", "");
                        if (errorType.isEmpty()) {
                            errorType = object.optString("phonologicalError", "");
                        }
                        String phonologyProcess = object.optString("phonologyProcess", "");
                        if (phonologyProcess.isEmpty()) {
                            phonologyProcess = object.optString("phonologicalProcess", "");
                        }
                        String canElicit = object.optString("canElicit", "");
                        if (canElicit.isEmpty()) {
                            // 从answerPhonology中提取isInducible字段，这是用户实际选择的可诱发状态
                            JSONArray answerPhonology = object.optJSONArray("answerPhonology");
                            if (answerPhonology != null && answerPhonology.length() > 0) {
                                for (int j = 0; j < answerPhonology.length(); j++) {
                                    JSONObject charPhonology = answerPhonology.optJSONObject(j);
                                    if (charPhonology != null) {
                                        JSONObject phonology = charPhonology.optJSONObject("phonology");
                                        if (phonology != null) {
                                            boolean isInducible = phonology.optBoolean("isInducible", false);
                                            if (isInducible) {
                                                canElicit = "可诱发";
                                                break; // 只要有一个字符可诱发，就显示可诱发
                                            }
                                        }
                                    }
                                }
                                // 如果没有找到可诱发的，显示不可诱发
                                if (canElicit.isEmpty()) {
                                    canElicit = "不可诱发";
                                }
                            } else {
                                // 如果answerPhonology为空，尝试从targetWord中提取
                                if (targetWord != null && targetWord.length() > 0) {
                                    for (int j = 0; j < targetWord.length(); j++) {
                                        JSONObject charPhonology = targetWord.optJSONObject(j);
                                        if (charPhonology != null) {
                                            JSONObject phonology = charPhonology.optJSONObject("phonology");
                                            if (phonology != null) {
                                                boolean isInducible = phonology.optBoolean("isInducible", false);
                                                if (isInducible) {
                                                    canElicit = "可诱发";
                                                    break; // 只要有一个字符可诱发，就显示可诱发
                                                }
                                            }
                                        }
                                    }
                                    // 如果没有找到可诱发的，显示不可诱发
                                    if (canElicit.isEmpty()) {
                                        canElicit = "不可诱发";
                                    }
                                } else {
                                    canElicit = "不可诱发";
                                }
                            }
                        }
                        String audioPath = object.optString("audioPath", "");

                        // 添加数据行，使用数组索引+1作为词条序号
                        addDataCell(tableArticulation, String.valueOf(i + 1), simsun, 1);
                        addDataCell(tableArticulation, word, simsun, 1);
                        addDataCell(tableArticulation, pinyin, simsun, 1);
                        addDataCell(tableArticulation, initial, simsun, 1);
                        addDataCell(tableArticulation, vowel, simsun, 1);
                        addDataCell(tableArticulation, vowelHead, simsun, 1);
                        addDataCell(tableArticulation, vowelBody, simsun, 1);
                        addDataCell(tableArticulation, vowelTail, simsun, 1);
                        addDataCell(tableArticulation, errorType, simsun, 1);
                        addDataCell(tableArticulation, phonologyProcess, simsun, 1);
                        addDataCell(tableArticulation, canElicit, simsun, 1);
                        addDataCell(tableArticulation, audioPath.isEmpty() ? "无" : "有", simsun, 1);
                    }
                } else {
                    // 无数据时显示空行
                    addDataCell(tableArticulation, "", simsun, 12);
                }

                document.add(tableArticulation);

                // （二）评估结果
                document.add(new Paragraph("（二）评估结果", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 计算声母正确率
                int totalInitial = 0, correctInitial = 0;
                JSONArray aArray = evaluations.optJSONArray("A");
                if (aArray != null) {
                    for (int i = 0; i < aArray.length(); i++) {
                        JSONObject item = aArray.optJSONObject(i);
                        if (item == null) continue;
                        JSONArray targets = item.optJSONArray("targetWord");
                        JSONArray answers = item.optJSONArray("answerPhonology");
                        if (targets != null) {
                            for (int idx = 0; idx < targets.length(); idx++) {
                                JSONObject target = targets.optJSONObject(idx);
                                String targetInitial = "";
                                if (target != null) {
                                    JSONObject phonology = target.optJSONObject("phonology");
                                    if (phonology != null) {
                                        targetInitial = phonology.optString("initial", "");
                                    }
                                }
                                if (!targetInitial.isEmpty()) {
                                    totalInitial++;
                                    JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                                    String answerInitial = "";
                                    if (answer != null) {
                                        JSONObject phonology = answer.optJSONObject("phonology");
                                        if (phonology != null) {
                                            answerInitial = phonology.optString("initial", "");
                                        }
                                    }
                                    if (targetInitial.equals(answerInitial)) {
                                        correctInitial++;
                                    }
                                }
                            }
                        }
                    }
                }

                double initialRate = totalInitial > 0 ? (correctInitial * 100.0 / totalInitial) : 0;
                String speechClarity = "";
                if (initialRate >= 85) {
                    speechClarity = "轻度";
                } else if (initialRate >= 65) {
                    speechClarity = "轻中度";
                } else if (initialRate >= 50) {
                    speechClarity = "中重度";
                } else {
                    speechClarity = "重度";
                }

                document.add(new Paragraph("声母正确率：" + String.format("%.2f%%", initialRate), simsun));
                document.add(new Paragraph("语音清晰度等级：" + speechClarity, simsun));
                document.add(new Paragraph(" ", simsun));

                // 1. 声母正确率统计表
                document.add(new Paragraph("1. 声母正确率统计表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 创建声母统计表
                PdfPTable initialTable = new PdfPTable(2);
                initialTable.setWidthPercentage(100);
                initialTable.setSpacingBefore(10f);
                initialTable.setSpacingAfter(15f);

                // 声母列表
                String[] initials = {"b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h", "j", "q", "x", "zh", "ch", "sh", "r", "z", "c", "s"};

                // 计算每个声母的正确率
                for (String initial : initials) {
                    int count = 0, correct = 0;
                    if (aArray != null) {
                        for (int i = 0; i < aArray.length(); i++) {
                            JSONObject item = aArray.optJSONObject(i);
                            if (item == null) continue;
                            JSONArray targets = item.optJSONArray("targetWord");
                            JSONArray answers = item.optJSONArray("answerPhonology");
                            if (targets != null) {
                                for (int idx = 0; idx < targets.length(); idx++) {
                                    JSONObject target = targets.optJSONObject(idx);
                                    if (target != null) {
                                        JSONObject phonology = target.optJSONObject("phonology");
                                        if (phonology != null && initial.equals(phonology.optString("initial", ""))) {
                                            count++;
                                            JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                                            if (answer != null) {
                                                JSONObject answerPhonology = answer.optJSONObject("phonology");
                                                if (answerPhonology != null && initial.equals(answerPhonology.optString("initial", ""))) {
                                                    correct++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String rate = count > 0 ? (correct + "/" + count) : "0/0";
                    addDataCell(initialTable, initial, simsun, 1);
                    addDataCell(initialTable, rate, simsun, 1);
                }

                document.add(initialTable);
                document.add(new Paragraph(" ", simsun));

                // 2. 韵母正确率统计表
                document.add(new Paragraph("2. 韵母正确率统计表", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 单韵母
                document.add(new Paragraph("单韵母", simsunBold));
                PdfPTable singleVowelTable = new PdfPTable(2);
                singleVowelTable.setWidthPercentage(100);
                singleVowelTable.setSpacingBefore(10f);
                singleVowelTable.setSpacingAfter(15f);

                String[] singleVowels = {"a", "o", "e", "i", "u", "ü", "er"};
                for (String vowel : singleVowels) {
                    int count = 0, correct = 0;
                    if (aArray != null) {
                        for (int i = 0; i < aArray.length(); i++) {
                            JSONObject item = aArray.optJSONObject(i);
                            if (item == null) continue;
                            JSONArray targets = item.optJSONArray("targetWord");
                            JSONArray answers = item.optJSONArray("answerPhonology");
                            if (targets != null) {
                                for (int idx = 0; idx < targets.length(); idx++) {
                                    JSONObject target = targets.optJSONObject(idx);
                                    if (target != null) {
                                        JSONObject phonology = target.optJSONObject("phonology");
                                        if (phonology != null) {
                                            String medial = phonology.optString("medial", "");
                                            String nucleus = phonology.optString("nucleus", "");
                                            String coda = phonology.optString("coda", "");
                                            String targetVowel = medial + nucleus + coda;
                                            if (vowel.equals(targetVowel)) {
                                                count++;
                                                JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                                                if (answer != null) {
                                                    JSONObject answerPhonology = answer.optJSONObject("phonology");
                                                    if (answerPhonology != null) {
                                                        String answerMedial = answerPhonology.optString("medial", "");
                                                        String answerNucleus = answerPhonology.optString("nucleus", "");
                                                        String answerCoda = answerPhonology.optString("coda", "");
                                                        String answerVowel = answerMedial + answerNucleus + answerCoda;
                                                        if (vowel.equals(answerVowel)) {
                                                            correct++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String rate = count > 0 ? (correct + "/" + count) : "0/0";
                    addDataCell(singleVowelTable, vowel, simsun, 1);
                    addDataCell(singleVowelTable, rate, simsun, 1);
                }

                document.add(singleVowelTable);
                document.add(new Paragraph(" ", simsun));

                // 复合韵母
                document.add(new Paragraph("复合韵母", simsunBold));
                PdfPTable compoundVowelTable = new PdfPTable(2);
                compoundVowelTable.setWidthPercentage(100);
                compoundVowelTable.setSpacingBefore(10f);
                compoundVowelTable.setSpacingAfter(15f);

                String[] compoundVowels = {"ai", "ei", "ui", "ao", "ou", "iu", "ie", "üe", "er", "an", "en", "in", "un", "ün", "ang", "eng", "ing", "ong"};
                for (String vowel : compoundVowels) {
                    int count = 0, correct = 0;
                    if (aArray != null) {
                        for (int i = 0; i < aArray.length(); i++) {
                            JSONObject item = aArray.optJSONObject(i);
                            if (item == null) continue;
                            JSONArray targets = item.optJSONArray("targetWord");
                            JSONArray answers = item.optJSONArray("answerPhonology");
                            if (targets != null) {
                                for (int idx = 0; idx < targets.length(); idx++) {
                                    JSONObject target = targets.optJSONObject(idx);
                                    if (target != null) {
                                        JSONObject phonology = target.optJSONObject("phonology");
                                        if (phonology != null) {
                                            String medial = phonology.optString("medial", "");
                                            String nucleus = phonology.optString("nucleus", "");
                                            String coda = phonology.optString("coda", "");
                                            String targetVowel = medial + nucleus + coda;
                                            if (vowel.equals(targetVowel)) {
                                                count++;
                                                JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                                                if (answer != null) {
                                                    JSONObject answerPhonology = answer.optJSONObject("phonology");
                                                    if (answerPhonology != null) {
                                                        String answerMedial = answerPhonology.optString("medial", "");
                                                        String answerNucleus = answerPhonology.optString("nucleus", "");
                                                        String answerCoda = answerPhonology.optString("coda", "");
                                                        String answerVowel = answerMedial + answerNucleus + answerCoda;
                                                        if (vowel.equals(answerVowel)) {
                                                            correct++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String rate = count > 0 ? (correct + "/" + count) : "0/0";
                    addDataCell(compoundVowelTable, vowel, simsun, 1);
                    addDataCell(compoundVowelTable, rate, simsun, 1);
                }

                document.add(compoundVowelTable);
                document.add(new Paragraph(" ", simsun));

                // 鼻音韵母
                document.add(new Paragraph("鼻音韵母", simsunBold));
                PdfPTable nasalVowelTable = new PdfPTable(2);
                nasalVowelTable.setWidthPercentage(100);
                nasalVowelTable.setSpacingBefore(10f);
                nasalVowelTable.setSpacingAfter(15f);

                String[] nasalVowels = {"an", "en", "in", "un", "ün", "ang", "eng", "ing", "ong", "ian", "uan", "üan", "iang", "uang"};
                for (String vowel : nasalVowels) {
                    int count = 0, correct = 0;
                    if (aArray != null) {
                        for (int i = 0; i < aArray.length(); i++) {
                            JSONObject item = aArray.optJSONObject(i);
                            if (item == null) continue;
                            JSONArray targets = item.optJSONArray("targetWord");
                            JSONArray answers = item.optJSONArray("answerPhonology");
                            if (targets != null) {
                                for (int idx = 0; idx < targets.length(); idx++) {
                                    JSONObject target = targets.optJSONObject(idx);
                                    if (target != null) {
                                        JSONObject phonology = target.optJSONObject("phonology");
                                        if (phonology != null) {
                                            String medial = phonology.optString("medial", "");
                                            String nucleus = phonology.optString("nucleus", "");
                                            String coda = phonology.optString("coda", "");
                                            String targetVowel = medial + nucleus + coda;
                                            if (vowel.equals(targetVowel)) {
                                                count++;
                                                JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                                                if (answer != null) {
                                                    JSONObject answerPhonology = answer.optJSONObject("phonology");
                                                    if (answerPhonology != null) {
                                                        String answerMedial = answerPhonology.optString("medial", "");
                                                        String answerNucleus = answerPhonology.optString("nucleus", "");
                                                        String answerCoda = answerPhonology.optString("coda", "");
                                                        String answerVowel = answerMedial + answerNucleus + answerCoda;
                                                        if (vowel.equals(answerVowel)) {
                                                            correct++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String rate = count > 0 ? (correct + "/" + count) : "0/0";
                    addDataCell(nasalVowelTable, vowel, simsun, 1);
                    addDataCell(nasalVowelTable, rate, simsun, 1);
                }

                document.add(nasalVowelTable);
                document.add(new Paragraph(" ", simsun));

                // 3. 错误韵母
                document.add(new Paragraph("3. 错误韵母", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 收集错误韵母
                StringBuilder errorVowels = new StringBuilder();
                if (aArray != null) {
                    for (int i = 0; i < aArray.length(); i++) {
                        JSONObject item = aArray.optJSONObject(i);
                        if (item == null) continue;
                        JSONArray targets = item.optJSONArray("targetWord");
                        JSONArray answers = item.optJSONArray("answerPhonology");
                        if (targets != null) {
                            for (int idx = 0; idx < targets.length(); idx++) {
                                JSONObject target = targets.optJSONObject(idx);
                                if (target != null) {
                                    JSONObject phonology = target.optJSONObject("phonology");
                                    if (phonology != null) {
                                        String medial = phonology.optString("medial", "");
                                        String nucleus = phonology.optString("nucleus", "");
                                        String coda = phonology.optString("coda", "");
                                        String targetVowel = medial + nucleus + coda;
                                        if (!targetVowel.isEmpty()) {
                                            JSONObject answer = answers != null ? answers.optJSONObject(idx) : null;
                                            if (answer != null) {
                                                JSONObject answerPhonology = answer.optJSONObject("phonology");
                                                if (answerPhonology != null) {
                                                    String answerMedial = answerPhonology.optString("medial", "");
                                                    String answerNucleus = answerPhonology.optString("nucleus", "");
                                                    String answerCoda = answerPhonology.optString("coda", "");
                                                    String answerVowel = answerMedial + answerNucleus + answerCoda;
                                                    if (!targetVowel.equals(answerVowel)) {
                                                        if (errorVowels.length() > 0) {
                                                            errorVowels.append(", ");
                                                        }
                                                        errorVowels.append(targetVowel);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (errorVowels.length() > 0) {
                    document.add(new Paragraph("错误韵母：" + errorVowels.toString(), simsun));
                } else {
                    document.add(new Paragraph("错误韵母：无", simsun));
                }
                document.add(new Paragraph(" ", simsun));

                // 4. 诊断（直接勾选）- 显示保存的诊断文本
                document.add(new Paragraph("4. 诊断（直接勾选）", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 加载保存的诊断勾选状态
                String savedDiagnosis = safeText(evaluations.optString("clinical_diagnosis", ""));

                if (!savedDiagnosis.isEmpty()) {
                    document.add(new Paragraph(savedDiagnosis, simsun));
                } else {
                    document.add(new Paragraph("未选择", simsun));
                }
                document.add(new Paragraph(" ", simsun));

                // 5. 语言清晰度等级
                document.add(new Paragraph("5. 语言清晰度等级：" + speechClarity, simsunBold));
                document.add(new Paragraph("声母正确率：" + String.format("%.2f%%", initialRate), simsun));
                document.add(new Paragraph(" ", simsun));

                // 6. 干预建议 - 显示保存的干预建议文本
                document.add(new Paragraph("6. 干预建议", simsunBold));
                document.add(new Paragraph(" ", simsun));

                // 加载保存的干预建议状态
                String savedSuggestions = safeText(evaluations.optString("assessment_suggestions", ""));

                if (!savedSuggestions.isEmpty()) {
                    document.add(new Paragraph(savedSuggestions, simsun));
                } else {
                    document.add(new Paragraph("未选择", simsun));
                }
                document.add(new Paragraph(" ", simsun));
            }

            document.close();


            Toast.makeText(context, "文件已保存", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (document.isOpen()) {
                    document.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void generateEvaluationPdf(OutputStream outputStream, String fname, String moduleType) {
        try {
            JSONObject data = dataManager.getInstance().loadData(fname);
            JSONObject filtered = filterEvaluationsForModule(data, moduleType);
            evaluationDataOverride.set(filtered);
            evaluationModuleOverride.set(moduleType);
            generatePdf(outputStream, fname);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            evaluationDataOverride.remove();
            evaluationModuleOverride.remove();
        }
    }

    private static String normalizeModuleKey(String moduleType) {
        if (moduleType == null) {
            return "";
        }
        return moduleType.trim().toLowerCase();
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static JSONObject filterEvaluationsForModule(JSONObject source, String moduleType) throws JSONException {
        JSONObject output = source == null ? new JSONObject() : new JSONObject(source.toString());
        JSONObject evaluations = output.optJSONObject("evaluations");
        if (evaluations == null) {
            evaluations = new JSONObject();
            output.put("evaluations", evaluations);
        }
        String moduleKey = moduleType == null ? "" : moduleType.trim();
        if (moduleKey.isEmpty()) {
            return output;
        }
        JSONObject filtered = new JSONObject();
        // 复制全局字段，包括诊断和干预建议
        filtered.put("clinical_diagnosis", evaluations.optString("clinical_diagnosis", ""));
        filtered.put("assessment_suggestions", evaluations.optString("assessment_suggestions", ""));
        filtered.put("speech_intelligibility", evaluations.optString("speech_intelligibility", ""));
        filtered.put("initial_accuracy", evaluations.optString("initial_accuracy", ""));

        switch (moduleKey) {
            case "articulation":
                filtered.put("A", evaluations.optJSONArray("A"));
                break;
            case "prelinguistic":
                filtered.put("PL", evaluations.optJSONArray("PL"));
                break;
            case "vocabulary":
                filtered.put("E", evaluations.optJSONArray("E"));
                filtered.put("EV", evaluations.optJSONArray("EV"));
                filtered.put("RE", evaluations.optJSONArray("RE"));
                filtered.put("S", evaluations.optJSONArray("S"));
                filtered.put("NWR", evaluations.optJSONArray("NWR"));
                break;
            case "syntax":
            case "syntax_comprehension":
            case "syntax_expression":
                if ("syntax_comprehension".equals(moduleKey)) {
                    // 只复制RG相关数据
                    filtered.put("RG", evaluations.optJSONArray("RG"));
                    filtered.put("RG1", evaluations.optJSONArray("RG1"));
                    filtered.put("RG2", evaluations.optJSONArray("RG2"));
                    filtered.put("RG3", evaluations.optJSONArray("RG3"));
                    filtered.put("RG4", evaluations.optJSONArray("RG4"));
                } else if ("syntax_expression".equals(moduleKey)) {
                    // 只复制SE相关数据
                    filtered.put("SE", evaluations.optJSONArray("SE"));
                    filtered.put("SE1", evaluations.optJSONArray("SE1"));
                    filtered.put("SE2", evaluations.optJSONArray("SE2"));
                    filtered.put("SE3", evaluations.optJSONArray("SE3"));
                    filtered.put("SE4", evaluations.optJSONArray("SE4"));
                } else {
                    // 复制所有句法相关数据
                    filtered.put("RG", evaluations.optJSONArray("RG"));
                    filtered.put("RG1", evaluations.optJSONArray("RG1"));
                    filtered.put("RG2", evaluations.optJSONArray("RG2"));
                    filtered.put("RG3", evaluations.optJSONArray("RG3"));
                    filtered.put("RG4", evaluations.optJSONArray("RG4"));
                    filtered.put("SE", evaluations.optJSONArray("SE"));
                    filtered.put("SE1", evaluations.optJSONArray("SE1"));
                    filtered.put("SE2", evaluations.optJSONArray("SE2"));
                    filtered.put("SE3", evaluations.optJSONArray("SE3"));
                    filtered.put("SE4", evaluations.optJSONArray("SE4"));
                }
                break;
            case "social":
                filtered.put("SOCIAL", evaluations.optJSONArray("SOCIAL"));
                filtered.put("SOCIAL1", evaluations.optJSONArray("SOCIAL1"));
                filtered.put("SOCIAL2", evaluations.optJSONArray("SOCIAL2"));
                filtered.put("SOCIAL3", evaluations.optJSONArray("SOCIAL3"));
                filtered.put("SOCIAL4", evaluations.optJSONArray("SOCIAL4"));
                filtered.put("SOCIAL5", evaluations.optJSONArray("SOCIAL5"));
                filtered.put("SOCIAL6", evaluations.optJSONArray("SOCIAL6"));
                break;
            default:
                return output;
        }
        ensureEvalArray(filtered, "A");
        ensureEvalArray(filtered, "E");
        ensureEvalArray(filtered, "EV");
        ensureEvalArray(filtered, "RE");
        ensureEvalArray(filtered, "RG");
        ensureEvalArray(filtered, "S");
        ensureEvalArray(filtered, "NWR");
        ensureEvalArray(filtered, "PST");
        ensureEvalArray(filtered, "PN");
        ensureEvalArray(filtered, "PL");
        ensureEvalArray(filtered, "SE1");
        ensureEvalArray(filtered, "SE2");
        ensureEvalArray(filtered, "SE3");
        ensureEvalArray(filtered, "SE4");
        ensureEvalArray(filtered, "RG1");
        ensureEvalArray(filtered, "RG2");
        ensureEvalArray(filtered, "RG3");
        ensureEvalArray(filtered, "RG4");
        ensureEvalArray(filtered, "SOCIAL");
        ensureEvalArray(filtered, "SOCIAL1");
        ensureEvalArray(filtered, "SOCIAL2");
        ensureEvalArray(filtered, "SOCIAL3");
        ensureEvalArray(filtered, "SOCIAL4");
        ensureEvalArray(filtered, "SOCIAL5");
        ensureEvalArray(filtered, "SOCIAL6");
        output.put("evaluations", filtered);
        return output;
    }

    private static void ensureEvalArray(JSONObject target, String key) throws JSONException {
        if (target == null || key == null) {
            return;
        }
        if (!target.has(key) || target.isNull(key)) {
            target.put(key, new JSONArray());
        }
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font, int col) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setColspan(col);
        cell.setFixedHeight(22.5f);
        table.addCell(cell);
    }

    private static void addHeaderCell2(PdfPTable table, String text, Font font, int col, BaseColor color, float fixedheight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(color);
        cell.setColspan(col);
        cell.setFixedHeight(fixedheight * 1.5f);
        table.addCell(cell);
    }

    private static void addDataCell(PdfPTable table, String text, Font font, int col) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(col);
        cell.setFixedHeight(22.5f);
        table.addCell(cell);
    }

    private static void addDataCell2(PdfPTable table, String text, Font font, int col, float fixedheight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(col);
        cell.setFixedHeight(fixedheight * 1.5f);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, PdfPTable nested, int col, float fixedheight) {
        PdfPCell cell = new PdfPCell(nested);
        cell.setPadding(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(col);
        cell.setFixedHeight(fixedheight * 1.5f);
        table.addCell(cell);
    }

    private static void addTreatmentPlanSection(Document document, Font titleFont, Font bodyFont, JSONObject plan) throws Exception {
        document.add(new Paragraph(getStringRes(R.string.pdf_treatment_plan_title), titleFont));
        document.add(new Paragraph(" ", bodyFont));

        JSONObject caseSummary = plan.optJSONObject("case_summary");
        if (caseSummary != null) {
            document.add(new Paragraph(getStringRes(R.string.pdf_section_case_summary), titleFont));
            addKeyValue(document, getStringRes(R.string.pdf_label_chief_complaint), caseSummary.optString("chief_complaint", ""), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_key_findings), caseSummary.optJSONArray("key_findings"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_suspected_diagnosis), caseSummary.optJSONArray("suspected_diagnosis"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_risk_flags), caseSummary.optJSONArray("risk_flags"), bodyFont);
            document.add(new Paragraph(" ", bodyFont));
        }

        JSONObject overall = plan.optJSONObject("overall_goals");
        if (overall != null) {
            boolean hasOverall = hasItems(overall.optJSONArray("within_4_weeks"))
                    || hasItems(overall.optJSONArray("within_12_weeks"))
                    || hasItems(overall.optJSONArray("within_24_weeks"));
            if (hasOverall) {
                document.add(new Paragraph(getStringRes(R.string.pdf_section_overall_goals), titleFont));
                addList(document, getStringRes(R.string.pdf_label_within_4_weeks), overall.optJSONArray("within_4_weeks"), bodyFont);
                addList(document, getStringRes(R.string.pdf_label_within_12_weeks), overall.optJSONArray("within_12_weeks"), bodyFont);
                addList(document, getStringRes(R.string.pdf_label_within_24_weeks), overall.optJSONArray("within_24_weeks"), bodyFont);
                document.add(new Paragraph(" ", bodyFont));
            }
        }

        JSONObject modulePlan = plan.optJSONObject("module_plan");
        if (modulePlan != null) {
            boolean hasAnyModule = hasModuleContent(modulePlan.optJSONObject("speech_sound"))
                    || hasModuleContent(modulePlan.optJSONObject("prelinguistic"))
                    || hasModuleContent(modulePlan.optJSONObject("vocabulary"))
                    || hasModuleContent(modulePlan.optJSONObject("syntax"))
                    || hasModuleContent(modulePlan.optJSONObject("social_pragmatics"));
            if (hasAnyModule) {
                document.add(new Paragraph(getStringRes(R.string.pdf_section_module_plan), titleFont));
                addModulePlan(document, getStringRes(R.string.pdf_module_speech_sound), modulePlan.optJSONObject("speech_sound"), bodyFont, true);
                addModulePlan(document, getStringRes(R.string.pdf_module_prelinguistic), modulePlan.optJSONObject("prelinguistic"), bodyFont, false);
                addModulePlan(document, getStringRes(R.string.pdf_module_vocabulary), modulePlan.optJSONObject("vocabulary"), bodyFont, false);
                addModulePlan(document, getStringRes(R.string.pdf_module_syntax), modulePlan.optJSONObject("syntax"), bodyFont, false);
                addModulePlan(document, getStringRes(R.string.pdf_module_social_pragmatics), modulePlan.optJSONObject("social_pragmatics"), bodyFont, false);
                document.add(new Paragraph(" ", bodyFont));
            }
        }

        JSONObject schedule = plan.optJSONObject("schedule_recommendation");
        if (schedule != null) {
            String sessions = getValueAsText(schedule, "sessions_per_week");
            String minutes = getValueAsText(schedule, "minutes_per_session");
            String review = getValueAsText(schedule, "review_in_weeks");
            if (!sessions.isEmpty() || !minutes.isEmpty() || !review.isEmpty()) {
                document.add(new Paragraph(getStringRes(R.string.pdf_section_schedule), titleFont));
                addKeyValue(document, getStringRes(R.string.pdf_label_sessions_per_week), sessions, bodyFont);
                addKeyValue(document, getStringRes(R.string.pdf_label_minutes_per_session), minutes, bodyFont);
                addKeyValue(document, getStringRes(R.string.pdf_label_review_in_weeks), review, bodyFont);
                document.add(new Paragraph(" ", bodyFont));
            }
        }

        JSONArray therapistNotes = plan.optJSONArray("notes_for_therapist");
        addList(document, getStringRes(R.string.pdf_label_notes_for_therapist), therapistNotes, bodyFont);
        JSONArray parentNotes = plan.optJSONArray("notes_for_parents");
        addList(document, getStringRes(R.string.pdf_label_notes_for_parents), parentNotes, bodyFont);
    }
    private static void renderOverallModules(Document document, JSONArray modules, Font bodyFont) throws Exception {
        if (modules == null || modules.length() == 0) {
            document.add(new Paragraph("暂无可用报告内容", bodyFont));
            return;
        }
        for (int i = 0; i < modules.length(); i++) {
            JSONObject module = modules.optJSONObject(i);
            if (module == null) {
                continue;
            }
            String moduleTitle = module.optString("moduleTitle", "模块报告");
            document.add(new Paragraph(moduleTitle, bodyFont));
            JSONArray sections = module.optJSONArray("sections");
            if (sections == null) {
                document.add(new Paragraph(" ", bodyFont));
                continue;
            }
            for (int j = 0; j < sections.length(); j++) {
                JSONObject section = sections.optJSONObject(j);
                if (section == null) {
                    continue;
                }
                String title = section.optString("title", "");
                String contentType = section.optString("contentType", "");
                if (!title.trim().isEmpty()) {
                    document.add(new Paragraph(title, bodyFont));
                }
                if ("bullets".equals(contentType)) {
                    addJsonList(document, section.optJSONArray("items"), bodyFont);
                } else {
                    String text = section.optString("text", "");
                    if (!text.trim().isEmpty()) {
                        document.add(new Paragraph(text, bodyFont));
                    }
                }
                document.add(new Paragraph(" ", bodyFont));
            }
        }
    }

    public static void writeTreatmentPlanPdf(Context context, Uri uri, JSONObject childJson) throws Exception {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        PdfGenerator.context = context.getApplicationContext();
        JSONObject plan = childJson == null ? null : childJson.optJSONObject("treatmentPlan");
        if (plan == null) {
            throw new IllegalStateException("Missing treatment plan");
        }
        OutputStream outputStream = null;
        Document document = new Document();
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                throw new IOException("Cannot open output stream");
            }
            PdfWriter.getInstance(document, outputStream);
            document.open();
            BaseFont baseFont = loadChineseBaseFont(context);
            if (baseFont == null) {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            }
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font bodyFont = new Font(baseFont, 11, Font.NORMAL);

            String reportMode = plan.optString("reportMode", "");
            String title = "overall_intervention".equals(reportMode) ? "总体干预报告" : "治疗方案";
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);
            document.add(new Paragraph(" ", bodyFont));

            JSONObject info = childJson == null ? null : childJson.optJSONObject("info");
            if (info != null) {
                document.add(new Paragraph("儿童信息", bodyFont));
                addKeyValueLine(document, "姓名", info.optString("name", ""), bodyFont);
                addKeyValueLine(document, "出生日期", info.optString("birthDate", ""), bodyFont);
                addKeyValueLine(document, "测试日期", info.optString("testDate", ""), bodyFont);
                document.add(new Paragraph(" ", bodyFont));
            }

            if (plan.has("modules")) {
                renderOverallModules(document, plan.optJSONArray("modules"), bodyFont);
            } else {
                renderTreatmentPlanModules(document, plan.optJSONObject("case_summary"),
                        plan.optJSONObject("module_plan"), bodyFont);
            }
        } finally {
            try {
                document.close();
            } catch (Exception ignored) {
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void writeChildInfoPdf(Context context, Uri uri, JSONObject childJson) throws Exception {
        if (context == null || uri == null) {
            String message = context != null ? context.getString(R.string.pdf_error_invalid_params) : "Invalid parameters";
            throw new IllegalArgumentException(message);
        }
        PdfGenerator.context = context.getApplicationContext();
        OutputStream outputStream = null;
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        try {
            Log.d("PdfGenerator", "writeChildInfoPdf start, uri=" + uri);
            Log.d("PdfGenerator", "writeChildInfoPdf opening output stream");
            outputStream = context.getContentResolver().openOutputStream(uri);
            Log.d("PdfGenerator", "writeChildInfoPdf output stream opened=" + (outputStream != null));
            if (outputStream == null) {
                throw new IOException(context.getString(R.string.pdf_error_open_output));
            }
            JSONObject info = childJson == null ? null : childJson.optJSONObject("info");
            Typeface chineseTypeface = loadChineseTypeface(context);
            if (chineseTypeface == null) {
                Log.w("PdfGenerator", "writeChildInfoPdf chinese typeface missing, fallback to Typeface.DEFAULT");
                chineseTypeface = Typeface.DEFAULT;
            }
            Log.d("PdfGenerator", "writeChildInfoPdf typeface ready=" + (chineseTypeface != null));
            TreatmentPlanStrings strings = TreatmentPlanStrings.from(context);
            ChildInfoPdfRenderer renderer = new ChildInfoPdfRenderer(info, chineseTypeface, strings);
            int totalPages = renderer.measurePageCount();
            Log.d("PdfGenerator", "writeChildInfoPdf measured page count=" + totalPages);
            Log.d("PdfGenerator", "writeChildInfoPdf rendering document");
            renderer.render(document, totalPages);
            Log.d("PdfGenerator", "writeChildInfoPdf writing document to output stream");
            document.writeTo(outputStream);
            Log.d("PdfGenerator", "writeChildInfoPdf completed successfully");
        } catch (Exception e) {
            Log.e("PdfGenerator", "writeChildInfoPdf failed", e);
            throw e;
        } finally {
            Log.d("PdfGenerator", "writeChildInfoPdf entering finally");
            try {
                document.close();
                Log.d("PdfGenerator", "writeChildInfoPdf document closed");
            } catch (Exception ignored) {
                Log.e("PdfGenerator", "writeChildInfoPdf close document failed", ignored);
            }
            if (outputStream != null) {
                try {
                    Log.d("PdfGenerator", "writeChildInfoPdf flushing output stream");
                    outputStream.flush();
                    Log.d("PdfGenerator", "writeChildInfoPdf closing output stream");
                    outputStream.close();
                } catch (IOException ignored) {
                    Log.e("PdfGenerator", "writeChildInfoPdf close output stream failed", ignored);
                }
            }
        }
    }

    private static void addModulePlan(Document document, String title, JSONObject plan, Font bodyFont, boolean isSpeechSound) throws Exception {
        if (!hasModuleContent(plan)) {
            return;
        }
        document.add(new Paragraph(title, bodyFont));
        addList(document, getStringRes(R.string.pdf_label_targets), plan.optJSONArray("targets"), bodyFont);
        if (isSpeechSound) {
            addList(document, getStringRes(R.string.pdf_label_methods), plan.optJSONArray("methods"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_activities), plan.optJSONArray("activities"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_sample_activities), plan.optJSONArray("sample_activities"), bodyFont);
        } else {
            addList(document, getStringRes(R.string.pdf_label_activities), plan.optJSONArray("activities"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_methods), plan.optJSONArray("methods"), bodyFont);
        }
        addList(document, getStringRes(R.string.pdf_label_home_practice), plan.optJSONArray("home_practice"), bodyFont);
        addList(document, getStringRes(R.string.pdf_label_metrics), plan.optJSONArray("metrics"), bodyFont);
        if (isSpeechSound) {
            addSpeechStages(document, plan, bodyFont);
        }
        document.add(new Paragraph(" ", bodyFont));
    }

    private static void addSpeechStages(Document document, JSONObject plan, Font bodyFont) throws Exception {
        if (plan == null) {
            return;
        }
        JSONArray stages = plan.optJSONArray("stages");
        if (!hasStageContent(stages)) {
            return;
        }
        document.add(new Paragraph(getStringRes(R.string.pdf_section_stage_training), bodyFont));
        for (int i = 0; i < stages.length(); i++) {
            JSONObject stage = stages.optJSONObject(i);
            if (stage == null) {
                continue;
            }
            String name = stage.optString("name", "").trim();
            if (name.isEmpty()) {
                name = getStringRes(R.string.pdf_stage_prefix) + (i + 1);
            }
            document.add(new Paragraph(name, bodyFont));
            addList(document, getStringRes(R.string.pdf_label_focus), stage.optJSONArray("focus"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_activities), stage.optJSONArray("activities"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_home_practice), stage.optJSONArray("home_practice"), bodyFont);
            addList(document, getStringRes(R.string.pdf_label_metrics), stage.optJSONArray("metrics"), bodyFont);
        }
    }

    private static String sanitizeBulletText(String text) {
        if (text == null) {
            return "";
        }
        String value = text.trim();
        boolean stripped;
        do {
            stripped = false;
            if (value.isEmpty()) {
                return value;
            }
            char first = value.charAt(0);
            if (isBulletChar(first)) {
                value = value.substring(1).trim();
                stripped = true;
                continue;
            }
            if (first == '-' || first == '–' || first == '—' || first == '*') {
                if (value.length() > 1 && Character.isWhitespace(value.charAt(1))) {
                    value = value.substring(1).trim();
                    stripped = true;
                }
            }
        } while (stripped);
        return value;
    }

    private static boolean isBulletChar(char c) {
        return c == '•' || c == '·' || c == '●';
    }

    private static void addList(Document document, String title, JSONArray items, Font bodyFont) throws Exception {
        if (!hasItems(items)) {
            return;
        }
        String labelSeparator = getStringRes(R.string.pdf_label_separator);
        String bulletPrefix = getStringRes(R.string.pdf_bullet_prefix);
        document.add(new Paragraph(title + labelSeparator, bodyFont));
        for (int i = 0; i < items.length(); i++) {
            String item = sanitizeBulletText(items.optString(i, ""));
            if (!item.isEmpty()) {
                document.add(new Paragraph(bulletPrefix + item, bodyFont));
            }
        }
    }

    private static void addKeyValue(Document document, String title, String value, Font bodyFont) throws Exception {
        String text = value != null ? value.trim() : "";
        if (text.isEmpty()) {
            return;
        }
        String labelSeparator = getStringRes(R.string.pdf_label_separator);
        document.add(new Paragraph(title + labelSeparator + text, bodyFont));
    }

    private static boolean hasItems(JSONArray items) {
        if (items == null || items.length() == 0) {
            return false;
        }
        for (int i = 0; i < items.length(); i++) {
            String item = items.optString(i, "").trim();
            if (!item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasModuleContent(JSONObject plan) {
        if (plan == null) {
            return false;
        }
        return hasItems(plan.optJSONArray("targets"))
                || hasItems(plan.optJSONArray("methods"))
                || hasItems(plan.optJSONArray("sample_activities"))
                || hasItems(plan.optJSONArray("activities"))
                || hasItems(plan.optJSONArray("home_practice"))
                || hasItems(plan.optJSONArray("metrics"))
                || hasStageContent(plan.optJSONArray("stages"));
    }

    private static boolean hasStageContent(JSONArray stages) {
        if (stages == null || stages.length() == 0) {
            return false;
        }
        for (int i = 0; i < stages.length(); i++) {
            JSONObject stage = stages.optJSONObject(i);
            if (stage == null) {
                continue;
            }
            if (hasItems(stage.optJSONArray("focus"))
                    || hasItems(stage.optJSONArray("activities"))
                    || hasItems(stage.optJSONArray("home_practice"))
                    || hasItems(stage.optJSONArray("metrics"))) {
                return true;
            }
        }
        return false;
    }

    private static String getValueAsText(JSONObject object, String key) {
        if (object == null || key == null) {
            return "";
        }
        Object value = object.opt(key);
        if (value == null || JSONObject.NULL.equals(value)) {
            return "";
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return String.valueOf(value).trim();
    }

    private static String getInfoValue(JSONObject info, String key) {
        if (info == null || key == null) {
            return getStringRes(R.string.pdf_placeholder);
        }
        return valueOrDefault(info.optString(key, ""));
    }

    private static String valueOrDefault(String value) {
        String text = value == null ? "" : value.trim();
        return text.isEmpty() ? getStringRes(R.string.pdf_placeholder) : text;
    }

    private static final class TreatmentPlanStrings {
        final String title;
        final String placeholder;
        final String familyEmpty;
        final String labelSeparator;
        final String bulletPrefix;
        final String datePrefix;
        final String pageIndicatorFormat;
        final String sectionPersonalInfo;
        final String sectionFamilyMembers;
        final String familyMemberPrefix;
        final String labelName;
        final String labelGender;
        final String labelBirthDate;
        final String labelTestDate;
        final String labelExaminer;
        final String labelPhone;
        final String labelAddress;
        final String labelFamilyStatus;
        final String labelRelation;
        final String labelOccupation;
        final String labelEducation;
        final String sectionCaseSummary;
        final String labelAgeMonths;
        final String labelChiefComplaint;
        final String labelKeyFindings;
        final String labelTestResults;
        final String labelSuspectedDiagnosis;
        final String labelRiskFlags;
        final String sectionOverallGoals;
        final String labelWithin4Weeks;
        final String labelWithin12Weeks;
        final String labelWithin24Weeks;
        final String sectionModulePlan;
        final String moduleSpeechSound;
        final String modulePrelinguistic;
        final String moduleVocabulary;
        final String moduleSyntax;
        final String moduleSocialPragmatics;
        final String sectionStageTraining;
        final String sectionSchedule;
        final String labelSessionsPerWeek;
        final String labelMinutesPerSession;
        final String labelReviewInWeeks;
        final String sectionNotes;
        final String labelNotesForTherapist;
        final String labelNotesForParents;
        final String labelTargets;
        final String labelMethods;
        final String labelActivities;
        final String labelSampleActivities;
        final String labelHomePractice;
        final String labelMetrics;
        final String stagePrefix;
        final String labelFocus;

        private TreatmentPlanStrings(
                String title,
                String placeholder,
                String familyEmpty,
                String labelSeparator,
                String bulletPrefix,
                String datePrefix,
                String pageIndicatorFormat,
                String sectionPersonalInfo,
                String sectionFamilyMembers,
                String familyMemberPrefix,
                String labelName,
                String labelGender,
                String labelBirthDate,
                String labelTestDate,
                String labelExaminer,
                String labelPhone,
                String labelAddress,
                String labelFamilyStatus,
                String labelRelation,
                String labelOccupation,
                String labelEducation,
                String sectionCaseSummary,
                String labelAgeMonths,
                String labelChiefComplaint,
                String labelKeyFindings,
                String labelTestResults,
                String labelSuspectedDiagnosis,
                String labelRiskFlags,
                String sectionOverallGoals,
                String labelWithin4Weeks,
                String labelWithin12Weeks,
                String labelWithin24Weeks,
                String sectionModulePlan,
                String moduleSpeechSound,
                String modulePrelinguistic,
                String moduleVocabulary,
                String moduleSyntax,
                String moduleSocialPragmatics,
                String sectionStageTraining,
                String sectionSchedule,
                String labelSessionsPerWeek,
                String labelMinutesPerSession,
                String labelReviewInWeeks,
                String sectionNotes,
                String labelNotesForTherapist,
                String labelNotesForParents,
                String labelTargets,
                String labelMethods,
                String labelActivities,
                String labelSampleActivities,
                String labelHomePractice,
                String labelMetrics,
                String stagePrefix,
                String labelFocus) {
            this.title = title;
            this.placeholder = placeholder;
            this.familyEmpty = familyEmpty;
            this.labelSeparator = labelSeparator;
            this.bulletPrefix = bulletPrefix;
            this.datePrefix = datePrefix;
            this.pageIndicatorFormat = pageIndicatorFormat;
            this.sectionPersonalInfo = sectionPersonalInfo;
            this.sectionFamilyMembers = sectionFamilyMembers;
            this.familyMemberPrefix = familyMemberPrefix;
            this.labelName = labelName;
            this.labelGender = labelGender;
            this.labelBirthDate = labelBirthDate;
            this.labelTestDate = labelTestDate;
            this.labelExaminer = labelExaminer;
            this.labelPhone = labelPhone;
            this.labelAddress = labelAddress;
            this.labelFamilyStatus = labelFamilyStatus;
            this.labelRelation = labelRelation;
            this.labelOccupation = labelOccupation;
            this.labelEducation = labelEducation;
            this.sectionCaseSummary = sectionCaseSummary;
            this.labelAgeMonths = labelAgeMonths;
            this.labelChiefComplaint = labelChiefComplaint;
            this.labelKeyFindings = labelKeyFindings;
            this.labelTestResults = labelTestResults;
            this.labelSuspectedDiagnosis = labelSuspectedDiagnosis;
            this.labelRiskFlags = labelRiskFlags;
            this.sectionOverallGoals = sectionOverallGoals;
            this.labelWithin4Weeks = labelWithin4Weeks;
            this.labelWithin12Weeks = labelWithin12Weeks;
            this.labelWithin24Weeks = labelWithin24Weeks;
            this.sectionModulePlan = sectionModulePlan;
            this.moduleSpeechSound = moduleSpeechSound;
            this.modulePrelinguistic = modulePrelinguistic;
            this.moduleVocabulary = moduleVocabulary;
            this.moduleSyntax = moduleSyntax;
            this.moduleSocialPragmatics = moduleSocialPragmatics;
            this.sectionStageTraining = sectionStageTraining;
            this.sectionSchedule = sectionSchedule;
            this.labelSessionsPerWeek = labelSessionsPerWeek;
            this.labelMinutesPerSession = labelMinutesPerSession;
            this.labelReviewInWeeks = labelReviewInWeeks;
            this.sectionNotes = sectionNotes;
            this.labelNotesForTherapist = labelNotesForTherapist;
            this.labelNotesForParents = labelNotesForParents;
            this.labelTargets = labelTargets;
            this.labelMethods = labelMethods;
            this.labelActivities = labelActivities;
            this.labelSampleActivities = labelSampleActivities;
            this.labelHomePractice = labelHomePractice;
            this.labelMetrics = labelMetrics;
            this.stagePrefix = stagePrefix;
            this.labelFocus = labelFocus;
        }

        static TreatmentPlanStrings from(Context context) {
            return new TreatmentPlanStrings(
                    context.getString(R.string.pdf_treatment_plan_title),
                    context.getString(R.string.pdf_placeholder),
                    context.getString(R.string.pdf_family_empty),
                    context.getString(R.string.pdf_label_separator),
                    context.getString(R.string.pdf_bullet_prefix),
                    context.getString(R.string.pdf_date_prefix),
                    context.getString(R.string.pdf_page_indicator),
                    context.getString(R.string.pdf_section_personal_info),
                    context.getString(R.string.pdf_section_family_members),
                    context.getString(R.string.pdf_family_member_prefix),
                    context.getString(R.string.pdf_label_name),
                    context.getString(R.string.pdf_label_gender),
                    context.getString(R.string.pdf_label_birth_date),
                    context.getString(R.string.pdf_label_test_date),
                    context.getString(R.string.pdf_label_examiner),
                    context.getString(R.string.pdf_label_phone),
                    context.getString(R.string.pdf_label_address),
                    context.getString(R.string.pdf_label_family_status),
                    context.getString(R.string.pdf_label_relation),
                    context.getString(R.string.pdf_label_occupation),
                    context.getString(R.string.pdf_label_education),
                    context.getString(R.string.pdf_section_case_summary),
                    context.getString(R.string.pdf_label_age_months),
                    context.getString(R.string.pdf_label_chief_complaint),
                    context.getString(R.string.pdf_label_key_findings),
                    context.getString(R.string.pdf_label_test_results),
                    context.getString(R.string.pdf_label_suspected_diagnosis),
                    context.getString(R.string.pdf_label_risk_flags),
                    context.getString(R.string.pdf_section_overall_goals),
                    context.getString(R.string.pdf_label_within_4_weeks),
                    context.getString(R.string.pdf_label_within_12_weeks),
                    context.getString(R.string.pdf_label_within_24_weeks),
                    context.getString(R.string.pdf_section_module_plan),
                    context.getString(R.string.pdf_module_speech_sound),
                    context.getString(R.string.pdf_module_prelinguistic),
                    context.getString(R.string.pdf_module_vocabulary),
                    context.getString(R.string.pdf_module_syntax),
                    context.getString(R.string.pdf_module_social_pragmatics),
                    context.getString(R.string.pdf_section_stage_training),
                    context.getString(R.string.pdf_section_schedule),
                    context.getString(R.string.pdf_label_sessions_per_week),
                    context.getString(R.string.pdf_label_minutes_per_session),
                    context.getString(R.string.pdf_label_review_in_weeks),
                    context.getString(R.string.pdf_section_notes),
                    context.getString(R.string.pdf_label_notes_for_therapist),
                    context.getString(R.string.pdf_label_notes_for_parents),
                    context.getString(R.string.pdf_label_targets),
                    context.getString(R.string.pdf_label_methods),
                    context.getString(R.string.pdf_label_activities),
                    context.getString(R.string.pdf_label_sample_activities),
                    context.getString(R.string.pdf_label_home_practice),
                    context.getString(R.string.pdf_label_metrics),
                    context.getString(R.string.pdf_stage_prefix),
                    context.getString(R.string.pdf_label_focus));
        }
    }


    private static class TreatmentPlanPdfRenderer {
        private static final float PAGE_WIDTH = 595f;
        private static final float PAGE_HEIGHT = 842f;
        private static final float MARGIN = 36f;
        private static final float FOOTER_HEIGHT = 28f;
        private static final float HEADER_GAP = 8f;
        private static final float SECTION_GAP = 12f;
        private static final float LINE_GAP = 6f;
        private static final float GRID_GAP = 12f;
        private static final float CARD_GAP = 10f;
        private static final float CARD_PADDING = 10f;
        private static final float SECTION_PADDING = 6f;
        private static final float BULLET_INDENT = 12f;
        private static final float BULLET_GAP = 4f;
        private static final float FINDINGS_PADDING = 8f;
        private static final float FINDINGS_RADIUS = 6f;
        private static final float INFO_BLOCK_PADDING = 10f;
        private static final float INFO_BLOCK_RADIUS = 8f;
        private static final float MEMBER_CONTAINER_PADDING = 8f;
        private static final float MEMBER_CARD_PADDING = 8f;
        private static final float MEMBER_CARD_RADIUS = 6f;
        private static final float MEMBER_CONTAINER_RADIUS = 8f;
        private static final float MEMBER_CARD_GAP = 8f;
        private static final String PLACEHOLDER_TEXT = "（根据评估情况待定）";
        private static final String BULLET_SYMBOL = "•";
        private static final String LABEL_SCORE_DATA = "评分/数据";
        private static final String STAGE_LABEL_ACTIVITIES = "活动建议";
        private static final String STAGE_LABEL_METRICS = "评估指标";
        private static final String MODULE_TITLE_SPEECH_SOUND = "语音/构音";
        private static final String MODULE_TITLE_PRELINGUISTIC = "前语言";
        private static final String MODULE_TITLE_VOCABULARY = "词汇";
        private static final String MODULE_TITLE_SYNTAX = "句法";
        private static final String MODULE_TITLE_SOCIAL = "社会交往";
        private static final String SECTION_INTERVENTION_GUIDE = "干预指导";
        private static final String SECTION_ARTICULATION_OVERALL = "评估结果（整体）";
        private static final String SECTION_ARTICULATION_MASTERED = "已掌握的能力";
        private static final String SECTION_ARTICULATION_NOT_MASTERED = "未掌握能力整体说明";
        private static final String SECTION_ARTICULATION_FOCUS = "【需要重点关注的能力】";
        private static final String SECTION_ARTICULATION_UNSTABLE = "【不稳定的能力】";
        private static final String SECTION_ARTICULATION_SMART = "干预目标（SMART）";
        private static final String SECTION_ARTICULATION_HOME = "家庭干预指导建议";
        private static final String LABEL_AGE = "年龄";
        private static final String AGE_MISSING_TEXT = "未提供";
        private static final String MEMBER_SEPARATOR = "｜";

        protected final JSONObject info;
        protected final JSONObject plan;
        protected final TreatmentPlanStrings strings;

        private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint moduleTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint bodyPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint boldPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint smallPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint sectionFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint accentSectionFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint cardFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint cardStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private boolean measureOnly;
        private android.graphics.pdf.PdfDocument document;
        private android.graphics.pdf.PdfDocument.Page page;
        private Canvas canvas;
        private float y;
        private int pageNumber;
        private int totalPages;

        TreatmentPlanPdfRenderer(JSONObject info, JSONObject plan, Typeface chineseTypeface, TreatmentPlanStrings strings) {
            this.info = info;
            this.plan = plan;
            this.strings = strings;
            titlePaint.setColor(Color.BLACK);
            titlePaint.setTextSize(18f);
            Typeface baseTypeface = chineseTypeface;
            titlePaint.setTypeface(baseTypeface);
            titlePaint.setFakeBoldText(true);

            moduleTitlePaint.setColor(Color.BLACK);
            moduleTitlePaint.setTextSize(12f);
            moduleTitlePaint.setTypeface(baseTypeface);
            moduleTitlePaint.setFakeBoldText(true);

            bodyPaint.setColor(Color.BLACK);
            bodyPaint.setTextSize(10f);
            bodyPaint.setTypeface(baseTypeface);

            boldPaint.setColor(Color.BLACK);
            boldPaint.setTextSize(10f);
            boldPaint.setTypeface(baseTypeface);
            boldPaint.setFakeBoldText(true);

            smallPaint.setColor(Color.DKGRAY);
            smallPaint.setTextSize(9f);
            smallPaint.setTypeface(baseTypeface);

            linePaint.setColor(Color.DKGRAY);
            linePaint.setStrokeWidth(1f);

            sectionFillPaint.setColor(0xFFEFEFEF);
            accentSectionFillPaint.setColor(0xFFE6F1FF);

            cardFillPaint.setColor(0xFFF9F9F9);
            cardStrokePaint.setColor(0xFFDDDDDD);
            cardStrokePaint.setStyle(Paint.Style.STROKE);
            cardStrokePaint.setStrokeWidth(1f);
        }

        int measurePageCount() {
            return renderInternal(true, null, 0);
        }

        void render(android.graphics.pdf.PdfDocument document, int totalPages) {
            renderInternal(false, document, totalPages);
        }

        private int renderInternal(boolean measureOnly, android.graphics.pdf.PdfDocument document, int totalPages) {
            this.measureOnly = measureOnly;
            this.document = document;
            this.totalPages = totalPages;
            this.pageNumber = 0;
            startPage();
            drawLeadingSections();
            drawDocumentBody();
            // TODO(隐藏需求): 频次建议与备注区块暂时隐藏，后续恢复时取消注释。
            // drawSchedule();
            // drawNotes();
            finishPage();
            return pageNumber;
        }

        protected void drawLeadingSections() {
            drawPersonalInfo();
        }

        protected void drawDocumentBody() {
            drawAccentSectionHeader(SECTION_INTERVENTION_GUIDE);
            drawModuleBlocks();
        }

        private void startPage() {
            pageNumber++;
            if (!measureOnly) {
                android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                        new android.graphics.pdf.PdfDocument.PageInfo.Builder((int) PAGE_WIDTH, (int) PAGE_HEIGHT, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                drawHeader();
            }
            y = MARGIN + getHeaderHeight() + HEADER_GAP;
        }

        private void finishPage() {
            if (!measureOnly && page != null) {
                drawFooter();
                document.finishPage(page);
            }
            page = null;
            canvas = null;
        }

        private void ensureSpace(float requiredHeight) {
            float top = MARGIN + getHeaderHeight() + HEADER_GAP;
            float bottom = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT;
            if (y + requiredHeight > bottom && y > top) {
                finishPage();
                startPage();
            }
        }

        private void drawHeader() {
            float contentWidth = getContentWidth();
            String dateText = getDateText();
            float titleHeight = 0f;
            if (pageNumber == 1) {
                titleHeight = drawTextBlock(getDocumentTitle(), titlePaint, MARGIN, MARGIN, contentWidth, Layout.Alignment.ALIGN_CENTER);
            }
            float dateY = MARGIN + (pageNumber == 1 ? titleHeight + 2f : 0f);
            drawTextBlock(dateText, smallPaint, MARGIN, dateY, contentWidth, Layout.Alignment.ALIGN_OPPOSITE);
        }

        private void drawFooter() {
            float contentWidth = getContentWidth();
            int total = totalPages > 0 ? totalPages : pageNumber;
            String text = String.format(Locale.CHINA, strings.pageIndicatorFormat, pageNumber, total);
            float footerY = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT + 8f;
            drawTextBlock(text, smallPaint, MARGIN, footerY, contentWidth, Layout.Alignment.ALIGN_CENTER);
        }

        private float getHeaderHeight() {
            float contentWidth = getContentWidth();
            String dateText = getDateText();
            float dateHeight = measureTextHeight(dateText, smallPaint, contentWidth, Layout.Alignment.ALIGN_OPPOSITE);
            if (pageNumber == 1) {
                float titleHeight = measureTextHeight(getDocumentTitle(), titlePaint, contentWidth, Layout.Alignment.ALIGN_CENTER);
                return titleHeight + dateHeight + 2f;
            }
            return dateHeight;
        }

        protected String getDocumentTitle() {
            return strings.title;
        }

        protected String getDateText() {
            return strings.datePrefix + new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        }

        private float getContentWidth() {
            return PAGE_WIDTH - MARGIN * 2f;
        }

        protected float getRendererContentWidth() {
            return getContentWidth();
        }

        protected float getRendererMargin() {
            return MARGIN;
        }

        protected float getRendererCursorY() {
            return y;
        }

        protected void setRendererCursorY(float value) {
            y = value;
        }

        protected void advanceRendererCursor(float delta) {
            y += delta;
        }

        protected void ensureRendererSpace(float requiredHeight) {
            ensureSpace(requiredHeight);
        }

        protected boolean isRendererMeasureOnly() {
            return measureOnly;
        }

        protected Canvas getRendererCanvas() {
            return canvas;
        }

        protected float measureRendererBodyTextHeight(String text, float width) {
            return measureTextHeight(text, bodyPaint, width, Layout.Alignment.ALIGN_NORMAL);
        }

        protected float drawRendererBodyTextBlock(String text, float x, float top, float width, Layout.Alignment alignment) {
            return drawTextBlock(text, bodyPaint, x, top, width, alignment);
        }

        private void drawDivider() {
            ensureSpace(SECTION_GAP);
            if (!measureOnly) {
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
            }
            y += SECTION_GAP;
        }

        private void drawPersonalInfo() {
            drawAccentSectionHeader(strings.sectionPersonalInfo);
            List<Field> fields = new ArrayList<>();
            fields.add(new Field(strings.labelName, infoValue("name"), 1, false));
            fields.add(new Field(strings.labelGender, infoValue("gender"), 1, false));
            fields.add(new Field(strings.labelBirthDate, infoValue("birthDate"), 1, false));
            fields.add(new Field(strings.labelPhone, infoValue("phone"), 1, false));
            fields.add(new Field(strings.labelAddress, infoValue("address"), 2, false));
            fields.add(new Field(strings.labelFamilyStatus, safeText(info == null ? "" : info.optString("familyStatus", "")), 2, true, true));
            drawKeyValueGrid(fields);

            drawSubHeaderPlain(strings.sectionFamilyMembers);
            List<MemberInfo> members = getFamilyMembers();
            if (members.isEmpty()) {
                drawParagraph(strings.familyEmpty);
                return;
            }
            drawFamilyMembersCard(members);
        }

        private void drawCaseSummary() {
            JSONObject summary = plan == null ? null : plan.optJSONObject("case_summary");
            if (summary == null) {
                return;
            }
            String chief = safeText(summary.optString("chief_complaint", ""));
            List<String> keyFindings = getList(summary, "key_findings");
            List<String> suspected = getList(summary, "suspected_diagnosis");
            List<String> risks = getList(summary, "risk_flags");
            if (chief.isEmpty() && keyFindings.isEmpty() && suspected.isEmpty() && risks.isEmpty()) {
                return;
            }
            drawSectionHeader(strings.sectionCaseSummary);
            drawLabelValue(strings.labelChiefComplaint, chief);
            drawBulletSection(strings.labelKeyFindings, keyFindings, false);
            drawBulletSection(strings.labelSuspectedDiagnosis, suspected, false);
            drawBulletSection(strings.labelRiskFlags, risks, false);
        }

        private void drawOverallGoals() {
            JSONObject overall = plan == null ? null : plan.optJSONObject("overall_goals");
            if (overall == null) {
                return;
            }
            List<String> within4 = getList(overall, "within_4_weeks");
            List<String> within12 = getList(overall, "within_12_weeks");
            List<String> within24 = getList(overall, "within_24_weeks");
            if (within4.isEmpty() && within12.isEmpty() && within24.isEmpty()) {
                return;
            }
            drawSectionHeader(strings.sectionOverallGoals);
            drawBulletSection(strings.labelWithin4Weeks, within4, false);
            drawBulletSection(strings.labelWithin12Weeks, within12, false);
            drawBulletSection(strings.labelWithin24Weeks, within24, false);
        }

        private void drawModulePlan() {
            JSONObject modulePlan = plan == null ? null : plan.optJSONObject("module_plan");
            if (modulePlan == null) {
                return;
            }
            boolean hasAny = hasModuleContent(modulePlan.optJSONObject("speech_sound"))
                    || hasModuleContent(modulePlan.optJSONObject("prelinguistic"))
                    || hasModuleContent(modulePlan.optJSONObject("vocabulary"))
                    || hasModuleContent(modulePlan.optJSONObject("syntax"))
                    || hasModuleContent(modulePlan.optJSONObject("social_pragmatics"));
            if (!hasAny) {
                return;
            }
            drawSectionHeader(strings.sectionModulePlan);
            drawModule(strings.moduleSpeechSound, modulePlan.optJSONObject("speech_sound"), true);
            drawModule(strings.modulePrelinguistic, modulePlan.optJSONObject("prelinguistic"), false);
            drawModule(strings.moduleVocabulary, modulePlan.optJSONObject("vocabulary"), false);
            drawModule(strings.moduleSyntax, modulePlan.optJSONObject("syntax"), false);
            drawModule(strings.moduleSocialPragmatics, modulePlan.optJSONObject("social_pragmatics"), false);
        }

        private void drawModuleBlocks() {
            JSONObject modulePlan = plan == null ? null : plan.optJSONObject("module_plan");
            drawModuleBlock(MODULE_TITLE_SPEECH_SOUND,
                    modulePlan == null ? null : modulePlan.optJSONObject("speech_sound"),
                    plan,
                    false);
            drawModuleBlock(MODULE_TITLE_PRELINGUISTIC,
                    modulePlan == null ? null : modulePlan.optJSONObject("prelinguistic"),
                    plan,
                    true);
            drawModuleBlock(MODULE_TITLE_VOCABULARY,
                    modulePlan == null ? null : modulePlan.optJSONObject("vocabulary"),
                    plan,
                    true);
            drawModuleBlock(MODULE_TITLE_SYNTAX,
                    modulePlan == null ? null : modulePlan.optJSONObject("syntax"),
                    plan,
                    true);
            drawModuleBlock(MODULE_TITLE_SOCIAL,
                    modulePlan == null ? null : modulePlan.optJSONObject("social_pragmatics"),
                    plan,
                    true);
        }

        private void drawModuleBlock(String title, JSONObject moduleData, JSONObject summaryData, boolean drawLine) {
            drawModuleHeader(title, drawLine);
            boolean isSpeechSound = MODULE_TITLE_SPEECH_SOUND.equals(title);
            drawFindingsCard(isSpeechSound, moduleData, summaryData);
            drawInterventionGuide(moduleData, isSpeechSound);
            y += SECTION_GAP;
        }

        protected void drawModuleHeader(String title, boolean drawLine) {
            float contentWidth = getContentWidth();
            Layout.Alignment alignment = getModuleTitleAlignment();
            float titleHeight = measureTextHeight(title, moduleTitlePaint, contentWidth, alignment);
            float required = (drawLine ? LINE_GAP : 0f) + titleHeight + LINE_GAP;
            ensureSpace(required);
            if (drawLine && !measureOnly) {
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
            }
            if (drawLine) {
                y += LINE_GAP;
            }
            drawTextBlock(title, moduleTitlePaint, MARGIN, y, contentWidth, alignment);
            y += titleHeight + LINE_GAP;
        }

        protected Layout.Alignment getModuleTitleAlignment() {
            return Layout.Alignment.ALIGN_NORMAL;
        }

        private void drawFindingsCard(boolean isSpeechSound, JSONObject moduleData, JSONObject summaryData) {
            List<String> keyFindings = getList(moduleData, "key_findings");
            if (keyFindings.isEmpty()) {
                keyFindings = new ArrayList<>();
                keyFindings.add(placeholderText());
            }

            String label = strings.labelTestResults;
            float contentWidth = getContentWidth() - FINDINGS_PADDING * 2f;
            float height = FINDINGS_PADDING;
            height += measureBulletSectionHeight(label, keyFindings, contentWidth);
            height += FINDINGS_PADDING;

            ensureSpace(height + LINE_GAP);
            if (!measureOnly) {
                RectF rect = new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height);
                canvas.drawRoundRect(rect, FINDINGS_RADIUS, FINDINGS_RADIUS, sectionFillPaint);
            }
            float cursorY = y + FINDINGS_PADDING;
            cursorY += drawBulletSectionAt(label, keyFindings, MARGIN + FINDINGS_PADDING, cursorY, contentWidth);
            y += height + LINE_GAP;
        }

        private void drawFamilyMembersCard(List<MemberInfo> members) {
            float contentWidth = getContentWidth();
            float innerWidth = contentWidth - MEMBER_CONTAINER_PADDING * 2f;
            float totalHeight = MEMBER_CONTAINER_PADDING;
            for (MemberInfo member : members) {
                totalHeight += measureFamilyMemberCardHeight(member, innerWidth) + MEMBER_CARD_GAP;
            }
            if (!members.isEmpty()) {
                totalHeight -= MEMBER_CARD_GAP;
            }
            totalHeight += MEMBER_CONTAINER_PADDING;

            ensureSpace(totalHeight + LINE_GAP);
            if (!measureOnly) {
                RectF container = new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + totalHeight);
                canvas.drawRoundRect(container, MEMBER_CONTAINER_RADIUS, MEMBER_CONTAINER_RADIUS, sectionFillPaint);
            }
            float cursorY = y + MEMBER_CONTAINER_PADDING;
            float cardX = MARGIN + MEMBER_CONTAINER_PADDING;
            for (MemberInfo member : members) {
                float cardHeight = measureFamilyMemberCardHeight(member, innerWidth);
                if (!measureOnly) {
                    RectF card = new RectF(cardX, cursorY, cardX + innerWidth, cursorY + cardHeight);
                    canvas.drawRoundRect(card, MEMBER_CARD_RADIUS, MEMBER_CARD_RADIUS, cardFillPaint);
                    canvas.drawRoundRect(card, MEMBER_CARD_RADIUS, MEMBER_CARD_RADIUS, cardStrokePaint);
                }
                drawFamilyMemberCardContent(member, cardX, cursorY, innerWidth);
                cursorY += cardHeight + MEMBER_CARD_GAP;
            }
            y += totalHeight + LINE_GAP;
        }

        private float measureFamilyMemberCardHeight(MemberInfo member, float width) {
            float textWidth = width - MEMBER_CARD_PADDING * 2f;
            String line1 = buildMemberLine1(member);
            String line2 = buildMemberLine2(member);
            String line3 = buildMemberLine3(member);
            float height = MEMBER_CARD_PADDING;
            height += measureTextHeight(line1, bodyPaint, textWidth, Layout.Alignment.ALIGN_NORMAL) + LINE_GAP;
            height += measureTextHeight(line2, bodyPaint, textWidth, Layout.Alignment.ALIGN_NORMAL) + LINE_GAP;
            height += measureTextHeight(line3, bodyPaint, textWidth, Layout.Alignment.ALIGN_NORMAL);
            height += MEMBER_CARD_PADDING;
            return height;
        }

        private void drawFamilyMemberCardContent(MemberInfo member, float x, float y, float width) {
            float textWidth = width - MEMBER_CARD_PADDING * 2f;
            float cursorY = y + MEMBER_CARD_PADDING;
            String line1 = buildMemberLine1(member);
            String line2 = buildMemberLine2(member);
            String line3 = buildMemberLine3(member);
            cursorY += drawTextBlock(line1, bodyPaint, x + MEMBER_CARD_PADDING, cursorY, textWidth, Layout.Alignment.ALIGN_NORMAL);
            cursorY += LINE_GAP;
            cursorY += drawTextBlock(line2, bodyPaint, x + MEMBER_CARD_PADDING, cursorY, textWidth, Layout.Alignment.ALIGN_NORMAL);
            cursorY += LINE_GAP;
            drawTextBlock(line3, bodyPaint, x + MEMBER_CARD_PADDING, cursorY, textWidth, Layout.Alignment.ALIGN_NORMAL);
        }

        private String buildMemberLine1(MemberInfo member) {
            String name = valueOrPlaceholder(member.name);
            String relation = valueOrPlaceholder(member.relation);
            return strings.labelName + strings.labelSeparator + name
                    + "  " + MEMBER_SEPARATOR + "  "
                    + strings.labelRelation + strings.labelSeparator + relation;
        }

        private String buildMemberLine2(MemberInfo member) {
            String phone = valueOrPlaceholder(member.phone);
            return strings.labelPhone + strings.labelSeparator + phone;
        }

        private String buildMemberLine3(MemberInfo member) {
            String occupation = valueOrPlaceholder(member.occupation);
            String education = valueOrPlaceholder(member.education);
            return strings.labelOccupation + strings.labelSeparator + occupation
                    + "  " + MEMBER_SEPARATOR + "  "
                    + strings.labelEducation + strings.labelSeparator + education;
        }

        private void drawPlanList(JSONObject moduleData) {
            List<String> targets = getList(moduleData, "targets");
            List<String> methods = getList(moduleData, "methods");
            if (methods.isEmpty()) {
                methods = getList(moduleData, "activities");
            }
            List<String> homePractice = getList(moduleData, "home_practice");
            drawBulletSection(strings.labelTargets, targets, true);
            drawBulletSection(strings.labelMethods, methods, true);
            drawBulletSection(strings.labelHomePractice, homePractice, true);
        }

        private void drawInterventionGuide(JSONObject moduleData, boolean isSpeechSound) {
            JSONObject guide = resolveInterventionGuide(moduleData, isSpeechSound);

            drawSubHeader(SECTION_ARTICULATION_OVERALL);
            drawParagraph(ensureParagraphText(readGuideText(guide, "overall_summary", "text")));

            drawSubHeader(SECTION_ARTICULATION_MASTERED);
            String masteredIntro = readGuideText(guide, "mastered", "intro");
            if (!masteredIntro.isEmpty()) {
                drawParagraph(masteredIntro);
            }
            drawBulletSection(null, readGuideList(guide, "mastered", "items"), true);

            drawSubHeader(SECTION_ARTICULATION_NOT_MASTERED);
            drawParagraph(ensureParagraphText(readGuideText(guide, "not_mastered_overview", "text")));

            drawSubHeader(SECTION_ARTICULATION_FOCUS);
            drawBulletSection(null, readGuideList(guide, "focus", "items"), true);

            drawSubHeader(SECTION_ARTICULATION_UNSTABLE);
            drawBulletSection(null, readGuideList(guide, "unstable", "items"), true);

            drawSubHeader(SECTION_ARTICULATION_SMART);
            String smartText = readGuideText(guide, "smart_goal", "text");
            if (smartText.isEmpty() && isSpeechSound && guide != null) {
                JSONObject smartGoal = guide.optJSONObject("smart_goal");
                if (smartGoal != null) {
                    smartText = ArticulationPlanHelper.buildSmartGoalText(smartGoal);
                }
            }
            drawParagraph(ensureParagraphText(smartText));

            drawSubHeader(SECTION_ARTICULATION_HOME);
            drawBulletSection(null, readGuideList(guide, "home_guidance", "items"), true);
        }

        private JSONObject resolveInterventionGuide(JSONObject moduleData, boolean isSpeechSound) {
            if (moduleData == null) {
                return null;
            }
            JSONObject guide = moduleData.optJSONObject("intervention_guide");
            if (guide == null && isSpeechSound) {
                guide = moduleData.optJSONObject("articulation");
            }
            return guide;
        }

        private String readGuideText(JSONObject guide, String sectionKey, String textKey) {
            if (guide == null || sectionKey == null || textKey == null) {
                return "";
            }
            JSONObject section = guide.optJSONObject(sectionKey);
            return section == null ? "" : safeText(section.optString(textKey, ""));
        }

        private List<String> readGuideList(JSONObject guide, String sectionKey, String listKey) {
            if (guide == null || sectionKey == null || listKey == null) {
                return new ArrayList<>();
            }
            JSONObject section = guide.optJSONObject(sectionKey);
            return getList(section, listKey);
        }

        private String ensureParagraphText(String text) {
            String value = safeText(text);
            return value.isEmpty() ? placeholderText() : value;
        }

        private boolean hasArticulation(JSONObject moduleData) {
            return moduleData != null && moduleData.optJSONObject("articulation") != null;
        }

        private void drawArticulationPlan(JSONObject moduleData) {
            if (moduleData == null) {
                return;
            }
            JSONObject articulation = moduleData.optJSONObject("articulation");
            if (articulation == null) {
                return;
            }

            JSONObject overall = articulation.optJSONObject("overall_summary");
            drawSubHeader(SECTION_ARTICULATION_OVERALL);
            drawParagraph(overall == null ? "" : overall.optString("text", ""));

            JSONObject mastered = articulation.optJSONObject("mastered");
            drawSubHeader(SECTION_ARTICULATION_MASTERED);
            String masteredIntro = mastered == null ? "" : safeText(mastered.optString("intro", ""));
            if (!masteredIntro.isEmpty()) {
                drawParagraph(masteredIntro);
            }
            drawBulletSection(null, getList(mastered, "items"), true);

            JSONObject notMastered = articulation.optJSONObject("not_mastered_overview");
            drawSubHeader(SECTION_ARTICULATION_NOT_MASTERED);
            drawParagraph(notMastered == null ? "" : notMastered.optString("text", ""));

            JSONObject focus = articulation.optJSONObject("focus");
            String focusTitle = focus == null ? "" : safeText(focus.optString("title", ""));
            drawArticulationList(resolveArticulationTitle(focusTitle, SECTION_ARTICULATION_FOCUS),
                    getList(focus, "items"));
            String focusNote = focus == null ? "" : safeText(focus.optString("note", ""));
            if (!focusNote.isEmpty()) {
                drawParagraph(focusNote);
            }

            JSONObject unstable = articulation.optJSONObject("unstable");
            String unstableTitle = unstable == null ? "" : safeText(unstable.optString("title", ""));
            drawArticulationList(resolveArticulationTitle(unstableTitle, SECTION_ARTICULATION_UNSTABLE),
                    getList(unstable, "items"));

            JSONObject smartGoal = articulation.optJSONObject("smart_goal");
            drawSubHeader(SECTION_ARTICULATION_SMART);
            String smartText = smartGoal == null ? "" : safeText(smartGoal.optString("text", ""));
            if (smartText.isEmpty() && smartGoal != null) {
                smartText = ArticulationPlanHelper.buildSmartGoalText(smartGoal);
            }
            drawParagraph(smartText);

            JSONObject homeGuidance = articulation.optJSONObject("home_guidance");
            drawArticulationList(SECTION_ARTICULATION_HOME, getList(homeGuidance, "items"));
        }

        private void drawArticulationList(String title, List<String> items) {
            drawSubHeader(title);
            List<String> filtered = new ArrayList<>();
            if (items != null) {
                for (String item : items) {
                    String text = safeText(item);
                    if (text.isEmpty() || PLACEHOLDER_TEXT.equals(text) || ArticulationPlanHelper.MISSING_DETAIL_HINT.equals(text)) {
                        continue;
                    }
                    filtered.add(text);
                }
            }
            if (filtered.isEmpty()) {
                drawParagraph(ArticulationPlanHelper.MISSING_DETAIL_HINT);
                return;
            }
            drawBulletSection(null, filtered, false);
        }

        private String resolveArticulationTitle(String title, String fallback) {
            return safeText(title).isEmpty() ? fallback : title.trim();
        }

        private void drawStageCards(JSONObject moduleData) {
            List<StageData> stages = getStageDataList(moduleData);
            if (stages.isEmpty()) {
                return;
            }
            float contentWidth = getContentWidth();
            float totalHeight = measureStageSectionHeight(stages, contentWidth);
            float top = MARGIN + getHeaderHeight() + HEADER_GAP;
            float bottom = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT;
            if (y + totalHeight > bottom && y > top) {
                finishPage();
                startPage();
            }
            float titleHeight = measureTextHeight(strings.sectionStageTraining, boldPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL);
            ensureSpace(titleHeight + LINE_GAP);
            drawTextBlock(strings.sectionStageTraining, boldPaint, MARGIN, y, contentWidth, Layout.Alignment.ALIGN_NORMAL);
            y += titleHeight + LINE_GAP;
            for (StageData stage : stages) {
                drawStageCard(stage);
            }
        }

        private float measureStageSectionHeight(List<StageData> stages, float contentWidth) {
            float height = measureTextHeight(strings.sectionStageTraining, boldPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL) + LINE_GAP;
            float innerWidth = contentWidth - CARD_PADDING * 2f;
            for (StageData stage : stages) {
                height += measureStageCardHeight(stage, innerWidth) + CARD_GAP;
            }
            return height;
        }

        private List<StageData> getStageDataList(JSONObject moduleData) {
            List<StageData> stages = new ArrayList<>();
            JSONArray stagesArray = moduleData == null ? null : moduleData.optJSONArray("stages");
            if (stagesArray == null || stagesArray.length() == 0) {
                return stages;
            }
            for (int i = 0; i < stagesArray.length(); i++) {
                JSONObject stage = stagesArray.optJSONObject(i);
                if (stage == null) {
                    continue;
                }
                String name = safeText(stage.optString("name", ""));
                if (name.isEmpty()) {
                    name = strings.stagePrefix + (i + 1);
                }
                stages.add(new StageData(
                        name,
                        getList(stage, "focus"),
                        getList(stage, "activities"),
                        getList(stage, "metrics")
                ));
            }
            return stages;
        }

        private void drawSchedule() {
            JSONObject schedule = plan == null ? null : plan.optJSONObject("schedule_recommendation");
            if (schedule == null) {
                return;
            }
            drawSectionHeader(strings.sectionSchedule, accentSectionFillPaint);
            int sessions = schedule.optInt("sessions_per_week", 0);
            int minutes = schedule.optInt("minutes_per_session", 0);
            int review = schedule.optInt("review_in_weeks", 0);
            drawLabelValue(strings.labelSessionsPerWeek, sessions > 0 ? String.valueOf(sessions) : "");
            drawLabelValue(strings.labelMinutesPerSession, minutes > 0 ? String.valueOf(minutes) : "");
            drawLabelValue(strings.labelReviewInWeeks, review > 0 ? String.valueOf(review) : "");
        }

        private void drawNotes() {
            List<String> therapistNotes = getList(plan, "notes_for_therapist");
            List<String> parentNotes = getList(plan, "notes_for_parents");
            if (therapistNotes.isEmpty() && parentNotes.isEmpty()) {
                return;
            }
            drawSectionHeader(strings.sectionNotes, accentSectionFillPaint);
            drawBulletSection(strings.labelNotesForTherapist, therapistNotes, false);
            drawBulletSection(strings.labelNotesForParents, parentNotes, false);
        }

        private void drawModule(String title, JSONObject module, boolean speechSound) {
            if (!hasModuleContent(module)) {
                return;
            }
            drawSubHeader(title);
            List<String> targets = getList(module, "targets");
            List<String> activities = getList(module, "activities");
            List<String> methods = getList(module, "methods");
            List<String> sampleActivities = getList(module, "sample_activities");
            List<String> homePractice = getList(module, "home_practice");

            drawBulletSection(strings.labelTargets, targets, false);
            if (speechSound) {
                drawBulletSection(strings.labelMethods, methods, false);
                drawBulletSection(strings.labelSampleActivities, sampleActivities, false);
                drawBulletSection(strings.labelActivities, activities, false);
            } else {
                if (!activities.isEmpty()) {
                    drawBulletSection(strings.labelActivities, activities, false);
                } else if (!methods.isEmpty()) {
                    drawBulletSection(strings.labelMethods, methods, false);
                    methods = new ArrayList<>();
                }
                drawBulletSection(strings.labelMethods, methods, false);
            }
            drawBulletSection(strings.labelHomePractice, homePractice, false);

            if (speechSound) {
                drawSpeechStages(module);
            }
        }

        private void drawSpeechStages(JSONObject module) {
            JSONArray stagesArray = module == null ? null : module.optJSONArray("stages");
            if (stagesArray == null || stagesArray.length() == 0) {
                return;
            }
            drawSubHeader(strings.sectionStageTraining);
            for (int i = 0; i < stagesArray.length(); i++) {
                JSONObject stage = stagesArray.optJSONObject(i);
                if (stage == null) {
                    continue;
                }
                String name = safeText(stage.optString("name", ""));
                if (name.isEmpty()) {
                    name = strings.stagePrefix + (i + 1);
                }
                StageData data = new StageData(
                        name,
                        getList(stage, "focus"),
                        getList(stage, "activities"),
                        getList(stage, "metrics")
                );
                drawStageCard(data);
            }
        }

        private void drawStageCard(StageData stage) {
            float contentWidth = getContentWidth() - CARD_PADDING * 2f;
            float cardHeight = measureStageCardHeight(stage, contentWidth);
            ensureSpace(cardHeight + CARD_GAP);
            if (!measureOnly) {
                RectF rect = new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight);
                canvas.drawRoundRect(rect, 8f, 8f, cardFillPaint);
                canvas.drawRoundRect(rect, 8f, 8f, cardStrokePaint);
            }
            float cursorY = y + CARD_PADDING;
            cursorY += drawTextBlock(stage.name, boldPaint, MARGIN + CARD_PADDING, cursorY, contentWidth, Layout.Alignment.ALIGN_NORMAL);
            cursorY += LINE_GAP;
            cursorY += drawBulletSectionAt(strings.labelFocus, stage.focus, MARGIN + CARD_PADDING, cursorY, contentWidth);
            cursorY += drawBulletSectionAt(STAGE_LABEL_ACTIVITIES, stage.activities, MARGIN + CARD_PADDING, cursorY, contentWidth);
            y += cardHeight + CARD_GAP;
        }

        private float measureStageCardHeight(StageData stage, float contentWidth) {
            float height = CARD_PADDING;
            height += measureTextHeight(stage.name, boldPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL);
            height += LINE_GAP;
            height += measureBulletSectionHeight(strings.labelFocus, stage.focus, contentWidth);
            height += measureBulletSectionHeight(STAGE_LABEL_ACTIVITIES, stage.activities, contentWidth);
            height += CARD_PADDING;
            return height;
        }

        protected void drawSectionHeader(String title) {
            drawSectionHeader(title, sectionFillPaint, Layout.Alignment.ALIGN_NORMAL);
        }

        protected void drawSectionHeader(String title, Paint backgroundPaint) {
            drawSectionHeader(title, backgroundPaint, Layout.Alignment.ALIGN_NORMAL);
        }

        protected void drawAccentSectionHeader(String title) {
            drawSectionHeader(title, accentSectionFillPaint, Layout.Alignment.ALIGN_CENTER);
        }

        private void drawSectionHeader(String title, Paint backgroundPaint, Layout.Alignment alignment) {
            float contentWidth = getContentWidth();
            float textHeight = measureTextHeight(title, boldPaint, contentWidth - SECTION_PADDING * 2f, alignment);
            float boxHeight = textHeight + SECTION_PADDING * 2f;
            ensureSpace(boxHeight + SECTION_GAP);
            if (!measureOnly) {
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight, backgroundPaint);
                drawTextBlock(title, boldPaint, MARGIN + SECTION_PADDING, y + SECTION_PADDING,
                        contentWidth - SECTION_PADDING * 2f, alignment);
            }
            y += boxHeight + SECTION_GAP;
        }

        protected void drawSubHeader(String title) {
            float height = measureTextHeight(title, boldPaint, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            ensureSpace(height + LINE_GAP);
            drawTextBlock(title, boldPaint, MARGIN, y, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            y += height + LINE_GAP;
        }

        private void drawSubHeaderPlain(String title) {
            float height = measureTextHeight(title, bodyPaint, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            ensureSpace(height + LINE_GAP);
            drawTextBlock(title, bodyPaint, MARGIN, y, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            y += height + LINE_GAP;
        }

        protected void drawParagraph(String text) {
            if (safeText(text).isEmpty()) {
                return;
            }
            float height = measureTextHeight(text, bodyPaint, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            ensureSpace(height + LINE_GAP);
            drawTextBlock(text, bodyPaint, MARGIN, y, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            y += height + LINE_GAP;
        }

        private void drawLabelValue(String label, String value) {
            String text = label + strings.labelSeparator + valueOrPlaceholder(value);
            float height = measureTextHeight(text, bodyPaint, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            ensureSpace(height + LINE_GAP);
            drawTextBlock(text, bodyPaint, MARGIN, y, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
            y += height + LINE_GAP;
        }

        protected void drawInfoLine(String label, String value) {
            drawLabelValue(label, value);
        }

        private void drawBulletLine(String line) {
            float indentX = MARGIN + BULLET_INDENT;
            float width = getContentWidth() - BULLET_INDENT;
            String value = sanitizeBulletText(line);
            if (value.isEmpty()) {
                return;
            }
            float height = measureBulletItemHeight(value, width);
            ensureSpace(height + LINE_GAP);
            drawBulletItem(value, indentX, y, width);
            y += height + LINE_GAP;
        }

        protected void drawBulletSection(String title, List<String> items, boolean showPlaceholder) {
            if (items == null) {
                items = new ArrayList<>();
            }
            if (items.isEmpty() && !showPlaceholder) {
                return;
            }
            if (!TextUtils.isEmpty(title)) {
                float titleHeight = measureTextHeight(title, boldPaint, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
                ensureSpace(titleHeight + LINE_GAP);
                drawTextBlock(title, boldPaint, MARGIN, y, getContentWidth(), Layout.Alignment.ALIGN_NORMAL);
                y += titleHeight + LINE_GAP;
            }
            if (items.isEmpty()) {
                items = new ArrayList<>();
                items.add(placeholderText());
            }
            for (String item : items) {
                String value = sanitizeBulletText(safeText(item));
                if (value.isEmpty()) {
                    value = placeholderText();
                }
                float indentX = MARGIN + BULLET_INDENT;
                float width = getContentWidth() - BULLET_INDENT;
                float height = measureBulletItemHeight(value, width);
                ensureSpace(height + LINE_GAP);
                drawBulletItem(value, indentX, y, width);
                y += height + LINE_GAP;
            }
        }

        protected void drawHighlightedParagraphBlock(String text) {
            String value = safeText(text);
            if (value.isEmpty()) {
                value = placeholderText();
            }
            float innerWidth = getContentWidth() - INFO_BLOCK_PADDING * 2f;
            float textHeight = measureTextHeight(value, bodyPaint, innerWidth, Layout.Alignment.ALIGN_NORMAL);
            float blockHeight = INFO_BLOCK_PADDING * 2f + textHeight;
            ensureSpace(blockHeight + LINE_GAP);
            if (!measureOnly) {
                RectF rect = new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + blockHeight);
                canvas.drawRoundRect(rect, INFO_BLOCK_RADIUS, INFO_BLOCK_RADIUS, sectionFillPaint);
                canvas.drawRoundRect(rect, INFO_BLOCK_RADIUS, INFO_BLOCK_RADIUS, cardStrokePaint);
                drawTextBlock(value, bodyPaint, MARGIN + INFO_BLOCK_PADDING, y + INFO_BLOCK_PADDING,
                        innerWidth, Layout.Alignment.ALIGN_NORMAL);
            }
            y += blockHeight + LINE_GAP;
        }

        protected void drawHighlightedBulletBlock(List<String> items) {
            List<String> lines = items == null ? new ArrayList<>() : new ArrayList<>(items);
            if (lines.isEmpty()) {
                lines.add(placeholderText());
            }
            float innerWidth = getContentWidth() - INFO_BLOCK_PADDING * 2f;
            float blockHeight = INFO_BLOCK_PADDING;
            blockHeight += measureBulletSectionHeight(null, lines, innerWidth);
            blockHeight += INFO_BLOCK_PADDING;
            ensureSpace(blockHeight + LINE_GAP);
            if (!measureOnly) {
                RectF rect = new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + blockHeight);
                canvas.drawRoundRect(rect, INFO_BLOCK_RADIUS, INFO_BLOCK_RADIUS, sectionFillPaint);
                canvas.drawRoundRect(rect, INFO_BLOCK_RADIUS, INFO_BLOCK_RADIUS, cardStrokePaint);
            }
            float cursorY = y + INFO_BLOCK_PADDING;
            cursorY += drawBulletSectionAt(null, lines, MARGIN + INFO_BLOCK_PADDING, cursorY, innerWidth);
            y += blockHeight + LINE_GAP;
        }

        private float drawBulletSectionAt(String title, List<String> items, float x, float startY, float width) {
            float used = 0f;
            if (!TextUtils.isEmpty(title)) {
                float titleHeight = measureTextHeight(title, boldPaint, width, Layout.Alignment.ALIGN_NORMAL);
                if (!measureOnly) {
                    drawTextBlock(title, boldPaint, x, startY + used, width, Layout.Alignment.ALIGN_NORMAL);
                }
                used += titleHeight + LINE_GAP;
            }
            if (items == null || items.isEmpty()) {
                items = new ArrayList<>();
                items.add(placeholderText());
            }
            float listX = x + BULLET_INDENT;
            float listWidth = width - BULLET_INDENT;
            for (String item : items) {
                String value = sanitizeBulletText(safeText(item));
                if (value.isEmpty()) {
                    value = placeholderText();
                }
                float itemHeight = measureBulletItemHeight(value, listWidth);
                if (!measureOnly) {
                    drawBulletItem(value, listX, startY + used, listWidth);
                }
                used += itemHeight + LINE_GAP;
            }
            return used;
        }

        private float measureBulletSectionHeight(String title, List<String> items, float width) {
            float height = 0f;
            if (!TextUtils.isEmpty(title)) {
                height += measureTextHeight(title, boldPaint, width, Layout.Alignment.ALIGN_NORMAL) + LINE_GAP;
            }
            if (items == null || items.isEmpty()) {
                items = new ArrayList<>();
                items.add(placeholderText());
            }
            float listWidth = width - BULLET_INDENT;
            for (String item : items) {
                String value = sanitizeBulletText(safeText(item));
                if (value.isEmpty()) {
                    value = placeholderText();
                }
                height += measureBulletItemHeight(value, listWidth) + LINE_GAP;
            }
            return height;
        }

        private float measureBulletItemHeight(String text, float width) {
            if (TextUtils.isEmpty(text)) {
                return 0f;
            }
            float bulletWidth = bodyPaint.measureText(BULLET_SYMBOL) + BULLET_GAP;
            float textWidth = Math.max(1f, width - bulletWidth);
            return measureTextHeight(text, bodyPaint, textWidth, Layout.Alignment.ALIGN_NORMAL);
        }

        private void drawBulletItem(String text, float x, float y, float width) {
            if (TextUtils.isEmpty(text)) {
                return;
            }
            float bulletWidth = bodyPaint.measureText(BULLET_SYMBOL) + BULLET_GAP;
            float textWidth = Math.max(1f, width - bulletWidth);
            if (!measureOnly) {
                Paint.FontMetrics fm = bodyPaint.getFontMetrics();
                float baseline = y - fm.ascent;
                canvas.drawText(BULLET_SYMBOL, x, baseline, bodyPaint);
            }
            drawTextBlock(text, bodyPaint, x + bulletWidth, y, textWidth, Layout.Alignment.ALIGN_NORMAL);
        }

        private void drawKeyValueGrid(List<Field> fields) {
            float contentWidth = getContentWidth();
            float columnWidth = (contentWidth - GRID_GAP) / 2f;
            Field pending = null;
            StaticLayout pendingLayout = null;
            for (Field field : fields) {
                String valueText = field.allowEmpty ? safeText(field.value) : valueOrPlaceholder(field.value);
                String text = field.label + strings.labelSeparator + valueText;
                TextPaint paint = field.bold ? boldPaint : bodyPaint;
                StaticLayout layout = buildStaticLayout(text, paint, (int) (field.span == 2 ? contentWidth : columnWidth),
                        Layout.Alignment.ALIGN_NORMAL);
                if (field.span == 2) {
                    if (pending != null) {
                        float height = pendingLayout.getHeight();
                        ensureSpace(height + LINE_GAP);
                        drawLayout(pendingLayout, MARGIN, y);
                        y += height + LINE_GAP;
                        pending = null;
                        pendingLayout = null;
                    }
                    float height = layout.getHeight();
                    ensureSpace(height + LINE_GAP);
                    drawLayout(layout, MARGIN, y);
                    y += height + LINE_GAP;
                } else if (pending == null) {
                    pending = field;
                    pendingLayout = layout;
                } else {
                    float rowHeight = Math.max(pendingLayout.getHeight(), layout.getHeight());
                    ensureSpace(rowHeight + LINE_GAP);
                    drawLayout(pendingLayout, MARGIN, y);
                    drawLayout(layout, MARGIN + columnWidth + GRID_GAP, y);
                    y += rowHeight + LINE_GAP;
                    pending = null;
                    pendingLayout = null;
                }
            }
            if (pendingLayout != null) {
                float height = pendingLayout.getHeight();
                ensureSpace(height + LINE_GAP);
                drawLayout(pendingLayout, MARGIN, y);
                y += height + LINE_GAP;
            }
        }

        private String infoValue(String key) {
            if (info == null || key == null) {
                return placeholderText();
            }
            String value = safeText(info.optString(key, ""));
            return value.isEmpty() ? placeholderText() : value;
        }

        private String valueOrPlaceholder(String value) {
            String text = safeText(value);
            return text.isEmpty() ? placeholderText() : text;
        }

        protected String placeholderText() {
            String text = safeText(strings.placeholder);
            if (text.isEmpty() || "???".equals(text) || "-".equals(text)) {
                return PLACEHOLDER_TEXT;
            }
            return text;
        }

        private String formatAgeDisplay(int ageMonths) {
            if (ageMonths <= 0) {
                return AGE_MISSING_TEXT;
            }
            int years = ageMonths / 12;
            int months = ageMonths % 12;
            return years + "岁" + months + "个月";
        }

        private JSONObject resolveCaseSummary(JSONObject summaryData) {
            if (summaryData == null) {
                return null;
            }
            JSONObject summary = summaryData.optJSONObject("case_summary");
            return summary == null ? summaryData : summary;
        }

        private List<MemberInfo> getFamilyMembers() {
            List<MemberInfo> members = new ArrayList<>();
            JSONArray array = info == null ? null : info.optJSONArray("familyMembers");
            if (array == null) {
                return members;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String name = safeText(item.optString("member_name", ""));
                String relation = safeText(item.optString("relation", ""));
                String phone = safeText(item.optString("member_phone", ""));
                String occupation = safeText(item.optString("occupation", ""));
                String education = safeText(item.optString("education", ""));
                if (name.isEmpty() && relation.isEmpty() && phone.isEmpty() && occupation.isEmpty() && education.isEmpty()) {
                    continue;
                }
                members.add(new MemberInfo(name, relation, phone, occupation, education));
            }
            return members;
        }

        protected void drawFamilyMembersSection(String title, String emptyText) {
            drawAccentSectionHeader(title);
            List<MemberInfo> members = getFamilyMembers();
            if (members.isEmpty()) {
                drawParagraph(safeText(emptyText).isEmpty() ? strings.familyEmpty : emptyText);
                return;
            }
            drawFamilyMembersCard(members);
        }

        protected List<String> getList(JSONObject obj, String key) {
            List<String> result = new ArrayList<>();
            if (obj == null || key == null) {
                return result;
            }
            JSONArray array = obj.optJSONArray(key);
            if (array == null) {
                return result;
            }
            for (int i = 0; i < array.length(); i++) {
                String value = safeText(array.optString(i, ""));
                if (!value.isEmpty()) {
                    result.add(value);
                }
            }
            return result;
        }

        private boolean hasModuleContent(JSONObject module) {
            if (module == null) {
                return false;
            }
            return !getList(module, "targets").isEmpty()
                    || !getList(module, "methods").isEmpty()
                    || !getList(module, "sample_activities").isEmpty()
                    || !getList(module, "activities").isEmpty()
                    || !getList(module, "home_practice").isEmpty()
                    || !getList(module, "metrics").isEmpty()
                    || hasStages(module.optJSONArray("stages"));
        }

        private boolean hasStages(JSONArray stages) {
            if (stages == null || stages.length() == 0) {
                return false;
            }
            for (int i = 0; i < stages.length(); i++) {
                JSONObject stage = stages.optJSONObject(i);
                if (stage == null) {
                    continue;
                }
                if (!getList(stage, "focus").isEmpty()
                        || !getList(stage, "activities").isEmpty()
                        || !getList(stage, "home_practice").isEmpty()
                        || !getList(stage, "metrics").isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        private float measureTextHeight(String text, TextPaint paint, float width, Layout.Alignment alignment) {
            if (TextUtils.isEmpty(text)) {
                return 0f;
            }
            StaticLayout layout = buildStaticLayout(text, paint, (int) width, alignment);
            return layout.getHeight();
        }

        private float drawTextBlock(String text, TextPaint paint, float x, float y, float width, Layout.Alignment alignment) {
            if (TextUtils.isEmpty(text)) {
                return 0f;
            }
            StaticLayout layout = buildStaticLayout(text, paint, (int) width, alignment);
            drawLayout(layout, x, y);
            return layout.getHeight();
        }

        private void drawLayout(StaticLayout layout, float x, float y) {
            if (measureOnly || layout == null) {
                return;
            }
            canvas.save();
            canvas.translate(x, y);
            layout.draw(canvas);
            canvas.restore();
        }

        private StaticLayout buildStaticLayout(String text, TextPaint paint, int width, Layout.Alignment alignment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                        .setAlignment(alignment)
                        .setLineSpacing(0f, 1.25f)
                        .setIncludePad(false)
                        .build();
            }
            return new StaticLayout(text, paint, width, alignment, 1.25f, 0f, false);
        }

        protected String safeText(String value) {
            return value == null ? "" : value.trim();
        }

        private static final class Field {
            final String label;
            final String value;
            final int span;
            final boolean bold;
            final boolean allowEmpty;

            Field(String label, String value, int span, boolean bold) {
                this(label, value, span, bold, false);
            }

            Field(String label, String value, int span, boolean bold, boolean allowEmpty) {
                this.label = label;
                this.value = value;
                this.span = span;
                this.bold = bold;
                this.allowEmpty = allowEmpty;
            }
        }

        private static final class MemberInfo {
            final String name;
            final String relation;
            final String phone;
            final String occupation;
            final String education;

            MemberInfo(String name, String relation, String phone, String occupation, String education) {
                this.name = name;
                this.relation = relation;
                this.phone = phone;
                this.occupation = occupation;
                this.education = education;
            }
        }

        private static final class StageData {
            final String name;
            final List<String> focus;
            final List<String> activities;
            final List<String> metrics;

            StageData(String name, List<String> focus, List<String> activities, List<String> metrics) {
                this.name = name;
                this.focus = focus;
                this.activities = activities;
                this.metrics = metrics;
            }
        }
    }

    private static final class ChildInfoPdfRenderer extends TreatmentPlanPdfRenderer {
        private static final String TAG = "PdfGenerator";
        private static final float IMAGE_MAX_HEIGHT = 220f;
        private static final float IMAGE_SIDE_MARGIN = 24f;
        private static final float IMAGE_FRAME_PADDING = 8f;
        private static final float IMAGE_ITEM_GAP = 16f;
        private static final float IMAGE_LABEL_GAP = 6f;

        private final JSONObject backgroundInfo;
        private final Paint imageBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint imageFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint imageBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        ChildInfoPdfRenderer(JSONObject info, Typeface chineseTypeface, TreatmentPlanStrings strings) {
            super(info, new JSONObject(), chineseTypeface, strings);
            JSONObject bg = info == null ? null : info.optJSONObject("backgroundInfo");
            this.backgroundInfo = bg == null ? new JSONObject() : bg;
            imageBorderPaint.setColor(0xFFDDDDDD);
            imageBorderPaint.setStyle(Paint.Style.STROKE);
            imageBorderPaint.setStrokeWidth(1f);
            imageFillPaint.setColor(0xFFFDFDFD);
            imageFillPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected String getDocumentTitle() {
            return "儿童信息档案";
        }

        @Override
        protected void drawLeadingSections() {
            drawAccentSectionHeader("儿童基本信息");
            drawInfoLine(strings.labelName, info == null ? "" : info.optString("name", ""));
            drawInfoLine(strings.labelGender, info == null ? "" : info.optString("gender", ""));
            drawInfoLine(strings.labelBirthDate, info == null ? "" : info.optString("birthDate", ""));
            drawInfoLine("年龄", calculateAgeText(info == null ? "" : info.optString("birthDate", "")));
            drawInfoLine(getStringRes(R.string.pdf_label_test_date), info == null ? "" : info.optString("testDate", ""));
            drawInfoLine(getStringRes(R.string.pdf_label_examiner), info == null ? "" : info.optString("examiner", ""));

            drawAccentSectionHeader("联系方式");
            drawInfoLine(strings.labelPhone, info == null ? "" : info.optString("phone", ""));
            drawInfoLine(strings.labelAddress, info == null ? "" : info.optString("address", ""));

            drawFamilyMembersSection("家庭成员", "暂无家庭成员信息");
        }

        @Override
        protected void drawDocumentBody() {
            drawAccentSectionHeader("儿童详细信息");
            drawBackgroundSection("基本照护", buildBasicCareLines(optObject(backgroundInfo, "basicCare")));
            drawBackgroundSection("出生史", buildBirthHistoryLines(optObject(backgroundInfo, "birthHistory")));
            drawBackgroundSection("生长发育", buildGrowthDevelopmentLines(optObject(backgroundInfo, "growthDevelopment")));
            drawBackgroundSection("已诊断疾病", buildDiagnosedDisordersLines(optObject(backgroundInfo, "diagnosedDisorders")));
            drawBackgroundSection("一般发展", buildGeneralDevelopmentLines(optObject(backgroundInfo, "generalDevelopment")));
            drawBackgroundSection("言语运动功能", buildSpeechMotorFunctionLines(optObject(backgroundInfo, "speechMotorFunction")));
            drawBackgroundSection("表达方式", buildExpressionModeLines(optObject(backgroundInfo, "expressionMode")));
            drawBackgroundSection("语言相关担忧", buildLanguageConcernLines(optObject(backgroundInfo, "languageConcern")));
            drawMedicalDocumentsSection(MedicalDiagnosisImageHelper.optMedicalDocuments(backgroundInfo));
        }

        private void drawBackgroundSection(String title, List<String> lines) {
            drawSubHeader(title);
            if (lines == null || lines.isEmpty()) {
                drawParagraph(placeholderText());
                return;
            }
            for (String line : lines) {
                drawParagraph(line);
            }
        }

        private void drawMedicalDocumentsSection(JSONArray medicalDocuments) {
            drawSubHeader("医学资料");
            List<String> lines = new ArrayList<>();
            int count = medicalDocuments == null ? 0 : medicalDocuments.length();
            lines.add("图片数量：" + count);
            // if (count <= 0) {
            //     lines.add("文件名列表：暂无");
            // } else {
            //     lines.add("文件名列表：");
            // }
            for (String line : lines) {
                drawParagraph(line);
            }
            if (count > 0) {
                List<String> fileNames = new ArrayList<>();
                for (int i = 0; i < medicalDocuments.length(); i++) {
                    JSONObject item = medicalDocuments.optJSONObject(i);
                    String fileName = MedicalDiagnosisImageHelper.getFileName(item);
                    fileNames.add(fileName.isEmpty() ? "未命名文件" : fileName);
                }
                drawBulletSection(null, fileNames, false);
                drawSubHeader("图片预览");
                drawMedicalDocumentImages(medicalDocuments);
            }
        }

        private void drawMedicalDocumentImages(JSONArray medicalDocuments) {
            if (medicalDocuments == null || medicalDocuments.length() == 0) {
                return;
            }
            for (int i = 0; i < medicalDocuments.length(); i++) {
                JSONObject item = medicalDocuments.optJSONObject(i);
                String displayName = item == null ? "" : MedicalDiagnosisImageHelper.getFileName(item);
                if (displayName.isEmpty()) {
                    displayName = "未命名文件";
                }
                String localPath = item == null ? "" : MedicalDiagnosisImageHelper.getLocalPath(item);
                drawSingleMedicalImage(displayName, localPath, i + 1);
            }
        }

        private void drawSingleMedicalImage(String displayName, String localPath, int index) {
            String title = safeText(displayName);
            if (title.isEmpty()) {
                title = "医学资料图片 " + index;
            }

            float contentWidth = getRendererContentWidth();
            float titleHeight = measureRendererBodyTextHeight(title, contentWidth);
            float maxImageWidth = contentWidth - IMAGE_SIDE_MARGIN;
            Bitmap bitmap = null;
            try {
                bitmap = decodeScaledBitmap(localPath, maxImageWidth, IMAGE_MAX_HEIGHT);
                ImageLayoutResult layoutResult = bitmap == null
                        ? new ImageLayoutResult(Math.min(maxImageWidth, 160f), 44f)
                        : computeImageBounds(bitmap.getWidth(), bitmap.getHeight(), maxImageWidth, IMAGE_MAX_HEIGHT);

                float frameWidth = layoutResult.drawWidth + IMAGE_FRAME_PADDING * 2f;
                float frameHeight = layoutResult.drawHeight + IMAGE_FRAME_PADDING * 2f;
                float requiredHeight = titleHeight + IMAGE_LABEL_GAP + frameHeight + IMAGE_ITEM_GAP;
                ensureRendererSpace(requiredHeight);

                drawRendererBodyTextBlock(title, getRendererMargin(), getRendererCursorY(), contentWidth, Layout.Alignment.ALIGN_NORMAL);
                advanceRendererCursor(titleHeight + IMAGE_LABEL_GAP);

                float frameLeft = getRendererMargin() + (contentWidth - frameWidth) / 2f;
                float frameTop = getRendererCursorY();
                RectF frameRect = new RectF(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight);
                if (!isRendererMeasureOnly()) {
                    Canvas rendererCanvas = getRendererCanvas();
                    rendererCanvas.drawRoundRect(frameRect, 6f, 6f, imageFillPaint);
                    rendererCanvas.drawRoundRect(frameRect, 6f, 6f, imageBorderPaint);
                    if (bitmap != null) {
                        RectF imageRect = new RectF(
                                frameLeft + IMAGE_FRAME_PADDING,
                                frameTop + IMAGE_FRAME_PADDING,
                                frameLeft + IMAGE_FRAME_PADDING + layoutResult.drawWidth,
                                frameTop + IMAGE_FRAME_PADDING + layoutResult.drawHeight
                        );
                        rendererCanvas.drawBitmap(bitmap, null, imageRect, imageBitmapPaint);
                    } else {
                        drawRendererBodyTextBlock("图片加载失败：" + title,
                                frameLeft + IMAGE_FRAME_PADDING,
                                frameTop + IMAGE_FRAME_PADDING,
                                frameWidth - IMAGE_FRAME_PADDING * 2f,
                                Layout.Alignment.ALIGN_CENTER);
                    }
                }
                advanceRendererCursor(frameHeight + IMAGE_ITEM_GAP);
            } finally {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }

        private Bitmap decodeScaledBitmap(String localPath, float maxWidth, float maxHeight) {
            String path = safeText(localPath);
            if (path.isEmpty()) {
                Log.e(TAG, "child info pdf image skipped: empty localPath");
                return null;
            }
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                Log.e(TAG, "child info pdf image skipped: file missing, path=" + path);
                return null;
            }

            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, boundsOptions);
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                Log.e(TAG, "child info pdf image skipped: invalid bounds, path=" + path);
                return null;
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = computeInSampleSize(
                    boundsOptions.outWidth,
                    boundsOptions.outHeight,
                    maxWidth,
                    maxHeight
            );
            decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(path, decodeOptions);
                if (bitmap == null) {
                    Log.e(TAG, "child info pdf image skipped: decode returned null, path=" + path);
                }
                return bitmap;
            } catch (OutOfMemoryError error) {
                Log.e(TAG, "child info pdf image skipped: oom, path=" + path, error);
                return null;
            } catch (Exception e) {
                Log.e(TAG, "child info pdf image skipped: decode failed, path=" + path, e);
                return null;
            }
        }

        private ImageLayoutResult computeImageBounds(int rawWidth, int rawHeight, float maxWidth, float maxHeight) {
            if (rawWidth <= 0 || rawHeight <= 0) {
                return new ImageLayoutResult(Math.min(maxWidth, 120f), 44f);
            }
            float scale = Math.min(Math.min(maxWidth / rawWidth, maxHeight / rawHeight), 1f);
            return new ImageLayoutResult(rawWidth * scale, rawHeight * scale);
        }

        private int computeInSampleSize(int rawWidth, int rawHeight, float maxWidth, float maxHeight) {
            int inSampleSize = 1;
            if (rawWidth <= maxWidth && rawHeight <= maxHeight) {
                return inSampleSize;
            }
            int halfWidth = rawWidth / 2;
            int halfHeight = rawHeight / 2;
            while ((halfWidth / inSampleSize) >= maxWidth && (halfHeight / inSampleSize) >= maxHeight) {
                inSampleSize *= 2;
            }
            return Math.max(1, inSampleSize);
        }

        private static final class ImageLayoutResult {
            final float drawWidth;
            final float drawHeight;

            ImageLayoutResult(float drawWidth, float drawHeight) {
                this.drawWidth = drawWidth;
                this.drawHeight = drawHeight;
            }
        }

        private String calculateAgeText(String birthDate) {
            String value = safeText(birthDate);
            if (value.isEmpty()) {
                return "";
            }
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                Date birth = format.parse(value);
                if (birth == null) {
                    return "";
                }
                Calendar birthCalendar = Calendar.getInstance();
                birthCalendar.setTime(birth);
                Calendar now = Calendar.getInstance();
                int years = now.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
                int months = now.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH);
                int days = now.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH);
                if (days < 0) {
                    months--;
                }
                if (months < 0) {
                    years--;
                    months += 12;
                }
                if (years < 0) {
                    return "";
                }
                return years + "岁" + months + "个月";
            } catch (Exception ignored) {
                return "";
            }
        }
    }

    private static JSONObject optObject(JSONObject object, String key) {
        if (object == null) {
            return new JSONObject();
        }
        JSONObject result = object.optJSONObject(key);
        return result == null ? new JSONObject() : result;
    }

    private static List<String> buildBasicCareLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("0-3岁主要照护者", joinSelected(
                selectedLabel(section.optBoolean("caregiver0To3Parents"), "父母"),
                selectedLabel(section.optBoolean("caregiver0To3Grandparents"), "祖父母"),
                selectedLabel(section.optBoolean("caregiver0To3Nanny"), "保姆"),
                section.optBoolean("caregiver0To3Other") ? appendDetail("其他", section.optString("caregiver0To3OtherText")) : null
        )));
        lines.add(labelValue("0-3岁主要语言环境", section.optString("language0To3")));
        lines.add(labelValue("3-6岁主要照护者", joinSelected(
                selectedLabel(section.optBoolean("caregiver3To6Parents"), "父母"),
                selectedLabel(section.optBoolean("caregiver3To6Grandparents"), "祖父母"),
                selectedLabel(section.optBoolean("caregiver3To6Nanny"), "保姆"),
                section.optBoolean("caregiver3To6Other") ? appendDetail("其他", section.optString("caregiver3To6OtherText")) : null
        )));
        lines.add(labelValue("3-6岁主要语言环境", section.optString("language3To6")));
        lines.add(labelValue("方言环境", section.optString("dialect")));
        return lines;
    }

    private static List<String> buildBirthHistoryLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("生产方式", translateEnum(section.optString("deliveryMethod"))));
        lines.add(labelValue("中耳炎", boolText(section.optBoolean("otitisMedia"))));
        lines.add(labelValue("呼吸系统疾病", boolText(section.optBoolean("respiratoryDisease"))));
        lines.add(labelValue("头部外伤", boolText(section.optBoolean("headInjury"))));
        lines.add(labelValue("癫痫", boolText(section.optBoolean("epilepsy"))));
        lines.add(labelValue("低出生体重", boolText(section.optBoolean("lowWeight"))));
        lines.add(labelValue("出生体重低于2000克", boolText(section.optBoolean("lowWeightBelow2000"))));
        lines.add(labelValue("出生体重低于1500克", boolText(section.optBoolean("lowWeightBelow1500"))));
        lines.add(labelValue("入住保温箱", boolText(section.optBoolean("incubator"))));
        lines.add(labelValue("保温箱天数", section.optString("incubatorDays")));
        lines.add(labelValue("黄疸", boolText(section.optBoolean("jaundice"))));
        lines.add(labelValue("脑膜炎", boolText(section.optBoolean("meningitis"))));
        lines.add(labelValue("唇腭裂", boolText(section.optBoolean("cleftLipPalate"))));
        lines.add(labelValue("脐带绕颈", boolText(section.optBoolean("umbilicalCordNeck"))));
        lines.add(labelValue("缺氧", boolText(section.optBoolean("hypoxia"))));
        lines.add(labelValue("长期用药", boolText(section.optBoolean("medication"))));
        lines.add(labelValue("用药说明", section.optString("medicationText")));
        lines.add(labelValue("其他情况", boolText(section.optBoolean("other"))));
        lines.add(labelValue("其他说明", section.optString("otherText")));
        return lines;
    }

    private static List<String> buildGrowthDevelopmentLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("喂养方式", translateEnum(section.optString("feedingMethod"))));
        lines.add(labelValue("微笑发育", translateEnum(section.optString("smileStatus"))));
        lines.add(labelValue("独坐发育", translateEnum(section.optString("sitStatus"))));
        lines.add(labelValue("抬头发育", translateEnum(section.optString("headControlStatus"))));
        lines.add(labelValue("爬行发育", translateEnum(section.optString("crawlStatus"))));
        lines.add(labelValue("独走发育", translateEnum(section.optString("walkStatus"))));
        lines.add(labelValue("咿呀发声", translateEnum(section.optString("vocalizationStatus"))));
        lines.add(labelValue("单词表达", translateEnum(section.optString("singleWordStatus"))));
        lines.add(labelValue("短语表达", translateEnum(section.optString("phraseStatus"))));
        return lines;
    }

    private static List<String> buildDiagnosedDisordersLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("无明确诊断", boolText(section.optBoolean("none"))));
        lines.add(labelValue("发育迟缓", boolText(section.optBoolean("developmentalDelay"))));
        lines.add(labelValue("脑瘫", boolText(section.optBoolean("cerebralPalsy"))));
        lines.add(labelValue("孤独症", boolText(section.optBoolean("autism"))));
        lines.add(labelValue("唐氏综合征", boolText(section.optBoolean("downSyndrome"))));
        lines.add(labelValue("智力障碍", boolText(section.optBoolean("intellectualDisability"))));
        lines.add(labelValue("注意缺陷多动障碍", boolText(section.optBoolean("adhd"))));
        lines.add(labelValue("其他诊断", boolText(section.optBoolean("other"))));
        lines.add(labelValue("其他诊断说明", section.optString("otherText")));
        return lines;
    }

    private static List<String> buildGeneralDevelopmentLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("视觉情况", translateEnum(section.optString("visionStatus"))));
        lines.add(labelValue("听觉情况", translateEnum(section.optString("hearingStatus"))));
        lines.add(labelValue("进食习惯", translateEnum(section.optString("eatingHabitStatus"))));
        lines.add(labelValue("咀嚼困难", boolText(section.optBoolean("eatingHabitChewingDifficulty"))));
        lines.add(labelValue("吞咽困难", boolText(section.optBoolean("eatingHabitSwallowingDifficulty"))));
        return lines;
    }

    private static List<String> buildSpeechMotorFunctionLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("唇部功能", translateEnum(section.optString("lipsStatus"))));
        lines.add(labelValue("舌部功能", translateEnum(section.optString("tongueStatus"))));
        lines.add(labelValue("下颌功能", translateEnum(section.optString("jawStatus"))));
        lines.add(labelValue("腭咽功能", translateEnum(section.optString("velopharyngealStatus"))));
        lines.add(labelValue("轮替运动", translateEnum(section.optString("alternatingMotionStatus"))));
        lines.add(labelValue("流涎控制", translateEnum(section.optString("salivaControlStatus"))));
        lines.add(labelValue("呼吸功能", translateEnum(section.optString("breathingStatus"))));
        lines.add(labelValue("嗓音情况", translateEnum(section.optString("voiceStatus"))));
        lines.add(labelValue("言语进食功能", translateEnum(section.optString("speechEatingStatus"))));
        lines.add(labelValue("咀嚼困难", boolText(section.optBoolean("speechEatingChewingDifficulty"))));
        lines.add(labelValue("吞咽困难", boolText(section.optBoolean("speechEatingSwallowingDifficulty"))));
        lines.add(labelValue("其他说明", section.optString("other")));
        return lines;
    }

    private static List<String> buildExpressionModeLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("口语表达", boolText(section.optBoolean("spokenLanguage"))));
        lines.add(labelValue("非口语表达", boolText(section.optBoolean("nonverbal"))));
        lines.add(labelValue("音调变化表达", boolText(section.optBoolean("nonverbalVoicePitch"))));
        lines.add(labelValue("身体动作表达", boolText(section.optBoolean("nonverbalBodyLanguage"))));
        lines.add(labelValue("辅助设备表达", boolText(section.optBoolean("nonverbalAssistiveDevice"))));
        lines.add(labelValue("辅助设备说明", section.optString("nonverbalAssistiveDeviceText")));
        return lines;
    }

    private static List<String> buildLanguageConcernLines(JSONObject section) {
        List<String> lines = new ArrayList<>();
        lines.add(labelValue("两岁前是否会说词汇", translateEnum(section.optString("vocabByTwoYears"))));
        lines.add(labelValue("两岁半前是否会说句子", translateEnum(section.optString("sentenceByTwoHalfYears"))));
        lines.add(labelValue("家长认为语言正常", boolText(section.optBoolean("parentConcernNormal"))));
        lines.add(labelValue("家长担心不会说话", boolText(section.optBoolean("parentConcernCannotSpeak"))));
        lines.add(labelValue("家长担心说话不清", boolText(section.optBoolean("parentConcernUnclearSpeech"))));
        lines.add(labelValue("家长担心听不懂", boolText(section.optBoolean("parentConcernCannotUnderstand"))));
        lines.add(labelValue("家长担心反应慢", boolText(section.optBoolean("parentConcernSlowResponse"))));
        lines.add(labelValue("家长主要诉求", section.optString("parentPrimaryRequest")));
        return lines;
    }

    private static String translateEnum(String value) {
        String key = value == null ? "" : value.trim();
        if (key.isEmpty()) {
            return "";
        }
        if ("natural".equals(key)) {
            return "自然分娩";
        }
        if ("premature".equals(key)) {
            return "早产";
        }
        if ("cesarean".equals(key)) {
            return "剖宫产";
        }
        if ("major_illness".equals(key)) {
            return "重大疾病史";
        }
        if ("breast".equals(key)) {
            return "母乳喂养";
        }
        if ("formula".equals(key)) {
            return "配方奶喂养";
        }
        if ("normal".equals(key)) {
            return "正常";
        }
        if ("delayed".equals(key)) {
            return "发育迟缓";
        }
        if ("abnormal".equals(key)) {
            return "异常";
        }
        if ("yes".equals(key)) {
            return "是";
        }
        if ("no".equals(key)) {
            return "否";
        }
        return key;
    }

    private static String boolText(boolean value) {
        return value ? "是" : "否";
    }

    private static String labelValue(String label, String value) {
        String safeLabel = label == null ? "" : label.trim();
        String safeValue = value == null ? "" : value.trim();
        if (safeValue.isEmpty()) {
            safeValue = "未填写";
        }
        return safeLabel + "：" + safeValue;
    }

    private static String selectedLabel(boolean selected, String label) {
        return selected ? label : null;
    }

    private static String appendDetail(String label, String detail) {
        String safeDetail = detail == null ? "" : detail.trim();
        return safeDetail.isEmpty() ? label : label + "（" + safeDetail + "）";
    }

    private static String joinSelected(String... values) {
        List<String> parts = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    parts.add(value.trim());
                }
            }
        }
        if (parts.isEmpty()) {
            return "暂无";
        }
        return TextUtils.join("、", parts);
    }

    private static final class OverallInterventionPdfRenderer extends TreatmentPlanPdfRenderer {
        private static final String DEFAULT_BODY_TITLE = "总体干预报告";
        private static final String SECTION_TITLE_PRIORITY_FOCUS = "需要重点关注的能力";
        private static final String SECTION_TITLE_PRIORITY_FOCUS_DISPLAY = "【需要重点关注的能力】";
        private static final String SECTION_TITLE_UNSTABLE = "不稳定的能力";
        private static final String SECTION_TITLE_UNSTABLE_DISPLAY = "【不稳定的能力】";
        private static final String SECTION_KEY_ASSESSMENT_RESULT = "assessment_result";

        OverallInterventionPdfRenderer(JSONObject info, JSONObject plan, Typeface chineseTypeface, TreatmentPlanStrings strings) {
            super(info, plan, chineseTypeface, strings);
        }

        @Override
        protected Layout.Alignment getModuleTitleAlignment() {
            return Layout.Alignment.ALIGN_CENTER;
        }

        @Override
        protected void drawDocumentBody() {
            drawAccentSectionHeader(resolveBodyTitle());
            JSONArray modules = plan == null ? null : plan.optJSONArray("modules");
            if (modules == null || modules.length() == 0) {
                drawParagraph(placeholderText());
                return;
            }
            for (int i = 0; i < modules.length(); i++) {
                JSONObject module = modules.optJSONObject(i);
                if (module == null) {
                    continue;
                }
                drawModuleHeader(safeModuleTitle(module), i > 0);
                drawModuleSections(module.optJSONArray("sections"));
            }
        }

        @Override
        protected String getDateText() {
            JSONObject metadata = plan == null ? null : plan.optJSONObject("metadata");
            String date = safeText(metadata == null ? "" : metadata.optString("date", ""));
            if (!date.isEmpty()) {
                return strings.datePrefix + date;
            }
            return super.getDateText();
        }

        private String resolveBodyTitle() {
            JSONObject metadata = plan == null ? null : plan.optJSONObject("metadata");
            String title = safeText(metadata == null ? "" : metadata.optString("reportTitle", ""));
            return title.isEmpty() ? DEFAULT_BODY_TITLE : title;
        }

        private String safeModuleTitle(JSONObject module) {
            String moduleType = safeText(module == null ? "" : module.optString("moduleType", ""));
            if ("prelinguistic".equals(moduleType)) {
                return "前语言模块干预报告";
            }
            if ("social".equals(moduleType)) {
                return "社交模块干预报告";
            }
            if ("vocabulary".equals(moduleType)) {
                return "词汇模块干预报告";
            }
            if ("syntax".equals(moduleType)) {
                return "句法模块干预报告";
            }
            if ("articulation".equals(moduleType)) {
                return "构音模块干预报告";
            }
            String title = safeText(module == null ? "" : module.optString("moduleTitle", ""));
            return title.isEmpty() ? placeholderText() : title;
        }

        private void drawModuleSections(JSONArray sections) {
            if (sections == null || sections.length() == 0) {
                drawParagraph(placeholderText());
                return;
            }
            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.optJSONObject(i);
                if (section == null) {
                    continue;
                }
                drawSchemaSection(section);
            }
        }

        private void drawSchemaSection(JSONObject section) {
            String title = formatSectionTitle(section);
            if (!title.isEmpty()) {
                drawSubHeader(title);
            }
            String contentType = safeText(section.optString("contentType", ""));
            if (isAssessmentResultSection(section)) {
                if ("bullets".equals(contentType)) {
                    drawHighlightedBulletBlock(resolveSectionItems(section));
                } else {
                    drawHighlightedParagraphBlock(resolveParagraphText(section));
                }
                return;
            }
            if ("bullets".equals(contentType)) {
                drawBulletSection(null, resolveSectionItems(section), true);
                return;
            }
            drawParagraph(resolveParagraphText(section));
        }

        private String formatSectionTitle(JSONObject section) {
            String rawTitle = safeText(section == null ? "" : section.optString("title", ""));
            if (SECTION_TITLE_PRIORITY_FOCUS.equals(rawTitle)) {
                return SECTION_TITLE_PRIORITY_FOCUS_DISPLAY;
            }
            if (SECTION_TITLE_UNSTABLE.equals(rawTitle)) {
                return SECTION_TITLE_UNSTABLE_DISPLAY;
            }
            return rawTitle;
        }

        private boolean isAssessmentResultSection(JSONObject section) {
            String key = safeText(section == null ? "" : section.optString("key", ""));
            return SECTION_KEY_ASSESSMENT_RESULT.equals(key);
        }

        private String resolveParagraphText(JSONObject section) {
            String text = safeText(section == null ? "" : section.optString("text", ""));
            if (!text.isEmpty()) {
                return text;
            }
            List<String> items = getList(section, "items");
            if (items.isEmpty()) {
                return placeholderText();
            }
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                String value = safeText(item);
                if (value.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(value);
            }
            return sb.length() == 0 ? placeholderText() : sb.toString();
        }

        private List<String> resolveSectionItems(JSONObject section) {
            List<String> items = getList(section, "items");
            if (!items.isEmpty()) {
                return items;
            }
            List<String> fallback = new ArrayList<>();
            String text = safeText(section == null ? "" : section.optString("text", ""));
            if (text.isEmpty()) {
                return fallback;
            }
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                String value = safeText(line);
                if (!value.isEmpty()) {
                    fallback.add(value);
                }
            }
            return fallback;
        }
    }

    private static boolean hasFamilyMembers(JSONArray members) {
        if (members == null || members.length() == 0) {
            return false;
        }
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.optJSONObject(i);
            if (member == null) {
                continue;
            }
            if (!member.optString("member_name", "").trim().isEmpty()
                    || !member.optString("relation", "").trim().isEmpty()
                    || !member.optString("member_phone", "").trim().isEmpty()
                    || !member.optString("occupation", "").trim().isEmpty()
                    || !member.optString("education", "").trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void renderTreatmentPlanModules(Document document, JSONObject caseSummary, JSONObject modulePlan, Font bodyFont) throws Exception {
        if (caseSummary != null) {
            document.add(new Paragraph("个案概述", bodyFont));
            addKeyValueLine(document, "主诉", caseSummary.optString("chief_complaint", ""), bodyFont);
            addJsonListWithLabel(document, "关键发现", caseSummary.optJSONArray("key_findings"), bodyFont);
            addJsonListWithLabel(document, "疑似诊断", caseSummary.optJSONArray("suspected_diagnosis"), bodyFont);
            addJsonListWithLabel(document, "风险提示", caseSummary.optJSONArray("risk_flags"), bodyFont);
            document.add(new Paragraph(" ", bodyFont));
        }
        if (modulePlan == null) {
            document.add(new Paragraph("暂无干预计划内容", bodyFont));
            return;
        }
        String[] modules = new String[]{"speech_sound", "prelinguistic", "vocabulary", "syntax", "social_pragmatics"};
        String[] titles = new String[]{"语音/构音", "前语言", "词汇", "句法", "社会交往"};
        for (int i = 0; i < modules.length; i++) {
            JSONObject module = modulePlan.optJSONObject(modules[i]);
            if (module == null) {
                continue;
            }
            document.add(new Paragraph(titles[i], bodyFont));
            addJsonListWithLabel(document, "关键发现", module.optJSONArray("key_findings"), bodyFont);
            addJsonListWithLabel(document, "目标", module.optJSONArray("targets"), bodyFont);
            addJsonListWithLabel(document, "方法", module.optJSONArray("methods"), bodyFont);
            addJsonListWithLabel(document, "活动", module.optJSONArray("activities"), bodyFont);
            addJsonListWithLabel(document, "家庭练习", module.optJSONArray("home_practice"), bodyFont);
            addJsonListWithLabel(document, "评估指标", module.optJSONArray("metrics"), bodyFont);
            document.add(new Paragraph(" ", bodyFont));
        }
    }

    private static void addKeyValueLine(Document document, String label, String value, Font font) throws Exception {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        document.add(new Paragraph(label + "：" + value.trim(), font));
    }

    private static void addJsonListWithLabel(Document document, String label, JSONArray array, Font font) throws Exception {
        if (array == null || array.length() == 0) {
            return;
        }
        document.add(new Paragraph(label + "：", font));
        addJsonList(document, array, font);
    }

    private static void addJsonList(Document document, JSONArray array, Font font) throws Exception {
        if (array == null || array.length() == 0) {
            return;
        }
        for (int i = 0; i < array.length(); i++) {
            String item = array.optString(i, "");
            if (item == null || item.trim().isEmpty()) {
                continue;
            }
            document.add(new Paragraph("- " + item.trim(), font));
        }
    }
}




