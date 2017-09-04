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

import com.fedepot.Razor;

import io.netty.util.concurrent.FastThreadLocal;


/**
 * Http Context accessible for all actions
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class HttpContext {

    private static final FastThreadLocal<HttpContext> fastThreadLocal = new FastThreadLocal<>();

    private static Razor app;

    private Request request;

    private Response response;

    public static Request request() {

        HttpContext context = get();

        if (context != null) {

            return context.request;
        }

        return null;
    }

    public static Response response() {

        HttpContext context = get();

        if (context != null) {

            return context.response;
        }

        return null;
    }

    public static Razor app() {

        return app;
    }

    public static void init(Razor razor) {

        app = razor;
    }

    public HttpContext(Request request, Response response) {

        this.request = request;
        this.response = response;
    }

    public static void set(HttpContext context) {

        fastThreadLocal.set(context);
    }

    public static HttpContext get() {

        return fastThreadLocal.get();
    }

    public static void remove() {

        fastThreadLocal.remove();
    }
}
