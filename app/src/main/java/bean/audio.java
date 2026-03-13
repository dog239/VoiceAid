package bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import utils.audioInfoUtils;

public class audio {
    private String id;
    private String title;
    private String time;
    private String duration;
    private String path;
    private long durationLong;
    private long lastModified;
    private String fileSuffix;
    private long fileLength;

    private int currentProgress = 0;
    private boolean isPlaying = false;
    // 创建临时文件
    private File tempFile;
    public audio(String path){
        this.path = path;
    }


    public audio(String id, String title, String time, String duration, String path, long durationLong, long lastModified, String fileSuffix, long fileLength) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.duration = duration;
        this.path = path;
        this.durationLong = durationLong;
        this.lastModified = lastModified;
        this.fileSuffix = fileSuffix;
        this.fileLength = fileLength;
    }
    /*
    从网络获取并实例化，需要创建临时文件，写入存储
     */
    public audio(String id, String title, String time, String duration, byte[] audio) {
        audioInfoUtils audioInfoUtils = utils.audioInfoUtils.getInstance();
        this.id = id;
        this.title = title;
        this.time = time;
        this.duration = duration;
        try {
            this.tempFile = File.createTempFile("tempAudio", ".amr");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(audio);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.path = this.tempFile.getPath();
        this.durationLong = audioInfoUtils.getAFDuration(this.path);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDurationLong(long durationLong) {
        this.durationLong = durationLong;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public long getDurationLong() {
        return durationLong;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public long getFileLength() {
        return fileLength;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public File getTempFile() {
        return tempFile;
    }

    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }
}
