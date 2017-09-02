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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
//import io.netty.handler.codec.http.cors.CorsConfig;
//import io.netty.handler.codec.http.cors.CorsConfigBuilder;
//import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Netty channel initializer
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private Razor razor;

    HttpServerInitializer(Razor razor) {

        this.razor = razor;
    }

    @Override
    public void initChannel(final SocketChannel socketChannel) throws Exception {

        ChannelPipeline pl = socketChannel.pipeline();

        // TODO ssl handler

        pl.addLast("codec", new HttpServerCodec());

        // enable gzip
        pl.addLast("gzip", new HttpContentCompressor());

        pl.addLast("continue", new HttpServerExpectContinueHandler());
        pl.addLast("aggregator", new HttpObjectAggregator(512*1024));
        pl.addLast("chunk", new ChunkedWriteHandler());

        // cors
//        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().maxAge(60l).build();
//        pl.addLast("cors", new CorsHandler(corsConfig));

        pl.addLast("request", new HttpServerHandler(razor));
    }
}
