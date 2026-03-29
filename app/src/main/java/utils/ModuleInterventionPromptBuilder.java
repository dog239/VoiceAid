package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ModuleInterventionPromptBuilder {
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private ModuleInterventionPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return "你是儿童言语语言治疗干预报告助手。只输出严格合法 JSON，不要 Markdown。"
                + "必须使用简体中文值，JSON key 保持英文。";
    }

    public static String buildUserPrompt(String moduleType, JSONObject childData) throws JSONException {
        JSONObject moduleInput = buildModuleInput(moduleType, childData);
        return buildUserPromptFromModuleInput(moduleType, moduleInput, "");
    }

    public static String buildUserPromptWithRag(String moduleType,
                                                JSONObject childData,
                                                String ragContext) throws JSONException {
        if (isBlank(ragContext)) {
            return buildUserPrompt(moduleType, childData);
        }
        JSONObject moduleInput = buildModuleInput(moduleType, childData);
        return buildUserPromptFromModuleInput(moduleType, moduleInput, ragContext);
    }

    /**
     * Current single-module LLM input fields:
     * - moduleType/moduleTitle/subtypes: route and schema identity.
     * - ageMonths/chiefComplaint: case context kept in prompt only.
     * - moduleEvaluations.summary: stable structured summary, preferred for RAG query extraction.
     * - moduleEvaluations raw arrays: still passed to the model for grounding, not used as retrieval text.
     * - existingModuleReport: historical report content kept in prompt only.
     *
     * Privacy rule for RAG:
     * name/address/phone/familyMembers/chiefComplaint/free-text history must not enter retrieval.
     */
    public static JSONObject buildModuleInput(String moduleType, JSONObject childData) throws JSONException {
        String safeModuleType = ModuleReportHelper.normalizeModuleType(moduleType);
        String moduleTitle = ModuleReportHelper.moduleTitle(safeModuleType);
        JSONArray subtypes = ModuleReportHelper.defaultSubtypes(safeModuleType);
        JSONObject info = childData == null ? null : childData.optJSONObject("info");
        JSONObject evaluations = childData == null ? null : childData.optJSONObject("evaluations");

        int ageMonths = calculateAgeMonths(
                info == null ? "" : info.optString("birthDate", ""),
                info == null ? "" : info.optString("testDate", ""));
        if (ageMonths < 0) {
            ageMonths = 0;
        }

        JSONObject moduleInput = new JSONObject();
        moduleInput.put("moduleType", safeModuleType);
        moduleInput.put("moduleTitle", moduleTitle);
        moduleInput.put("subtypes", subtypes);
        moduleInput.put("ageMonths", ageMonths);
        moduleInput.put("chiefComplaint", info == null ? "" : safe(info.optString("chiefComplaint", "")));
        moduleInput.put("moduleEvaluations", selectModuleEvaluations(safeModuleType, evaluations));
        moduleInput.put("existingModuleReport", loadCurrentModuleReport(childData, safeModuleType));
        return moduleInput;
    }

    private static String buildUserPromptFromModuleInput(String moduleType,
                                                         JSONObject moduleInput,
                                                         String ragContext) throws JSONException {
        String safeModuleType = ModuleReportHelper.normalizeModuleType(moduleType);
        String moduleTitle = ModuleReportHelper.moduleTitle(safeModuleType);
        JSONArray subtypes = ModuleReportHelper.defaultSubtypes(safeModuleType);
        JSONObject schema = ModuleInterventionGuideSchema.createDefault(safeModuleType, moduleTitle, subtypes);

        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下模块评测结构化数据，生成该模块的干预报告 JSON。");
        prompt.append("必须只输出 JSON，不要解释文本。\n");
        prompt.append("要求：\n");
        prompt.append("1) 输出字段必须与 schema 完全一致。\n");
        prompt.append("2) 字段允许为空，但结构必须稳定。\n");
        prompt.append("3) smartGoal.cycleWeeks 为正整数，accuracyThreshold 为 0~1 小数。\n");
        prompt.append("4) meta.reviewStatus 初始为 draft，reviewedByTherapist 初始为 false。\n");
        prompt.append("5) 输出中不要包含姓名、电话、地址等隐私信息。\n");
        if (!isBlank(ragContext)) {
            prompt.append("6) 以本次个案测评结果为主，检索知识仅作参考。\n");
            prompt.append("7) 不得根据检索知识编造本次未测得的事实。\n");
            prompt.append("8) 即使使用检索知识，输出也必须严格符合下方 JSON schema。\n");
        }

        if ("articulation".equals(safeModuleType)) {
            appendArticulationPrompt(prompt);
        } else if ("syntax".equals(safeModuleType)) {
            appendSyntaxPrompt(prompt);
        } else if ("vocabulary".equals(safeModuleType)) {
            appendVocabularyPrompt(prompt);
        } else if ("social".equals(safeModuleType)) {
            appendSocialPrompt(prompt);
        } else if ("prelinguistic".equals(safeModuleType)) {
            appendPrelinguisticPrompt(prompt);
        }

        prompt.append("\n输入数据:\n").append(moduleInput.toString()).append("\n");
        if (!isBlank(ragContext)) {
            prompt.append("\nRAG 参考知识:\n").append(ragContext).append("\n");
        }
        prompt.append("\n输出 schema:\n").append(schema.toString()).append("\n");
        return prompt.toString();
    }

    private static void appendArticulationPrompt(StringBuilder prompt) {
        prompt.append("9) [Articulation] This is an articulation / speech-sound production intervention report. Focus on phoneme production, target consonants or vowels, word-position stability, multisyllabic-word retention, and carryover to connected speech when the data supports it.\n");
        prompt.append("10) [Articulation] overallSummary must compare articulation performance with age/development expectations, clearly stating that articulation is not yet fully age-appropriate and that some speech sounds show clear difficulty or unstable production.\n");
        prompt.append("11) [Articulation] Structure the content to map closely to this report style: overall assessment, mastered abilities, not-mastered overview, priority focus abilities, unstable abilities, SMART goals, and home guidance.\n");
        prompt.append("12) [Articulation] mastered[] should name concrete observable speech-sound abilities already relatively stable, such as stable production of certain vowels, early consonants, or target sounds in specific word positions. Do not use vague statements only.\n");
        prompt.append("13) [Articulation] notMasteredOverview should summarize both not-yet-established and not-yet-stable articulation weaknesses, especially difficulties tied to complex place/manner of articulation, specific target sounds, word positions, multisyllabic words, or connected speech carryover when supported by the data.\n");
        prompt.append("14) [Articulation] focus[] should prioritize the most important target sounds or production abilities that repeatedly fail across attempts, such as later-developing consonants, difficult word positions, or keeping the target sound in multisyllabic words. Keep the wording concrete.\n");
        prompt.append("15) [Articulation] unstable[] should describe abilities that have appeared but remain inconsistent across word positions, lexical items, multisyllabic words, or higher speech-load conditions. Make the instability explicit.\n");
        prompt.append("16) [Articulation] smartGoal.text must contain 1 to 3 articulation-training goals. Prefer goals such as: within 8-10 weeks, with visual and auditory cues, correctly produce target sounds at word or phrase level, reach at least 80% accuracy, and stay stable across two consecutive evaluations. If data supports it, distinguish word, phrase, and connected-speech levels.\n");
        prompt.append("17) [Articulation] homeGuidance[] should be directly usable at home: provide slow and clear speech models, embed target sounds in games or daily routines, reinforce accurate productions naturally, repeat target sounds in real contexts, and avoid frequent correction or high-pressure imitation demands.\n");
        prompt.append("18) [Articulation] Do not write this as a vocabulary, syntax, social, or prelinguistic report. Stay focused on speech-sound production rather than general language ability. If connected-speech evidence is weak, say so conservatively instead of inventing carryover performance.\n");
    }

    private static void appendSyntaxPrompt(StringBuilder prompt) {
        prompt.append("9) [Syntax] This is one combined syntax assessment module report. Analyze RG (comprehension) and SE (expression) together and output one unified syntax intervention guide.\n");
        prompt.append("10) [Syntax] overallSummary must describe both syntax comprehension and syntax expression against age/development expectations, clearly stating that some grammatical structures are not yet stable in understanding or expression and still need support.\n");
        prompt.append("11) [Syntax] Use sections that map closely to this report style: overall assessment, mastered abilities, not-mastered overview, priority focus abilities, unstable abilities, SMART goals, and home guidance.\n");
        prompt.append("12) [Syntax] mastered[] should name concrete syntax structures already understood or expressed, such as subject-predicate, verb-object, simple questions, adjective+noun combinations, spatial structures, temporal-order sentences, or other observed structures.\n");
        prompt.append("13) [Syntax] notMasteredOverview should be a concise overall paragraph summarizing structures that are not yet stably understood or expressed, especially complex questions, passive, comparative, causal, adversative, double negative, conditional, spatial, and temporal-order structures when supported by data.\n");
        prompt.append("14) [Syntax] focus[] should prioritize the most important not-yet-established syntax structures and make the training priority obvious. When possible, cover both comprehension and expression difficulties in the same structure family.\n");
        prompt.append("15) [Syntax] unstable[] should describe abilities that have appeared but are still inconsistent, especially structures that may work in simple contexts but break down when sentence pattern, context, or response demand changes.\n");
        prompt.append("16) [Syntax] smartGoal.text must contain 1 to 3 concrete, trainable, observable goals tied to syntax structures. Prefer goals such as understanding and expressing target sentence patterns with picture/context support, measurable accuracy, or a clear number of correct productions/responses within a session.\n");
        prompt.append("17) [Syntax] homeGuidance[] should be activity-based and directly executable: model target sentence patterns in daily conversation, ask questions with picture books or life scenes, run two-step instruction games, use cards/objects for sentence imitation and expansion, and ask parents to record progress for therapist feedback.\n");
        prompt.append("18) [Syntax] Keep the tone as a formal syntax assessment intervention report. Do not write it as only comprehension, only expression, or a vague language-delay report. Stay focused on syntax structure types rather than abstract ability labels.\n");
    }

    private static void appendVocabularyPrompt(StringBuilder prompt) {
        prompt.append("9) [Vocabulary] This is one unified vocabulary intervention report. The input may contain receptive vocabulary, expressive vocabulary, and supporting task results such as RE, S, and NWR. Treat all of them as combined evidence inside the same vocabulary module and output only one merged report.\n");
        prompt.append("10) [Vocabulary] First synthesize the combined vocabulary summary, then describe one overall vocabulary profile. Never split the output into a receptive report and an expressive report, and never write RE / S / NWR as independent module reports.\n");
        prompt.append("11) [Vocabulary] Focus on receptive vocabulary, expressive vocabulary, the balance or imbalance between understanding and output, lexical retrieval / naming, vocabulary size, semantic understanding, and whether performance drops when task load increases.\n");
        prompt.append("12) [Vocabulary] Use RE, S, and NWR only as supporting signals for vocabulary processing, language retention, phonological load, or task-demand sensitivity. They can help explain why vocabulary performance becomes unstable, but they must not become the main training line by themselves.\n");
        prompt.append("13) [Vocabulary] overallSummary must clearly state whether receptive vocabulary is relatively stronger, expressive vocabulary is relatively stronger, or both still need support. If understanding is better than expression, explicitly say input is relatively better than output. If expression is better than understanding, explicitly say so as well.\n");
        prompt.append("14) [Vocabulary] Structure the content to map closely to this report style: overall assessment, mastered abilities, not-mastered overview, priority focus abilities, unstable abilities, SMART goals, and home guidance.\n");
        prompt.append("15) [Vocabulary] mastered[] should summarize relatively stable vocabulary abilities such as understanding common nouns, action words, adjectives, category concepts, or expressing familiar high-frequency words. Prefer ability-level wording over mechanically listing every item.\n");
        prompt.append("16) [Vocabulary] notMasteredOverview should summarize weak or not-yet-stable abilities, especially weak semantic understanding, weak naming/retrieval, poor expressive access, mismatch between comprehension and expression, and vocabulary breakdown when memory or task load rises.\n");
        prompt.append("17) [Vocabulary] focus[] should prioritize the most important vocabulary targets. If basic receptive vocabulary is weak, do not make complex expressive training the main focus. Distinguish receptive goals, expressive goals, and vocabulary-generalization goals when needed, but keep them inside one unified report.\n");
        prompt.append("18) [Vocabulary] unstable[] should describe emerging or load-sensitive abilities, such as understanding better than naming, naming better than semantic selection, partial success within lexical classes, or reduced stability under repetition, memory, or phonological-demand tasks.\n");
        prompt.append("19) [Vocabulary] smartGoal.text must contain 2 to 4 short-cycle, observable, trainable vocabulary goals. Goals should fit the child's current level: receptive weakness -> listening / selecting / pointing goals; expressive weakness -> naming / describing / functional-expression goals; imbalance -> bridge receptive knowledge into expressive use; load-sensitive weakness -> improve stability within limited task demands first.\n");
        prompt.append("20) [Vocabulary] homeGuidance[] must be caregiver-friendly and daily-life based: repeated exposure to high-frequency words, naming in routines, picture/object matching, action-word practice in real activities, category sorting, functional requesting/labeling, and short repeated opportunities to generalize vocabulary across home situations.\n");
        prompt.append("21) [Vocabulary] Do not write this as an articulation, syntax, social, or prelinguistic report. Do not collapse the report into a generic language-delay statement, and do not frame RE / S / NWR as separate training modules.\n");
        prompt.append("22) [Vocabulary] If RAG reference knowledge is provided, use it only as professional background. The case summary is the primary evidence, and you must not invent findings that do not appear in the measured vocabulary summary.\n");
    }

    private static void appendSocialPrompt(StringBuilder prompt) {
        prompt.append("9) [Social] This is a social-pragmatic intervention report. Use only the SOCIAL items that were actually measured this time. Never mention unmeasured questions or unselected groups.\n");
        prompt.append("10) [Social] Infer measured groups from the provided social summary and measured question numbers. The report must stay within the ability level of those measured groups instead of using a fixed age template or a full 60-item conclusion.\n");
        prompt.append("11) [Social] Map the report structure closely to: overall assessment, mastered abilities, not-mastered overview, priority focus abilities, unstable abilities, SMART goals, and home guidance.\n");
        prompt.append("12) [Social] mastered[] must be built from score=2 abilities only. focus[] must be built from score=0 abilities only. unstable[] must be built from score=1 abilities only. Use the original ability point names or very close merged wording, deduplicate similar items, and keep developmental order by question number.\n");
        prompt.append("13) [Social] overallSummary must summarize only the measured groups. Use careful professional wording such as overall performance being relatively strong within the measured range, or several abilities still being unstable and needing support. Avoid absolute statements.\n");
        prompt.append("14) [Social] notMasteredOverview should be a short paragraph summarizing the still-weak abilities seen in score=0 and score=1 items, emphasizing interaction foundation, peer interaction, rules, cooperation, communication repair, or negotiation according to the measured groups.\n");
        prompt.append("15) [Social] smartGoal.text must contain 2 to 4 concrete, trainable, observable goals chosen from score=0 and score=1 abilities. Match the goal style to the measured groups: early groups focus on joint attention, imitation, turn-taking, initiation, and social motivation; middle groups focus on peer play, sharing, rules, cooperation, and help-seeking; later groups focus on conversation, empathy, problem solving, communication repair, negotiation, and group decision making.\n");
        prompt.append("16) [Social] homeGuidance[] must contain 3 to 5 directly executable family activities aligned with the measured groups, such as face-to-face turn-taking games, greeting routines, simple instruction games, peer cooperation games, role play, story discussion, problem-solving talk, misunderstanding repair practice, or negotiation routines.\n");
        prompt.append("17) [Social] Do not write a generic language-delay report. Focus on social ability points from the measured items themselves: initiation, response, joint attention, turn-taking, rule-following, cooperation, peer interaction, emotion understanding, help-seeking, communication repair, negotiation, and related observed skills.\n");
    }

    private static void appendPrelinguisticPrompt(StringBuilder prompt) {
        prompt.append("9) [Prelinguistic] This is one unified prelinguistic / early-communication intervention report. If the input contains PL, PL_A, PL_B, or other scene-based observations, treat them as different observation scenes inside the same prelinguistic module and output only one merged report.\n");
        prompt.append("10) [Prelinguistic] First synthesize the merged prelinguistic summary across scenes, then describe one overall profile. Never split the output into scene A / scene B reports, and never write separate recommendations for each scene as if they were different modules.\n");
        prompt.append("11) [Prelinguistic] Focus on communication foundations before formal language: attention and participation, joint attention / shared attention, imitation, response to sound or language stimulation, communicative intent, early interaction quality, play participation, and caregiver-supported generalization.\n");
        prompt.append("12) [Prelinguistic] If performance differs by scene, partner, support level, or activity type, explicitly summarize this as scene-dependent / context-dependent performance. Do not pretend the child is stable across all settings when the summary suggests inconsistency.\n");
        prompt.append("13) [Prelinguistic] overallSummary must clearly state whether the child's prelinguistic and early communication foundation is still unstable for developmental expectations, and should summarize both relative strengths and scene-dependent weaknesses in one integrated paragraph.\n");
        prompt.append("14) [Prelinguistic] mastered[] should list observable interaction strengths already present, such as better attention to people, responding to adult bids, brief joint attention, imitation, turn-taking, interest in shared routines, or communication attempts under caregiver support.\n");
        prompt.append("15) [Prelinguistic] notMasteredOverview should summarize the still-weak or not-yet-stable abilities, especially weak initiation, inconsistent shared attention, reduced response to sound/language stimulation, limited interaction maintenance, limited communicative intent, poor transfer across scenes, or heavy dependence on prompts/support.\n");
        prompt.append("16) [Prelinguistic] focus[] should prioritize foundational intervention targets, especially attention and engagement, joint attention, imitation, communicative intent, response consistency, early interaction quality, and generalization across daily scenes. If the foundation is weak, do not make advanced vocabulary or syntax training the main focus.\n");
        prompt.append("17) [Prelinguistic] unstable[] should describe abilities that appear in some scenes but not others, or only appear with adult scaffolding. Make the scene dependence, prompt dependence, or partner dependence explicit.\n");
        prompt.append("18) [Prelinguistic] smartGoal.text must contain 1 to 3 short-cycle, observable, behavior-based goals. Goals should fit early interaction, such as initiating interaction, sustaining joint attention, responding to name or language stimulation, imitating actions/sounds, or completing several turns in familiar routines with caregiver support.\n");
        prompt.append("19) [Prelinguistic] homeGuidance[] should be short, high-frequency, daily-life, caregiver-friendly suggestions: face-to-face routines, imitation games, pause-and-wait interaction, shared book/picture routines, object-sharing, simple turn-taking, following the child's interest, and timely reinforcement of communication attempts.\n");
        prompt.append("20) [Prelinguistic] Do not write this as an articulation, vocabulary, syntax, or formal expressive-language report. Avoid framing the plan around pronunciation accuracy, sentence complexity, or advanced language teaching when prelinguistic interaction foundations are still weak.\n");
        prompt.append("21) [Prelinguistic] If RAG reference knowledge is provided, use it only as professional background. The child's merged summary is the primary evidence, and you must not invent findings that do not appear in the case summary.\n");
    }

    private static JSONObject selectModuleEvaluations(String moduleType, JSONObject evaluations) throws JSONException {
        JSONObject out = new JSONObject();
        JSONObject source = evaluations == null ? new JSONObject() : evaluations;

        JSONObject summary = new JSONObject();
        switch (moduleType) {
            case "articulation":
                summary = ModuleReportHelper.getArticulationSummaryJson(source);
                break;
            case "syntax":
                summary = ModuleReportHelper.getSyntaxSummaryJson(source);
                break;
            case "vocabulary":
                summary = ModuleReportHelper.getVocabularySummaryJson(source);
                break;
            case "social":
                summary = ModuleReportHelper.getSocialSummaryJson(source);
                break;
            case "prelinguistic":
                summary = ModuleReportHelper.getPrelinguisticSummaryJson(source);
                break;
            default:
                break;
        }
        out.put("summary", summary);

        switch (moduleType) {
            case "articulation":
                out.put("A", cloneArray(source.optJSONArray("A")));
                break;
            case "syntax":
                out.put("RG", ModuleReportHelper.getMergedSyntaxArray(source, "RG"));
                out.put("SE", ModuleReportHelper.getMergedSyntaxArray(source, "SE"));
                break;
            case "social":
                out.put("SOCIAL", ModuleReportHelper.getMeasuredSocialArray(source.optJSONArray("SOCIAL")));
                break;
            case "prelinguistic":
                out.put("PL", ModuleReportHelper.getMergedPrelinguisticArray(source));
                break;
            case "vocabulary":
                out.put("E", cloneArray(source.optJSONArray("E")));
                out.put("EV", cloneArray(source.optJSONArray("EV")));
                out.put("receptive", ModuleReportHelper.getPrimaryVocabularyReceptiveArray(source));
                out.put("RE", cloneArray(source.optJSONArray("RE")));
                out.put("S", cloneArray(source.optJSONArray("S")));
                out.put("NWR", cloneArray(source.optJSONArray("NWR")));
                break;
            default:
                out.put("raw", source);
                break;
        }
        return out;
    }

    private static JSONObject loadCurrentModuleReport(JSONObject childData, String moduleType) {
        if (childData == null) {
            return new JSONObject();
        }
        JSONObject report = ModuleReportHelper.loadModuleReport(childData, moduleType);
        return report == null ? new JSONObject() : report;
    }

    private static JSONArray cloneArray(JSONArray source) {
        JSONArray out = new JSONArray();
        if (source == null) {
            return out;
        }
        for (int i = 0; i < source.length(); i++) {
            out.put(source.opt(i));
        }
        return out;
    }

    private static int calculateAgeMonths(String birthDate, String testDate) {
        Date birth = parseDate(birthDate);
        if (birth == null) {
            return -1;
        }
        Date test = parseDate(testDate);
        if (test == null) {
            test = new Date();
        }
        Calendar b = Calendar.getInstance();
        b.setTime(birth);
        Calendar t = Calendar.getInstance();
        t.setTime(test);
        int months = (t.get(Calendar.YEAR) - b.get(Calendar.YEAR)) * 12
                + (t.get(Calendar.MONTH) - b.get(Calendar.MONTH));
        if (t.get(Calendar.DAY_OF_MONTH) < b.get(Calendar.DAY_OF_MONTH)) {
            months -= 1;
        }
        return months;
    }

    private static Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN, Locale.US).parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
