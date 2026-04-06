package utils.net;

import android.app.Activity;

/**
 * utils/NetServiceProvider.java 统一选择 Stub/Network，实现可切换。
 */
public final class NetServiceProvider {
    private static final boolean USE_STUB = false;

    private NetServiceProvider() {
    }

    public static NetService get(Activity activity) {
        if (USE_STUB) {
            return new NetServiceStub(activity);
        }
        return new NetServiceNet(activity);
    }
}
