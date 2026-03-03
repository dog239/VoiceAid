package bean;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapter.CustomViewPager;
import utils.testcontext;

/**
 * 社交能力评估（SOCIAL）
 */
public class social extends evaluation {
    private String ability;
    private String focus;
    private String content;
    private Integer score;
    private String observation;
    private String time;
    private String audioPath;

    private final String colum1 = "题号";
    private final String colum2 = "社交能力";
    private final String colum3 = "考查点";
    private final String colum4 = "题目内容";
    private final String colum5 = "得分";

    public social(int num, String ability, String focus, String content, Integer score, String observation, String time, String audioPath) {
        super(num);
        this.num = num;
        this.ability = ability;
        this.focus = focus;
        this.content = content;
        this.score = score;
        this.observation = observation;
        this.time = time;
        this.audioPath = audioPath;
    }

    private utils.allquestionlistener listener;
    public void setAllQuestionListener(utils.allquestionlistener listener){
        this.listener = listener;
    }

    public static social fromJson(JSONObject object) throws JSONException {
        int num = object.optInt("num", 0);
        String ability = object.optString("ability", "");
        String focus = object.optString("focus", "");
        String content = object.optString("content", "");
        Integer score = object.has("score") && !object.isNull("score") ? object.getInt("score") : null;
        String observation = object.has("observation") && !object.isNull("observation") ? object.getString("observation") : null;
        String time = object.has("time") && !object.isNull("time") ? object.getString("time") : null;
        String audioPath = object.has("audioPath") && !object.isNull("audioPath") ? object.getString("audioPath") : null;
        return new social(num, ability, focus, content, score, observation, time, audioPath);
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public Boolean getResult() {
        if (score == null) {
            return null;
        }
        return score >= 1;
    }

    @Override
    public int handle(View[] views, int position) {
        for(int i=0;i<5;i++)
            if (i < views.length)
                views[i].setVisibility(View.VISIBLE);
        if(position==0){
            if (views.length > 0) ((TextView)views[0]).setText(colum1);
            if (views.length > 1) ((TextView)views[1]).setText(colum2);
            if (views.length > 2) ((TextView)views[2]).setText(colum3);
            if (views.length > 3) ((TextView)views[3]).setText(colum4);
            if (views.length > 4) ((TextView)views[4]).setText(colum5);
        }
        else {
            if (views.length > 0) ((TextView)views[0]).setText(num <= 0 ? "" : String.valueOf(num));
            if (views.length > 1) ((TextView)views[1]).setText(ability);
            if (views.length > 2) ((TextView)views[2]).setText(focus);
            if (views.length > 3) ((TextView)views[3]).setText(content);
            if (views.length > 4) {
                int value = score == null ? 0 : score;
                ((TextView)views[4]).setText(String.valueOf(value));
            }
        }
        return 4;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList, List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList, String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        ImageView imageView = view.findViewById(R.id.imageView);
        Button startButton = view.findViewById(R.id.btn_start);
        Button score0Button = view.findViewById(R.id.btn_wrong);
        Button score1Button = view.findViewById(R.id.btn_mid);
        Button score2Button = view.findViewById(R.id.btn_right);
        TextView numberTextView = view.findViewById(R.id.tv_2);

        if (startButton != null) {
            startButton.setVisibility(View.GONE);
        }

        score0Button.setText("0=尚未出现");
        score1Button.setText("1=偶尔出现");
        score2Button.setText("2=稳定出现");
        // 设置按钮的点击监听器
        score0Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnswer(0, position, counter, timer);
            }
        });
        score1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnswer(1, position, counter, timer);
            }
        });
        score2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnswer(2, position, counter, timer);
            }
        });

        imageView.setVisibility(View.GONE);

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("第").append(TabString[position]).append("题：\n");
        contentBuilder.append(content).append("\n\n");
        contentBuilder.append("评分标准：\n");
        contentBuilder.append("0= 尚未出现 / 明显缺失（几乎从不这样做）\n");
        contentBuilder.append("1 = 偶尔出现 / 需要明显提醒或帮助（在大人提醒、引导、示范下才会）\n");
        contentBuilder.append("2 = 稳定出现 / 自然情境中会主动使用（不需要提醒，经常自发出现）");
        
        numberTextView.setText(contentBuilder.toString());
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        // 无论是否有时间记录，都启用按钮
        score0Button.setEnabled(true);
        score1Button.setEnabled(true);
        score2Button.setEnabled(true);

        // 设置按钮的点击监听器
        score0Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnswer(0, position, counter, timer);
            }
        });
        score1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnswer(1, position, counter, timer);
            }
        });
        score2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnswer(2, position, counter, timer);
            }
        });
    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num", num);
        object.put("ability", ability);
        object.put("focus", focus);
        object.put("content", content);
        if (score != null) {
            object.put("score", score);
            object.put("observation", observation == null ? JSONObject.NULL : observation);
            object.put("audioPath", audioPath == null ? JSONObject.NULL : audioPath);
            object.put("time", time);
        } else {
            object.put("score", JSONObject.NULL);
            object.put("observation", JSONObject.NULL);
            object.put("audioPath", JSONObject.NULL);
            object.put("time", JSONObject.NULL);
        }
        // 确保SOCIAL数组存在
        JSONArray socialArray = evaluations.optJSONArray("SOCIAL");
        if (socialArray == null) {
            socialArray = new JSONArray();
            evaluations.put("SOCIAL", socialArray);
        }
        // 根据题目编号更新或添加到相应位置
        int index = num - 1; // 题目编号从1开始，数组索引从0开始
        if (index >= 0) {
            // 如果索引超出数组长度，先填充null值
            while (socialArray.length() <= index) {
                socialArray.put(JSONObject.NULL);
            }
            // 更新对应位置的条目
            socialArray.put(index, object);
        } else {
            // 如果索引无效，添加到数组末尾
            socialArray.put(object);
        }
    }

    private void handleAnswer(int value, int position, TextView counter, TextView timer) {
        if (time == null) {
            testcontext.getInstance().incrementCount();
            Integer count = testcontext.getInstance().getCount();
            Integer lengths = testcontext.getInstance().getLengths();
            int countValue = count != null ? count : 0;
            int lengthsValue = lengths != null ? lengths : 0;
            counter.setText(countValue+"/"+ lengthsValue);
        }
        score = value;
        time = new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date());
        timer.setText(time);
        testcontext.getInstance().setAllowSwipe(true);
        Integer count = testcontext.getInstance().getCount();
        Integer lengths = testcontext.getInstance().getLengths();
        int countValue = count != null ? count : 0;
        int lengthsValue = lengths != null ? lengths : 0;
        nextPage(position, countValue, lengthsValue);
    }

    private void nextPage(int position,int count, int lengths) {
        int nP = position + 1;
        if (count >= lengths) {
            Activity context = testcontext.getInstance().getContext();
            if (context != null) {
                Toast.makeText(context, "已完成测评题目！", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onAllQuestionComplete();
                }
                context.finish();
            }
            return;
        }
        Activity context = testcontext.getInstance().getContext();
        if (context != null) {
            if(nP >= lengths){
                CustomViewPager viewPager = testcontext.getInstance().getViewPager();
                if (viewPager != null) {
                    viewPager.setCurrentItem(testcontext.getInstance().searchOne(), true);
                }
            }
            else {
                CustomViewPager viewPager = testcontext.getInstance().getViewPager();
                if (viewPager != null) {
                    viewPager.setCurrentItem(nP, true);
                }
            }
        }
    }
}
