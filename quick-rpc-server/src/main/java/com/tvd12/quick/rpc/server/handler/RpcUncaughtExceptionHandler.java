package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

public interface RpcUncaughtExceptionHandler {

    @SuppressWarnings("rawtypes")
    void handleException(
        RpcRequest request,
        RpcResponse response,
        Exception exception
    ) throws Exception;
}
