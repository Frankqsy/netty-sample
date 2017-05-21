package org.daisy.stevin.netty.sample.server.springmvc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.daisy.stevin.netty.spring.NettyServletRequest;
import org.daisy.stevin.netty.spring.NettyServletResponse;
import org.daisy.stevin.netty.spring.NettyServletUtil;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Servlet servlet;
    private final ServletContext servletContext;

    public HttpRequestHandler(Servlet servlet) {
        this.servlet = servlet;
        this.servletContext = this.servlet.getServletConfig().getServletContext();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            NettyServletUtil.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            NettyServletUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        NettyServletRequest servletRequest = NettyServletUtil.createNettyServletRequest(fullHttpRequest, servletContext);
        NettyServletResponse servletResponse = new NettyServletResponse();

        this.servlet.service(servletRequest, servletResponse);

        if (servletResponse.isError()) {
            NettyServletUtil.sendError(ctx, HttpResponseStatus.valueOf(servletResponse.getStatus()));
            return;
        }

        NettyServletUtil.writeResponse(ctx, servletRequest, servletResponse);
    }

}
