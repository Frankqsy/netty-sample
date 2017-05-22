package org.daisy.stevin.netty.sample.client.uds;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.util.CharsetUtil;

public class UdsClient {
    public static void main(String[] args) throws Exception {
        String path = "/data/uds/auds.sock";
        if (args != null && args.length > 0) {
            path = args[0];
        } else {
            System.out.println("no abtract namespace path,use default!");
        }
        path = "\0" + path;
        new UdsClient().start(path);
    }

    public void start(String path) throws Exception {
        final UdsClientHandler clientHandler = new UdsClientHandler();
        EventLoopGroup group = new EpollEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            //@formatter:off
            b.group(group)
             .channel(EpollDomainSocketChannel.class)
             .handler(new ChannelInitializer<DomainSocketChannel>() {
                @Override
                protected void initChannel(DomainSocketChannel ch) throws Exception {
                     ch.pipeline().addLast(clientHandler);                
                }
              });
            //@formatter:on
            ChannelFuture f = b.connect(new DomainSocketAddress(path)).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    @Sharable
    private class UdsClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("client channel active!");
            // ctx.writeAndFlush(Unpooled.copiedBuffer("Netty uds!", CharsetUtil.UTF_8));
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            System.out.println("Client received: " + msg.toString(CharsetUtil.UTF_8));
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("Error occur when reading from Unix domain socket: " + cause.getMessage());
            cause.printStackTrace();
            ctx.close();
        }
    }
}
