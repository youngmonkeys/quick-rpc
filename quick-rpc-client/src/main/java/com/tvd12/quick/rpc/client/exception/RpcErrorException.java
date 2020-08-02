package com.tvd12.quick.rpc.client.exception;

import lombok.Getter;

public class RpcErrorException extends RuntimeException {
	private static final long serialVersionUID = 3710094643516024486L;
	
	@Getter
	protected final Object data;
	
	public RpcErrorException(String cmd, String requestId, Object data) {
		super("rpc command: " + cmd + ", requestId: " + requestId + " error");
		this.data = data;
	}

}
