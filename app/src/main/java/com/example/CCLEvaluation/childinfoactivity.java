package com.example.CCLEvaluation;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
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
    private EditText textAddress;
    private EditText textPhone;
    private RadioGroup genderGroup;
    private RecyclerView familyRecyclerView;
    private FamilyMemberAdapter familyMemberAdapter;
    private SimpleDateFormat dateFormat;


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
        textAddress = findViewById(R.id.et_address);
        textPhone = findViewById(R.id.et_phone);
        genderGroup = findViewById(R.id.rg_gender);
        familyRecyclerView = findViewById(R.id.rv_family_members);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textBirth.setKeyListener(null);
        textBirth.setOnClickListener(v -> showBirthDatePicker());

        familyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        familyMemberAdapter = new FamilyMemberAdapter(createDefaultMembers(), this::handleDeleteMember);
        familyRecyclerView.setAdapter(familyMemberAdapter);

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
                info.put("class", "");
                info.put("serialNumber", "");
                info.put("testDate", dateFormat.format(new Date()));
                info.put("testLocation", "");
                info.put("examiner", "");

                Intent intent = new Intent(childinfoactivity.this, evmenuactivity.class);
                JSONObject data = dataManager.getInstance().createData(info);
                String baseName = tNa.isEmpty() ? "child" : tNa;
                String fName = dataManager.getInstance().saveData(baseName + System.currentTimeMillis() + ".json", data);
                dataManager.getInstance().createIndex(fName,"Index");
                intent.putExtra("fName", fName);
                String Uid = getIntent().getStringExtra("Uid");
                intent.putExtra("Uid", Uid);
                intent.putExtra("private",true);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }

        } else if (v.getId()==R.id.btn_delete) {
            clearForm();
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show();
        }

    }

    private void clearForm() {
        textName.setText("");
        genderGroup.clearCheck();
        textBirth.setText("");
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
