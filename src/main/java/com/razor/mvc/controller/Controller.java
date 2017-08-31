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


package com.razor.mvc.controller;

import com.razor.exception.RazorException;
import com.razor.mvc.annotation.RoutePrefix;
import com.razor.mvc.http.ContentType;
import com.razor.mvc.http.HttpContext;
import com.razor.mvc.middleware.Middleware;
import com.razor.mvc.renderer.TemplateRenderer;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.razor.mvc.Constants.*;

/**
 * Razor abstract controller
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@RoutePrefix
public abstract class Controller implements IController {

    private HttpContext httpContext;

    protected HttpContext Context() {

        return httpContext;
    }

    /**
     * <del>Override</del> this method in your controller to register middlewares to the controller
     *
     * @return middlewares to register
     */
    public static Set<Middleware> registerMiddlewares() {

        return null;
    }

    protected void Render(String templatePath) {

        TemplateRenderer renderer = new TemplateRenderer(templateFullPath(templatePath));

        proxyRender(renderer);
    }

    protected void Render(String templatePath, Map<String, Object> data) {

        TemplateRenderer renderer = new TemplateRenderer(templateFullPath(templatePath), data);

        proxyRender(renderer);
    }

    protected void Render(String templatePath, String dataKey, String dataValue) {

        TemplateRenderer renderer = new TemplateRenderer(templateFullPath(templatePath), dataKey, dataValue);
        renderer.setContentType(ContentType.HTML);

        proxyRender(renderer);
    }

    private void proxyRender(TemplateRenderer renderer) {

        try {

            renderer.render(httpContext.request(), httpContext.response());
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

        String templateDir = httpContext.app().getEnv().get(ENV_KEY_TEMPLATE_ROOT_DIR, DEFAULT_TEMPLATE_ROOT_DIR);
        // TODO cache this variable
        return templateDir.concat("/").concat(path);
    }
}
