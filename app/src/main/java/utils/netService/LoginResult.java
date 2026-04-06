package utils.netService;

public final class LoginResult {
    private final String uid;
    private final String username;

    public LoginResult(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }
}
