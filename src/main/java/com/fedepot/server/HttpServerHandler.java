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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static io.netty.buffer.Unpooled.copiedBuffer;


/**
 * Default http server handler
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private Razor razor;

    private StaticFileHandler staticFileHandler;

    private SessionHandler sessionHandler;

    private ExceptionHandler exceptionHandler;

    HttpServerHandler(Razor razor) {

        this.razor = razor;
        this.staticFileHandler = new StaticFileHandler(razor);
        this.sessionHandler = razor.getSessionManager() != null ? new SessionHandler(razor) : null;
        this.exceptionHandler = razor.getExceptionHandler();
    }

    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {

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
            request.session();

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

                throw e;
            }

        } finally {

            if (!response.flushed()) {

                response.end();
            }

            HttpContext.remove();
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

    private void handleRoute(ChannelHandlerContext ctx, RouteSignature signature) throws Exception {

        Request request = signature.request();
        Response response = signature.response();

        applyMiddlewares(signature);

        // default cors action
        if (request.method().equals(HttpMethod.OPTIONS)) {

            if (!response.flushed() && request.getOrigin() != null) {

                response.sendStatus(405);
            } else {

                response.sendStatus(200);
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
        router.getMiddlewares().forEach(middleware -> {

            middleware.apply(signature.request(), signature.response());
        });
    }
}
