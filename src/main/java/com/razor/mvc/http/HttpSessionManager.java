/**
 * Copyright (c) 2017, Touchumind<chinash2010@gmail.com>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.razor.mvc.http;

import com.razor.Razor;
import com.razor.cache.Cache;

import lombok.extern.slf4j.Slf4j;

import static com.razor.mvc.Constants.*;

/**
 * Session manager implement
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class HttpSessionManager implements SessionManager {

    private static final String SESSION_CACHE_GROUP = "_SESSION_";

    private Cache cache;

    private int sessionTimeout;

    public HttpSessionManager(Cache cache, Razor razor) {

        this.cache = cache;
        this.sessionTimeout = razor.getEnv().getInt(ENV_KEY_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT);
    }

    @Override
    public void add(Session session) {

        cache.add(session.id(), session, sessionTimeout, SESSION_CACHE_GROUP);
    }

    @Override
    public void remove(String id) {

        cache.delete(id, SESSION_CACHE_GROUP);
    }

    @Override
    public Session get(String id) {

        try {

            return (Session)cache.get(id, SESSION_CACHE_GROUP, null);
        } catch (Exception e) {

            log.error("Get session from cache has error", e);

            return null;
        }
    }

    @Override
    public void persist() {

        // Current use ehcache which has disk persistence feature, no more work to do here
    }

    @Override
    public void restore() {

        // Current use ehcache which has disk persistence feature, no more work to do here
    }
}
