package com.tvd12.quick.rpc.server.asm;

import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

import lombok.Getter;
import lombok.Setter;

public abstract class RpcAsmAbstractRequestHandler implements RpcAsmRequestHandler {

	@Getter
	@Setter
	protected String command;
	
	@Override
	public void handle(RpcRequest<Object> request, RpcResponse response) {
		Object responseData = handleRequest(request, response);
		if(responseData != null)
			response.write(responseData);
	}
	
	protected abstract Object handleRequest(RpcRequest<Object> request, RpcResponse response);
	
}
