package bean;

import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class evaluation {
    protected int num;
    public final static String E  = "词汇表达";
    public final static String RE  = "词汇理解";
    public final static String S  = "词义";
    public final static String NWR  = "非词复述";
    public final static String A  = "构音";
    public final static String RG  = "语法理解";
    public final static String PST  = "看图说故事";
    public final static String PN  = "个人生活经验";
    public final static String PL  = "前语言能力";

    public evaluation(int num) {
        this.num = num;
    }

    /**
     * 策略模式，定义抽象方法，返回该item的textview数量
     *
     * @param views  该item的各视图
     * @param position  第几个item
     * @return
     */
    public abstract int handle(View[] views, int position);

    /**
     * 处理viewpager中每个item的不同对象事件
     * @param view
     * @param position
     * @param ImageIdList
     * @param TabString
     */
    public abstract void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                              List<String []> StringGroupIdList,String[] Hint,
                              String[] TabString, TextView counter, TextView timer);

    /**
     * 转为Json格式
     * @return
     */
    public abstract void toJson(JSONObject evaluations) throws JSONException;

    /**
     * 获取某测评题目的答题时间
     * @return
     */
    public abstract String getTime();

    /**
     * 获取某测评题目的答题状态,可用来判断用户是否答题
     * @return
     */
    public abstract Boolean getResult();
}
