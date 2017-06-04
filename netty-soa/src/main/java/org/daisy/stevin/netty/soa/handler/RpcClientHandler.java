package org.daisy.stevin.netty.soa.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.daisy.stevin.netty.soa.consumer.RpcConsumer;
import org.daisy.stevin.netty.soa.provider.RpcResponse;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcClientHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!(msg instanceof RpcResponse)) {
            throw new IllegalArgumentException("object type error: " + msg.toString());
        }
        RpcResponse response = (RpcResponse) msg;
        RpcConsumer.receive(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
