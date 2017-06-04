package org.daisy.stevin.netty.soa;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public interface Invoker<T> {
    Class<T> getInterface();

    RpcResult invoke(Invocation invocation) throws Exception;
}
