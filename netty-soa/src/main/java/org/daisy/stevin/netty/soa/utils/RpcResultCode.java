package org.daisy.stevin.netty.soa.utils;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public enum RpcResultCode {
    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),
    /**
     * 系统异常
     */
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常"),
    /**
     * 未找到invoker
     */
    INVOKER_NOT_FOUND("INVOKER_NOT_FOUND", "未找到invoker"),
    /**
     * rpc返回码为空
     */
    RPC_RESULT_CODE_IS_NULL("RPC_RESULT_CODE_IS_NULL", "rpc返回码为空"),;
    /**
     * 枚举码
     */
    private String code;
    /**
     * 枚举描述
     */
    private String msg;

    private RpcResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static RpcResultCode getTypeByCode(String code) {
        for (RpcResultCode type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return RPC_RESULT_CODE_IS_NULL;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RpcResultCode{");
        sb.append("code='").append(code).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
