package org.daisy.stevin.netty.spring;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.*;

public class NettyHttpServletUtil {
    public static DispatcherServlet getDispatcherServletFromXml(String configLocation) throws ServletException {
        XmlWebApplicationContext appContext = new XmlWebApplicationContext();
        ContextLoader contextLoader = new ContextLoader(appContext);

        ServletContext servletContext = new NettyServletContext();
        servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, configLocation);
        contextLoader.initWebApplicationContext(servletContext);
        ServletConfig servletConfig = new NettyServletConfig(servletContext);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        dispatcherServlet.init(servletConfig);
        return dispatcherServlet;
    }

    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer(String.format("Failure: %s\r\n", status.toString()), CharsetUtil.UTF_8);

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    public static void writeResponse(ChannelHandlerContext ctx, NettyHttpServletRequest servletRequest, NettyHttpServletResponse servletResponse) throws IOException {
        // Decide whether to close the connection or not.
        // boolean keepAlive = isKeepAlive(request);
        FullHttpResponse response = createFullHttpResponse(servletResponse);

        // if (keepAlive) {
        // // Add 'Content-Length' header only for a keep-alive connection.
        // response.setHeader(CONTENT_LENGTH,
        // response.getContent().readableBytes());
        // // Add keep alive header as per:
        // // -
        // //
        // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
        // response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        // }
        copyCookiesFromNettyRequest(servletRequest, response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public static void copyCookiesFromNettyRequest(NettyHttpServletRequest servletRequest, FullHttpResponse response) {
        NettyHttpSession httpSession = (NettyHttpSession) servletRequest.getSession();
        List<Cookie> cookies = new ArrayList<>();
        Enumeration<String> attriNames = httpSession.getAttributeNames();
        while (attriNames.hasMoreElements()) {
            String name = attriNames.nextElement();
            Object value = httpSession.getAttribute(name);
            if (ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
                cookies.add(new DefaultCookie(name, value.toString()));
            }
        }
        response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookies));
    }

    public static FullHttpResponse createFullHttpResponse(NettyHttpServletResponse servletResponse) throws IOException {
        if (servletResponse == null) {
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        }
        StringBuilder sbuf = new StringBuilder();
        if (servletResponse.getStringWriter() != null) {
            sbuf.append(servletResponse.getStringWriter().getBuffer().toString());
            servletResponse.flushBuffer();
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(servletResponse.getStatus()),
                Unpooled.copiedBuffer(sbuf.toString(), CharsetUtil.UTF_8));
        servletResponse.getHeaderNames().forEach((header) -> {
            Collection<String> collection = servletResponse.getHeaders(header);
            collection.forEach((element) -> response.headers().add(header, element));
        });
        if (!response.headers().contains("Content-Type")) {
            response.headers().add("Content-Type", servletResponse.getContentType());
        }
        return response;
    }

    public static NettyHttpServletRequest createNettyServletRequest(FullHttpRequest fullHttpRequest, ServletContext servletContext) {
        NettyHttpSession httpSession = createNettyHttpSession(fullHttpRequest, servletContext);
        String method = fullHttpRequest.method().name();
        byte[] array = null;
        if (fullHttpRequest.content().hasArray()) {
            array = fullHttpRequest.content().array();
        }
        String contentType = fullHttpRequest.headers().get("Content-type");
        NettyHttpServletRequest servletRequest = new NettyHttpServletRequest(fullHttpRequest.uri(), servletContext, method, array, contentType, httpSession);
        fullHttpRequest.headers().names().forEach((name) -> {
            List<String> values = fullHttpRequest.headers().getAll(name);
            values.forEach((value) -> servletRequest.addHeader(name, value));
        });
        return servletRequest;
    }

    public static NettyHttpSession createNettyHttpSession(FullHttpRequest request, ServletContext servletContext) {
        NettyHttpSession httpSession = new NettyHttpSession(servletContext);
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (NettyStringUtil.isEmpty(cookieString)) {
            return httpSession;
        }
        Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);
        cookies.forEach((cookie) -> httpSession.setAttribute(cookie.name(), cookie.value()));
        return httpSession;
    }

}
