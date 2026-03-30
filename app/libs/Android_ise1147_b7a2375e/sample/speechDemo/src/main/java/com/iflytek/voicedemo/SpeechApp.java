package com.iflytek.voicedemo;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechUtility;

public class SpeechApp extends Application {

    public static final String PRIVACY_KEY = "privacy_key";
    private static boolean mscInitialize = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void initializeMsc(Context context){
        if (mscInitialize) return;
        // 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用“,”分隔。
        // 设置你申请的应用appid
        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        StringBuffer param = new StringBuffer();
        param.append("appid=" + context.getString(R.string.app_id));
        param.append(",");
        SpeechUtility.createUtility(context, param.toString());
        mscInitialize = true;
    }

}
