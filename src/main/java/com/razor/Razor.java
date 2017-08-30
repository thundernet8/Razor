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


package com.razor;

import com.razor.ioc.ContainerBuilder;
import com.razor.ioc.IContainer;
import com.razor.ioc.IContainerBuilder;
import com.razor.env.Env;
import com.razor.mvc.annotation.RoutePrefix;
import com.razor.mvc.controller.APIController;
import com.razor.mvc.controller.Controller;
import com.razor.mvc.controller.IController;
import com.razor.mvc.middleware.Middleware;
import com.razor.mvc.route.RouteManager;
import com.razor.server.NettyServer;

import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static com.razor.mvc.Constants.*;

/**
 * Razor entrance
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Razor {

    private Env env = Env.defaults();

    // Application class
    private Class<?> appClass;

    // Ioc container builder
    private IContainerBuilder iocBuilder;

    // Ioc container
    private IContainer ioc;

    private NettyServer nettyServer = new NettyServer();

    private final Set<String> statics = new HashSet<>(DEFAULT_STATICS);

    private final Set<Middleware> rootMiddlewares = new HashSet<>();

    private final Map<String, Set<Middleware>> pathMiddlewares = new HashMap<>();

    public static Razor self() {

        return new Razor();
    }

    /**
     * Specify the host and port for server listening
     * @param host remote address
     * @param port port
     * @return Razor
     */
    public Razor listen(@NonNull String host, int port) {

        env.set(ENV_KEY_SERVER_HOST, host);
        env.set(ENV_KEY_SERVER_PORT, port);

        return this;
    }

    public void start(@NonNull Class<?> appClass, String[] args) {

        start(appClass, DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT, args);
    }

    public void start(@NonNull Class<?> appClass, @NonNull String host, int port, String[] args) {

        try {

            env.set(ENV_KEY_SERVER_HOST, host);
            assert port >= 80 : "Port should be a positive value and greater or equal to 80";
            env.set(ENV_KEY_SERVER_PORT, port);
            this.appClass = appClass;

            this.startUp();

            new Thread(() -> {
                try {

                    nettyServer.run(Razor.this, args);
                } catch (Exception e) {

                    log.error("Run razor in new thread failed, error: {}", e.getMessage());
                }
            }).start();
        } catch (Exception e) {

            log.error("Run razor failed, error: {}", e.getMessage());
        }
    }


    public void stop() {

        // TODO calculate run time
        nettyServer.shutdown();
    }

    /**
     * Add static path rule
     * @param rules static path rule, a full path or path prefix or file extension, e.g `/favicon.png`, `/statics/`, `.png`
     * @return Razor
     */
    public Razor addStatic(String... rules) {

        statics.addAll(Arrays.asList(rules));

        return this;
    }

    /**
     * Specify web root dir of resource, default `web` which means a web folder under your resources folder, and web folder under your classpath when running
     *
     * @param webDir web root dir
     * @return Razor
     */
    public Razor webRoot(String webDir) {

        if (!Pattern.compile("^([^/.])([0-9a-zA-Z_]*)([^/.])$").matcher(webDir).find()) {

            log.error("Content package should only include numbers, latin letters, _ or /, and should not start or end with /");

            return this;
        }
        try {

            env.set(ENV_KEY_WEB_ROOT_DIR, webDir);
            statics.add("/".concat(webDir).concat("/"));
        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return this;
    }

    /**
     * Specify the file index for a directory
     * @param indexs index files
     * @return Razor
     */
    public Razor indexs(List<String> indexs) {

        try {

            env.set(ENV_KEY_INDEX_FILES, indexs);
        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return this;
    }

    /**
     * Enable/disable ssl support
     *
     * @param ssl ssl enable status
     * @return Razor
     */
    public Razor ssl(boolean ssl) {

        try {

            env.set(ENV_KEY_SSL, ssl);
        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return this;
    }

    /**
     * Apply middleware to the request handler
     *
     * @param middleware middleware handler
     * @return Razor self
     */
    public Razor use(Middleware middleware) {

        // TODO add a response time calculating middleware

        if (middleware.getPriority() < 0) {
            middleware.setPriority(rootMiddlewares.size());
        }
        rootMiddlewares.add(middleware);

        return this;
    }

    /**
     * Register middleware to the request handler and specified route
     *
     * @param path path the middleware apply to, support universal match
     *             e.g `/books/novel/*`
     *             note: must start with `/`
     * @param middleware middleware handler
     * @return Razor self
     */
    public Razor use(@NonNull String path, Middleware middleware) {

        Set<Middleware> exists = pathMiddlewares.get(path);

        if (exists == null) {

            exists = new HashSet<>();
        }

        if (middleware.getPriority() < 0) {
            middleware.setPriority(rootMiddlewares.size() + exists.size());
        }
        exists.add(middleware);
        pathMiddlewares.put(path, exists);

        return this;
    }

    /**
     * Register middleware to the specified controller class
     *
     * @param controllerClass a controller class
     * @param middleware middleware handler
     * @return Razor self
     */
    public Razor use (@NonNull Class<? extends IController> controllerClass, Middleware middleware) {

        String routePrefix = "/";
        RoutePrefix routePrefixAnnotation = controllerClass.getAnnotation(RoutePrefix.class);

        if (routePrefixAnnotation != null) {

            routePrefix = routePrefixAnnotation.value();
        }

        if (!routePrefix.startsWith("/")) {

            routePrefix = "/".concat(routePrefix);
        }

        return use(routePrefix.concat("/*"), middleware);
    }

    /**
     * Initialize the container
     */
    private void initIoc() {

        iocBuilder = ContainerBuilder.getInstance(appClass);

        // register controllers
        iocBuilder.autoRegister(Controller.class);
        iocBuilder.autoRegister(APIController.class);

        ioc = iocBuilder.build();
    }

    /**
     * Initialize the middlewares
     */
    private void initMiddlewares() {

        Set<Class<? extends IController>> controllers = new Reflections(appClass.getPackage().getName()).getSubTypesOf(IController.class);
        controllers.forEach(controller -> {

            try {

                Method method = controller.getMethod("registerMiddlewares", controller);

                Set<Middleware> middlewares = (Set<Middleware>)method.invoke(null);
                middlewares.forEach(middleware -> {

                    use(controller, middleware);
                });
            } catch (Exception e) {

                log.error(e.toString());
            }
        });
    }

    /**
     * Initialize the routes
     */
    private void initRoutes() {

        RouteManager.getInstance(this).registerRoutes();
    }

    /**
     * Other preparations to be done at last
     */
    private void startUp() {

        this.initIoc();
        this.initMiddlewares();
        this.initRoutes();
    }
}
