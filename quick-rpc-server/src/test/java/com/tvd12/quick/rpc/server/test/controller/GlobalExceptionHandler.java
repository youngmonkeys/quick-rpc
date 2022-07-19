package com.tvd12.quick.rpc.server.test.controller;

import com.tvd12.quick.rpc.server.annotation.RpcExceptionHandler;
import com.tvd12.quick.rpc.server.annotation.RpcTryCatch;

@RpcExceptionHandler
public class GlobalExceptionHandler {

    @RpcTryCatch(RuntimeException.class)
    public void handle(RuntimeException e) {
        e.printStackTrace();
    }

    @RpcTryCatch(IllegalArgumentException.class)
    public String handle(IllegalArgumentException e) {
        return e.getMessage();
    }
}
