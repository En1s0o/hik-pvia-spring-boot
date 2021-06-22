package io.en1s0o.common.cookie.cache;

import okhttp3.Cookie;
import okhttp3.internal.annotations.EverythingIsNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * 默认的 cookie 缓存
 *
 * @author En1s0o
 */
@EverythingIsNonNull
public class DefaultCookieCache implements CookieCache {

    private final Set<IdentifiableCookie> cookies;

    public DefaultCookieCache() {
        cookies = new HashSet<>();
    }

    @Override
    public void addAll(Collection<Cookie> newCookies) {
        for (IdentifiableCookie cookie : IdentifiableCookie.decorateAll(newCookies)) {
            cookies.remove(cookie);
            cookies.add(cookie);
        }
    }

    @Override
    public Iterator<Cookie> iterator() {
        return new SetCookieCacheIterator();
    }

    private class SetCookieCacheIterator implements Iterator<Cookie> {

        private final Iterator<IdentifiableCookie> iterator;

        private SetCookieCacheIterator() {
            Objects.requireNonNull(cookies);
            iterator = cookies.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next().getCookie();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

    }

}
