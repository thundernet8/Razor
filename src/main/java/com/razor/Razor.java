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
import com.razor.mvc.Controller;
import com.razor.mvc.route.RouteManager;
import com.razor.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.util.*;
import java.util.regex.Pattern;

import org.reflections.Reflections;

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

    public static Razor self() {

        return new Razor();
    }

    /**
     * Specify the host and port for server listening
     * @param host remote address
     * @param port port
     * @return Razor
     */
    public Razor listen(@NonNull String host, @NonNull int port) {

        env.set(ENV_KEY_SERVER_HOST, host);
        env.set(ENV_KEY_SERVER_PORT, port);

        return this;
    }

    public void start(@NonNull Class<?> appClass, String[] args) {

        start(appClass, DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT, args);
    }

    public void start(@NonNull Class<?> appClass, @NonNull String host, @NonNull int port, String[] args) {

        try {
            env.set(ENV_KEY_SERVER_HOST, host);
            assert port >= 80 : "Port should be a positive value and greater or equal to 80";
            env.set(ENV_KEY_SERVER_PORT, port);
            this.appClass = appClass;
            this.initIoc();
            this.initRoutes();
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
     * Specify content dir of resource, default `content` which means a content folder under your resources folder
     * @param contentDir content dir
     * @return Razor
     */
    public Razor content(String contentDir) {

        if (!Pattern.compile("^([^/.])([0-9a-zA-Z_]*)([^/.])$").matcher(contentDir).find()) {
            log.error("Content package should only include numbers, latin letters, _ or /, and should not start or end with /");
            return this;
        }
        try {
            env.set(ENV_KEY_RESOURCE_CONTENT_DIR, contentDir);
            statics.add("/".concat(contentDir).concat("/"));
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

    // IOC
    private void initIoc() {

        iocBuilder = ContainerBuilder.getInstance(appClass);
        // register controllers
        iocBuilder.autoRegister(Controller.class);

        ioc = iocBuilder.build();
    }

    // Routes
    private void initRoutes() {

        RouteManager.getInstance(appClass).registerRoutes();
    }

    /**
     * Other preparations
     */
    private void startUp() {

        // add content folder path to statics paths
        String contentDir = env.get(ENV_KEY_RESOURCE_CONTENT_DIR, DEFAULT_RESOURCE_CONTENT_DIR);
        statics.add("/".concat(contentDir).concat("/"));
    }
}
