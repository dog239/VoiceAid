package history;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CCLEvaluation.evmenuactivity;
import com.example.CCLEvaluation.R;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.dataManager;
import utils.dialogUtils;

public class showchildinformation extends AppCompatActivity {

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
            JSONObject object = data.optJSONObject("info");
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
                Intent intent = new Intent(showchildinformation.this, evmenuactivity.class);
                intent.putExtra("fName", fName);
                String Uid = getIntent().getStringExtra("Uid");
                intent.putExtra("Uid", Uid);
                Boolean old1 = getIntent().getBooleanExtra("old1",false);
                intent.putExtra("old1",old1);
                startActivity(intent);
                finish();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = getIntent().getIntExtra("position",-1);
                showLogoutConfirmationDialog(fName,position);
            }
        });
    }

    private void  clearStudentInfoAudio(String fname, int position) throws Exception {
        JSONObject data = dataManager.getInstance().loadData(fname);
        String[] tasks = {"A","E","NWR","PN","PST","RE","RG","S"};
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
        // 保存数据到SharedPreferences
        JSONObject index = dataManager.getInstance().loadData("Index.json");
        int number = index.getInt("number");
        for(int i=number-position;i<number;++i){
            index.put(String.valueOf(i),index.getString(String.valueOf(i+1)));
        }
        index.remove(String.valueOf(number));
        index.put("number",number-1);
        dataManager.getInstance().saveData("Index.json",index);
        Toast.makeText(this,"儿童信息已删除",Toast.LENGTH_SHORT).show();
    }
    private void showLogoutConfirmationDialog(String fname,int position) {
        dialogUtils.showDialog(this, "提示信息", "您确定要删除该学生的全部信息和答题记录吗？",
                "确认", () -> {
                    try {
                        clearStudentInfoAudio(fname,position);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(showchildinformation.this, historylist.class);
                    startActivity(intent);
                    finish();
                }, "取消", null);
    }
}

