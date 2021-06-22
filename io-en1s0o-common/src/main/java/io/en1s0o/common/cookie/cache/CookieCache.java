package io.en1s0o.common.cookie.cache;

import okhttp3.Cookie;

import java.util.Collection;

/**
 * 处理 cookie 会话存储
 *
 * @author En1s0o
 */
public interface CookieCache extends Iterable<Cookie> {

    /**
     * 添加新 cookies 到会话中，已经存在的 cookies 将被覆盖。
     *
     * @param cookies 新 cookies
     */
    void addAll(Collection<Cookie> cookies);

}
