package com.tvd12.quick.rpc.server.reflect;

import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.RpcRequestData;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import lombok.Getter;

import java.lang.reflect.Parameter;

public abstract class RpcHandlerMethod {

    @Getter
    protected final EzyMethod method;

    public RpcHandlerMethod(EzyMethod method) {
        this.method = method;
    }

    public String getName() {
        return method.getName();
    }

    public Parameter[] getParameters() {
        return method.getMethod().getParameters();
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Class<?> getRequestDataType() {
        Class<?> dataType = null;
        for (Parameter parameter : getParameters()) {
            Class<?> type = parameter.getType();
            if (type != RpcRequest.class
                && type != RpcResponse.class
                && type != RpcSession.class
                && !Throwable.class.isAssignableFrom(type)
            ) {
                dataType = type;
                if (parameter.isAnnotationPresent(RpcRequestData.class)) {
                    break;
                }
            }
        }
        return dataType;
    }
}
