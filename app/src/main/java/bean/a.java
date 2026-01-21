package bean;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

    private String errorType; // 错误类型，默认空
    private String phonologyProcess; // 音系历程，默认空
    private String pinyin; // 拼音，可选
    private static final String[] ERROR_TYPE_OPTIONS = new String[]{
            "",
            "替代",
            "增加",
            "减少",
            "扭曲"
    };

    private static final String[] PHONOLOGY_PROCESS_OPTIONS = new String[]{
            "",
            "前置",
            "后置",
            "塞音化",
            "去塞擦音化",
            "送气化",
            "去送气化",
            "圆唇化",
            "展唇化",
            "腭化",
            "鼻化",
            "去卷舌化",
            "卷舌化"
    };

    public a(int num, String target, String progress, String target_tone1, String target_tone2, bean.audio audio, String time) {
        super(num);
        this.target = target;
        this.audio = audio;
        this.time = time;
        this.errorType = null;
        this.phonologyProcess = null;
        this.pinyin = null;
    }

    public a(int num, List<CharacterPhonology> targetWord, String progress, bean.audio audio, String time) {
        super(num);
        this.targetWord = targetWord;
        this.audio = audio;
        this.time = time;
        this.errorType = null;
        this.phonologyProcess = null;
        this.pinyin = null;
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

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getPhonologyProcess() {
        return phonologyProcess;
    }

    public void setPhonologyProcess(String phonologyProcess) {
        this.phonologyProcess = phonologyProcess;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public void ensureAnswerSizeFromTarget() {
        if (targetWord == null) return;
        if (answerPhonology == null) {
            answerPhonology = new java.util.ArrayList<>();
        }
        while (answerPhonology.size() < targetWord.size()) {
            CharacterPhonology src = targetWord.get(answerPhonology.size());
            CharacterPhonology cp = new CharacterPhonology();
            if (src != null) {
                cp.hanzi = src.hanzi;
                if (src.phonology != null) {
                    PhonologyPart part = new PhonologyPart();
                    part.initial = src.phonology.initial;
                    part.medial = src.phonology.medial;
                    part.nucleus = src.phonology.nucleus;
                    part.coda = src.phonology.coda;
                    part.isInducible = src.phonology.isInducible;
                    cp.phonology = part;
                }
            }
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
        // 词条序号 | 词条 | 拼音 | 声母 | 韵头 | 韵腹 | 韵尾 | 错误类型 | 音系历程 | 是否可诱发 | 录音
        for (int i = 0; i < views.length; i++) views[i].setVisibility(View.GONE);
        for (int i = 0; i < 11 && i < views.length; i++) views[i].setVisibility(View.VISIBLE);
        if (position == 0) {
            ((TextView) views[0]).setText("词条序号");
            ((TextView) views[1]).setText("词条");
            ((TextView) views[2]).setText("拼音");
            ((TextView) views[3]).setText("声母");
            ((TextView) views[4]).setText("韵头");
            ((TextView) views[5]).setText("韵腹");
            ((TextView) views[6]).setText("韵尾");
            ((TextView) views[7]).setText("错误类型");
            ((TextView) views[8]).setText("音系历程");
            ((TextView) views[9]).setText("是否可诱发");
            if (views.length > 10) ((TextView) views[10]).setText("录音");
        } else {
            ((TextView) views[0]).setText(String.valueOf(num));
            ((TextView) views[1]).setText(buildTargetHanzi());
            String pinyinValue = computePinyinFallback();
            ((TextView) views[2]).setText(pinyinValue == null ? "" : pinyinValue);
            ((TextView) views[3]).setText(joinParts(answerPhonology, PartType.INITIAL));
            ((TextView) views[4]).setText(joinParts(answerPhonology, PartType.MEDIAL));
            ((TextView) views[5]).setText(joinParts(answerPhonology, PartType.NUCLEUS));
            ((TextView) views[6]).setText(joinParts(answerPhonology, PartType.CODA));
            ((TextView) views[7]).setText(errorType == null ? "" : errorType);
            if (views.length > 8) {
                ((TextView) views[8]).setText(phonologyProcess == null ? "" : phonologyProcess);
            }
            if (views.length > 9) {
                ((TextView) views[9]).setText(joinInducible(answerPhonology));
            }
            if (views.length > 10) {
                if (audio != null && time != null) {
                    ((TextView) views[10]).setTextColor(ResultContext.getInstance().getContext().getResources().getColor(R.color.audio_green));
                    ((TextView) views[10]).setText(ResultContext.getInstance().getContext().getString(R.string.audio));
                    AudioPlayer.getInstance().addIcon((TextView) views[10]);
                    views[10].setOnClickListener(v -> AudioPlayer.getInstance().play(audio.getPath(), position - 1));
                } else {
                    ((TextView) views[10]).setText("");
                }
            }
        }
        return 10;
    }

    private enum PartType {INITIAL, MEDIAL, NUCLEUS, CODA}

    private String buildTargetHanzi() {
        if (targetWord == null || targetWord.isEmpty()) return target == null ? "" : target;
        StringBuilder sb = new StringBuilder();
        for (CharacterPhonology cp : targetWord) {
            if (cp != null && cp.hanzi != null) sb.append(cp.hanzi);
        }
        return sb.toString();
    }

    private String joinParts(List<CharacterPhonology> ans, PartType type) {
        if (ans == null || ans.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ans.size(); i++) {
            CharacterPhonology cp = ans.get(i);
            if (cp == null || cp.phonology == null) {
                sb.append(" ");
            } else {
                switch (type) {
                    case INITIAL:
                        sb.append(nullToEmpty(cp.phonology.initial));
                        break;
                    case MEDIAL:
                        sb.append(nullToEmpty(cp.phonology.medial));
                        break;
                    case NUCLEUS:
                        sb.append(nullToEmpty(cp.phonology.nucleus));
                        break;
                    case CODA:
                        sb.append(nullToEmpty(cp.phonology.coda));
                        break;
                }
            }
            if (i < ans.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private String joinInducible(List<CharacterPhonology> ans) {
        if (ans == null || ans.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ans.size(); i++) {
            CharacterPhonology cp = ans.get(i);
            boolean flag = cp != null && cp.phonology != null && cp.phonology.isInducible;
            sb.append(flag ? "✔" : "");
            if (i < ans.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void toJson(JSONObject evaluations) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("num", num);
        object.put("target", target);
        object.put("errorType", errorType == null ? "" : errorType);
        object.put("phonologyProcess", phonologyProcess == null ? "" : phonologyProcess);
        String pinyinValue = computePinyinFallback();
        object.put("pinyin", pinyinValue == null ? "" : pinyinValue);
        if (targetWord != null) {
            JSONArray arr = new JSONArray();
            for (CharacterPhonology cp : targetWord) {
                if (cp != null) arr.put(cp.toJson());
            }
            object.put("targetWord", arr);
        } else {
            object.put("targetWord", JSONObject.NULL);
        }
        JSONArray arrAns = new JSONArray();
        if (answerPhonology != null) {
            for (CharacterPhonology cp : answerPhonology) {
                if (cp != null) arrAns.put(cp.toJson());
            }
        }
        object.put("answerPhonology", arrAns);
        if (audio != null && time != null) {
            object.put("audioPath", audio.getPath());
            object.put("time", time);
        } else {
            object.put("audioPath", JSONObject.NULL);
            object.put("time", JSONObject.NULL);
        }
        evaluations.getJSONArray("A").put(object);
    }

    public static a fromJson(JSONObject object) throws JSONException {
        int num = object.optInt("num", 0);
        String target = object.optString("target", null);
        String audioPath = object.isNull("audioPath") ? null : object.optString("audioPath", null);
        String time = object.isNull("time") ? null : object.optString("time", null);

        String error = object.optString("errorType", "");
        if (error != null && error.isEmpty()) error = null;
        String phonologyProcess = null;
        if (object.has("phonologyProcess") && !object.isNull("phonologyProcess")) {
            phonologyProcess = object.optString("phonologyProcess", null);
            if (phonologyProcess != null && phonologyProcess.isEmpty()) phonologyProcess = null;
        }
        String pinyin = null;
        if (object.has("pinyin") && !object.isNull("pinyin")) {
            pinyin = object.optString("pinyin", null);
            if (pinyin != null && pinyin.isEmpty()) pinyin = null;
        }

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

        List<CharacterPhonology> answerPhonology = null;
        if (object.has("answerPhonology") && !object.isNull("answerPhonology")) {
            JSONArray ansArr = object.optJSONArray("answerPhonology");
            if (ansArr != null) {
                answerPhonology = new java.util.ArrayList<>();
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
                    answerPhonology.add(cp);
                }
            }
        }

        bean.audio audioObj = null;
        if (audioPath != null && !audioPath.equals(JSONObject.NULL)) {
            audioObj = new audio(audioPath);
        }
        a item = new a(num, targetWord, null, audioObj, time);
        item.setTarget(target);
        item.setAnswerPhonology(answerPhonology);
        item.setErrorType(error);
        item.setPhonologyProcess(phonologyProcess);
        item.setPinyin(pinyin);
        return item;
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
        Spinner spError = view.findViewById(R.id.sp_error_type);
        Spinner spPhonologyProcess = view.findViewById(R.id.sp_phonology_process);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, ERROR_TYPE_OPTIONS);
        spError.setAdapter(adapter);
        int defIndex = 0;
        if (errorType != null) {
            for (int i = 0; i < ERROR_TYPE_OPTIONS.length; i++) {
                if (ERROR_TYPE_OPTIONS[i].equals(errorType)) {
                    defIndex = i;
                    break;
                }
            }
        }
        spError.setSelection(defIndex);
        final boolean[] firstSelect = new boolean[]{true};

        ArrayAdapter<String> processAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, PHONOLOGY_PROCESS_OPTIONS);
        spPhonologyProcess.setAdapter(processAdapter);
        int defProcessIndex = 0;
        if (phonologyProcess != null) {
            for (int i = 0; i < PHONOLOGY_PROCESS_OPTIONS.length; i++) {
                if (PHONOLOGY_PROCESS_OPTIONS[i].equals(phonologyProcess)) {
                    defProcessIndex = i;
                    break;
                }
            }
            spPhonologyProcess.setSelection(defProcessIndex);
        }

        spError.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstSelect[0] && errorType == null && pos == 0) {
                    firstSelect[0] = false;
                    return;
                }
                firstSelect[0] = false;
                errorType = ERROR_TYPE_OPTIONS[pos];
                if (errorType != null && errorType.isEmpty()) errorType = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spPhonologyProcess.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                phonologyProcess = PHONOLOGY_PROCESS_OPTIONS[pos];
                if (phonologyProcess != null && phonologyProcess.isEmpty()) phonologyProcess = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
    private interface StringConsumer {
        void accept(String text);
    }

    private TextWatcher simpleWatcher(StringConsumer consumer) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                consumer.accept(s.toString());
            }
        };
    }

    private void setAns(int idx, PartSetter setter) {
        ensureAnswerSizeFromTarget();
        CharacterPhonology cpAns = answerPhonology.get(idx);
        if (cpAns.phonology == null) cpAns.phonology = new PhonologyPart();
        setter.set(cpAns.phonology);
    }

    private void nextPage(int position, int count, int lengths) {

        int nP = position + 1;
        if (count >= lengths) {
            Toast.makeText(testcontext.getInstance().getContext(), "已完成测评题目！", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onAllQuestionComplete();
            }
            testcontext.getInstance().getContext().finish();
        }
        if (nP >= lengths) {
            testcontext.getInstance().getViewPager().setCurrentItem(testcontext.getInstance().searchOne(), true);
        } else {
            testcontext.getInstance().getViewPager().setCurrentItem(nP, true);
        }
    }

    private String computePinyinFallback() {
        if (pinyin != null && !pinyin.isEmpty()) return pinyin;
        if (target != null && !target.isEmpty()) {
            String mapped = ImageUrls.getAPinyin(target);
            if (mapped != null && !mapped.isEmpty()) return mapped;
        }
        if (targetWord != null && !targetWord.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (CharacterPhonology cp : targetWord) {
                if (cp == null || cp.phonology == null) continue;
                String partInitial = nullToEmpty(cp.phonology.initial);
                String partMedial = nullToEmpty(cp.phonology.medial);
                String partNucleus = nullToEmpty(cp.phonology.nucleus);
                String partCoda = nullToEmpty(cp.phonology.coda);
                if (sb.length() > 0) sb.append(" ");
                sb.append(partInitial).append(partMedial).append(partNucleus).append(partCoda);
            }
            if (sb.length() > 0) return sb.toString();
        }
        if (target != null && !target.isEmpty()) return target;
        String built = buildTargetHanzi();
        return built == null ? "" : built;
    }
}
