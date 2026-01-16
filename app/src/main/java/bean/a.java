package bean;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;
import com.example.CCLEvaluation.testactivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.ImageUrls;
import utils.ResultContext;
import utils.dataManager;
import utils.testcontext;

/**
 * 构音（A）
 */
public class a extends evaluation {
    private String target;
    private String progress;
    private String target_tone1;
    private String target_tone2;
    private bean.audio audio;
    private Boolean result;
    private String time;
    private final String colum1 = "题号";
    private final String colum2 = "目标词";
    private final String colum3 = "产出过程";
    private final String colum4 = "目标音1";
    private final String colum5 = "目标音2";
    private final String colum6 = "答题时长";
    private final String colum7 = "录音";

    private long timeInMilliseconds = 0L;

    public a(int num, String target, String progress, String target_tone1, String target_tone2, bean.audio audio, String time) {
        super(num);
        this.target = target;
        this.progress = progress;
        this.target_tone1 = target_tone1;
        this.target_tone2 = target_tone2;
        this.audio = audio;
        this.time = time;
    }

    private utils.allquestionlistener listener;
    public void setAllQuestionListener(utils.allquestionlistener listener){
        this.listener = listener;
    }

    public int getNum() {
        return this.num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }


    public String getTarget_tone1() {
        return target_tone1;
    }

    public void setTarget_tone1(String target_tone1) {
        this.target_tone1 = target_tone1;
    }

    public String getTarget_tone2() {
        return target_tone2;
    }

    public void setTarget_tone2(String target_tone2) {
        this.target_tone2 = target_tone2;
    }

    public bean.audio getAudio() {
        return audio;
    }

    public void setAudio(bean.audio audio) {
        this.audio = audio;
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
        String[][] a_ans = ImageUrls.A_proAns;
        for (int i = 0; i < 7; i++)
            views[i].setVisibility(View.VISIBLE);
        if (position == 0) {
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
            ((TextView)views[1]).setText(target);
            if(progress!=null)
                ((TextView)views[2]).setText(progress);
            if(target_tone1!=null){
                if(target_tone1.equals(a_ans[position-1][0])){
                    ((TextView)views[3]).setText(target_tone1);
                }else{
                    ((TextView)views[3]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.red));
                    ((TextView)views[3]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));
                }
            }
            if (target_tone2!=null){
                if(target_tone2.equals(a_ans[position-1][1])){
                    ((TextView)views[4]).setText(target_tone2);
                }else{
                    ((TextView)views[4]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.red));
                    ((TextView)views[4]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));
                }
            }
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
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView numberTextView = view.findViewById(R.id.tv_2);
        TextView ansTextView = view.findViewById(R.id.tv_ans);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        RadioButton radioButton1 = view.findViewById(R.id.rb1);
        RadioButton radioButton2 = view.findViewById(R.id.rb2);
        CheckBox checkBox1 = view.findViewById(R.id.cb1);
        CheckBox checkBox2 = view.findViewById(R.id.cb2);

        Button startButton = view.findViewById(R.id.btn_start);
        Button nextButton = view.findViewById(R.id.btn_next);

        imageView.setImageResource(ImageIdList.get(position));
        numberTextView.setText("第"+TabString[position]+"题：图片画的是什么？");
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
        ansTextView.setText(Hint[position]);
        radioButton1.setText("自发");
        radioButton2.setText("仿说");
        checkBox1.setText(StringGroupIdList.get(position)[0]);
        if(!StringGroupIdList.get(position)[1].equals("")){
            checkBox2.setText(StringGroupIdList.get(position)[1]);
        }



        if(time!=null){
            ansTextView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            radioButton1.setVisibility(View.VISIBLE);
            radioButton2.setVisibility(View.VISIBLE);
            radioButton1.setEnabled(false);
            radioButton2.setEnabled(false);
            if(progress.equals("自发")){
                radioButton1.setChecked(true);
                radioButton2.setChecked(false);
            }else if(progress.equals("仿说")){
                radioButton2.setChecked(true);
                radioButton1.setChecked(false);
            }else{
                radioButton1.setChecked(false);
                radioButton2.setChecked(false);
            }
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setEnabled(false);
            if(!target_tone1.equals("")){
                checkBox1.setChecked(true);
            }else{
                checkBox1.setChecked(false);
            }
            if(!StringGroupIdList.get(position)[1].equals("")){
                checkBox2.setVisibility(View.VISIBLE);
                checkBox2.setEnabled(false);
                if(!target_tone2.equals("")){
                    checkBox2.setChecked(true);
                }else{
                    checkBox2.setChecked(false);
                }
            }
            startButton.setEnabled(false);
        }




        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };



        Runnable updateTimerThread = new Runnable() {
            public void run() {

                //  timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                int secs = (int) (timeInMilliseconds / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                //timer.setText(String.format("%02d:%02d", mins, secs));

                // timerHandler.postDelayed(this, 0);
            }
        };



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb1){
                    progress = "自发";
                }
                if(checkedId == R.id.rb2){
                    progress = "仿说";
                }
            }
        });
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    target_tone1 = StringGroupIdList.get(position)[0];
                }else{
                    target_tone1 = "";
                }
            }
        });
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    target_tone2 = StringGroupIdList.get(position)[1];
                }else{
                    target_tone2 = "";
                }
            }
        });

        startButton.setOnClickListener(v ->{
            try {
                ansTextView.setVisibility(View.VISIBLE);
                radioButton1.setVisibility(View.VISIBLE);
                radioButton2.setVisibility(View.VISIBLE);
                checkBox1.setVisibility(View.VISIBLE);
                if(!StringGroupIdList.get(position)[1].equals("")) {
                    checkBox2.setVisibility(View.VISIBLE);
                }
                target_tone1 = "";
                target_tone2 = "";
                progress = "";
                testcontext.getInstance().getViewPager().setPagingEnabled(false);

                startButton.setEnabled(false);
                imageView.setVisibility(View.VISIBLE);
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


            testcontext.getInstance().getViewPager().setPagingEnabled(true);

            if(audio!=null){
                for(int i = 0;i<radioGroup.getChildCount();i++){
                    radioGroup.getChildAt(i).setEnabled(false);
                }
                checkBox1.setEnabled(false);
                checkBox2.setEnabled(false);
                AudioRecorder.getInstance().stopRecorder();
                result = new Boolean(true);
            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());

        });

    }




    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        object.put("target",target);
        if(time!=null){
            object.put("progress",progress);
            //if(target_tone1!=null){
            object.put("target_tone1",target_tone1);
            //}else{
            //object.put("target_tone1",JSONObject.NULL);
            //}
            //if(target_tone2!=null){
            object.put("target_tone2",target_tone2);
            //}else{
            //object.put("target_tone2",JSONObject.NULL);
            //}
            object.put("audioPath",audio.getPath());
            object.put("time",time);
        }
        else {
            object.put("progress",JSONObject.NULL);
            object.put("target_tone1",JSONObject.NULL);
            object.put("target_tone2",JSONObject.NULL);
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        evaluations.getJSONArray("A").put(object);
    }

    private void nextPage(int position,int count, int lengths){

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
        }else{
            testcontext.getInstance().getViewPager().setCurrentItem(nP, true);
        }
    }
}
