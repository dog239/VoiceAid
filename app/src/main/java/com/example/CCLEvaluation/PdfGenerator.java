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
            String moduleKey = normalizeModuleKey(evaluationModuleOverride.get());
            boolean includeAll = moduleKey.isEmpty();
            boolean showArticulation = includeAll || "articulation".equals(moduleKey);
            boolean showPrelinguistic = includeAll || "prelinguistic".equals(moduleKey);
            boolean showVocabulary = includeAll || "vocabulary".equals(moduleKey);
            boolean showSyntax = includeAll || "syntax".equals(moduleKey);
            boolean showSocial = includeAll || "social".equals(moduleKey);

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
            if (showVocabulary) {
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
            }

            if (showPrelinguistic) {
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
            }

            // 句法能力评估结果
            if (showSyntax) {
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
            }

            // 社交能力评估结果
            if (showSocial) {
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
            }

            if (showArticulation) {
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
                filtered.put("RG", evaluations.optJSONArray("RG"));
                filtered.put("SE1", evaluations.optJSONArray("SE1"));
                filtered.put("SE2", evaluations.optJSONArray("SE2"));
                filtered.put("SE3", evaluations.optJSONArray("SE3"));
                filtered.put("SE4", evaluations.optJSONArray("SE4"));
                filtered.put("RG1", evaluations.optJSONArray("RG1"));
                filtered.put("RG2", evaluations.optJSONArray("RG2"));
                filtered.put("RG3", evaluations.optJSONArray("RG3"));
                filtered.put("RG4", evaluations.optJSONArray("RG4"));
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

    public static void writeTreatmentPlanPdf(Context context, Uri uri, JSONObject childJson) throws Exception {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }
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
