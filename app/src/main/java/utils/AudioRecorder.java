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
    private volatile boolean isAlive;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
    private String outputFilePath;
    private Thread currentThread;
    private Thread timerThread;
    private volatile boolean isTimerAlive;

    private long time;//录音计时器
    private long timerTime;//独立计时器

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
        // 清除之前的监听器，防止多个监听器并行运行
        this.onRefreshUIThreadListener = null;
        // 设置新的监听器
        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
    }

    public boolean getAlive() {
        return isAlive;
    }

    /**
     * 只启动计时器，不启动录音
     */
    public void startTimer() {
        // 先停止之前的独立计时器线程
        stopTimer();
        
        // 重置独立计时器状态
        isTimerAlive = true;
        timerTime = 0;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        
        // 只启动独立计时器，不启动录音
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isTimerAlive) {
                    handler.sendEmptyMessage(1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        isTimerAlive = false;
                    }
                }
            }
        });
        timerThread.start();
    }

    /**
     * 开启子线程，只获取计时时间，反馈主线程
     */
    private Thread createTimerThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isTimerAlive) {
                    handler.sendEmptyMessage(1); // 使用不同的消息类型
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        isTimerAlive = false;
                    }
                }
            }
        });
        return thread;
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
        // 先停止之前的计时器线程
        if (currentThread != null && currentThread.isAlive()) {
            currentThread.interrupt(); // 中断之前的线程
            try {
                currentThread.join(500); // 等待线程完成，最多等待500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 确保线程已经终止
            if (currentThread.isAlive()) {
                currentThread = null;
            }
        }
        
        // 重置所有状态
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                // 捕获可能的异常
            }
            recorder.release();
            recorder = null;
        }
        isAlive = false;
        time = 0;
        // 不要重置onRefreshUIThreadListener，保留之前设置的监听器
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        
        // 初始化新的录音
        recorder = new MediaRecorder();
        isAlive = true;
        time = 0;
        //设置录音对象的参数
        setRecorder();
        try {
            recorder.prepare();
            recorder.start();
            currentThread = createThread();
            currentThread.start();
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
            // 处理消息类型1：只计时不录音
            if (message.what == 1) {
                if (!isTimerAlive) {
                    return false;
                }
                timerTime += 1000;
                if (onRefreshUIThreadListener != null) {
                    String times = calTime(timerTime);
                    onRefreshUIThreadListener.onRefresh(times);
                }
                return false;
            }
            // 处理消息类型0：录音并计时
            if (!isAlive) {
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

    private String calTime(long time) {
        // 直接计算分和秒，确保计时器按照正常时间流动
        long minutes = time / 60000;
        long seconds = (time % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 结束录音或停止计时器
     */
    public void stopRecorder() {
        // 无论recorder是否为null，都停止计时器和线程
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                // 捕获可能的异常
            }
            recorder.release(); // 释放资源
            recorder = null;
        }
        // 重置计时器
        time = 0;
        // 停止线程
        isAlive = false;
        if (currentThread != null) {
            currentThread.interrupt(); // 中断线程
            try {
                currentThread.join(500); // 等待线程完成，最多等待500ms
            } catch (InterruptedException e) {
                // 处理中断异常，如果被中断，主线程也可能需要处理这个中断
                Thread.currentThread().interrupt();
            }
            // 确保线程已经终止
            if (currentThread.isAlive()) {
                currentThread = null;
            }
        }
        // 清除handler中的所有消息，防止计时器继续运行
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        // 重置监听器，防止多个监听器并行运行
        onRefreshUIThreadListener = null;

    }

    /**
     * 停止独立计时器
     */
    public void stopTimer() {
        // 停止独立计时器线程
        isTimerAlive = false;
        if (timerThread != null) {
            timerThread.interrupt(); // 中断线程
            try {
                timerThread.join(500); // 等待线程完成，最多等待500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            timerThread = null;
        }
        // 重置独立计时器
        timerTime = 0;
        // 清除handler中的所有消息，防止计时器继续运行
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        // 不要重置监听器，保留之前设置的监听器
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
     */
    public void pauseRecorder() {
        if (isAlive) {
            // 不使用join()，避免阻塞主线程
            isAlive = false;//停止线程
            if (currentThread != null) {
                currentThread.interrupt();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    recorder.pause();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                stopRecorder();
            }
        }
    }

    /**
     * 恢复录音，适用于安卓7.0以上，否则重新录制
     */
    public void resumeRecorder() throws IOException {
        if (!isAlive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    recorder.resume();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                resetRecorder();
            }
            isAlive = true;//继续线程
            currentThread = createThread();
            currentThread.start();
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
        // 无论文件是否存在，都设置outputFilePath并配置recorder
        if (!file.exists()) {
            file.createNewFile();
        }
        outputFilePath = file.getAbsolutePath();
        recorder.setOutputFile(outputFilePath);
        recorder.setMaxDuration(10 * 60 * 1000);//不超过10分钟
    }


    public String getOutputFilePath() {
        return outputFilePath;
    }
}
