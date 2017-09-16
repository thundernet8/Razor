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


package com.fedepot.ioc;

import com.fedepot.ioc.exception.DependencyResolveException;
import com.fedepot.ioc.walker.ClassesWalker;
import com.fedepot.ioc.walker.ConstructorWalker;
import com.fedepot.ioc.walker.FieldsWalker;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Beans container
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class Ioc implements IContainer {

    @SuppressWarnings("unchecked")
    private final Map<String, ServiceBean> beanPool = new HashMap();

    private final Map<String, ServiceBean> namedBeanPool = new HashMap<>();

    private final Map<String, Map<Object, ServiceBean>> keyedBeanPool = new HashMap<>();

    Ioc(List<ServiceBean> beans) {
        // TODO getInterfaces() and register
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

                namedBeanPool.put(typeName.concat("-").concat(bean.getName()), bean);
            }

            beanPool.put(typeName, bean);
        }
    }

    @Override
    public <T> T resolve(Class<T> t) {

        if (t.isInterface()) {

            Class<?>[] implementers = ClassesWalker.cachedImplementers(t);

            if (implementers.length == 0) {
                return null;
            }

            for (Class<?> implementer : implementers) {

                Object ret = resolve(implementer);

                if (ret != null) {

                    return (T)ret;
                }
            }

            return null;
        }

        try {

            return resolveBean(beanPool.get(t.getName()));
        } catch (DependencyResolveException e) {

            log.error("Resolve {} encounter exception: {}", t.getName(), e.getMessage());

            return null;
        }
    }

    @Override
    public <T> T resolveNamed(Class<T> t, String name) {

        if (t.isInterface()) {

            Class<?>[] implementers = ClassesWalker.cachedImplementers(t);

            if (implementers.length == 0) {

                return null;
            }

            for (Class<?> implementer : implementers) {

                Object ret = resolveNamed(implementer, name);

                if (ret != null) {

                    return (T)ret;
                }
            }

            return null;
        }

        try {

            return resolveBean(namedBeanPool.get(t.getName().concat("-").concat(name)));
        } catch (DependencyResolveException e) {

            log.error("Resolve {} named {} encounter exception: {}", t.getName(), name, e.getMessage());

            return null;
        }
    }

    @Override
    public <T, E extends Enum<E>> T resolveKeyed(Class<T> t, E enumKey) {

        if (t.isInterface()) {

            Class<?>[] implementers = ClassesWalker.cachedImplementers(t);

            if (implementers.length == 0) {

                return null;
            }

            for (Class<?> implementer : implementers) {

                Object ret = resolveKeyed(implementer, enumKey);

                if (ret != null) {

                    return (T)ret;
                }
            }

            return null;
        }

        try {

            Map<Object, ServiceBean> svbMap = keyedBeanPool.get(t.getName());

            return resolveBean(svbMap.get(enumKey));
        } catch (DependencyResolveException e) {

            log.error("Resolve {} keyed {} encounter exception: {}", t.getName(), enumKey.toString(), e.getMessage());

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveBean(ServiceBean svb) throws DependencyResolveException {

        if (svb == null) {

            return null;
        }

        Object bean = svb.getBean();

        if (bean != null) {

            return (T)bean;
        }

        try {
            Class<?> clazz = svb.getImplType();

            if (clazz.isInterface()) {

                Class<?>[] implementers = ClassesWalker.cachedImplementers(clazz);
                for (Class<?> implementer : implementers) {

                    Object ret = resolve(implementer);

                    if (ret != null) {

                        return (T)ret;
                    }
                }

                throw new DependencyResolveException("Cannot resolve interface " + clazz.getName());
            }

            // resolve constructor and args
            Constructor constructor = ConstructorWalker.cachedInjectConstructor(clazz);
            Class[] parameterTypes = constructor.getParameterTypes();
            Object ins;

            if (parameterTypes.length == 0) {

                ins = constructor.newInstance();
            } else {

                Object[] args = Arrays.stream(parameterTypes).map(this::resolve).toArray();
                ins = constructor.newInstance(args);
            }

            // resolve fields
            Field[] fields = FieldsWalker.cachedInjectFields(clazz);
            Arrays.stream(fields).forEach(field -> {

                // boolean accessible = field.isAccessible();
                try {

                    field.setAccessible(true);
                    field.set(ins, resolve(field.getType()));
                } catch (IllegalAccessException e) {

                    log.error("Access field {} of {} with exception: {}", field.getName(), clazz.getName(), e.getMessage());
                } finally {

                    // field.setAccessible(accessible);
                }
            });


            if (svb.isSington()) {

                svb.setBean(ins);
            }

            return (T)ins;
        } catch (Exception e) {

            e.printStackTrace();
            throw new DependencyResolveException(e.getMessage(), e.getCause());
        }

    }
}
