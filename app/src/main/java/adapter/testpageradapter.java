package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

import bean.evaluation;
import utils.testcontext;

public class testpageradapter extends PagerAdapter {

    private List<Integer> ImageIdList;
    private String[] TabString;
    private Integer viewId;
    private ArrayList<evaluation> evaluations;
    private List<Integer []> ImageGroupIdList;
    private List<String []> StringGroupIdList;
    private String[] Hint;
    private TextView counter, timer;



    public testpageradapter(Integer viewId, List<Integer> ImageIdList, String[] TabString, ArrayList<evaluation> evaluations,
                            List<Integer []> ImageGroupIdList, List<String []> StringGroupIdList, String[] Hint,
                            TextView counter, TextView timer) {
        this.viewId = viewId;
        this.ImageIdList = ImageIdList;
        this.TabString = TabString;
        this.evaluations = evaluations;
        this.ImageGroupIdList = ImageGroupIdList;
        this.StringGroupIdList = StringGroupIdList;
        this.Hint = Hint;
        this.counter = counter;
        this.timer = timer;
    }

    @Override
    public int getCount() {
        return testcontext.getInstance().getLengths();
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(viewId, container, false);
        evaluations.get(position).test(view,position,ImageIdList,ImageGroupIdList,StringGroupIdList,Hint,TabString, counter, timer);
        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public Boolean getAllowSwipe() {
        return testcontext.getInstance().getAllowSwipe();
    }

}
