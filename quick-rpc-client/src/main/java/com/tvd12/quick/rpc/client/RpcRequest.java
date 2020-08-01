package com.tvd12.quick.rpc.client;

public interface RpcRequest {

	String getRequestType();
	
	<T> T getData();
	
}
