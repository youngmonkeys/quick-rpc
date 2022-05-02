package com.tvd12.quick.rpc.client.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.client.annotation.RpcResponseHandled;

public final class RpcResponseHandledAnnotations {

    private RpcResponseHandledAnnotations() {}

    public static String getCommand(RpcResponseHandled annotation) {
        if (EzyStrings.isNoContent(annotation.value())) {
            return annotation.command();
        }
        return annotation.value();
    }

    public static String getCommand(Class<?> rpcHandlerClass) {
        return getCommand(rpcHandlerClass.getAnnotation(RpcResponseHandled.class));
    }
}
