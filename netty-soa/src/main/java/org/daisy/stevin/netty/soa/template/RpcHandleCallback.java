package org.daisy.stevin.netty.soa.template;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public interface RpcHandleCallback {
    /**
     * 检查及补全参数
     */
    public void checkAndFillParams();

    /**
     * 执行待处理操作，比如模型的创建，修改，删除等
     */
    public void process()throws Exception;
}
