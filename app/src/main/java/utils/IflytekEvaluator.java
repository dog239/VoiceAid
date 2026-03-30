package utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechUtility;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 封装讯飞语音评测：输出总分并回调。
 */
public final class IflytekEvaluator {
    private static final String TAG = "IflytekEvaluator";
    private static boolean initialized = false;

    private IflytekEvaluator() {
    }

    public interface ScoreCallback {
        void onScore(Float totalScore, String rawResult, String error);
    }

    public static synchronized void init(Context context, String appId) {
        if (initialized) return;
        if (context == null || TextUtils.isEmpty(appId)) {
            Log.w(TAG, "init skipped: context/appId missing");
            return;
        }
        String params = "appid=" + appId;
        SpeechUtility.createUtility(context.getApplicationContext(), params);
        initialized = true;
        Log.d(TAG, "init ok: appIdLen=" + appId.length());
    }

    public static void evaluateFromAudio(Context context,
                                         String referenceText,
                                         String audioPath,
                                         ScoreCallback callback) {
        Log.d(TAG, "evaluateFromAudio: refLen=" + (referenceText == null ? 0 : referenceText.length())
                + ", audioPath=" + audioPath);
        Log.d(TAG, "evaluateFromAudio: initialized=" + initialized);
        if (context == null) {
            Log.w(TAG, "return: context is null");
            if (callback != null) callback.onScore(null, null, "context is null");
            return;
        }
        if (TextUtils.isEmpty(referenceText)) {
            Log.w(TAG, "return: referenceText is empty");
            if (callback != null) callback.onScore(null, null, "referenceText is empty");
            return;
        }
        if (TextUtils.isEmpty(audioPath) || !new File(audioPath).exists()) {
            Log.w(TAG, "return: audioPath missing");
            if (callback != null) callback.onScore(null, null, "audioPath missing");
            return;
        }

        SpeechEvaluator evaluator = SpeechEvaluator.createEvaluator(context.getApplicationContext(), null);
        if (evaluator == null) {
            Log.w(TAG, "return: SpeechEvaluator is null");
            if (callback != null) callback.onScore(null, null, "SpeechEvaluator is null");
            return;
        }

        configureEvaluator(evaluator);
        evaluator.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        int ret = evaluator.startEvaluating(referenceText, null, new EvaluatorListener() {
            private String lastResult;

            @Override
            public void onResult(EvaluatorResult result, boolean isLast) {
                if (result == null) return;
                lastResult = result.getResultString();
                Log.d(TAG, "onResult: isLast=" + isLast + ", resultLen=" + (lastResult == null ? 0 : lastResult.length()));
                if (!isLast) return;
                Float score = parseTotalScore(lastResult);
                Log.d(TAG, "onResult: totalScore=" + score);
                if (callback != null) callback.onScore(score, lastResult, null);
                evaluator.destroy();
                Log.d(TAG, "evaluate complete");
            }

            @Override
            public void onError(SpeechError error) {
                if (callback != null) {
                    String msg = error == null ? "unknown" : (error.getErrorCode() + ":" + error.getErrorDescription());
                    Log.w(TAG, "onError: " + msg);
                    callback.onScore(null, lastResult, msg);
                }
                evaluator.destroy();
            }

            @Override
            public void onBeginOfSpeech() {
                Log.d(TAG, "onBeginOfSpeech");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech");
            }

            @Override
            public void onVolumeChanged(int volume, byte[] data) {
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, android.os.Bundle obj) {
            }
        });

        Log.d(TAG, "startEvaluating ret=" + ret);
        if (ret != 0) {
            evaluator.destroy();
            if (callback != null) callback.onScore(null, null, "startEvaluating failed: " + ret);
            return;
        }

        Log.d(TAG, "convert audio begin");
        byte[] audioBytes = AudioCodecConverter.decodeAmrToWavBytes(audioPath);
        if (audioBytes == null || audioBytes.length == 0) {
            Log.w(TAG, "audio convert failed or empty");
            evaluator.destroy();
            if (callback != null) callback.onScore(null, null, "audio bytes empty");
            return;
        }
        Log.d(TAG, "audioBytes=" + audioBytes.length);
        evaluator.writeAudio(audioBytes, 0, audioBytes.length);
        evaluator.stopEvaluating();
    }

    private static void configureEvaluator(SpeechEvaluator evaluator) {
        Log.d(TAG, "configureEvaluator");
        evaluator.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        evaluator.setParameter(SpeechConstant.ISE_CATEGORY, "read_word");
        evaluator.setParameter(SpeechConstant.RESULT_LEVEL, "complete");
        evaluator.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        evaluator.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        evaluator.setParameter("ise_unite", "1");
        evaluator.setParameter("rst", "entirety");
        evaluator.setParameter("extra_ability", "syll_phone_err_msg;pitch;multi_dimension");
    }

    private static Float parseTotalScore(String xml) {
        if (TextUtils.isEmpty(xml)) return null;
        Float score = extractScore(xml, "total_score\\\"\\s*value\\\"\\s*=\\\"([0-9.]+)\\\"");
        if (score != null) return score;
        score = extractScore(xml, "total_score\\\"\\s*=\\\"([0-9.]+)\\\"");
        if (score != null) return score;
        score = extractScore(xml, "total_score=\\\"([0-9.]+)\\\"");
        return score;
    }

    private static Float extractScore(String xml, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(xml);
        if (!matcher.find()) return null;
        try {
            return Float.parseFloat(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
