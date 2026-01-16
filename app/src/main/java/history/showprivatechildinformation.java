package history;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CCLEvaluation.evmenuactivity;
import com.example.CCLEvaluation.R;

import org.json.JSONObject;

import utils.dataManager;
import utils.dialogUtils;
import utils.Netinteractutils;

public class showprivatechildinformation extends AppCompatActivity {

    private LinearLayout TableChild;
    private Button buttonSave;
    private Button buttonDelete;
    private EditText textName;
    private EditText textClass;
    private EditText textNumber;
    private EditText textBirth;
    private EditText textTestTime;
    private EditText textTestPlace;
    private EditText textTester;
    private String fName;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_info);


        TableChild = findViewById(R.id.tab_child);
        buttonSave = findViewById(R.id.btn_save);
        buttonDelete = findViewById(R.id.btn_delete);
        textName = findViewById(R.id.et_name);
        textClass = findViewById(R.id.et_class);
        textNumber = findViewById(R.id.et_number);
        textBirth = findViewById(R.id.et_birth);
        textTestTime = findViewById(R.id.et_test_time);
        textTestPlace = findViewById(R.id.et_place);
        textTester = findViewById(R.id.et_tester);


        fName = getIntent().getStringExtra("fName");
        try {
            JSONObject data = dataManager.getInstance().loadData(fName);
            JSONObject object = data.getJSONObject("info");
            Log.d("wwwww",data.toString());
            dataManager.getInstance().saveData(fName,data);
            assert object != null;
            textName.setText(object.getString("name"));
            textName.setKeyListener(null);
            textClass.setText(object.getString("class"));
            textClass.setKeyListener(null);
            textNumber.setText(object.getString("serialNumber"));
            textNumber.setKeyListener(null);
            textBirth.setText(object.getString("birthDate"));
            textBirth.setKeyListener(null);
            textTestTime.setText(object.getString("testDate"));
            textTestTime.setKeyListener(null);
            textTestPlace.setText(object.getString("testLocation"));
            textTestPlace.setKeyListener(null);
            textTester.setText(object.getString("examiner"));
            textTester.setKeyListener(null);
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
                intent.putExtra("private",true);
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
                position = getIntent().getIntExtra("position",-1);
                String Uid = getIntent().getStringExtra("Uid");
                showLogoutConfirmationDialog(fName,Uid);
            }
        });
    }

    private void  clearStudentInfoAudio(String fname) throws Exception {
        dataManager.getInstance().deleteData(fname);

        // 使用下划线作为分隔符来分割字符串
        String[] parts = fname.split("_");
        // 检查分割后的数组长度，确保至少有两个部分
        if (parts.length >= 2) {
            // 第一部分是我们想要的"23"
            String part1 = parts[0];
            // 第二部分是我们想要的"100008"
            String part2 = parts[1].split("\\.")[0]; // 如果需要确保去除.json后缀
            Netinteractutils.getInstance(showprivatechildinformation.this).deleteEvaluation(part2,part1);
        }
        Toast.makeText(showprivatechildinformation.this,"儿童信息已删除",Toast.LENGTH_SHORT).show();
    }
    private void showLogoutConfirmationDialog(String fname,String uid) {
        dialogUtils.showDialog(this, "提示信息", "您确定要删除该学生的全部信息和答题记录吗？",
                "确认", () -> {
                    try {
                        clearStudentInfoAudio(fname);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(showprivatechildinformation.this, privatehistorylist.class);
                    intent.putExtra("Uid",uid);
                    startActivity(intent);
                    finish();
                }, "取消", null);
    }
}

