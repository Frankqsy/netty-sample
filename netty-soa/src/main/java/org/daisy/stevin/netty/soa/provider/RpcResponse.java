package org.daisy.stevin.netty.soa.provider;

import org.daisy.stevin.netty.soa.RpcResult;

import java.io.Serializable;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -7540885521795871327L;
    private long rpcId;
    private RpcResult result;

    public long getRpcId() {
        return rpcId;
    }

    public void setRpcId(long rpcId) {
        this.rpcId = rpcId;
    }

    public RpcResult getResult() {
        return result;
    }

    public void setResult(RpcResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RpcResponse{");
        sb.append("rpcId=").append(rpcId);
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }
}
