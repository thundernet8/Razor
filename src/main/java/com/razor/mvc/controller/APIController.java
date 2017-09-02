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


package com.razor.mvc.controller;

import com.razor.mvc.annotation.RoutePrefix;
import com.razor.mvc.http.ActionResult;
import com.razor.mvc.http.ContentType;
import com.razor.mvc.http.HttpContext;

import com.razor.mvc.http.Response;
import lombok.extern.slf4j.Slf4j;

import static com.razor.mvc.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * Abstract controller specified for API actions
 */
@Slf4j
@RoutePrefix
public class APIController implements IController {

    private HttpContext httpContext;

    protected HttpContext Context() {

        return httpContext;
    }

    /**
     * Send a json response immediately
     *
     * @param json data to send
     */
    protected void JSON(Object json) {

        Response response = Context().response();
        response.header(CONTENT_TYPE, ContentType.JSON.getMimeTypeWithCharset());
        response.end(ActionResult.build(json, json.getClass()).getBytes());
    }
}
