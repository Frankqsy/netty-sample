package org.daisy.stevin.netty.soa;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class RpcResult<T> implements Serializable {

    private static final long serialVersionUID = 618283714965502653L;
    /**
     * 结果码
     */
    private String resultCode;

    /**
     * 结果描述
     */
    private String resultMsg;

    /**
     * 返回结果
     */
    private T result;

    /**
     * 扩展信息
     */
    private Map<String, Object> extendInfo;

    /**
     * 调用是否成功
     */
    private boolean isSuccess = false;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Map<String, Object> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RpcResult{");
        sb.append("resultCode='").append(resultCode).append('\'');
        sb.append(", resultMsg='").append(resultMsg).append('\'');
        sb.append(", result=").append(result);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append(", isSuccess=").append(isSuccess);
        sb.append('}');
        return sb.toString();
    }
}
