package io.en1s0o.common.http;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * 基本请求
 *
 * @author En1s0o
 */
public abstract class Request {

    private final String url;

    private final Map<String, String> headers;

    public Request(@Nonnull String url, Map<String, String> headers) {
        this.url = url;
        if (headers == null) {
            this.headers = new HashMap<>();
        } else {
            this.headers = new HashMap<>(headers);
        }
    }

    /**
     * 根据 host 解析 url，当 url 为完整路径时，host 不起作用。
     *
     * @param host 基 url
     * @return 完整 url
     */
    public HttpUrl resolveHttpUrl(HttpUrl host) {
        try {
            return HttpUrl.get(url);
        } catch (Exception e) {
            HttpUrl httpUrl = host.resolve(url);
            if (httpUrl == null) {
                throw new IllegalArgumentException(url);
            }
            return httpUrl;
        }
    }

    /**
     * 获取请求头
     *
     * @return 请求头
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 获取请求体
     *
     * @return 请求体
     */
    public abstract RequestBody getBody();

}
