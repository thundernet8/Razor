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
import com.razor.mvc.controller.APIController;
import com.razor.mvc.controller.Controller;
import com.razor.mvc.annotation.*;
import com.razor.mvc.http.HttpMethod;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Routes manager
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class RouteManager {

    private static RouteManager instance;

    private Razor razor;

    private Class<?> appClass;

    /**
     * Routers with fixed path
     */
    private final Map<String, Router> routerMap = new HashMap<>();

    /**
     * Routers with regex type path
     */
    private final Set<Router> routerSet = new HashSet<>();

    private RouteManager(Razor razor) {

        this.razor = razor;
        this.appClass = razor.getAppClass();
    }

    public static RouteManager getInstance(Razor razor) {

        if (instance == null) {
            synchronized (RouteManager.class) {

                if (instance == null) {

                    instance = new RouteManager(razor);
                }
            }
        }

        return instance;
    }

    /**
     * Register routes when startup
     */
    public void registerRoutes() {

        Set<Class<? extends Controller>> controllers = new Reflections(appClass.getPackage().getName()).getSubTypesOf(Controller.class);
        Set<Class<? extends APIController>> apiControllers = new Reflections(appClass.getPackage().getName()).getSubTypesOf(APIController.class);
        controllers.forEach(this::parseControllerRoutes);
        apiControllers.forEach(this::parseControllerRoutes);
    }

    /**
     * Parse controller routes from a specified controller class
     *
     * @param clazz {@link Controller} or {@link APIController} class
     */
    private void parseControllerRoutes(Class<?> clazz) {

        String routePrefix = "";
        RoutePrefix rpAnnotation = clazz.getAnnotation(RoutePrefix.class);

        if (rpAnnotation != null) {

            routePrefix = rpAnnotation.value();
        }

        Method[] actions = clazz.getDeclaredMethods();

        for (Method action : actions) {

            Route rtAnnotation = action.getAnnotation(Route.class);

            if (rtAnnotation == null) {

                continue;
            }

            String route = rtAnnotation.value();
            assert !StringUtils.isEmpty(route) : action.getName() + " Route should not have a empty path";
            RouteMatcher matcher = new RouteMatcher(routePrefix, route);
            assert matcher.isValid() : clazz.getName().concat(" controller Route prefix or ").concat(action.getName()).concat(" action route has invalid format");

            HttpPost postAnnotation = action.getAnnotation(HttpPost.class);
            HttpPut putAnnotation = action.getAnnotation(HttpPut.class);
            HttpDelete delAnnotation = action.getAnnotation(HttpDelete.class);
            HttpAll httpAllAnnotation = action.getAnnotation(HttpAll.class);
            String httpMethod;
            if (httpAllAnnotation != null) {

                httpMethod = HttpMethod.ALL;
            } else if (delAnnotation != null) {

                httpMethod = HttpMethod.DELETE;
            } else if (putAnnotation != null) {

                httpMethod = HttpMethod.PUT;
            } else if (postAnnotation != null) {

                httpMethod = HttpMethod.POST;
            } else {

                httpMethod = HttpMethod.GET;
            }

            addRoute(new Router(httpMethod, clazz, action, matcher));
        }
    }

    private void addRoute(Router router) {

        router.collectMiddlewares(razor);

        if (router.isGeneric()) {
            routerSet.add(router);
        } else {
            routerMap.put(router.getHashKey(), router);
        }
    }

    /**
     * Find route with request method and path
     *
     * @param path path to request
     * @param httpMethod http method
     * @return Router instance
     */
    public Router findRoute(String path, String httpMethod) {

        Router router = routerMap.get(path.concat("::").concat(httpMethod));
        if (router != null) {
            return router;
        }

        router = routerMap.get(path.concat("::").concat(HttpMethod.ALL));
        if (router != null) {
            return router;
        }

        for (Router router1 : routerSet) {
            String routerHttpMethod = router1.getHttpMethod();
            if ((routerHttpMethod.equals(HttpMethod.ALL) || StringUtils.equals(routerHttpMethod, httpMethod)) && router1.match(path)) {
                return router1;
            }
        }

        return null;
    }

    public Router findRoute(String path) {

        return findRoute(path, HttpMethod.GET);
    }
}
