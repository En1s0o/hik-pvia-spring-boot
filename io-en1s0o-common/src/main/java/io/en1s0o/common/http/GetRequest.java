package io.en1s0o.common.http;

import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * GET 请求
 *
 * @author En1s0o
 */
public class GetRequest extends Request {

    private final RequestBody body;

    /**
     * 构造 GET 请求
     *
     * @param url 相对或绝对路径
     */
    public GetRequest(@Nonnull String url) {
        this(url, null);
    }

    /**
     * 构造 GET 请求
     *
     * @param url     相对或绝对路径
     * @param headers 请求头
     */
    public GetRequest(@Nonnull String url, Map<String, String> headers) {
        super(url, headers);
        this.body = null;
    }

    /**
     * 构造 GET 请求，通常不会有 Body，这不是标准的 HTTP 请求
     *
     * @param url     相对或绝对路径
     * @param headers 请求头
     * @param body    请求体
     */
    public GetRequest(@Nonnull String url, Map<String, String> headers, RequestBody body) {
        super(url, headers);
        this.body = body;
    }

    /**
     * 获取请求体，在 GET 请求中，不在 HTTP 规范中
     *
     * @return 请求体
     */
    @Override
    public RequestBody getBody() {
        return body;
    }

}
