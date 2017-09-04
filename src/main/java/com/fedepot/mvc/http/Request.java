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


package com.fedepot.mvc.http;

import com.fedepot.exception.NotImplementException;
import com.fedepot.mvc.route.RouteManager;
import com.fedepot.mvc.route.Router;
import com.fedepot.mvc.route.PathParameter;
import com.fedepot.server.SessionHandler;
import com.fedepot.util.HttpKit;
import com.fedepot.util.MimeKit;
import com.fedepot.util.UrlKit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.AsciiString;
import lombok.NonNull;
import lombok.Setter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fedepot.mvc.http.HttpHeaderNames.*;

/**
 * Http Request
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class Request {

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private ChannelHandlerContext channelCxt;

    private FullHttpRequest fullHttpRequest;

    private SessionHandler sessionHandler;

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
     * Parsed body data, default to null
     * if Content-Type is application/json, use {@link com.razor.mvc.annotation.FromBody} deserialize rawBody to a certain type
     * if Content-Type is application/x-www-form-urlencoded {@see Request.formParams}
     * if Content-Type is multipart/form-data {@see Request.formParams Request.files}
     */
    @Getter
    @Setter
    private Object body;

    /**
     * Raw body content
     */
    @Getter
    private ByteBuf rawBody;

    @Getter
    private String rawCookie;

    /**
     * Parsed cookie key-value pairs, default to null, use cookie-parser middleware to set it
     */
    @Getter
    @Setter
    private Map<String, String> cookies;

    /**
     * Get specified cookie value
     *
     * @param name cookie name
     * @return cookie value
     */
    public Optional<String> cookie(String name) {

        if (cookies == null) {

            return  Optional.empty();
        }

        return Optional.ofNullable(cookies.get(name));
    }

    /**
     * Add cookie
     *
     * @param cookie {@link Cookie} object
     */
    public void cookie(Cookie cookie) {

        if (cookies == null) {

            cookies = new HashMap<>();
        }

        cookies.put(cookie.getName(), cookie.getValue());
    }

    public Session session() {

        return sessionHandler.getSession(this);
    }

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
     * Request origin header
     */
    @Getter
    private String origin;


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
    private PathParameter[] pathParams;

    public PathParameter[] pathParams() {

        return pathParams;
    }

    /**
     * data items from form body
     */
    private Map<String, List<String>> formParams;

    public Map<String, List<String>> formParams() {

        return formParams;
    }

    /**
     * Url queries
     */
    private Map<String, List<String>> queries;

    public Map<String, List<String>> queries() {

        return queries;
    }

    /**
     * Form upload files
     */
    private Map<String, FormFile> files;

    public Map<String, FormFile> files() {

        return files;
    }

    /**
     * Get form file by form part name
     *
     * @param name form part name
     * @return FormFile or null
     */
    public Optional<FormFile> getFile(@NonNull String name) {

        return Optional.ofNullable(files.get(name));
    }

    /**
     * Store some data, useful for middleware
     */
    private Map<String, Object> locals;

    public Map<String, Object> locals() {

        return locals;
    }

    public void locals(String key, Object value) {

        if (locals == null) {

            locals = new HashMap<>();
        }

        locals.put(key, value);
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
        Matcher matcher = Pattern.compile("^([^:]+)(:(\\d+))?$").matcher(host);

        if (matcher.matches()) {

            hostname = matcher.group(1);
            // port = matcher.group(3);
        }

        String requestWith = headers.get("X-Requested-With");
        xhr = StringUtils.equals(requestWith, "XMLHttpRequest");

        ip = HttpKit.getIP(headers);

        method = fullHttpRequest.method().name().toUpperCase();

        String originMethod;

        if (method.equals(HttpMethod.OPTIONS)) {

            originMethod = headers.get(ACCESS_CONTROL_REQUEST_METHOD);
            if (originMethod == null) {

                originMethod = HttpMethod.POST;
            }
        } else {

            originMethod = method;
        }

        origin = headers.get(ORIGIN);

        // netty server do not implement https, use nginx forward request and implement https
        if (headers.get(X_FORWARDED_PROTO) != null && headers.get(X_FORWARDED_PROTO).toLowerCase().equals("https")) {

            protocol = "https";
        } else {

            protocol = "http";
        }

        secure = StringUtils.equals(protocol, "https");

        rawCookie = fullHttpRequest.headers().get("Cookie");

        baseUrl = fullHttpRequest.uri();

        rawBody = fullHttpRequest.content().copy();

        path = UrlKit.purgeUrlQueries(baseUrl);

        queries = UrlKit.parseQueries(baseUrl);

        isStatic = UrlKit.isStaticFile(HttpContext.app().getStatics(), path);

        if (isStatic) {

            return;
        }

        // below for non static requests

        if (!method.equals(HttpMethod.GET)) {

            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, fullHttpRequest);
            decoder.getBodyHttpDatas().forEach(this::parseBodyData);
        }

        Router router = RouteManager.getInstance(HttpContext.app()).findRoute(path, originMethod);
        if (router != null) {

            matchRoute = true;
            this.router = router;
            pathParams = router.getRouteMatcher().getParams(path);
        }
    }

    public static Request build(ChannelHandlerContext cxt, FullHttpRequest req, SessionHandler sessionHandler) {

        Request request = new Request(cxt, req);
        request.sessionHandler = sessionHandler;

        return request;
    }


    /**
     * Check if specified content type are acceptable and return the best match, empty for none match
     *
     * @param docType content type
     * @return matched content type
     */
    public String accept(String docType) {

        // TODO
        throw new NotImplementException();
    }

    /**
     * Check if specified content types are acceptable and return the best match, empty for none match
     *
     * @param docTypes content types
     * @return matched content type
     */
    public String accept(String[] docTypes) {

        // TODO
        throw new NotImplementException();
    }

    /**
     * Returns the specified HTTP request header field (case-insensitive match)
     *
     * @param field header field
     * @return field value
     */
    public String get(String field) {

        return fullHttpRequest.headers().get(field);
    }

    /**
     * Returns the specified HTTP request header field (case-insensitive match)
     *
     * @param field header field {@link AsciiString}
     * @return field value
     */
    public String get(AsciiString field) {

        return fullHttpRequest.headers().get(field);
    }


    /**
     * Parse data from http request body
     *
     * @param data {@link InterfaceHttpData}
     */
    private void parseBodyData(InterfaceHttpData data) {

        try {
            switch (data.getHttpDataType()) {

                case Attribute:
                    Attribute attribute = (Attribute)data;

                    if (this.formParams == null) {

                        this.formParams = new HashMap<>();
                    }
                    this.formParams.put(attribute.getName(), Arrays.asList(attribute.getValue()));
                    break;
                case FileUpload:
                    FileUpload fileUpload = (FileUpload)data;
                    handleFileUpload(fileUpload);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {

            log.error("Parse request form data with error", e);
        } finally {

            data.release();
        }
    }


    /**
     * Handler file upload request(Content-Type: multipart/form-data)
     *
     * @param fileUpload {@link FileUpload}
     * @throws IOException IO exception
     */
    private void handleFileUpload(FileUpload fileUpload) throws IOException {

        if (fileUpload.isCompleted()) {

            String contentType = MimeKit.of(fileUpload.getFilename());

            if (contentType == null) {

                contentType = URLConnection.guessContentTypeFromName(fileUpload.getFilename());
            }

            FormFile formFile = new FormFile(fileUpload.getName(), fileUpload.getFilename(), contentType, fileUpload.length());

            if (fileUpload.isInMemory()) {

                formFile.setData(fileUpload.getByteBuf().array());
            } else {

                formFile.setData(Files.readAllBytes(fileUpload.getFile().toPath()));
            }

            if (files == null) {

                files = new HashMap<>();
            }

            files.put(fileUpload.getName(), formFile);
        }
    }
}
