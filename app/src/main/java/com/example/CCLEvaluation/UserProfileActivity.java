package com.example.CCLEvaluation;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.dataManager;

public class UserProfileActivity extends AppCompatActivity {

    private String fName;
    private String uid;
    private String childUser;

    private TextView tvHeaderName;
    private TextView tvHeaderId;
    private TextView tvName;
    private TextView tvGender;
    private TextView tvBirth;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvFamilyName;
    private TextView tvFamilyRelation;
    private LinearLayout llFamilyContainer;
    private Button btnOpenChildDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        initData();
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderId = findViewById(R.id.tv_header_id);
        tvName = findViewById(R.id.tv_name);
        tvGender = findViewById(R.id.tv_gender);
        tvBirth = findViewById(R.id.tv_birth);
        tvPhone = findViewById(R.id.tv_phone);
        tvAddress = findViewById(R.id.tv_address);
        tvFamilyName = findViewById(R.id.tv_family_name);
        tvFamilyRelation = findViewById(R.id.tv_family_relation);
        llFamilyContainer = findViewById(R.id.ll_family_container);
        btnOpenChildDetail = findViewById(R.id.btn_open_child_detail);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnOpenChildDetail.setOnClickListener(v -> openChildDetailEditor());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_profile) {
            return true;
        } else if (item.getItemId() == R.id.nav_modules) {
            Intent intent = new Intent(this, AssessmentModulesActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        } else if (item.getItemId() == R.id.nav_reports) {
            Intent intent = new Intent(this, SelectReportActivity.class);
            intent.putExtra("fName", fName);
            intent.putExtra("Uid", uid);
            intent.putExtra("childID", childUser);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    private void initData() {
        fName = getIntent().getStringExtra("fName");
        uid = getIntent().getStringExtra("Uid");
        childUser = getIntent().getStringExtra("childID");

        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "未找到当前个案信息。", Toast.LENGTH_SHORT).show();
            btnOpenChildDetail.setEnabled(false);
            return;
        }

        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject info = data.optJSONObject("info");
            if (info == null) {
                info = new JSONObject();
            }

            String name = info.optString("name", "未填写");
            String gender = info.optString("gender", "未填写");
            String birthDate = info.optString("birthDate", "未填写");
            String phone = info.optString("phone", "未填写");
            String address = info.optString("address", "未填写");
            String displayId = "ID: " + ((childUser != null && !childUser.trim().isEmpty()) ? childUser : fName);

            tvHeaderName.setText(name);
            tvHeaderId.setText(displayId);
            tvName.setText(name);
            tvGender.setText(gender);
            tvBirth.setText(birthDate);
            tvPhone.setText(phone);
            tvAddress.setText(address);

            JSONArray familyMembers = info.optJSONArray("familyMembers");
            if (familyMembers != null && familyMembers.length() > 0) {
                JSONObject member = familyMembers.optJSONObject(0);
                if (member != null) {
                    tvFamilyName.setText(member.optString("member_name", "未填写"));
                    tvFamilyRelation.setText("关系: " + member.optString("relation", "未填写"));
                }
            } else {
                tvFamilyName.setText("暂无家庭成员信息");
                tvFamilyRelation.setText("");
            }
        } catch (Exception e) {
            Toast.makeText(this, "数据加载失败。", Toast.LENGTH_SHORT).show();
        }
    }

    private void openChildDetailEditor() {
        if (fName == null || fName.trim().isEmpty()) {
            Toast.makeText(this, "缺少儿童记录。", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ChildDetailEditActivity.class);
        intent.putExtra(ChildDetailEditActivity.EXTRA_FILE_NAME, fName);
        startActivity(intent);
    }
}
