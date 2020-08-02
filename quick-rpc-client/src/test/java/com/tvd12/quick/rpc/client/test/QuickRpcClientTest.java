package com.tvd12.quick.rpc.client.test;

import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.quick.rpc.client.QuickRpcClient;
import com.tvd12.quick.rpc.client.request.RpcRequest;

public class QuickRpcClientTest {

	public static void main(String[] args) throws Exception {
		QuickRpcClient client = QuickRpcClient.builder()
				.build();
		System.out.println("connect success");
		client.fire(new RpcRequest("test", "1", EzyEntityFactory.newObject()));
//		client.close();
	}
	
}
