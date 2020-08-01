package com.tvd12.quick.rpc.client.test;

import com.tvd12.quick.rpc.client.QuickRpcClient;

public class QuickRpcClientTest {

	public static void main(String[] args) throws Exception {
		QuickRpcClient client = QuickRpcClient.builder()
				.build();
		Thread.sleep(1000);
		client.close();
	}
	
}
