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


package com.fedepot.server;

import com.fedepot.Razor;
import com.fedepot.exception.ExceptionHandler;
import com.fedepot.exception.RazorException;
import com.fedepot.ioc.IContainer;
import com.fedepot.mvc.controller.APIController;
import com.fedepot.mvc.controller.Controller;
import com.fedepot.mvc.http.*;
import com.fedepot.mvc.middleware.CookieParserMiddleware;
import com.fedepot.mvc.route.RouteSignature;
import com.fedepot.mvc.route.Router;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static com.fedepot.mvc.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.buffer.Unpooled.copiedBuffer;


/**
 * Default http server handler
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private Razor razor;

    private StaticFileHandler staticFileHandler;

    private SessionHandler sessionHandler;

    private ExceptionHandler exceptionHandler;

    private final static ExecutorService workerThreadService = newBlockingExecutorUseCallerRun(Runtime.getRuntime().availableProcessors() * 2);

    private static ExecutorService newBlockingExecutorUseCallerRun(int size) {

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Razor-BlockingExecutor-pool-%d").build();

        return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), threadFactory, (r, executor) -> {

            try {

                executor.getQueue().put(r);
            } catch (InterruptedException e) {

                throw new RuntimeException(e);
            }
        });
    }

    HttpServerHandler(Razor razor) {

        this.razor = razor;
        this.staticFileHandler = new StaticFileHandler(razor);
        this.sessionHandler = razor.getSessionManager() != null ? new SessionHandler(razor) : null;
        this.exceptionHandler = razor.getExceptionHandler();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpRequest) {

            final FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            workerThreadService.execute(() -> {

                handleMessage(ctx, fullHttpRequest);
            });
        } else {

            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        super.exceptionCaught(ctx, cause);

        ctx.writeAndFlush(new DefaultFullHttpResponse(

                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));

        // TODO dev usage
        cause.printStackTrace();

        ctx.close();

        log.error(cause.getMessage());
    }

    private void handleMessage(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {

        // TODO
        // HEAD request support

        Request request = Request.build(ctx, fullHttpRequest, sessionHandler);
        Response response = Response.build(ctx);

        HttpContext.set(new HttpContext(request, response));

        try {

            if (request.isStatic()) {

                staticFileHandler.handle(ctx, request, response);
                return;
            }

            // prepare cookies for session
            new CookieParserMiddleware().apply(request, response);

            // check session
            if (!request.method().equals(HttpMethod.OPTIONS)) {
                request.session();
            }

            // TODO complete RouteSignature
            RouteSignature routeSignature = RouteSignature.builder().request(request).response(response).build();
            Router router = request.router();

            if (router != null) {

                routeSignature.setRouter(router);
                this.handleRoute(ctx, routeSignature);
            } else {

                response.notFound();
            }

        } catch (Exception e) {

            if (!response.flushed()) {

                response.interanlError();
            }

            if (this.exceptionHandler != null) {

                exceptionHandler.handle(e, razor);
            } else {

                log.error("Handle inbound message failed", e);
            }

        } finally {

            if (!response.flushed()) {

                response.end();
            }

            HttpContext.remove();
        }
    }

    private void handleRoute(ChannelHandlerContext ctx, RouteSignature signature) throws Exception {

        Request request = signature.request();
        Response response = signature.response();

        applyMiddlewares(signature);

        // default cors action
        if (request.method().equals(HttpMethod.OPTIONS)) {
            if (!response.flushed()) {
                if (request.getOrigin() != null) {

                    response.sendStatus(405);
                } else {

                    response.sendStatus(200);
                }
            }

            return;
        }

        IContainer ioc = razor.getIoc();
        Class<?> controllerClass = signature.getRouter().getTargetType();
        Class<?> superClass = controllerClass.getSuperclass();

        if (superClass != Controller.class && superClass != APIController.class) {

            throw new RazorException(controllerClass.getName() + " is not a controller or api controller");
        }

        Object controller = ioc.resolve(controllerClass);

        if (controller == null) {

            response.interanlError();
            return;
        }

        Method action = signature.getRouter().getAction();

        try {

            Object result;
            if (action.getParameterTypes().length == 0) {

                result = action.invoke(controller);
            } else {

                Object[] args = signature.getParameters();
                result = action.invoke(controller, args);
            }

            Class<?> returnType = action.getReturnType();

            if (response.flushed()) {

                return;
            }

            if (returnType == Void.TYPE) {

                result = "";
            }

            if (response.get(CONTENT_TYPE) == null) {

                response.header(CONTENT_TYPE, ContentType.TEXT.getMimeTypeWithCharset());
            }

            response.end(ActionResult.build(result, returnType).getBytes());
        } catch (Exception e) {

            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Apply registered middlewares before action execution
     *
     * @param signature RouteSignature
     */
    private void applyMiddlewares(RouteSignature signature) {

        Router router = signature.getRouter();
        IContainer ioc = razor.getIoc();
        router.getMiddlewares().forEach(middleware -> {
            middleware = ioc.resolve(middleware);
            if (signature.response() == null || !signature.response().flushed()) {
                middleware.apply(signature.request(), signature.response());
            }
        });
    }
}
