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

import com.fedepot.ioc.annotation.Service;
import com.fedepot.ioc.annotation.IocIgnore;
import com.fedepot.ioc.exception.DependencyRegisterException;
import com.fedepot.ioc.walker.ClassesWalker;
import com.fedepot.ioc.walker.ConstructorWalker;
import com.fedepot.ioc.walker.FieldsWalker;

import com.fedepot.mvc.controller.APIController;
import com.fedepot.mvc.controller.Controller;
import com.fedepot.util.ReflectKit;
import org.reflections.Reflections;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Beans container builder
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class ContainerBuilder implements IContainerBuilder {

    private static ContainerBuilder instance;

    private Class<?> appClass;

    private ContainerBuilder(Class<?> appClass) {
        this.appClass = appClass;
    }

    public static ContainerBuilder getInstance(Class<?> appClass) {

        if (instance == null) {
            synchronized (ContainerBuilder.class) {

                if (instance == null) {

                    instance = new ContainerBuilder(appClass);

                    instance.autoRegister();
                }
            }
        }

        return instance;
    }

    private final List<RegistrationBuilder> rbs = new ArrayList<>();

    private final Set<Class<?>> registeredTypes = new HashSet<>();

    /**
     * Auto register classes annotated with `Service`
     */
    private void autoRegister() {

        Reflections reflections = ReflectKit.getReflections(appClass);

        // scan inject annotated class
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(Service.class);
        types.forEach(this::recursiveRegisterType);

        // cache constructors
        types.forEach(t -> {
            try {

                ConstructorWalker.findInjectConstructor(t);
            } catch (Exception e) {

                log.error(e.getMessage());
            }
        });
    }

    @Override
    public <T> IRegistrationBuilder registerType(Class<T> implementer) throws DependencyRegisterException {

        if (implementer.isInterface()) {

            throw new DependencyRegisterException("Should not register a interface directly");
        }
        RegistrationBuilder rb = RegistrationBuilder.forType(implementer);
        rbs.add(rb);

        return rb;
    }

    @Override
    public <T> IRegistrationBuilder registerInstance(T instance) {

        RegistrationBuilder rb = RegistrationBuilder.forInstance(instance);
        rbs.add(rb);
        return rb;
    }

    @Override
    public <T> void autoRegister(Class<T> clazz) {

        IocIgnore ignore = clazz.getAnnotation(IocIgnore.class);

        if (ignore != null) {

            return;
        }

        if (clazz.isInterface()) {

            recursiveRegisterType(clazz);
            return;
        }

        if (clazz == Controller.class || clazz == APIController.class) {

            this.registerControllers(clazz);
        }
    }

    private  <T> void registerControllers(Class<T> abstractController) {

        Reflections reflections = ReflectKit.getReflections(appClass);

        Set<Class<? extends T>> controllers = reflections.getSubTypesOf(abstractController);

        controllers.forEach(this::recursiveRegisterType);
        log.info("Ioc registered {} controllers", controllers.size());
    }

    private void recursiveRegisterType(Class<?> clazz) {

        if (registeredTypes.contains(clazz)) {

            return;
        }

        IocIgnore ignore = clazz.getAnnotation(IocIgnore.class);

        if (ignore != null) {

            return;
        }

        registeredTypes.add(clazz);

        // register these who implement the interface
        if (clazz.isInterface()) {

            Class<?>[] implementers = ClassesWalker.reflectImplementers(appClass, clazz);
            Arrays.stream(implementers).forEach(this::recursiveRegisterType);
            return;
        }

        try {
            // controller self
            Service inject = clazz.getAnnotation(Service.class);
            if (inject != null && inject.sington()) {
                registerType(clazz).singleInstance();
            } else {
                registerType(clazz).instancePerDependency();
            }

            // constructor parameters
            Constructor constructor = ConstructorWalker.findInjectConstructor(clazz);
            if (constructor != null) {

                Parameter[] paramNames = constructor.getParameters();
                Arrays.stream(paramNames).map(Parameter::getType).forEach(this::recursiveRegisterType);
                log.info("Ioc registered {} parameters for {} constructor", paramNames, clazz.getName());

                // class fields
                registerFields(clazz);
            }
        } catch (Exception e) {

            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerFields(Class<?> clazz) throws DependencyRegisterException  {

        try {

            Field[] serviceFields = FieldsWalker.findInjectFields(clazz);
            Arrays.stream(serviceFields).map(Field::getType).forEach(this::recursiveRegisterType);
            log.info("Ioc registered {} fields for controller {}", serviceFields.length, clazz.getName());
        } catch (Exception e) {

            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public IContainer build() {

        Ioc ioc = new Ioc(rbs.stream().map(t -> ServiceBean.fromRegistrationData(t.getRegistrationData())).collect(Collectors.toList()));

        // dispose
        rbs.clear();
        registeredTypes.clear();

        return ioc;
    }
}
