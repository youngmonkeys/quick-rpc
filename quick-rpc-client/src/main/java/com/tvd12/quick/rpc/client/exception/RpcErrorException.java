package com.tvd12.quick.rpc.client.exception;

public class RpcErrorException extends RuntimeException {
	private static final long serialVersionUID = 3710094643516024486L;
	
	protected final Object data;
	
	public RpcErrorException(String cmd, String requestId, Object data) {
		super("rpc command: " + cmd + ", requestId: " + requestId + " error");
		this.data = data;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T)data;
	}

}
