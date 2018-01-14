package org.daisy.stevin.netty.sample.server.restful;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Objects;

/**
 * Created by shaoyang.qi on 2017/5/21.
 */
public class RestfulServer {
    private final int port;
    public static boolean isSSL;

    public RestfulServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("restful-context.xml");
        Objects.requireNonNull(ac, "ApplicationContext is required not null");
        NettyServer nettyServer = ac.getBean(NettyServer.class);
        nettyServer.setPort(port);
        nettyServer.setSSL(isSSL);
        nettyServer.start();
        System.out.println(String.format("RestfulServer start at port %d.", port));
        System.out.println(String.format("Open your browser and navigate to http://localhost:%d%s", port, nettyServer.getRootResourcePath()));
    }

    public static void main(String[] args) throws Exception {
        int port = args != null && args.length > 0 ? Integer.parseInt(args[0]) : 8081;
        isSSL = args != null && args.length > 1 ? true : false;
        new RestfulServer(port).run();
    }
}
