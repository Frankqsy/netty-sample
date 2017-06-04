package org.daisy.stevin.netty.soa.consumer;

import io.netty.channel.Channel;
import org.daisy.stevin.netty.soa.Invocation;
import org.daisy.stevin.netty.soa.Invoker;
import org.daisy.stevin.netty.soa.RpcResult;
import org.daisy.stevin.netty.soa.utils.RpcClient;

import java.util.concurrent.ExecutionException;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class ConsumerInvoker<T> implements Invoker {
    private RpcClient rpcClient;
    private Class<T> clazz;

    public ConsumerInvoker() {
    }

    public ConsumerInvoker(Class<T> clazz) {
        this.clazz = clazz;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class getInterface() {
        return clazz;
    }

    @Override
    public RpcResult invoke(Invocation invocation) throws ExecutionException, InterruptedException {
        Channel channel = rpcClient.newChannel();
        RpcRequest request = new RpcRequest();
        request.setInvocation(invocation);
        channel.writeAndFlush(request);
        RpcFuture rpcFuture = new RpcFuture(request.getRpcId());
        return rpcFuture.get().getResult();
    }
}
