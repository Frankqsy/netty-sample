package org.daisy.stevin.netty.soa.provider;

import org.daisy.stevin.netty.soa.Invocation;
import org.daisy.stevin.netty.soa.Invoker;
import org.daisy.stevin.netty.soa.RpcResult;
import org.daisy.stevin.netty.soa.template.RpcResultFactory;
import org.daisy.stevin.netty.soa.utils.RpcResultCode;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class ProviderInvoker<T> implements Invoker<T> {
    private Class<T> clazz;

    public ProviderInvoker(Class<T> clazz) {
        Objects.requireNonNull(clazz, () -> "clazz must not be null!");
        this.clazz = clazz;
    }

    @Override
    public Class<T> getInterface() {
        return clazz;
    }

    @Override
    public RpcResult invoke(Invocation invocation) throws Exception {
        Method method = clazz.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        Object instance = clazz.newInstance();
        Object result = method.invoke(instance, invocation.getArguments());
        return RpcResultFactory.constructRpcResult(RpcResultCode.SUCCESS, result);
    }
}
