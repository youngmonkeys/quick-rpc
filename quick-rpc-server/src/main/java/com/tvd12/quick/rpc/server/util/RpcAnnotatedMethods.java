package com.tvd12.quick.rpc.server.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.Rpc;

public final class RpcAnnotatedMethods {

    private RpcAnnotatedMethods() {}

    public static String getCommand(EzyMethod method) {
        String command = RpcAnnotations
            .getCommand(method.getAnnotation(Rpc.class));
        if (EzyStrings.isNoContent(command)) {
            return method.getName();
        }
        return command;
    }
}
