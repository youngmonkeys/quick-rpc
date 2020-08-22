package com.tvd12.quick.rpc.server.exception;

import lombok.Getter;

@Getter
public class RpcHandleErrorException extends RuntimeException {
	private static final long serialVersionUID = 9093897698483385745L;
	
	protected final Object responseData;
	
	public RpcHandleErrorException(Object responseData) {
		this("rpc handle error", responseData);
	}
	
	public RpcHandleErrorException(String msg, Object responseData) {
		super(msg);
		this.responseData = responseData;
	}
	
}
