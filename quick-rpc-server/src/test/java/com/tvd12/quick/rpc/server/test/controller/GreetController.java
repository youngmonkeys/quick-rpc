package com.tvd12.quick.rpc.server.test.controller;

import com.tvd12.quick.rpc.server.annotation.Rpc;
import com.tvd12.quick.rpc.server.annotation.RpcController;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.test.data.GreetRequest;
import com.tvd12.quick.rpc.server.test.data.GreetResponse;

@RpcController("Say")
public class GreetController {

    @Rpc("Greet")
    public GreetResponse greet(
        GreetRequest request,
        RpcRequest<?> r,
        RpcResponse response,
        RpcSession session
    ) {
        return new GreetResponse("Hello " + request.getWho() + "!");
    }
}
