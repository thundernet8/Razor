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


package com.fedepot.mvc.controller;

import com.fedepot.exception.RazorException;
import com.fedepot.mvc.annotation.RoutePrefix;
import com.fedepot.mvc.http.ContentType;
import com.fedepot.mvc.http.HttpContext;
import com.fedepot.mvc.middleware.Middleware;
import com.fedepot.mvc.renderer.TemplateRenderer;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.fedepot.mvc.Constants.*;

/**
 * Razor abstract controller
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@RoutePrefix
public abstract class Controller implements IController {

    protected HttpContext Context() {

        return HttpContext.get();
    }

    /**
     * <del>Override</del> this method in your controller to register middlewares to the controller
     *
     * @return middlewares to register
     */
    public static Set<Middleware> registerMiddlewares() {

        return null;
    }

    /**
     * Render view with template
     *
     * @param templatePath the template used to render
     */
    protected void Render(String templatePath) {

        TemplateRenderer renderer = new TemplateRenderer(templateFullPath(templatePath));

        proxyRender(renderer);
    }

    /**
     * Render view with template and data
     *
     * @param templatePath the template used to render
     * @param data data for rendering
     */
    protected void Render(String templatePath, Map<String, Object> data) {

        TemplateRenderer renderer = new TemplateRenderer(templateFullPath(templatePath), data);

        proxyRender(renderer);
    }

    /**
     * Render view with template and single key, value
     *
     * @param templatePath the template used to render
     * @param dataKey variable key
     * @param dataValue variable value
     */
    protected void Render(String templatePath, String dataKey, String dataValue) {

        TemplateRenderer renderer = new TemplateRenderer(templateFullPath(templatePath), dataKey, dataValue);
        renderer.setContentType(ContentType.HTML);

        proxyRender(renderer);
    }

    private void proxyRender(TemplateRenderer renderer) {

        try {

            renderer.render(HttpContext.request(), HttpContext.response());
        } catch (RazorException e) {

            // TODO
            // confirm this exception could be captured by {@link HttpServerHandler} and send 500 message
            // or we should send 500 in the response
            log.error(e.toString());
        }
    }

    /**
     * Full path relative to classpath
     *
     * @param path origin path
     * @return full path
     */
    private String templateFullPath(String path) {

        if (path.startsWith(File.separator)) {
            path = path.substring(1);
        }

        String templateDir = HttpContext.app().getEnv().get(ENV_KEY_TEMPLATE_ROOT_DIR, DEFAULT_TEMPLATE_ROOT_DIR);
        return templateDir.concat("/").concat(path);
    }
}
