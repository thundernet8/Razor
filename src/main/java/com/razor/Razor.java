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


package com.razor;

import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import com.razor.env.Env;

/**
 * Razor entrance
 *
 * @author Touchumind
 * @since 0.0.1
 * @date 2017/8/21
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Razor {
    private Env env;

    public Env getEnv() {
        return env;
    }

    // Application class
    private Class<?> appClass;

    public Class<?> getAppClass() {
        return appClass;
    }

    public static Razor self() {
        return new Razor();
    }

    public Razor listen(@NonNull String host, @NonNull int port) {
        // TODO
        return this;
    }

    public void run(@NonNull Class<?> appClass, String[] args) {
        // TODO
        this.appClass = appClass;
    }

    public void stop() {
        // TODO
    }
}
