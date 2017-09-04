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


package com.fedepot.cache;

import java.util.Optional;

/**
 * Cache interface
 *
 * @author Touchumind
 * @since 0.0.1
 */
public interface Cache {

    void add(String key, Object value, int expires);

    void add(String key, Object value, int expires, String group);

    boolean safeAdd(String key, Object value, int expires);

    boolean safeAdd(String key, Object value, int expires, String group);

    void delete(String key);

    void delete(String key, String group);

    void clear();

    void clear(String group);

    Optional<Object> get(String key);

    Optional<Object> get(String key, String group);

    Object get(String key, Object defaultValue);

    Object get(String key, String group, Object defaultValue);

    long incr(String key, int by);

    long incr(String key, String group, int by);

    long decr(String key, int by);

    long decr(String key, String group, int by);

    void shutdown();
}
