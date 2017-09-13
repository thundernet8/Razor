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


import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Reflect classes for a interface
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class ClassesWalker {

    private static final Map<Class<?>, Class<?>[]> classesMap = new HashMap<>();

    private static Class<?> appClass;

    public static <T> Class<?>[] reflectImplementers(Class<?> appClass, Class<T> implType) {

        Reflections reflections = new Reflections(ClasspathHelper.forPackage(appClass.getPackage().getName()), new SubTypesScanner(), new FilterBuilder().include(".*.class"));

        Class<?>[] implementers = reflections.getSubTypesOf(implType).stream().map(impl -> (Class<?>)impl).toArray(Class<?>[]::new);

        // cache reflection results
        classesMap.put(implType, implementers);

        ClassesWalker.appClass = appClass;

        return implementers;
    }

    public static Class<?>[] cachedImplementers(Class<?> implType) {

        Class<?>[] implementers = classesMap.get(implType);

        if (implementers != null) {

            return implementers;
        }

        return reflectImplementers(appClass, implType);
    }
}
