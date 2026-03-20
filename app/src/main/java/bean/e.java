package bean;

import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import utils.ResultContext;
import utils.testcontext;

/**
 * 词汇表达（E）
 */
public class e extends evaluation {
    private String target;
    private Boolean result;
    private String time;
    private android.os.Handler handler;
    private Runnable runnable;

    private String colum1 = "序号";
    private String colum2 = "测试点";
    private String colum3 = "目标词";
    private String colum4 = "结果";
    private String colum5 = "答题时长";
    public e(int num, String target, Boolean result, String time) {
        super(num);
        this.target = target;
        this.result = result;
        this.time = time;
    }
    
    public e(int num, String colum1, String colum2, String colum3, String colum4, String colum5) {
        super(num);
        this.colum1 = colum1;
        this.colum2 = colum2;
        this.colum3 = colum3;
        this.colum4 = colum4;
        this.colum5 = colum5;
        this.target = colum1;
        this.result = null;
        this.time = colum5;
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

    @Override
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public void stopTimer() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public int handle(View[] views, int position) {
        for(int i=0;i<5;i++)
            views[i].setVisibility(View.VISIBLE);
        if(num == 0){
            // 处理表头行
            if (target.equals("序号")) {
                // 使用自定义表头文本
                ((TextView)views[0]).setText("序号");
                ((TextView)views[1]).setText("测试点");
                ((TextView)views[2]).setText("目标词");
                ((TextView)views[3]).setText("结果");
                ((TextView)views[4]).setText("答题时长");
            } else {
                // 使用默认表头文本
                ((TextView)views[0]).setText(colum1);
                ((TextView)views[1]).setText(colum2);
                ((TextView)views[2]).setText(colum3);
                ((TextView)views[3]).setText(colum4);
                ((TextView)views[4]).setText(colum5);
            }
        }
        else if(num == -1) {
            // 处理总分行
            ((TextView)views[0]).setText("");
            ((TextView)views[1]).setText("");
            ((TextView)views[2]).setText(target);
            ((TextView)views[3]).setText(result != null ? result.toString() : "");
            ((TextView)views[4]).setText(time != null ? time : "");
        }
        else {
            ((TextView)views[0]).setText(String.valueOf(num));
            
            // 根据题目编号设置测试点
            String testPoint = "";
            switch (num) {
                case 1:
                    testPoint = "名词";
                    break;
                case 2:
                    testPoint = "名词";
                    break;
                case 3:
                    testPoint = "动词";
                    break;
                case 4:
                    testPoint = "动词";
                    break;
                case 5:
                    testPoint = "形容词";
                    break;
                case 6:
                    testPoint = "形容词";
                    break;
                case 7:
                    testPoint = "分类名词（名词上位词）";
                    break;
                default:
                    testPoint = "";
                    break;
            }
            ((TextView)views[1]).setText(testPoint);
            
            ((TextView)views[2]).setText(target);

            if(result!=null)
                ((TextView)views[3]).setText(result? ResultContext.getInstance().getContext().getResources().getString(R.string.right) :
                        ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));
            if(time!=null){
                ((TextView)views[4]).setText(time);
            }
        }
        return 4;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList,List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList,String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        // 获取图片视图
        ImageView imageView = view.findViewById(R.id.imageView);
        GridLayout gridLayout = view.findViewById(R.id.gridLayout);
        ImageView image1 = view.findViewById(R.id.image1);
        ImageView image2 = view.findViewById(R.id.image2);
        Button start = view.findViewById(R.id.btn_start);
        Button correct = view.findViewById(R.id.btn_right);
        Button wrong = view.findViewById(R.id.btn_wrong);
        TextView tv2 = view.findViewById(R.id.tv_2);

        // 隐藏所有图片视图，根据题目位置再显示
        imageView.setVisibility(View.GONE);
        gridLayout.setVisibility(View.GONE);
        image1.setVisibility(View.GONE);
        image2.setVisibility(View.GONE);

        // 根据题目位置设置不同的提示语格式
        if (position == 0) {
            // 第一题：杯子
            tv2.setText("第"+TabString[position]+"题：这个是____");
        } else if (position == 1) {
            // 第二题：耳朵
            tv2.setText("第"+TabString[position]+"题：这个是____");
        } else if (position == 2) {
            // 第三题：睡觉
            tv2.setText("第"+TabString[position]+"题：小朋友在____");
        } else if (position == 3) {
            // 第四题：喝水
            tv2.setText("第"+TabString[position]+"题：小朋友在____");
        } else if (position == 4) {
            // 第五题：红色和蓝色
            tv2.setText("第"+TabString[position]+"题：这个方块是红色，这个方块是____");
        } else if (position == 5) {
            // 第六题：牛仔裤和香蕉
            tv2.setText("第"+TabString[position]+"题：裤子是衣服，香蕉是____");
        } else if (position == 6) {
            // 第七题：短和长
            tv2.setText("第"+TabString[position]+"题：这根铅笔短，这根铅笔____");
        }
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        if(result!=null){
            // 显示相应的图片
            if (position < 4) {
                imageView.setVisibility(View.VISIBLE);
                if (position < ImageIdList.size()) {
                    imageView.setImageResource(ImageIdList.get(position));
                }
            } else if (position == 4) {
                gridLayout.setVisibility(View.VISIBLE);
                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                if (position < ImageIdList.size()) {
                    image1.setImageResource(ImageIdList.get(position));
                }
                if (position + 1 < ImageIdList.size()) {
                    image2.setImageResource(ImageIdList.get(position + 1));
                }
            } else if (position == 5) {
                gridLayout.setVisibility(View.VISIBLE);
                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                // 显示牛仔裤和香蕉图片
                if (6 < ImageIdList.size()) {
                    image1.setImageResource(ImageIdList.get(6));
                }
                if (7 < ImageIdList.size()) {
                    image2.setImageResource(ImageIdList.get(7));
                }
            } else if (position == 6) {
                gridLayout.setVisibility(View.VISIBLE);
                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                // 显示短和长图片
                if (8 < ImageIdList.size()) {
                    image1.setImageResource(ImageIdList.get(8));
                }
                if (9 < ImageIdList.size()) {
                    image2.setImageResource(ImageIdList.get(9));
                }
            }
            start.setEnabled(false);
            if(result){
                correct.setEnabled(false);
                wrong.setEnabled(true);
            }
            else{
                wrong.setEnabled(false);
                correct.setEnabled(true);
            }
            if (time != null) {
                timer.setText(time);
            }
        }

        start.setOnClickListener(v -> {
            try {
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                start.setEnabled(false);
                correct.setEnabled(true);
                wrong.setEnabled(true);
                // 显示相应的图片
                if (position < 4) {
                    imageView.setVisibility(View.VISIBLE);
                    if (position < ImageIdList.size()) {
                        imageView.setImageResource(ImageIdList.get(position));
                    }
                } else if (position == 4) {
                    gridLayout.setVisibility(View.VISIBLE);
                    image1.setVisibility(View.VISIBLE);
                    image2.setVisibility(View.VISIBLE);
                    if (position < ImageIdList.size()) {
                        image1.setImageResource(ImageIdList.get(position));
                    }
                    if (position + 1 < ImageIdList.size()) {
                        image2.setImageResource(ImageIdList.get(position + 1));
                    }
                } else if (position == 5) {
                    gridLayout.setVisibility(View.VISIBLE);
                    image1.setVisibility(View.VISIBLE);
                    image2.setVisibility(View.VISIBLE);
                    // 显示牛仔裤和香蕉图片
                    if (6 < ImageIdList.size()) {
                        image1.setImageResource(ImageIdList.get(6));
                    }
                    if (7 < ImageIdList.size()) {
                        image2.setImageResource(ImageIdList.get(7));
                    }
                } else if (position == 6) {
                    gridLayout.setVisibility(View.VISIBLE);
                    image1.setVisibility(View.VISIBLE);
                    image2.setVisibility(View.VISIBLE);
                    // 显示短和长图片
                    if (8 < ImageIdList.size()) {
                        image1.setImageResource(ImageIdList.get(8));
                    }
                    if (9 < ImageIdList.size()) {
                        image2.setImageResource(ImageIdList.get(9));
                    }
                }

                handler = new android.os.Handler();
                runnable = new Runnable() {
                    long startTime = System.currentTimeMillis();
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - startTime;
                        int minutes = (int) (elapsedTime / 60000);
                        int seconds = (int) ((elapsedTime % 60000) / 1000);
                        String timeString = String.format("%02d:%02d", minutes, seconds);
                        timer.setText(timeString);
                        time = timeString;
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.post(runnable);

                // 移除这里的incrementCount()调用，因为题目还没有完成
                counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

            } catch (Exception e) {
                Toast.makeText(testcontext.getInstance().getContext(), "启动失败！", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        });
        correct.setOnClickListener(v -> {
            testcontext.getInstance().getViewPager().setPagingEnabled(true);
            correct.setEnabled(false);
            wrong.setEnabled(true);
            stopTimer();
            result = new Boolean(true);
            // 增加完成题目的计数
            testcontext.getInstance().incrementCount();
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
            testcontext.getInstance().setAllowSwipe(true);

        });
        wrong.setOnClickListener(v -> {
            testcontext.getInstance().getViewPager().setPagingEnabled(true);
            correct.setEnabled(true);
            wrong.setEnabled(false);
            stopTimer();
            result = new Boolean(false);
            // 增加完成题目的计数
            testcontext.getInstance().incrementCount();
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
            testcontext.getInstance().setAllowSwipe(true);
        });

    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num",num);
        object.put("target",target);
        if(result!=null){
            object.put("result",result);
            object.put("time",time);
        }
        else {
            object.put("result",JSONObject.NULL);
            object.put("time",time != null ? time : JSONObject.NULL);
        }
        evaluations.getJSONArray("E").put(object);
    }

    private void nextPage(int position,int count, int lengths) {
        int nP = position + 1;
        if (count >= lengths) {
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onAllQuestionComplete();
            }
            // 不再直接finish()，而是由listener.onAllQuestionComplete()调用performCleanup
            return;
        }
        if(nP >= lengths){
            testcontext.getInstance().getViewPager().setCurrentItem(testcontext.getInstance().searchOne(), true);
        }
        else {
            testcontext.getInstance().getViewPager().setCurrentItem(nP, true);
        }
    }

}
