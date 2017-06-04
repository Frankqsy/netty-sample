package org.daisy.stevin.netty.soa.provider;

import io.netty.util.internal.StringUtil;
import org.daisy.stevin.netty.soa.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcProvider {
    private final Map<String, Invoker> invokers = new ConcurrentHashMap<>();

    private RpcProvider() {
    }

    private static class INNER {
        private static final RpcProvider INSTANCE = new RpcProvider();
    }

    public static RpcProvider getInstance() {
        return INNER.INSTANCE;
    }

    public Invoker getInvoker(String className) {
        return invokers.get(className);
    }

    public void addInvoker(String interfaceName, Class<?> clazz) {
        if (StringUtil.isNullOrEmpty(interfaceName)) {
            return;
        }
        Invoker invoker = new ProviderInvoker(clazz);
        invokers.put(interfaceName, invoker);
    }

}
