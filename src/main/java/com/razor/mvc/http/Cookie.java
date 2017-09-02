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

import lombok.*;

/**
 * Cookie wrapper
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Cookie {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String value;

    @Getter
    @Setter
    private String path = "/";

    @Getter
    @Setter
    private String domain;

    @Getter
    @Setter
    private long maxAge = -1;

    @Setter
    private boolean httpOnly = false;

    public boolean isHttpOnly() {

        return httpOnly;
    }

    @Setter
    private boolean secure = false;

    public boolean isSecure() {

        return secure;
    }

    private boolean sameSite;

    public boolean isSameSite() {

        return sameSite;
    }

    public void sameSite(boolean value) {

        sameSite = value;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(name);
        sb.append("=").append(value).append(";");

        if (domain != null) {

            sb.append(" Domain=").append(domain).append(";");
        }

        sb.append(" Max-Age=").append(maxAge).append(";");

        if (path != null) {

            sb.append(" Path=").append(path).append(";");
        }

        if (secure) {

            sb.append(" Secure;");
        }

        if (httpOnly) {

            sb.append(" HttpOnly;");
        }

        if (sameSite) {

            sb.append(" SameSite=Strict;");
        }

        return sb.toString();
    }
}
