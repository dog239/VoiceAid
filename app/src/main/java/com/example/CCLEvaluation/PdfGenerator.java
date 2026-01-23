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
import java.util.List;
import java.util.Locale;
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
            Context ctx = requireContext();
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
            String gender;
            String c;
            String serialNumber;
            String testData;
            String birthDate;
            String address;
            String phone;
            String familyStatus;
            String testLocation;
            String examiner;
            JSONArray familyMembers;

            JSONObject data = dataManager.getInstance().loadData(fname);
            JSONObject info = data.optJSONObject("info");
            name = getInfoValue(info, "name");
            gender = getInfoValue(info, "gender");
            birthDate = getInfoValue(info, "birthDate");
            address = getInfoValue(info, "address");
            phone = getInfoValue(info, "phone");
            familyStatus = getInfoValue(info, "familyStatus");
            familyMembers = info != null ? info.optJSONArray("familyMembers") : null;
            c = getInfoValue(info, "class");
            serialNumber = getInfoValue(info, "serialNumber");
            testData = getInfoValue(info, "testDate");
            testLocation = getInfoValue(info, "testLocation");
            examiner = getInfoValue(info, "examiner");
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
                        String tone1 = object.optString("target_tone1", "");
                        if (!tone1.equals("")) {
                            String originalString = tone1;
                            for (int j = 0; j < characs.length; j++) {
                                if (characs[j].equals(originalString)) {
                                    score[j]++;
                                }
                            }
                        }
                        String tone2 = object.optString("target_tone2", "");
                        if (!tone2.equals("")) {
                            String originalString = tone2;
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
            // 鏈€澶у搴?
            BaseFont baseFont = loadChineseBaseFont(ctx);
            if (baseFont == null) {
                Toast.makeText(ctx, getStringRes(R.string.pdf_error_font_missing), Toast.LENGTH_LONG).show();
                document.close();
                return;
            }
            Font simsun = new Font(baseFont, 10);
            Font simsunBold = new Font(baseFont, 10, Font.BOLD);
            Font simsunBig = new Font(baseFont, 18, Font.BOLD);
            Font simsunSmall = new Font(baseFont, 8, Font.BOLD);

            //璁剧疆鍐呭
            Paragraph paragraph = new Paragraph(getStringRes(R.string.pdf_report_title), simsunBig);
            paragraph.setAlignment(1);
            //寮曠敤瀛椾綋
            document.add(paragraph);


            PdfPTable baseInfoTable = new PdfPTable(4);
            baseInfoTable.setWidthPercentage(100);
            baseInfoTable.setSpacingBefore(20f);
            baseInfoTable.setSpacingAfter(10f);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_section_family_info), simsunBold, 4);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_label_name), simsun, 1);
            addDataCell(baseInfoTable,name,simsun,1);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_label_gender), simsun, 1);
            addDataCell(baseInfoTable,gender,simsun,1);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_label_birth_date), simsun, 1);
            addDataCell(baseInfoTable,birthDate,simsun,1);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_label_phone), simsun, 1);
            addDataCell(baseInfoTable,phone,simsun,1);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_label_address), simsun, 1);
            addDataCellAuto(baseInfoTable,address,simsun,3);
            addHeaderCell(baseInfoTable, getStringRes(R.string.pdf_label_family_status), simsun, 1);
            addDataCellAuto(baseInfoTable,familyStatus,simsun,3);
            document.add(baseInfoTable);

            PdfPTable familyTable = new PdfPTable(5);
            familyTable.setWidthPercentage(100);
            familyTable.setSpacingBefore(5f);
            familyTable.setSpacingAfter(15f);
            addHeaderCell(familyTable, getStringRes(R.string.pdf_section_family_members), simsunBold, 5);
            addHeaderCell(familyTable, getStringRes(R.string.pdf_label_family_member_name), simsun, 1);
            addHeaderCell(familyTable, getStringRes(R.string.pdf_label_relation), simsun, 1);
            addHeaderCell(familyTable, getStringRes(R.string.pdf_label_phone), simsun, 1);
            addHeaderCell(familyTable, getStringRes(R.string.pdf_label_occupation), simsun, 1);
            addHeaderCell(familyTable, getStringRes(R.string.pdf_label_education), simsun, 1);
            if (hasFamilyMembers(familyMembers)) {
                for (int i = 0; i < familyMembers.length(); i++) {
                    JSONObject member = familyMembers.optJSONObject(i);
                    if (member == null) {
                        continue;
                    }
                    addDataCell(familyTable, valueOrDefault(member.optString("member_name", "")), simsun,1);
                    addDataCell(familyTable, valueOrDefault(member.optString("relation", "")), simsun,1);
                    addDataCell(familyTable, valueOrDefault(member.optString("member_phone", "")), simsun,1);
                    addDataCell(familyTable, valueOrDefault(member.optString("occupation", "")), simsun,1);
                    addDataCell(familyTable, valueOrDefault(member.optString("education", "")), simsun,1);
                }
            } else {
                addDataCellAuto(familyTable, getStringRes(R.string.pdf_family_empty), simsun, 5);
            }
            document.add(familyTable);


            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(15f);
            addHeaderCell(table, getStringRes(R.string.pdf_section_child_info), simsunBold, 6);
            addHeaderCell(table, getStringRes(R.string.pdf_label_name), simsun, 1);
            addDataCell(table,name,simsun,5);
            addHeaderCell(table, getStringRes(R.string.pdf_label_class), simsun, 1);
            addDataCell(table,c,simsun,2);
            addHeaderCell(table, getStringRes(R.string.pdf_label_serial_number), simsun, 1);
            addDataCell(table,serialNumber,simsun,2);
            addHeaderCell(table, getStringRes(R.string.pdf_label_birth_date), simsun, 1);
            addDataCell(table,birthDate,simsun,2);
            addHeaderCell(table, getStringRes(R.string.pdf_label_test_date), simsun, 1);
            addDataCell(table,testData,simsun,2);
            addHeaderCell(table, getStringRes(R.string.pdf_label_test_location), simsun, 1);
            addDataCell(table,testLocation,simsun,2);
            addHeaderCell(table, getStringRes(R.string.pdf_label_examiner), simsun, 1);
            addDataCell(table,examiner,simsun,2);
            document.add(table);



            // 鍒涘缓琛ㄦ牸锛岃缃搴?
            PdfPTable table21 = new PdfPTable(4);
            table21.setWidthPercentage(100);

            table21.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addHeaderCell(table21, "1", simsun,1);
            addHeaderCell(table21, "2", simsun,1);
            addHeaderCell(table21, "3", simsun,1);
            addHeaderCell(table21, getStringRes(R.string.pdf_label_average), simsun, 1);
            for (String scorepn : scorePN) {
                addDataCell(table21, scorepn, simsun,1);
            }
            addDataCell(table21, scorePNAVE, simsun,1);
            // 鍒涘缓琛ㄦ牸锛岃缃搴?
            PdfPTable table22 = new PdfPTable(6);
            table22.setWidthPercentage(100);
            table22.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            // 娣诲姞鏍囬鍗曞厓鏍?
            addHeaderCell(table22, "1", simsun,1);
            addHeaderCell(table22, "2", simsun,1);
            addHeaderCell(table22, "3", simsun,1);
            addHeaderCell(table22, "4", simsun,1);
            addHeaderCell(table22, "5", simsun,1);
            addHeaderCell(table22, getStringRes(R.string.pdf_label_average), simsunSmall, 1);
            for (String scorepst : scorePST) {
                addDataCell(table22, scorepst, simsun,1);
            }
            addDataCell(table22, scorePSTAVE, simsun,1);


            PdfPTable table2 = new PdfPTable(6);
            table2.setWidthPercentage(100);
            table2.setSpacingBefore(15f);
            table2.setSpacingAfter(15f);

            addHeaderCell(table2, getStringRes(R.string.pdf_section_eval_results), simsunBold, 6);
            addHeaderCell(table2, getStringRes(R.string.pdf_eval_vocab_expression), simsun, 1);
            addDataCell(table2,scoreE,simsun,2);
            addHeaderCell(table2, getStringRes(R.string.pdf_eval_vocab_comprehension), simsun, 1);
            addDataCell(table2,scoreRE,simsun,2);
            addHeaderCell(table2, getStringRes(R.string.pdf_eval_semantics), simsun, 1);
            addDataCell(table2,scoreS,simsun,2);
            addHeaderCell(table2, getStringRes(R.string.pdf_eval_nonword_repetition), simsun, 1);
            addDataCell(table2,scoreNWR,simsun,2);
            addHeaderCell(table2, getStringRes(R.string.pdf_eval_grammar_comprehension), simsun, 1);
            addDataCell(table2,scoreRG,simsun,2);
            addHeaderCell2(table2, getStringRes(R.string.pdf_eval_picture_story), simsun, 1, LIGHT_GRAY, 30f);
            addTableCell(table2,table22,2,30f);
            addHeaderCell2(table2, getStringRes(R.string.pdf_eval_personal_narrative), simsun, 1, LIGHT_GRAY, 30f);
            addTableCell(table2,table21,5,30f);
            document.add(table2);

            PdfPTable table31 = new PdfPTable(1);
            table31.setWidthPercentage(100);
            table31.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addHeaderCell2(table31, getStringRes(R.string.pdf_manner_plosive), simsunSmall, 1, BaseColor.ORANGE, 30f);
            addHeaderCell2(table31, getStringRes(R.string.pdf_manner_nasal), simsunSmall, 1, BaseColor.ORANGE, 15f);
            addHeaderCell2(table31, getStringRes(R.string.pdf_manner_lateral), simsunSmall, 1, BaseColor.ORANGE, 15f);
            addHeaderCell2(table31, getStringRes(R.string.pdf_manner_fricative), simsunSmall, 1, BaseColor.ORANGE, 30f);
            addHeaderCell2(table31, getStringRes(R.string.pdf_manner_affricate), simsunSmall, 1, BaseColor.ORANGE, 30f);

            PdfPTable table32 = new PdfPTable(1);
            table32.setWidthPercentage(100);
            table32.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            addDataCell2(table32, getStringRes(R.string.pdf_label_unaspirated), simsunSmall, 1, 15f);
            addDataCell2(table32, getStringRes(R.string.pdf_label_aspirated), simsunSmall, 1, 15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32,"",simsunSmall,1,15f);
            addDataCell2(table32, getStringRes(R.string.pdf_label_unaspirated), simsunSmall, 1, 15f);
            addDataCell2(table32, getStringRes(R.string.pdf_label_aspirated), simsunSmall, 1, 15f);

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
            addHeaderCell2(table3, getStringRes(R.string.pdf_articulation_title), simsun, 9, LIGHT_GRAY, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_label_manner), simsunSmall, 2, BaseColor.PINK, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_label_place), simsunSmall, 7, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3,"",simsunSmall,2, BaseColor.PINK,15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_bilabial), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_labiodental), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_alveolar), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_alveolar_middle), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_retroflex), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_palatal), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addHeaderCell2(table3, getStringRes(R.string.pdf_place_velar), simsunSmall, 1, BaseColor.YELLOW, 15f);
            addTableCell(table3,table31,1,120f);
            addTableCell(table3,table32,1,120f);
            addTableCell(table3,table33,1,120f);
            addTableCell(table3,table34,1,120f);
            addTableCell(table3,table35,1,120f);
            addTableCell(table3,table36,3,120f);
            addTableCell(table3,table37,1,120f);
            document.add(table3);

            JSONObject treatmentPlan = data.optJSONObject("treatmentPlan");
            if (treatmentPlan != null) {
                addTreatmentPlanSection(document, simsunBold, simsun, treatmentPlan);
            }

            document.close();


            Toast.makeText(ctx, getStringRes(R.string.pdf_export_success), Toast.LENGTH_LONG).show();
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
        JSONObject plan = childJson == null ? null : childJson.optJSONObject("treatmentPlan");
        if (plan == null) {
            throw new IllegalStateException(context.getString(R.string.pdf_error_missing_plan));
        }
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
            TreatmentPlanPdfRenderer renderer = new TreatmentPlanPdfRenderer(info, plan, chineseTypeface, strings);
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

    private static void addList(Document document, String title, JSONArray items, Font bodyFont) throws Exception {
        if (!hasItems(items)) {
            return;
        }
        String labelSeparator = getStringRes(R.string.pdf_label_separator);
        String bulletPrefix = getStringRes(R.string.pdf_bullet_prefix);
        document.add(new Paragraph(title + labelSeparator, bodyFont));
        for (int i = 0; i < items.length(); i++) {
            String item = items.optString(i, "").trim();
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


    private static final class TreatmentPlanPdfRenderer {
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
        private static final String SECTION_ARTICULATION_MASTERED = "本次评估中已掌握的能力";
        private static final String SECTION_ARTICULATION_NOT_MASTERED = "未掌握能力的整体说明";
        private static final String SECTION_ARTICULATION_FOCUS = "需要重点关注的能力";
        private static final String SECTION_ARTICULATION_UNSTABLE = "不稳定的能力";
        private static final String SECTION_ARTICULATION_SMART = "干预目标（SMART）";
        private static final String SECTION_ARTICULATION_HOME = "家庭干预指导建议";
        private static final String LABEL_AGE = "年龄";
        private static final String AGE_MISSING_TEXT = "未提供";
        private static final String MEMBER_SEPARATOR = "｜";

        private final JSONObject info;
        private final JSONObject plan;
        private final TreatmentPlanStrings strings;

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
            drawPersonalInfo();
            drawAccentSectionHeader(SECTION_INTERVENTION_GUIDE);
            drawModuleBlocks();
            // TODO(隐藏需求): 频次建议与备注区块暂时隐藏，后续恢复时取消注释。
            // drawSchedule();
            // drawNotes();
            finishPage();
            return pageNumber;
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
            String dateText = strings.datePrefix + new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
            float titleHeight = 0f;
            if (pageNumber == 1) {
                titleHeight = drawTextBlock(strings.title, titlePaint, MARGIN, MARGIN, contentWidth, Layout.Alignment.ALIGN_CENTER);
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
            String dateText = strings.datePrefix + new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
            float dateHeight = measureTextHeight(dateText, smallPaint, contentWidth, Layout.Alignment.ALIGN_OPPOSITE);
            if (pageNumber == 1) {
                float titleHeight = measureTextHeight(strings.title, titlePaint, contentWidth, Layout.Alignment.ALIGN_CENTER);
                return titleHeight + dateHeight + 2f;
            }
            return dateHeight;
        }

        private float getContentWidth() {
            return PAGE_WIDTH - MARGIN * 2f;
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
            drawFindingsCard(moduleData, summaryData);
            if (MODULE_TITLE_SPEECH_SOUND.equals(title) && hasArticulation(moduleData)) {
                drawArticulationPlan(moduleData);
            } else {
                drawPlanList(moduleData);
                drawStageCards(moduleData);
            }
            y += SECTION_GAP;
        }

        private void drawModuleHeader(String title, boolean drawLine) {
            float contentWidth = getContentWidth();
            float titleHeight = measureTextHeight(title, moduleTitlePaint, contentWidth, Layout.Alignment.ALIGN_NORMAL);
            float required = (drawLine ? LINE_GAP : 0f) + titleHeight + LINE_GAP;
            ensureSpace(required);
            if (drawLine && !measureOnly) {
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
            }
            if (drawLine) {
                y += LINE_GAP;
            }
            drawTextBlock(title, moduleTitlePaint, MARGIN, y, contentWidth, Layout.Alignment.ALIGN_NORMAL);
            y += titleHeight + LINE_GAP;
        }

        private void drawFindingsCard(JSONObject moduleData, JSONObject summaryData) {
            List<String> keyFindings = getList(moduleData, "key_findings");
            if (keyFindings.isEmpty()) {
                keyFindings = new ArrayList<>();
                keyFindings.add(placeholderText());
            }

            float contentWidth = getContentWidth() - FINDINGS_PADDING * 2f;
            float height = FINDINGS_PADDING;
            height += measureBulletSectionHeight(strings.labelKeyFindings, keyFindings, contentWidth);
            height += FINDINGS_PADDING;

            ensureSpace(height + LINE_GAP);
            if (!measureOnly) {
                RectF rect = new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height);
                canvas.drawRoundRect(rect, FINDINGS_RADIUS, FINDINGS_RADIUS, sectionFillPaint);
            }
            float cursorY = y + FINDINGS_PADDING;
            cursorY += drawBulletSectionAt(strings.labelKeyFindings, keyFindings, MARGIN + FINDINGS_PADDING, cursorY, contentWidth);
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

        private void drawSectionHeader(String title) {
            drawSectionHeader(title, sectionFillPaint, Layout.Alignment.ALIGN_NORMAL);
        }

        private void drawSectionHeader(String title, Paint backgroundPaint) {
            drawSectionHeader(title, backgroundPaint, Layout.Alignment.ALIGN_NORMAL);
        }

        private void drawAccentSectionHeader(String title) {
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

        private void drawSubHeader(String title) {
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

        private void drawParagraph(String text) {
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

        private void drawBulletLine(String line) {
            float indentX = MARGIN + BULLET_INDENT;
            float width = getContentWidth() - BULLET_INDENT;
            float height = measureBulletItemHeight(line, width);
            ensureSpace(height + LINE_GAP);
            drawBulletItem(line, indentX, y, width);
            y += height + LINE_GAP;
        }

        private void drawBulletSection(String title, List<String> items, boolean showPlaceholder) {
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
                String value = safeText(item).isEmpty() ? placeholderText() : item.trim();
                float indentX = MARGIN + BULLET_INDENT;
                float width = getContentWidth() - BULLET_INDENT;
                float height = measureBulletItemHeight(value, width);
                ensureSpace(height + LINE_GAP);
                drawBulletItem(value, indentX, y, width);
                y += height + LINE_GAP;
            }
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
                String value = safeText(item).isEmpty() ? placeholderText() : item.trim();
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
                String value = safeText(item).isEmpty() ? placeholderText() : item.trim();
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

        private String placeholderText() {
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

        private List<String> getList(JSONObject obj, String key) {
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

        private String safeText(String value) {
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



