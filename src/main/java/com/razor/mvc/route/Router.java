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

import com.razor.Razor;
import com.razor.mvc.middleware.Middleware;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Router bind method
     */
    private Method action;


    /**
     * Middlewares which will be applied to this route
     */
    private List<Middleware> middlewares;

    Router(String httpMethod, Class<?> targetType, Method action, RouteMatcher routeMatcher) {

        this.httpMethod = httpMethod.toUpperCase();
        this.targetType = targetType;
        this.action = action;
        this.routeMatcher = routeMatcher;

    }

    boolean match (String url) {

        return routeMatcher.getPattern().matcher(url).matches();
    }

    private String getFullPath() {

        String routePrefix = routeMatcher.getRoutePrefix();
        String route = routeMatcher.getRoute();

        if (route == null || route.isEmpty()) {

            return routePrefix;
        }

        if (routePrefix.endsWith("/")) {

            return routePrefix.concat(route);
        }

        return routePrefix.concat("/").concat(route);
    }

    /**
     * Unique string for a router
     *
     * @return String
     */
    String getHashKey() {

        return getFullPath().concat("::").concat(httpMethod.toUpperCase());
    }

    /**
     * Router got a generic url match
     *
     * @return boolean
     */
    boolean isGeneric() {

        return routeMatcher.isUniversal();
    }

    /**
     * Collect registered middlewares these are valid for this route
     */
    void collectMiddlewares(Razor razor) {

        List<Middleware> collection = new ArrayList<>(razor.getRootMiddlewares());

        String path = getFullPath();
        Pattern pattern = Pattern.compile("^(/[0-9a-zA-Z-_./]+)?(/[0-9a-zA-Z-_.]*\\{.+}.*)?((/\\*)(.*))?$");
        Matcher matcher = pattern.matcher(path);

        if (matcher.matches() && !(matcher.group(1).equals(path))) {

            path = matcher.group(1).concat("/*");
        }

        for (String key : razor.getPathMiddlewares().keySet()) {

            boolean match = false;

            if (key.equals(path)) {

                match = true;
            } else if (key.endsWith("/*")) {

                if (path.startsWith(key.substring(0, key.length() - 1))) {

                    match = true;
                }
            }

            if (match) {

                collection.addAll(razor.getPathMiddlewares().get(key));
            }
        }

        Collections.sort(collection);

        middlewares = collection;
    }
}
