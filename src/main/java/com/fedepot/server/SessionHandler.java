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


package com.fedepot.server;

import com.fedepot.Razor;
import com.fedepot.env.Env;
import com.fedepot.mvc.http.*;

import com.groupon.uuid.UUID;

import java.time.Instant;
import java.util.Optional;

import static com.fedepot.mvc.Constants.*;

/**
 * Session handler
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class SessionHandler {

    /**
     * Name of the cookie for saving session id
     */
    private final String sessionKey;

    /**
     * Session timeout seconds
     */
    private final int timeout;

    private SessionManager sessionManager;


    SessionHandler(Razor razor) {

        Env env = razor.getEnv();

        this.sessionKey = env.get(ENV_KEY_SESSION_KEY, DEFAULT_SESSION_KEY);
        this.timeout = env.getInt(ENV_KEY_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT);
        this.sessionManager = razor.getSessionManager();
    }

    public Session getSession(Request request) {

        Session session = null;

        Optional<String> sessionId = request.cookie(sessionKey);

        Response response = HttpContext.response();

        if (sessionId.isPresent()) {

            session = sessionManager.get(sessionId.get());
        }

        if (session != null) {

            if (session.expireAt() < Instant.now().getEpochSecond()) {

                removeSession(session, response);
            }

            session.setIsFirstTime(false);
            return session;
        }

        return createSession(request, response);
    }

    private Session createSession(Request request, Response response) {

        String sessionId = new UUID().toString();
        long now = Instant.now().getEpochSecond();
        long expires = now + timeout;

        Session session = new HttpSession(sessionId, now, expires);
        session.setIsFirstTime(true);
        sessionManager.add(session);

        Cookie cookie = Cookie.builder().name(sessionKey).value(sessionId).httpOnly(true).maxAge(timeout).build();
        request.cookie(cookie);
        response.cookie(cookie);

        return session;
    }

    private void removeSession(Session session, Response response) {

        session.attributes().clear();
        sessionManager.remove(session.id());

        response.cookie(Cookie.builder().name(session.id()).maxAge(-1).build());
    }
}
