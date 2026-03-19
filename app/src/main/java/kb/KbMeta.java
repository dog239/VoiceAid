package kb;

public final class KbMeta {
    public static final KbMeta EMPTY = new KbMeta("", "");

    public final String kbVersion;
    public final String buildTime;

    public KbMeta(String kbVersion, String buildTime) {
        this.kbVersion = kbVersion == null ? "" : kbVersion;
        this.buildTime = buildTime == null ? "" : buildTime;
    }
}