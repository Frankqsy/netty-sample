package org.daisy.stevin.netty.soa.consumer;

import org.daisy.stevin.netty.soa.Invocation;
import org.daisy.stevin.netty.soa.provider.RpcResponse;
import org.daisy.stevin.netty.soa.utils.Constant;
import org.daisy.stevin.netty.soa.utils.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcConsumer {
    public static <T> T newProxy(final Class<T> clazz) throws Exception {
        return newProxy(clazz, Constant.LOCAL_HOST, Constant.DEFAULT_PORT);
    }

    public static <T> T newProxy(final Class<T> clazz, String host, int port) throws Exception {
        RpcClient rpcClient = new RpcClient(host, port);
        final ConsumerInvoker consumerInvoker = new ConsumerInvoker(clazz);
        consumerInvoker.setRpcClient(rpcClient);
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation invocation = new Invocation();
                invocation.setClazz(clazz);
                invocation.setMethodName(method.getName());
                invocation.setParameterTypes(method.getParameterTypes());
                invocation.setArguments(args);

                return consumerInvoker.invoke(invocation).getResult();
            }
        };
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, invocationHandler);
    }

    public static void receive(RpcResponse response) {
        if (response == null) {
            return;
        }
        RpcFuture rpcFuture = RpcFuture.getFuture(response.getRpcId());
        if (rpcFuture != null) {
            rpcFuture.receive(response);
        }
    }
}
