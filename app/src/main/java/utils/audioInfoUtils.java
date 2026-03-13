package utils;

import android.media.MediaMetadataRetriever;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class audioInfoUtils {
    private MediaMetadataRetriever mediaMetadataRetriever;
    private audioInfoUtils(){}
    private static audioInfoUtils aInfo;
    public static audioInfoUtils getInstance(){
        if(aInfo == null){
            synchronized (audioInfoUtils.class){
                if(aInfo == null){
                    aInfo = new audioInfoUtils();
                }
            }
        }
        return aInfo;
    }
    public long getAFDuration(String filePath){
        long duration = 0;
        if(mediaMetadataRetriever == null){
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);;
        String t = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.parseLong(t);
        return duration;

    }
    public String getAudioFormatDuration(String format, long durLong){
        durLong-=8*3600*1000;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(durLong));
    }
    public String getAudioFormatDuration(long durLong){
        return getAudioFormatDuration("HH:mm:ss",durLong);
    }
    public String getAFArtist(String filePath){
        long duration = 0;
        if(mediaMetadataRetriever == null){
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);;
        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        return artist;
    }
    public void releaseRetriever() throws IOException {
        if(mediaMetadataRetriever!=null){
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }

    }

}
