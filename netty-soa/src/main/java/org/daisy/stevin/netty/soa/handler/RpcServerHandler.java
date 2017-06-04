package org.daisy.stevin.netty.soa.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.daisy.stevin.netty.soa.Invocation;
import org.daisy.stevin.netty.soa.Invoker;
import org.daisy.stevin.netty.soa.RpcResult;
import org.daisy.stevin.netty.soa.consumer.RpcRequest;
import org.daisy.stevin.netty.soa.provider.RpcProvider;
import org.daisy.stevin.netty.soa.provider.RpcResponse;
import org.daisy.stevin.netty.soa.template.AbstractRpcHandleCallback;
import org.daisy.stevin.netty.soa.template.RpcHandleTemplate;
import org.daisy.stevin.netty.soa.template.RpcResultFactory;
import org.daisy.stevin.netty.soa.utils.RpcResultCode;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcServerHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!(msg instanceof RpcRequest)) {
            throw new IllegalArgumentException("object type error: " + msg.toString());
        }
        RpcRequest request = (RpcRequest) msg;
        final RpcResult rpcResult = new RpcResult();
        RpcHandleTemplate.execute(rpcResult, new AbstractRpcHandleCallback() {
            @Override
            public void process() throws Exception {
                Invocation invocation = request.getInvocation();
                Invoker invoker = RpcProvider.getInstance().getInvoker(invocation.getClazz().getSimpleName());
                if (invoker == null) {
                    RpcResultFactory.constructRpcResult(rpcResult, RpcResultCode.INVOKER_NOT_FOUND, null);
                    return;
                }
                RpcResult result = invoker.invoke(invocation);
                RpcResultFactory.constructRpcResult(rpcResult, RpcResultCode.getTypeByCode(result.getResultCode()), result.getResult());
            }
        });
        RpcResponse response = new RpcResponse();
        response.setRpcId(request.getRpcId());
        response.setResult(rpcResult);
        channel.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
