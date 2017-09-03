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


package com.razor.mvc.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Session object implement
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class HttpSession implements Session {

    private String id = null;

    private long createAt = -1;

    private long expireAt = -1;

    private Map<String, Object> attributes = new HashMap<>();

    public HttpSession(String id, long createAt, long expireAt) {

        this.id = id;
        this.createAt = createAt;
        this.expireAt = expireAt;
    }

    @Override
    public String id() {

        return id;
    }

    @Override
    public long createAt() {

        return createAt;
    }

    @Override
    public long expireAt() {

        return expireAt;
    }

    @Override
    public Map<String, Object> attributes() {

        return attributes;
    }

    @Override
    public <T> T attribute(String name) {

        Object value = attributes.get(name);

        if (value != null) {

            return (T)value;
        }

        return null;
    }

    @Override
    public void addAttribute(String name, Object value) {

        this.attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {

        this.attributes.remove(name);
    }
}
