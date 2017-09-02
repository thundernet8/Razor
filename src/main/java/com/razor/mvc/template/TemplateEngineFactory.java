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


package com.razor.mvc.template;

import lombok.extern.slf4j.Slf4j;

/**
 * Template engine factory
 *
 *
 */
@Slf4j
public class TemplateEngineFactory {

    private static TemplateEngine instance;

    /**
     * Set template engine
     *
     * @param templateEngine template engine instance
     */
    public static void setTemplateEngine(TemplateEngine templateEngine) {

        instance = templateEngine;

        log.info("Template engine is set to: {}", templateEngine.getName());
    }

    /**
     * Get current template engine
     *
     * @return selected template engine instance
     */
    public static TemplateEngine getEngine() {

        if (instance == null) {

            instance = new BeetlTemplateEngine();
        }

        return instance;
    }
}
