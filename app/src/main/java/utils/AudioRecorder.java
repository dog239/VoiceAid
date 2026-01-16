package utils;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioRecorder {
    private MediaRecorder recorder;
    private Boolean isAlive;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
    private SimpleDateFormat calSdf = new SimpleDateFormat("mm:ss");
    private String outputFilePath;
    private Thread thread;

    private long startTime;


    private int time;//录音计时器

    //单例模式
    private AudioRecorder() {}
    private static AudioRecorder instance;

    public static AudioRecorder getInstance() {
        if (instance == null) {
            synchronized (AudioRecorder.class) {
                if (instance == null) {
                    instance = new AudioRecorder();
                }
            }
        }
        return instance;
    }

    /**
     * 设置Activity的UI界面的回调接口
     */
    public interface OnRefreshUIThreadListener {
        void onRefresh(String time);
    }

    private OnRefreshUIThreadListener onRefreshUIThreadListener;

    public void setOnRefreshUIThreadListener(OnRefreshUIThreadListener onRefreshUIThreadListener) {
        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
    }

    public Boolean getAlive() {
        return isAlive;
    }

    /**
     * 开启录音
     */
//    public void startRecorder() throws IOException {
//        if (recorder == null) {
//            recorder = new MediaRecorder();
//        }
//        isAlive = true;
//        //设置录音对象的参数
//        setRecorder();
//        try {
//            recorder.prepare();
//            recorder.start();
//            startTime = System.currentTimeMillis(); // 记录开始时间
//            if (thread != null && thread.isAlive()) {
//                thread.interrupt(); // 确保没有旧线程在运行
//            }
//            thread = createThread();
//            thread.start();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 开启子线程，获取当前录制时间，反馈主线程
//     */
//    private Thread createThread() {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (isAlive) {
//                    handler.sendEmptyMessage(0);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        // 正确处理中断异常，退出循环
//                        isAlive = false;
//                        break;
//
//                    }
//                }
//            }
//        });
//        return thread;
//    }
//
//    private Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(@NonNull Message message) {
//            if (recorder == null) {
//                return false;
//            }
//            long currentTime = System.currentTimeMillis(); // 获取当前时间
//            long elapsedTime = currentTime - startTime; // 计算已过去的时间
//            if (onRefreshUIThreadListener != null) {
//                String formattedTime = calTime(elapsedTime);
//                onRefreshUIThreadListener.onRefresh(formattedTime);
//            }
//            return true;
//        }
//    });
//
//    private String calTime(long time) {
//        return new SimpleDateFormat("mm:ss").format(new Date(time));
//    }

    public void startRecorder() throws IOException {
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        isAlive = true;
        //设置录音对象的参数
        setRecorder();
        try {
            recorder.prepare();
            recorder.start();
//            if (thread != null && thread.isAlive()) {
//                thread.interrupt(); // 确保没有旧线程在运行
//            }
            thread = createThread();
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 开启子线程，获取当前录制时间，反馈主线程
     */
    private Thread createThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isAlive) {
                    handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        isAlive = false;
                    }
                }
            }
        });
        return thread;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (recorder == null) {
                return false;
            }
            time += 1000;
            if (onRefreshUIThreadListener != null) {
                String times = calTime(time);
                onRefreshUIThreadListener.onRefresh(times);
            }
            return false;
        }
    });

    private String calTime(int time) {
        time -= 8 * 3600 * 1000;
        String format = calSdf.format(new Date(time));
        return format;
    }

    /**
     * 结束录音
     */
    public void stopRecorder() {
        if (recorder != null) {
            recorder.stop();
            recorder.release(); // 释放资源
            recorder = null;
            time = 0;
            isAlive = false;//停止线程
            if (thread != null) {
                thread.interrupt(); // 中断线程
                try {
                    thread.join(); // 等待线程完成
                } catch (InterruptedException e) {
                    // 处理中断异常，如果被中断，主线程也可能需要处理这个中断
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

    /**
     * 特殊中断录音
     */
    public void interruptRecorder(){
        stopRecorder();
        File file = new File(outputFilePath);
        file.delete();
    }

    /**
     * 暂停录音，适用于安卓7.0以上，否则结束录音
     * thread.join() 将阻塞当前线程，直到目标线程 thread 终止
     */
    public void pauseRecorder() throws InterruptedException {
        if (isAlive) {
            thread.join();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.pause();
            } else {
                stopRecorder();
            }
            isAlive = false;//停止线程

        }
    }

    /**
     * 恢复录音，适用于安卓7.0以上，否则重新录制
     */
    public void resumeRecorder() throws IOException {
        if (!isAlive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.resume();
            } else {
                resetRecorder();
            }
            isAlive = true;//继续线程
            thread = createThread();
            thread.start();
        }
    }

    /**
     * 重新录制
     */
    public void resetRecorder() throws IOException {
        stopRecorder();
        // 删除录制的输出文件
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        if (onRefreshUIThreadListener != null) {
           // String times = calTime(time);
            //onRefreshUIThreadListener.onRefresh(times);
        }
        startRecorder();
    }


    /**
     * 设置录音对象的参数
     *
     * @throws IOException
     */
    private void setRecorder() throws IOException {
        recorder.reset();
        //获取麦克风的声音
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置输出格式
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        //设置编码格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        //设置输出文件
        String time = sdf.format(new Date());
        File file = new File(dirpath.PATH_FETCH_DIR_AUDIO, time + ".amr");
        if (!file.exists()) {
            file.createNewFile();
            outputFilePath = file.getAbsolutePath();
            recorder.setOutputFile(outputFilePath);
            recorder.setMaxDuration(10 * 60 * 1000);//不超过10分钟
        }
    }


    public String getOutputFilePath() {
        return outputFilePath;
    }
}
