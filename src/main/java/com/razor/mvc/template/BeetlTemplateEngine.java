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

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Beetl template engine
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class BeetlTemplateEngine implements TemplateEngine{

    private static GroupTemplate groupTemplate;

    private static GroupTemplate getGroupTemplate() {

        if (groupTemplate == null) {
//            synchronized (BeetlTemplateEngine.class) {
//
//                if (groupTemplate == null) {

                    try {

                        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
                        Configuration cfg = Configuration.defaultConfiguration();
                        groupTemplate = new GroupTemplate(resourceLoader, cfg);
                    } catch (IOException e) {

                        log.error(e.toString());
                    }
//                }
//            }
        }

        return groupTemplate;
    }

    @Override
    public String getName() {

        return "Beetl";
    }

    @Override
    public String render(String templatePath, Map<String, Object> data) throws Exception {

        GroupTemplate gt = getGroupTemplate();
        Template template = gt.getTemplate(templatePath);
        template.binding(data);

        return template.render();
    }
}
