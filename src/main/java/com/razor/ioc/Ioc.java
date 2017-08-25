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


package com.razor.ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Beans container
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class Ioc implements IContainer {

    @SuppressWarnings("unchecked")
    private final Map<String, ServiceBean> beanPool = new HashMap();

    private final Map<String, ServiceBean> namedBeanPool = new HashMap<>();

    private final Map<String, Map<Object, ServiceBean>> keyedBeanPool = new HashMap<>();

    public Ioc(List<ServiceBean> beans) {
        for (ServiceBean bean : beans) {
            String typeName = bean.getRegType().getName();
            if (bean.hasKey()) {
                Map<Object, ServiceBean> innerMap = keyedBeanPool.get(typeName);
                if (innerMap == null) {
                    innerMap = new HashMap<>();
                }
                innerMap.put(bean.getKey(), bean);
                keyedBeanPool.put(typeName, innerMap);
            }
            if (bean.hasName()) {
                namedBeanPool.put(typeName + "-" + bean.getName(), bean);
            }
            beanPool.put(typeName, bean);
        }
    }

    @Override
    public <T> T resolve(Class<T> t) {
        return resolveBean(beanPool.get(t.getName()));
    }

    @Override
    public <T> T resolveNamed(Class<T> t, String name) {
        return resolveBean(namedBeanPool.get(t.getName() + "-" + name));
    }

    @Override
    public <T, E extends Enum<E>> T resolveKeyed(Class<T> t, E enumKey) {
        Map<Object, ServiceBean> svbMap = keyedBeanPool.get(t.getName());
        return resolveBean(svbMap.get(enumKey));
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveBean(ServiceBean svb) {
        // TODO
        if (svb == null) {
            return null;
        }
        Object bean = svb.getBean();
        if (bean != null) {
            return (T)bean;
        }

        try {
            Class<?> clazz = Class.forName(svb.getImplType().getName());
            Object ins = clazz.newInstance();
            return (T)ins;
        } catch (Exception e) {
            return null;
        }

    }
}
