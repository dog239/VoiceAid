package bean;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import com.example.CCLEvaluation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.ResultContext;
import utils.RulerSeekBar;
import utils.testcontext;

/**
 * 个人生活经验（PN）
 */
public class pn extends evaluation {
    private RulerSeekBar mFontSeekBar;

    private Integer score;
    private String time;
    private Boolean result;
    private bean.audio audio;

    private final String colum1 = "题号";
    private final String colum2 = "得分";
    private final String colum3 = "答题时长";
    private final String colum4 = "录音";
    public pn(int num, Integer score, bean.audio audio, String time) {
        super(num);
        this.num = num;
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
        for(int i=0;i<4;i++)
            views[i].setVisibility(View.VISIBLE);
        if(position==0){
            ((TextView)views[0]).setText(colum1);
            ((TextView)views[1]).setText(colum2);
            ((TextView)views[2]).setText(colum3);
            ((TextView)views[3]).setText(colum4);
        }
        else {
            ((TextView)views[0]).setText(String.valueOf(num));
            ((TextView)views[1]).setText(String.valueOf(score));
            if(audio!=null){
                ((TextView)views[2]).setText(time);
                ((TextView)views[3]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                ((TextView)views[3]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                AudioPlayer.getInstance().addIcon((TextView)views[3]);
                views[3].setOnClickListener(v -> AudioPlayer.getInstance().play(audio.getPath(),position-1));
            }
        }
        return 3;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList,String[] Hint, String[] TabString,
                     TextView counter, TextView timer){

        ImageView imageView = view.findViewById(R.id.imageView);
        Button startButton = view.findViewById(R.id.btn_start);
        Button nextButton = view.findViewById(R.id.btn_next);
        TextView tvHint = view.findViewById(R.id.tv_hint);
        TextView numberTextView = view.findViewById(R.id.tv_2);
        TextView tvscore = view.findViewById(R.id.tvscore);

        mFontSeekBar = view.findViewById(R.id.dpi_seek_bar);
        mFontSeekBar.setRulerCount(4);
        mFontSeekBar.setRulerColor(0x00000000);
        mFontSeekBar.setRulerWidth(5);
        mFontSeekBar.setShowTopOfThumb(false);
        mFontSeekBar.setProgress(0);



        imageView.setImageResource(ImageIdList.get(position));
        tvHint.setText(StringGroupIdList.get(position)[0]);
        tvHint.append("\t");

        numberTextView.setText("第"+TabString[position]+"题：说一说");
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        if(time!=null && !time.equals("null")){
            imageView.setVisibility(View.VISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            mFontSeekBar.setProgress(score);
            mFontSeekBar.setVisibility(View.VISIBLE);
            mFontSeekBar.setEnabled(false);
            tvscore.setText("打分："+String.valueOf(score)+"分");
            tvscore.setVisibility(View.VISIBLE);
            startButton.setEnabled(false);
        }


        mFontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                score = progress;
                tvscore.setText("打分："+String.valueOf(score)+"分");
                Log.d("iiiii", "onProgressChanged: index = " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };
        startButton.setOnClickListener(v -> {
            try {
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                startButton.setEnabled(false);
                imageView.setVisibility(View.VISIBLE);
                tvHint.setVisibility(View.VISIBLE);
                score=0;
                mFontSeekBar.setProgress(0);
                mFontSeekBar.setVisibility(View.VISIBLE);
                tvscore.setText("打分："+String.valueOf(score)+"分");
                tvscore.setVisibility(View.VISIBLE);

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
                AudioRecorder.getInstance().stopRecorder();
            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());


        });

    }



    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        if(time!=null){
            object.put("score",score);
            object.put("audioPath",audio.getPath());
            object.put("time",time);
        }
        else {
            object.put("score",JSONObject.NULL);
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        evaluations.getJSONArray("PN").put(object);
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
