package utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 将 AMR-WB 录音转为 WAV(PCM16/16k) 并输出字节。
 */
public final class AudioCodecConverter {
    private static final String TAG = "AudioCodecConverter";
    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int TARGET_CHANNELS = 1;

    private AudioCodecConverter() {
    }

    public static byte[] decodeAmrToWavBytes(String path) {
        if (path == null || path.isEmpty()) return null;
        File input = new File(path);
        if (!input.exists()) return null;

        Log.d(TAG, "decodeAmrToWavBytes: input=" + path + ", size=" + input.length());
        File tempWav = null;
        try {
            tempWav = File.createTempFile("ise_", ".wav");
            boolean ok = decodeToWavFile(path, tempWav.getAbsolutePath());
            if (!ok) return null;
            Log.d(TAG, "decodeAmrToWavBytes: wavSize=" + tempWav.length());
            return readAllBytes(tempWav);
        } catch (IOException e) {
            Log.e(TAG, "transcode failed", e);
            return null;
        } finally {
            if (tempWav != null && tempWav.exists()) {
                //noinspection ResultOfMethodCallIgnored
                tempWav.delete();
            }
        }
    }

    private static boolean decodeToWavFile(String inputPath, String outputWavPath) {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;
        FileOutputStream wavOut = null;
        try {
            extractor.setDataSource(inputPath);
            int trackIndex = selectAudioTrack(extractor);
            if (trackIndex < 0) return false;
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime == null) return false;
            Log.d(TAG, "decodeToWavFile: mime=" + mime + ", format=" + format);

            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, null, null, 0);
            decoder.start();

            wavOut = new FileOutputStream(outputWavPath);
            // Reserve WAV header; fill later.
            wavOut.write(new byte[44]);

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            boolean outputDone = false;
            int totalPcmBytes = 0;

            while (!outputDone) {
                if (!inputDone) {
                    int inIndex = decoder.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, presentationTimeUs, 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                if (outIndex >= 0) {
                    ByteBuffer outBuffer = outputBuffers[outIndex];
                    byte[] chunk = new byte[info.size];
                    outBuffer.get(chunk);
                    outBuffer.clear();
                    if (info.size > 0) {
                        wavOut.write(chunk);
                        totalPcmBytes += info.size;
                    }
                    decoder.releaseOutputBuffer(outIndex, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                }
            }

            wavOut.flush();
            writeWavHeader(outputWavPath, totalPcmBytes, TARGET_SAMPLE_RATE, TARGET_CHANNELS);
            Log.d(TAG, "decodeToWavFile: pcmBytes=" + totalPcmBytes);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "decode to wav failed", e);
            return false;
        } finally {
            extractor.release();
            if (decoder != null) {
                try {
                    decoder.stop();
                } catch (Exception ignored) {
                }
                decoder.release();
            }
            if (wavOut != null) {
                try {
                    wavOut.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static int selectAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    private static void writeWavHeader(String wavPath, int pcmDataSize, int sampleRate, int channels) throws IOException {
        int byteRate = sampleRate * channels * 2;
        int totalDataLen = pcmDataSize + 36;
        byte[] header = new byte[44];
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        writeInt(header, 4, totalDataLen);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        writeInt(header, 16, 16);
        writeShort(header, 20, (short) 1);
        writeShort(header, 22, (short) channels);
        writeInt(header, 24, sampleRate);
        writeInt(header, 28, byteRate);
        writeShort(header, 32, (short) (channels * 2));
        writeShort(header, 34, (short) 16);
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        writeInt(header, 40, pcmDataSize);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(wavPath);
            out.write(header, 0, 44);
        } finally {
            if (out != null) out.close();
        }
    }

    private static void writeInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xff);
        data[offset + 1] = (byte) ((value >> 8) & 0xff);
        data[offset + 2] = (byte) ((value >> 16) & 0xff);
        data[offset + 3] = (byte) ((value >> 24) & 0xff);
    }

    private static void writeShort(byte[] data, int offset, short value) {
        data[offset] = (byte) (value & 0xff);
        data[offset + 1] = (byte) ((value >> 8) & 0xff);
    }

    private static byte[] readAllBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }
}
