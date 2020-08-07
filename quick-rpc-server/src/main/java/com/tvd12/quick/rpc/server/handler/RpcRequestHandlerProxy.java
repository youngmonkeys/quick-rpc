package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

import lombok.Getter;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RpcRequestHandlerProxy implements RpcRequestHandler {

	@Getter
	protected final Class dataType;
	protected final RpcRequestHandler handler;
	
	public RpcRequestHandlerProxy(RpcRequestHandler handler) {
		this.handler = handler;
		this.dataType = handler.getDataType();
	}
	
	@Override
	public void handle(RpcRequest request, RpcResponse response) {
		handler.handle(request, response);
	}
	
}
