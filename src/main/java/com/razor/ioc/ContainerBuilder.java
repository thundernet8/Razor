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

import com.razor.ioc.annotation.Inject;
import com.razor.ioc.walker.ClassesWalker;
import com.razor.ioc.walker.ConstructorWalker;
import com.razor.ioc.walker.FieldsWalker;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Beans container builder
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class ContainerBuilder implements IContainerBuilder {

    private Class<?> appClass;

    public ContainerBuilder(Class<?> appClass) {
        this.appClass = appClass;
    }

    private final List<RegistrationBuilder> rbs = new ArrayList<>();

    @Override
    public <T> IRegistrationBuilder registerType(Class<T> implementationType) {
        RegistrationBuilder rb = RegistrationBuilder.forType(implementationType);
        rbs.add(rb);

        // register these who implement the interface
        if (implementationType.isInterface()) {
            Class<?>[] implementers = ClassesWalker.reflectImplementers(appClass, implementationType);
            Arrays.stream(implementers).forEach(implementer -> {
                RegistrationBuilder _rb = (RegistrationBuilder)registerType(implementer).as(implementationType).named(implementer.getName());
                if (implementer.getAnnotation(Inject.class).sington()) {
                    _rb.singleInstance();
                }
                rbs.add(_rb);
            });
        }

        return rb;
    }

    @Override
    public <T> IRegistrationBuilder registerInstance(T instance) {
        RegistrationBuilder rb = RegistrationBuilder.forInstance(instance);
        rbs.add(rb);
        return rb;
    }

    @Override
    public <T> void autoRegister(Class<T> abstractController) {
        this.registerControllers(abstractController);

        // scan inject annotated class
        Set<Class<?>> types = new Reflections(appClass.getPackage().getName()).getTypesAnnotatedWith(Inject.class);
        types.forEach(this::registerType);

        types.forEach(t -> {
            try {
                ConstructorWalker.findInjectConstructor(t);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    private  <T> void registerControllers(Class<T> abstractController) {
        Set<Class<? extends T>> controllers = new Reflections(appClass.getPackage().getName().concat(".controllers")).getSubTypesOf(abstractController);
        controllers.forEach(this::registerController);
        log.info("Ioc registered {} controllers", controllers.size());
    }

    private void registerController(Class<?> clazz) {
        // constructor self
        registerType(clazz);

        // constructor parameters
        try {
            Constructor constructor = ConstructorWalker.findInjectConstructor(clazz);
            Parameter[] paramNames = constructor.getParameters();
            Arrays.stream(paramNames).map(Parameter::getType).forEach(this::registerType);
            log.info("Ioc registered {} for {} constructor", paramNames, clazz.getName());
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        // class fields
        registerFields(clazz);
    }

    private void registerFields(Class<?> clazz) {

        Field[] serviceFields = FieldsWalker.findInjectFields(clazz);
        Arrays.stream(serviceFields).map(Field::getType).forEach(this::registerType);
        log.info("Ioc registered {} fields for controller {}", serviceFields.length, clazz.getName());
    }

    @Override
    public IContainer build() {
        return new Ioc(rbs.stream().map(t -> ServiceBean.fromRegistrationData(t.getRegistrationData())).collect(Collectors.toList()));
    }
}
