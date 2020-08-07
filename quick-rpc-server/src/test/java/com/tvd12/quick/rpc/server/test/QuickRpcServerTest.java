package com.tvd12.quick.rpc.server.test;

import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.quick.rpc.server.QuickRpcServer;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;

public class QuickRpcServerTest {

	public static void main(String[] args) throws Exception {
		QuickRpcSettings settings = QuickRpcSettings.builder()
				.username("admin")
				.password("admin")
				.build();
		QuickRpcServer server = new QuickRpcServer(settings)
				.addRequestHandler("test", new RpcRequestHandler<EzyData>() {
					@Override
					public void handle(RpcRequest<EzyData> request, RpcResponse response) {
						response.write(EzyEntityFactory.newObject());
					}
				});
		server.start();
	}
	
}
