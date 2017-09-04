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

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * Registration data helper for ContainerBuilder
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationBuilder implements IRegistrationBuilder {

    private RegistrationData registrationData;

    private static RegistrationBuilder init() {

        RegistrationBuilder rb = new RegistrationBuilder();
        rb.registrationData = RegistrationData.defaults();

        return rb;
    }

    static <T> RegistrationBuilder forType(Class<T> implementer) {

        RegistrationBuilder rb = init();
        rb.registrationData.setImplType(implementer);

        return rb;
    }

    static <T> RegistrationBuilder forInstance(T instance) {

        RegistrationBuilder rb = init();

        // instance's lifecycle must be sington
        rb.registrationData.setSington(true);
        rb.registrationData.setInstance(instance);
        rb.registrationData.setRegType(instance.getClass());

        return rb;
    }

    @Override
    public <T> RegistrationBuilder as(Class<T> implementationType) {

        registrationData.setRegType(implementationType);

        return this;
    }

    @Override
    public RegistrationBuilder named(String name) {

        if (registrationData.getKey() != null) {

            log.warn("redundant name identity assignment");

            return this;
        }

        registrationData.setName(name);

        return this;
    }

    @Override
    public <E extends Enum<E>> RegistrationBuilder keyed(E enumKey) {

        if (!StringUtils.isEmpty(registrationData.getName())) {

            log.warn("redundant key identity assignment");

            return this;
        }

        registrationData.setKey(enumKey);

        return this;
    }

    @Override
    public RegistrationBuilder instancePerDependency() {

        registrationData.setSington(false);

        return this;
    }

    @Override
    public RegistrationBuilder singleInstance() {

        registrationData.setSington(true);

        return this;
    }
}
