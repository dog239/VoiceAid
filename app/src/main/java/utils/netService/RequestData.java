package utils.netService;

import org.json.JSONObject;

public final class RequestData {
    private RequestData() {
    }

    public static String captchaPurpose(String code) {
        if (code == null) {
            return null;
        }
        switch (code.trim()) {
            case "0":
                return "login";
            case "1":
                return "register";
            case "2":
                return "reset_password";
            case "3":
                return "logoff";
            default:
                return null;
        }
    }

    public static String jsonString(JSONObject jsonObject) {
        return jsonObject == null ? "{}" : jsonObject.toString();
    }
}
