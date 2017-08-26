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
import com.razor.mvc.Controller;
import com.razor.mvc.annotation.HttpGet;
import com.razor.mvc.annotation.HttpPost;
import com.razor.mvc.annotation.Route;
import com.razor.mvc.annotation.RoutePrefix;
import com.razor.test.ITest;
import com.razor.test.Service;
import com.razor.test.IService;

@RoutePrefix("shop")
public class BookController extends Controller implements ITest {

    public String defaultName = "book";

    @FromService
    public IService service;

    public BookController(Service service0) {
        System.out.println(service0.name);
        System.out.println(service0.date);
    }

    public void out() {
        System.out.println(service.getName());
        System.out.println(service.getDate());
    }

    @HttpGet
    @Route("books/{int:id}.html")
    public String getBookDetail(int id) {

        return "Book ".concat(Integer.toString(id)).concat(" detail");
    }

    @HttpPost
    @Route("books/{int:id}.html")
    public String updateBookDetail(int id) {

        return "Book ".concat(Integer.toString(id)).concat(" update");
    }

    @HttpGet
    @Route("books/{string:category}/list")
    public String getCategoriedBooks(String category) {

        return "Books list of category ".concat(category);
    }

    @HttpGet
    @Route("books/list")
    public String getBooks() {

        return "Books list of ";
    }
}
