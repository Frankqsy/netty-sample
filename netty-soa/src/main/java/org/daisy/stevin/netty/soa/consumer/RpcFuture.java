package org.daisy.stevin.netty.soa.consumer;

import org.daisy.stevin.netty.soa.provider.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcFuture {
    private static final Map<Long, RpcFuture> FUTURES = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();
    private Condition done = lock.newCondition();
    private RpcResponse response;

    public RpcFuture(long rpcId) {
        FUTURES.put(rpcId, this);
    }

    public RpcResponse get() throws InterruptedException {
        if (isDone()) {
            return response;
        }
        lock.lock();
        try {
            while (!isDone()) {
                done.await();
            }
            return response;
        } finally {
            lock.unlock();
        }
    }

    public void receive(RpcResponse response) {
        lock.lock();
        try {
            this.response = response;
            done.signal();
        } finally {
            lock.unlock();
        }
    }

    public boolean isDone() {
        return response != null;
    }

    public static RpcFuture getFuture(long rpcId) {
        return FUTURES.get(rpcId);
    }
}
