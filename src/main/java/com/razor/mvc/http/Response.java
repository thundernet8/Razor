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

import com.razor.exception.NotImplementException;
import com.razor.mvc.Constants;
import com.razor.server.ProgressiveFutureListener;
import com.razor.util.DateKit;

import com.razor.util.MimeKit;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static com.razor.mvc.http.HttpHeaderNames.*;

/**
 * Http Request
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class Response {

    private ChannelHandlerContext channelCxt;

    private boolean keepAlive = true;

    /**
     * Indicate the response has been flushed or not(which also means headers has been sent)
     */
    private boolean flushed = false;

    public boolean flushed() {

        return flushed;
    }

    /**
     * Reference of http request
     */
    private Request request;

    /**
     * Server http response
     */
    private HttpResponse httpResponse;

    /**
     * The real response sent by netty server
     *
     * @param response netty defined http response
     */
    private void setHttpResponse(HttpResponse response) {

        httpResponse = response;

        Iterator<Map.Entry<AsciiString, AsciiString>> iterator = headerQueue.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<AsciiString, AsciiString> entry = iterator.next();
            AsciiString field = entry.getKey();
            AsciiString value = entry.getValue();

            if (!field.isEmpty() && !value.isEmpty()) {

                httpResponse.headers().set(entry.getKey(), entry.getValue());
            }

            iterator.remove();
        }
    }

    /**
     * In order to add headers when httpResponse instance is not prepared
     */
    private Map<AsciiString, AsciiString> headerQueue = new HashMap<>();

    /**
     * Http response status, include status code and cause message
     */
    private HttpResponseStatus status;

    public HttpResponseStatus getStatus() {

        if (status == null) {
            return OK;
        }

        return status;
    }

    private Response(ChannelHandlerContext cxt, Request req, FullHttpResponse res) {

        this.channelCxt = cxt;
        this.request = req;
        this.httpResponse = res;

        if (req != null && !req.keepAlive()) {

            keepAlive = false;
        }
        // TODO

    }

    public static Response build(ChannelHandlerContext cxt, Request req, FullHttpResponse res) {

        return new Response(cxt, req, res);
    }

    public static Response build(ChannelHandlerContext cxt, Request req) {

        return new Response(cxt, req, null);
    }

    /**
     * Set response header
     *
     * @param field header field, ref {@link HttpHeaderNames}
     * @param value header value, ref {@link HttpHeaderValues}
     * @return Response self
     */
    public Response header(AsciiString field, AsciiString value) {

        if (httpResponse == null) {

            headerQueue.put(field, value);
        } else {

            httpResponse.headers().set(field, value);
        }

        return this;
    }

    /**
     * Set response header
     *
     * @param field header field, ref {@link HttpHeaderNames}
     * @param value header value
     */
    public Response header(AsciiString field, String value) {

        return header(field, new AsciiString(value));
    }

    /**
     * Set response header
     *
     * @param field header field, ref {@link HttpHeaderNames}
     * @param value header value
     * @return Response self
     */
    public Response header(String field, String value) {

        return header(new AsciiString(field), new AsciiString(value));
    }

    /**
     * Search specified header field for response
     *
     * @param field http response header field
     * @return field value
     */
    public String get(AsciiString field) {

        if (httpResponse != null) {

            return httpResponse.headers().get(field);
        }

        AsciiString value = headerQueue.get(field);

        if (value != null && !value.isEmpty()) {

            return value.toString();
        }

        return null;
    }

    /**
     * Search specified header field for response
     *
     * @param field http response header field
     * @return field value
     */
    public String get(String field) {

        return get(new AsciiString(field));
    }

    /**
     * Set Content-Disposition to attachment, and the the resource will have a download behaviour when be requested
     */
    public Response attachment() {

        header(CONTENT_DISPOSITION, "attachment");

        return this;
    }

    /**
     * Set Content-Disposition to attachment, mime type also be auto-detected and set
     *
     * @param filePath file path
     * @return Response self
     */
    public Response attachment(String filePath) {

        int index = filePath.lastIndexOf("/");
        String filename = index > -1 ? filePath.substring(index+1) : filePath;
        String mime = MimeKit.of(filename);

        header(CONTENT_DISPOSITION, "attachment; filename=".concat(filename));
        header(CONTENT_TYPE, mime);

        return this;
    }

    /**
     * Set cookie
     *
     * @param name cookie name
     * @param value cookie value
     * @return Response self
     */
    public Response cookie(String name, String value) {

        return cookie(name, value, null);
    }

    /**
     * Set cookie
     *
     * @param name cookie name
     * @param value cookie value
     * @param options cookie options (options key now support `domain`, `expires`, `httpOnly`, `maxAge`, `path`, `secure`, `sameSite`)
     * @return Response self
     */
    public Response cookie(String name, String value, Map<String, Object> options) {

        StringBuilder sb = new StringBuilder(name);
        sb.append("=").append(value).append(";");

        if (options != null) {

            Object domain = options.get("domain");
            if (domain != null) {

                sb.append(" Domain=").append(domain.toString()).append(";");
            }

            Object expires = options.get("expires");
            if (expires != null) {

                sb.append(" Expires=").append(expires.toString()).append(";");
            }

            Object maxAge = options.get("maxAge");
            if (maxAge != null) {

                sb.append(" Max-Age=").append(maxAge.toString()).append(";");
            }

            Object path = options.get("path");
            if (path != null) {

                sb.append(" Path=").append(path.toString()).append(";");
            }

            Object secure = options.get("secure");
            if (secure != null && (boolean)secure) {

                sb.append(" Secure;");
            }

            Object httpOnly = options.get("httpOnly");
            if (httpOnly != null && (boolean)httpOnly) {

                sb.append(" HttpOnly;");
            }

            Object sameSite = options.get("sameSite");
            if (sameSite != null && (boolean)sameSite) {

                sb.append(" SameSite=Strict;");
            }
        }

        header(SET_COOKIE, sb.toString());

        return this;
    }

    public Response clearCookie(String name) {

        Map<String, Object> options = new HashMap<>();
        options.put("expires", DateKit.getGmtDateString(new Date(0)));

        if (httpResponse != null) {

            httpResponse.headers().remove(name);
        } else {

            headerQueue.remove(name);
        }

        return cookie(name, "", options);
    }

    /**
     * Set http response status code and reason
     *
     * @param statusCode http status code
     * @return Response self
     */
    public Response status(int statusCode) {

        this.status = HttpResponseStatus.valueOf(statusCode);

        return this;
    }

    public void setDate() {

        header(DATE, DateKit.getGmtDateString());
    }

    public void setPowerBy() {

        header(SERVER, "Netty");
        header(X_POWER_BY, "Razor");
    }

    /**
     * Send json response
     *
     * @param data data to send
     */
    public void json(Object data) {

        // TODO
    }

    /**
     * Redirect
     *
     * @param path new path to visit
     */
    public void redirect(String path) {

        redirect(path, 302);
    }

    /**
     * Redirect
     *
     * @param path new path to visit
     * @param code response code
     */
    public void redirect(String path, int code) {

        keepAlive = false;

        header(LOCATION, path);

        if (code < 300 || code >= 400) {

            code = 302;
        }

        status = HttpResponseStatus.valueOf(code);

        end();
    }

    public void location(String path) {

        // TODO location to another path with refer header
    }

    /**
     * Specify content-type
     *
     * @param type mime short name
     * @return Response self
     */
    public Response type(String type) {

        String mime = MimeKit.get(type);

        if (mime != null) {

            header(CONTENT_TYPE, mime);
        }

        return this;
    }

    /**
     * Set vary response header
     *
     * @param field vary value
     * @return Response self
     */
    public Response vary(String field) {

        String vary = get(VARY);

        if (vary != null) {

            header(VARY, vary.concat(", ").concat(field));
        } else {

            header(VARY, field);
        }

        return this;
    }

    public void send(Object data) {

        // TODO send multi-type data
        throw new NotImplementException();
    }

    /**
     * Send specified http response status and status code as response text
     *
     * @param code http status code
     */
    public void sendStatus(int code) {

        status(code).end(Integer.toString(code));
    }

    /**
     * Send response immediately when error occurs
     * @param status Http response status including code and cause message
     */
    public void sendError(HttpResponseStatus status) {

        this.status = status;

        setHttpResponse(new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(status.toString(), CharsetUtil.UTF_8)));
        header(CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT);

        writeFlush(true);
    }

    /**
     * Send response immediately for a file response
     * @param raf RandomAccessFile
     */
    public void sendFile(RandomAccessFile raf, long length) {

        this.status = HttpResponseStatus.OK;

        setDate();
        setPowerBy();

        setHttpResponse(new DefaultHttpResponse(HTTP_1_1, status, true));

        // Write initial line and headers
        channelCxt.write(httpResponse);
        // Write content
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;

        if (false /* if has ssl handler */) {

            // TODO support ssl
        } else {

            sendFileFuture = channelCxt.write(new DefaultFileRegion(raf.getChannel(), 0, length), channelCxt.newProgressivePromise());
            lastContentFuture = channelCxt.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }

        sendFileFuture.addListener(new ProgressiveFutureListener(raf));

        if (!keepAlive) {

            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        flush();
    }


    /**
     * Write and flush channel context
     *
     * @param close whether close http connection
     */
    private void writeFlush(boolean close) {

        if (flushed()) {

            return;
        }

        setDate();
        setPowerBy();

        if (close) {

            channelCxt.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        } else {

            header(CONNECTION, "keep-alive");
            channelCxt.writeAndFlush(httpResponse);
        }

        flush();
    }

    private void flush() {

        flushed = true;

        request = null;
        httpResponse = null;
    }

    /**
     * End the response immediately
     */
    public void end() {

        if (httpResponse == null) {

            setHttpResponse(new DefaultFullHttpResponse(HTTP_1_1, getStatus()));
        }

        writeFlush(!keepAlive);
    }

    /**
     * End the response immediately
     *
     * @param data data to send
     * @param options more options, specify the second arg of a valid encoding option, e.g `gzip`, `deflate`
     */
    public void end(String data, String[]... options) {

        // TODO encoding
        header(CONTENT_LENGTH, Integer.toString(data.length()));

        if (httpResponse == null) {

            setHttpResponse(new DefaultFullHttpResponse(
                    HTTP_1_1,
                    getStatus(),
                    Unpooled.copiedBuffer(data, CharsetUtil.UTF_8)
            ));
        }

        writeFlush(!keepAlive);
    }

    /**
     * End the response immediately
     *
     * @param data bytes data to send
     * @param options more options, specify the second arg of a valid encoding option, e.g `gzip`, `deflate`
     */
    public void end(byte[] data, String[]... options) {

        setHttpResponse(new DefaultFullHttpResponse(
                HTTP_1_1,
                getStatus(),
                Unpooled.copiedBuffer(data)
        ));

        header(CONTENT_LENGTH, Integer.toString(data.length));

        writeFlush(!keepAlive);
    }

}
