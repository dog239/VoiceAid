package bean;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CCLEvaluation.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.ImageUrls;
import utils.ResultContext;
import utils.testcontext;

/**
 * 构音（A）
 */
public class a extends evaluation {
    private String target;
    private bean.audio audio;
    private Boolean result;
    private String time;
    private final String colum1 = "题号";
    private final String colum2 = "目标词";
    private final String colum3 = "答题时长";
    private final String colum4 = "录音";

    private long timeInMilliseconds = 0L;

    // 新的音韵拆解数据模型：按汉字拆分，并包含声母/介音/韵腹/韵尾与可诱发标记
    public static class PhonologyPart {
        public String initial;
        public String medial;
        public String nucleus;
        public String coda;
        public boolean isInducible;

        public JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("initial", initial == null ? JSONObject.NULL : initial);
            o.put("medial", medial == null ? JSONObject.NULL : medial);
            o.put("nucleus", nucleus == null ? JSONObject.NULL : nucleus);
            o.put("coda", coda == null ? JSONObject.NULL : coda);
            o.put("isInducible", isInducible);
            return o;
        }
    }

    public static class CharacterPhonology {
        public String hanzi;
        public PhonologyPart phonology;

        public JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("hanzi", hanzi == null ? JSONObject.NULL : hanzi);
            o.put("phonology", phonology == null ? JSONObject.NULL : phonology.toJson());
            return o;
        }
    }

    private List<CharacterPhonology> targetWord; // 新结构

    // 用户作答部分：可编辑的音韵拆解答案
    private List<CharacterPhonology> answerPhonology;

    public a(int num, String target, String progress, String target_tone1, String target_tone2, bean.audio audio, String time) {
        super(num);
        this.target = target;
        this.audio = audio;
        this.time = time;
    }

    public a(int num, List<CharacterPhonology> targetWord, String progress, bean.audio audio, String time) {
        super(num);
        this.targetWord = targetWord;
        this.audio = audio;
        this.time = time;
    }


    public List<CharacterPhonology> getTargetWord() {
        return targetWord;
    }

    public void setTargetWord(List<CharacterPhonology> targetWord) {
        this.targetWord = targetWord;
    }

    public List<CharacterPhonology> getAnswerPhonology() {
        return answerPhonology;
    }

    public void setAnswerPhonology(List<CharacterPhonology> answerPhonology) {
        this.answerPhonology = answerPhonology;
    }

    public void ensureAnswerSizeFromTarget() {
        if (targetWord == null) return;
        if (answerPhonology == null) answerPhonology = new java.util.ArrayList<>();
        while (answerPhonology.size() < targetWord.size()) {
            CharacterPhonology cp = new CharacterPhonology();
            cp.hanzi = targetWord.get(answerPhonology.size()).hanzi;
            PhonologyPart part = new PhonologyPart();
            cp.phonology = part;
            answerPhonology.add(cp);
        }
    }

    private utils.allquestionlistener listener;

    public void setAllQuestionListener(utils.allquestionlistener listener) {
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

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int handle(View[] views, int position) {
        for (int i = 0; i < views.length; i++) {
            views[i].setVisibility(View.GONE);
        }
        for (int i = 0; i < 4; i++) {
            views[i].setVisibility(View.VISIBLE);
        }
        if (position == 0) {
            ((TextView) views[0]).setText(colum1);
            ((TextView) views[1]).setText(colum2);
            ((TextView) views[2]).setText(colum3);
            ((TextView) views[3]).setText(colum4);
        } else {
            ((TextView) views[0]).setText(String.valueOf(num));
            ((TextView) views[1]).setText(target);
            if (audio != null) {
                ((TextView) views[2]).setText(time);
                ((TextView) views[3]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                ((TextView) views[3]).setText(ResultContext.getInstance().getContext().getResources().getString(R.string.audio));
                AudioPlayer.getInstance().addIcon((TextView) views[3]);
                views[3].setOnClickListener(v -> AudioPlayer.getInstance().play(audio.getPath(), position - 1));
            }
        }
        return 3;
    }


    public static a fromJson(JSONObject object) throws JSONException {
        int num = object.optInt("num", 0);
        String target = object.optString("target", null);
        String progress = object.isNull("progress") ? null : object.optString("progress", null);
        String targetTone1 = object.isNull("target_tone1") ? null : object.optString("target_tone1", null);
        String targetTone2 = object.isNull("target_tone2") ? null : object.optString("target_tone2", null);
        String audioPath = object.isNull("audioPath") ? null : object.optString("audioPath", null);
        String time = object.isNull("time") ? null : object.optString("time", null);
        List<CharacterPhonology> targetWord = null;
        if (object.has("targetWord") && !object.isNull("targetWord")) {
            JSONArray arr = object.optJSONArray("targetWord");
            if (arr != null) {
                targetWord = new java.util.ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    if (item == null) continue;
                    CharacterPhonology cp = new CharacterPhonology();
                    cp.hanzi = item.isNull("hanzi") ? null : item.optString("hanzi", null);
                    JSONObject phObj = item.optJSONObject("phonology");
                    if (phObj != null) {
                        PhonologyPart part = new PhonologyPart();
                        part.initial = phObj.isNull("initial") ? null : phObj.optString("initial", null);
                        part.medial = phObj.isNull("medial") ? null : phObj.optString("medial", null);
                        part.nucleus = phObj.isNull("nucleus") ? null : phObj.optString("nucleus", null);
                        part.coda = phObj.isNull("coda") ? null : phObj.optString("coda", null);
                        part.isInducible = phObj.optBoolean("isInducible", false);
                        cp.phonology = part;
                    }
                    targetWord.add(cp);
                }
            }
        }
        bean.audio audioObj = null;
        if (audioPath != null && !audioPath.equals(JSONObject.NULL)) {
            audioObj = new audio(audioPath);
        }
        a item = new a(num, target, progress, targetTone1, targetTone2, audioObj, time);
        item.setTargetWord(targetWord); // may be null if missing
        if (object.has("answerPhonology") && !object.isNull("answerPhonology")) {
            JSONArray ansArr = object.optJSONArray("answerPhonology");
            if (ansArr != null) {
                List<CharacterPhonology> ansList = new java.util.ArrayList<>();
                for (int i = 0; i < ansArr.length(); i++) {
                    JSONObject itemAns = ansArr.optJSONObject(i);
                    if (itemAns == null) continue;
                    CharacterPhonology cp = new CharacterPhonology();
                    cp.hanzi = itemAns.isNull("hanzi") ? null : itemAns.optString("hanzi", null);
                    JSONObject phObj = itemAns.optJSONObject("phonology");
                    if (phObj != null) {
                        PhonologyPart part = new PhonologyPart();
                        part.initial = phObj.isNull("initial") ? null : phObj.optString("initial", null);
                        part.medial = phObj.isNull("medial") ? null : phObj.optString("medial", null);
                        part.nucleus = phObj.isNull("nucleus") ? null : phObj.optString("nucleus", null);
                        part.coda = phObj.isNull("coda") ? null : phObj.optString("coda", null);
                        part.isInducible = phObj.optBoolean("isInducible", false);
                        cp.phonology = part;
                    }
                    ansList.add(cp);
                }
                item.setAnswerPhonology(ansList);
            }
        }
        return item;
    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num", num);
        object.put("target", target);
        if (targetWord != null) {
            JSONArray arr = new JSONArray();
            for (CharacterPhonology cp : targetWord) {
                if (cp != null) {
                    arr.put(cp.toJson());
                }
            }
            object.put("targetWord", arr);
        } else {
            object.put("targetWord", JSONObject.NULL);
        }
        if (answerPhonology != null) {
            JSONArray arrAns = new JSONArray();
            for (CharacterPhonology cp : answerPhonology) {
                if (cp != null) {
                    arrAns.put(cp.toJson());
                }
            }
            object.put("answerPhonology", arrAns);
        }
        if (time != null) {
            object.put("audioPath", audio.getPath());
            object.put("time", time);
        } else {
            object.put("audioPath", JSONObject.NULL);
            object.put("time", JSONObject.NULL);
        }
        evaluations.getJSONArray("A").put(object);
    }

    @Override
    public void test(View view, int position, List<Integer> ImageIdList, List<Integer[]> ImageGroupIdList,
                     List<String[]> StringGroupIdList, String[] Hint, String[] TabString,
                     TextView counter, TextView timer) {
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView numberTextView = view.findViewById(R.id.tv_2);
        TextView ansTextView = view.findViewById(R.id.tv_ans);
        Button startButton = view.findViewById(R.id.btn_start);
        Button nextButton = view.findViewById(R.id.btn_next);

        imageView.setImageResource(ImageIdList.get(position));
        numberTextView.setText("第" + TabString[position] + "题：图片画的是什么？");
        counter.setText(testcontext.getInstance().getCount() + "/" + testcontext.getInstance().getLengths());
        ansTextView.setText(Hint[position]);

        if (time != null) {
            ansTextView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            startButton.setEnabled(false);
        }


        AudioRecorder.OnRefreshUIThreadListener listener = time -> {
            this.time = time;
            timer.setText(time);
        };


        startButton.setOnClickListener(v -> {
            try {
                ansTextView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                testcontext.getInstance().getViewPager().setPagingEnabled(false);
                startButton.setEnabled(false);
                AudioRecorder.getInstance().setOnRefreshUIThreadListener(listener);
                AudioRecorder.getInstance().startRecorder();
                audio = new audio(AudioRecorder.getInstance().getOutputFilePath());
                testcontext.getInstance().incrementCount();
                counter.setText(testcontext.getInstance().getCount() + "/" + testcontext.getInstance().getLengths());
            } catch (IOException e) {
                Toast.makeText(testcontext.getInstance().getContext(), "录制失败！", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        });


        nextButton.setOnClickListener(v -> {


            testcontext.getInstance().getViewPager().setPagingEnabled(true);

            if (audio != null) {
                AudioRecorder.getInstance().stopRecorder();
                result = Boolean.TRUE;
            }
            nextPage(position, testcontext.getInstance().getCount(), testcontext.getInstance().getLengths());
        });

        LinearLayout phonologyContainer = view.findViewById(R.id.ll_phonology_container);
        ensureAnswerSizeFromTarget();
        renderPhonologyTable(phonologyContainer);
    }

    private void renderPhonologyTable(LinearLayout container) {
        container.removeViews(1, Math.max(0, container.getChildCount() - 1));
        if (targetWord == null) return;
        for (int i = 0; i < targetWord.size(); i++) {
            CharacterPhonology targetCp = targetWord.get(i);
            CharacterPhonology ansCp = (answerPhonology != null && i < answerPhonology.size()) ? answerPhonology.get(i) : null;
            LinearLayout row = new LinearLayout(container.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 4, 0, 4);

            TextView tvHan = new TextView(container.getContext());
            tvHan.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            tvHan.setText(targetCp != null ? targetCp.hanzi : "");
            row.addView(tvHan);

            EditText etInitial = createPartEdit(container, ansCp != null && ansCp.phonology != null ? ansCp.phonology.initial : "");
            EditText etMedial = createPartEdit(container, ansCp != null && ansCp.phonology != null ? ansCp.phonology.medial : "");
            EditText etNucleus = createPartEdit(container, ansCp != null && ansCp.phonology != null ? ansCp.phonology.nucleus : "");
            EditText etCoda = createPartEdit(container, ansCp != null && ansCp.phonology != null ? ansCp.phonology.coda : "");
            CheckBox cbInd = new CheckBox(container.getContext());
            cbInd.setChecked(ansCp != null && ansCp.phonology != null && ansCp.phonology.isInducible);

            row.addView(etInitial);
            row.addView(etMedial);
            row.addView(etNucleus);
            row.addView(etCoda);
            row.addView(cbInd);

            final int idx = i;
            cbInd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ensureAnswerSizeFromTarget();
                CharacterPhonology cpAns = answerPhonology.get(idx);
                if (cpAns.phonology == null) cpAns.phonology = new PhonologyPart();
                cpAns.phonology.isInducible = isChecked;
            });

            etInitial.addTextChangedListener(simpleWatcher(text -> setAns(idx, part -> part.initial = text)));
            etMedial.addTextChangedListener(simpleWatcher(text -> setAns(idx, part -> part.medial = text)));
            etNucleus.addTextChangedListener(simpleWatcher(text -> setAns(idx, part -> part.nucleus = text)));
            etCoda.addTextChangedListener(simpleWatcher(text -> setAns(idx, part -> part.coda = text)));

            container.addView(row);
        }
    }

    private EditText createPartEdit(LinearLayout container, String val) {
        EditText et = new EditText(container.getContext());
        et.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        et.setSingleLine(true);
        if (val != null) et.setText(val);
        return et;
    }

    private interface PartSetter {
        void set(PhonologyPart part);
    }

    // 简单字符串消费者，避免依赖 Java 8 Consumer 接口以兼容低 API
    private interface StringConsumer { void accept(String text); }

    private TextWatcher simpleWatcher(StringConsumer consumer) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { consumer.accept(s.toString()); }
        };
    }

    private void setAns(int idx, PartSetter setter) {
        ensureAnswerSizeFromTarget();
        CharacterPhonology cpAns = answerPhonology.get(idx);
        if (cpAns.phonology == null) cpAns.phonology = new PhonologyPart();
        setter.set(cpAns.phonology);
    }

    private void nextPage(int position,int count, int lengths){

        int nP = position + 1;
        if (count >= lengths) {
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();
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
