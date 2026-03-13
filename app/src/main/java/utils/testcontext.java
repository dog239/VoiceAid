package utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.HashMap;

import adapter.CustomViewPager;
import bean.evaluation;

public class testcontext {
    private testcontext() {};

    private Activity context;
    private CustomViewPager viewPager;



    private Boolean allowSwipe = true;//是否允许翻页

    private ArrayList<evaluation> evaluations;//测评题目
    private HashMap<String, Integer> groupCounts = new HashMap<>();//每个场景+组的完成题目数量
    private Integer currentCount = 0;//当前组的完成题目数量
    private Integer lengths = 0;//题目总数
    private Integer groupNumber = 1;//当前评估分组编号
    private String scene = "A"; // 当前选择的场景

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
        // 重置状态，确保每次测评都从新开始
        this.currentCount = 0;
        this.lengths = 0;
        this.groupNumber = 1;
        this.groupCounts.clear();
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
        return currentCount;
    }

    public void incrementCount() {
        this.currentCount++;
        // 保存到分组计数中，使用场景+组号作为键
        if (groupNumber != null) {
            String key = scene + "_" + groupNumber;
            groupCounts.put(key, currentCount);
        }
    }

    public Integer getLengths() {
        return lengths;
    }

    public void setLengths(Integer lengths) {
        this.lengths = lengths;
    }

    public Integer getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(Integer groupNumber) {
        // 保存当前组的计数，使用场景+组号作为键
        if (this.groupNumber != null) {
            String key = scene + "_" + this.groupNumber;
            groupCounts.put(key, currentCount);
        }
        // 设置新的组号
        this.groupNumber = groupNumber;
        // 恢复新组的计数，如果没有则设置为0，使用场景+组号作为键
        String newKey = scene + "_" + groupNumber;
        if (groupCounts.containsKey(newKey)) {
            currentCount = groupCounts.get(newKey);
        } else {
            currentCount = 0;
        }
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
    public int searchOne(){        // 使用实际的题目数量
        int groupLength = this.lengths;
        if (evaluations == null || evaluations.size() == 0) {
            return 0;
        }
        this.currentCount = 0;
        for(int i = 0; i < groupLength && i < evaluations.size(); i++){
            if(evaluations.get(i) != null && evaluations.get(i).getTime()!=null)
                this.currentCount++;
        }
        // 保存到分组计数中，使用场景+组号作为键
        if (groupNumber != null) {
            String key = scene + "_" + groupNumber;
            groupCounts.put(key, currentCount);
        }
        for(int i = 0; i < groupLength && i < evaluations.size(); i++){
            if(evaluations.get(i) != null && evaluations.get(i).getTime()==null)
                return i;
        }
        // 所有题目都完成时，返回最后一题的索引
        int lastIndex = Math.min(groupLength - 1, evaluations.size() - 1);
        return Math.max(0, lastIndex);
    }
    
    /**
     * 重置测评状态，用于切换分组时
     */
    public void resetEvaluationState() {
        this.currentCount = 0;
        this.evaluations = null;
        this.lengths = 0;
        this.groupNumber = 1;
        this.groupCounts.clear();
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }










    /**
     * 释放单例
     */
    public void release(){
        instance = null;
    }


}
