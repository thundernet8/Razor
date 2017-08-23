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
import com.razor.env.Env;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.Channel;

import static com.razor.mvc.Constants.*;

/**
 * Netty Http Server
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class NettyServer {

    private Razor razor;
    private Env env;

    private Channel channel;
    private final NioEventLoopGroup masterGroup;
    private final NioEventLoopGroup slaveGroup;

    public NettyServer() {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }

    public void start(Razor razor, String[] args) throws Exception {
        this.razor = razor;
        this.env = razor.getEnv();

        this.startServer();
    }

    private void startServer() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() { shutdown(); }
        });

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer(razor))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            this.channel = bootstrap.bind(env.get(ENV_KEY_SERVER_HOST, DEFAULT_SERVER_HOST), env.getInt(ENV_KEY_SERVER_PORT, DEFAULT_SERVER_PORT)).sync().channel();
            log.info("{} started and listen on {}", HttpServerHandler.class.getName(), channel.localAddress());
        } catch (final InterruptedException e){
            log.error("Netty server startup failed, error: {}", e.getMessage());
        }
    }

    public void shutdown() {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Netty server shutdown failed, error: {}", e.getMessage());
        }
    }
}
