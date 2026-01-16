package utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import adapter.CustomViewPager;
import bean.evaluation;

public class testcontext {
    private testcontext() {};

    private Activity context;
    private CustomViewPager viewPager;



    private Boolean allowSwipe = true;//是否允许翻页

    private ArrayList<evaluation> evaluations;//测评题目
    private Integer count = 0;//完成题目数量
    private Integer lengths;//题目总数

    private static testcontext instance;
    public static testcontext getInstance(){
        if(instance == null){
            synchronized (testcontext.class){
                if(instance == null){
                    instance = new testcontext();
                }
            }
        }
        return instance;
    }
    public Activity getContext() {
        return context;
    }

    /**
     * 需要先设置上下文
     * @param context
     */
    public void setContext(Activity context) {
        this.context = context;
    }

    /**
     * 需要先设置viewpager
     * @return
     */
    public CustomViewPager getViewPager() {
        return viewPager;
    }

    public ArrayList<evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(ArrayList<evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public void setViewPager(CustomViewPager viewPager) {
        this.viewPager = viewPager;
    }
    public Boolean getAllowSwipe() {
        return allowSwipe;
    }


    public void setAllowSwipe(Boolean allowSwipe) {
        this.allowSwipe = allowSwipe;
    }

    public Integer getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }

    public Integer getLengths() {
        return lengths;
    }

    public void setLengths(Integer lengths) {
        this.lengths = lengths;
    }

    /**
     * 已答题目个数，获取第一个未完成的题目
     * @return
     */
//    public int searchOne(){
//        for(int i = 0; i< testcontext.getInstance().getLengths(); i++){
//            if(evaluations.get(i).getResult()!=null)
//                this.count++;
//        }
//        for(int i = 0; i< testcontext.getInstance().getLengths(); i++){
//            if(evaluations.get(i).getResult()==null)
//                return i;
//        }
//        return 0;
//    }
    public int searchOne(){
        for(int i = 0; i< testcontext.getInstance().getLengths(); i++){
            if(evaluations.get(i).getTime()!=null)
                this.count++;
        }
        for(int i = 0; i< testcontext.getInstance().getLengths(); i++){
            if(evaluations.get(i).getTime()==null)
                return i;
        }
        return 0;
    }










    /**
     * 释放单例
     */
    public void release(){
        instance = null;
    }


}
