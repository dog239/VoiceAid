package com.example.CCLEvaluation;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import utils.NetInteractUtils;
import utils.dataManager;

public class childinfoactivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSave;
    private Button buttonDelete;
    private Button buttonAddMember;
    private Button buttonAddBackgroundInfo;
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
    private String existingFileName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_info);

        initViews();
        initBaseInfo();
        bindEvents();
    }

    private void initViews() {
        buttonSave = findViewById(R.id.btn_save);
        buttonDelete = findViewById(R.id.btn_delete);
        buttonAddMember = findViewById(R.id.btn_add_member);
        buttonAddBackgroundInfo = findViewById(R.id.btn_add_background_info);
        textName = findViewById(R.id.et_name);
        textBirth = findViewById(R.id.et_birth);
        textTestTime = findViewById(R.id.et_test_time);
        textAddress = findViewById(R.id.et_address);
        textPhone = findViewById(R.id.et_phone);
        genderGroup = findViewById(R.id.rg_gender);
        familyRecyclerView = findViewById(R.id.rv_family_members);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        familyRecyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        familyRecyclerView.setNestedScrollingEnabled(false);
        familyRecyclerView.setFocusable(false);
        familyRecyclerView.setHasFixedSize(false);
        familyRecyclerView.setItemAnimator(null);
        familyMemberAdapter = new FamilyMemberAdapter(createDefaultMembers(), this::handleDeleteMember);
        familyRecyclerView.setAdapter(familyMemberAdapter);
    }

    private void initBaseInfo() {
        textBirth.setKeyListener(null);
        textBirth.setOnClickListener(v -> showBirthDatePicker());
        textTestTime.setText(dateFormat.format(new Date()));

        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        examinerName = prefs.getString("Username", "");

        String uid = getIntent().getStringExtra("Uid");
        if ((examinerName == null || examinerName.trim().isEmpty()) && !TextUtils.isEmpty(uid)) {
            NetInteractUtils.getInstance(this).setUserInfoCallback(user -> {
                try {
                    JSONObject obj = new JSONObject(user);
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

        existingFileName = getIntent().getStringExtra("fName");
        if (!TextUtils.isEmpty(existingFileName)) {
            reloadBasicInfo();
        }
    }

    private void bindEvents() {
        buttonSave.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonAddMember.setOnClickListener(v ->
                familyMemberAdapter.addMember(new FamilyMemberAdapter.FamilyMember()));
        buttonAddBackgroundInfo.setOnClickListener(v -> openBackgroundInfoEditor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(existingFileName)) {
            reloadBasicInfo();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            saveAndStartAssessment();
        } else if (v.getId() == R.id.btn_delete) {
            clearForm();
            Toast.makeText(this, "Cleared.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAndStartAssessment() {
        try {
            String fName = upsertCurrentChildRecord();
            JSONObject data = dataManager.getInstance().loadData(fName);

            String uid = getIntent().getStringExtra("Uid");
            if (!TextUtils.isEmpty(uid)) {
                Toast.makeText(this, "Uploading assessment...", Toast.LENGTH_SHORT).show();
                NetInteractUtils.getInstance(this).setUploadEvaluationCallback(childUserID ->
                        startAssessment(fName, uid, childUserID));
                NetInteractUtils.getInstance(this).uploadEvaluation(uid, data.toString());
            } else {
                startAssessment(fName, null, null);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject buildInfoJson(String name,
                                     String birthDate,
                                     String testDate,
                                     String address,
                                     String phone,
                                     String gender) throws Exception {
        JSONObject info = new JSONObject();
        info.put("name", name);
        info.put("gender", gender);
        info.put("birthDate", birthDate);
        info.put("address", address);
        info.put("phone", phone);
        info.put("familyMembers", FamilyMemberAdapter.toJsonArray(familyMemberAdapter.getMembers()));
        info.put("testDate", testDate.isEmpty() ? dateFormat.format(new Date()) : testDate);

        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String cachedName = prefs.getString("Username", "");
        String examiner = (cachedName == null || cachedName.trim().isEmpty()) ? examinerName : cachedName;
        info.put("examiner", examiner == null ? "" : examiner);
        return info;
    }

    private void openBackgroundInfoEditor() {
        try {
            existingFileName = upsertCurrentChildRecord();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to prepare child record.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ChildDetailEditActivity.class);
        intent.putExtra(ChildDetailEditActivity.EXTRA_FILE_NAME, existingFileName);
        startActivity(intent);
    }

    private String upsertCurrentChildRecord() throws Exception {
        String name = textName.getText().toString().trim();
        String birthDate = textBirth.getText().toString().trim();
        String testDate = textTestTime.getText().toString().trim();
        String address = textAddress.getText().toString().trim();
        String phone = textPhone.getText().toString().trim();
        String gender = getSelectedGender();

        JSONObject info = buildInfoJson(name, birthDate, testDate, address, phone, gender);
        if (TextUtils.isEmpty(existingFileName)) {
            JSONObject data = dataManager.getInstance().createData(info);
            String baseName = name.isEmpty() ? "child" : name;
            existingFileName = dataManager.getInstance().saveData(baseName + System.currentTimeMillis() + ".json", data);
            dataManager.getInstance().createIndex(existingFileName, "Index");
            return existingFileName;
        }

        JSONObject data = dataManager.getInstance().loadData(existingFileName);
        JSONObject savedInfo = data.optJSONObject("info");
        if (savedInfo != null && savedInfo.has("backgroundInfo")) {
            info.put("backgroundInfo", savedInfo.optJSONObject("backgroundInfo"));
        }
        data.put("info", info);
        dataManager.getInstance().saveChildJson(existingFileName, data);
        return existingFileName;
    }

    private void reloadBasicInfo() {
        try {
            JSONObject data = dataManager.getInstance().loadData(existingFileName);
            JSONObject info = data.optJSONObject("info");
            if (info == null) {
                return;
            }
            textName.setText(info.optString("name", ""));
            textBirth.setText(info.optString("birthDate", ""));
            textTestTime.setText(info.optString("testDate", dateFormat.format(new Date())));
            textAddress.setText(info.optString("address", ""));
            textPhone.setText(info.optString("phone", ""));

            String gender = info.optString("gender", "");
            if ("\u7537".equals(gender)) {
                genderGroup.check(R.id.rb_gender_male);
            } else if ("\u5973".equals(gender)) {
                genderGroup.check(R.id.rb_gender_female);
            } else {
                genderGroup.clearCheck();
            }

            familyMemberAdapter.setMembers(FamilyMemberAdapter.fromJsonArray(info.optJSONArray("familyMembers")));
        } catch (Exception ignored) {
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
            return "\u7537";
        }
        if (checkedId == R.id.rb_gender_female) {
            return "\u5973";
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
