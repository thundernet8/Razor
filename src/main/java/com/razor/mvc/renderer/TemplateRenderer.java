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


package com.razor.mvc.renderer;

import com.razor.exception.RazorException;
import com.razor.mvc.http.EContentType;
import com.razor.mvc.http.Request;
import com.razor.mvc.http.Response;
import com.razor.mvc.template.TemplateEngineFactory;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * Renderer using template
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class TemplateRenderer extends Renderer {

    /**
     * Path relative to classpath
     */
    private String templatePath;

    private Map<String, Object> model;

    public TemplateRenderer(String path) {

        this.templatePath = path;
        this.model = new HashMap<>();

    }

    public TemplateRenderer(String path, Map<String, Object> model) {

        this.templatePath = path;
        this.model = model;
    }

    public TemplateRenderer(String path, String dataKey, Object dataValue) {

        this.templatePath = path;
        this.model = new HashMap<>();
        this.model.put(dataKey, dataValue);
    }

    @Override
    public void render(Request request, Response response) throws RazorException {

        try {

            String view = TemplateEngineFactory.getEngine().render(templatePath, model);

            // TODO flush data, content-type and other headers
            if (response.header(CONTENT_TYPE) == null) {
                response.header(CONTENT_TYPE, getContentType().getMimeType());
            }
            response.send(view);

        } catch (Exception e) {

            throw new RazorException(e);
        }
    }
}
