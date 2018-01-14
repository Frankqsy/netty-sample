package org.daisy.stevin.netty.sample.server.restful;

import io.netty.channel.ChannelOption;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by shaoyang.qi on 2018/1/14
 */
@Component
public class NettyServer {
    @Autowired
    private ApplicationContext ac;
    private NettyJaxrsServer nettyJaxrsServer;
    private String rootResourcePath = "/";
    private int port = 8081;
    private boolean isSSL = false;

    public void start() {
        ResteasyDeployment deployment = new ResteasyDeployment();
        Collection<Object> providers = ac.getBeansWithAnnotation(Provider.class).values();
        Collection<Object> controllers = ac.getBeansWithAnnotation(Controller.class).values();
        Objects.requireNonNull(controllers);
        if (Objects.nonNull(providers)) {
            deployment.getProviders().addAll(providers);
        }
        // extract only controller annotated beans
        deployment.getResources().addAll(controllers);
        nettyJaxrsServer = new NettyJaxrsServer();
        nettyJaxrsServer.setChannelOptions(Collections.singletonMap(ChannelOption.SO_REUSEADDR, true));
        nettyJaxrsServer.setDeployment(deployment);
        nettyJaxrsServer.setPort(port);
        nettyJaxrsServer.setRootResourcePath(rootResourcePath);
        nettyJaxrsServer.setSecurityDomain(null);
        nettyJaxrsServer.start();
    }

    @PreDestroy
    public void cleanUp() {
        nettyJaxrsServer.stop();
    }

    public String getRootResourcePath() {
        return rootResourcePath;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSSL(boolean SSL) {
        isSSL = SSL;
    }
}
