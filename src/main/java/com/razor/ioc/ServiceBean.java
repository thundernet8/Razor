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

import org.apache.commons.lang3.StringUtils;
import lombok.Getter;

/**
 * Bean descriptor
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Getter
public class ServiceBean {

    private Class<?> regType;

    private Class<?> implType;

    private String name;

    private Object key;

    private Object bean;

    private boolean sington;

    boolean hasName() {
        return !StringUtils.isEmpty(name);
    }

    boolean hasKey() {
        return key != null;
    }

    boolean isSington() {
        return sington;
    }

    private ServiceBean(RegistrationData rd) {
        implType = rd.getImplType();
        regType = rd.getRegType();
        name = rd.getName();
        key = rd.getKey();
        bean = rd.getInstance();
        sington = rd.isSington();

        if (regType == null) {
            regType = implType;
        }
        // TODO
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    static ServiceBean fromRegistrationData(RegistrationData rd) {
        return new ServiceBean(rd);
    }
}
