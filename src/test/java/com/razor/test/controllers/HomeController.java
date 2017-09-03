package com.razor.test.controllers;

import com.razor.mvc.controller.Controller;
import com.razor.mvc.annotation.Route;
import com.razor.mvc.http.Cookie;

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


public class HomeController extends Controller {

    @Route("")
    public void index() {

        //return "Home page";
        Object cookies = Context().request().getCookies();

        Context().response().cookie(Cookie.builder().name("xxx").maxAge(-1).build());

        Render("home.htm", "var1", "home result using beetl");
    }

    @Route("redirect")
    public void redirect() {

        Context().response().location("http://www.baidu.com/");
    }

    @Route("redirect-local")
    public void redirectLocal() {

        Context().response().location("/local");
    }

    @Route("local")
    public String local() {

        return "local";
    }

    @Route("generic/*")
    public String genericRoute() {

        return "generic";
    }
}
