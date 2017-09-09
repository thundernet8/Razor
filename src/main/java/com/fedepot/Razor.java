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


package com.fedepot;

import com.fedepot.event.EventEmitter;
import com.fedepot.event.EventType;
import com.fedepot.exception.ExceptionHandler;
import com.fedepot.ioc.ContainerBuilder;
import com.fedepot.ioc.IContainer;
import com.fedepot.ioc.IContainerBuilder;
import com.fedepot.env.Env;
import com.fedepot.mvc.annotation.RoutePrefix;
import com.fedepot.cache.Cache;
import com.fedepot.cache.Ehcache;
import com.fedepot.mvc.controller.APIController;
import com.fedepot.mvc.controller.Controller;
import com.fedepot.mvc.controller.IController;
import com.fedepot.mvc.http.HttpContext;
import com.fedepot.mvc.http.HttpSessionManager;
import com.fedepot.mvc.http.Session;
import com.fedepot.mvc.http.SessionManager;
import com.fedepot.mvc.middleware.Middleware;
import com.fedepot.mvc.route.RouteManager;
import com.fedepot.mvc.template.BeetlTemplateEngine;
import com.fedepot.mvc.template.JtwigTemplateEngine;
import com.fedepot.mvc.template.TemplateEngine;
import com.fedepot.mvc.template.TemplateEngineFactory;
import com.fedepot.server.NettyServer;
import com.fedepot.util.FileKit;

import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static com.fedepot.mvc.Constants.*;

/**
 * Razor entrance
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Razor {

    /**
     * App environments
     */
    @Getter
    private Env env = Env.fromXml();

    /**
     * Current entry application class
     */
    @Getter
    private Class<?> appClass;

    /**
     * Services container builder
     */
    private IContainerBuilder iocBuilder;

    /**
     * Services container
     */
    @Getter
    private IContainer ioc;

    /**
     * Netty server
     */
    private NettyServer nettyServer = new NettyServer();

    /**
     * Path rules of static resource directory
     */
    @Getter
    private final Set<String> statics = new HashSet<>((List<String>)(env.getObject(ENV_KEY_STATIC_RULES).orElse(DEFAULT_STATICS)));

    /**
     * Static route prefix map with server directory
     */
    @Getter
    private final Map<String, String> staticsMap = new HashMap<>();

    /**
     * Middlewares registered to all routes
     */
    @Getter
    private final Set<Middleware> rootMiddlewares = new HashSet<>();

    /**
     * Middlewares registered to specified route
     */
    @Getter
    private final Map<String, Set<Middleware>> pathMiddlewares = new HashMap<>();

    /**
     * Session manager
     */
    @Getter
    private SessionManager sessionManager = new HttpSessionManager(Ehcache.newInstance("_SESSION_"), this);

    /**
     * Exception handler for request
     */
    @Getter
    private ExceptionHandler exceptionHandler = null;

    /**
     * Event emitter
     */
    @Getter
    private final EventEmitter eventEmitter = EventEmitter.newInstance();

    /**
     * App classes waiting for registering in IOC
     */
    private final Set<Class<?>> registerClassQueue = new HashSet<>();


    /**
     * Instances in App waiting for registering in IOC
     */
    private final Set<Object> registerInstanceQueue = new HashSet<>();

    /**
     * Initialize razor instance
     *
     * @return Razor instance
     */
    public static Razor self() {

        return new Razor();
    }

    /**
     * Specify the host and port for server listening
     *
     * @param host remote address
     * @param port port
     * @return Razor
     */
    public Razor listen(@NonNull String host, int port) {

        env.set(ENV_KEY_SERVER_HOST, host);
        env.set(ENV_KEY_SERVER_PORT, port);

        return this;
    }

    /**
     * Start the app server
     *
     * @param appClass app class
     * @param args other args
     */
    public void start(@NonNull Class<?> appClass, String[] args) {

        start(appClass, env.get(ENV_KEY_SERVER_HOST, DEFAULT_SERVER_HOST), env.getInt(ENV_KEY_SERVER_PORT, DEFAULT_SERVER_PORT), args);
    }

    /**
     * Start the app server
     *
     * @param appClass app class
     * @param host server binding host
     * @param port server binding port
     * @param args other args
     */
    public void start(@NonNull Class<?> appClass, @NonNull String host, int port, String[] args) {

        eventEmitter.emit(EventType.APP_START, this);

        try {

            env.set(ENV_KEY_SERVER_HOST, host);
            assert port >= 80 : "Port should be a positive value and greater or equal to 80";
            env.set(ENV_KEY_SERVER_PORT, port);
            this.appClass = appClass;

            this.startUp();

            new Thread(() -> {
                try {

                    nettyServer.run(Razor.this, args);
                    eventEmitter.emit(EventType.APP_STARTED, this);
                } catch (Exception e) {

                    log.error("Run razor in new thread failed, error: {}", e.getMessage());
                }
            }).start();
        } catch (Exception e) {

            log.error("Run razor failed, error: {}", e.getMessage());
        }
    }


    /**
     * Shutdown app and server
     */
    public void stop() {

        eventEmitter.emit(EventType.APP_STOP, this);

        // TODO calculate run time
        nettyServer.shutdown();
    }

    /**
     * Add static path rules
     *
     * @param rules static path rule, a full path or path prefix or file extension, e.g `/favicon.png`, `/statics/`, `.png`
     * @return Razor
     */
    public Razor addStatic(String... rules) {

        statics.addAll(Arrays.asList(rules));

        return this;
    }

    /**
     * Map a static route prefix to binding a specified server directory
     *
     * @param routePrefix route prefix
     * @param folder server directory
     * @return Razor self
     */
    public Razor mapStatic(String routePrefix, String folder) {

        if (!routePrefix.startsWith("/")) {

            routePrefix = "/".concat(routePrefix);
        }

        if (!folder.startsWith("/")) {

            folder = "/".concat(folder);
        }

        staticsMap.put(routePrefix, folder);

        return this;
    }

    /**
     * Specify web root dir of resource, default `WWW` which means a WWW folder under your resources folder, and WWW folder under your classpath when running
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

            env.set(ENV_KEY_WEB_ROOT_FOLDER, webDir);
            statics.add("/".concat(webDir).concat("/"));
        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return this;
    }

    /**
     * Specify other resources dir to replace default classpath resources dir, when your app run as a packaged jar(static resources also packaged), it's useful to apply this option
     * Note: any configuration files e.g app.xml, ehcache.xml and view templates should keep the old path and will be read at runtime.
     * So recommend that only copy web static files(not includes templates in the WEB-INF folder) to outer resources directory and keep same hierarchy as the old way
     *
     * @param resDir resources absolute directory
     * @return Razor self
     */
    public Razor resDir(String resDir) {

        if (!resDir.startsWith("/") && !Pattern.compile("^([a-zA-Z]):".concat(File.separator + File.separator)).matcher(resDir).find()) {

            log.error("Resource directory must be a absolute directory");

            return this;
        }

        try {

            env.set(ENV_KEY_RESOURCES_DIR, resDir);
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
        // TODO support
        try {

            env.set(ENV_KEY_SSL, ssl);
        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return this;
    }

    /**
     * Enable/disable gzip support
     *
     * @param gzip gzip enable status
     * @return Razor
     */
    public Razor gzip(boolean gzip) {

        try {

            env.set(ENV_KEY_GZIP, gzip);
        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return this;
    }


    /**
     * Customize 404 page
     *
     * @param template 404 page template file path, relative templates root path
     * @return Razor self
     */
    public Razor set404(String template) {

        env.set(ENV_KEY_404_PAGE_TEMPLATE, template);

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
     * Set template engine
     *
     * @param templateEngine template engine that implement {@link TemplateEngine}
     * @return Razor self
     */
    public Razor useTemplateEngine(TemplateEngine templateEngine) {

        TemplateEngineFactory.setTemplateEngine(templateEngine);

        return this;
    }

    /**
     * Select pre-defined template engine
     *
     * @param name template engine name
     * @return Razor self
     */
    public Razor useTemplateEngine(String name) {

        switch (name) {

            case "Jtwig":
                useTemplateEngine(new JtwigTemplateEngine());
                break;
            default:
                useTemplateEngine(new BeetlTemplateEngine());
        }

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
     * Register a class from App in IOC
     *
     * @param clazz class to register
     */
    public void registerClass(Class<?> clazz) {

        this.registerClassQueue.add(clazz);
    }

    /**
     * Register a instance from App in IOC
     *
     * @param instance instance to register
     */
    public void registerInstance(Object instance) {

        if (registerInstanceQueue.stream().anyMatch(x -> x.getClass() == instance.getClass())) {

            return;
        }
        this.registerInstanceQueue.add(instance);
    }

    /**
     * Initialize the container
     */
    private void initIoc() {

        iocBuilder = ContainerBuilder.getInstance(appClass);

        // register controllers
        iocBuilder.autoRegister(Controller.class);
        iocBuilder.autoRegister(APIController.class);

        iocBuilder.autoRegister(ExceptionHandler.class);

        iocBuilder.autoRegister(Session.class);
        iocBuilder.autoRegister(SessionManager.class);

        iocBuilder.autoRegister(Cache.class);

        iocBuilder.registerInstance(this);

        registerClassQueue.forEach(iocBuilder::autoRegister);
        registerInstanceQueue.forEach(iocBuilder::registerInstance);

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
            } catch (NoSuchMethodException e) {

                log.info(e.toString());
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
     * Initialize customized implements of some features
     */
    private void initImplements() {

        // Exception handler
        Set<Class<? extends ExceptionHandler>> exceptionHandlers = new Reflections(appClass.getPackage().getName()).getSubTypesOf(ExceptionHandler.class);
        if (exceptionHandlers.size() > 0) {

            this.exceptionHandler = ioc.resolve(exceptionHandlers.iterator().next());
        }

        // Session manager
        Set<Class<? extends SessionManager>> sessionManagers = new Reflections(appClass.getPackage().getName()).getSubTypesOf(SessionManager.class);
        if (sessionManagers.size() > 0) {

            this.sessionManager = ioc.resolve(sessionManagers.iterator().next());
        }
    }

    /**
     * Initialize runtime constants
     */
    private void initRuntime() {

        String resDir = env.get(ENV_KEY_RESOURCES_DIR, "");

        String templateAbsPath;
        if (resDir.equals("")) {

            String webAbsPath = APP_CLASS_PATH.concat(File.separator).concat(env.get(ENV_KEY_WEB_ROOT_FOLDER, DEFAULT_WEB_ROOT_FOLDER));
            env.set(ENV_RT_KEY_WEB_ROOT_ABS_PATH, webAbsPath);

            templateAbsPath = APP_CLASS_PATH.concat(File.separator).concat(env.get(ENV_KEY_TEMPLATE_ROOT_FOLDER, DEFAULT_TEMPLATE_ROOT_FOLDER));
            env.set(ENV_RT_KEY_TEMPLATE_ROOT_ABS_PATH, templateAbsPath);
        } else {

            templateAbsPath = resDir.concat(File.separator).concat(env.get(ENV_KEY_TEMPLATE_ROOT_FOLDER, DEFAULT_TEMPLATE_ROOT_FOLDER));
            env.set(ENV_RT_KEY_WEB_ROOT_ABS_PATH, resDir.concat(File.separator).concat(env.get(ENV_KEY_WEB_ROOT_FOLDER, DEFAULT_WEB_ROOT_FOLDER)));
            env.set(ENV_RT_KEY_TEMPLATE_ROOT_ABS_PATH, templateAbsPath);
        }

        String[] templates = new String[]{ENV_KEY_403_PAGE_TEMPLATE, ENV_KEY_404_PAGE_TEMPLATE, ENV_KEY_500_PAGE_TEMPLATE, ENV_KEY_502_PAGE_TEMPLATE};
        String[] keys = new String[]{ENV_RT_KEY_403_HTML, ENV_RT_KEY_404_HTML, ENV_RT_KEY_500_HTML, ENV_RT_KEY_502_HTML};

        for (int i=0; i<templates.length; i++) {

            if (env.get(templates[i]).isPresent()) {

                String html = FileKit.read(templateAbsPath.concat(env.get(templates[i]).get()));

                if (html != null) {

                    env.set(keys[i], html);
                }
            } else {

                env.set(keys[i], "");
            }
        }

        // TODO more

    }

    /**
     * Other preparations to be done at last
     */
    private void startUp() {

        HttpContext.init(this);

        this.initIoc();
        this.initMiddlewares();
        this.initRoutes();
        this.initImplements();
        this.initRuntime();

        log.info("App use resources root folder: {}", env.get(ENV_KEY_RESOURCES_DIR, APP_CLASS_PATH));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            log.info("App is shutting down!");

            this.stop();
        }));
    }
}
