package com.example.CCLEvaluation;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import utils.dataManager;
import utils.NetInteractUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class childinfoactivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSave;
    private Button buttonDelete;
    private Button buttonAddMember;
    private EditText textName;
    private EditText textBirth;
    private EditText textTestTime;
    private EditText textAddress;
    private EditText textPhone;
    private RadioGroup genderGroup;
    private RecyclerView familyRecyclerView;
    private FamilyMemberAdapter familyMemberAdapter;
    private SimpleDateFormat dateFormat;
    private String examinerName;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_info);
        buttonSave = findViewById(R.id.btn_save);
        buttonDelete = findViewById(R.id.btn_delete);
        buttonAddMember = findViewById(R.id.btn_add_member);

        textName = findViewById(R.id.et_name);
        textBirth = findViewById(R.id.et_birth);
        textTestTime = findViewById(R.id.et_test_time);
        textAddress = findViewById(R.id.et_address);
        textPhone = findViewById(R.id.et_phone);
        genderGroup = findViewById(R.id.rg_gender);
        familyRecyclerView = findViewById(R.id.rv_family_members);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textBirth.setKeyListener(null);
        textBirth.setOnClickListener(v -> showBirthDatePicker());
        textTestTime.setText(dateFormat.format(new Date()));

        familyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        familyMemberAdapter = new FamilyMemberAdapter(createDefaultMembers(), this::handleDeleteMember);
        familyRecyclerView.setAdapter(familyMemberAdapter);

        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        examinerName = prefs.getString("Username", "");
        String uid = getIntent().getStringExtra("Uid");
        if ((examinerName == null || examinerName.trim().isEmpty()) && uid != null && !uid.isEmpty()) {
            NetInteractUtils.getInstance(this).setUserInfoCallback(user -> {
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(user);
                    String username = obj.optString("Username", "");
                    if (!username.trim().isEmpty()) {
                        prefs.edit().putString("Username", username).apply();
                        examinerName = username;
                    }
                } catch (Exception ignored) {
                }
            });
            NetInteractUtils.getInstance(this).getUserInfo(uid);
        }

        buttonSave.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonAddMember.setOnClickListener(v ->
                familyMemberAdapter.addMember(new FamilyMemberAdapter.FamilyMember()));

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save){
            String tNa = textName.getText().toString().trim();
            String tB = textBirth.getText().toString().trim();
            String tTestDate = textTestTime.getText().toString().trim();
            String tAddr = textAddress.getText().toString().trim();
            String tPhone = textPhone.getText().toString().trim();
            String tGender = getSelectedGender();
            try {
                JSONObject info = new JSONObject();
                info.put("name", tNa);
                info.put("gender", tGender);
                info.put("birthDate", tB);
                info.put("address", tAddr);
                info.put("phone", tPhone);
                info.put("familyMembers", FamilyMemberAdapter.toJsonArray(familyMemberAdapter.getMembers()));
                info.put("testDate", tTestDate.isEmpty() ? dateFormat.format(new Date()) : tTestDate);

                SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                String cachedName = prefs.getString("Username", "");
                String examiner = (cachedName == null || cachedName.trim().isEmpty()) ? examinerName : cachedName;
                info.put("examiner", examiner == null ? "" : examiner);

                JSONObject data = dataManager.getInstance().createData(info);
                String baseName = tNa.isEmpty() ? "child" : tNa;
                String fName = dataManager.getInstance().saveData(baseName + System.currentTimeMillis() + ".json", data);
                dataManager.getInstance().createIndex(fName,"Index");

                String Uid = getIntent().getStringExtra("Uid");
                if (Uid != null && !Uid.isEmpty()) {
                    Toast.makeText(this, "正在上传测评...", Toast.LENGTH_SHORT).show();
                    NetInteractUtils.getInstance(this).setUploadEvaluationCallback(childUserID -> {
                        startAssessment(fName, Uid, childUserID);
                    });
                    NetInteractUtils.getInstance(this).uploadEvaluation(Uid, data.toString());
                } else {
                    startAssessment(fName, null, null);
                }
            } catch (Exception e) {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }

        } else if (v.getId()==R.id.btn_delete) {
            clearForm();
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show();
        }

    }

    private void startAssessment(String fName, String uid, String childUserID) {
        Intent intent = new Intent(childinfoactivity.this, AssessmentModulesActivity.class);
        intent.putExtra("fName", fName);
        if (uid != null) {
            intent.putExtra("Uid", uid);
        }
        if (childUserID != null) {
            intent.putExtra("childID", childUserID);
        }
        intent.putExtra("private", true);
        startActivity(intent);
        finish();
    }

    private void clearForm() {
        textName.setText("");
        genderGroup.clearCheck();
        textBirth.setText("");
        textTestTime.setText(dateFormat.format(new Date()));
        textAddress.setText("");
        textPhone.setText("");
        familyMemberAdapter.setMembers(createDefaultMembers());
    }

    private String getSelectedGender() {
        int checkedId = genderGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_gender_male) {
            return "男";
        }
        if (checkedId == R.id.rb_gender_female) {
            return "女";
        }
        return "";
    }

    private void showBirthDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String current = textBirth.getText().toString().trim();
        if (!current.isEmpty()) {
            try {
                Date parsed = dateFormat.parse(current);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (Exception ignored) {
            }
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(y, m, d);
            textBirth.setText(dateFormat.format(selected.getTime()));
        }, year, month, day);
        dialog.show();
    }

    private void handleDeleteMember(int position) {
        familyMemberAdapter.removeMember(position);
    }

    private ArrayList<FamilyMemberAdapter.FamilyMember> createDefaultMembers() {
        ArrayList<FamilyMemberAdapter.FamilyMember> members = new ArrayList<>();
        members.add(new FamilyMemberAdapter.FamilyMember());
        return members;
    }
}
