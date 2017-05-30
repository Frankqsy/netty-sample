package org.daisy.stevin.netty.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Created by shaoyang.qi on 2017/5/29.
 */
public class NettyRequestDispatcher implements RequestDispatcher {

    private final Log logger = LogFactory.getLog(getClass());

    private final String resource;


    public NettyRequestDispatcher(String resource) {
        Assert.notNull(resource, "resource must not be null");
        this.resource = resource;
    }


    @Override
    public void forward(ServletRequest request, ServletResponse response) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(response, "Response must not be null");
        if (response.isCommitted()) {
            throw new IllegalStateException("Cannot perform forward - response is already committed");
        }
        getNettyServletResponse(response).setForwardedUrl(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("NettyRequestDispatcher: forwarding to [" + this.resource + "]");
        }
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(response, "Response must not be null");
        getNettyServletResponse(response).addIncludedUrl(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("NettyRequestDispatcher: including [" + this.resource + "]");
        }
    }

    /**
     * Obtain the underlying {@link NettyHttpServletResponse}, unwrapping
     * {@link HttpServletResponseWrapper} decorators if necessary.
     */
    protected NettyHttpServletResponse getNettyServletResponse(ServletResponse response) {
        if (response instanceof NettyHttpServletResponse) {
            return (NettyHttpServletResponse) response;
        }
        if (response instanceof HttpServletResponseWrapper) {
            return getNettyServletResponse(((HttpServletResponseWrapper) response).getResponse());
        }
        throw new IllegalArgumentException("NettyRequestDispatcher requires NettyServletResponse");
    }

}
