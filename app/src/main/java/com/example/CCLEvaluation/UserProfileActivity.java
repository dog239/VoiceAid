package com.example.CCLEvaluation;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
    
    private TextView tvHeaderName, tvHeaderId;
    private TextView tvName, tvGender, tvBirth, tvPhone, tvAddress;
    private TextView tvFamilyName, tvFamilyRelation;
    private LinearLayout llFamilyContainer;

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

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // Just finish to go back to previous activity

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

        if (fName == null) {
            Toast.makeText(this, "未找到用户信息", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject info = data.optJSONObject("info");
            
            if (info != null) {
                String name = info.optString("name", "N/A");
                String gender = info.optString("gender", "N/A");
                String birthDate = info.optString("birthDate", "N/A");
                String phone = info.optString("phone", "N/A");
                String address = info.optString("address", "N/A");
                // ID logic: maybe use childID or generate one? The layout has a placeholder ID.
                // Using childUser as ID if available, else name.
                String displayId = "ID: " + (childUser != null ? childUser : name);

                tvHeaderName.setText(name);
                tvHeaderId.setText(displayId);
                
                tvName.setText(name);
                tvGender.setText(gender);
                tvBirth.setText(birthDate);
                tvPhone.setText(phone);
                tvAddress.setText(address);
                
                // Family Members
                JSONArray familyMembers = info.optJSONArray("familyMembers");
                if (familyMembers != null && familyMembers.length() > 0) {
                    JSONObject member = familyMembers.getJSONObject(0);
                    tvFamilyName.setText(member.optString("name", "N/A"));
                    tvFamilyRelation.setText("关系: " + member.optString("relation", "N/A"));
                    // Show container if hidden (it's visible by default in XML)
                } else {
                    // Hide family section if no members? Or clear text.
                    tvFamilyName.setText("无");
                    tvFamilyRelation.setText("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
        }
    }
}
