package org.daisy.stevin.netty.spring;

import org.springframework.beans.BeanUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NettyAsyncContext implements AsyncContext {
    private ServletRequest servletRequest;
    private ServletResponse servletResponse;
    private long timeout = 10 * 1000L;
    private List<AsyncListener> asyncListeners = new ArrayList<>();

    public NettyAsyncContext(ServletRequest servletRequest, ServletResponse servletResponse) {
        Objects.requireNonNull(servletRequest, () -> "ServletRequest cannot be null in SpringAsyncContext");
        Objects.requireNonNull(servletResponse, () -> "ServletResponse cannot be null in SpringAsyncContext");
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    @Override
    public ServletRequest getRequest() {
        return servletRequest;
    }

    @Override
    public ServletResponse getResponse() {
        return servletResponse;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return true;
    }

    @Override
    public void dispatch() {
        HttpServletRequest httpRequest = (HttpServletRequest) getRequest();
        String path = httpRequest.getRequestURI();
        dispatch(path);
    }

    @Override
    public void dispatch(String path) {
        dispatch(servletRequest.getServletContext(), path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        HttpServletRequest httpRequest = (HttpServletRequest) getRequest();
        servletRequest.setAttribute(ASYNC_REQUEST_URI, httpRequest.getRequestURI());
        servletRequest.setAttribute(ASYNC_CONTEXT_PATH, httpRequest.getContextPath());
        servletRequest.setAttribute(ASYNC_SERVLET_PATH, httpRequest.getServletPath());
        servletRequest.setAttribute(ASYNC_QUERY_STRING, httpRequest.getQueryString());
    }

    @Override
    public void complete() {
        try {
            servletResponse.flushBuffer();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for (AsyncListener asyncListener : asyncListeners) {
            try {
                asyncListener.onComplete(new AsyncEvent(this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void addListener(AsyncListener listener) {
        asyncListeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
        this.asyncListeners.add(listener);
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        return BeanUtils.instantiateClass(clazz);
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
