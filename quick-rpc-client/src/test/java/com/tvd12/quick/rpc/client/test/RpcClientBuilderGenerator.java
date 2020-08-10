package com.tvd12.quick.rpc.client.test;

import com.tvd12.ezyfox.tool.EzyBuilderCreator;
import com.tvd12.quick.rpc.client.request.RpcRequest;

public class RpcClientBuilderGenerator {

	public static void main(String[] args) throws Exception {
		System.out.println(new EzyBuilderCreator()
                .create(RpcRequest.class));
	}
	
}
