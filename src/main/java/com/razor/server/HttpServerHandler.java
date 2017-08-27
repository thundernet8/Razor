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


package com.razor.server;

import com.razor.Razor;
import com.razor.ioc.IContainer;
import com.razor.mvc.Controller;
import com.razor.mvc.http.Request;
import com.razor.mvc.http.Response;
import com.razor.mvc.route.RouteManager;
import com.razor.mvc.route.RouteSignature;
import com.razor.mvc.route.Router;
import com.razor.mvc.route.UrlParameter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.ReferenceCountUtil;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * Default ChannelInboundHandler
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private Razor razor;

    public HttpServerHandler(Razor razor) {
        this.razor = razor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // super.channelRead(ctx, msg);
        if (msg instanceof FullHttpRequest)
        {
            final FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            final String responseMessage = "Hello from Netty!";
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    copiedBuffer(responseMessage.getBytes())
            );

            if (HttpUtil.isKeepAlive(fullHttpRequest))
            {
                fullHttpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            fullHttpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            fullHttpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
            fullHttpResponse.headers().set("X-Power-By", "Razor");

            // TODO
            Request request = Request.build(ctx, fullHttpRequest);
            Response response = null;
            RouteSignature routeSignature = RouteSignature.builder().request(request).response(response).build();

            Router router = RouteManager.getInstance(razor.getAppClass()).findRoute(request.uri(), fullHttpRequest.method().name());
            if (router != null) {
                routeSignature.setRouter(router);
                this.handleRoute(ctx, routeSignature);
            } else {
                // TODO 404
                ctx.write(fullHttpResponse);
            }

            //ctx.write(fullHttpResponse);

            ReferenceCountUtil.release(msg);
        }
        else
        {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // super.channelReadComplete(ctx);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // super.exceptionCaught(ctx, cause);

        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));

        cause.printStackTrace();
        ctx.close();
    }

    private void handleRoute(ChannelHandlerContext ctx, RouteSignature signature) {

        IContainer ioc = razor.getIoc();
        Class<?> controllerClass = signature.getRouter().getTargetType();

        if (controllerClass.getSuperclass() != Controller.class) {
            // throw exception
        }

        Controller controller = (Controller)ioc.resolve(controllerClass);
        Method action = signature.getRouter().getAction();
        UrlParameter[] params = signature.getRouter().getRouteMatcher().getParams(signature.request().uri());
        try {
            String result = action.invoke(controller, Arrays.stream(params).map(p -> p.getValue()).toArray(Object[]::new)).toString();
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    copiedBuffer(result.getBytes())
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    copiedBuffer(e.getMessage().getBytes())
            ));
        }

        // TODO
    }
}
