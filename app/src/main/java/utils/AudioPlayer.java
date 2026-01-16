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
        if (player == null) {
            player = new MediaPlayer();
            //播放完成监听
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audioIcons.get(playPos).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                }
            });
        }
        if(position==playPos){
            if(player.isPlaying()){
                player.stop();
                audioIcons.get(position).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                return false;
            }
            else {
                player.start();
                audioIcons.get(position).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.playing));
                return true;
            }
        }
        else{
            if (player.isPlaying()) {
                player.stop();
                audioIcons.get(playPos).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
            }
            try {
                player.reset();
                //设置播放音频资源
                player.setDataSource(Path);
                player.prepare();
                player.start();
                playPos = position;
                audioIcons.get(position).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.playing));

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
