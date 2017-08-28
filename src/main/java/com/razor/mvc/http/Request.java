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
import com.razor.mvc.route.RouteManager;
import com.razor.mvc.route.Router;
import com.razor.mvc.route.RouteParameter;
import com.razor.util.UrlKit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;
import lombok.Setter;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Http Request
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class Request {

    private ChannelHandlerContext channelCxt;

    public ChannelHandlerContext serverContext() {

        return channelCxt;
    }

    private FullHttpRequest fullHttpRequest;

    public static Razor app;

    /**
     * Full url including query strings
     */
    @Getter
    private String baseUrl;

    private String path;

    public String path() {

        return path;
    }

    /**
     * Parsed body data, default to null, use body-parse middleware to set it
     */
    @Getter
    @Setter
    private Object body;

    @Getter
    private String originCookie;

    /**
     * Parsed cookie key-value pairs, default to null, use cookie-parse middleware to set it
     */
    @Getter
    @Setter
    public Map<String, String> cookies;

    // TODO basic auth infos in header

    // TODO cache-control
    private boolean fresh;

    /**
     * Hostname from Host HTTP header, eg. Host: "example.com:3000" -> hostname: "example.com"
     */
    @Getter
    private String hostname;

    /**
     * HTTP Host header
     */
    @Getter
    private String host;

    /**
     * remote IP address of the request
     */
    @Getter
    private String ip;

    /**
     * http or https
     */
    @Getter
    private String protocol;

    public String protocol() {

        return protocol;
    }

    private boolean secure;

    /**
     * `request.protocol equals to https`
     * @return true for ssl connection, otherwise false
     */
    public boolean secure() {

        return secure;
    }

    /**
     * HTTP method
     */
    private String method;

    public String method() {

        return method;
    }

    /**
     * Request match a controller action
     */
    private boolean matchRoute = false;

    public boolean matchRoute() {

        return matchRoute;
    }

    /**
     * Current request matched route
     */
    private Router router;

    public Router router() {

        return router;
    }

    /**
     * url match route properties, e.g you have a route /book/:name, the `name` would be one url parameter
     */
    private RouteParameter[] params;

    public RouteParameter[] params() {

        return params;
    }

    /**
     * Url query key-value pairs
     */
    private UrlQuery[] queries;

    public UrlQuery[] queries() {

        return queries;
    }

    /**
     * Indicate request is from an ajax call or not
     */
    private boolean xhr;

    public boolean xhr() {

        return xhr;
    }

    /**
     * Indicate this request retrieving a static file
     */
    private boolean isStatic;

    public boolean isStatic() {

        return isStatic;
    }

    /**
     * Indicate http connection keep alive
     * @return true for keep alive connection
     */
    public boolean keepAlive() {

        return HttpUtil.isKeepAlive(fullHttpRequest);
    }

    private Request(ChannelHandlerContext channelCxt, FullHttpRequest fullHttpRequest) {

        this.channelCxt = channelCxt;
        this.fullHttpRequest = fullHttpRequest;

        HttpHeaders headers = fullHttpRequest.headers();
        host = headers.get("Host");
        Matcher matcher = Pattern.compile("^([^:]+)(:(\\\\d+))?$").matcher(host);

        if (matcher.find()) {

            hostname = matcher.group(1);
            // port = matcher.group(3);
        }

        String requestWith = headers.get("X-Requested-With");
        xhr = StringUtils.equals(requestWith, "XMLHttpRequest");

        method = fullHttpRequest.method().name().toUpperCase();

//        protocol = fullHttpRequest.protocolVersion() // TODO

        secure = StringUtils.equals(protocol, "https");

        originCookie = fullHttpRequest.headers().get("Cookie");

        baseUrl = fullHttpRequest.uri();

        path = UrlKit.purgeUrlQueries(baseUrl);

        queries = UrlKit.parseQueries(baseUrl);

        isStatic = UrlKit.isStaticFile(app.getStatics(), path);

        if (isStatic) {

            return;
        }

        // below for non static requests
        Router router = RouteManager.getInstance(app.getAppClass()).findRoute(path, method);
        if (router != null) {

            matchRoute = true;
            this.router = router;
            params = router.getRouteMatcher().getParams(path);
        }
    }

    public static Request build(ChannelHandlerContext cxt, FullHttpRequest req) {

        return new Request(cxt, req);
    }


    /**
     * Check if specified content type are acceptable and return the best match, empty for none match
     * @param docType content type
     * @return matched content type
     */
    public String accept(String docType) {

        // TODO
        return "";
    }

    /**
     * Check if specified content types are acceptable and return the best match, empty for none match
     * @param docTypes content types
     * @return matched content type
     */
    public String accept(String[] docTypes) {

        // TODO
        return "";
    }

    /**
     * Returns the specified HTTP request header field (case-insensitive match)
     * @param field header field
     * @return field value
     */
    public String get(String field) {

        return fullHttpRequest.headers().get(field);
    }

    /**
     * Returns the specified HTTP request header field (case-insensitive match)
     * @param field header field {@link AsciiString}
     * @return field value
     */
    public String get(AsciiString field) {

        return fullHttpRequest.headers().get(field);
    }
}
