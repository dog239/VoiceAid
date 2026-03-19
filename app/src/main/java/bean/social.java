package bean;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;

import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.ma;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        for(int i=0;i<7;i++)
            if (i < views.length)
                views[i].setVisibility(View.VISIBLE);
        if(num == 0){
            if (views.length > 0) ((TextView)views[0]).setText("题目序号");
            if (views.length > 1) ((TextView)views[1]).setText("社交能力");
            if (views.length > 2) ((TextView)views[2]).setText("考查点");
            if (views.length > 3) ((TextView)views[3]).setText("题目内容");
            if (views.length > 4) ((TextView)views[4]).setText("0 = 尚未出现");
            if (views.length > 5) ((TextView)views[5]).setText("1 = 偶尔出现");
            if (views.length > 6) ((TextView)views[6]).setText("2 = 稳定出现");
        } else if(num == -2) {
            // 第二行表头
            if (views.length > 0) ((TextView)views[0]).setText("");
            if (views.length > 1) ((TextView)views[1]).setText("");
            if (views.length > 2) ((TextView)views[2]).setText("");
            if (views.length > 3) ((TextView)views[3]).setText("");
            if (views.length > 4) ((TextView)views[4]).setText("");
            if (views.length > 5) ((TextView)views[5]).setText("");
            if (views.length > 6) ((TextView)views[6]).setText("");
        } else if(num == -3) {
            // 组标题
            if (views.length > 0) ((TextView)views[0]).setText(ability == null ? "" : ability);
            if (views.length > 1) ((TextView)views[1]).setText("");
            if (views.length > 2) ((TextView)views[2]).setText("");
            if (views.length > 3) ((TextView)views[3]).setText("");
            if (views.length > 4) ((TextView)views[4]).setText("");
            if (views.length > 5) ((TextView)views[5]).setText("");
            if (views.length > 6) ((TextView)views[6]).setText("");
        } else {
            if (views.length > 0) ((TextView)views[0]).setText(num <= 0 ? "" : String.valueOf(num));
            if (views.length > 1) ((TextView)views[1]).setText(ability == null ? "" : ability);
            if (views.length > 2) ((TextView)views[2]).setText(focus == null ? "" : focus);
            if (views.length > 3) ((TextView)views[3]).setText(content == null ? "" : content);
            if (views.length > 4) ((TextView)views[4]).setText(score != null && score == 0 ? "✓" : "");
            if (views.length > 5) ((TextView)views[5]).setText(score != null && score == 1 ? "✓" : "");
            if (views.length > 6) ((TextView)views[6]).setText(score != null && score == 2 ? "✓" : "");
        }
        return 6;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList, List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList, String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        try {
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

            if (imageView != null) {
                imageView.setVisibility(View.GONE);
            }

            StringBuilder contentBuilder = new StringBuilder();
            String questionNumber = String.valueOf(position + 1);
            if (TabString != null && position >= 0 && position < TabString.length) {
                questionNumber = TabString[position];
            }
            contentBuilder.append("第").append(questionNumber).append("题：\n");
            contentBuilder.append(content).append("\n\n");
            contentBuilder.append("评分标准：\n");
            contentBuilder.append("0= 尚未出现 / 明显缺失（几乎从不这样做）\n");
            contentBuilder.append("1 = 偶尔出现 / 需要明显提醒或帮助（在大人提醒、引导、示范下才会）\n");
            contentBuilder.append("2 = 稳定出现 / 自然情境中会主动使用（不需要提醒，经常自发出现）");
            
            if (numberTextView != null) {
                numberTextView.setText(contentBuilder.toString());
                numberTextView.setTextSize(20); // 增大字体大小
                // 增大布局尺寸
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(10, 10, 10, 10);
                numberTextView.setLayoutParams(params);
                numberTextView.setPadding(20, 30, 20, 30);
            }
            
            // 固定使用10作为每组题目数量
            int groupLength = 10;

            // 无论是否有时间记录，都启用按钮
            if (score0Button != null) score0Button.setEnabled(true);
            if (score1Button != null) score1Button.setEnabled(true);
            if (score2Button != null) score2Button.setEnabled(true);

            // 设置按钮的点击监听器
            if (score0Button != null) {
                score0Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleAnswer(0, position, counter, timer);
                    }
                });
            }
            if (score1Button != null) {
                score1Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleAnswer(1, position, counter, timer);
                    }
                });
            }
            if (score2Button != null) {
                score2Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleAnswer(2, position, counter, timer);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 不显示错误信息，避免用户困惑
        }
    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        try {
            if (evaluations == null) {
                return;
            }
            
            JSONObject object = new JSONObject();
            try {
                object.put("num", num);
                object.put("ability", ability == null ? "" : ability);
                object.put("focus", focus == null ? "" : focus);
                object.put("content", content == null ? "" : content);
                if (score != null) {
                    object.put("score", score);
                    object.put("observation", observation == null ? JSONObject.NULL : observation);
                    object.put("audioPath", audioPath == null ? JSONObject.NULL : audioPath);
                    object.put("time", time == null ? JSONObject.NULL : time);
                } else {
                    object.put("score", JSONObject.NULL);
                    object.put("observation", JSONObject.NULL);
                    object.put("audioPath", JSONObject.NULL);
                    object.put("time", JSONObject.NULL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            
            // 获取当前组号，默认为1
            int groupNumber = 1;
            try {
                Integer currentGroup = testcontext.getInstance().getGroupNumber();
                if (currentGroup != null) {
                    groupNumber = currentGroup;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 为每个组使用单独的JSONArray存储数据
            String socialKey = "SOCIAL" + groupNumber;
            JSONArray socialArray = evaluations.optJSONArray(socialKey);
            if (socialArray == null) {
                socialArray = new JSONArray();
                try {
                    evaluations.put(socialKey, socialArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }
            
            // 计算当前组内的题目索引（每组10题）
            int startIndex = (groupNumber - 1) * 10;
            int index = num - startIndex - 1; // 组内题目索引从0开始
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
                // 如果索引无效，添加到数组末尾
                socialArray.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAnswer(int value, int position, TextView counter, TextView timer) {
        // 固定使用10作为每组题目数量
        int groupLength = 10;
        
        try {
            // 记录答题时间和分数
            score = value;
            time = new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date());
            if (timer != null) {
                timer.setText(time);
            }
            
            testcontext.getInstance().setAllowSwipe(true);
            
            // 强制更新当前题目的状态到testcontext
            testcontext.getInstance().setEvaluations(testcontext.getInstance().getEvaluations());
            
            // 更新testcontext中的计数，确保状态一致
            testcontext.getInstance().searchOne();
            
            // 直接计算当前进度（基于当前位置）
            int completedCount = position + 1;
            
            // 更新计数器显示
            if (counter != null) {
                counter.setText(completedCount + "/" + groupLength);
            }
            
            // 检查是否完成所有题目
            if (completedCount >= groupLength) {
                // 所有题目都已完成
                Activity context = testcontext.getInstance().getContext();
                if (context != null) {
                    Toast.makeText(context, "已完成测评题目！", Toast.LENGTH_SHORT).show();
                    // 调用回调方法，让testactivity处理后续逻辑
                    if (listener != null) {
                        listener.onAllQuestionComplete();
                    }
                }
                return;
            }
            
            // 还有题目未完成，继续导航
            int nextPosition = position + 1;
            // 确保nextPosition在有效范围内
            CustomViewPager viewPager = testcontext.getInstance().getViewPager();
            if (viewPager != null) {
                PagerAdapter adapter = viewPager.getAdapter();
                if (adapter != null) {
                    int adapterCount = adapter.getCount();
                    if (nextPosition >= 0 && nextPosition < adapterCount) {
                        viewPager.setCurrentItem(nextPosition, true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 不显示错误信息，避免用户困惑
        }
    }

    private void nextPage(int position,int count, int lengths) {
        // 此方法已不再使用
    }
}
