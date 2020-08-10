package com.tvd12.quick.rpc.server.test;

import com.tvd12.quick.rpc.server.QuickRpcServer;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;
import com.tvd12.quick.rpc.server.test.data.GreetRequest;
import com.tvd12.quick.rpc.server.test.data.GreetResponse;

public class QuickRpcServerTest {

	public static void main(String[] args) throws Exception {
		QuickRpcSettings settings = QuickRpcSettings.builder()
				.username("admin")
				.password("admin")
				.build();
		QuickRpcServer server = new QuickRpcServer(settings)
				.scan("com.tvd12.quick.rpc.server.test.data")
				.addRequestHandler("Greet", new RpcRequestHandler<GreetRequest>() {
					@Override
					public void handle(RpcRequest<GreetRequest> request, RpcResponse response) {
						response.write(new GreetResponse("Greet " + request.getData().getWho() + "!"));
					}
				});
		server.start();
	}
	
}
