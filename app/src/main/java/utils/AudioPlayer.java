package utils;

import android.media.MediaPlayer;
import android.widget.TextView;

import com.example.CCLEvaluation.R;

import java.util.ArrayList;

public class AudioPlayer {
    private int playPos = -1;
    private MediaPlayer player;
    private ArrayList<TextView> audioIcons = new ArrayList<>();//纵列播放图标

    private AudioPlayer() {};
    private static AudioPlayer instance;
    public static AudioPlayer getInstance(){
        if(instance == null){
            synchronized (AudioPlayer.class){
                if(instance == null){
                    instance = new AudioPlayer();
                }
            }
        }
        return instance;
    }
    public void addIcon(TextView textView){
        audioIcons.add(textView);
    }
    public int getIconCount() {
        return audioIcons.size();
    }
    public void setPlayPos(int playPos) {
        this.playPos = playPos;
    }


    /**
     * 播放音频
     * @param Path
     * @param position
     * @return false:播放失败，有音频
     *
     */
    public Boolean play(String Path, int position) {
        // 确保位置在有效范围内
        if (position < 0 || position >= audioIcons.size()) {
            return false;
        }
        
        if (player == null) {
            player = new MediaPlayer();
            //播放完成监听
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (playPos >= 0 && playPos < audioIcons.size()) {
                        // 播放完成后恢复录音图标，并清除背景资源
                        audioIcons.get(playPos).setBackgroundResource(0); // 清除背景资源
                        audioIcons.get(playPos).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                    }
                }
            });
        }
        if(position==playPos){
            if(player.isPlaying()){
                player.stop();
                if (position >= 0 && position < audioIcons.size()) {
                    // 停止播放后恢复录音图标，并清除背景资源
                    audioIcons.get(position).setBackgroundResource(0); // 清除背景资源
                    audioIcons.get(position).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                }
                return false;
            }
            else {
                player.start();
                if (position >= 0 && position < audioIcons.size()) {
                    // 播放中显示停止图标（小×）
                    audioIcons.get(position).setBackgroundResource(R.drawable.playing);
                    audioIcons.get(position).setText("");
                }
                return true;
            }
        }
        else{
            if (player.isPlaying()) {
                player.stop();
                if (playPos >= 0 && playPos < audioIcons.size()) {
                    // 停止其他播放后恢复录音图标，并清除背景资源
                    audioIcons.get(playPos).setBackgroundResource(0); // 清除背景资源
                    audioIcons.get(playPos).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                }
            }
            try {
                player.reset();
                //设置播放音频资源
                player.setDataSource(Path);
                player.prepare();
                player.start();
                playPos = position;
                if (position >= 0 && position < audioIcons.size()) {
                    // 播放中显示停止图标（小×）
                    audioIcons.get(position).setBackgroundResource(R.drawable.playing);
                    audioIcons.get(position).setText("");
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    public void stop(){
        if(player!=null){
            player.release();
            player = null;
        }
    }
}
