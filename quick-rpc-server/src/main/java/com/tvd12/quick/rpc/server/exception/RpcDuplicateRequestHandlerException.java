package com.tvd12.quick.rpc.server.exception;

import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;

public class RpcDuplicateRequestHandlerException extends IllegalStateException {
    private static final long serialVersionUID = 2586181034307827101L;

    @SuppressWarnings("rawtypes")
    public RpcDuplicateRequestHandlerException(
        String command,
        RpcRequestHandler old, RpcRequestHandler now) {
        super("duplicate handler for: " + command + " <> " + old + " => " + now);
    }
}
