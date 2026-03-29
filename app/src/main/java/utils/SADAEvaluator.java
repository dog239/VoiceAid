package utils;

import java.util.List;

import bean.a.CharacterPhonology;

public final class SADAEvaluator {
    private SADAEvaluator() {
    }

    public static String computeAutoErrorType(List<CharacterPhonology> targetWord,
                                              List<CharacterPhonology> answerPhonology) {
        String targetJoined = buildJoinedPhonologyString(targetWord);
        String answerJoined = buildJoinedPhonologyString(answerPhonology);
        if (targetJoined.equals(answerJoined)) {
            return null;
        }
        int targetLen = targetJoined.length();
        int answerLen = answerJoined.length();
        if (answerLen > targetLen) {
            return "增加";
        }
        if (answerLen < targetLen) {
            return "减少";
        }
        return "替代";
    }

    private static String buildJoinedPhonologyString(List<CharacterPhonology> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (CharacterPhonology cp : list) {
            if (cp == null || cp.phonology == null) continue;
            sb.append(nullToEmpty(cp.phonology.initial));
            sb.append(nullToEmpty(cp.phonology.medial));
            sb.append(nullToEmpty(cp.phonology.nucleus));
            sb.append(nullToEmpty(cp.phonology.coda));
        }
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}

