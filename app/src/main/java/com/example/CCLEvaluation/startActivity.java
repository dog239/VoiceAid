package com.example.CCLEvaluation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

import audiotest.Audiocheck;
import history.allhistorylist;
import history.historylist;
import history.privatehistorylist;
import login.LoginActivity;
import utils.Ifileinter;
import utils.dialogUtils;
import utils.dirpath;
import utils.permissionutils;
import utils.sdcard;

public class startActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout test;
    private LinearLayout setting;
    private String[] permissions = {android.Manifest.permission.RECORD_AUDIO};

    private String Uid;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        test = findViewById(R.id.test);
        setting = findViewById(R.id.setting);

        // Clear login status
        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        Uid = preferences.getString("Uid", null);
        boolean isAdmin = preferences.getBoolean("isAdmin", false);


        //申请权限
        permissionutils.getInstance().RequestPermissions(this, permissions, listener);
        createAppInfoDir();
        createAppAudioDir();

        test.setOnClickListener(this);
        setting.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionutils.getInstance().onRequestPesResult(this, requestCode, permissions, grantResults);
    }

    private void createAppInfoDir() {
        File audioDir = sdcard.getInstance(this).createAppFetchDir(Ifileinter.FETCH_DIR_INFO);
        dirpath.PATH_FETCH_DIR_INFO = audioDir.getAbsolutePath();
    }

    private void createAppAudioDir() {
        File audioDir = sdcard.getInstance(this).createAppFetchDir(Ifileinter.FETCH_DIR_AUDIO);
        dirpath.PATH_FETCH_DIR_AUDIO = audioDir.getAbsolutePath();
    }

    permissionutils.OnPermissionCallBackListener listener = new permissionutils.OnPermissionCallBackListener() {
        @Override
        public void onGranted() {
            //授权成功操作
        }

        @Override
        public void onDenied(List<String> deniedPes) {
            //授权失败操作，提示手动开启权限
            permissionutils.getInstance().showDialogTip(startActivity.this);
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.test) {
            Intent intent = new Intent(startActivity.this, MainActivity.class);
            intent.putExtra("Uid", Uid);
            intent.putExtra("isTest",true);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.setting) {
            Intent intent = new Intent(startActivity.this, MainActivity.class);
            intent.putExtra("Uid", Uid);
            intent.putExtra("isTest",false);
            startActivity(intent);
            finish();
        }
    }
}
