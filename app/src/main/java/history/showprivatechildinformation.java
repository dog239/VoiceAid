package history;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CCLEvaluation.evmenuactivity;
import com.example.CCLEvaluation.FamilyMemberAdapter;
import com.example.CCLEvaluation.R;

import org.json.JSONObject;

import java.util.List;

import utils.dataManager;
import utils.dialogUtils;
import utils.Netinteractutils;

public class showprivatechildinformation extends AppCompatActivity {

    private Button buttonSave;
    private Button buttonDelete;
    private Button buttonAddMember;
    private EditText textName;
    private EditText textBirth;
    private EditText textTestTime;
    private EditText textAddress;
    private EditText textPhone;
    private RadioButton genderMale;
    private RadioButton genderFemale;
    private RecyclerView familyRecyclerView;
    private String fName;
    private int position;

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
        genderMale = findViewById(R.id.rb_gender_male);
        genderFemale = findViewById(R.id.rb_gender_female);
        familyRecyclerView = findViewById(R.id.rv_family_members);

        fName = getIntent().getStringExtra("fName");
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject object = data.optJSONObject("info");
            Log.d("wwwww", data.toString());
            dataManager.getInstance().saveData(fName, data);
            if (object == null) {
                object = new JSONObject();
            }
            textName.setText(object.optString("name", ""));
            textBirth.setText(object.optString("birthDate", ""));
            textTestTime.setText(object.optString("testDate", ""));
            textAddress.setText(object.optString("address", ""));
            textPhone.setText(object.optString("phone", ""));
            String gender = object.optString("gender", "");
            if ("男".equals(gender)) {
                genderMale.setChecked(true);
            } else if ("女".equals(gender)) {
                genderFemale.setChecked(true);
            }

            familyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            FamilyMemberAdapter familyMemberAdapter = new FamilyMemberAdapter(
                    getFamilyMembers(object.optJSONArray("familyMembers")),
                    position -> {}
            );
            familyMemberAdapter.setReadOnly(true);
            familyRecyclerView.setAdapter(familyMemberAdapter);

            setReadOnly(textName);
            setReadOnly(textBirth);
            setReadOnly(textTestTime);
            setReadOnly(textAddress);
            setReadOnly(textPhone);
            genderMale.setEnabled(false);
            genderFemale.setEnabled(false);
            buttonAddMember.setVisibility(View.GONE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        buttonSave.setText("进入测试");
        buttonDelete.setText("删除儿童信息");
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(showprivatechildinformation.this, evmenuactivity.class);
                intent.putExtra("fName", fName);
                intent.putExtra("reload", true);
                intent.putExtra("private", true);
                String Uid = getIntent().getStringExtra("Uid");
                intent.putExtra("Uid", Uid);
                String childUser = getIntent().getStringExtra("childID");
                intent.putExtra("childID", childUser);
                startActivity(intent);
                finish();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = getIntent().getIntExtra("position", -1);
                String Uid = getIntent().getStringExtra("Uid");
                showLogoutConfirmationDialog(fName, Uid);
            }
        });
    }

    private void clearStudentInfoAudio(String fname) throws Exception {
        dataManager.getInstance().deleteData(fname);

        String[] parts = fname.split("_");
        if (parts.length >= 2) {
            String part1 = parts[0];
            String part2 = parts[1].split("\\.")[0];
            Netinteractutils.getInstance(showprivatechildinformation.this).deleteEvaluation(part2, part1);
        }
        Toast.makeText(showprivatechildinformation.this, "儿童信息已删除", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmationDialog(String fname, String uid) {
        dialogUtils.showDialog(this,
                "提示信息",
                "您确定要删除该学生的全部信息和答题记录吗？",
                "确认", () -> {
                    try {
                        clearStudentInfoAudio(fname);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(showprivatechildinformation.this, privatehistorylist.class);
                    intent.putExtra("Uid", uid);
                    startActivity(intent);
                    finish();
                }, "取消", null);
    }

    private void setReadOnly(EditText editText) {
        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }

    private List<FamilyMemberAdapter.FamilyMember> getFamilyMembers(org.json.JSONArray array) {
        return FamilyMemberAdapter.fromJsonArray(array);
    }
}
