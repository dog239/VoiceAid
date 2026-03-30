package utils;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 将录音文件转为 PCM16(16k) 并输出内存字节。
 * 仅用于构音扭曲判别的语音评测写入。
 */
public final class AudioCodecConverter {
    private static final String TAG = "AudioCodecConverter";

    private AudioCodecConverter() {
    }

    public static byte[] readAllBytes(String path) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "read bytes failed", e);
            return null;
        }
    }
}

