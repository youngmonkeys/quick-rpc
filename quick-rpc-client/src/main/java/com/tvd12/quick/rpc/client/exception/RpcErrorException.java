package com.tvd12.quick.rpc.client.exception;

import com.tvd12.quick.rpc.client.entity.RpcError;

import lombok.Getter;

public class RpcErrorException extends RuntimeException {
	private static final long serialVersionUID = 3710094643516024486L;
	
	@Getter
	protected final RpcError error;
	
	public RpcErrorException(
			String cmd, 
			String requestId, 
			RpcError error
	) {
		super("rpc command: " + cmd + ", requestId: " + requestId + " error");
		this.error = error;
	}
	
	public <T> T getErrorData(Class<T> dataType) {
		return error.getData(dataType);
	}

}
