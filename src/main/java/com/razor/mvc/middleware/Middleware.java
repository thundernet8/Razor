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


package com.razor.mvc.middleware;

import com.razor.mvc.http.Request;
import com.razor.mvc.http.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Request middleware functional interface
 *
 * @author Touchumind
 * @since 0.0.1
 */
@FunctionalInterface
public interface Middleware extends Comparable<Middleware> {

    static Map<Middleware, Integer> priorities = new HashMap<>();

    default public int getPriority() {

        try {

            return priorities.get(this);
        } catch (NullPointerException e) {

            return -1;
        }
    }

    default public void setPriority(int priority) {

        assert priority >=0 : "priority should not smaller than 0";

        priorities.put(this, priority);
    }

    public void apply(Request req, Response res);

    default public int compareTo(Middleware other) {

        return getPriority() - other.getPriority();
    }
}
