package com.tvd12.quick.rpc.server.test;

import com.tvd12.quick.rpc.server.QuickRpcServer;
import com.tvd12.quick.rpc.server.asm.RpcRequestHandlerImplementer;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;

public class QuickRpcServerTest {

    public static void main(String[] args) throws Exception {
        RpcRequestHandlerImplementer.setDebug(true);
        QuickRpcSettings settings = QuickRpcSettings.builder()
            .username("admin")
            .password("admin")
            .build();
        QuickRpcServer server = new QuickRpcServer(settings)
            .scan("com.tvd12.quick.rpc.server.test.data")
            .scan("com.tvd12.quick.rpc.server.test.handler")
            .scan("com.tvd12.quick.rpc.server.test.controller");
        server.start();
        System.out.println(server);
        Thread.sleep(1000L);
//		server.stop();
    }
}
