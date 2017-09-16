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

import com.fedepot.mvc.annotation.*;
import com.fedepot.mvc.controller.APIController;
import com.fedepot.mvc.http.FormFile;
import com.fedepot.mvc.http.HttpContext;
import com.fedepot.mvc.http.Request;
import com.fedepot.mvc.http.Response;
import com.fedepot.mvc.json.GsonFactory;
import com.razor.test.model.Book;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RoutePrefix("api")
public class BookController extends APIController {

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
    @Route("books/list/{string:name}")
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

        Request request = Request();
        return request.getBaseUrl();
//        return "Books list ";
    }

    @HttpPost
    @Route("books/{int:id}/update")
    public void multiParamsTest(Integer id, @FromBody Book book, @QueryParam("sort") String sort) {

        // curl -H "Content-Type: application/json" -X POST -d '{"name":"xyz","id":123,"isbn":xxx}' http://127.0.0.1:8090/api/books/199/update?sort=asc
        // curl -H "Content-Type:application/x-www-form-urlencoded" -X POST -d "name=xyz&id=123&isbn=xxx" http://127.0.0.1:8090/api/books/199/update?sort=asc
        System.out.println(Request().getQueries().get("sort"));
        System.out.println(sort);
        System.out.println(book);
        System.out.println(id);
        JSON(GsonFactory.getGson().toJson(book));
    }

    @HttpPost
    @Route("books/upload")
    public void upload() {

        // curl -F "key=key1" -F "comment=this is an txt file" -F "file1=@/Users/WXQ/Desktop/filetest.txt" http://127.0.0.1:8090/api/books/upload

        Request request = Request();
        Response response = Response();

        Map<String, List<String>> params = request.getFormParams();
        System.out.println(GsonFactory.getGson().toJson(params));

        FormFile file = request.getFile("file1").orElse(null);
        if (file != null) {
            String data = new String(file.getData(), StandardCharsets.UTF_8);
            response.end(data);
        } else {

            response.end("get form file failed");
        }
    }

}
