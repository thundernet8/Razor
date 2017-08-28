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


package com.razor.mvc.route;

import com.razor.mvc.Controller;
import java.lang.reflect.Method;
import lombok.Getter;

/**
 * Router
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Getter
public class Router {

    private RouteMatcher routeMatcher;

    private String httpMethod;

    /**
     * Controller class
     */
    private Class<?> targetType;

    /**
     * Controller instance
     */
//    private Controller target;


    /**
     * Router bind method
     */
    private Method action;



    public Router(String httpMethod, Class<?> targetType, Method action, RouteMatcher routeMatcher) {

        this.httpMethod = httpMethod.toUpperCase();
        this.targetType = targetType;
        this.action = action;
        this.routeMatcher = routeMatcher;
    }

    public boolean match (String url) {

        return routeMatcher.getPattern().matcher(url).matches();
    }

    public String getFullPath() {

        String routePrefix = routeMatcher.getRoutePrefix();
        String route = routeMatcher.getRoute();

        if (route == null || route.isEmpty()) {
            return routePrefix;
        }
        return routePrefix.concat("/").concat(route);
    }

    /**
     * Unique string for a router
     *
     * @return String
     */
    public String getHashKey() {

        return getFullPath().concat("::").concat(httpMethod.toUpperCase());
    }

    /**
     * Router got a generic url match
     *
     * @return boolean
     */
    public boolean isGeneric() {

        return routeMatcher.getParamTypes().length > 0;
    }
}
