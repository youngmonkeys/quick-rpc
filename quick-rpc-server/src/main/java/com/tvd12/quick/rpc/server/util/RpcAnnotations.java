package com.tvd12.quick.rpc.server.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.server.annotation.Rpc;

public final class RpcAnnotations {

    private RpcAnnotations() {}

    public static String getCommand(Rpc annotation) {
        if (EzyStrings.isNoContent(annotation.value())) {
            return annotation.command();
        }
        return annotation.value();
    }
}
