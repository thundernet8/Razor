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


package com.fedepot.ioc.walker;

import com.fedepot.ioc.exception.DependencyResolveException;
import com.fedepot.ioc.annotation.ForInject;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Find right inject constructor for a class
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class ConstructorWalker {

    private static final Map<Class<?>, Constructor> constructorMap = new HashMap<>();

    public static Constructor findInjectConstructor(Class<?> clazz) throws DependencyResolveException {

        Constructor[] constructors = clazz.getConstructors();

        if (constructors.length == 0) {

            throw new DependencyResolveException("Cannot resolve constructor for Type: " + clazz.getName());
        }

        Constructor injectConstructor = constructors[0];

        if (constructors.length > 1) {
            try {

                Constructor annotatedConstructor = null;
                // find a annotated constructor which is specified for injection
                for (Constructor constructor : constructors) {

                    if (constructor.getAnnotation(ForInject.class) != null && annotatedConstructor == null) {

                        annotatedConstructor = constructor;
                    }
                }

                if (annotatedConstructor != null) {

                    injectConstructor = annotatedConstructor;
                }

            } catch (Exception e) {

                log.error("Walk class for constructor encounter exception: {}", e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        constructorMap.put(clazz, injectConstructor);

        return injectConstructor;
    }

    public static Constructor cachedInjectConstructor(Class<?> clazz) throws DependencyResolveException {

        Constructor constructor = constructorMap.get(clazz);
        if (constructor != null) {

            return constructor;
        }
        constructor = findInjectConstructor(clazz);
        if (constructor == null) {

            throw new DependencyResolveException("Cannot resolve constructor for Type: " + clazz.getName());
        }

        return constructor;
    }
}
