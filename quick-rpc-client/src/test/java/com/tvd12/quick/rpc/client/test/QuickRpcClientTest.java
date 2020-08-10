package com.tvd12.quick.rpc.client.test;

import com.tvd12.quick.rpc.client.QuickRpcClient;
import com.tvd12.quick.rpc.client.request.RpcRequest;
import com.tvd12.quick.rpc.client.test.data.GreetRequest;

public class QuickRpcClientTest {

	public static void main(String[] args) throws Exception {
		QuickRpcClient client = QuickRpcClient.builder()
				.scan("com.tvd12.quick.rpc.client.test.data")
				.build();
		System.out.println("connect success");
		RpcRequest request = RpcRequest.builder()
				.command("Greet")
				.id("1")
				.data(new GreetRequest("Dzung"))
				.build();
		System.out.println("response: " + client.call(request));
//		client.close();
	}
	
}
