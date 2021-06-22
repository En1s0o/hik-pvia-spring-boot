package io.en1s0o.common.cookie;

import io.en1s0o.common.cookie.cache.CookieCache;
import io.en1s0o.common.cookie.cache.DefaultCookieCache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.internal.annotations.EverythingIsNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 在内存中的 CookieJar
 *
 * @author En1s0o
 */
@EverythingIsNonNull
public class InMemoryCookieJar implements CookieJar {

    private final CookieCache cache;

    public InMemoryCookieJar() {
        this(new DefaultCookieCache());
    }

    public InMemoryCookieJar(CookieCache cache) {
        this.cache = cache;
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();
            if (isCookieExpired(currentCookie)) {
                it.remove();
            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        return validCookies;
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

}
