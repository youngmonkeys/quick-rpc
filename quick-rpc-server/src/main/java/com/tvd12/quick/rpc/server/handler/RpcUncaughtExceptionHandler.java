package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

public interface RpcUncaughtExceptionHandler<D> {

	void handleException(
			RpcRequest<D> request, RpcResponse response,
			Exception exception) throws Exception;
	
	void setExceptionHandler(Object exceptionHandler);

}
