package com.tvd12.quick.rpc.client.exception;

public class RpcClientNotConnectedException extends RuntimeException {
	private static final long serialVersionUID = 3710094643516024486L;
	
	public RpcClientNotConnectedException() {
		super("has no connected socket clients");
	}
	
}
