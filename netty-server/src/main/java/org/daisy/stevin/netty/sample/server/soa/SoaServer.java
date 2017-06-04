package org.daisy.stevin.netty.sample.server.soa;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.daisy.stevin.netty.sample.server.soa.facade.HelloService;
import org.daisy.stevin.netty.sample.server.soa.impl.HelloServiceImpl;
import org.daisy.stevin.netty.soa.handler.DecoderHandler;
import org.daisy.stevin.netty.soa.handler.EncoderHandler;
import org.daisy.stevin.netty.soa.handler.RpcServerHandler;
import org.daisy.stevin.netty.soa.provider.RpcProvider;
import org.daisy.stevin.netty.soa.utils.Constant;

/**
 * Created by shaoyang.qi on 2017/5/21.
 */
public class SoaServer {
    private final int port;
    public static boolean isSSL;

    public SoaServer(int port) {
        this.port = port;
    }

    private void registerServices() {
        RpcProvider.getInstance().addInvoker(HelloService.class.getSimpleName(), HelloServiceImpl.class);
    }

    private void startServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DecoderHandler());
                    ch.pipeline().addLast(new RpcServerHandler());
                    ch.pipeline().addLast(new EncoderHandler());
                }
            });
            Channel ch = b.bind(port).sync().channel();
            System.out.println(String.format("HTTP Upload server at port %d.", port));
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void run() throws Exception {
        registerServices();
        startServer();
    }

    public static void main(String[] args) throws Exception {
        int port = args != null && args.length > 0 ? Integer.parseInt(args[0]) : Constant.DEFAULT_PORT;
        isSSL = args != null && args.length > 1 ? true : false;
        new SoaServer(port).run();
    }
}
