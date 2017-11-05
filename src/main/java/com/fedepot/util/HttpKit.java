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


package com.fedepot.util;

import io.netty.handler.codec.http.HttpHeaders;

import static com.fedepot.mvc.http.HttpHeaderNames.*;


/**
 * Http request/response related utils
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class HttpKit {

    /**
     * Get proxy ip list from headers
     *
     * @param headers http headers
     * @return ip list
     */
    public static String[] proxyIP(HttpHeaders headers) {

        CharSequence ip = headers.get(X_FORWARDED_FOR);

        if (ip == null) {

            return new String[]{};
        }

        return ip.toString().split(",");
    }

    public static String getIP(HttpHeaders headers) {

        String[] ips = proxyIP(headers);

        if (ips.length > 0 && !"".equals(ips[0])) {

            return ips[0].split(":")[0];
        }

        CharSequence realIpChar = headers.get(X_REAL_IP);

        if (realIpChar != null) {

            String[] realIp = realIpChar.toString().split(":");

            if (realIp.length > 0 && !"[".equals(realIp[0])) {

                return realIp[0];
            }
        }

        return "127.0.0.1";
    }
}
