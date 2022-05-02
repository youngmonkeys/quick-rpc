package com.tvd12.quick.rpc.server.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.server.annotation.RpcRequestHandled;

public final class RpcRequestHandledAnnotations {

    private RpcRequestHandledAnnotations() {}

    public static String getCommand(RpcRequestHandled annotation) {
        if (EzyStrings.isNoContent(annotation.value())) {
            return annotation.command();
        }
        return annotation.value();
    }

    public static String getCommand(Class<?> rpcHandlerClass) {
        return getCommand(rpcHandlerClass.getAnnotation(RpcRequestHandled.class));
    }
}
