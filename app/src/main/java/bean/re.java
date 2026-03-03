package bean;

import android.util.Log;
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
import java.util.concurrent.atomic.AtomicReference;

import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.ImageUrls;
import utils.ResultContext;
import utils.testcontext;

/**
 * 词汇理解（RE）
 */
public class re extends evaluation {
    private String target;
    private String select;
    private Boolean result;

    private String targetC;

    private int select_num;

    private bean.audio audio;
    private String time;
    private final String colum1 = "题号";
    private final String colum2 = "目标词";
    private final String colum3 = "被试选择";
    private final String colum4 = "结果";
    private final String colum5 = "答题时间";
    private final String colum6 = "录音";
    public re(int num, String target, String targetC, String select, int select_num, Boolean result, bean.audio audio, String time) {
        super(num);
        this.target = target;
        this.select = select;
        this.select_num = select_num;
        this.result = result;
        this.audio = audio;
        this.time = time;
        this.targetC = targetC;
    }

    private utils.allquestionlistener listener;
    public void setAllQuestionListener(utils.allquestionlistener listener){
        this.listener = listener;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }
    public int getSelect_Num() {
        return select_num;
    }

    public void setSelect_Num(int select_num) {
        this.select_num = select_num;
    }

    public int getNum() {
        return num;
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

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
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
        else {
            ((TextView)views[0]).setText(String.valueOf(num));
            ((TextView)views[1]).setText(targetC);
            if(select!=null)
                ((TextView)views[2]).setText(select);
            if(result!=null)
                ((TextView)views[3]).setText(result? ResultContext.getInstance().getContext().getResources().getString(R.string.right) :
                        ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));
            if(audio!=null){
                if(audio!=null){
                    ((TextView)views[4]).setText(time);
                    ((TextView)views[5]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                    ((TextView)views[5]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                    AudioPlayer.getInstance().addIcon((TextView)views[5]);
                    views[5].setOnClickListener(v -> AudioPlayer.getInstance().play(audio.getPath(),position-1));
                }
            }

        }
        return 5;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList,String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        ImageView[] imageViews = new ImageView[6];
        imageViews[0] = view.findViewById(R.id.imageView1);
        imageViews[1] = view.findViewById(R.id.imageView2);
        imageViews[2] = view.findViewById(R.id.imageView3);
        imageViews[3] = view.findViewById(R.id.imageView4);
        imageViews[4] = view.findViewById(R.id.imageView5);
        imageViews[5] = view.findViewById(R.id.imageView6);
        TextView tv_hint = view.findViewById(R.id.tv_hint);
        Button startButton = view.findViewById(R.id.btn_start);
        Button nextButton = view.findViewById(R.id.btn_next);
        TextView numberTextView = view.findViewById(R.id.tv_2);

        for(int i=0;i<6;++i){
            imageViews[i].setImageResource(ImageGroupIdList.get(position)[i]);
            Log.d("IMAGE", String.valueOf(ImageGroupIdList.get(position)[i]));
        }


        numberTextView.setText("第"+TabString[position]+"题：找一找");
        tv_hint.setText(targetC);
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        if(result!=null){
            for(int i=0;i<6;++i){
                imageViews[i].setVisibility(View.VISIBLE);
            }
            startButton.setEnabled(false);
            imageViews[select_num].setBackgroundResource(R.drawable.cage2);

        }

        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };
        startButton.setOnClickListener(v -> {
            try {
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                startButton.setEnabled(false);
                for(int i=0;i<6;++i){
                    imageViews[i].setVisibility(View.VISIBLE);
                }
                select = "";
                select_num = -1;
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
                imageViews[0].setEnabled(false);
                imageViews[1].setEnabled(false);
                imageViews[2].setEnabled(false);
                imageViews[3].setEnabled(false);
                if(select != null && select.equals(targetC)){
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

        imageViews[0].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=0;i<6;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[0].setBackgroundResource(R.drawable.cage2);
            select = ImageUrls.RE_imageUrlsC[ImageUrls.RE_turn[num-1][0]];
            select_num = 0;
        });
        imageViews[1].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=0;i<6;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[1].setBackgroundResource(R.drawable.cage2);
            select = ImageUrls.RE_imageUrlsC[ImageUrls.RE_turn[num-1][1]];
            select_num = 1;
        });
        imageViews[2].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=0;i<6;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[2].setBackgroundResource(R.drawable.cage2);
            select = ImageUrls.RE_imageUrlsC[ImageUrls.RE_turn[num-1][2]];
            select_num = 2;
        });
        imageViews[3].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=0;i<6;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[3].setBackgroundResource(R.drawable.cage2);
            select = ImageUrls.RE_imageUrlsC[ImageUrls.RE_turn[num-1][3]];
            select_num = 3;
        });
        imageViews[4].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=0;i<6;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[4].setBackgroundResource(R.drawable.cage2);
            select = ImageUrls.RE_imageUrlsC[ImageUrls.RE_turn[num-1][4]];
            select_num = 4;
        });
        imageViews[5].setOnClickListener(v -> {
            // 清除之前选中的红框
            for(int i=0;i<6;++i){
                imageViews[i].setBackgroundResource(R.drawable.cage3);
            }
            // 为新选中的按钮设置红框背景
            imageViews[5].setBackgroundResource(R.drawable.cage2);
            select = ImageUrls.RE_imageUrlsC[ImageUrls.RE_turn[num-1][5]];
            select_num = 5;
        });
    }


    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        object.put("target",target);
        object.put("targetC",targetC);
        if(time!=null){
            object.put("result",result);
            object.put("select",select);
            object.put("select_num",select_num);
            object.put("audioPath",audio.getPath());
            object.put("time",time);
        }else {
            object.put("result",JSONObject.NULL);
            object.put("select",JSONObject.NULL);
            object.put("select_num",JSONObject.NULL);
            object.put("audioPath",JSONObject.NULL);
            object.put("time",JSONObject.NULL);
        }
        evaluations.getJSONArray("RE").put(object);
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
