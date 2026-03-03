package bean;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import utils.AudioRecorder;
import utils.ImageUrls;
import utils.testcontext;

/**
 * 非词复述（NWR）
 */
public class nwr extends evaluation {
    private String[] question;
    private Boolean[] results;
    private Boolean result;
    private String time;
    private bean.audio audio;
    private final String colum1 = "题号";
    private final String colum2 = "题目";
    private final String colum3 = "测试结果";

    public nwr(int num, String[] question, Boolean[] results, bean.audio audio, String time) {
        super(num);
        this.num = num;
        this.question = question;
        this.results = results;
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

    public String[] getQuestion() {
        return question;
    }

    public void setQuestion(String[] question) {
        this.question = question;
    }

    public Boolean getResult() {
        //Log.d("rrrrr",time);
        if(time == null ){
            return null;
        }else if(!time.equals("null")){
            return true;
        }else{
            return null;
        }
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
        for(int i=0;i<3;i++)
            views[i].setVisibility(View.VISIBLE);
        if (position == 0) {
            ((TextView)views[0]).setText(colum1);
            ((TextView)views[1]).setText(colum2);
            ((TextView)views[2]).setText(colum3);
        }
        else {
            ((TextView)views[0]).setText(String.valueOf(num));
            String s = "";
            for(String q:question){
                s+=q+",";
            }
            ((TextView)views[1]).setText(s.substring(0,s.length()-1));
            ((TextView)views[2]).setText(s.substring(0,s.length()-1));
        }
        return 2;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList,String[] Hint, String[] TabString,
                     TextView counter, TextView timer){
        TextView[] textViews = new TextView[6];
        CheckBox[] checkBoxes = new CheckBox[6];

        textViews[0] = view.findViewById(R.id.tv1);
        textViews[1] = view.findViewById(R.id.tv2);
        textViews[2] = view.findViewById(R.id.tv3);
        textViews[3] = view.findViewById(R.id.tv4);
        textViews[4] = view.findViewById(R.id.tv5);
        textViews[5] = view.findViewById(R.id.tv6);

        checkBoxes[0] = view.findViewById(R.id.cb1);
        checkBoxes[1] = view.findViewById(R.id.cb2);
        checkBoxes[2] = view.findViewById(R.id.cb3);
        checkBoxes[3] = view.findViewById(R.id.cb4);
        checkBoxes[4] = view.findViewById(R.id.cb5);
        checkBoxes[5] = view.findViewById(R.id.cb6);
        TextView numberTextView = view.findViewById(R.id.tv_2);
        Button startButton = view.findViewById(R.id.btn_start);
        Button nextButton = view.findViewById(R.id.btn_next);

        numberTextView.setText("第"+TabString[position]+"题：读一读");
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());


        for(int i=0;i<6;++i){
            textViews[5-i].setText(ImageUrls.NWR_characsC[position][i]);
            checkBoxes[i].setText(StringGroupIdList.get(position)[i]);
            question[i] = ImageUrls.NWR_characsC[position][i];
        }


        if(time!=null){
            for(int i=0;i<6;++i){
                checkBoxes[i].setVisibility(View.VISIBLE);
                textViews[i].setVisibility(View.VISIBLE);
                checkBoxes[i].setEnabled(false);
                textViews[i].setEnabled(false);
                if(results[i]){
                    checkBoxes[i].setChecked(true);
                }else{
                    checkBoxes[i].setChecked(false);
                }
            }
            startButton.setEnabled(false);
        }



        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };
        checkBoxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    results[0] = true;
                }else{
                    results[0] = false;
                }
                for(int i=0;i<6;++i){
                    Log.d("xavier0",String.valueOf(results[0]));
                }

            }
        });

        checkBoxes[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    results[1] = true;
                }else{
                    results[1] = false;
                }
                for(int i=0;i<6;++i){
                    Log.d("xavier1",String.valueOf(results[0]));
                }
            }
        });

        checkBoxes[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    results[2] = true;
                }else{
                    results[2] = false;
                }
                for(int i=0;i<6;++i){
                    Log.d("xavier2",String.valueOf(results[0]));
                }
            }

        });

        checkBoxes[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    results[3] = true;
                }else{
                    results[3] = false;
                }
                for(int i=0;i<6;++i){
                    Log.d("xavier3",String.valueOf(results[0]));
                }
            }
        });

        checkBoxes[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    results[4] = true;
                }else{
                    results[4] = false;
                }
                for(int i=0;i<6;++i){
                    Log.d("xavier4",String.valueOf(results[0]));
                }
            }
        });

        checkBoxes[5].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 将RadioButton的选中状态存储到SharedPreferences中
                if(isChecked){
                    results[5] = true;
                }else{
                    results[5] = false;
                }
                for(int i=0;i<6;++i){
                    Log.d("xavier5",String.valueOf(results[0]));
                }
            }
        });
        startButton.setOnClickListener(v ->{
            try {
                for(int i=0;i<6;++i){
                    checkBoxes[i].setVisibility(View.VISIBLE);
                    textViews[i].setVisibility(View.VISIBLE);
                }
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                startButton.setEnabled(false);
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
                for(int i=0;i<6;++i){
                    checkBoxes[i].setEnabled(false);
                }
                AudioRecorder.getInstance().stopRecorder();
            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
            testcontext.getInstance().setAllowSwipe(true);
        });




    }


    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        if(time!=null){
            for(int i=0;i<6;++i){
                object.put("question"+(i+1),question[i]);
                object.put("results"+(i+1),results[i]);
                Log.d("xavierresults",i+String.valueOf(results[i]));
            }
            object.put("audioPath",audio.getPath());
            object.put("time",time);
        }
        else {
            for(int i=0;i<6;++i){
                object.put("question"+(i+1),question[i]);
                object.put("results"+(i+1),false);
            }
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        Log.d("xavier",object.toString());
        evaluations.getJSONArray("NWR").put(object);
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
