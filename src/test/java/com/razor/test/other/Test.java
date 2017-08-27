package com.razor.test.other;

import com.razor.Razor;
import com.razor.mvc.Constants;
import com.razor.test.MvcTest;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


public class Test {

    public static void main(String[] args) {
        String host = "baidu.com";
        Pattern pattern = Pattern.compile("^([^:]+)(:(\\d+))?$");
        Matcher matcher = pattern.matcher(host);
        matcher.find();
        String hostname = matcher.group(1);

        try {
            String pa = new File(Constants.class.getResource("/").getPath()).getCanonicalPath();
            System.out.println(pa);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println(File.separator);
    }
}
