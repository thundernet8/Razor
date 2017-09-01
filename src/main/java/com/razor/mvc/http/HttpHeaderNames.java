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


import io.netty.util.AsciiString;

/**
 * extended netty http header names, and format the letter case
 *
 * @author Touchumind
 * @since 0.0.1
 */
public final class HttpHeaderNames {

    public static final AsciiString ACCEPT = new AsciiString("Accept");

    public static final AsciiString ACCEPT_CHARSET = new AsciiString("Accept-Charset");

    public static final AsciiString ACCEPT_ENCODING = new AsciiString("Accept-Encoding");

    public static final AsciiString ACCEPT_LANGUAGE = new AsciiString("Accept-Language");

    public static final AsciiString ACCEPT_RANGES = new AsciiString("Accept-Ranges");

    public static final AsciiString ACCEPT_PATCH = new AsciiString("Accept-Patch");

    public static final AsciiString ACCESS_CONTROL_ALLOW_CREDENTIALS = new AsciiString("Access-Control-Allow-Credentials");

    public static final AsciiString ACCESS_CONTROL_ALLOW_HEADERS = new AsciiString("Access-Control-Allow-Headers");

    public static final AsciiString ACCESS_CONTROL_ALLOW_METHODS = new AsciiString("Access-Control-Allow-Methods");

    public static final AsciiString ACCESS_CONTROL_ALLOW_ORIGIN = new AsciiString("Access-Control-Allow-Origin");

    public static final AsciiString ACCESS_CONTROL_EXPOSE_HEADERS = new AsciiString("Access-Control-Expose-Headers");

    public static final AsciiString ACCESS_CONTROL_MAX_AGE = new AsciiString("Access-Control-Max-Age");

    public static final AsciiString ACCESS_CONTROL_REQUEST_HEADERS = new AsciiString("Access-Control-Request-Headers");

    public static final AsciiString ACCESS_CONTROL_REQUEST_METHOD = new AsciiString("Access-Control-Request-Method");

    public static final AsciiString AGE = new AsciiString("Age");

    public static final AsciiString ALLOW = new AsciiString("Allow");

    public static final AsciiString AUTHORIZATION = new AsciiString("Authorization");

    public static final AsciiString CACHE_CONTROL = new AsciiString("Cache-Control");

    public static final AsciiString CONNECTION = new AsciiString("Connection");

    public static final AsciiString CONTENT_BASE = new AsciiString("Content-Base");

    public static final AsciiString CONTENT_ENCODING = new AsciiString("Content-Encoding");

    public static final AsciiString CONTENT_LANGUAGE = new AsciiString("Content-Language");

    public static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");

    public static final AsciiString CONTENT_LOCATION = new AsciiString("Content-Location");

    public static final AsciiString CONTENT_TRANSFER_ENCODING = new AsciiString("Content-Transfer-Encoding");

    public static final AsciiString CONTENT_DISPOSITION = new AsciiString("Content-Disposition");

    public static final AsciiString CONTENT_MD5 = new AsciiString("Content-MD5");

    public static final AsciiString CONTENT_RANGE = new AsciiString("Content-Range");

    public static final AsciiString CONTENT_SECURITY_POLICY = new AsciiString("Content-Security-Policy");

    public static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");

    public static final AsciiString COOKIE = new AsciiString("Cookie");

    public static final AsciiString DATE = new AsciiString("Date");

    public static final AsciiString ETAG = new AsciiString("Etag");

    public static final AsciiString EXPECT = new AsciiString("Expect");

    public static final AsciiString EXPIRES = new AsciiString("Expires");

    public static final AsciiString FROM = new AsciiString("From");

    public static final AsciiString HOST = new AsciiString("Host");

    public static final AsciiString IF_MATCH = new AsciiString("If-Match");

    public static final AsciiString IF_MODIFIED_SINCE = new AsciiString("If-Modified-Since");

    public static final AsciiString IF_NONE_MATCH = new AsciiString("If-None-Match");

    public static final AsciiString IF_RANGE = new AsciiString("If-Range");

    public static final AsciiString IF_UNMODIFIED_SINCE = new AsciiString("If-Unmodified-Since");

    public static final AsciiString KEEP_ALIVE = new AsciiString("Keep-Alive");

    public static final AsciiString LAST_MODIFIED = new AsciiString("Last-Modified");

    public static final AsciiString LOCATION = new AsciiString("Location");

    public static final AsciiString MAX_FORWARDS = new AsciiString("Max-Forwards");

    public static final AsciiString ORIGIN = new AsciiString("Origin");

    public static final AsciiString PRAGMA = new AsciiString("Pragma");

    public static final AsciiString PROXY_AUTHENTICATE = new AsciiString("Proxy-Authenticate");

    public static final AsciiString PROXY_AUTHORIZATION = new AsciiString("Proxy-Authorization");

    public static final AsciiString RANGE = new AsciiString("Range");

    public static final AsciiString REFERER = new AsciiString("Referer");

    public static final AsciiString RETRY_AFTER = new AsciiString("Retry-After");

    public static final AsciiString SEC_WEBSOCKET_KEY1 = new AsciiString("Sec-Websocket-Key1");

    public static final AsciiString SEC_WEBSOCKET_KEY2 = new AsciiString("Sec-Websocket-Key2");

    public static final AsciiString SEC_WEBSOCKET_LOCATION = new AsciiString("Sec-Websocket-Location");

    public static final AsciiString SEC_WEBSOCKET_ORIGIN = new AsciiString("Sec-Websocket-Origin");

    public static final AsciiString SEC_WEBSOCKET_PROTOCOL = new AsciiString("Sec-Websocket-Protocol");

    public static final AsciiString SEC_WEBSOCKET_VERSION = new AsciiString("Sec-Websocket-Version");

    public static final AsciiString SEC_WEBSOCKET_KEY = new AsciiString("Sec-Websocket-Key");

    public static final AsciiString SEC_WEBSOCKET_ACCEPT = new AsciiString("Sec-Websocket-Accept");

    public static final AsciiString SEC_WEBSOCKET_EXTENSIONS = new AsciiString("Sec-Websocket-Extensions");

    public static final AsciiString SERVER = new AsciiString("Server");

    public static final AsciiString SET_COOKIE = new AsciiString("Set-Cookie");

    public static final AsciiString SET_COOKIE2 = new AsciiString("Set-Cookie2");

    public static final AsciiString TE = new AsciiString("Te");

    public static final AsciiString TRAILER = new AsciiString("Trailer");

    public static final AsciiString TRANSFER_ENCODING = new AsciiString("Transfer-Encoding");

    public static final AsciiString UPGRADE = new AsciiString("Upgrade");

    public static final AsciiString USER_AGENT = new AsciiString("User-Agent");

    public static final AsciiString VARY = new AsciiString("Vary");

    public static final AsciiString VIA = new AsciiString("Via");

    public static final AsciiString WARNING = new AsciiString("Warning");

    public static final AsciiString WEBSOCKET_LOCATION = new AsciiString("Websocket-Location");

    public static final AsciiString WEBSOCKET_ORIGIN = new AsciiString("Websocket-Origin");

    public static final AsciiString WEBSOCKET_PROTOCOL = new AsciiString("Websocket-Protocol");

    public static final AsciiString WWW_AUTHENTICATE = new AsciiString("WWW-Authenticate");

    public static final AsciiString X_FRAME_OPTIONS = new AsciiString("X-Frame-Options");

    public static final AsciiString X_POWERED_BY = new AsciiString("X-Powered-By");

    public static final AsciiString X_FORWARDED_FOR = new AsciiString("X-Forwarded-For");

    public static final AsciiString X_REAL_IP = new AsciiString("X-Real-IP");

    private HttpHeaderNames() { }
}
