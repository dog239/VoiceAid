package bean;

import android.view.View;
import android.widget.Button;
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
 * 词义（S）
 */
public class s extends evaluation {
    private String question;
    private String answer;
    private Boolean result;
    private String time;

    private bean.audio audio;

    private final String colum1 = "题号";
    private final String colum2 = "题目";
    private final String colum3 = "答案";
    private final String colum4 = "被试结果";
    private final String colum5 = "答题时间";
    private final String colum6 = "录音";


    public s(int num, String question, String answer, Boolean result, bean.audio audio, String time) {
        super(num);
        this.num = num;
        this.question = question;
        this.answer = answer;
        this.result = result;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
    @Override
    public String getTime() {
        return time;
    }
    public void setTime(String time){
        this.time = time;
    }

    @Override
    public int handle(View[] views, int position) {
        for(int i=0;i<6;i++)
            views[i].setVisibility(View.VISIBLE);
        if(position==0){
            ((TextView)views[0]).setText(colum1);
            ((TextView)views[1]).setText(colum2);
            ((TextView)views[2]).setText(colum3);
            ((TextView)views[3]).setText(colum4);
            ((TextView)views[4]).setText(colum5);
            ((TextView)views[5]).setText(colum6);
        }
        else{
            ((TextView)views[0]).setText(String.valueOf(num));
            ((TextView)views[1]).setText(question);
            ((TextView)views[2]).setText(answer);
            if(result!=null)
                ((TextView)views[3]).setText(result? ResultContext.getInstance().getContext().getResources().getString(R.string.right) :
                        ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));

            if(audio!=null){
                ((TextView)views[4]).setText(time);
                ((TextView)views[5]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                ((TextView)views[5]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                AudioPlayer.getInstance().addIcon((TextView)views[5]);
                views[5].setOnClickListener(v -> AudioPlayer.getInstance().play(audio.getPath(),position-1));
            }
        }
        return 5;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList,String[] Hint, String[] TabString,
                     TextView counter, TextView timer){
        TextView textView = view.findViewById(R.id.tv_hint);
        Button start = view.findViewById(R.id.btn_start);
        Button correct = view.findViewById(R.id.btn_right);
        Button wrong = view.findViewById(R.id.btn_wrong);
        TextView tv2 = view.findViewById(R.id.tv_2);
        tv2.setText("第"+TabString[position]+"题：找找两样物品的相似点");
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        textView.setText(Hint[position]);


        if(result!=null){
            textView.setVisibility(View.VISIBLE);
            start.setEnabled(false);
            if(result){
                correct.setEnabled(false);
                wrong.setEnabled(true);
            }
            else{
                wrong.setEnabled(false);
                correct.setEnabled(true);
            }
        }



        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };

        start.setOnClickListener(v -> {
            try {
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                start.setEnabled(false);
                correct.setEnabled(true);
                wrong.setEnabled(true);
                textView.setVisibility(View.VISIBLE);
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
        correct.setOnClickListener(v -> {
            testcontext.getInstance().getViewPager().setPagingEnabled(true);
            correct.setEnabled(false);
            wrong.setEnabled(true);
            if(audio!=null){
                AudioRecorder.getInstance().stopRecorder();
                result = new Boolean(true);
            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
        });
        wrong.setOnClickListener(v -> {
            testcontext.getInstance().getViewPager().setPagingEnabled(true);
            correct.setEnabled(true);
            wrong.setEnabled(false);
            if(audio!=null){
                AudioRecorder.getInstance().stopRecorder();
                result = new Boolean(false);
            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
        });


    }


    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        object.put("question",question);
        object.put("answer",answer);
        if(result!=null){
            object.put("result",result);
            object.put("audioPath",audio.getPath());
            object.put("time",time);
        }
        else {
            object.put("result",JSONObject.NULL);
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        evaluations.getJSONArray("S").put(object);

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
