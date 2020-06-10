package com.tvd12.quick.rpc;

public interface RpcClient {

	void fire(String requestType, Object data);
	
	<T> T call(String requestType, Object data, Class<T> returnType);
	
	void execute(String requestType, Object data, RpcCallback callback);
	
}
