package org.daisy.stevin.netty.spring;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import java.util.Enumeration;

public class NettyServletConfig implements ServletConfig {
    private ServletContext servletContext;

    public NettyServletConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return this.servletContext.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return this.servletContext.getInitParameterNames();
    }

}
