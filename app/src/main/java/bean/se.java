package bean;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
 * 句法表达（SE）
 */
public class se extends evaluation {
    private String question;
    private String right_option;
    private String answer;
    private Integer score;
    private String time;
    private bean.audio audio;
    private Boolean result;
    private final String colum1 = "测试阶段";
    private final String colum2 = "题号";
    private final String colum3 = "例句";
    private final String colum4 = "正确选项";
    private final String colum5 = "被选选项";
    private final String colum6 = "测试结果";
    private final String colum7 = "答题时间";
    private final String colum8 = "对应测试语法点";
    
    // 新增字段
    private String grammarPoint;
    private String group;
    private String questionNum;

    public se(int num, String question, String right_option, String answer, Boolean result, Integer score, bean.audio audio, String time) {
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
    
    // 新构造函数，支持分组、题目编号、正确选项、被选选项、答题时间和语法点
    public se(int num, String group, String questionNum, String question, String right_option, String answer, String result, Integer score, bean.audio audio, String time, String grammarPoint) {
        super(num);
        this.num = num;
        this.question = question;
        this.right_option = right_option;
        this.answer = answer;
        this.result = result != null && !result.isEmpty() ? Boolean.valueOf(result.equals("正确")) : null;
        this.score = score;
        this.audio = audio;
        this.time = time;
        this.grammarPoint = grammarPoint;
        this.group = group;
        this.questionNum = questionNum;
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
       // 确保有足够的视图来显示所有列（包括答题时间和录音）
       int requiredViews = 10; // 测试阶段、题号、例句、正确选项、被选选项、测试结果、答题时间、录音、对应测试语法点
       if (views.length < requiredViews) {
           // 如果视图不够，只显示可用的
           requiredViews = views.length;
       }
       
       for(int i=0;i<requiredViews;i++)
           views[i].setVisibility(View.VISIBLE);
       
       if(position==0){
           // 表头
           if (requiredViews > 0) ((TextView)views[0]).setText("测试阶段");
           if (requiredViews > 1) ((TextView)views[1]).setText("题  号");
           if (requiredViews > 2) ((TextView)views[2]).setText("例  句");
           if (requiredViews > 3) ((TextView)views[3]).setText("正确选项");
           if (requiredViews > 4) ((TextView)views[4]).setText("被选选项");
           if (requiredViews > 5) ((TextView)views[5]).setText("测试结果");
           if (requiredViews > 6) ((TextView)views[6]).setText("答题时间");
           if (requiredViews > 7) ((TextView)views[7]).setText("录音");
           if (requiredViews > 8) ((TextView)views[8]).setText("对应测试语法点");
       }
       else if (num == -3) {
           // 组标题
           if (requiredViews > 0) {
               ((TextView)views[0]).setText(question); // question字段存储组标题
               ((TextView)views[0]).setBackgroundColor(0xFFE0E0E0);
               // 合并单元格
               if (views[0].getLayoutParams() instanceof android.widget.TableRow.LayoutParams) {
                   android.widget.TableRow.LayoutParams params = (android.widget.TableRow.LayoutParams) views[0].getLayoutParams();
                   params.span = requiredViews;
                   views[0].setLayoutParams(params);
               }
               // 隐藏其他单元格
               for (int i = 1; i < requiredViews; i++) {
                   views[i].setVisibility(View.GONE);
               }
           }
       }
       else {
           // 普通题目行
           if (requiredViews > 0) ((TextView)views[0]).setText(group);
           if (requiredViews > 1) ((TextView)views[1]).setText(questionNum);
           if (requiredViews > 2) {
               ((TextView)views[2]).setTextSize(20);
               if(question.length()>8){
                   ((TextView)views[2]).setTextSize(15);
               }
               ((TextView)views[2]).setText(question);
           }
           if (requiredViews > 3) ((TextView)views[3]).setText(right_option);
           if (requiredViews > 4 && answer!=null) ((TextView)views[4]).setText(answer);
           if (requiredViews > 5) {
               String resultText = result != null ? (result ? "正确" : "错误") : "";
               ((TextView)views[5]).setText(resultText);
           }
           if (requiredViews > 6) {
               if(time!=null){
                   ((TextView)views[6]).setText(time);
               } else {
                   ((TextView)views[6]).setText("");
               }
           }
           if (requiredViews > 7) {
               if(audio != null && audio.getPath() != null) {
                   // 添加录音播放按钮或图标
                   ((TextView)views[7]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                   ((TextView)views[7]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                   ((TextView)views[7]).setTextSize(20); // 调整图标大小
                   // 将播放按钮添加到AudioPlayer的audioIcons列表中
                   final int audioPosition = AudioPlayer.getInstance().getIconCount();
                   AudioPlayer.getInstance().addIcon((TextView)views[7]);
                   views[7].setOnClickListener(v -> {
                       // 播放录音
                       if (audio.getPath() != null) {
                           // 使用录音图标的实际位置
                           AudioPlayer.getInstance().play(audio.getPath(), audioPosition);
                       }
                   });
               } else {
                   ((TextView)views[7]).setText("");
               }
           }
           if (requiredViews > 8) ((TextView)views[8]).setText(grammarPoint);
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

       // 检查是否为示例题目
       int groupNumber = testcontext.getInstance().getGroupNumber();
       boolean isExample = isExampleQuestion(position, groupNumber);
       if (isExample) {
           numberTextView.setText("示例");
       } else {
           numberTextView.setText("第"+TabString[position]+"题");
       }
       hintTextView.setText(Hint[position]);
       counter.setText(testcontext.getInstance().getCount()+"/"+ testcontext.getInstance().getLengths());

       // 根据题目编号确定题目类型和加载对应的图片
       int questionNumber = position + 1;
       boolean isAnswered = time != null;

       if(isAnswered){
           startButton.setEnabled(false);
           // 显示已答题目的状态
           showQuestionInterface(view, questionNumber, isAnswered);
           // 显示已记录的时间
           if(time != null) {
               timer.setText(time);
           } else {
               timer.setText("00:00");
           }
       } else {
           // 未回答的题目，重置计时器为0
           timer.setText("00:00");
       }

       // 重置计时器为0
       timer.setText("00:00");
       
       AudioRecorder.OnRefreshUIThreadListener listener = time -> {
           this.time = time;
           timer.setText(time);
       };
       startButton.setOnClickListener(v -> {
           try {
               testcontext.getInstance().getViewPager().setPagingEnabled(false);
               startButton.setEnabled(false);
               result = false;
               answer = "";
               score = -1;
               
               // 重置计时器为0
               timer.setText("00:00");
               
               AudioRecorder.getInstance().setOnRefreshUIThreadListener(listener);
               AudioRecorder.getInstance().startRecorder();
               audio = new audio(AudioRecorder.getInstance().getOutputFilePath());

               // 根据题目类型显示对应的界面
               showQuestionInterface(view, questionNumber, isAnswered);

           } catch (IOException e) {
               Toast.makeText(testcontext.getInstance().getContext(), "录制失败！", Toast.LENGTH_SHORT).show();
               throw new RuntimeException(e);
           }
       });
       nextButton.setOnClickListener(v -> {
           if(audio!=null){
               // 禁用所有交互控件
               for(int i=0;i<5;++i){
                   if(imageViews[i] != null) imageViews[i].setEnabled(false);
               }
               if(yesButton != null) yesButton.setEnabled(false);
               if(noButton != null) noButton.setEnabled(false);

               if(result != null && answer.equals(right_option)){
                   AudioRecorder.getInstance().stopRecorder();
                   result = new Boolean(true);
               }else{
                   AudioRecorder.getInstance().stopRecorder();
                   result = new Boolean(false);
               }

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

   private boolean isExampleQuestion(int questionNumber, int groupNumber) {
       // 判断当前题目是否为示例题目
       if (groupNumber == 1) {
           // 第一组示例题目位置
           return questionNumber == 0 || questionNumber == 4 || questionNumber == 10;
       } else if (groupNumber == 2) {
           // 第二组示例题目位置
           return questionNumber == 3 || questionNumber == 7 || questionNumber == 11;
       } else if (groupNumber == 4) {
           // 第四组示例题目位置
           return questionNumber == 3 || questionNumber == 7 || questionNumber == 11 || questionNumber == 16;
       }
       return false;
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

       // 获取当前组别
       int groupNumber = testcontext.getInstance().getGroupNumber();

       // 获取当前题目的图片名称数组
       String[] imageNames = getImageNamesForQuestion(questionNumber, groupNumber);
       if (imageNames != null && imageNames.length > 0) {
           // 检查是否是第四组最后一题（第20题）
           boolean isFourthGroupLastQuestion = (groupNumber == 4 && questionNumber == 20);
           
           if (isFourthGroupLastQuestion) {
            // 第四组最后一题：逆序旋转180度一起展示四张图片
            if (imageNames.length > 0) {
                // 反转图片顺序
                String[] reversedImageNames = new String[imageNames.length];
                for (int i = 0; i < imageNames.length; i++) {
                    reversedImageNames[i] = imageNames[imageNames.length - 1 - i];
                }
                
                // 一起展示所有图片，调小尺寸以确保全部显示
                int maxHeight = 120; // 调小图片高度
                
                // 确保显示所有图片，包括第二行的两张图片
                for(int i=0;i<reversedImageNames.length && i<4;++i){
                    int resId = testcontext.getInstance().getContext().getResources()
                            .getIdentifier(reversedImageNames[i], "drawable", testcontext.getInstance().getContext().getPackageName());
                    if(resId > 0) {
                        // 从imageView1开始使用，因为imageView0是单张图片模式
                        imageViews[i+1].setVisibility(View.VISIBLE);
                        imageViews[i+1].setImageResource(resId);
                        imageViews[i+1].setScaleType(ImageView.ScaleType.FIT_CENTER);
                        // 旋转图片180度
                        imageViews[i+1].setRotation(180);
                        // 设置较小的高度以确保全部显示
                        imageViews[i+1].setMaxHeight(maxHeight);
                        // 设置固定的布局参数以确保图片正确排列
                        imageViews[i+1].requestLayout();
                        if(!isAnswered) imageViews[i+1].setEnabled(true);
                    }
                }
            }
        } else {
               // 其他题目：保持原有布局逻辑
               int imageCount = imageNames.length;
               if (imageCount == 1) {
                   // 单张图片模式
                   int resId = testcontext.getInstance().getContext().getResources()
                           .getIdentifier(imageNames[0], "drawable", testcontext.getInstance().getContext().getPackageName());
                   if(resId > 0) {
                       imageViews[0].setVisibility(View.VISIBLE);
                       imageViews[0].setImageResource(resId);
                       if(!isAnswered) imageViews[0].setEnabled(true);
                   }
               } else if (imageCount >= 2) {
                   // 多张图片模式
                   // 根据图片数量设置合适的最大高度
                   int maxHeight;
                   if (imageCount == 2) {
                       maxHeight = 200; // 两张图片可以设置较大的高度
                   } else {
                       maxHeight = 150; // 四张图片设置较小的高度以确保全部显示
                   }
                   
                   // 确保显示所有图片，包括第二行的两张图片
                   for(int i=0;i<imageCount && i<4;++i){
                       int resId = testcontext.getInstance().getContext().getResources()
                               .getIdentifier(imageNames[i], "drawable", testcontext.getInstance().getContext().getPackageName());
                       if(resId > 0) {
                           // 从imageView1开始使用，因为imageView0是单张图片模式
                           imageViews[i+1].setVisibility(View.VISIBLE);
                           imageViews[i+1].setImageResource(resId);
                           imageViews[i+1].setScaleType(ImageView.ScaleType.FIT_CENTER);
                           // 设置合适的最大高度
                           imageViews[i+1].setMaxHeight(maxHeight);
                           // 设置固定的布局参数以确保图片正确排列
                           imageViews[i+1].requestLayout();
                           if(!isAnswered) imageViews[i+1].setEnabled(true);
                       }
                   }
               }
           }
       }

       // 检查是否为示例题目
       boolean isExample = isExampleQuestion(questionNumber - 1, groupNumber);

       // 显示判断对错按钮（示例题目不显示）
       if(yesButton != null && noButton != null) {
           if (!isExample) {
               yesButton.setVisibility(View.VISIBLE);
               noButton.setVisibility(View.VISIBLE);
               if(!isAnswered) {
                   yesButton.setEnabled(true);
                   noButton.setEnabled(true);
               }
           } else {
               // 示例题目隐藏判断按钮
               yesButton.setVisibility(View.GONE);
               noButton.setVisibility(View.GONE);
           }
       }

       // 设置是/否按钮点击监听器（示例题目不设置）
       if (!isExample) {
           setupYesNoClickListeners(yesButton, noButton, questionNumber);
       }
       
       // 显示/隐藏判断对错标签容器
       View judgmentContainer = view.findViewById(R.id.ll_judgment);
       if (judgmentContainer != null) {
           // 句法表达模块不显示判断对错标签容器
           judgmentContainer.setVisibility(View.GONE);
       }
   }

   private String[] getImageNamesForQuestion(int questionNumber, int groupNumber) {
       // 根据组别和题目编号返回对应的图片名称数组
       if (groupNumber == 1) {
           // 第一组图片资源
           switch (questionNumber) {
               case 1: return new String[]{"hj1"};            // 示例：这个是弟弟睡觉
               case 2: return new String[]{"hj2"};            // 第一题
               case 3: return new String[]{"hj3"};            // 第二题
               case 4: return new String[]{"hj4"};            // 第三题
               case 5: return new String[]{"hj5"};            // 示例：这个是拍球
               case 6: return new String[]{"hj6"};            // 第四题
               case 7: return new String[]{"hj7"};            // 第五题
               case 8: return new String[]{"hj8"};            // 第六题
               case 9: return new String[]{"hj9", "hj10"};     // 第七题
               case 10: return new String[]{"hj11", "hj12"};   // 第八题
               case 11: return new String[]{"hj13", "hj14"};   // 第九题
               case 12: return new String[]{"hj15"};           // 示例：这个是妈妈吃苹果
               case 13: return new String[]{"hj16"};           // 第十题
               case 14: return new String[]{"hj17"};           // 第十一题
               case 15: return new String[]{"hj18"};           // 第十二题
               case 16: return new String[]{"hj19", "hj20"};   // 第十三题
               case 17: return new String[]{"hj21", "hj22"};   // 第十四题
               case 18: return new String[]{"hj23", "hj24"};   // 第十五题
               default: return null;
           }
       } else if (groupNumber == 2) {
           // 第二组图片资源
           switch (questionNumber) {
               case 1: return new String[]{"hk1", "hk2"};     // 第一题
               case 2: return new String[]{"hk3"};            // 第二题
               case 3: return new String[]{"hk4"};            // 第三题
               case 4: return new String[]{"hk5"};            // 示例：小朋友想知道大象会不会跳舞
               case 5: return new String[]{"hk6"};            // 第四题
               case 6: return new String[]{"hk7"};            // 第五题
               case 7: return new String[]{"hk8"};            // 第六题
               case 8: return new String[]{"hk9"};            // 示例：小朋友不认识这个动物
               case 9: return new String[]{"hk10"};           // 第七题
               case 10: return new String[]{"hk11"};           // 第八题
               case 11: return new String[]{"hk12"};           // 第九题
               case 12: return new String[]{"hk13"};           // 示例：这幅图是猫咪在桌子上
               case 13: return new String[]{"hk14"};           // 第十题
               case 14: return new String[]{"hk15"};           // 第十一题
               case 15: return new String[]{"hk16"};           // 第十二题
               case 16: return new String[]{"hk17", "hk18"};   // 第十三题
               case 17: return new String[]{"hk19", "hk20"};   // 第十四题
               case 18: return new String[]{"hk21", "hk22"};   // 第十五题
               default: return null;
           }
       } else if (groupNumber == 3) {
           // 第三组图片资源
           switch (questionNumber) {
               case 1: return new String[]{"hl1"};            // 第一题
               case 2: return new String[]{"hl2"};            // 第二题
               case 3: return new String[]{"hl3"};            // 第三题
               case 4: return new String[]{"hl4"};            // 第四题
               case 5: return new String[]{"hl5"};            // 第五题
               case 6: return new String[]{"hl6"};            // 第六题
               case 7: return new String[]{"hl7", "hl8"};     // 第七题
               case 8: return new String[]{"hl9"};            // 第八题
               case 9: return new String[]{"hl10"};           // 第九题
               case 10: return new String[]{"hl11", "hl12"};   // 第十题
               case 11: return new String[]{"hl13", "hl14"};   // 第十一题
               case 12: return new String[]{"hl15", "hl16"};   // 第十二题
               case 13: return new String[]{"hl17"};           // 第十三题
               case 14: return new String[]{"hl18", "hl19"};   // 第十四题
               case 15: return new String[]{"hl20", "hl21"};   // 第十五题
               case 16: return new String[]{"hl22", "hl23"};   // 第十六题
               default: return null;
           }
       } else if (groupNumber == 4) {
           // 第四组图片资源
           switch (questionNumber) {
               case 1: return new String[]{"hm1"};            // 第一题：小朋友怎么了，为什么？
               case 2: return new String[]{"hm2"};            // 第二题：小朋友在干什么，为什么？
               case 3: return new String[]{"hm3"};            // 第三题：小朋友怎么了，为什么？
               case 4: return new String[]{"hm4"};            // 示例：看这幅图，下雨了，但是小朋友没带伞
               case 5: return new String[]{"hm5"};            // 第四题：看看这个图，小狗想进屋
               case 6: return new String[]{"hm6"};            // 第五题：小朋友想骑自行车
               case 7: return new String[]{"hm7"};            // 第六题：他想吃饼干
               case 8: return new String[]{"hm8"};            // 示例：看这幅图，小女孩正在洗衣服
               case 9: return new String[]{"hm9"};            // 第七题：那这幅图呢？
               case 10: return new String[]{"hm10"};           // 第八题：那这幅图呢？
               case 11: return new String[]{"hm11"};           // 第九题：那这幅图呢？
               case 12: return new String[]{"hm12"};           // 示例：看这幅图，如果吃很多糖，就会牙疼
               case 13: return new String[]{"hm13"};           // 第十题：那这幅图呢？
               case 14: return new String[]{"hm14"};           // 第十一题：那这幅图呢？
               case 15: return new String[]{"hm15"};           // 第十二题：那这幅图呢？
               case 16: return new String[]{"hm16"};           // 示例：这幅图，叔叔比阿姨胖
               case 17: return new String[]{"hm17"};           // 第十三题：请用一句话比一比这两个小朋友的身高
               case 18: return new String[]{"hm18"};           // 第十四题：用一句话比一比两个小朋友花的多少
               case 19: return new String[]{"hm19"};           // 第十五题：用一句话比一比铅笔的长短
               case 20: return new String[]{"hm20", "hm21", "hm22", "hm23"};   // 16. 看图讲故事
               default: return null;
           }
       }
       return null;
   }

   private void setupYesNoClickListeners(Button yesButton, Button noButton, int questionNumber) {
        if(yesButton == null || noButton == null) return;
        
        yesButton.setOnClickListener(v -> {
            answer = "RIGHT";
            result = true;
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            
            // 停止录音
            if(audio != null) {
                AudioRecorder.getInstance().stopRecorder();
            }
            
            // 跳转到下一题
            goToNextPage(questionNumber - 1);
        });
        
        noButton.setOnClickListener(v -> {
            answer = "WRONG";
            result = false;
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            
            // 停止录音
            if(audio != null) {
                AudioRecorder.getInstance().stopRecorder();
            }
            
            // 跳转到下一题
            goToNextPage(questionNumber - 1);
        });
    }

   private void goToNextPage(int position) {
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
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();
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

   @Override
   public void toJson(JSONObject evaluations) throws JSONException {
       // 检查是否为示例题目，如果是则不保存到JSON中
       int groupNumber = testcontext.getInstance().getGroupNumber();
       boolean isExample = isExampleQuestion(num - 1, groupNumber);
       if (isExample) {
           return; // 示例题目不保存
       }
       
       JSONObject object = new JSONObject();
       object.put("question",question);
       object.put("num",num);
       object.put("right_option",right_option);
       if(time!=null){
           object.put("result",result);
           object.put("answer",answer);
           object.put("score",score);
           if(audio != null) {
               object.put("audioPath",audio.getPath());
           } else {
               object.put("audioPath",JSONObject.NULL);
           }
           object.put("time",time);
       }
       else {
           object.put("result",JSONObject.NULL);
           object.put("answer",JSONObject.NULL);
           object.put("score",JSONObject.NULL);
           object.put("audioPath",JSONObject.NULL);
           object.put("time",JSONObject.NULL);
       }
       // 使用组特定的JSONArray键
       String seKey = "SE" + groupNumber;
       // 确保JSONArray存在
       if (!evaluations.has(seKey)) {
           evaluations.put(seKey, new JSONArray());
       }
       JSONArray seArray = evaluations.getJSONArray(seKey);
       // 检查是否已经存在相同题号的记录
       boolean found = false;
       for (int i = 0; i < seArray.length(); i++) {
           JSONObject item = seArray.getJSONObject(i);
           if (item.getInt("num") == num) {
               // 更新现有记录
               seArray.put(i, object);
               found = true;
               break;
           }
       }
       if (!found) {
           // 添加新记录
           seArray.put(object);
       }
   }
}
