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

import com.razor.mvc.Constants;
import com.razor.server.ProgressiveFutureListener;
import com.razor.util.DateKit;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * Http Request
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class Response {

    private ChannelHandlerContext channelCxt;

    /**
     * Indicate the response has been flushed or not
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
     */
    public void header(AsciiString field, AsciiString value) {

        if (httpResponse == null) {

            headerQueue.put(field, value);
        } else {

            httpResponse.headers().set(field, value);
        }
    }

    /**
     * Set response header
     *
     * @param field header field, ref {@link HttpHeaderNames}
     * @param value header value
     */
    public void header(AsciiString field, String value) {

        header(field, new AsciiString(value));
    }

    /**
     * Set response header
     *
     * @param field header field, ref {@link HttpHeaderNames}
     * @param value header value
     */
    public void header(String field, String value) {

        header(new AsciiString(field), new AsciiString(value));
    }

    /**
     * Search specified header field for response
     *
     * @param field http response header field
     * @return field value
     */
    public String header(AsciiString field) {

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
    public String header(String field) {

        return header(new AsciiString(field));
    }

    /**
     * Set 304 response header
     */
    public void notModified() {

        setHttpResponse(new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED));
        setDate();

        writeFlush(true);
    }

    public void setDate() {

        header(DATE, DateKit.getGmtDateString());
    }

    public void setPowerBy() {

        header(SERVER, "Netty");
        header("X-Power-By", "Razor");
    }

    /**
     * Send response immediately when error occurs
     * @param status Http response status including code and cause message
     */
    public void sendError(HttpResponseStatus status) {

        this.status = status;

        setDate();

        setHttpResponse(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(status.toString(), CharsetUtil.UTF_8)));
        header(HttpHeaderNames.CONTENT_TYPE, new AsciiString(Constants.CONTENT_TYPE_TEXT));

        writeFlush(true);
    }

    /**
     * Send response immediately for a file response
     * @param raf RandomAccessFile
     */
    public void sendFile(RandomAccessFile raf, long length) {

        this.status = HttpResponseStatus.OK;

        setHttpResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, status, true));

        header("X-Power-By", "Razor");

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

        if (request == null || !request.keepAlive()) {

            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        flush();
    }

    /**
     * Send the response
     */
    public void send() {

        // TODO
    }

    public void send(String view) {

        // TODO
        setDate();

        setHttpResponse(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                getStatus(),
                Unpooled.copiedBuffer(view, CharsetUtil.UTF_8)
        ));

        header(CONTENT_LENGTH, Integer.toString(view.length()));

        writeFlush(request == null || !request.keepAlive());
    }

    public void send(byte[] data) {

        // TODO
        setDate();

        setHttpResponse(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                getStatus(),
                Unpooled.copiedBuffer(data)
        ));

        header(CONTENT_LENGTH, Integer.toString(data.length));

        writeFlush(request == null || !request.keepAlive());
    }

    /**
     * Write and flush channel context
     *
     * @param close whether close http connection
     */
    private void writeFlush(boolean close) {

        setPowerBy();

        if (close) {

            channelCxt.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        } else {

            header(CONNECTION, KEEP_ALIVE);
            channelCxt.writeAndFlush(httpResponse);
        }

        flushed = true;
    }

    private void flush() {

        flushed = true;
    }
}
