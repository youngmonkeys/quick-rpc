package com.tvd12.quick.rpc.server.asm;

import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

import lombok.Getter;
import lombok.Setter;

public abstract class RpcAsmAbstractRequestHandler implements RpcAsmRequestHandler {

	@Getter
	@Setter
	protected String command;
	
	protected static final Object NO_RESPONSE = new Object();
	
	@Override
	public void handle(RpcRequest<Object> request, RpcResponse response) {
		try {
			Object responseData = handleRequest(request, response);
			if(responseData != NO_RESPONSE)
				response.write(responseData);
		}
		catch (Exception e) {
			handleException(request, response, e);
		}
	}

	protected abstract Object handleRequest(
			RpcRequest<Object> request, RpcResponse response);
	
	protected abstract void handleException(
			RpcRequest<Object> request, RpcResponse response, Exception exception);
	
}
