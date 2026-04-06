package utils.netService;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public final class HttpClientFactory {
    private HttpClientFactory() {
    }

    public static OkHttpClient createDefault() {
        return new OkHttpClient.Builder().build();
    }

    public static OkHttpClient createWithTimeouts(long connectSeconds, long readSeconds, long writeSeconds) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectSeconds, TimeUnit.SECONDS)
                .readTimeout(readSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeSeconds, TimeUnit.SECONDS)
                .build();
    }
}
