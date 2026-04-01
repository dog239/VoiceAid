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

import com.example.CCLEvaluation.AssessmentModulesActivity;
import com.example.CCLEvaluation.ChildDetailEditActivity;
import com.example.CCLEvaluation.FamilyMemberAdapter;
import com.example.CCLEvaluation.R;

import org.json.JSONObject;

import java.util.List;

import utils.NetInteractUtils;
import utils.dataManager;
import utils.dialogUtils;

public class showprivatechildinformation extends AppCompatActivity {

    private Button buttonSave;
    private Button buttonDelete;
    private Button buttonAddMember;
    private Button buttonAddBackgroundInfo;
    private EditText textName;
    private EditText textBirth;
    private EditText textTestTime;
    private EditText textAddress;
    private EditText textPhone;
    private RadioButton genderMale;
    private RadioButton genderFemale;
    private RecyclerView familyRecyclerView;
    private FamilyMemberAdapter familyMemberAdapter;
    private String fName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_info);

        initViews();
        fName = getIntent().getStringExtra("fName");
        familyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        familyMemberAdapter = new FamilyMemberAdapter(getFamilyMembers(null), ignored -> {});
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

        buttonSave.setText("\u8FDB\u5165\u6D4B\u8BD5");
        buttonDelete.setText("\u5220\u9664\u513F\u7AE5\u4FE1\u606F");
        buttonSave.setOnClickListener(v -> openAssessment());
        buttonDelete.setOnClickListener(v -> {
            String uid = getIntent().getStringExtra("Uid");
            showLogoutConfirmationDialog(fName, uid);
        });
        buttonAddBackgroundInfo.setOnClickListener(v -> openBackgroundEditor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildInfo();
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
        genderMale = findViewById(R.id.rb_gender_male);
        genderFemale = findViewById(R.id.rb_gender_female);
        familyRecyclerView = findViewById(R.id.rv_family_members);
    }

    private void loadChildInfo() {
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject info = data.optJSONObject("info");
            if (info == null) {
                info = new JSONObject();
            }
            textName.setText(info.optString("name", ""));
            textBirth.setText(info.optString("birthDate", ""));
            textTestTime.setText(info.optString("testDate", ""));
            textAddress.setText(info.optString("address", ""));
            textPhone.setText(info.optString("phone", ""));

            String gender = info.optString("gender", "");
            genderMale.setChecked("\u7537".equals(gender));
            genderFemale.setChecked("\u5973".equals(gender));

            familyMemberAdapter.setMembers(getFamilyMembers(info.optJSONArray("familyMembers")));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load child info.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBackgroundEditor() {
        Intent intent = new Intent(this, ChildDetailEditActivity.class);
        intent.putExtra(ChildDetailEditActivity.EXTRA_FILE_NAME, fName);
        startActivity(intent);
    }

    private void openAssessment() {
        Intent intent = new Intent(showprivatechildinformation.this, AssessmentModulesActivity.class);
        intent.putExtra("fName", fName);
        intent.putExtra("reload", true);
        intent.putExtra("private", true);
        String uid = getIntent().getStringExtra("Uid");
        intent.putExtra("Uid", uid);
        String childUser = getIntent().getStringExtra("childID");
        intent.putExtra("childID", childUser);
        startActivity(intent);
        finish();
    }

    private void clearStudentInfoAudio(String fname) throws Exception {
        dataManager.getInstance().deleteData(fname);

        String[] parts = fname.split("_");
        if (parts.length >= 2) {
            String part1 = parts[0];
            String part2 = parts[1].split("\\.")[0];
            NetInteractUtils.getInstance(showprivatechildinformation.this).deleteEvaluation(part2, part1);
        }
        Toast.makeText(showprivatechildinformation.this, "Child deleted.", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmationDialog(String fname, String uid) {
        dialogUtils.showDialog(this,
                "\u63D0\u793A\u4FE1\u606F",
                "\u60A8\u786E\u5B9A\u8981\u5220\u9664\u8BE5\u513F\u7AE5\u7684\u5168\u90E8\u4FE1\u606F\u548C\u7B54\u9898\u8BB0\u5F55\u5417\uFF1F",
                "\u786E\u8BA4", () -> {
                    try {
                        clearStudentInfoAudio(fname);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(showprivatechildinformation.this, privatehistorylist.class);
                    intent.putExtra("Uid", uid);
                    startActivity(intent);
                    finish();
                }, "\u53D6\u6D88", null);
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
