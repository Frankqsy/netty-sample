package org.daisy.stevin.netty.soa.consumer;

import org.daisy.stevin.netty.soa.Invocation;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -8006540257899998254L;
    private static final AtomicLong GEN_ID = new AtomicLong(0);

    private final long rpcId;
    private Invocation invocation;

    public RpcRequest() {
        rpcId = newRpcId();
    }

    public static long newRpcId() {
        return GEN_ID.incrementAndGet();
    }

    public long getRpcId() {
        return rpcId;
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RpcRequest{");
        sb.append("rpcId=").append(rpcId);
        sb.append(", invocation=").append(invocation);
        sb.append('}');
        return sb.toString();
    }
}
