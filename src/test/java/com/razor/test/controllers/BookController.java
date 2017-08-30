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


package com.razor.test.controllers;


import com.razor.ioc.annotation.FromService;
import com.razor.mvc.controller.APIController;
import com.razor.mvc.controller.Controller;
import com.razor.mvc.annotation.HttpGet;
import com.razor.mvc.annotation.HttpPost;
import com.razor.mvc.annotation.Route;
import com.razor.mvc.annotation.RoutePrefix;
import com.razor.mvc.http.HttpContext;
import com.razor.mvc.http.Request;
import com.razor.test.IService;

@RoutePrefix("api")
public class BookController extends APIController {

    public String defaultName = "book";

    @FromService
    public IService service;

    public BookController() {

    }


    @HttpGet
    @Route("books/{int:id}")
    public void getBookDetail(int id) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"action\": \"get\", \"id\":");
        sb.append(id);
        sb.append("}");
        JSON(sb.toString());
    }

    @HttpPost
    @Route("books/{int:id}")
    public void updateBookDetail(int id) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"action\": \"update\", \"id\":");
        sb.append(id);
        sb.append("}");
        JSON(sb.toString());
    }

    @HttpGet
    @Route("books/{string:category}/list")
    public void getCategoriedBooks(String category) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"action\": \"getlist\", \"category\":");
        sb.append("\"");
        sb.append(category);
        sb.append("\"");
        sb.append("}");
        JSON(sb.toString());
    }

    @HttpGet
    @Route("books/{string:name}")
    public void getBook(String name) {

        JSON("\"" + name + "\"");
    }

    @HttpGet
    @Route("books/list")
    public String getBooks() {

        HttpContext context = Context();

        if (context == null) {
            return "null context";
        }

        Request request = context.request();
        return request.getBaseUrl();
//        return "Books list ";
    }


}
