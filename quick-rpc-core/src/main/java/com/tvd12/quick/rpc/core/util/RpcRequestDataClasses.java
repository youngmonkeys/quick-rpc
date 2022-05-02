package com.tvd12.quick.rpc.core.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.ezyfox.reflect.EzyClasses;
import com.tvd12.quick.rpc.core.annotation.RpcRequest;

public final class RpcRequestDataClasses {

    private static final String REQUEST_SUFFIX = "Request";

    private RpcRequestDataClasses() {}

    public static String getCommand(Class<?> requestClass) {
        String command = null;
        RpcRequest ann = requestClass.getAnnotation(RpcRequest.class);
        if (ann != null) {
            command = RpcRequestAnnotations.getCommand(ann);
        }
        if (EzyStrings.isNoContent(command)) {
            command = EzyClasses.getVariableName(requestClass, REQUEST_SUFFIX);
        }
        return command;
    }
}
