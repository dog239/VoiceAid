package history;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import utils.dataManager;
import utils.dialogUtils;

public class showchildinformation extends AppCompatActivity {

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
                Intent intent = new Intent(showchildinformation.this, evmenuactivity.class);
                intent.putExtra("fName", fName);
                String Uid = getIntent().getStringExtra("Uid");
                intent.putExtra("Uid", Uid);
                Boolean old1 = getIntent().getBooleanExtra("old1", false);
                intent.putExtra("old1", old1);
                startActivity(intent);
                finish();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = getIntent().getIntExtra("position", -1);
                showLogoutConfirmationDialog(fName, position);
            }
        });
    }

    private void clearStudentInfoAudio(String fname, int position) throws Exception {
        JSONObject data = dataManager.getInstance().loadData(fname);
        String[] tasks = {"A", "E", "NWR", "PN", "PST", "RE", "RG", "S"};
        for (String task : tasks) {
            JSONArray jsonArray = data.getJSONObject("evaluations").getJSONArray(task);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.has("audioPath") && !object.isNull("audioPath")) {
                    dataManager.getInstance().deleteAudioFile(object.getString("audioPath"));
                }
            }
        }
        dataManager.getInstance().deleteData(fname);
        JSONObject index = dataManager.getInstance().loadData("Index.json");
        int number = index.getInt("number");
        for (int i = number - position; i < number; ++i) {
            index.put(String.valueOf(i), index.getString(String.valueOf(i + 1)));
        }
        index.remove(String.valueOf(number));
        index.put("number", number - 1);
        dataManager.getInstance().saveData("Index.json", index);
        Toast.makeText(this, "儿童信息已删除", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmationDialog(String fname, int position) {
        dialogUtils.showDialog(this,
                "提示信息",
                "您确定要删除该学生的全部信息和答题记录吗？",
                "确认", () -> {
                    try {
                        clearStudentInfoAudio(fname, position);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(showchildinformation.this, historylist.class);
                    startActivity(intent);
                    finish();
                }, "取消", null);
    }

    private void setReadOnly(EditText editText) {
        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }

    private List<FamilyMemberAdapter.FamilyMember> getFamilyMembers(JSONArray array) {
        return FamilyMemberAdapter.fromJsonArray(array);
    }
}
