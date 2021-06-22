package io.en1s0o.common.cookie.cache;

import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 这是一个 {@link Cookie} 装饰类，重写了 {@link #equals} 和 {@link #hashCode} 方法，
 * 以便通过以下属性来标识 {@link Cookie}： name, domain, path, secure & hostOnly
 * <p>
 * 对于确定何时必须重写会话中已经存在的 cookie 很有用
 *
 * @author En1s0o
 */
class IdentifiableCookie {

    private final Cookie cookie;

    static List<IdentifiableCookie> decorateAll(Collection<Cookie> cookies) {
        List<IdentifiableCookie> identifiableCookies = new ArrayList<>(cookies.size());
        for (Cookie cookie : cookies) {
            identifiableCookies.add(new IdentifiableCookie(cookie));
        }
        return identifiableCookies;
    }

    private IdentifiableCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    Cookie getCookie() {
        return cookie;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IdentifiableCookie)) {
            return false;
        }

        IdentifiableCookie that = (IdentifiableCookie) other;
        return that.cookie.name().equals(this.cookie.name())
                && that.cookie.domain().equals(this.cookie.domain())
                && that.cookie.path().equals(this.cookie.path())
                && that.cookie.secure() == this.cookie.secure()
                && that.cookie.hostOnly() == this.cookie.hostOnly();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + cookie.name().hashCode();
        hash = 31 * hash + cookie.domain().hashCode();
        hash = 31 * hash + cookie.path().hashCode();
        hash = 31 * hash + (cookie.secure() ? 0 : 1);
        hash = 31 * hash + (cookie.hostOnly() ? 0 : 1);
        return hash;
    }

}
