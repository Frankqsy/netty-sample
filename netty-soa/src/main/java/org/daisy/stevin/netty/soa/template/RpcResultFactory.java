package org.daisy.stevin.netty.soa.template;

import org.daisy.stevin.netty.soa.RpcResult;
import org.daisy.stevin.netty.soa.utils.RpcResultCode;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcResultFactory {

    public static <T> void constructRpcResult(RpcResult<T> rpcResult, RpcResultCode rpcResultCode, T result) {
        // 返回结果为空时需要new出来，理论上不存在这种情况
        if (rpcResult == null) {
            return;
        }
        // 内部返回结果为空时需要设置详细描述信息
        if (rpcResultCode == null) {
            rpcResult.setResultCode(RpcResultCode.RPC_RESULT_CODE_IS_NULL.getCode());
            rpcResult.setResultMsg(RpcResultCode.RPC_RESULT_CODE_IS_NULL.getMsg());
            rpcResult.setSuccess(false);
            rpcResult.setResult(null);
            return;
        }
        // 补全相关参数
        rpcResult.setResult(result);
        rpcResult.setResultCode(rpcResultCode.getCode());
        rpcResult.setResultMsg(rpcResultCode.getMsg());
        rpcResult.setSuccess(rpcResultCode == RpcResultCode.SUCCESS);
    }

    public static <T> RpcResult<T> constructRpcResult(RpcResultCode rpcResultCode, T result) {
        RpcResult rpcResult = new RpcResult<T>();
        constructRpcResult(rpcResult, rpcResultCode, result);
        return rpcResult;
    }
}
