package com.razor.test;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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


public class PatternTest {

    public static void main(String[] args) {
        String routePrefix = "shop";
        String route = "books/{int:id}/{name}.html";

        Pattern pattern = Pattern.compile("^([^/])([0-9a-zA-Z-_/{}:.]+)([^/])$");
        Matcher matcher = pattern.matcher(route);
        if (matcher.matches()) {
            String a = matcher.group(2);
            System.out.println("matches");
        } else {
            System.out.println("not match");
        }


        // find '{type:param}' pairs
        int start = 0;
        boolean inPair = false;
        boolean inName = false;
        ArrayList<String> pairs = new ArrayList<>();
        ArrayList<String> types = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        StringBuilder currentPair = new StringBuilder();
        StringBuilder currentParamType = new StringBuilder();
        StringBuilder currentParamName = new StringBuilder();
        StringBuilder patternBuilder = new StringBuilder("^");
        patternBuilder.append(routePrefix);
        patternBuilder.append("/");
        while (start < route.length()) {
            String ch = route.substring(start, start + 1);
            if (StringUtils.equals(ch, "{")) {
                inPair = true;
                inName = false;
                currentPair.setLength(0);
                currentParamType.setLength(0);
                currentParamName.setLength(0);
            } else if (StringUtils.equals(ch, "}")) {
                inPair = false;
                inName = false;
                pairs.add(currentPair.toString());
                if (StringUtils.isEmpty(currentParamName.toString())) {
                    types.add("String");
                    names.add(currentParamType.toString());
                    patternBuilder.append("([0-9a-zA-Z-_]+)");
                } else {
                    boolean isNumber = StringUtils.equals(currentParamType.toString().toLowerCase(), "int");
                    types.add(isNumber ? "int" : "String");
                    names.add(currentParamName.toString());
                    patternBuilder.append(isNumber ? "([0-9]+)" : "([0-9a-zA-Z-_]+)");
                }
            } else if (StringUtils.equals(ch, ":")) {
                inName = true;
                currentPair.append(ch);
            } else if (inPair) {
                currentPair.append(ch);
                if (inName) {
                    currentParamName.append(ch);
                } else {
                    currentParamType.append(ch);
                }
            } else {
                patternBuilder.append(ch);
            }
            start++;
        }

        patternBuilder.append("$");

        Matcher mc = Pattern.compile(patternBuilder.toString()).matcher("shop/books/10/hao.html");
        boolean test = mc.matches();
        String t1 = mc.group(0);
        String t2 = mc.group(1);
        String t3 = mc.group(2);

        System.out.println("done");
    }
}
