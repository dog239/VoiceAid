package audiotest;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;


import com.example.CCLEvaluation.mainactivity;
import com.example.CCLEvaluation.R;

import java.io.File;
import java.io.IOException;

public class Audiocheckresult extends Audiocheck {
    private MediaPlayer mediaPlayer;
    private Button btnPlay;
    private Button btnSatisfied;
    private Button btnNotSatisfied;

    private ProgressBar progressBar;
    private String Uid;

    private String fileName = null;

    private String filePath = null;
    private CountDownTimer countDownTimer = new CountDownTimer(20000, 100) {
        public void onTick(long millisUntilFinished) {
            int progress = (int) (20000 - millisUntilFinished) / 200; // Update progress bar every 1 second
            progressBar.setProgress(progress);
        }

        public void onFinish() {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_check_result);
        Uid = getIntent().getStringExtra("Uid");
        btnPlay = findViewById(R.id.btnPlay);
        btnSatisfied = findViewById(R.id.btnSatisfied);
        btnNotSatisfied = findViewById(R.id.btnNotSatisfied);
        progressBar = findViewById(R.id.progressBar2);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setProgress(0);
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                playRecording();
            }
        });

        btnSatisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Intent intent;
                intent = new Intent( Audiocheckresult.this, mainactivity.class);
                intent.putExtra("Uid", Uid);
                intent.putExtra("isTest", false);
                startActivity(intent);
                finish();
            }
        });

        btnNotSatisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Intent intent;
                intent = new Intent( Audiocheckresult.this, Audiocheck.class);
                intent.putExtra("Uid", Uid);
                startActivity(intent);
                finish();
            }
        });
    }

    private void playRecording() {

        fileName = "check" + ".amr";
        filePath = getSDPath(this) + "/test/" + fileName;
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(filePath);
            //toastUtil.Toastmsg(AudioCheckResult.this, Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.3gp");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
       countDownTimer.start();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public static File getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT >= 29) {
                //Android10之后
                sdDir = context.getExternalFilesDir(null);
            } else {
                sdDir = Environment.getExternalStorageDirectory();// 获取SD卡根目录
            }
        } else {
            sdDir = Environment.getRootDirectory();// 获取跟目录
        }
        return sdDir;
    }

}
