package com.example.CCLEvaluation;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.ChildBackgroundInfoHelper;
import utils.MedicalDiagnosisImageHelper;
import utils.dataManager;

public class ChildDetailEditActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_NAME = "fName";

    private String fName;
    private JSONObject childData;
    private boolean isEditing;

    private ViewGroup formRoot;
    private ImageView btnBack;
    private Button btnEdit;
    private Button btnSave;
    private Button btnChooseMedicalDocuments;
    private Button btnTakeMedicalDocuments;
    private TextView tvMedicalDocumentsEmpty;
    private RecyclerView rvMedicalDocuments;
    private MedicalDocumentImageAdapter medicalDocumentImageAdapter;

    private final List<JSONObject> medicalDocumentImages = new ArrayList<>();
    private final List<JSONObject> persistedMedicalDocumentImages = new ArrayList<>();
    private final Set<String> deletedMedicalDocumentPaths = new HashSet<>();
    private File currentCameraPhotoFile;

    private ActivityResultLauncher<String> pickMultiplePhotosLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail_edit);

        registerPhotoLaunchers();
        initViews();
        bindActions();
        loadChildData();
        fillForm();
        setEditingMode(false);
    }

    private void registerPhotoLaunchers() {
        pickMultiplePhotosLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris == null || uris.isEmpty()) {
                Toast.makeText(this, "未选择图片。", Toast.LENGTH_SHORT).show();
                return;
            }
            handleSelectedPhotos(uris);
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (!success || currentCameraPhotoFile == null || !currentCameraPhotoFile.exists()) {
                MedicalDiagnosisImageHelper.deleteFileQuietly(currentCameraPhotoFile);
                currentCameraPhotoFile = null;
                Toast.makeText(this, "拍照失败，请重试。", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject imageInfo = MedicalDiagnosisImageHelper.buildImageMetadata(currentCameraPhotoFile, "image/jpeg", "");
                medicalDocumentImages.add(imageInfo);
                currentCameraPhotoFile = null;
                refreshMedicalDocumentsUi();
                Toast.makeText(this, "拍照成功，保存后生效。", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                MedicalDiagnosisImageHelper.deleteFileQuietly(currentCameraPhotoFile);
                currentCameraPhotoFile = null;
                Toast.makeText(this, "图片处理失败，请重试。", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initViews() {
        formRoot = findViewById(R.id.layout_child_detail_content);
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit_child_detail);
        btnSave = findViewById(R.id.btn_save_child_detail);
        btnChooseMedicalDocuments = findViewById(R.id.btn_choose_medical_documents);
        btnTakeMedicalDocuments = findViewById(R.id.btn_take_medical_documents);
        tvMedicalDocumentsEmpty = findViewById(R.id.tv_medical_documents_empty);
        rvMedicalDocuments = findViewById(R.id.rv_medical_documents);

        rvMedicalDocuments.setLayoutManager(new GridLayoutManager(this, 2));
        rvMedicalDocuments.setNestedScrollingEnabled(false);
        medicalDocumentImageAdapter = new MedicalDocumentImageAdapter(this::removeMedicalDocumentAt);
        rvMedicalDocuments.setAdapter(medicalDocumentImageAdapter);
    }

    private void bindActions() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnEdit.setOnClickListener(v -> {
            if (isEditing) {
                return;
            }
            setEditingMode(true);
            View first = findViewById(R.id.et_language_0_3);
            if (first != null) {
                first.requestFocus();
            }
        });
        btnSave.setOnClickListener(v -> {
            if (!isEditing) {
                return;
            }
            saveForm();
        });
        btnChooseMedicalDocuments.setOnClickListener(v -> {
            if (!isEditing) {
                return;
            }
            pickMultiplePhotosLauncher.launch("image/*");
        });
        btnTakeMedicalDocuments.setOnClickListener(v -> {
            if (!isEditing) {
                return;
            }
            launchCamera();
        });
    }

    private void loadChildData() {
        fName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(this, "缺少儿童记录。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            childData = dataManager.getInstance().loadData(fName);
        } catch (Exception e) {
            childData = new JSONObject();
            Toast.makeText(this, "读取儿童记录失败。", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillForm() {
        JSONObject info = optObject(childData, "info");
        JSONObject backgroundInfo = ChildBackgroundInfoHelper.optBackgroundInfo(info);

        JSONObject basicCare = optObject(backgroundInfo, "basicCare");
        setChecked(R.id.cb_caregiver_0_3_parents, basicCare.optBoolean("caregiver0To3Parents"));
        setChecked(R.id.cb_caregiver_0_3_grandparents, basicCare.optBoolean("caregiver0To3Grandparents"));
        setChecked(R.id.cb_caregiver_0_3_nanny, basicCare.optBoolean("caregiver0To3Nanny"));
        setChecked(R.id.cb_caregiver_0_3_other, basicCare.optBoolean("caregiver0To3Other"));
        setText(R.id.et_caregiver_0_3_other, basicCare.optString("caregiver0To3OtherText"));
        setText(R.id.et_language_0_3, basicCare.optString("language0To3"));
        setChecked(R.id.cb_caregiver_3_6_parents, basicCare.optBoolean("caregiver3To6Parents"));
        setChecked(R.id.cb_caregiver_3_6_grandparents, basicCare.optBoolean("caregiver3To6Grandparents"));
        setChecked(R.id.cb_caregiver_3_6_nanny, basicCare.optBoolean("caregiver3To6Nanny"));
        setChecked(R.id.cb_caregiver_3_6_other, basicCare.optBoolean("caregiver3To6Other"));
        setText(R.id.et_caregiver_3_6_other, basicCare.optString("caregiver3To6OtherText"));
        setText(R.id.et_language_3_6, basicCare.optString("language3To6"));
        setText(R.id.et_dialect, basicCare.optString("dialect"));

        JSONObject birthHistory = optObject(backgroundInfo, "birthHistory");
        setDeliveryMethod(birthHistory.optString("deliveryMethod"));
        setChecked(R.id.cb_birth_otitis_media, birthHistory.optBoolean("otitisMedia"));
        setChecked(R.id.cb_birth_respiratory_disease, birthHistory.optBoolean("respiratoryDisease"));
        setChecked(R.id.cb_birth_head_injury, birthHistory.optBoolean("headInjury"));
        setChecked(R.id.cb_birth_epilepsy, birthHistory.optBoolean("epilepsy"));
        setChecked(R.id.cb_birth_low_weight, birthHistory.optBoolean("lowWeight"));
        setChecked(R.id.cb_birth_low_weight_below_2000, birthHistory.optBoolean("lowWeightBelow2000"));
        setChecked(R.id.cb_birth_low_weight_below_1500, birthHistory.optBoolean("lowWeightBelow1500"));
        setChecked(R.id.cb_birth_incubator, birthHistory.optBoolean("incubator"));
        setText(R.id.et_birth_incubator_days, birthHistory.optString("incubatorDays"));
        setChecked(R.id.cb_birth_jaundice, birthHistory.optBoolean("jaundice"));
        setChecked(R.id.cb_birth_meningitis, birthHistory.optBoolean("meningitis"));
        setChecked(R.id.cb_birth_cleft_lip_palate, birthHistory.optBoolean("cleftLipPalate"));
        setChecked(R.id.cb_birth_umbilical_cord_neck, birthHistory.optBoolean("umbilicalCordNeck"));
        setChecked(R.id.cb_birth_hypoxia, birthHistory.optBoolean("hypoxia"));
        setChecked(R.id.cb_birth_medication, birthHistory.optBoolean("medication"));
        setText(R.id.et_birth_medication, birthHistory.optString("medicationText"));
        setChecked(R.id.cb_birth_other, birthHistory.optBoolean("other"));
        setText(R.id.et_birth_other, birthHistory.optString("otherText"));

        JSONObject growthDevelopment = optObject(backgroundInfo, "growthDevelopment");
        setFeedingMethod(growthDevelopment.optString("feedingMethod"));
        setNormalDelayed(R.id.rg_smile_status, growthDevelopment.optString("smileStatus"), R.id.rb_smile_normal, R.id.rb_smile_delayed);
        setNormalDelayed(R.id.rg_sit_status, growthDevelopment.optString("sitStatus"), R.id.rb_sit_normal, R.id.rb_sit_delayed);
        setNormalDelayed(R.id.rg_head_control_status, growthDevelopment.optString("headControlStatus"), R.id.rb_head_control_normal, R.id.rb_head_control_delayed);
        setNormalDelayed(R.id.rg_crawl_status, growthDevelopment.optString("crawlStatus"), R.id.rb_crawl_normal, R.id.rb_crawl_delayed);
        setNormalDelayed(R.id.rg_walk_status, growthDevelopment.optString("walkStatus"), R.id.rb_walk_normal, R.id.rb_walk_delayed);
        setNormalDelayed(R.id.rg_vocalization_status, growthDevelopment.optString("vocalizationStatus"), R.id.rb_vocalization_normal, R.id.rb_vocalization_delayed);
        setNormalDelayed(R.id.rg_single_word_status, growthDevelopment.optString("singleWordStatus"), R.id.rb_single_word_normal, R.id.rb_single_word_delayed);
        setNormalDelayed(R.id.rg_phrase_status, growthDevelopment.optString("phraseStatus"), R.id.rb_phrase_normal, R.id.rb_phrase_delayed);

        JSONObject diagnosedDisorders = optObject(backgroundInfo, "diagnosedDisorders");
        setChecked(R.id.cb_disorder_none, diagnosedDisorders.optBoolean("none"));
        setChecked(R.id.cb_disorder_developmental_delay, diagnosedDisorders.optBoolean("developmentalDelay"));
        setChecked(R.id.cb_disorder_cerebral_palsy, diagnosedDisorders.optBoolean("cerebralPalsy"));
        setChecked(R.id.cb_disorder_autism, diagnosedDisorders.optBoolean("autism"));
        setChecked(R.id.cb_disorder_down_syndrome, diagnosedDisorders.optBoolean("downSyndrome"));
        setChecked(R.id.cb_disorder_intellectual_disability, diagnosedDisorders.optBoolean("intellectualDisability"));
        setChecked(R.id.cb_disorder_adhd, diagnosedDisorders.optBoolean("adhd"));
        setChecked(R.id.cb_disorder_other, diagnosedDisorders.optBoolean("other"));
        setText(R.id.et_disorder_other, diagnosedDisorders.optString("otherText"));

        JSONObject generalDevelopment = optObject(backgroundInfo, "generalDevelopment");
        setNormalAbnormal(R.id.rg_vision_status, generalDevelopment.optString("visionStatus"), R.id.rb_vision_normal, R.id.rb_vision_abnormal);
        setNormalAbnormal(R.id.rg_hearing_status, generalDevelopment.optString("hearingStatus"), R.id.rb_hearing_normal, R.id.rb_hearing_abnormal);
        setNormalAbnormal(R.id.rg_eating_habit_status, generalDevelopment.optString("eatingHabitStatus"), R.id.rb_eating_habit_normal, R.id.rb_eating_habit_abnormal);
        setChecked(R.id.cb_eating_habit_chewing_difficulty, generalDevelopment.optBoolean("eatingHabitChewingDifficulty"));
        setChecked(R.id.cb_eating_habit_swallowing_difficulty, generalDevelopment.optBoolean("eatingHabitSwallowingDifficulty"));

        JSONObject speechMotorFunction = optObject(backgroundInfo, "speechMotorFunction");
        setNormalAbnormal(R.id.rg_lips_status, speechMotorFunction.optString("lipsStatus"), R.id.rb_lips_normal, R.id.rb_lips_abnormal);
        setNormalAbnormal(R.id.rg_tongue_status, speechMotorFunction.optString("tongueStatus"), R.id.rb_tongue_normal, R.id.rb_tongue_abnormal);
        setNormalAbnormal(R.id.rg_jaw_status, speechMotorFunction.optString("jawStatus"), R.id.rb_jaw_normal, R.id.rb_jaw_abnormal);
        setNormalAbnormal(R.id.rg_velopharyngeal_status, speechMotorFunction.optString("velopharyngealStatus"), R.id.rb_velopharyngeal_normal, R.id.rb_velopharyngeal_abnormal);
        setNormalAbnormal(R.id.rg_alternating_motion_status, speechMotorFunction.optString("alternatingMotionStatus"), R.id.rb_alternating_motion_normal, R.id.rb_alternating_motion_abnormal);
        setNormalAbnormal(R.id.rg_saliva_control_status, speechMotorFunction.optString("salivaControlStatus"), R.id.rb_saliva_control_normal, R.id.rb_saliva_control_abnormal);
        setNormalAbnormal(R.id.rg_breathing_status, speechMotorFunction.optString("breathingStatus"), R.id.rb_breathing_normal, R.id.rb_breathing_abnormal);
        setNormalAbnormal(R.id.rg_voice_status, speechMotorFunction.optString("voiceStatus"), R.id.rb_voice_normal, R.id.rb_voice_abnormal);
        setNormalAbnormal(R.id.rg_speech_eating_status, speechMotorFunction.optString("speechEatingStatus"), R.id.rb_speech_eating_normal, R.id.rb_speech_eating_abnormal);
        setChecked(R.id.cb_speech_eating_chewing_difficulty, speechMotorFunction.optBoolean("speechEatingChewingDifficulty"));
        setChecked(R.id.cb_speech_eating_swallowing_difficulty, speechMotorFunction.optBoolean("speechEatingSwallowingDifficulty"));
        setText(R.id.et_speech_other, speechMotorFunction.optString("other"));

        JSONObject expressionMode = optObject(backgroundInfo, "expressionMode");
        setChecked(R.id.cb_expression_spoken_language, expressionMode.optBoolean("spokenLanguage"));
        setChecked(R.id.cb_expression_nonverbal, expressionMode.optBoolean("nonverbal"));
        setChecked(R.id.cb_nonverbal_voice_pitch, expressionMode.optBoolean("nonverbalVoicePitch"));
        setChecked(R.id.cb_nonverbal_body_language, expressionMode.optBoolean("nonverbalBodyLanguage"));
        setChecked(R.id.cb_nonverbal_assistive_device, expressionMode.optBoolean("nonverbalAssistiveDevice"));
        setText(R.id.et_nonverbal_assistive_device, expressionMode.optString("nonverbalAssistiveDeviceText"));

        JSONObject languageConcern = optObject(backgroundInfo, "languageConcern");
        setYesNo(R.id.rg_vocab_by_two_years, languageConcern.optString("vocabByTwoYears"), R.id.rb_vocab_by_two_years_yes, R.id.rb_vocab_by_two_years_no);
        setYesNo(R.id.rg_sentence_by_two_half_years, languageConcern.optString("sentenceByTwoHalfYears"), R.id.rb_sentence_by_two_half_years_yes, R.id.rb_sentence_by_two_half_years_no);
        setChecked(R.id.cb_parent_concern_normal, languageConcern.optBoolean("parentConcernNormal"));
        setChecked(R.id.cb_parent_concern_cannot_speak, languageConcern.optBoolean("parentConcernCannotSpeak"));
        setChecked(R.id.cb_parent_concern_unclear_speech, languageConcern.optBoolean("parentConcernUnclearSpeech"));
        setChecked(R.id.cb_parent_concern_cannot_understand, languageConcern.optBoolean("parentConcernCannotUnderstand"));
        setChecked(R.id.cb_parent_concern_slow_response, languageConcern.optBoolean("parentConcernSlowResponse"));
        setText(R.id.et_parent_primary_request, languageConcern.optString("parentPrimaryRequest"));

        persistedMedicalDocumentImages.clear();
        persistedMedicalDocumentImages.addAll(MedicalDiagnosisImageHelper.toObjectList(
                backgroundInfo.optJSONArray(MedicalDiagnosisImageHelper.FIELD_MEDICAL_DOCUMENTS)));
        medicalDocumentImages.clear();
        medicalDocumentImages.addAll(MedicalDiagnosisImageHelper.cloneList(persistedMedicalDocumentImages));
        deletedMedicalDocumentPaths.clear();
        refreshMedicalDocumentsUi();
    }

    private void saveForm() {
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(this, "缺少儿童记录。", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject safeChildData = childData == null ? new JSONObject() : childData;
            JSONObject info = ensureObject(safeChildData, "info");
            JSONObject backgroundInfo = ensureObject(info, "backgroundInfo");

            JSONObject basicCare = new JSONObject();
            basicCare.put("caregiver0To3Parents", isChecked(R.id.cb_caregiver_0_3_parents));
            basicCare.put("caregiver0To3Grandparents", isChecked(R.id.cb_caregiver_0_3_grandparents));
            basicCare.put("caregiver0To3Nanny", isChecked(R.id.cb_caregiver_0_3_nanny));
            basicCare.put("caregiver0To3Other", isChecked(R.id.cb_caregiver_0_3_other));
            basicCare.put("caregiver0To3OtherText", getFieldText(R.id.et_caregiver_0_3_other));
            basicCare.put("language0To3", getFieldText(R.id.et_language_0_3));
            basicCare.put("caregiver3To6Parents", isChecked(R.id.cb_caregiver_3_6_parents));
            basicCare.put("caregiver3To6Grandparents", isChecked(R.id.cb_caregiver_3_6_grandparents));
            basicCare.put("caregiver3To6Nanny", isChecked(R.id.cb_caregiver_3_6_nanny));
            basicCare.put("caregiver3To6Other", isChecked(R.id.cb_caregiver_3_6_other));
            basicCare.put("caregiver3To6OtherText", getFieldText(R.id.et_caregiver_3_6_other));
            basicCare.put("language3To6", getFieldText(R.id.et_language_3_6));
            basicCare.put("dialect", getFieldText(R.id.et_dialect));
            backgroundInfo.put("basicCare", basicCare);

            JSONObject birthHistory = new JSONObject();
            birthHistory.put("deliveryMethod", getDeliveryMethod());
            birthHistory.put("otitisMedia", isChecked(R.id.cb_birth_otitis_media));
            birthHistory.put("respiratoryDisease", isChecked(R.id.cb_birth_respiratory_disease));
            birthHistory.put("headInjury", isChecked(R.id.cb_birth_head_injury));
            birthHistory.put("epilepsy", isChecked(R.id.cb_birth_epilepsy));
            birthHistory.put("lowWeight", isChecked(R.id.cb_birth_low_weight));
            birthHistory.put("lowWeightBelow2000", isChecked(R.id.cb_birth_low_weight_below_2000));
            birthHistory.put("lowWeightBelow1500", isChecked(R.id.cb_birth_low_weight_below_1500));
            birthHistory.put("incubator", isChecked(R.id.cb_birth_incubator));
            birthHistory.put("incubatorDays", getFieldText(R.id.et_birth_incubator_days));
            birthHistory.put("jaundice", isChecked(R.id.cb_birth_jaundice));
            birthHistory.put("meningitis", isChecked(R.id.cb_birth_meningitis));
            birthHistory.put("cleftLipPalate", isChecked(R.id.cb_birth_cleft_lip_palate));
            birthHistory.put("umbilicalCordNeck", isChecked(R.id.cb_birth_umbilical_cord_neck));
            birthHistory.put("hypoxia", isChecked(R.id.cb_birth_hypoxia));
            birthHistory.put("medication", isChecked(R.id.cb_birth_medication));
            birthHistory.put("medicationText", getFieldText(R.id.et_birth_medication));
            birthHistory.put("other", isChecked(R.id.cb_birth_other));
            birthHistory.put("otherText", getFieldText(R.id.et_birth_other));
            backgroundInfo.put("birthHistory", birthHistory);

            JSONObject growthDevelopment = new JSONObject();
            growthDevelopment.put("feedingMethod", getFeedingMethod());
            growthDevelopment.put("smileStatus", getNormalDelayed(R.id.rg_smile_status, R.id.rb_smile_normal, R.id.rb_smile_delayed));
            growthDevelopment.put("sitStatus", getNormalDelayed(R.id.rg_sit_status, R.id.rb_sit_normal, R.id.rb_sit_delayed));
            growthDevelopment.put("headControlStatus", getNormalDelayed(R.id.rg_head_control_status, R.id.rb_head_control_normal, R.id.rb_head_control_delayed));
            growthDevelopment.put("crawlStatus", getNormalDelayed(R.id.rg_crawl_status, R.id.rb_crawl_normal, R.id.rb_crawl_delayed));
            growthDevelopment.put("walkStatus", getNormalDelayed(R.id.rg_walk_status, R.id.rb_walk_normal, R.id.rb_walk_delayed));
            growthDevelopment.put("vocalizationStatus", getNormalDelayed(R.id.rg_vocalization_status, R.id.rb_vocalization_normal, R.id.rb_vocalization_delayed));
            growthDevelopment.put("singleWordStatus", getNormalDelayed(R.id.rg_single_word_status, R.id.rb_single_word_normal, R.id.rb_single_word_delayed));
            growthDevelopment.put("phraseStatus", getNormalDelayed(R.id.rg_phrase_status, R.id.rb_phrase_normal, R.id.rb_phrase_delayed));
            backgroundInfo.put("growthDevelopment", growthDevelopment);

            JSONObject diagnosedDisorders = new JSONObject();
            diagnosedDisorders.put("none", isChecked(R.id.cb_disorder_none));
            diagnosedDisorders.put("developmentalDelay", isChecked(R.id.cb_disorder_developmental_delay));
            diagnosedDisorders.put("cerebralPalsy", isChecked(R.id.cb_disorder_cerebral_palsy));
            diagnosedDisorders.put("autism", isChecked(R.id.cb_disorder_autism));
            diagnosedDisorders.put("downSyndrome", isChecked(R.id.cb_disorder_down_syndrome));
            diagnosedDisorders.put("intellectualDisability", isChecked(R.id.cb_disorder_intellectual_disability));
            diagnosedDisorders.put("adhd", isChecked(R.id.cb_disorder_adhd));
            diagnosedDisorders.put("other", isChecked(R.id.cb_disorder_other));
            diagnosedDisorders.put("otherText", getFieldText(R.id.et_disorder_other));
            backgroundInfo.put("diagnosedDisorders", diagnosedDisorders);

            JSONObject generalDevelopment = new JSONObject();
            generalDevelopment.put("visionStatus", getNormalAbnormal(R.id.rg_vision_status, R.id.rb_vision_normal, R.id.rb_vision_abnormal));
            generalDevelopment.put("hearingStatus", getNormalAbnormal(R.id.rg_hearing_status, R.id.rb_hearing_normal, R.id.rb_hearing_abnormal));
            generalDevelopment.put("eatingHabitStatus", getNormalAbnormal(R.id.rg_eating_habit_status, R.id.rb_eating_habit_normal, R.id.rb_eating_habit_abnormal));
            generalDevelopment.put("eatingHabitChewingDifficulty", isChecked(R.id.cb_eating_habit_chewing_difficulty));
            generalDevelopment.put("eatingHabitSwallowingDifficulty", isChecked(R.id.cb_eating_habit_swallowing_difficulty));
            backgroundInfo.put("generalDevelopment", generalDevelopment);

            JSONObject speechMotorFunction = new JSONObject();
            speechMotorFunction.put("lipsStatus", getNormalAbnormal(R.id.rg_lips_status, R.id.rb_lips_normal, R.id.rb_lips_abnormal));
            speechMotorFunction.put("tongueStatus", getNormalAbnormal(R.id.rg_tongue_status, R.id.rb_tongue_normal, R.id.rb_tongue_abnormal));
            speechMotorFunction.put("jawStatus", getNormalAbnormal(R.id.rg_jaw_status, R.id.rb_jaw_normal, R.id.rb_jaw_abnormal));
            speechMotorFunction.put("velopharyngealStatus", getNormalAbnormal(R.id.rg_velopharyngeal_status, R.id.rb_velopharyngeal_normal, R.id.rb_velopharyngeal_abnormal));
            speechMotorFunction.put("alternatingMotionStatus", getNormalAbnormal(R.id.rg_alternating_motion_status, R.id.rb_alternating_motion_normal, R.id.rb_alternating_motion_abnormal));
            speechMotorFunction.put("salivaControlStatus", getNormalAbnormal(R.id.rg_saliva_control_status, R.id.rb_saliva_control_normal, R.id.rb_saliva_control_abnormal));
            speechMotorFunction.put("breathingStatus", getNormalAbnormal(R.id.rg_breathing_status, R.id.rb_breathing_normal, R.id.rb_breathing_abnormal));
            speechMotorFunction.put("voiceStatus", getNormalAbnormal(R.id.rg_voice_status, R.id.rb_voice_normal, R.id.rb_voice_abnormal));
            speechMotorFunction.put("speechEatingStatus", getNormalAbnormal(R.id.rg_speech_eating_status, R.id.rb_speech_eating_normal, R.id.rb_speech_eating_abnormal));
            speechMotorFunction.put("speechEatingChewingDifficulty", isChecked(R.id.cb_speech_eating_chewing_difficulty));
            speechMotorFunction.put("speechEatingSwallowingDifficulty", isChecked(R.id.cb_speech_eating_swallowing_difficulty));
            speechMotorFunction.put("other", getFieldText(R.id.et_speech_other));
            backgroundInfo.put("speechMotorFunction", speechMotorFunction);

            JSONObject expressionMode = new JSONObject();
            expressionMode.put("spokenLanguage", isChecked(R.id.cb_expression_spoken_language));
            expressionMode.put("nonverbal", isChecked(R.id.cb_expression_nonverbal));
            expressionMode.put("nonverbalVoicePitch", isChecked(R.id.cb_nonverbal_voice_pitch));
            expressionMode.put("nonverbalBodyLanguage", isChecked(R.id.cb_nonverbal_body_language));
            expressionMode.put("nonverbalAssistiveDevice", isChecked(R.id.cb_nonverbal_assistive_device));
            expressionMode.put("nonverbalAssistiveDeviceText", getFieldText(R.id.et_nonverbal_assistive_device));
            backgroundInfo.put("expressionMode", expressionMode);

            JSONObject languageConcern = new JSONObject();
            languageConcern.put("vocabByTwoYears", getYesNo(R.id.rg_vocab_by_two_years, R.id.rb_vocab_by_two_years_yes, R.id.rb_vocab_by_two_years_no));
            languageConcern.put("sentenceByTwoHalfYears", getYesNo(R.id.rg_sentence_by_two_half_years, R.id.rb_sentence_by_two_half_years_yes, R.id.rb_sentence_by_two_half_years_no));
            languageConcern.put("parentConcernNormal", isChecked(R.id.cb_parent_concern_normal));
            languageConcern.put("parentConcernCannotSpeak", isChecked(R.id.cb_parent_concern_cannot_speak));
            languageConcern.put("parentConcernUnclearSpeech", isChecked(R.id.cb_parent_concern_unclear_speech));
            languageConcern.put("parentConcernCannotUnderstand", isChecked(R.id.cb_parent_concern_cannot_understand));
            languageConcern.put("parentConcernSlowResponse", isChecked(R.id.cb_parent_concern_slow_response));
            languageConcern.put("parentPrimaryRequest", getFieldText(R.id.et_parent_primary_request));
            backgroundInfo.put("languageConcern", languageConcern);

            saveMedicalDocumentsState(backgroundInfo);

            dataManager.getInstance().saveChildJson(fName, safeChildData);
            childData = safeChildData;
            setResult(RESULT_OK);
            refreshMedicalDocumentsUi();
            Toast.makeText(this, "保存成功。", Toast.LENGTH_SHORT).show();
            setEditingMode(false);
        } catch (Exception e) {
            Toast.makeText(this, "保存失败。", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMedicalDocumentsState(JSONObject backgroundInfo) throws JSONException {
        backgroundInfo.put(
                MedicalDiagnosisImageHelper.FIELD_MEDICAL_DOCUMENTS,
                MedicalDiagnosisImageHelper.toJsonArray(medicalDocumentImages)
        );
        for (String path : deletedMedicalDocumentPaths) {
            if (!TextUtils.isEmpty(path)) {
                MedicalDiagnosisImageHelper.deleteFileQuietly(new File(path));
            }
        }
        persistedMedicalDocumentImages.clear();
        persistedMedicalDocumentImages.addAll(MedicalDiagnosisImageHelper.cloneList(medicalDocumentImages));
        deletedMedicalDocumentPaths.clear();
    }

    private void handleSelectedPhotos(List<Uri> uris) {
        int addedCount = 0;
        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }
            try {
                JSONObject imageInfo = MedicalDiagnosisImageHelper.copyImageToAppStorage(this, uri, fName);
                medicalDocumentImages.add(imageInfo);
                addedCount++;
            } catch (Exception ignored) {
            }
        }
        refreshMedicalDocumentsUi();
        if (addedCount > 0) {
            Toast.makeText(this, "已追加 " + addedCount + " 张图片，保存后生效。", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "图片处理失败，请重试。", Toast.LENGTH_SHORT).show();
        }
    }
    private void launchCamera() {
        try {
            currentCameraPhotoFile = MedicalDiagnosisImageHelper.createImageFile(this, fName, ".jpg");
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".file_provider",
                    currentCameraPhotoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            currentCameraPhotoFile = null;
            Toast.makeText(this, "无法打开相机，请稍后重试。", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeMedicalDocumentAt(int position) {
        if (!isEditing || position < 0 || position >= medicalDocumentImages.size()) {
            return;
        }
        JSONObject removed = medicalDocumentImages.remove(position);
        String localPath = MedicalDiagnosisImageHelper.getLocalPath(removed);
        if (wasPersistedMedicalDocument(localPath)) {
            deletedMedicalDocumentPaths.add(localPath);
        } else {
            MedicalDiagnosisImageHelper.deleteImageFile(removed);
        }
        refreshMedicalDocumentsUi();
    }

    private boolean wasPersistedMedicalDocument(String localPath) {
        if (TextUtils.isEmpty(localPath)) {
            return false;
        }
        for (JSONObject item : persistedMedicalDocumentImages) {
            if (localPath.equals(MedicalDiagnosisImageHelper.getLocalPath(item))) {
                return true;
            }
        }
        return false;
    }

    private void refreshMedicalDocumentsUi() {
        medicalDocumentImageAdapter.setItems(MedicalDiagnosisImageHelper.cloneList(medicalDocumentImages), isEditing);
        updateMedicalDocumentsHint();
    }

    private void setEditingMode(boolean editing) {
        isEditing = editing;
        setFormEditable(editing);
        updateActionButtons();
        refreshMedicalDocumentsUi();
    }

    private void updateActionButtons() {
        btnEdit.setEnabled(!isEditing);
        btnSave.setEnabled(isEditing);
        btnEdit.setAlpha(isEditing ? 0.6f : 1f);
        btnSave.setAlpha(isEditing ? 1f : 0.6f);
        btnBack.setEnabled(true);
    }

    private void setFormEditable(boolean editable) {
        applyEditableState(formRoot, editable);
    }

    private void applyEditableState(View view, boolean editable) {
        if (view == null) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_back || id == R.id.btn_edit_child_detail || id == R.id.btn_save_child_detail) {
            return;
        }
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setEnabled(editable);
            editText.setFocusable(editable);
            editText.setFocusableInTouchMode(editable);
            editText.setClickable(editable);
        } else if (view instanceof CheckBox || view instanceof RadioButton) {
            view.setEnabled(editable);
        } else if (id == R.id.btn_choose_medical_documents || id == R.id.btn_take_medical_documents) {
            view.setEnabled(editable);
            view.setAlpha(editable ? 1f : 0.6f);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyEditableState(group.getChildAt(i), editable);
            }
        }
    }

    private void updateMedicalDocumentsHint() {
        int count = medicalDocumentImages.size();
        if (count <= 0) {
            tvMedicalDocumentsEmpty.setText("暂无已上传的医学诊断资料图片");
            tvMedicalDocumentsEmpty.setVisibility(View.VISIBLE);
        } else {
            tvMedicalDocumentsEmpty.setText("已保存 " + count + " 张医学诊断资料图片");
            tvMedicalDocumentsEmpty.setVisibility(View.GONE);
        }
    }

    private void setDeliveryMethod(String value) {
        if ("natural".equals(value)) {
            checkRadio(R.id.rg_delivery_method, R.id.rb_delivery_natural);
        } else if ("premature".equals(value)) {
            checkRadio(R.id.rg_delivery_method, R.id.rb_delivery_premature);
        } else if ("cesarean".equals(value)) {
            checkRadio(R.id.rg_delivery_method, R.id.rb_delivery_cesarean);
        } else if ("major_illness".equals(value)) {
            checkRadio(R.id.rg_delivery_method, R.id.rb_delivery_major_illness);
        }
    }

    private String getDeliveryMethod() {
        int checkedId = ((RadioGroup) findViewById(R.id.rg_delivery_method)).getCheckedRadioButtonId();
        if (checkedId == R.id.rb_delivery_natural) {
            return "natural";
        }
        if (checkedId == R.id.rb_delivery_premature) {
            return "premature";
        }
        if (checkedId == R.id.rb_delivery_cesarean) {
            return "cesarean";
        }
        if (checkedId == R.id.rb_delivery_major_illness) {
            return "major_illness";
        }
        return "";
    }

    private void setFeedingMethod(String value) {
        if ("breast".equals(value)) {
            checkRadio(R.id.rg_feeding_method, R.id.rb_feeding_breast);
        } else if ("formula".equals(value)) {
            checkRadio(R.id.rg_feeding_method, R.id.rb_feeding_formula);
        }
    }

    private String getFeedingMethod() {
        int checkedId = ((RadioGroup) findViewById(R.id.rg_feeding_method)).getCheckedRadioButtonId();
        if (checkedId == R.id.rb_feeding_breast) {
            return "breast";
        }
        if (checkedId == R.id.rb_feeding_formula) {
            return "formula";
        }
        return "";
    }

    private void setNormalDelayed(@IdRes int groupId, String value, @IdRes int normalId, @IdRes int delayedId) {
        if ("normal".equals(value)) {
            checkRadio(groupId, normalId);
        } else if ("delayed".equals(value)) {
            checkRadio(groupId, delayedId);
        }
    }

    private String getNormalDelayed(@IdRes int groupId, @IdRes int normalId, @IdRes int delayedId) {
        int checkedId = ((RadioGroup) findViewById(groupId)).getCheckedRadioButtonId();
        if (checkedId == normalId) {
            return "normal";
        }
        if (checkedId == delayedId) {
            return "delayed";
        }
        return "";
    }

    private void setNormalAbnormal(@IdRes int groupId, String value, @IdRes int normalId, @IdRes int abnormalId) {
        if ("normal".equals(value)) {
            checkRadio(groupId, normalId);
        } else if ("abnormal".equals(value)) {
            checkRadio(groupId, abnormalId);
        }
    }

    private String getNormalAbnormal(@IdRes int groupId, @IdRes int normalId, @IdRes int abnormalId) {
        int checkedId = ((RadioGroup) findViewById(groupId)).getCheckedRadioButtonId();
        if (checkedId == normalId) {
            return "normal";
        }
        if (checkedId == abnormalId) {
            return "abnormal";
        }
        return "";
    }

    private void setYesNo(@IdRes int groupId, String value, @IdRes int yesId, @IdRes int noId) {
        if ("yes".equals(value)) {
            checkRadio(groupId, yesId);
        } else if ("no".equals(value)) {
            checkRadio(groupId, noId);
        }
    }

    private String getYesNo(@IdRes int groupId, @IdRes int yesId, @IdRes int noId) {
        int checkedId = ((RadioGroup) findViewById(groupId)).getCheckedRadioButtonId();
        if (checkedId == yesId) {
            return "yes";
        }
        if (checkedId == noId) {
            return "no";
        }
        return "";
    }

    private void checkRadio(@IdRes int groupId, @IdRes int childId) {
        RadioGroup radioGroup = findViewById(groupId);
        radioGroup.check(childId);
    }

    private void setChecked(@IdRes int id, boolean checked) {
        CheckBox checkBox = findViewById(id);
        checkBox.setChecked(checked);
    }

    private boolean isChecked(@IdRes int id) {
        CheckBox checkBox = findViewById(id);
        return checkBox.isChecked();
    }

    private void setText(@IdRes int id, @Nullable String value) {
        EditText editText = findViewById(id);
        editText.setText(value == null ? "" : value);
    }

    private String getFieldText(@IdRes int id) {
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }

    private JSONObject ensureObject(JSONObject parent, String key) throws JSONException {
        JSONObject object = parent.optJSONObject(key);
        if (object == null) {
            object = new JSONObject();
            parent.put(key, object);
        }
        return object;
    }

    private JSONObject optObject(JSONObject parent, String key) {
        if (parent == null) {
            return new JSONObject();
        }
        JSONObject object = parent.optJSONObject(key);
        return object == null ? new JSONObject() : object;
    }
}
