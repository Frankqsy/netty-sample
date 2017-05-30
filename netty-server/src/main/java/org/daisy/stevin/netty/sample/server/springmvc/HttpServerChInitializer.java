package org.daisy.stevin.netty.sample.server.springmvc;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.daisy.stevin.netty.spring.NettyHttpServletUtil;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;

public class HttpServerChInitializer extends ChannelInitializer<SocketChannel> {
    private final DispatcherServlet dispatcherServlet;

    public HttpServerChInitializer() throws ServletException {
        this("classpath*:springmvc-*.xml");
    }

    public HttpServerChInitializer(String configLocation) throws ServletException {
        this.dispatcherServlet = NettyHttpServletUtil.getDispatcherServletFromXml(configLocation);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();
        // http-request解码器 http服务器端对request解码
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        // http-response编码器 http服务器端对response编码
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // 压缩
        // Compresses an HttpMessage and an HttpContent in gzip or deflate encoding
        // while respecting the "Accept-Encoding" header.
        // If there is no matching encoding, no compression is done.
        //
        pipeline.addLast("deflater", new HttpContentCompressor());
        pipeline.addLast("handler", new HttpRequestHandler(this.dispatcherServlet));
    }

}
