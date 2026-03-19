package kb;

import java.util.List;

public interface KbRepository {
    List<KbStrategy> query(String module, String tag, int topK);
}
