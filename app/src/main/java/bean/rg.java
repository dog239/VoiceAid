package bean;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

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
    // 存储第四组第十题六个小问的结果
    private Boolean[] subQuestionResults = new Boolean[6];
    // 存储第四组第十题六个小问的答案
    private String[] subQuestionAnswers = new String[6];
    private final String colum1 = "题号";
    private final String colum2 = "题目";
    private final String colum3 = "正确选项";
    private final String colum4 = "被选选项";
    private final String colum5 = "结果";
    private final String colum6 = "答题时间";
    private final String colum7 = "录音";
    
    // 计时器相关变量
    private android.os.Handler handler;
    private Runnable runnable;

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

    // 用于结果显示的构造器，接受11个参数（包含答题时间）
    private String group; // 组别
    private String questionNum; // 题目编号
    private String grammarPoint; // 语法点
    
    public rg(int num, String group, String questionNum, String question, String right_option, String answer, String result, Integer score, bean.audio audio, String time, String grammarPoint) {
        super(num);
        this.num = num;
        this.group = group;
        this.questionNum = questionNum;
        this.question = question;
        this.right_option = right_option;
        this.answer = answer;
        this.result = result != null && !result.isEmpty() ? Boolean.valueOf(result.equals("正确")) : null;
        this.score = score;
        this.audio = audio;
        this.time = time;
        this.grammarPoint = grammarPoint;
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
        // 确保至少有8个视图来显示所有信息（包括答题时间）
        int requiredViews = 8;
        if (views.length < requiredViews) {
            // 如果视图数量不足，只使用可用的视图
            requiredViews = views.length;
        }
        
        for(int i=0;i<requiredViews;i++)
            views[i].setVisibility(View.VISIBLE);
        
        if(position==0){
            // 显示表头
            ((TextView)views[0]).setText("测试组别");
            ((TextView)views[1]).setText("题  号");
            ((TextView)views[2]).setText("例  句");
            ((TextView)views[3]).setText("正确选项");
            ((TextView)views[4]).setText("被选选项");
            ((TextView)views[5]).setText("测试结果");
            if (requiredViews > 6) {
                ((TextView)views[6]).setText("答题时间");
            }
            if (requiredViews > 7) {
                ((TextView)views[7]).setText("对应测试语法点");
            }
        }
        else {
            // 显示题目数据
            // 测试组别
            if (group != null) {
                ((TextView)views[0]).setText(group);
            } else {
                ((TextView)views[0]).setText("");
            }
            
            // 题目编号
            if (questionNum != null) {
                ((TextView)views[1]).setText(questionNum);
            } else {
                ((TextView)views[1]).setText(String.valueOf(num));
            }
            
            // 例句
            ((TextView)views[2]).setTextSize(20);
            if(question.length()>8){
                ((TextView)views[2]).setTextSize(15);
            }
            ((TextView)views[2]).setText(question);
            
            // 正确选项
            ((TextView)views[3]).setText(right_option);
            
            // 被选选项
            if(answer!=null){
                ((TextView)views[4]).setText(answer);
            } else {
                ((TextView)views[4]).setText("");
            }
            
            // 测试结果
            if(result!=null){
                ((TextView)views[5]).setText(result? ResultContext.getInstance().getContext().getResources().getString(R.string.right) :
                        ResultContext.getInstance().getContext().getResources().getString(R.string.wrong));
            } else {
                ((TextView)views[5]).setText("");
            }
            
            // 答题时间
            if (requiredViews > 6) {
                if(time!=null){
                    ((TextView)views[6]).setText(time);
                } else {
                    ((TextView)views[6]).setText("");
                }
            }
            
            // 对应测试语法点
            if (requiredViews > 7 && grammarPoint != null) {
                ((TextView)views[7]).setText(grammarPoint);
            }
        }
        return requiredViews - 1;
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
        Button yesButton = view.findViewById(R.id.btn_yes);
        Button noButton = view.findViewById(R.id.btn_no);

        // 隐藏所有控件
        for(int i=0;i<5;++i){
            imageViews[i].setVisibility(View.GONE);
        }
        if(yesButton != null) yesButton.setVisibility(View.GONE);
        if(noButton != null) noButton.setVisibility(View.GONE);

        numberTextView.setText("第"+TabString[position]+"题");
        hintTextView.setText(Hint[position]);
        counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

        // 根据题目编号确定题目类型和加载对应的图片
        int questionNumber = position + 1;
        boolean isAnswered = time != null;

        if(isAnswered){
            startButton.setEnabled(false);
            // 显示已答题目的状态
            showQuestionInterface(view, questionNumber, isAnswered);
        }

        // 初始化计时器变量
        handler = new android.os.Handler();

        startButton.setOnClickListener(v -> {
            testcontext.getInstance().getViewPager().setPagingEnabled(false);
            startButton.setEnabled(false);
            result = false;
            answer = "";
            score = -1;

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

            // 根据题目类型显示对应的界面
            showQuestionInterface(view, questionNumber, isAnswered);
        });
        nextButton.setOnClickListener(v -> {
            // 禁用所有交互控件
            for(int i=0;i<5;++i){
                if(imageViews[i] != null) imageViews[i].setEnabled(false);
            }
            if(yesButton != null) yesButton.setEnabled(false);
            if(noButton != null) noButton.setEnabled(false);

            // 停止计时器
            handler.removeCallbacks(runnable);

            // 正确判断结果：直接使用已经在点击事件中设置的result值
            // 因为result已经在setupImageClickListeners和setupYesNoClickListeners中正确设置
            if (result == null) {
                // 如果result为null，进行默认判断
                result = answer != null && right_option != null && answer.equals(right_option);

            }

            // 记录答题时长
            time = timer.getText().toString();

            // 只有当用户完成题目时才增加计数
            testcontext.getInstance().incrementCount();
            // 实时更新进度条
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

            // 保存当前题目数据
            try {
                // 使用testcontext中的上下文获取fName
                Activity context = utils.testcontext.getInstance().getContext();
                if (context != null) {
                    String fName = context.getIntent().getStringExtra("fName");
                    if (fName != null) {
                        JSONObject data = utils.dataManager.getInstance().loadData(fName);
                        JSONObject evaluations = data.getJSONObject("evaluations");
                        toJson(evaluations);
                        utils.dataManager.getInstance().saveData(fName, data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
            testcontext.getInstance().getViewPager().setPagingEnabled(true);

        });
    }

    private void showQuestionInterface(View view, int questionNumber, boolean isAnswered) {
        ImageView[] imageViews = new ImageView[5];
        imageViews[0] = view.findViewById(R.id.imageView0);
        imageViews[1] = view.findViewById(R.id.imageView1);
        imageViews[2] = view.findViewById(R.id.imageView2);
        imageViews[3] = view.findViewById(R.id.imageView3);
        imageViews[4] = view.findViewById(R.id.imageView4);
        Button yesButton = view.findViewById(R.id.btn_yes);
        Button noButton = view.findViewById(R.id.btn_no);
        TextView hintTextView = view.findViewById(R.id.tv_hint);

        // 隐藏所有控件
        for(int i=0;i<5;++i){
            imageViews[i].setVisibility(View.GONE);
        }
        if(yesButton != null) yesButton.setVisibility(View.GONE);
        if(noButton != null) noButton.setVisibility(View.GONE);

        // 获取当前组别
        int groupNumber = testcontext.getInstance().getGroupNumber();

        // 获取当前题目的图片名称数组
        String[] imageNames = getImageNamesForQuestion(questionNumber);
        if (imageNames == null || imageNames.length == 0) {
            return; // 没有图片，直接返回
        }

        // 根据图片数量动态显示
        int imageCount = imageNames.length;

        // 根据组别和题目编号确定题目类型
        boolean needYesNoButtons = false;
        boolean isSingleImage = false;

        // 判断是否需要显示是/否按钮
        if (groupNumber == 1) {
            // 第一组
            if (questionNumber >= 13 && questionNumber <= 15) {
                needYesNoButtons = true;
                isSingleImage = true;
            } else if (questionNumber >= 16 && questionNumber <= 17) {
                // 5张图片的题目
            } else if (questionNumber >= 18 && questionNumber <= 21) {
                // 4张图片的题目
            }
        } else if (groupNumber == 2) {
            // 第二组
            if (questionNumber >= 7 && questionNumber <= 9) {
                needYesNoButtons = true;
                isSingleImage = true;
            } else if (questionNumber >= 10 && questionNumber <= 12) {
                // 5张图片的题目
            } else if (questionNumber >= 13 && questionNumber <= 18) {
                // 4张图片的题目
            }
        } else if (groupNumber == 3) {
            // 第三组
            if (questionNumber >= 16 && questionNumber <= 18) {
                // 需要按两次的题目，仍然显示4张图片
            } else {
                // 1-15题：4张图片
            }
        } else if (groupNumber == 4) {
            // 第四组
            if (questionNumber >= 7 && questionNumber <= 9) {
                needYesNoButtons = true;
                isSingleImage = true;
            } else if (questionNumber == 10) {
                // 故事题，不需要显示图片，有特殊处理
            }
        }

        // 处理单一图片+是/否按钮的情况
        if (isSingleImage && imageCount == 1) {
            // 显示单个图片
            imageViews[0].setVisibility(View.VISIBLE);
            if(!isAnswered) imageViews[0].setEnabled(true);

            // 加载图片
            int resId = testcontext.getInstance().getContext().getResources()
                    .getIdentifier(imageNames[0], "drawable", testcontext.getInstance().getContext().getPackageName());
            if(resId > 0) {
                imageViews[0].setImageResource(resId);
            }

            // 显示是/否按钮
            if(yesButton != null && noButton != null) {
                yesButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.VISIBLE);
                if(!isAnswered) {
                    yesButton.setEnabled(true);
                    noButton.setEnabled(true);
                }
            }

            // 设置是/否按钮点击监听器
            setupYesNoClickListeners(yesButton, noButton, questionNumber);
            return;
        }

        // 处理第四组第10题的故事理解（六个小问题）
        if (groupNumber == 4 && questionNumber == 10) {
            // 加载gr67图片
            int resId = testcontext.getInstance().getContext().getResources()
                    .getIdentifier("gr67", "drawable", testcontext.getInstance().getContext().getPackageName());
            if(resId > 0) {
                imageViews[0].setVisibility(View.VISIBLE);
                imageViews[0].setImageResource(resId);
            }
            
            // 隐藏其他图片
            for(int i=1;i<5;++i){
                imageViews[i].setVisibility(View.GONE);
            }
            
            // 隐藏黑色标题文字
            TextView numberTextView = view.findViewById(R.id.tv_2);
            if (numberTextView != null) {
                numberTextView.setVisibility(View.GONE);
            }
            
            // 显示故事题的提示文字
            if (hintTextView != null) {
                hintTextView.setVisibility(View.VISIBLE);
                hintTextView.setText("故事理解：请根据故事内容回答以下问题");
                hintTextView.setTextSize(18);
            }
            
            // 显示判断对错标签
            View judgmentLayout = view.findViewById(R.id.ll_judgment);
            if (judgmentLayout != null) {
                judgmentLayout.setVisibility(View.VISIBLE);
            }
            
            // 为每个子问题的单选按钮设置点击事件
            // 问题1
            RadioGroup rg_q1 = view.findViewById(R.id.rg_q1);
            if (rg_q1 != null) {
                rg_q1.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rb_q1_yes) {
                        subQuestionResults[0] = true;
                        subQuestionAnswers[0] = "RIGHT";
                    } else if (checkedId == R.id.rb_q1_no) {
                        subQuestionResults[0] = false;
                        subQuestionAnswers[0] = "WRONG";
                    }
                });
            }
            
            // 问题2
            RadioGroup rg_q2 = view.findViewById(R.id.rg_q2);
            if (rg_q2 != null) {
                rg_q2.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rb_q2_yes) {
                        subQuestionResults[1] = true;
                        subQuestionAnswers[1] = "RIGHT";
                    } else if (checkedId == R.id.rb_q2_no) {
                        subQuestionResults[1] = false;
                        subQuestionAnswers[1] = "WRONG";
                    }
                });
            }
            
            // 问题3
            RadioGroup rg_q3 = view.findViewById(R.id.rg_q3);
            if (rg_q3 != null) {
                rg_q3.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rb_q3_yes) {
                        subQuestionResults[2] = true;
                        subQuestionAnswers[2] = "RIGHT";
                    } else if (checkedId == R.id.rb_q3_no) {
                        subQuestionResults[2] = false;
                        subQuestionAnswers[2] = "WRONG";
                    }
                });
            }
            
            // 问题4
            RadioGroup rg_q4 = view.findViewById(R.id.rg_q4);
            if (rg_q4 != null) {
                rg_q4.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rb_q4_yes) {
                        subQuestionResults[3] = true;
                        subQuestionAnswers[3] = "RIGHT";
                    } else if (checkedId == R.id.rb_q4_no) {
                        subQuestionResults[3] = false;
                        subQuestionAnswers[3] = "WRONG";
                    }
                });
            }
            
            // 问题5
            RadioGroup rg_q5 = view.findViewById(R.id.rg_q5);
            if (rg_q5 != null) {
                rg_q5.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rg_q5_yes) {
                        subQuestionResults[4] = true;
                        subQuestionAnswers[4] = "RIGHT";
                    } else if (checkedId == R.id.rg_q5_no) {
                        subQuestionResults[4] = false;
                        subQuestionAnswers[4] = "WRONG";
                    }
                });
            }
            
            // 问题6
            RadioGroup rg_q6 = view.findViewById(R.id.rg_q6);
            if (rg_q6 != null) {
                rg_q6.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rg_q6_yes) {
                        subQuestionResults[5] = true;
                        subQuestionAnswers[5] = "RIGHT";
                    } else if (checkedId == R.id.rg_q6_no) {
                        subQuestionResults[5] = false;
                        subQuestionAnswers[5] = "WRONG";
                    }
                });
            }
            

            // 故事题不需要显示图片，但需要操作员判断
            return;
        }

        // 处理多图片的情况（4张或5张）
        if (imageCount == 4) {
            // 4张图片：使用imageView1-4（跳过imageView0）
            for(int i=0;i<4;++i){
                imageViews[i+1].setVisibility(View.VISIBLE);
                if(!isAnswered) imageViews[i+1].setEnabled(true);
            }

            // 加载4张图片
            for(int i=0;i<4;++i){
                int resId = testcontext.getInstance().getContext().getResources()
                        .getIdentifier(imageNames[i], "drawable", testcontext.getInstance().getContext().getPackageName());
                if(resId > 0) {
                    imageViews[i+1].setImageResource(resId);
                }
            }

            // 设置点击监听器（使用1-4）
            setupImageClickListeners(imageViews, 1, 4, questionNumber);

        } else if (imageCount == 5) {
            // 5张图片：使用imageView0-4
            // 先隐藏所有图片
            for(int i=0;i<5;++i){
                imageViews[i].setVisibility(View.GONE);
            }
            
            // 首先显示第一张图片（最上层）
            int resId = testcontext.getInstance().getContext().getResources()
                    .getIdentifier(imageNames[0], "drawable", testcontext.getInstance().getContext().getPackageName());
            if(resId > 0) {
                imageViews[0].setVisibility(View.VISIBLE);
                imageViews[0].setImageResource(resId);
                if(!isAnswered) imageViews[0].setEnabled(true);
            }
            
            // 延迟八秒显示其余4张图片，并隐藏第一张图片
            if (!isAnswered) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 隐藏第一张图片
                        imageViews[0].setVisibility(View.GONE);
                        
                        // 显示其余4张图片
                        for(int i=1;i<5;++i){
                            int resId = testcontext.getInstance().getContext().getResources()
                                    .getIdentifier(imageNames[i], "drawable", testcontext.getInstance().getContext().getPackageName());
                            if(resId > 0) {
                                imageViews[i].setVisibility(View.VISIBLE);
                                imageViews[i].setImageResource(resId);
                                if(!isAnswered) imageViews[i].setEnabled(true);
                            }
                        }
                        
                        // 调整问题文本的显示
                        if (hintTextView != null) {
                            hintTextView.setTextSize(20);
                            hintTextView.setPadding(20, 40, 20, 20);
                            hintTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        }
                    }
                }, 8000); // 延迟八秒显示其余图片并隐藏第一张图片
            }

            // 设置点击监听器（使用1-4，因为第一张图片8秒后会消失）
            setupImageClickListeners(imageViews, 1, 4, questionNumber);

        } else if (imageCount < 4) {
            // 小于4张图片的情况
            for(int i=0;i<imageCount;++i){
                imageViews[i].setVisibility(View.VISIBLE);
                if(!isAnswered) imageViews[i].setEnabled(true);
            }

            // 加载图片
            for(int i=0;i<imageCount;++i){
                int resId = testcontext.getInstance().getContext().getResources()
                        .getIdentifier(imageNames[i], "drawable", testcontext.getInstance().getContext().getPackageName());
                if(resId > 0) {
                    imageViews[i].setImageResource(resId);
                }
            }

            // 设置点击监听器
            setupImageClickListeners(imageViews, 0, imageCount - 1, questionNumber);
        }
    }

    private String[] getImageNamesForQuestion(int questionNumber) {
        // 获取当前组别
        int groupNumber = testcontext.getInstance().getGroupNumber();
        
        // 根据组别和题目编号返回对应的图片名称数组
        if (groupNumber == 4) {
            // 第四组图片资源
            switch (questionNumber) {
                case 1: return new String[]{"gi1", "gi2", "gi3", "gi4"};            // 电视开着（正确：gi1）
                case 2: return new String[]{"gi5", "gi6", "gi7", "gi8"};            // 姐姐跳（正确：gi6）
                case 3: return new String[]{"gi9", "gi10", "gi11", "gi12"};         // 叔叔唱着歌（正确：gi9）
                case 4: return new String[]{"gi13", "gi14", "gi15", "gi16"};         // 宝宝没有不睡觉（正确：gi13）
                case 5: return new String[]{"gi17", "gi18", "gi19", "gi20"};         // 他不是不喜欢吃苹果（正确：gi17）
                case 6: return new String[]{"gi21", "gi22", "gi23", "gi24"};         // 小明不是不想去学校（正确：gi21）
                case 7: return new String[]{"gi25"};                                   // 除非洗手，否则不能吃饭
                case 8: return new String[]{"gi26"};                                   // 除非天气很冷，否则小明都去游泳
                case 9: return new String[]{"gi27"};                                   // 除非收拾好玩具，否则不能看电视
                case 10: return new String[]{"gr67"};                                   // 朵朵生日的故事（gr67图片）
                default: return null;
            }
        } else if (groupNumber == 3) {
            // 第三组图片资源
            switch (questionNumber) {
                case 1: return new String[]{"gh1", "gh2", "gh3", "gh4"};            // 弟弟被妈妈抱（正确：gh4）
                case 2: return new String[]{"gh5", "gh6", "gh7", "gh8"};            // 爸爸被小狗追（正确：gh7）
                case 3: return new String[]{"gh9", "gh10", "gh11", "gh12"};         // 猫咪被大象推（正确：gh10）
                case 4: return new String[]{"gh13", "gh14", "gh15", "gh16"};         // 哥哥比妹妹跑得快（正确：gh15）
                case 5: return new String[]{"gh17", "gh18", "gh19", "gh20"};         // 叔叔比阿姨胖（正确：gh17）
                case 6: return new String[]{"gh21", "gh22", "gh23", "gh24"};         // 红球比绿球大（正确：gh22）
                case 7: return new String[]{"gh25", "gh26", "gh27", "gh28"};         // 哪个是妹妹吃完了？（正确：gh28）
                case 8: return new String[]{"gh29", "gh30", "gh31", "gh32"};         // 花开了（正确：gh30）
                case 9: return new String[]{"gh33", "gh34", "gh35", "gh36"};         // 门关了（正确：gh33）
                case 10: return new String[]{"gh37", "gh38", "gh39", "gh40"};         // 因为下雨，所以打伞（正确：gh38）
                case 11: return new String[]{"gh41", "gh42", "gh43", "gh44"};         // 因为小明生病了，所以没上学（正确：gh42）
                case 12: return new String[]{"gh45", "gh46", "gh47", "gh48"};         // 因为天冷了，所以得穿棉袄（正确：gh45）
                case 13: return new String[]{"gh49", "gh50", "gh51", "gh52"};         // 哥哥想出去玩，但是下雨了（正确：gh49）
                case 14: return new String[]{"gh53", "gh54", "gh55", "gh56"};         // 小朋友想吃蛋糕，但是没有了（正确：gh53）
                case 15: return new String[]{"gh57", "gh58", "gh59", "gh60"};         // 他想踢足球，但是他腿受伤了（正确：gh57）
                case 16: return new String[]{"gh61", "gh62", "gh63", "gh64"};         // 先揉揉肚子，再挥挥手（正确顺序：gh61, gh62）
                case 17: return new String[]{"gh65", "gh66", "gh67", "gh68"};         // 先洗手，再吃饭（正确顺序：gh65, gh67）
                case 18: return new String[]{"gh69", "gh70", "gh71", "gh72"};         // 先刷牙，再睡觉（正确顺序：gh71, gh69）
                default: return null;
            }
        } else if (groupNumber == 2) {
            // 第二组图片资源
            switch (questionNumber) {
                case 1: return new String[]{"gr1", "gr2", "gr3", "gr4"};            // 大红苹果（正确：gr3）
                case 2: return new String[]{"gr5", "gr6", "gr7", "gr8"};            // 白色斑点狗（正确：gr6）
                case 3: return new String[]{"gr9", "gr10", "gr11", "gr12"};         // 小的圆形饼干（正确：gr12）
                case 4: return new String[]{"gr13", "gr14", "gr15", "gr16"};         // 给妈妈包包（正确：gr13）
                case 5: return new String[]{"gr17", "gr18", "gr19", "gr20"};         // 妹妹给小猫喂牛奶（正确：gr18）
                case 6: return new String[]{"gr21", "gr22", "gr23", "gr24"};         // 哥哥递给姐姐一束花（正确：gr21）
                case 7: return new String[]{"gr25"};                                   // 苹果是不是红的？（正确：触摸❌️）
                case 8: return new String[]{"gr26"};                                   // 姐姐戴没戴帽子？（正确：触摸❌️）
                case 9: return new String[]{"gr27"};                                   // 飞机是不是交通工具？（正确：触摸✅️）
                case 10: return new String[]{"gr28", "gr29", "gr30", "gr31", "gr32"}; // 去哪里看大象？（正确：gr29）
                case 11: return new String[]{"gr33", "gr34", "gr35", "gr36", "gr37"}; // 去哪里挖沙子？（正确：gr36）
                case 12: return new String[]{"gr38", "gr39", "gr40", "gr41", "gr42"}; // 小猫在哪里？（正确：gr42）
                case 13: return new String[]{"gr43", "gr44", "gr45", "gr46"};         // 他们都戴帽子（正确：gr43）
                case 14: return new String[]{"gr47", "gr48", "gr49", "gr50"};         // 小朋友都有苹果（正确：gr49）
                case 15: return new String[]{"gr51", "gr52", "gr53", "gr54"};         // 气球都飞走了（正确：gr52）
                case 16: return new String[]{"gr55", "gr56", "gr57", "gr58"};         // 哥哥推姐姐（正确：gr55）
                case 17: return new String[]{"gr59", "gr60", "gr61", "gr62"};         // 爷爷送小朋友礼物（正确：gr60）
                case 18: return new String[]{"gr63", "gr64", "gr65", "gr66"};         // 狗追猫（正确：gr66）
                default: return null;
            }
        } else {
            // 第一组图片资源
            switch (questionNumber) {
                case 1: return new String[]{"gm1", "gm2", "gm3", "gm4"};            // 小狗跑（正确：gm2）
                case 2: return new String[]{"gm5", "gm6", "gm7", "gm8"};            // 爷爷坐（正确：gm7）
                case 3: return new String[]{"gm9", "gm10", "gm11", "gm12"};         // 小鸟飞（正确：gm9）
                case 4: return new String[]{"gm13", "gm14", "gm15", "gm16"};         // 切香蕉（正确：gm15）
                case 5: return new String[]{"gm17", "gm18", "gm19", "gm20"};         // 吃苹果（正确：gm17）
                case 6: return new String[]{"gm21", "gm22", "gm23", "gm24"};         // 擦桌子（正确：gm22）
                case 7: return new String[]{"gm25", "gm26", "gm27", "gm28"};         // 哥哥搭积木（正确：gm25）
                case 8: return new String[]{"gm29", "gm30", "gm31", "gm32"};         // 爷爷看报纸（正确：gm31）
                case 9: return new String[]{"gm33", "gm34", "gm35", "gm36"};         // 兔子拔萝卜（正确：gm33）
                case 10: return new String[]{"gm37", "gm38", "gm39", "gm40"};         // 不能吃（正确：gm38）
                case 11: return new String[]{"gm41", "gm42", "gm43", "gm44"};         // 没有跑（正确：gm42）
                case 12: return new String[]{"gm45", "gm46", "gm47", "gm48"};         // 不会飞（正确：gm45）
                case 13: return new String[]{"gm49"};                                   // 这是苹果么？（正确：触摸❌️）
                case 14: return new String[]{"gm50"};                                   // 妹妹睡觉了么？（正确：触摸✅️）
                case 15: return new String[]{"gm51"};                                   // 天黑了吗？（正确：触摸❌️）
                case 16: return new String[]{"gm52", "gm53", "gm54", "gm55", "gm56"}; // 谁在画兔子？（正确：gm53）
                case 17: return new String[]{"gm57", "gm58", "gm59", "gm60", "gm61"}; // 爸爸在干什么？（正确：gm59）
                case 18: return new String[]{"gm62", "gm63", "gm64", "gm65"};         // 去哪里看医生？（正确：gm64）
                case 19: return new String[]{"gm66", "gm67", "gm68", "gm69"};         // 大苹果（正确：gm66）
                case 20: return new String[]{"gm70", "gm71", "gm72", "gm73"};         // 黄香蕉（正确：gm71）
                case 21: return new String[]{"gm74", "gm75", "gm76", "gm77"};         // 圆圆的饼干（正确：gm74）
                default: return null;
            }
        }
    }

    private void setupImageClickListeners(ImageView[] imageViews, int startIndex, int endIndex, int questionNumber) {
        // 获取当前组别
        int groupNumber = testcontext.getInstance().getGroupNumber();
        
        for(int i=startIndex;i<=endIndex;++i){
            final int index = i;
            imageViews[i].setOnClickListener(v -> {
                // 清除之前选中的红框
                for(int j=startIndex;j<=endIndex;++j){
                    imageViews[j].setBackgroundResource(R.drawable.cage3);
                }
                // 为新选中的图片设置红框背景
                imageViews[index].setBackgroundResource(R.drawable.cage2);
                
                // 根据题目类型和选择的图片设置答案
                String selectedAnswer = getAnswerForImageSelection(questionNumber, index - startIndex);
                
                // 处理第三组16-18题需要按两次的情况
                if (groupNumber == 3 && questionNumber >= 16 && questionNumber <= 18) {
                    if (!selectedAnswer.equals("TEMP")) {
                        // 两次点击完成，设置最终答案
                        answer = selectedAnswer;
                        // 设置结果：直接根据selectedAnswer判断是否正确
                        // 对于需要按两次的题目，正确答案是特定的序列
                        boolean isCorrect = false;
                        switch (questionNumber) {
                            case 16: // 先揉揉肚子，再挥挥手 - 先按gh61，再按gh62
                                isCorrect = selectedAnswer.equals("61,62");
                                break;
                            case 17: // 先洗手，再吃饭 - 先按gh65，再按gh67
                                isCorrect = selectedAnswer.equals("65,67");
                                break;
                            case 18: // 先刷牙，再睡觉 - 先按gh71，再按gh69
                                isCorrect = selectedAnswer.equals("71,69");
                                break;
                        }
                        result = isCorrect;
                        // 禁用所有图片的点击事件
                        for(int j=startIndex;j<=endIndex;++j){
                            imageViews[j].setEnabled(false);
                        }
                        // 停止计时器
                        if (handler != null && runnable != null) {
                            handler.removeCallbacks(runnable);
                        }
                        // 跳转到下一题
                        goToNextPage(questionNumber - 1);
                    }
                    // 第一次点击后不禁用，允许第二次点击
                } else {
                    // 普通题目，只能点击一次
                    answer = selectedAnswer;
                    // 设置结果：直接根据selectedAnswer判断是否正确
                    // 对于普通题目，正确答案是特定的选项
                    boolean isCorrect = false;
                    switch (groupNumber) {
                        case 1:
                            switch (questionNumber) {
                                case 1: isCorrect = selectedAnswer.equals("B"); break; // 小狗跑 - gm2正确 (索引1)
                                case 2: isCorrect = selectedAnswer.equals("C"); break; // 爷爷坐 - gm7正确 (索引2)
                                case 3: isCorrect = selectedAnswer.equals("A"); break; // 小鸟飞 - gm9正确 (索引0)
                                case 4: isCorrect = selectedAnswer.equals("C"); break; // 切香蕉 - gm15正确 (索引2)
                                case 5: isCorrect = selectedAnswer.equals("A"); break; // 吃苹果 - gm17正确 (索引0)
                                case 6: isCorrect = selectedAnswer.equals("B"); break; // 擦桌子 - gm22正确 (索引1)
                                case 7: isCorrect = selectedAnswer.equals("A"); break; // 哥哥搭积木 - gm25正确 (索引0)
                                case 8: isCorrect = selectedAnswer.equals("C"); break; // 爷爷看报纸 - gm31正确 (索引2)
                                case 9: isCorrect = selectedAnswer.equals("A"); break; // 兔子拔萝卜 - gm33正确 (索引0)
                                case 10: isCorrect = selectedAnswer.equals("B"); break; // 不能吃 - gm38正确 (索引1)
                                case 11: isCorrect = selectedAnswer.equals("B"); break; // 没有跑 - gm42正确 (索引1)
                                case 12: isCorrect = selectedAnswer.equals("A"); break; // 不会飞 - gm45正确 (索引0)
                                case 16: isCorrect = selectedAnswer.equals("B"); break; // 谁在画兔子？ - gm53正确 (索引1)
                                case 17: isCorrect = selectedAnswer.equals("C"); break; // 爸爸在干什么？ - gm59正确 (索引2)
                                case 18: isCorrect = selectedAnswer.equals("C"); break; // 去哪里看医生？ - gm64正确 (索引2)
                                case 19: isCorrect = selectedAnswer.equals("A"); break; // 大苹果 - gm66正确 (索引0)
                                case 20: isCorrect = selectedAnswer.equals("B"); break; // 黄香蕉 - gm71正确 (索引1)
                                case 21: isCorrect = selectedAnswer.equals("A"); break; // 圆圆的饼干 - gm74正确 (索引0)
                            }
                            break;
                        case 2:
                            switch (questionNumber) {
                                case 1: isCorrect = selectedAnswer.equals("C"); break; // 大红苹果 - gr3正确 (索引2)
                                case 2: isCorrect = selectedAnswer.equals("B"); break; // 白色斑点狗 - gr6正确 (索引1)
                                case 3: isCorrect = selectedAnswer.equals("D"); break; // 小的圆形饼干 - gr12正确 (索引3)
                                case 4: isCorrect = selectedAnswer.equals("A"); break; // 给妈妈包包 - gr13正确 (索引0)
                                case 5: isCorrect = selectedAnswer.equals("B"); break; // 妹妹给小猫喂牛奶 - gr18正确 (索引1)
                                case 6: isCorrect = selectedAnswer.equals("A"); break; // 哥哥递给姐姐一束花 - gr21正确 (索引0)
                                case 10: isCorrect = selectedAnswer.equals("B"); break; // 去哪里看大象？ - gr29正确 (索引1)
                                case 11: isCorrect = selectedAnswer.equals("D"); break; // 去哪里挖沙子？ - gr36正确 (索引3)
                                case 12: isCorrect = selectedAnswer.equals("E"); break; // 小猫在哪里？ - gr42正确 (索引4)
                                case 13: isCorrect = selectedAnswer.equals("A"); break; // 他们都戴帽子 - gr43正确 (索引0)
                                case 14: isCorrect = selectedAnswer.equals("C"); break; // 小朋友都有苹果 - gr49正确 (索引2)
                                case 15: isCorrect = selectedAnswer.equals("B"); break; // 气球都飞走了 - gr52正确 (索引1)
                                case 16: isCorrect = selectedAnswer.equals("A"); break; // 哥哥推姐姐 - gr55正确 (索引0)
                                case 17: isCorrect = selectedAnswer.equals("B"); break; // 爷爷送小朋友礼物 - gr60正确 (索引1)
                                case 18: isCorrect = selectedAnswer.equals("D"); break; // 狗追猫 - gr66正确 (索引3)
                            }
                            break;
                        case 3:
                            switch (questionNumber) {
                                case 1: isCorrect = selectedAnswer.equals("D"); break; // 弟弟被妈妈抱 - gh4正确 (索引3)
                                case 2: isCorrect = selectedAnswer.equals("C"); break; // 爸爸被小狗追 - gh7正确 (索引2)
                                case 3: isCorrect = selectedAnswer.equals("B"); break; // 猫咪被大象推 - gh10正确 (索引1)
                                case 4: isCorrect = selectedAnswer.equals("C"); break; // 哥哥比妹妹跑得快 - gh15正确 (索引2)
                                case 5: isCorrect = selectedAnswer.equals("A"); break; // 叔叔比阿姨胖 - gh17正确 (索引0)
                                case 6: isCorrect = selectedAnswer.equals("B"); break; // 红球比绿球大 - gh22正确 (索引1)
                                case 7: isCorrect = selectedAnswer.equals("D"); break; // 哪个是妹妹吃完了？ - gh28正确 (索引3)
                                case 8: isCorrect = selectedAnswer.equals("B"); break; // 花开了 - gh30正确 (索引1)
                                case 9: isCorrect = selectedAnswer.equals("A"); break; // 门关了 - gh33正确 (索引0)
                                case 10: isCorrect = selectedAnswer.equals("B"); break; // 因为下雨，所以打伞 - gh38正确 (索引1)
                                case 11: isCorrect = selectedAnswer.equals("B"); break; // 因为小明生病了，所以没上学 - gh42正确 (索引1)
                                case 12: isCorrect = selectedAnswer.equals("A"); break; // 因为天冷了，所以得穿棉袄 - gh45正确 (索引0)
                                case 13: isCorrect = selectedAnswer.equals("A"); break; // 哥哥想出去玩，但是下雨了 - gh49正确 (索引0)
                                case 14: isCorrect = selectedAnswer.equals("A"); break; // 小朋友想吃蛋糕，但是没有了 - gh53正确 (索引0)
                                case 15: isCorrect = selectedAnswer.equals("A"); break; // 他想踢足球，但是他腿受伤了 - gh57正确 (索引0)
                            }
                            break;
                        case 4:
                            switch (questionNumber) {
                                case 1: isCorrect = selectedAnswer.equals("A"); break; // 电视开着 - gi1正确 (索引0)
                                case 2: isCorrect = selectedAnswer.equals("B"); break; // 姐姐跳 - gi6正确 (索引3)
                                case 3: isCorrect = selectedAnswer.equals("A"); break; // 叔叔唱着歌 - gi9正确 (索引0)
                                case 4: isCorrect = selectedAnswer.equals("A"); break; // 宝宝没有不睡觉 - gi13正确 (索引0)
                                case 5: isCorrect = selectedAnswer.equals("A"); break; // 他不是不喜欢吃苹果 - gi17正确 (索引0)
                                case 6: isCorrect = selectedAnswer.equals("A"); break; // 小明不是不想去学校 - gi21正确 (索引0)
                            }
                            break;
                    }
                    result = isCorrect;
                    // 禁用所有图片的点击事件
                    for(int j=startIndex;j<=endIndex;++j){
                        imageViews[j].setEnabled(false);
                    }
                    // 停止计时器
                    if (handler != null && runnable != null) {
                        handler.removeCallbacks(runnable);
                    }
                    // 跳转到下一题
                    goToNextPage(questionNumber - 1);
                }
            });
        }
    }

    private void setupYesNoClickListeners(Button yesButton, Button noButton, int questionNumber) {
        if(yesButton == null || noButton == null) return;
        
        // 获取当前组别
        int groupNumber = testcontext.getInstance().getGroupNumber();
        
        yesButton.setOnClickListener(v -> {
            answer = "RIGHT";
            // 设置结果：按正确按钮显示正确
            result = true;
            
            // 处理第四组第十题的六个小问
            if (groupNumber == 4 && questionNumber == 10) {
                // 为每个小问设置独立的结果和答案
                for (int i = 0; i < subQuestionResults.length; i++) {
                    // 这里可以根据实际情况为每个小问设置不同的结果
                    // 暂时默认所有小问都设置为正确
                    subQuestionResults[i] = true;
                    subQuestionAnswers[i] = "RIGHT";
                }
            }
            
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            // 停止计时器
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            // 跳转到下一题
            goToNextPage(questionNumber - 1);
        });
        
        noButton.setOnClickListener(v -> {
            answer = "WRONG";
            // 设置结果：按错误按钮显示错误
            result = false;
            
            // 处理第四组第十题的六个小问
            if (groupNumber == 4 && questionNumber == 10) {
                // 为每个小问设置独立的结果和答案
                for (int i = 0; i < subQuestionResults.length; i++) {
                    // 这里可以根据实际情况为每个小问设置不同的结果
                    // 暂时默认所有小问都设置为错误
                    subQuestionResults[i] = false;
                    subQuestionAnswers[i] = "WRONG";
                }
            }
            
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            // 停止计时器
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            // 跳转到下一题
            goToNextPage(questionNumber - 1);
        });
    }

    // 用于存储第三组16-18题的点击顺序
    private String[] clickSequence = new String[2];
    private int clickCount = 0;

    private String getAnswerForImageSelection(int questionNumber, int selectedIndex) {
        // 获取当前组别
        int groupNumber = testcontext.getInstance().getGroupNumber();
        
        // 根据组别、题目编号和选择的图片索引返回对应的答案
        if (groupNumber == 4) {
            // 第四组答案
            switch (questionNumber) {
                case 1: // 电视开着 - gi1正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 2: // 姐姐跳 - gi6正确 (索引3)
                    return selectedIndex == 3 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 3: // 叔叔唱着歌 - gi9正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 4: // 宝宝没有不睡觉 - gi13正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 5: // 他不是不喜欢吃苹果 - gi17正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 6: // 小明不是不想去学校 - gi21正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 7: // 除非洗手，否则不能吃饭 - 小男孩不能吃饭
                    return "WRONG";
                case 8: // 除非天气很冷，否则小明都去游泳 - 小明去游泳
                    return "RIGHT";
                case 9: // 除非收拾好玩具，否则不能看电视 - 不可以看电视
                    return "WRONG";
                case 10: // 朵朵生日的故事 - 特殊处理
                    return "";
                default:
                    return String.valueOf((char)('A' + selectedIndex));
            }
        } else if (groupNumber == 3) {
            // 第三组答案
            if (questionNumber >= 16 && questionNumber <= 18) {
                // 处理需要按两次的题目
                return handleTwoClickQuestion(questionNumber, selectedIndex);
            } else {
                // 普通题目
                switch (questionNumber) {
                    case 1: // 弟弟被妈妈抱 - gh4正确 (索引3)
                        return selectedIndex == 3 ? "D" : String.valueOf((char)('A' + selectedIndex));
                    case 2: // 爸爸被小狗追 - gh7正确 (索引2)
                        return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                    case 3: // 猫咪被大象推 - gh10正确 (索引1)
                        return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                    case 4: // 哥哥比妹妹跑得快 - gh15正确 (索引2)
                        return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                    case 5: // 叔叔比阿姨胖 - gh17正确 (索引0)
                        return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                    case 6: // 红球比绿球大 - gh22正确 (索引1)
                        return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                    case 7: // 哪个是妹妹吃完了？ - gh28正确 (索引3)
                        return selectedIndex == 3 ? "D" : String.valueOf((char)('A' + selectedIndex));
                    case 8: // 花开了 - gh30正确 (索引1)
                        return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                    case 9: // 门关了 - gh33正确 (索引0)
                        return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                    case 10: // 因为下雨，所以打伞 - gh38正确 (索引1)
                        return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                    case 11: // 因为小明生病了，所以没上学 - gh42正确 (索引1)
                        return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                    case 12: // 因为天冷了，所以得穿棉袄 - gh45正确 (索引0)
                        return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                    case 13: // 哥哥想出去玩，但是下雨了 - gh49正确 (索引0)
                        return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                    case 14: // 小朋友想吃蛋糕，但是没有了 - gh53正确 (索引0)
                        return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                    case 15: // 他想踢足球，但是他腿受伤了 - gh57正确 (索引0)
                        return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                    default:
                        return String.valueOf((char)('A' + selectedIndex));
                }
            }
        } else if (groupNumber == 2) {
            // 第二组答案
            switch (questionNumber) {
                case 1: // 大红苹果 - gr3正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 2: // 白色斑点狗 - gr6正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 3: // 小的圆形饼干 - gr12正确 (索引3)
                    return selectedIndex == 3 ? "D" : String.valueOf((char)('A' + selectedIndex));
                case 4: // 给妈妈包包 - gr13正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 5: // 妹妹给小猫喂牛奶 - gr18正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 6: // 哥哥递给姐姐一束花 - gr21正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 7: // 苹果是不是红的？ - 触摸❌️则为正确的
                    return "WRONG";
                case 8: // 姐姐戴没戴帽子？ - 触摸❌️则为正确的
                    return "WRONG";
                case 9: // 飞机是不是交通工具？ - 触摸✅️则为正确
                    return "RIGHT";
                case 10: // 去哪里看大象？ - gr29正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 11: // 去哪里挖沙子？ - gr36正确 (索引3)
                    return selectedIndex == 3 ? "D" : String.valueOf((char)('A' + selectedIndex));
                case 12: // 小猫在哪里？ - gr42正确 (索引4)
                    return selectedIndex == 4 ? "E" : String.valueOf((char)('A' + selectedIndex));
                case 13: // 他们都戴帽子 - gr43正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 14: // 小朋友都有苹果 - gr49正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 15: // 气球都飞走了 - gr52正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 16: // 哥哥推姐姐 - gr55正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 17: // 爷爷送小朋友礼物 - gr60正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 18: // 狗追猫 - gr66正确 (索引3)
                    return selectedIndex == 3 ? "D" : String.valueOf((char)('A' + selectedIndex));
                default:
                    return String.valueOf((char)('A' + selectedIndex));
            }
        } else {
            // 第一组答案
            switch (questionNumber) {
                case 1: // 小狗跑 - gm2正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 2: // 爷爷坐 - gm7正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 3: // 小鸟飞 - gm9正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 4: // 切香蕉 - gm15正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 5: // 吃苹果 - gm17正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 6: // 擦桌子 - gm22正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 7: // 哥哥搭积木 - gm25正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 8: // 爷爷看报纸 - gm31正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 9: // 兔子拔萝卜 - gm33正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 10: // 不能吃 - gm38正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 11: // 没有跑 - gm42正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 12: // 不会飞 - gm45正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 13: // 这是苹果么？ - 触摸❌️是正确
                    return "WRONG";
                case 14: // 妹妹睡觉了么？ - 触摸✅️是正确
                    return "RIGHT";
                case 15: // 天黑了吗？ - 触摸❌️是正确
                    return "WRONG";
                case 16: // 谁在画兔子？ - gm53正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 17: // 爸爸在干什么？ - gm59正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 18: // 去哪里看医生？ - gm64正确 (索引2)
                    return selectedIndex == 2 ? "C" : String.valueOf((char)('A' + selectedIndex));
                case 19: // 大苹果 - gm66正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                case 20: // 黄香蕉 - gm71正确 (索引1)
                    return selectedIndex == 1 ? "B" : String.valueOf((char)('A' + selectedIndex));
                case 21: // 圆圆的饼干 - gm74正确 (索引0)
                    return selectedIndex == 0 ? "A" : String.valueOf((char)('A' + selectedIndex));
                default:
                    return String.valueOf((char)('A' + selectedIndex));
            }
        }
    }

    private String handleTwoClickQuestion(int questionNumber, int selectedIndex) {
        // 获取当前点击的图片名称
        String[] imageNames = getImageNamesForQuestion(questionNumber);
        String clickedImage = imageNames[selectedIndex];
        
        // 存储点击顺序
        if (clickCount < 2) {
            clickSequence[clickCount] = clickedImage;
            clickCount++;
            
            // 第一次点击后返回临时值
            if (clickCount == 1) {
                return "TEMP";
            }
        }
        
        // 两次点击完成，检查顺序
        if (clickCount == 2) {
            String sequence = clickSequence[0] + "," + clickSequence[1];
            
            // 重置点击计数
            clickCount = 0;
            
            // 检查是否符合正确顺序
            switch (questionNumber) {
                case 16: // 先揉揉肚子，再挥挥手 - 先按gh61，再按gh62
                    return sequence.equals("gh61,gh62") ? "61,62" : sequence;
                case 17: // 先洗手，再吃饭 - 先按gh65，再按gh67
                    return sequence.equals("gh65,gh67") ? "65,67" : sequence;
                case 18: // 先刷牙，再睡觉 - 先按gh71，再按gh69
                    return sequence.equals("gh71,gh69") ? "71,69" : sequence;
                default:
                    return sequence;
            }
        }
        
        return "";
    }


    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        // 处理第四组第十题的六个小问题
        int groupNumber = testcontext.getInstance().getGroupNumber();
        if (groupNumber == 4 && num == 10) {
            // 六个小问题的题目内容
            String[] subQuestions = {
                "1. 朵朵的生日是哪一天？",
                "2. 妈妈给朵朵买了什么礼物？",
                "3. 朵朵邀请了谁来参加生日派对？",
                "4. 谁给朵朵唱了生日歌？",
                "5. 朵朵许了什么愿望？",
                "6. 最后大家一起做了什么？"
            };
            
            // 为每个小问题创建单独的记录
            for (int i = 0; i < subQuestions.length; i++) {
                JSONObject object = new JSONObject();
                object.put("question", subQuestions[i]);
                object.put("num", 10 + (i + 1) * 0.1); // 使用小数来区分小问题，如10.1, 10.2等
                object.put("right_option", "RIGHT");
                
                if(time!=null){
                    // 为每个小问设置独立的结果
                    boolean subResult = false;
                    String subAnswer = "";
                    if (i < subQuestionResults.length && subQuestionResults[i] != null) {
                        subResult = subQuestionResults[i];
                    }
                    if (i < subQuestionAnswers.length && subQuestionAnswers[i] != null) {
                        subAnswer = subQuestionAnswers[i];
                    }
                    object.put("result", subResult);
                    object.put("answer", subAnswer.isEmpty() ? (subResult ? "RIGHT" : "WRONG") : subAnswer);
                    object.put("score", score);
                    object.put("time", time);
                }
                else {
                    object.put("result", JSONObject.NULL);
                    object.put("answer", JSONObject.NULL);
                    object.put("score", JSONObject.NULL);
                    object.put("time", JSONObject.NULL);
                }
                
                // 使用组特定的JSONArray键
                String rgKey = "RG" + groupNumber;
                // 确保JSONArray存在
                if (!evaluations.has(rgKey)) {
                    evaluations.put(rgKey, new JSONArray());
                }
                JSONArray rgArray = evaluations.getJSONArray(rgKey);
                
                // 检查是否已经存在相同题号的记录
                boolean found = false;
                for (int j = 0; j < rgArray.length(); j++) {
                    JSONObject item = rgArray.getJSONObject(j);
                    if (item.getDouble("num") == 10 + (i + 1) * 0.1) {
                        // 更新现有记录
                        rgArray.put(j, object);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // 添加新记录
                    rgArray.put(object);
                }
            }
        } else {
            // 普通题目的处理
            JSONObject object = new JSONObject();
            object.put("question", question);
            object.put("num", num);
            object.put("right_option", right_option);
            if(time!=null){
                object.put("result", result);
                object.put("answer", answer);
                object.put("score", score);
                object.put("time", time);
            }
            else {
                object.put("result", JSONObject.NULL);
                object.put("answer", JSONObject.NULL);
                object.put("score", JSONObject.NULL);
                object.put("time", JSONObject.NULL);
            }
            // 使用组特定的JSONArray键
            String rgKey = "RG" + groupNumber;
            // 确保JSONArray存在
            if (!evaluations.has(rgKey)) {
                evaluations.put(rgKey, new JSONArray());
            }
            JSONArray rgArray = evaluations.getJSONArray(rgKey);
            // 检查是否已经存在相同题号的记录
            boolean found = false;
            for (int i = 0; i < rgArray.length(); i++) {
                JSONObject item = rgArray.getJSONObject(i);
                if (item.getInt("num") == num) {
                    // 更新现有记录
                    rgArray.put(i, object);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // 添加新记录
                rgArray.put(object);
            }
        }
    }
    private void goToNextPage(int position) {
        // 停止计时器
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        

        // 获取当前Activity的timer TextView
        TextView timer = null;
        TextView counter = null;
        try {
            Activity context = testcontext.getInstance().getContext();
            if (context != null && context instanceof com.example.CCLEvaluation.testactivity) {
                com.example.CCLEvaluation.testactivity activity = (com.example.CCLEvaluation.testactivity) context;
                timer = activity.findViewById(R.id.timer);
                counter = activity.findViewById(R.id.counter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 记录答题时长
        if (timer != null) {
            time = timer.getText().toString();
        }

        // 只有当用户完成题目时才增加计数
        testcontext.getInstance().incrementCount();
        // 实时更新进度条
        if (counter != null) {
            counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());
        }

        // 保存当前题目数据
        try {
            // 使用testcontext中的上下文获取fName
            Activity context = utils.testcontext.getInstance().getContext();
            if (context != null) {
                String fName = context.getIntent().getStringExtra("fName");
                if (fName != null) {
                    JSONObject data = utils.dataManager.getInstance().loadData(fName);
                    JSONObject evaluations = data.getJSONObject("evaluations");
                    toJson(evaluations);
                    utils.dataManager.getInstance().saveData(fName, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
        testcontext.getInstance().getViewPager().setPagingEnabled(true);
    }
    
    private void nextPage(int position,int count, int lengths) {
        int nP = position + 1;
        if (count >= lengths) {
            if (listener != null) {
                listener.onAllQuestionComplete();
            }
            testcontext.getInstance().getContext().finish();
            return; // 防止后续代码执行
        }
        if(nP >= lengths){
            testcontext.getInstance().getViewPager().setCurrentItem(testcontext.getInstance().searchOne(), true);
        }
        else {
            testcontext.getInstance().getViewPager().setCurrentItem(nP, true);
        }
    }
}
