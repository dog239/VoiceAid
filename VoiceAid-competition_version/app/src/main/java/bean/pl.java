package bean;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import utils.testcontext;

/**
 * 前语言能力（PL）
 */
public class pl extends evaluation {
    private String skill;
    private String prompt;
    private Integer score;
    private String observation;
    private String time;
    private String audioPath;

    private final String colum1 = "题号";
    private final String colum2 = "技能";
    private final String colum3 = "得分";

    public pl(int num, String skill, String prompt, Integer score, String observation, String time, String audioPath) {
        super(num);
        this.num = num;
        this.skill = skill;
        this.prompt = prompt;
        this.score = score;
        this.observation = observation;
        this.time = time;
        this.audioPath = audioPath;
    }

    private utils.allquestionlistener listener;
    public void setAllQuestionListener(utils.allquestionlistener listener){
        this.listener = listener;
    }

    public static pl fromJson(JSONObject object) throws JSONException {
        int num = object.optInt("num", 0);
        String skill = object.optString("skill", "");
        String prompt = object.optString("prompt", "");
        Integer score = object.has("score") && !object.isNull("score") ? object.getInt("score") : null;
        String observation = object.has("observation") && !object.isNull("observation") ? object.getString("observation") : null;
        String time = object.has("time") && !object.isNull("time") ? object.getString("time") : null;
        String audioPath = object.has("audioPath") && !object.isNull("audioPath") ? object.getString("audioPath") : null;
        return new pl(num, skill, prompt, score, observation, time, audioPath);
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
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

    @Override
    public Boolean getResult() {
        if (score == null) {
            return null;
        }
        return score == 1;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int handle(View[] views, int position) {
        for(int i=0;i<3;i++)
            views[i].setVisibility(View.VISIBLE);
        if(position==0){
            ((TextView)views[0]).setText(colum1);
            ((TextView)views[1]).setText(colum2);
            ((TextView)views[2]).setText(colum3);
        }
        else {
            ((TextView)views[0]).setText(num <= 0 ? "" : String.valueOf(num));
            ((TextView)views[1]).setText(skill);
            int value = score == null ? 0 : score;
            ((TextView)views[2]).setText(String.valueOf(value));
        }
        return 2;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList, List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList, String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        ImageView imageView = view.findViewById(R.id.imageView);
        Button startButton = view.findViewById(R.id.btn_start);
        Button yesButton = view.findViewById(R.id.btn_right);
        Button noButton = view.findViewById(R.id.btn_wrong);
        TextView numberTextView = view.findViewById(R.id.tv_2);

        if (startButton != null) {
            startButton.setVisibility(View.GONE);
        }
        if (yesButton != null) {
            yesButton.setText("有");
        }
        if (noButton != null) {
            noButton.setText("无");
        }

        // 根据场景显示固定图片
        if (imageView != null) {
            String scene = testcontext.getInstance().getScene();
            if ("B".equals(scene)) {
                // 场景B：玩球场景
                int ballImageResId = view.getContext().getResources().getIdentifier("qy2", "drawable", view.getContext().getPackageName());
                if (ballImageResId != 0) {
                    imageView.setImageResource(ballImageResId);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                // 场景A：吹泡泡场景
                int bubbleImageResId = view.getContext().getResources().getIdentifier("qy1", "drawable", view.getContext().getPackageName());
                if (bubbleImageResId != 0) {
                    imageView.setImageResource(bubbleImageResId);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        }

        String skillText = skill == null ? "" : skill;
        String promptText = prompt == null ? "" : prompt;
        numberTextView.setText("第" + TabString[position] + "题：" + skillText + "\n提示：" + promptText);
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        if(time!=null){
            if (score != null && score == 1) {
                yesButton.setEnabled(false);
                noButton.setEnabled(true);
            } else if (score != null) {
                noButton.setEnabled(false);
                yesButton.setEnabled(true);
            } else {
                yesButton.setEnabled(false);
                noButton.setEnabled(false);
            }
        } else {
            yesButton.setEnabled(true);
            noButton.setEnabled(true);
        }

        yesButton.setOnClickListener(v -> handleAnswer(1, position, counter, timer));
        noButton.setOnClickListener(v -> handleAnswer(0, position, counter, timer));
    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        object.put("skill", skill);
        object.put("prompt", prompt);
        if(score!=null){
            object.put("score",score);
            object.put("observation", observation == null ? JSONObject.NULL : observation);
            object.put("audioPath", audioPath == null ? JSONObject.NULL : audioPath);
            object.put("time", time);
        }
        else {
            object.put("score",JSONObject.NULL);
            object.put("observation", JSONObject.NULL);
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        // 为不同场景使用不同的JSONArray键
        String scene = testcontext.getInstance().getScene();
        String plKey = "PL_" + scene;
        if (!evaluations.has(plKey)) {
            evaluations.put(plKey, new JSONArray());
        }
        evaluations.getJSONArray(plKey).put(object);
    }

    private void handleAnswer(int value, int position, TextView counter, TextView timer) {
        if (time == null) {
            testcontext.getInstance().incrementCount();
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
        }
        score = value;
        time = new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date());
        timer.setText(time);
        testcontext.getInstance().setAllowSwipe(true);
        nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
    }

    private void nextPage(int position,int count, int lengths) {
        int nP = position + 1;
        // 检查当前场景的所有题目是否都已完成
        boolean allQuestionsCompleted = true;
        for (int i = 0; i < lengths; i++) {
            if (testcontext.getInstance().getEvaluations() != null && i < testcontext.getInstance().getEvaluations().size()) {
                evaluation eval = testcontext.getInstance().getEvaluations().get(i);
                if (eval != null && eval.getTime() == null) {
                    allQuestionsCompleted = false;
                    break;
                }
            }
        }
        if (allQuestionsCompleted) {
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onAllQuestionComplete();
            }
            testcontext.getInstance().getContext().finish();
        }
        if(nP >= lengths){
            testcontext.getInstance().getViewPager().setCurrentItem(testcontext.getInstance().searchOne(), true);
        }
        else {
            testcontext.getInstance().getViewPager().setCurrentItem(nP, true);
        }
    }
}
