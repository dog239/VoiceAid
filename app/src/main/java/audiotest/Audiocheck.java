package audiotest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import login.LoginActivity;
import login.RegisterActivity;
import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.netService.NetInteractUtils;
import utils.dialogUtils;

public class Audiocheck extends AppCompatActivity {

    private MediaRecorder mMediaRecorder = null;
    private Button btnRecord;
    private ProgressBar progressBar;

    private String fileName = null;
    private String filePath = null;
    private String Uid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_check);
        btnRecord = findViewById(R.id.btnRecord);
        progressBar = findViewById(R.id.progressBar);
        Uid = getIntent().getStringExtra("Uid");
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecord.setEnabled(false);
                startRecord();

                new CountDownTimer(20000, 100) { // 20 seconds countdown
                    public void onTick(long millisUntilFinished) {
                        int progress = (int) (20000 - millisUntilFinished) / 200; // Update progress bar every 1 second
                        progressBar.setProgress(progress);
                    }
                    public void onFinish() {
                        stopRecord();
                        btnRecord.setEnabled(true);
                        Intent intent;
                        intent = new Intent(Audiocheck.this, Audiocheckresult.class);
                        intent.putExtra("Uid", Uid);
                        startActivity(intent);
                        finish();
                    }
                }.start();
            }
        });
    }

    public void startRecord() {

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置输出格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            //设置编码格式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            fileName = "check" + ".amr";

            File destDir = new File(getSDPath(this) + "/test/");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            filePath = getSDPath(this) + "/test/" + fileName;
            File file = new File(filePath);
            if (file.exists())
                file.delete();
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.i("failed!", e.getMessage());
        } catch (IOException e) {
            Log.i("failed!", e.getMessage());
        }
    }
    public void stopRecord() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (RuntimeException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
    public static File getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT>=29){
                //Android10之后
                sdDir = context.getExternalFilesDir(null);
            }else {
                sdDir = Environment.getExternalStorageDirectory();// 获取SD卡根目录
            }
        } else {
            sdDir = Environment.getRootDirectory();// 获取跟目录
        }
        return sdDir;
    }
}
