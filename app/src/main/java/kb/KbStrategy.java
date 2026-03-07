package kb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KbStrategy {
    public final String id;
    public final String module;
    public final List<String> skillTags;
    public final String title;
    public final String level;
    public final String text;
    public final String doNot;

    public KbStrategy(String id,
                      String module,
                      List<String> skillTags,
                      String title,
                      String level,
                      String text,
                      String doNot) {
        this.id = id;
        this.module = module;
        this.skillTags = skillTags == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(skillTags));
        this.title = title;
        this.level = level;
        this.text = text;
        this.doNot = doNot;
    }
}
