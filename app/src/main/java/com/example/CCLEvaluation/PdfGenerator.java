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
            boolean showSyntax = includeAll || "syntax".equals(moduleKey);
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
            JSONArray jsonArrayRG = evaluations.optJSONArray("RG");
            if (jsonArrayRG == null) {
                jsonArrayRG = new JSONArray();
            }
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

                // 创建句法能力测试点表格
                PdfPTable tableSyntax = new PdfPTable(4);
                tableSyntax.setWidthPercentage(100);
                tableSyntax.setSpacingBefore(10f);
                tableSyntax.setSpacingAfter(15f);

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

                // 加载RG（句法理解）和SE（句法表达）数据
                JSONArray rgArray = evaluations.optJSONArray("RG");
                JSONArray seArray = evaluations.optJSONArray("SE");
                
                // 计算每个测试点的正确率
                ArrayList<String> weaknessList = new ArrayList<>();
                ArrayList<String> inProgressList = new ArrayList<>();
                
                for (int i = 0; i < syntaxTestPoints.length; i++) {
                    int totalQuestions = Integer.parseInt(syntaxTestPoints[i][2]);
                    int correctCount = 0;
                    
                    // 检查RG数据（前6个测试点）
                    if (i < 6 && rgArray != null) {
                        for (int j = 0; j < rgArray.length(); j++) {
                            JSONObject item = rgArray.optJSONObject(j);
                            if (item != null) {
                                int groupNumber = item.optInt("groupNumber", 0);
                                if (groupNumber == i + 1) {
                                    boolean result = item.optBoolean("result", false);
                                    if (result) {
                                        correctCount++;
                                    }
                                }
                            }
                        }
                    }
                    
                    // 检查SE数据（后6个测试点）
                    if (i >= 6 && seArray != null) {
                        for (int j = 0; j < seArray.length(); j++) {
                            JSONObject item = seArray.optJSONObject(j);
                            if (item != null) {
                                int groupNumber = item.optInt("groupNumber", 0);
                                if (groupNumber == i - 5) {
                                    boolean result = item.optBoolean("result", false);
                                    if (result) {
                                        correctCount++;
                                    }
                                }
                            }
                        }
                    }
                    
                    // 计算正确率
                    String accuracy = totalQuestions > 0 ? String.format("%.1f%%", (double) correctCount / totalQuestions * 100) : "0%";
                    syntaxTestPoints[i][3] = accuracy;
                    
                    // 确定需要重点关注的能力和不稳定的能力
                    double accuracyValue = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0;
                    if (accuracyValue == 0) {
                        weaknessList.add(syntaxTestPoints[i][1]);
                    } else if (accuracyValue <= 0.33) {
                        inProgressList.add(syntaxTestPoints[i][1]);
                    }
                    
                    addDataCell(tableSyntax, syntaxTestPoints[i][0], simsun, 1);
                    addDataCell(tableSyntax, syntaxTestPoints[i][1], simsun, 1);
                    addDataCell(tableSyntax, syntaxTestPoints[i][2], simsun, 1);
                    addDataCell(tableSyntax, accuracy, simsun, 1);
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

                document.add(new Paragraph("具体建议：", simsun));
                
                // 针对需要重点关注的能力
                if (!weaknessList.isEmpty()) {
                    document.add(new Paragraph("1. 针对需要重点关注的能力：", simsun));
                    for (String ability : weaknessList) {
                        if (ability.contains("复合句")) {
                            document.add(new Paragraph("   - 复合句理解：通过简单的因果关系句子开始，逐步引导孩子理解复杂的句子结构", simsun));
                        } else if (ability.contains("条件句")) {
                            document.add(new Paragraph("   - 条件句表达：使用'如果...就...'等常见条件句结构，在日常生活中反复练习", simsun));
                        } else if (ability.contains("疑问句")) {
                            document.add(new Paragraph("   - 疑问句理解：通过实物和图片，帮助孩子理解不同类型的疑问句", simsun));
                        } else if (ability.contains("比较句")) {
                            document.add(new Paragraph("   - 比较句表达：使用具体的事物进行比较，如大小、多少、高矮等", simsun));
                        } else if (ability.contains("简单动词短语")) {
                            document.add(new Paragraph("   - 简单动词短语理解：通过动作示范和图片，帮助孩子理解动词短语的含义", simsun));
                        } else if (ability.contains("简单名词短语")) {
                            document.add(new Paragraph("   - 简单名词短语理解：通过实物和图片，帮助孩子理解名词短语的含义", simsun));
                        } else if (ability.contains("否定句")) {
                            document.add(new Paragraph("   - 否定句理解：通过对比和示范，帮助孩子理解否定句的含义", simsun));
                        } else if (ability.contains("因果句")) {
                            document.add(new Paragraph("   - 因果句表达：通过日常生活中的例子，帮助孩子理解因果关系", simsun));
                        }
                    }
                    document.add(new Paragraph(" ", simsun));
                }
                
                // 针对不稳定的能力
                if (!inProgressList.isEmpty()) {
                    document.add(new Paragraph("2. 针对不稳定的能力：", simsun));
                    for (String ability : inProgressList) {
                        if (ability.contains("复合句")) {
                            document.add(new Paragraph("   - 复合句理解：通过简单的因果关系句子开始，逐步引导孩子理解复杂的句子结构", simsun));
                        } else if (ability.contains("条件句")) {
                            document.add(new Paragraph("   - 条件句表达：使用'如果...就...'等常见条件句结构，在日常生活中反复练习", simsun));
                        } else if (ability.contains("疑问句")) {
                            document.add(new Paragraph("   - 疑问句理解：通过实物和图片，帮助孩子理解不同类型的疑问句", simsun));
                        } else if (ability.contains("比较句")) {
                            document.add(new Paragraph("   - 比较句表达：使用具体的事物进行比较，如大小、多少、高矮等", simsun));
                        } else if (ability.contains("简单动词短语")) {
                            document.add(new Paragraph("   - 简单动词短语理解：通过动作示范和图片，帮助孩子理解动词短语的含义", simsun));
                        } else if (ability.contains("简单名词短语")) {
                            document.add(new Paragraph("   - 简单名词短语理解：通过实物和图片，帮助孩子理解名词短语的含义", simsun));
                        } else if (ability.contains("否定句")) {
                            document.add(new Paragraph("   - 否定句理解：通过对比和示范，帮助孩子理解否定句的含义", simsun));
                        } else if (ability.contains("因果句")) {
                            document.add(new Paragraph("   - 因果句表达：通过日常生活中的例子，帮助孩子理解因果关系", simsun));
                        }
                    }
                    document.add(new Paragraph(" ", simsun));
                }

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
