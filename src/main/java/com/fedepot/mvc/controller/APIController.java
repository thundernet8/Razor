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


package com.fedepot.mvc.controller;

import com.fedepot.exception.HttpException;
import com.fedepot.mvc.annotation.RoutePrefix;
import com.fedepot.mvc.http.*;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import static com.fedepot.mvc.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * Abstract controller specified for API actions
 */
@Slf4j
@RoutePrefix
public class APIController implements IController {

    protected HttpContext Context() {

        return HttpContext.get();
    }

    protected Request Request() {

        return HttpContext.request();
    }

    protected  Response Response() {

        return HttpContext.response();
    }

    /**
     * Send a json response immediately
     *
     * @param json data to send
     */
    protected void JSON(Object json) {

        Response response = Response();
        response.header(CONTENT_TYPE, ContentType.JSON.getMimeTypeWithCharset());
        response.end(ActionResult.build(json, json.getClass()).getBytes());
    }

    /**
     * Send a successfully response
     *
     * @param data data to send
     */
    protected void Succeed(Object data) {

        Response().status(200);
        JSON(data);
    }

    /**
     * Send a failed response
     *
     * @param e exception
     */
    protected void Fail(Exception e) {

        int code = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
        String msg = e.getMessage();

        if (e instanceof HttpException) {

            code = ((HttpException) e).getCode();
        }

        log.error(msg, e);

        Response().status(code);
        JSON(msg);
    }
}
