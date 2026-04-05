package com.example.CCLEvaluation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import utils.AudioRecorder;
import utils.dirpath;
import utils.Ifileinter;
import utils.sdcard;
import utils.net.NetService;
import utils.net.NetServiceProvider;

public class ma extends AppCompatActivity implements View.OnClickListener {

    private Button captcha, register, login, upload, download, record, stop, play;
    private EditText cEdit;
    private TextView loading;

    private String jsonString;
    private String childUser;
    private MediaPlayer player;
    private NetService netService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.am);

        captcha = findViewById(R.id.i1);
        cEdit = findViewById(R.id.cEdit);
        register = findViewById(R.id.i2);
        login = findViewById(R.id.i3);
        upload = findViewById(R.id.i4);
        download = findViewById(R.id.i5);
        record = findViewById(R.id.i6);
        stop = findViewById(R.id.i7);
        play = findViewById(R.id.i8);
        loading = findViewById(R.id.loading);


        jsonString = //以下为测试儿童的信息
                "{\"info\":{\"name\":\"liziming\",\"class\":\"3-2\",\"serialNumber\":\"12345\"," +
                "\"birthDate\":\"2023-01-01\",\"testDate\":\"2024-07-02\",\"testLocation\":\"home\",\"examiner\":\"lzj\"}," +
                //以下为每道题的答题情况,[]为题目，数量为n，如某题是语音题，则需要有audioPath，并且同步上传该录音
                "\"evaluations\":" +
                        "{\"A\":[]," +
                        "\"E\":[{\"num\":1,\"target\":\"衣服\",\"result\":true,\"audioPath\":\"\\/storage\\/emulated\\/0\\/Android\\/data\\/com.example.CCLEvaluation\\/files\\/CCLEvaluation\\/audio\\/20240702_05_25_51.amr\",\"time\":\"00:02\"},{\"num\":2,\"target\":\"马\",\"result\":false,\"audioPath\":\"\\/storage\\/emulated\\/0\\/Android\\/data\\/com.example.CCLEvaluation\\/files\\/CCLEvaluation\\/audio\\/20240702_05_25_53.amr\",\"time\":\"00:02\"},{\"num\":3,\"target\":\"绳子\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":4,\"target\":\"船\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":5,\"target\":\"勺子\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":6,\"target\":\"电视\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":7,\"target\":\"袜子\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":8,\"target\":\"自行车\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":9,\"target\":\"羊\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":10,\"target\":\"皮带\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":11,\"target\":\"椅子\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":12,\"target\":\"桶\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":13,\"target\":\"梳子\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":14,\"target\":\"手套\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":15,\"target\":\"香蕉\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":16,\"target\":\"扫帚\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":17,\"target\":\"剪刀\",\"result\":null,\"audioPath\":null,\"time\":null}]," +
                        "\"NWR\":[]," +
                        "\"PN\":[]," +
                        "\"PST\":[]," +
                        "\"RE\":[]," +
                        "\"RG\":[]," +
                        "\"S\":[{\"num\":1,\"question\":\"咖啡和茶\",\"answer\":\"饮料\",\"result\":true,\"audioPath\":\"\\/storage\\/emulated\\/0\\/Android\\/data\\/com.example.CCLEvaluation\\/files\\/CCLEvaluation\\/audio\\/20240702_05_26_01.amr\",\"time\":\"00:01\"},{\"num\":2,\"question\":\"白菜和黄瓜\",\"answer\":\"蔬菜\",\"result\":false,\"audioPath\":\"\\/storage\\/emulated\\/0\\/Android\\/data\\/com.example.CCLEvaluation\\/files\\/CCLEvaluation\\/audio\\/20240702_05_26_03.amr\",\"time\":\"00:01\"},{\"num\":3,\"question\":\"花和草\",\"answer\":\"植物\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":4,\"question\":\"自行车和飞机\",\"answer\":\"交通工具\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":5,\"question\":\"冰箱和洗衣机\",\"answer\":\"家用电器\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":6,\"question\":\"铅笔和橡皮\",\"answer\":\"文具\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":7,\"question\":\"酱油和盐\",\"answer\":\"调料\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":8,\"question\":\"项链和耳环\",\"answer\":\"首饰\\/饰品\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":9,\"question\":\"钢琴和小提琴\",\"answer\":\"乐器\",\"result\":null,\"audioPath\":null,\"time\":null},{\"num\":10,\"question\":\"佛教和基督教\",\"answer\":\"宗教\",\"result\":null,\"audioPath\":null,\"time\":null}]}}";

        createAppAudioDir();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        netService = NetServiceProvider.get(this);
        /**
         * 这里定义网络请求时的等待动画，可以是某种动画，调整，提示等，我这里简单用TextView模拟：正在加载中
         */
        netService.setListener(isPlaying -> {
            if (isPlaying == true){//加载动画
                loading.setVisibility(View.VISIBLE);
            }
            else {//关闭动画
                loading.setVisibility(View.GONE);
            }
        });

        captcha.setOnClickListener(this);
        register.setOnClickListener(this);
        login.setOnClickListener(this);
        upload.setOnClickListener(this);
        download.setOnClickListener(this);
        record.setOnClickListener(this);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.i1){
            /**
             * 获取验证码，是为了做安全验证，有以下三个验证码模板，分别是登录，注册和修改，其他不合法；
             * contact为绑定的联系方式，可以是手机号或者邮箱
             * @param code 0：登录验证码，1：注册验证码，2：修改密码验证码，其他不合法
             * @param contact 手机号或邮箱二选一
             */
            netService.getCaptcha("1","19173144580");

        } else if (view.getId() == R.id.i2) {

            /**
             * 注册账号
             * @param bind
             * @param captcha 这里的验证码会发送至手机或邮箱，需要输入
             * @param username
             * @param password
             * @param password_confirm
             */
            netService.register("19173144580",cEdit.getText().toString(),
                    "qing1","123456","123456");

        } else if (view.getId() == R.id.i3) {

            /**
             * 先定义登录回调函数
             */
            netService.setLoginCallback(new NetService.LoginCallback() {
                /**
                 * 如果登录成功，则会执行该回调，否则会在前后文this中弹出Toast提示错误
                 * @param uid 后端返回的用户标识uid
                 * @param username 后端返回的用户名称
                 */
                @Override
                public void onLoginResult(String uid, String username) {
                    Toast.makeText(ma.this,"uid:"+uid+" "+"username:"+username,Toast.LENGTH_SHORT).show();
                }
            });

            //密码登录
            netService.loginWithPassword("qing1","123456");
            //验证码登录
//            netService.loginWithCaptcha("19173144580",cEdit.getText().toString());
        } else if (view.getId() == R.id.i4) {
            /**
             * 先定义上传回调函数
             */
            netService.setUploadEvaluationCallback(new NetService.UploadEvaluationCallback() {
                /**
                 * 如果上传成功，则会执行该回调，否则会在前后文this中弹出Toast提示错误
                 * @param childUserID 后端返回的本次上传的编号，或者说这个儿童用户的测评id
                 */
                @Override
                public void onUploadEvaluationResult(String childUserID) {
                    childUser = childUserID;
                    Toast.makeText(ma.this,"childUserID:"+childUserID,Toast.LENGTH_SHORT).show();
                }
            });


            /**
             * 上传测评
             * @param uid 比如是100007
             * @param childUser
             */
            netService.uploadEvaluation("100007",jsonString);

        } else if (view.getId() == R.id.i5) {
            /**
             * 获取用户上传的测评
             * 这里根据需求获取，例如获取该用户账户下的所有测评，或者某个测评（通过childUserID），如果该用户有许多测评，那么一次获取全部测评，
             * 可以先获取到所有的childUserIDs，然后根据需求获取的具体的测评
             * 以下为更新内容
             * 注意，不要混淆
             * 查
             * getEvaluations(uid):会根据用户uid获取所有测评，它的回调函数为AllEvaluationCallback()
             * getEvaluation(uid, childUserID):会根据用户uid和childUserID获取某个测评，它的回调函数为EvaluationCallback()
             * getEvaluationID(uid):会根据用户uid获取所有测评childUserID，它的回调函数为EvaluationIDCallback()
             * 删
             * deleteEvaluations(uid):会根据用户uid删除所有测评，无回调函数，不可靠，若产生错误，将Toast弹出
             * deleteEvaluation(uid, childUserID):会根据用户uid和childUserID删除某个测评，无回调函数，不可靠，若产生错误，将Toast弹出
             * 改
             * uploadEvaluation(uid, childUserID):会根据用户uid和childUserID更新该测评，无回调函数，不可靠，若产生错误，将Toast弹出
             */
            netService.setEvaluationCallback(new NetService.EvaluationCallback() {
                /**
                 *
                 * @param evaluation
                 * "evaluation":
                 *     {
                 *       "ID": "ChildUser ID",
                 *       "Info": "ChildUser Info",
                 *       "Evaluations": "ChildUser Evaluations",
                 *       "UserID": "User ID",
                 *       "Time": "Timestamp"
                 *     }
                 *     ***返回字符串没有包含"evaluation":{},已经通过json转换取出
                 *     则字符串为
                 *     "ID": "ChildUser ID",
                 *     "Info": "ChildUser Info",
                 *     "Evaluations": "ChildUser Evaluations",
                 *     "UserID": "User ID",
                 *     "Time": "Timestamp"
                 */
                @Override
                public void onEvaluationResult(String evaluation) throws JSONException {
                    Toast.makeText(ma.this,"evaluation:"+evaluation,Toast.LENGTH_SHORT).show();
                }

            });
            netService.getEvaluation("100007",childUser);

//            netService.setEvaluationsCallback(new NetService.EvaluationsCallback() {
//                @Override
//                public void onEvaluationsResult(String evaluations) {
//                    Toast.makeText(MainActivity.this,"evaluation:"+evaluations,Toast.LENGTH_SHORT).show();
//                }
//            });
//            netService.getEvaluations("100007");

//            netService.deleteEvaluation("100007",childUser);
//            netService.deleteEvaluations("100007");
//            netService.updateEvaluation("100007",childUser,jsonString);

        } else if (view.getId() == R.id.i6) {
            //开始录音
            try {
                AudioRecorder.getInstance().startRecorder();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (view.getId() == R.id.i7) {

            //停止录音
            AudioRecorder.getInstance().stopRecorder();

            /**
             * 上传录音,需要与测评同时上传，需要有测评中每个题目录音的文件路径，依次上传，这里做一个示例
             * @param uid
             * @param childUserID
             * @param title 题目名称，比如A,E
             * @param num 第几题
             * @param audioPath 文件路径
             */
            if (childUser!=null)
                netService.uploadAudio("100007",childUser,"A","1",
                        AudioRecorder.getInstance().getOutputFilePath());

        } else if (view.getId() == R.id.i8) {
            /**
             * 先定义下载录音回调函数，需要与获取测评同步获取
             */
            netService.setAudioCallback(new NetService.AudioCallback() {

                @Override
                public void onAudioResult(String audio) {
                    byte[] decodeAudio = new byte[0];
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        decodeAudio = Base64.getDecoder().decode(audio);
                    }
                    if (player==null)
                        player = new MediaPlayer();


                    //写入文件目录下
                    File tempAudioFile = null;
                    try {
                        tempAudioFile = File.createTempFile("audio"+ System.currentTimeMillis(), ".amr", new File(dirpath.PATH_FETCH_DIR_AUDIO));
                        try (OutputStream os = new FileOutputStream(tempAudioFile)) {
                            os.write(decodeAudio);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (tempAudioFile != null && tempAudioFile.exists()) {
                        MediaPlayer player = new MediaPlayer();
                        try {
                            player.setDataSource(tempAudioFile.getPath());
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


            if(childUser!=null)
                netService.getAudio("100007",childUser,"A","1");
        }


    }


    private void createAppAudioDir() {
        File audioDir = sdcard.getInstance(this).createAppFetchDir(Ifileinter.FETCH_DIR_AUDIO);
        dirpath.PATH_FETCH_DIR_AUDIO = audioDir.getAbsolutePath();
    }
}
