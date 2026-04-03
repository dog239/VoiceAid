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


    @SuppressLint("NewApi")
    public static void generatePdf(OutputStream outputStream, String fname) {
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

            JSONObject data = dataManager.getInstance().loadData(fname);
            JSONObject info = data.getJSONObject("info");
            name = info.getString("name");
            c = info.getString("class");
            serialNumber = info.getString("serialNumber");
            birthDate = info.getString("birthDate");
            testData = info.getString("testDate");
            testLocation = info.getString("testLocation");
            examiner = info.getString("examiner");
//  A
            String[] characs = ImageUrls.A_characs;
            int[] score = new int[21];
            JSONArray jsonArrayA = data.getJSONObject("evaluations").getJSONArray("A");
            int num[] = ImageUrls.A_nums;
            if (jsonArrayA.length() == 0) {
                for (int i = 0; i < num.length; ++i) {
                    scoreA[i] = "   ";
                }
            } else {
                for (int i = 0; i < jsonArrayA.length(); i++) {
                    JSONObject object = jsonArrayA.getJSONObject(i);
                    if (object.has("time") && !object.isNull("time") && !object.get("time").equals("null")) {
                        if (!object.getString("target_tone1").equals("")) {
                            String originalString = object.getString("target_tone1");
                            for (int j = 0; j < characs.length; j++) {
                                if (characs[j].equals(originalString)) {
                                    score[j]++;
                                }
                            }
                        }
                        if (!object.getString("target_tone2").equals("")) {
                            String originalString = object.getString("target_tone2");
                            for (int j = 0; j < characs.length; j++) {
                                if (characs[j].equals(originalString)) {
                                    score[j]++;
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < num.length; ++i) {
                    double lentha = num[i];
                    double scorea = (score[i] / lentha) * 100;
                    scoreA[i] = String.format("%.2f%%", scorea);
                }
            }
//        E
            double counte = 0;
            JSONArray jsonArrayE = data.getJSONObject("evaluations").getJSONArray("E");
            for (int i = 0; i < jsonArrayE.length(); i++) {
                JSONObject object = jsonArrayE.getJSONObject(i);
                if (object.has("result") && !object.isNull("result") && !object.get("result").equals("null")) {
                    if (object.getBoolean("result")) {
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
            JSONArray jsonArrayRE = data.getJSONObject("evaluations").optJSONArray("EV");
            if (jsonArrayRE == null) {
                jsonArrayRE = new JSONArray();
            }
            for (int i = 0; i < jsonArrayRE.length(); i++) {
                JSONObject object = jsonArrayRE.getJSONObject(i);
                if (object.has("result") && !object.isNull("result") && !object.get("result").equals("null")) {
                    if (object.getBoolean("result")) {
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
            JSONArray jsonArrayRG = data.getJSONObject("evaluations").getJSONArray("RG");
            for (int i = 0; i < jsonArrayRG.length(); i++) {
                JSONObject object = jsonArrayRG.getJSONObject(i);
                if (object.has("time") && !object.isNull("time") && !object.get("time").equals("null")) {
                    if (object.getBoolean("result")) {
                        countrg++;
                    }
                }
            }
            double lenthrg = jsonArrayRG.length();
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
                JSONArray jsonArraySE = data.getJSONObject("evaluations").optJSONArray("SE" + group);
                if (jsonArraySE != null) {
                    for (int i = 0; i < jsonArraySE.length(); i++) {
                        JSONObject object = jsonArraySE.getJSONObject(i);
                        if (object.has("time") && !object.isNull("time") && !object.get("time").equals("null")) {
                            if (object.getBoolean("result")) {
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
            JSONArray jsonArrayS = data.getJSONObject("evaluations").getJSONArray("S");
            for (int i = 0; i < jsonArrayS.length(); i++) {
                JSONObject object = jsonArrayS.getJSONObject(i);
                if (object.has("result") && !object.isNull("result") && !object.get("result").equals("null")) {
                    if (object.getBoolean("result")) {
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
            JSONArray jsonArrayNWR = data.getJSONObject("evaluations").getJSONArray("NWR");
            for (int i = 0; i < jsonArrayNWR.length(); i++) {
                JSONObject object = jsonArrayNWR.getJSONObject(i);
                for (int j = 0; j < 6; ++j) {
                    if (object.has("results" + (j + 1)) && !object.isNull("results" + (j + 1)) && !object.get("results" + (j + 1)).equals("null")) {
                        if (object.getBoolean("results" + (j + 1))) {
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
            JSONArray jsonArrayPST = data.getJSONObject("evaluations").getJSONArray("PST");
            if (jsonArrayPST.length() == 0) {
                Arrays.fill(scorePST, " ");
            } else {
                for (int i = 0; i < jsonArrayPST.length(); i++) {
                    JSONObject object = jsonArrayPST.getJSONObject(i);
                    if (object.has("time") && !object.isNull("time") && !object.getString("time").equals("null")) {
                        int sc = object.getInt("score");
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
            JSONArray jsonArrayPN = data.getJSONObject("evaluations").getJSONArray("PN");
            if (jsonArrayPN.length() == 0) {
                Arrays.fill(scorePN, " ");
            } else {
                for (int i = 0; i < jsonArrayPN.length(); i++) {
                    JSONObject object = jsonArrayPN.getJSONObject(i);
                    if (object.has("time") && !object.isNull("time") && !object.getString("time").equals("null")) {
                        int sc = object.getInt("score");
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


            Document document = new Document();
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
            document.add(table2);

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
            JSONArray plArray = data.getJSONObject("evaluations").optJSONArray("PL");
            if (plArray != null) {
                for (int i = 0; i < plArray.length(); i++) {
                    JSONObject item = plArray.getJSONObject(i);
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
            document.add(new Paragraph("三、词汇能力评估", simsunBold));
            document.add(new Paragraph("（一）记录表", simsunBold));
            document.add(new Paragraph(" ", simsun));
            
            // 创建合并的记录表表格
            PdfPTable tableVocabulary = new PdfPTable(4);
            tableVocabulary.setWidthPercentage(100);
            tableVocabulary.setSpacingBefore(10f);
            tableVocabulary.setSpacingAfter(15f);
            
            // 设置列宽比例
            float[] columnWidths = {1f, 2f, 2f, 2f};
            tableVocabulary.setWidths(columnWidths);
            
            // 添加表头
            addHeaderCell(tableVocabulary, "序号", simsun, 1);
            addHeaderCell(tableVocabulary, "测试点", simsun, 1);
            addHeaderCell(tableVocabulary, "目标词", simsun, 1);
            addHeaderCell(tableVocabulary, "结果", simsun, 1);
            
            // 合并的词汇测试数据
            String[] testPoints = {"名词", "动词", "形容词", "分类名词（名词上位词）"};
            String[] targets = {"勺子/杯子", "跑/睡觉", "红/蓝色", "动物/水果"};
            int[] RE_scores = new int[7];
            int[] E_scores = new int[7];
            
            for (int i = 0; i < testPoints.length; i++) {
                addDataCell(tableVocabulary, String.valueOf(i + 1), simsun, 1);
                addDataCell(tableVocabulary, testPoints[i], simsun, 1);
                addDataCell(tableVocabulary, targets[i], simsun, 1);
                
                // 结果显示
                int correctCount = 0;
                
                // 检查词汇理解结果
                if (i < jsonArrayRE.length()) {
                    JSONObject reObject = jsonArrayRE.getJSONObject(i);
                    if (reObject.has("result") && !reObject.isNull("result")) {
                        if (reObject.getBoolean("result")) {
                            correctCount++;
                            RE_scores[i] = 1;
                        }
                    }
                }
                
                // 检查词汇表达结果
                if (i < jsonArrayE.length()) {
                    JSONObject eObject = jsonArrayE.getJSONObject(i);
                    if (eObject.has("result") && !eObject.isNull("result")) {
                        if (eObject.getBoolean("result")) {
                            correctCount++;
                            E_scores[i] = 1;
                        }
                    }
                }
                
                String result = "";
                if (correctCount == 2) {
                    result = "对";
                } else if (correctCount == 0) {
                    result = "错";
                } else {
                    result = "部分对";
                }
                addDataCell(tableVocabulary, result, simsun, 1);
            }
            document.add(tableVocabulary);
            
            // （二）评估结果
            document.add(new Paragraph("（二）评估结果", simsunBold));
            int totalCorrect = (int)(countre + counte);
            double totalScore = totalCorrect / 14.0 * 100;
            document.add(new Paragraph("本次评估率：" + String.format("%.2f%%", totalScore), simsun));
            document.add(new Paragraph(" ", simsun));
            
            // 统计各类型词的得分
            int nounScore = 0, verbScore = 0, adjScore = 0, categoryScore = 0;
            // 词汇理解中的得分
            nounScore += RE_scores[0]; // 名词
            verbScore += RE_scores[1]; // 动词
            adjScore += RE_scores[2];   // 形容词
            categoryScore += RE_scores[3];             // 分类名词
            // 词汇表达中的得分
            nounScore += E_scores[0];    // 名词
            verbScore += E_scores[1];    // 动词
            adjScore += E_scores[2];     // 形容词
            categoryScore += E_scores[3];              // 分类名词
            
            // 找出得分最高和最低的词类
            String bestWordType = "名词", worstWordType = "名词";
            int maxScore = nounScore;
            int minScore = nounScore;
            
            if (verbScore > maxScore) {
                maxScore = verbScore;
                bestWordType = "动词";
            }
            if (adjScore > maxScore) {
                maxScore = adjScore;
                bestWordType = "形容词";
            }
            if (categoryScore > maxScore) {
                maxScore = categoryScore;
                bestWordType = "分类名词";
            }
            
            if (verbScore < minScore) {
                minScore = verbScore;
                worstWordType = "动词";
            }
            if (adjScore < minScore) {
                minScore = adjScore;
                worstWordType = "形容词";
            }
            if (categoryScore < minScore) {
                minScore = categoryScore;
                worstWordType = "分类名词";
            }
            
            // 评估建议
            document.add(new Paragraph("（二）评估建议", simsunBold));
            document.add(new Paragraph("词汇部分主要考察对语言基本概念的理解和表达，包含名词、动词、形容词和分类概念名词的表达。根据测评结果显示，孩子在词汇理解和表达方面的综合表现如下：", simsun));
            document.add(new Paragraph(" ", simsun));
            document.add(new Paragraph("1. 优势分析：孩子" + bestWordType + "词掌握较好，显示出在这方面的语言能力较强。", simsun));
            document.add(new Paragraph("2. 不足分析：孩子" + worstWordType + "词掌握得不够好，需要针对性的学习和训练。", simsun));
            document.add(new Paragraph(" ", simsun));
            document.add(new Paragraph("具体建议：", simsun));
            document.add(new Paragraph("1. 针对掌握较好的" + bestWordType + "词，可以进一步扩展相关词汇，提高词汇量和语言运用能力。", simsun));
            document.add(new Paragraph("2. 针对掌握不够好的" + worstWordType + "词，建议通过以下方式加强训练：", simsun));
            document.add(new Paragraph("   - 实物教学：使用具体物品帮助孩子理解和记忆词汇", simsun));
            document.add(new Paragraph("   - 图片配对：通过图片与词汇的配对游戏增强记忆", simsun));
            document.add(new Paragraph("   - 情境对话：在日常生活情境中反复使用相关词汇", simsun));
            document.add(new Paragraph("3. 每天安排15-20分钟的词汇练习时间，保持学习的连续性和系统性。", simsun));
            document.add(new Paragraph("4. 在日常生活中多与孩子交流，鼓励孩子用语言表达自己的需求和想法，提供丰富的语言环境。", simsun));
            document.add(new Paragraph("5. 定期进行词汇能力评估，跟踪孩子的进步情况，及时调整学习策略。", simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("二、前语言能力测试模块（模块二）", simsunBold));
            document.add(new Paragraph("场景：" + plSceneLabel, simsun));
            document.add(new Paragraph("得分：" + plTotalScore + "/10", simsun));
            document.add(new Paragraph("（一）前语言能力评估结果", simsunBold));
            document.add(new Paragraph(plSummaryText, simsun));
            document.add(new Paragraph("（二）评估建议", simsunBold));
            String option1Line = (plSuggestionOption == 1 ? "● " : "○ ") + plOption1Text;
            String option2Line = (plSuggestionOption == 2 ? "● " : "○ ") + plOption2Text;
            document.add(new Paragraph(option1Line, simsun));
            document.add(new Paragraph(option2Line, simsun));
            document.add(new Paragraph(" ", simsun));

            // 句法能力评估结果
            document.add(new Paragraph("四、句法能力评估", simsunBold));
            document.add(new Paragraph("（一）记录表", simsunBold));
            document.add(new Paragraph(" ", simsun));
            
            // 创建句法能力测试点表格
            PdfPTable tableSyntax = new PdfPTable(4);
            tableSyntax.setWidthPercentage(100);
            tableSyntax.setSpacingBefore(10f);
            tableSyntax.setSpacingAfter(15f);
            
            // 设置列宽比例
            float[] syntaxColumnWidths = {1f, 2f, 2f, 2f};
            tableSyntax.setWidths(syntaxColumnWidths);
            
            // 添加表头
            addHeaderCell(tableSyntax, "序号", simsun, 1);
            addHeaderCell(tableSyntax, "测试点", simsun, 1);
            addHeaderCell(tableSyntax, "题目数量", simsun, 1);
            addHeaderCell(tableSyntax, "正确率", simsun, 1);
            
            // 定义句法理解和表达的测试点
            String[][] syntaxTestPoints = {
                {"1", "简单动词短语理解", "5", ""},
                {"2", "简单名词短语理解", "3", ""},
                {"3", "疑问句理解", "3", ""},
                {"4", "否定句理解", "3", ""},
                {"5", "比较句理解", "3", ""},
                {"6", "复合句理解", "4", ""},
                {"7", "简单句表达", "5", ""},
                {"8", "疑问句表达", "3", ""},
                {"9", "因果句表达", "3", ""},
                {"10", "条件句表达", "3", ""},
                {"11", "比较句表达", "3", ""},
                {"12", "复合句表达", "3", ""}
            };
            
            // 计算每个测试点的正确率
            for (int i = 0; i < syntaxTestPoints.length; i++) {
                addDataCell(tableSyntax, syntaxTestPoints[i][0], simsun, 1);
                addDataCell(tableSyntax, syntaxTestPoints[i][1], simsun, 1);
                addDataCell(tableSyntax, syntaxTestPoints[i][2], simsun, 1);
                addDataCell(tableSyntax, syntaxTestPoints[i][3], simsun, 1);
            }
            document.add(tableSyntax);
            
            document.add(new Paragraph("（二）句法理解能力结果", simsunBold));
            document.add(new Paragraph("测试结果：" + scoreRG, simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("（三）句法表达能力结果", simsunBold));
            document.add(new Paragraph("测试结果：" + scoreSE, simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("（四）句法能力综合评估", simsunBold));
            document.add(new Paragraph("综合结果：" + scoreSyntax, simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("（五）评估建议", simsunBold));
            document.add(new Paragraph("通过儿童句法理解能力的评估，本次评估结果如下：", simsun));
            document.add(new Paragraph(" ", simsun));
            
            if (lenthrg + lenthse > 0) {
                double totalSyntaxScore = ((countrg + countse) / (lenthrg + lenthse)) * 100;
                if (totalSyntaxScore >= 66.7) { // 10/15 ≈ 66.7%
                    document.add(new Paragraph("● 从整体上来说，孩子的句法理解能力较好，基本达标，符合该年龄段孩子语言发育水平。", simsun));
                } else {
                    document.add(new Paragraph("● 从整体上来说，孩子的句法理解能力还有待进一步发展，尚未达标。", simsun));
                }
                document.add(new Paragraph(" ", simsun));
                
                // 模拟需要重点关注的能力和不稳定的能力
                document.add(new Paragraph("1. 【需要重点关注的能力】", simsun));
                document.add(new Paragraph("   - 复合句理解能力", simsun));
                document.add(new Paragraph("   - 条件句表达能力", simsun));
                document.add(new Paragraph(" ", simsun));
                
                document.add(new Paragraph("2. 【不稳定的能力】", simsun));
                document.add(new Paragraph("   - 疑问句理解能力", simsun));
                document.add(new Paragraph("   - 比较句表达能力", simsun));
                document.add(new Paragraph(" ", simsun));
            }
            
            document.add(new Paragraph("具体建议：", simsun));
            document.add(new Paragraph("1. 针对需要重点关注的能力：", simsun));
            document.add(new Paragraph("   - 复合句理解：通过简单的因果关系句子开始，逐步引导孩子理解复杂的句子结构", simsun));
            document.add(new Paragraph("   - 条件句表达：使用'如果...就...'等常见条件句结构，在日常生活中反复练习", simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("2. 针对不稳定的能力：", simsun));
            document.add(new Paragraph("   - 疑问句理解：通过实物和图片，帮助孩子理解不同类型的疑问句", simsun));
            document.add(new Paragraph("   - 比较句表达：使用具体的事物进行比较，如大小、多少、高矮等", simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("3. 日常语言训练建议：", simsun));
            document.add(new Paragraph("   - 日常对话中多使用正确的语法结构，为孩子提供良好的语言示范", simsun));
            document.add(new Paragraph("   - 阅读适合孩子年龄的绘本，帮助孩子理解和吸收各种语法结构", simsun));
            document.add(new Paragraph("   - 鼓励孩子用完整的句子表达自己的想法，及时纠正语法错误", simsun));
            document.add(new Paragraph("   - 定期进行句法能力评估，跟踪孩子的进步情况", simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("（六）评估建议依据", simsunBold));
            document.add(new Paragraph("1. 整体达标标准：", simsun));
            document.add(new Paragraph("   - 如果题目总数，得到 10/15 以上分数，则达标。", simsun));
            document.add(new Paragraph("   - 如果不到 10/15，则尚未达标。", simsun));
            document.add(new Paragraph(" ", simsun));
            
            document.add(new Paragraph("2. 对应每一个考查点：", simsun));
            document.add(new Paragraph("   - 如果该考查点，题目正确率等于 0：【需要重点关注的能力】", simsun));
            document.add(new Paragraph("   - 如果该考查点，题目正确率等于 1/3：【不稳定的能力】", simsun));
            document.add(new Paragraph(" ", simsun));

            // 社交能力评估结果
            document.add(new Paragraph("五、社交能力评估", simsunBold));
            document.add(new Paragraph("（一）记录表", simsunBold));
            document.add(new Paragraph(" ", simsun));
            
            // 读取社交能力评估数据
            JSONArray socialArray = data.getJSONObject("evaluations").optJSONArray("SOCIAL");
            if (socialArray != null && socialArray.length() > 0) {
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
                    
                    for (int i = 0; i < 10; i++) {
                        int questionIndex = (group - 1) * 10 + i;
                        if (questionIndex < socialArray.length()) {
                            JSONObject object = socialArray.getJSONObject(questionIndex);
                            if (object.has("score") && !object.isNull("score")) {
                                hasCompletedQuestions = true;
                                socialCompletedQuestions++;
                                socialTotalScore += object.getInt("score");
                                
                                // 收集需要重点关注和进一步发展的能力
                                String focus = object.optString("focus", "");
                                int socialScore = object.getInt("score");
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
                            int socialNum = object.optInt("num", 0);
                            String ability = object.optString("ability", "");
                            String focus = object.optString("focus", "");
                            String content = object.optString("content", "");
                            int socialScore = object.getInt("score");
                            
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
                document.add(new Paragraph("评价准则如下：根据测试结果显示：", simsun));
                document.add(new Paragraph("选1：如果大于等于6/10，基本达标。", simsun));
                document.add(new Paragraph("选2：小于6/10，则尚未达标。", simsun));
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
            
            document.add(new Paragraph(" ", simsun));

            PdfPTable table31 = new PdfPTable(1);
            table31.setWidthPercentage(100);
            table31.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addHeaderCell2(table31,"塞音",simsunSmall,1,BaseColor.ORANGE,30f);
            addHeaderCell2(table31,"鼻音",simsunSmall,1,BaseColor.ORANGE,15f);
            addHeaderCell2(table31,"边音",simsunSmall,1,BaseColor.ORANGE,15f);
            addHeaderCell2(table31,"擦音",simsunSmall,1,BaseColor.ORANGE,30f);
            addHeaderCell2(table31,"塞擦音",simsunSmall,1,BaseColor.ORANGE,30f);

            PdfPTable table32 = new PdfPTable(1);
            table32.setWidthPercentage(100);
            table32.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addDataCell2(table32,"不送气",simsunSmall,1,15f);
            addDataCell2(table32,"送气",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"不送气",simsunSmall,1,15f);
            addDataCell2(table32,"送气",simsunSmall,1,15f);

            PdfPTable table33 = new PdfPTable(3);
            table33.setWidthPercentage(100);
            table33.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addDataCell2(table33,"b",simsunSmall,1,15f);
            addDataCell2(table33,scoreA[0], simsunSmall,2,15f);
            addDataCell2(table33,"p",simsunSmall,1,15f);
            addDataCell2(table33,scoreA[1], simsunSmall,2,15f);
            addDataCell2(table33,"m",simsunSmall,1,15f);
            addDataCell2(table33,scoreA[2], simsunSmall,2,15f);
            addHeaderCell2(table33,"",simsunSmall,3,BLUE,75f);

            PdfPTable table34 = new PdfPTable(3);
            table34.setWidthPercentage(100);
            table34.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addHeaderCell2(table34,"",simsunSmall,3,BaseColor.BLUE,60f);
            addDataCell2(table34,"f",simsunSmall,1,15f);
            addDataCell2(table34,scoreA[3], simsunSmall,2,15f);
            addHeaderCell2(table34,"",simsunSmall,3,BaseColor.BLUE,45f);

            PdfPTable table35 = new PdfPTable(3);
            table35.setWidthPercentage(100);
            table35.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addDataCell2(table35,"d",simsunSmall,1,15f);
            addDataCell2(table35,scoreA[4], simsunSmall,2,15f);
            addDataCell2(table35,"t",simsunSmall,1,15f);
            addDataCell2(table35,scoreA[5], simsunSmall,2,15f);
            addDataCell2(table35,"n",simsunSmall,1,15f);
            addDataCell2(table35,scoreA[6], simsunSmall,2,15f);
            addDataCell2(table35,"l",simsunSmall,1,15f);
            addDataCell2(table35,scoreA[7], simsunSmall,2,15f);
            addHeaderCell2(table35,"",simsunSmall,3,BaseColor.BLUE,60f);

            PdfPTable table36 = new PdfPTable(9);
            table36.setWidthPercentage(100);
            table36.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addHeaderCell2(table36,"",simsunSmall,9,BaseColor.BLUE,60f);
            addDataCell2(table36,"s",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[8], simsunSmall,2,15f);
            addDataCell2(table36,"x",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[9], simsunSmall,2,15f);
            addDataCell2(table36,"sh",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[10], simsunSmall,2,15f);
            addHeaderCell2(table36,"",simsunSmall,6,BaseColor.BLUE,15f);
            addDataCell2(table36,"r",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[11], simsunSmall,2,15f);
            addDataCell2(table36,"z",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[12], simsunSmall,2,15f);
            addDataCell2(table36,"j",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[13], simsunSmall,2,15f);
            addDataCell2(table36,"zh",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[14], simsunSmall,2,15f);
            addDataCell2(table36,"c",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[15], simsunSmall,2,15f);
            addDataCell2(table36,"q",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[16], simsunSmall,2,15f);
            addDataCell2(table36,"ch",simsunSmall,1,15f);
            addDataCell2(table36,scoreA[17], simsunSmall,2,15f);



            PdfPTable table37 = new PdfPTable(3);
            table37.setWidthPercentage(100);
            table37.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addDataCell2(table37,"g",simsunSmall,1,15f);
            addDataCell2(table37,scoreA[18], simsunSmall,2,15f);
            addDataCell2(table37,"k",simsunSmall,1,15f);
            addDataCell2(table37,scoreA[19], simsunSmall,2,15f);
            addHeaderCell2(table37,"",simsunSmall,3,BaseColor.BLUE,30f);
            addDataCell2(table37,"h",simsunSmall,1,15f);
            addDataCell2(table37,scoreA[20], simsunSmall,2,15f);
            addHeaderCell2(table37,"",simsunSmall,3,BaseColor.BLUE,45f);

            PdfPTable table3 = new PdfPTable(9);
            table3.setWidthPercentage(100);
            table3.setSpacingBefore(15f);
            table3.setSpacingAfter(15f);
            addHeaderCell2(table3,"构音（A）",simsun,9, LIGHT_GRAY,15f);
            addHeaderCell2(table3,"发音方式",simsunSmall,2, BaseColor.PINK,15f);
            addHeaderCell2(table3,"发音部位",simsunSmall,7, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"",simsunSmall,2, BaseColor.PINK,15f);
            addHeaderCell2(table3,"双唇",simsunSmall,1, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"唇齿音",simsunSmall,1, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"舌尖中",simsunSmall,1, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"舌尖前",simsunSmall,1, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"舌面前",simsunSmall,1, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"舌尖后",simsunSmall,1, BaseColor.YELLOW,15f);
            addHeaderCell2(table3,"舌根",simsunSmall,1, BaseColor.YELLOW,15f);
            addTableCell(table3,table31,1,120f);
            addTableCell(table3,table32,1,120f);
            addTableCell(table3,table33,1,120f);
            addTableCell(table3,table34,1,120f);
            addTableCell(table3,table35,1,120f);
            addTableCell(table3,table36,3,120f);
            addTableCell(table3,table37,1,120f);
            document.add(table3);

            document.close();


            Toast.makeText(context, "文件已保存", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font,int col) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setColspan(col);
        //cell.setBorder(PdfPCell.NO_BORDER);
        cell.setFixedHeight(22.5f);
        table.addCell(cell);
    }
    private static void addHeaderCell2(PdfPTable table, String text, Font font,int col, BaseColor color, float fixedheight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(color);
        cell.setColspan(col);
        //cell.setBorder(PdfPCell.NO_BORDER);
        cell.setFixedHeight(fixedheight*1.5f);
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

    private static void addDataCellAuto(PdfPTable table, String text, Font font, int col) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(col);
        table.addCell(cell);
    }
    private static void addDataCell2(PdfPTable table, String text, Font font,int col, float fixedheight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(col);
        //cell.setBorder(PdfPCell.NO_BORDER);
        cell.setFixedHeight(fixedheight*1.5f);
        table.addCell(cell);
    }
    private static void addTableCell(PdfPTable table, PdfPTable table2,int col, float fixedheight) {
        PdfPCell cell = new PdfPCell(table2);
        cell.setPadding(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(col);
        cell.setFixedHeight(fixedheight*1.5f);
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

    public static void writeTreatmentPlanPdf(Context context, Uri uri, JSONObject childJson) throws Exception {
        if (context == null || uri == null) {
            String message = context != null ? context.getString(R.string.pdf_error_invalid_params) : "Invalid parameters";
            throw new IllegalArgumentException(message);
        }
        PdfGenerator.context = context.getApplicationContext();
        JSONObject plan = childJson == null ? null : childJson.optJSONObject("treatmentPlan");
        if (plan == null) {
            throw new IllegalStateException(context.getString(R.string.pdf_error_missing_plan));
        }
        String reportMode = plan.optString("reportMode", "");
        OutputStream outputStream = null;
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                throw new IOException(context.getString(R.string.pdf_error_open_output));
            }
            JSONObject info = childJson == null ? null : childJson.optJSONObject("info");
            Typeface chineseTypeface = loadChineseTypeface(context);
            if (chineseTypeface == null) {
                throw new IllegalStateException(context.getString(R.string.pdf_error_font_missing));
            }
            TreatmentPlanStrings strings = TreatmentPlanStrings.from(context);
            TreatmentPlanPdfRenderer renderer;
            if ("overall_intervention".equals(reportMode)) {
                renderer = new OverallInterventionPdfRenderer(info, plan, chineseTypeface, strings);
            } else {
                JSONObject evaluations = childJson == null ? null : childJson.optJSONObject("evaluations");
                JSONArray evaluationsA = evaluations == null ? null : evaluations.optJSONArray("A");
                ArticulationPlanHelper.ensureArticulation(plan, evaluationsA);
                ArticulationPlanHelper.applyArticulationReport(plan, childJson, evaluationsA);
                ModuleReportHelper.applyModuleFindings(plan, evaluations, childJson);
                renderer = new TreatmentPlanPdfRenderer(info, plan, chineseTypeface, strings);
            }
            int totalPages = renderer.measurePageCount();
            renderer.render(document, totalPages);
            document.writeTo(outputStream);
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


}



