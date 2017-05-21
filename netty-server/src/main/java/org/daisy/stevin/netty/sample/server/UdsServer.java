package org.daisy.stevin.netty.sample.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.util.CharsetUtil;

public class UdsServer {
    public static void main(String[] args) throws Exception {
        new UdsServer().start("\0/data/uds/auds.sock");
    }

    public void start(String path) throws Exception {
        final UdsServerHandler serverHandler = new UdsServerHandler();
        EventLoopGroup group = new EpollEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            //@formatter:off
            b.group(group)
             .channel(EpollServerDomainSocketChannel.class)
             .childHandler(new ChannelInitializer<DomainSocketChannel>() {
                @Override
                protected void initChannel(DomainSocketChannel ch) throws Exception {
                     ch.pipeline().addLast(serverHandler);                
                }
              });
            //@formatter:on
            ChannelFuture f = b.bind(new DomainSocketAddress(path)).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    @Sharable
    private class UdsServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server channel active!");
            ctx.writeAndFlush(Unpooled.wrappedBuffer("hello,this is uds server~".getBytes()));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8));
            ctx.write(in);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
