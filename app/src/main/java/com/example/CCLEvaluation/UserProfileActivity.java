package com.example.CCLEvaluation;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import utils.dataManager;

public class UserProfileActivity extends AppCompatActivity {

    private static final String EMPTY_TEXT = "未填写";
    private static final String TAG = "UserProfileActivity";

    private String fName;
    private String uid;
    private String childUser;

    private boolean isEditMode = false;
    private boolean hasUnsavedChanges = false;
    private boolean isBindingData = false;

    private JSONObject childData = new JSONObject();
    private JSONObject currentInfo = new JSONObject();

    private TextView tvActionEdit;
    private TextView tvHeaderName;
    private TextView tvHeaderId;
    private TextView tvName;
    private TextView tvGender;
    private TextView tvBirth;
    private TextView tvPhone;
    private TextView tvAddress;
    private EditText etNameEdit;
    private RadioGroup rgGenderEdit;
    private EditText etBirthEdit;
    private EditText etPhoneEdit;
    private EditText etAddressEdit;
    private LinearLayout llFamilyContainer;
    private RecyclerView rvFamilyMembersEdit;
    private Button btnAddFamilyMemberInProfile;
    private Button btnExportChildInfoPdf;
    private Button btnOpenChildDetail;
    private FamilyMemberAdapter familyMemberAdapter;
    private SimpleDateFormat dateFormat;
    private ActivityResultLauncher<String> createPdfDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        createPdfDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                this::handlePdfDocumentCreated
        );

        initViews();
        initData();
        bindEvents();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                attemptLeavePage();
            }
        });
        loadAndBindChildData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(fName) && !isEditMode) {
            loadAndBindChildData();
        }
    }

    private void initViews() {
        tvActionEdit = findViewById(R.id.tv_action_edit);
        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderId = findViewById(R.id.tv_header_id);
        tvName = findViewById(R.id.tv_name);
        tvGender = findViewById(R.id.tv_gender);
        tvBirth = findViewById(R.id.tv_birth);
        tvPhone = findViewById(R.id.tv_phone);
        tvAddress = findViewById(R.id.tv_address);
        etNameEdit = findViewById(R.id.et_name_edit);
        rgGenderEdit = findViewById(R.id.rg_gender_edit);
        etBirthEdit = findViewById(R.id.et_birth_edit);
        etPhoneEdit = findViewById(R.id.et_phone_edit);
        etAddressEdit = findViewById(R.id.et_address_edit);
        llFamilyContainer = findViewById(R.id.ll_family_container);
        rvFamilyMembersEdit = findViewById(R.id.rv_family_members_edit);
        btnAddFamilyMemberInProfile = findViewById(R.id.btn_add_family_member_in_profile);
        btnExportChildInfoPdf = findViewById(R.id.btn_export_child_info_pdf);
        btnOpenChildDetail = findViewById(R.id.btn_open_child_detail);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> attemptLeavePage());

        rvFamilyMembersEdit.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rvFamilyMembersEdit.setNestedScrollingEnabled(false);
        rvFamilyMembersEdit.setItemAnimator(null);
        familyMemberAdapter = new FamilyMemberAdapter(new ArrayList<>(), this::handleDeleteMember);
        familyMemberAdapter.setOnMemberChangedListener(this::markUnsavedChanges);
        rvFamilyMembersEdit.setAdapter(familyMemberAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private void initData() {
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");
    }

    private void bindEvents() {
        tvActionEdit.setOnClickListener(v -> {
            if (isEditMode) {
                saveProfile();
            } else {
                enterEditMode();
            }
        });
        btnExportChildInfoPdf.setOnClickListener(v -> exportChildInfoPdf());
        btnOpenChildDetail.setOnClickListener(v -> openChildDetailEditor());
        btnAddFamilyMemberInProfile.setOnClickListener(v ->
                familyMemberAdapter.addMember(new FamilyMemberAdapter.FamilyMember()));

        etBirthEdit.setKeyListener(null);
        etBirthEdit.setOnClickListener(v -> showBirthDatePicker());

        TextWatcher editWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                markUnsavedChanges();
            }
        };
        etNameEdit.addTextChangedListener(editWatcher);
        etBirthEdit.addTextChangedListener(editWatcher);
        etPhoneEdit.addTextChangedListener(editWatcher);
        etAddressEdit.addTextChangedListener(editWatcher);
        rgGenderEdit.setOnCheckedChangeListener((group, checkedId) -> markUnsavedChanges());
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_profile) {
            return true;
        }
        if (isEditMode || hasUnsavedChanges) {
            Toast.makeText(this, "请先保存后再操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (item.getItemId() == R.id.nav_modules) {
            Intent intent = new Intent(this, AssessmentModulesActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
            return true;
        }
        if (item.getItemId() == R.id.nav_reports) {
            Intent intent = new Intent(this, SelectReportActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    private void loadAndBindChildData() {
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(this, "未找到当前儿童记录。", Toast.LENGTH_SHORT).show();
            btnOpenChildDetail.setEnabled(false);
            btnExportChildInfoPdf.setEnabled(false);
            tvActionEdit.setEnabled(false);
            return;
        }

        try {
            childData = dataManager.getInstance().loadData(fName);
            currentInfo = childData.optJSONObject("info");
            if (currentInfo == null) {
                currentInfo = new JSONObject();
            }
            bindInfoToViews(currentInfo);
        } catch (Exception e) {
            Log.e(TAG, "load child data failed", e);
            childData = new JSONObject();
            currentInfo = new JSONObject();
            Toast.makeText(this, "数据加载失败。", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindInfoToViews(JSONObject info) {
        isBindingData = true;
        try {
            String name = info.optString("name", "");
            String gender = info.optString("gender", "");
            String birthDate = info.optString("birthDate", "");
            String phone = info.optString("phone", "");
            String address = info.optString("address", "");

            tvHeaderName.setText(displayValue(name));
            tvHeaderId.setText("ID: " + (!TextUtils.isEmpty(childUser) ? childUser : fName));
            tvName.setText(displayValue(name));
            tvGender.setText(displayValue(gender));
            tvBirth.setText(displayValue(birthDate));
            tvPhone.setText(displayValue(phone));
            tvAddress.setText(displayValue(address));

            etNameEdit.setText(name);
            etBirthEdit.setText(birthDate);
            etPhoneEdit.setText(phone);
            etAddressEdit.setText(address);
            setSelectedGender(gender);

            familyMemberAdapter.setMembers(FamilyMemberAdapter.fromJsonArray(info.optJSONArray("familyMembers")));
            renderFamilyMembersDisplay(info.optJSONArray("familyMembers"));
        } finally {
            isBindingData = false;
        }
    }

    private void enterEditMode() {
        bindEditorFromCurrentInfo();
        setEditMode(true);
        hasUnsavedChanges = false;
    }

    private void bindEditorFromCurrentInfo() {
        bindInfoToViews(currentInfo == null ? new JSONObject() : currentInfo);
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        tvActionEdit.setText(editMode ? "保存" : "编辑");

        int displayVisibility = editMode ? View.GONE : View.VISIBLE;
        int editVisibility = editMode ? View.VISIBLE : View.GONE;

        tvName.setVisibility(displayVisibility);
        tvGender.setVisibility(displayVisibility);
        tvBirth.setVisibility(displayVisibility);
        tvPhone.setVisibility(displayVisibility);
        tvAddress.setVisibility(displayVisibility);

        etNameEdit.setVisibility(editVisibility);
        rgGenderEdit.setVisibility(editVisibility);
        etBirthEdit.setVisibility(editVisibility);
        etPhoneEdit.setVisibility(editVisibility);
        etAddressEdit.setVisibility(editVisibility);

        llFamilyContainer.setVisibility(displayVisibility);
        rvFamilyMembersEdit.setVisibility(editVisibility);
        btnAddFamilyMemberInProfile.setVisibility(editVisibility);
    }

    private void saveProfile() {
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(this, "缺少儿童记录。", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject latestData = dataManager.getInstance().loadData(fName);
            JSONObject info = latestData.optJSONObject("info");
            if (info == null) {
                info = new JSONObject();
                latestData.put("info", info);
            }

            info.put("name", etNameEdit.getText().toString().trim());
            info.put("gender", getSelectedGender());
            info.put("birthDate", etBirthEdit.getText().toString().trim());
            info.put("address", etAddressEdit.getText().toString().trim());
            info.put("phone", etPhoneEdit.getText().toString().trim());
            info.put("familyMembers", FamilyMemberAdapter.toJsonArray(familyMemberAdapter.getMembers()));

            dataManager.getInstance().saveChildJson(fName, latestData);
            childData = latestData;
            currentInfo = info;
            bindInfoToViews(currentInfo);
            hasUnsavedChanges = false;
            setEditMode(false);
            Toast.makeText(this, "保存成功。", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "save profile failed", e);
            Toast.makeText(this, "保存失败，请重试。", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportChildInfoPdf() {
        if (isEditMode || hasUnsavedChanges) {
            Toast.makeText(this, "请先保存后再导出", Toast.LENGTH_SHORT).show();
            return;
        }
        createPdfDocumentLauncher.launch(buildDefaultPdfName());
    }

    private void handlePdfDocumentCreated(Uri uri) {
        if (uri == null) {
            Log.d(TAG, "export child info pdf cancelled: uri is null");
            return;
        }

        if (TextUtils.isEmpty(fName)) {
            Log.e(TAG, "export child info pdf failed: child file name is empty");
            Toast.makeText(this, "导出失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "loading child data for pdf export, file=" + fName + ", uri=" + uri);
            JSONObject latestData = dataManager.getInstance().loadData(fName);
            Log.d(TAG, "child data loaded for pdf export");
            PdfGenerator.writeChildInfoPdf(this, uri, latestData);
            Log.d(TAG, "child info pdf exported successfully");
            Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "export child info pdf failed: invalid params or output target", e);
            Toast.makeText(this, "导出失败，请重试", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "export child info pdf failed: file creation or output stream error", e);
            Toast.makeText(this, "导出失败，请重试", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e(TAG, "export child info pdf failed: child data parse error", e);
            Toast.makeText(this, "导出失败，请重试", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "export child info pdf failed", e);
            Toast.makeText(this, "导出失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildDefaultPdfName() {
        String name = currentInfo == null ? "" : currentInfo.optString("name", "");
        name = sanitizeFileName(name);
        String date = new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date());
        return "儿童信息_" + name + "_" + date + ".pdf";
    }

    private void openChildDetailEditor() {
        if (isEditMode || hasUnsavedChanges) {
            Toast.makeText(this, "请先保存后再操作", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(this, "缺少儿童记录。", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ChildDetailEditActivity.class);
        intent.putExtra(ChildDetailEditActivity.EXTRA_FILE_NAME, fName);
        startActivity(intent);
    }

    private void renderFamilyMembersDisplay(JSONArray familyMembers) {
        llFamilyContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (familyMembers == null || familyMembers.length() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("暂无家庭成员信息");
            emptyView.setTextColor(0xFF64748B);
            emptyView.setTextSize(14f);
            emptyView.setPadding(16, 16, 16, 16);
            llFamilyContainer.addView(emptyView);
            return;
        }

        for (int i = 0; i < familyMembers.length(); i++) {
            JSONObject member = familyMembers.optJSONObject(i);
            if (member == null) {
                continue;
            }
            View itemView = inflater.inflate(R.layout.item_profile_family_member, llFamilyContainer, false);
            TextView tvMemberName = itemView.findViewById(R.id.tv_member_name);
            TextView tvMemberRelation = itemView.findViewById(R.id.tv_member_relation);
            TextView tvMemberPhone = itemView.findViewById(R.id.tv_member_phone);
            TextView tvMemberOccupation = itemView.findViewById(R.id.tv_member_occupation);
            TextView tvMemberEducation = itemView.findViewById(R.id.tv_member_education);
            View detailLayout = itemView.findViewById(R.id.layout_member_detail);
            ImageView arrowView = itemView.findViewById(R.id.iv_member_arrow);

            tvMemberName.setText(displayValue(member.optString("member_name", "")));
            tvMemberRelation.setText("关系: " + displayValue(member.optString("relation", "")));
            tvMemberPhone.setText("联系电话: " + displayValue(member.optString("member_phone", "")));
            tvMemberOccupation.setText("职业: " + displayValue(member.optString("occupation", "")));
            tvMemberEducation.setText("学历: " + displayValue(member.optString("education", "")));

            View.OnClickListener toggleListener = v -> {
                boolean expanded = detailLayout.getVisibility() == View.VISIBLE;
                detailLayout.setVisibility(expanded ? View.GONE : View.VISIBLE);
                arrowView.animate().rotation(expanded ? 0f : 90f).setDuration(180).start();
            };
            itemView.setOnClickListener(toggleListener);
            arrowView.setOnClickListener(toggleListener);

            llFamilyContainer.addView(itemView);
        }

        if (llFamilyContainer.getChildCount() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("暂无家庭成员信息");
            emptyView.setTextColor(0xFF64748B);
            emptyView.setTextSize(14f);
            emptyView.setPadding(16, 16, 16, 16);
            llFamilyContainer.addView(emptyView);
        }
    }

    private void showBirthDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String current = etBirthEdit.getText().toString().trim();
        if (!current.isEmpty()) {
            try {
                Date parsed = dateFormat.parse(current);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (Exception ignored) {
            }
        }
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    etBirthEdit.setText(dateFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void handleDeleteMember(int position) {
        familyMemberAdapter.removeMember(position);
    }

    private void markUnsavedChanges() {
        if (isBindingData) {
            return;
        }
        hasUnsavedChanges = true;
    }

    private void attemptLeavePage() {
        if (!isEditMode && !hasUnsavedChanges) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("当前修改尚未保存，是否放弃修改并离开？")
                .setNegativeButton("继续编辑", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("放弃离开", (dialog, which) -> finish())
                .show();
    }

    private String sanitizeFileName(String raw) {
        String value = raw == null ? "" : raw.trim();
        value = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return value.isEmpty() ? "未命名" : value;
    }

    private void setSelectedGender(String gender) {
        if ("男".equals(gender)) {
            rgGenderEdit.check(R.id.rb_gender_edit_male);
        } else if ("女".equals(gender)) {
            rgGenderEdit.check(R.id.rb_gender_edit_female);
        } else {
            rgGenderEdit.clearCheck();
        }
    }

    private String getSelectedGender() {
        int checkedId = rgGenderEdit.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_gender_edit_male) {
            return "男";
        }
        if (checkedId == R.id.rb_gender_edit_female) {
            return "女";
        }
        return "";
    }

    private String displayValue(String value) {
        return TextUtils.isEmpty(value) ? EMPTY_TEXT : value;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
