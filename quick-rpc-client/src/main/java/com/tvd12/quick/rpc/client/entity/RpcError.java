package com.tvd12.quick.rpc.client.entity;

public interface RpcError {
	
	String getCommand();
	
	String getRequestId();
	
	Object getRawData();

	<T> T getErrorData(Class<T> dataType);
	
}
