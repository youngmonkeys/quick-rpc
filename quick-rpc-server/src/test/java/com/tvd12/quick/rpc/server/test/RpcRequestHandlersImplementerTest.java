package com.tvd12.quick.rpc.server.test;

import com.tvd12.quick.rpc.server.asm.RpcRequestHandlerImplementer;
import com.tvd12.quick.rpc.server.asm.RpcRequestHandlersImplementer;
import com.tvd12.quick.rpc.server.test.controller.HelloController;

public class RpcRequestHandlersImplementerTest {

    public static void main(String[] args) {
        RpcRequestHandlerImplementer.setDebug(true);
        RpcRequestHandlersImplementer implementer = new RpcRequestHandlersImplementer();
        implementer.implement(new HelloController());
    }
}
