package utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class sdcard {
    private Context context;
    private sdcard(Context context){
        this.context = context;
    }
    private static sdcard sdCard;
    //单例模式
    public static sdcard getInstance(Context context){
        if(sdCard == null){
            synchronized (sdcard.class){
                if(sdCard == null){
                    sdCard = new sdcard(context);
                }
            }
        }
        return sdCard;
    }

    public boolean isHaveSD(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public File createAppDir(){
        if(isHaveSD()){
            File appDir = context.getExternalFilesDir(Ifileinter.APP_DIR);
            if(!appDir.exists()){
                appDir.mkdir();
            }
            dirpath.PATH_APP_DIR = appDir.getAbsolutePath();
            return appDir;

        }
        return null;
    }

    public File createAppFetchDir(String dir){
        File publicDir = createAppDir();
        if(publicDir!=null){
            File fetchDir = new File(publicDir, dir);
            if(!fetchDir.exists()){
                fetchDir.mkdir();
            }
            return fetchDir;
        }
        return null;
    }
}
