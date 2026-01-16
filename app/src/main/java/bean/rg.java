package bean;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.ResultContext;
import utils.testcontext;

/**
 * 语法理解（RG）
 */
public class rg extends evaluation {
    private String question;
    private String right_option;
    private String answer;
    private Integer score;
    private String time;
    private bean.audio audio;
    private Boolean result;
    private final String colum1 = "题号";
    private final String colum2 = "题目";
    private final String colum3 = "正确选项";
    private final String colum4 = "被选选项";
    private final String colum5 = "结果";
    private final String colum6 = "答题时间";
    private final String colum7 = "录音";

    public rg(int num, String question, String right_option, String answer, Boolean result, Integer score, bean.audio audio, String time) {
        super(num);
        this.num = num;
        this.question = question;
        this.right_option = right_option;
        this.answer = answer;
        this.result = result;
        this.score = score;
        this.audio = audio;
        this.time = time;
    }
    private utils.allquestionlistener listener;
    public void setAllQuestionListener(utils.allquestionlistener listener){
        this.listener = listener;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getRight_option() {
        return right_option;
    }

    public void setRight_option(String right_option) {
        this.right_option = right_option;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    @Override
    public String getTime() {
        return time;
    }

    @Override
    public Boolean getResult() {
        return result;
    }

    public void setTime(String time){
        this.time = time;
    }

    @Override
    public int handle(View[] views, int position) {
        for(int i=0;i<7;i++)
            views[i].setVisibility(View.VISIBLE);
        if(position==0){
            ((TextView)views[0]).setText(colum1);
            ((TextView)views[1]).setText(colum2);
            ((TextView)views[2]).setText(colum3);
            ((TextView)views[3]).setText(colum4);
            ((TextView)views[4]).setText(colum5);
            ((TextView)views[5]).setText(colum6);
            ((TextView)views[6]).setText(colum7);
        }
        else {
            ((TextView)views[0]).setText(String.valueOf(num));
            ((TextView)views[1]).setTextSize(20);
            if(question.length()>8){
                ((TextView)views[1]).setTextSize(15);
            }
            ((TextView)views[1]).setText(question);
            ((TextView)views[2]).setText(right_option);
            if(answer!=null)
                ((TextView)views[3]).setText(answer);
            if(result!=null)
                ((TextView)views[4]).setText(result? ResultContext.getInstance().getContext().getResources().getString(R.string.right) :
                        ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));
            if(audio!=null){
                ((TextView)views[5]).setText(time);
                ((TextView)views[6]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                ((TextView)views[6]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                AudioPlayer.getInstance().addIcon((TextView)views[6]);
                views[6].setOnClickListener(v -> AudioPlayer.getInstance().play(audio.getPath(),position-1));

            }

        }
        return 6;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList,String[] Hint, String[] TabString,
                     TextView counter, TextView timer){
        ImageView[] imageViews = new ImageView[5];
        imageViews[0] = view.findViewById(R.id.imageView0);
        imageViews[1] = view.findViewById(R.id.imageView1);
        imageViews[2] = view.findViewById(R.id.imageView2);
        imageViews[3] = view.findViewById(R.id.imageView3);
        imageViews[4] = view.findViewById(R.id.imageView4);
        Button startButton = view.findViewById(R.id.btn_start);
        Button nextButton = view.findViewById(R.id.btn_next);
        TextView numberTextView = view.findViewById(R.id.tv_2);
        TextView hintTextView = view.findViewById(R.id.tv_hint);

        imageViews[0].setImageResource(ImageIdList.get(position));


        numberTextView.setText("第"+TabString[position]+"题：找一找");
        hintTextView.setText(Hint[position]);
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        if(time!=null){
            for(int i=0;i<5;++i){
                imageViews[i].setVisibility(View.VISIBLE);
                imageViews[i].setEnabled(false);
            }
            startButton.setEnabled(false);
            if(answer != null){
                switch (answer){
                    case "A": imageViews[0].setBackgroundResource(R.drawable.cage2);
                        break;
                    case "B": imageViews[1].setBackgroundResource(R.drawable.cage2);
                        break;
                    case "C": imageViews[2].setBackgroundResource(R.drawable.cage2);
                        break;
                    case "D": imageViews[3].setBackgroundResource(R.drawable.cage2);
                        break;
                }
            }
        }

        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };
        startButton.setOnClickListener(v -> {
            try {
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                startButton.setEnabled(false);
                for(int i=0;i<5;++i){
                    imageViews[i].setVisibility(View.VISIBLE);
                }
                result = false;
                answer = "";
                score = -1;
                AudioRecorder.getInstance().setOnRefreshUIThreadListener(listener);
                AudioRecorder.getInstance().startRecorder();
                audio = new audio(AudioRecorder.getInstance().getOutputFilePath());
                testcontext.getInstance().incrementCount();
                counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

            } catch (IOException e) {
                Toast.makeText(testcontext.getInstance().getContext(), "录制失败！", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        });
        nextButton.setOnClickListener(v -> {
            if(audio!=null){
                imageViews[1].setEnabled(false);
                imageViews[2].setEnabled(false);
                imageViews[3].setEnabled(false);
                imageViews[4].setEnabled(false);

                if(result != null && answer.equals(right_option)){
                    AudioRecorder.getInstance().stopRecorder();
                    result = new Boolean(true);
                }else{
                    AudioRecorder.getInstance().stopRecorder();
                    result = new Boolean(false);
                }

            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
            testcontext.getInstance().getViewPager().setPagingEnabled(true);

        });

        imageViews[1].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=1;i<5;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[1].setBackgroundResource(R.drawable.cage2);
            answer = "D";
        });
        imageViews[2].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=1;i<5;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[2].setBackgroundResource(R.drawable.cage2);
            answer = "C";
        });
        imageViews[3].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=1;i<5;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[3].setBackgroundResource(R.drawable.cage2);
            answer = "B";
        });
        imageViews[4].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=1;i<5;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[4].setBackgroundResource(R.drawable.cage2);
            answer = "A";
        });
    }


    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("question",question);
        object.put("num",num);
        object.put("right_option",right_option);
        if(time!=null){
            object.put("result",result);
            object.put("answer",answer);
            object.put("score",score);
            object.put("audioPath",audio.getPath());
            object.put("time",time);
        }
        else {
            object.put("result",JSONObject.NULL);
            object.put("answer",JSONObject.NULL);
            object.put("score",JSONObject.NULL);
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        evaluations.getJSONArray("RG").put(object);

    }
    private void nextPage(int position,int count, int lengths) {
        int nP = position + 1;
        if (count >= lengths) {
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();
            //com.example.CCLEvaluation.testactivity.performCleanup();
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
