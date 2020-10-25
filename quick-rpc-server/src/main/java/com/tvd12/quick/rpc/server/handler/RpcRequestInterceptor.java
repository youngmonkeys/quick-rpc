package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

@SuppressWarnings("rawtypes")
public interface RpcRequestInterceptor {
	
	default void preHandle(RpcRequest request, RpcResponse response) {}
	
	default void postHandle(RpcRequest request, RpcResponse response) {}
	
	default void postHandle(RpcRequest request, RpcResponse response, Exception e) {}
	
}
