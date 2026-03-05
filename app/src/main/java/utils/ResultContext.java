package utils;

import android.content.Context;

public class ResultContext {

    public int R_id1,R_id2,R_id3,R_id4;

    private ResultContext() {};
    private Context context;
    private static ResultContext instance;
    public static ResultContext getInstance(){
        if(instance == null){
            synchronized (ResultContext.class){
                if(instance == null){
                    instance = new ResultContext();
                }
            }
        }
        return instance;
    }

    /**
     * 需要先设置上下文
     * @param context
     */
    public void setContext(Context context){
        this.context = context;
        R_id1 = context.getResources().getIdentifier("right", "drawable", context.getPackageName());
        R_id2 = context.getResources().getIdentifier("wrong", "drawable", context.getPackageName());
        R_id3 = context.getResources().getIdentifier("audio", "drawable", context.getPackageName());
        R_id4 = context.getResources().getIdentifier("playing", "drawable", context.getPackageName());
    }
    public Context getContext(){
        return context;
    }
}
