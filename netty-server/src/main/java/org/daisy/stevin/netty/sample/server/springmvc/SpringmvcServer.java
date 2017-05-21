package org.daisy.stevin.netty.sample.server.springmvc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by shaoyang.qi on 2017/5/21.
 */
public class SpringmvcServer {
    private final int port;
    public static boolean isSSL;

    public SpringmvcServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new HttpServerChInitializer());
            Channel ch = b.bind(port).sync().channel();
            System.out.println(String.format("Springmvcserver start at port %d.", port));
            System.out.println(String.format("Open your browser and navigate to http://localhost:%d/", port));
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = args != null && args.length > 0 ? Integer.parseInt(args[0]) : 8081;
        isSSL = args != null && args.length > 1 ? true : false;
        new SpringmvcServer(port).run();
    }
}
