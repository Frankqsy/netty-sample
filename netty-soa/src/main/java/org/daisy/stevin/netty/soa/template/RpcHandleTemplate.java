package org.daisy.stevin.netty.soa.template;

import org.daisy.stevin.netty.soa.RpcResult;
import org.daisy.stevin.netty.soa.utils.RpcResultCode;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcHandleTemplate {
    /**
     * 模板处理流程
     * <p>
     * <br>
     * 1.参数校验及补全 <br>
     * 2.执行具体业务操作 <br>
     * 3.异常处理
     *
     * @param rpcResult      返回结果
     * @param handleCallBack 操作返回接口
     */
    public static <T> void execute(RpcResult<T> rpcResult, RpcHandleCallback handleCallBack) {
        try {

            // 参数校验及补全
            handleCallBack.checkAndFillParams();

            // ִ执行具体业务操作
            handleCallBack.process();

        } catch (Throwable e) {
            RpcResultFactory.constructRpcResult(rpcResult, RpcResultCode.SYSTEM_ERROR, null);
        }

    }
}
