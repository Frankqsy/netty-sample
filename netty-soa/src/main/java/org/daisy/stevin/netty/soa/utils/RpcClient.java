package org.daisy.stevin.netty.soa.utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.daisy.stevin.netty.soa.handler.DecoderHandler;
import org.daisy.stevin.netty.soa.handler.EncoderHandler;
import org.daisy.stevin.netty.soa.handler.RpcClientHandler;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcClient {
    private final String host;
    private final int port;
    private Bootstrap bootstrap;

    public RpcClient() throws Exception {
        this(Constant.LOCAL_HOST, Constant.DEFAULT_PORT);
    }

    public RpcClient(String host, int port) throws Exception {
        this.host = host;
        this.port = port;
        init();
    }

    private void init() throws Exception {
        EventLoopGroup workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new DecoderHandler());
                ch.pipeline().addLast(new RpcClientHandler());
                ch.pipeline().addLast(new EncoderHandler());
            }
        });
    }

    public Channel newChannel() throws InterruptedException {
        ChannelFuture cf = bootstrap.connect(host, port).sync();
        Channel channel = cf.channel();
        return channel;
    }
}
