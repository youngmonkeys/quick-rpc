package com.tvd12.quick.rpc.client;

public interface RpcClient {

	void fire(RpcRequest request);
	
	void execute(RpcRequest request, RpcCallback callback);
	
	<T> T call(RpcRequest request, Class<T> returnType) throws Exception;
	
}
