package com.example.CCLEvaluation;

import static com.google.android.material.internal.ContextUtils.getActivity;
import static com.itextpdf.text.BaseColor.BLUE;
import static com.itextpdf.text.BaseColor.LIGHT_GRAY;
//import static com.itextpdf.kernel.pdf.PdfName.BaseFont;
//import static com.itextpdf.kernel.pdf.PdfName.T;
//import static com.itextpdf.kernel.pdf.PdfName.staticNames;
import static utils.permissionutils.permissionUtils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import utils.dataManager;
import utils.permissionutils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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




    public PdfGenerator(Context context) {
        this.context = context;
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


//RE
            double countre = 0;
            JSONArray jsonArrayRE = data.getJSONObject("evaluations").getJSONArray("RE");
            for (int i = 0; i < jsonArrayRE.length(); i++) {
                JSONObject object = jsonArrayRE.getJSONObject(i);
                if (object.has("time") && !object.isNull("time") && !object.get("time").equals("null")) {
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
            addHeaderCell2(table2,"看图说故事（PST）",simsun,1, LIGHT_GRAY,30f);
            addTableCell(table2,table22,2,30f);
            addHeaderCell2(table2,"个人生活经验（PN）",simsun,1, LIGHT_GRAY,30f);
            addTableCell(table2,table21,5,30f);
            document.add(table2);

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
}