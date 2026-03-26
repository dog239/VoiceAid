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

import utils.AudioRecorder;
import utils.testcontext;

/**
 * 词汇理解（EV）
 */
public class ev extends evaluation {
    private String target;
    private Boolean result;
    private String time;
    // 计时器相关变量
    private android.os.Handler handler;
    private Runnable runnable;

    private final String colum1 = "序号";
    private final String colum2 = "测试点";
    private final String colum3 = "目标词";
    private final String colum4 = "结果";
    private final String colum5 = "答题时长";
    public ev(int num, String target, Boolean result, String time) {
        super(num);
        this.target = target;
        this.result = result;
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
            ((TextView)views[0]).setText(colum1);
            ((TextView)views[1]).setText(colum2);
            ((TextView)views[2]).setText(colum3);
            ((TextView)views[3]).setText(colum4);
            ((TextView)views[4]).setText(colum5);
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
                    testPoint = "分类名词（名词上位词）";
                    break;
                case 7:
                    testPoint = "形容词";
                    break;
                default:
                    testPoint = "";
                    break;
            }
            ((TextView)views[1]).setText(testPoint);
            
            ((TextView)views[2]).setText(target);

            if(result!=null)
                ((TextView)views[3]).setText(result? "正确" : "错误");
            if(time!=null){
                ((TextView)views[4]).setText(time);
            }

        }
        return 4;
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList, List<Integer []> ImageGroupIdList,
                     List<String []> StringGroupIdList, String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        // 使用 word_test1.xml 布局中现有的控件
        GridLayout gridLayout = view.findViewById(R.id.gridLayout);
        TextView tv2 = view.findViewById(R.id.tv_2);
        Button start = view.findViewById(R.id.btn_start);
        Button correct = view.findViewById(R.id.btn_right);
        Button wrong = view.findViewById(R.id.btn_wrong);

        // 隐藏不需要的按钮和单个图片视图
        start.setVisibility(View.GONE);
        correct.setVisibility(View.GONE);
        wrong.setVisibility(View.GONE);
        view.findViewById(R.id.btn_mid).setVisibility(View.GONE);
        view.findViewById(R.id.imageView).setVisibility(View.GONE);

        // 题目配置：每个题目的指导语和正确答案索引
        String[][] questionConfigs = {
            {"请指出'勺子'在哪里？", "3"},  // 第一题：勺子（正确答案在第四张图片）
            {"找一找眼睛", "3"},          // 第二题：眼睛（正确答案在第四张图片）
            {"找一找跑", "2"},            // 第三题：跑（正确答案在第三张图片）
            {"找一找吃", "1"},            // 第四题：吃（正确答案在第二张图片）
            {"找一找红", "1"},            // 第五题：红（正确答案在第二张图片）
            {"找一找鸟", "0"},            // 第六题：鸟（正确答案在第一张图片）
            {"找一找大", "0"}             // 第七题：大（正确答案在第一张图片）
        };

        // 根据题目位置获取配置
        String[] config = position < questionConfigs.length ? questionConfigs[position] : questionConfigs[0];
        String instruction = config[0];
        int correctIndex = Integer.parseInt(config[1]);

        // 设置指导语
        tv2.setText("第" + TabString[position] + "题：" + instruction);
        // 重新计算已完成的题目数量并更新计数显示
        testcontext.getInstance().searchOne();
        counter.setText(testcontext.getInstance().getCount() + "/" + testcontext.getInstance().getLengths());

        // 显示网格布局
        gridLayout.setVisibility(View.VISIBLE);
        
        // 获取网格中的图片视图
        ImageView image1 = view.findViewById(R.id.image1);
        ImageView image2 = view.findViewById(R.id.image2);
        ImageView image3 = view.findViewById(R.id.image3);
        ImageView image4 = view.findViewById(R.id.image4);
        ImageView image5 = view.findViewById(R.id.image5);
        ImageView image6 = view.findViewById(R.id.image6);
        
        // 存储所有图片视图
        ImageView[] imageViews = {image1, image2, image3, image4, image5, image6};
        
        // 定义每个题目的起始索引
        int[] startIndices = {
            0,   // 第一题：狗、汽车、球、勺子
            4,   // 第二题：嘴巴、鼻子、身体、眼睛、耳朵、手
            10,  // 第三题：跳、坐、跑、哭
            14,  // 第四题：玩、吃、读、洗
            18,  // 第五题：黑、红、黄、蓝
            22,  // 第六题：鸟、苹果、衣服、碗
            26   // 第七题：大、小
        };
        
        // 隐藏所有图片
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i].setVisibility(View.GONE);
        }
        
        // 计算起始索引
        int startIndex = position < startIndices.length ? startIndices[position] : 0;
        
        // 根据题目位置确定显示的图片数量
        int imageCount = 4; // 默认显示4张图片
        if (position == 1) { // 第二题（索引为1）
            imageCount = 6; // 显示6张图片
        } else if (position == 6) { // 第七题（索引为6）
            imageCount = 2; // 只显示2张图片
        }
        
        // 显示相应数量的图片
        for (int i = 0; i < imageCount && i < imageViews.length; i++) {
            imageViews[i].setVisibility(View.VISIBLE);
            
            // 计算当前图片的索引
            int currentImageIndex = startIndex + i;
            
            // 确保索引不超出范围
            if (currentImageIndex < ImageIdList.size()) {
                imageViews[i].setImageResource(ImageIdList.get(currentImageIndex));
            } else {
                // 处理越界情况，使用默认图片
                if (!ImageIdList.isEmpty()) {
                    imageViews[i].setImageResource(ImageIdList.get(0));
                }
            }
        }
        
        // 添加点击标志位，确保只允许点击一次
        final boolean[] hasClicked = {false};
        
        // 为所有显示的图片设置点击事件
        for (int i = 0; i < imageCount && i < imageViews.length; i++) {
            final int finalI = i;
            imageViews[i].setOnClickListener(v -> {
                // 检查是否已经点击过
                if (hasClicked[0]) {
                    return; // 已经点击过，不再处理
                }
                
                // 标记为已点击
                hasClicked[0] = true;
                
                // 检查是否点击了正确答案
                result = (finalI == correctIndex);
                
                // 停止计时器
                handler.removeCallbacks(runnable);
                
                // 记录答题时长
                if (timer != null) {
                    time = timer.getText().toString();
                    // 立即更新evaluation对象的time属性
                    this.setTime(time);
                }
                
                // 增加计数器
                testcontext.getInstance().incrementCount();
                // 更新计数显示
                counter.setText(testcontext.getInstance().getCount() + "/" + testcontext.getInstance().getLengths());
                
                // 进入下一题
                nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
            });
        }
        
        // 初始化计时器变量
        handler = new android.os.Handler();
        
        // 重置计时器为0
        timer.setText("00:00");
        
        // 重新创建runnable对象，确保每次都使用新的开始时间
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
                handler.postDelayed(this, 1000);
            }
        };
        
        // 开始时间统计
        handler.post(runnable);
    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num", num);
        object.put("target", target);
        if (result != null) {
            object.put("result", result);
            object.put("time", time);
        } else {
            object.put("result", JSONObject.NULL);
            object.put("time", JSONObject.NULL);
        }
        evaluations.getJSONArray("EV").put(object);
    }

    private void nextPage(int position, int count, int lengths) {
        int nP = position + 1;
        if (count >= lengths) {
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onAllQuestionComplete();
            }
            // 直接返回菜单界面
            testcontext.getInstance().getContext().finish();
            return;
        }
        if (nP >= lengths) {
            testcontext.getInstance().getViewPager().setCurrentItem(testcontext.getInstance().searchOne(), true);
        } else {
            testcontext.getInstance().getViewPager().setCurrentItem(nP, true);
        }
    }
}
