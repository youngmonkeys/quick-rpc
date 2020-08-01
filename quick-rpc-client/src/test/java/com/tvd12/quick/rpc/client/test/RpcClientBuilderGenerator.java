package com.tvd12.quick.rpc.client.test;

import com.tvd12.ezyfox.tool.EzyBuilderCreator;
import com.tvd12.quick.rpc.client.QuickRpcClient;

public class RpcClientBuilderGenerator {

	public static void main(String[] args) throws Exception {
		System.out.println(new EzyBuilderCreator()
                .create(QuickRpcClient.class));
	}
	
}
