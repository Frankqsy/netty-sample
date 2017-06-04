package org.daisy.stevin.netty.soa;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class Invocation implements Serializable {

    private static final long serialVersionUID = 304481790183457227L;

    private Class<?> clazz;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Invocation{");
        sb.append("clazz=").append(clazz);
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", parameterTypes=").append(parameterTypes == null ? "null" : Arrays.asList(parameterTypes).toString());
        sb.append(", arguments=").append(arguments == null ? "null" : Arrays.asList(arguments).toString());
        sb.append('}');
        return sb.toString();
    }
}
