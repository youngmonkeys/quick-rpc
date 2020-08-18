package com.tvd12.quick.rpc.server.entity;

public class RpcResponse {

	@SuppressWarnings("rawtypes")
	protected final RpcRequest request;
	
	public final static RpcNoResponse NO_RESPONSE = RpcNoResponse.getInstance();
	
	@SuppressWarnings("rawtypes")
	public RpcResponse(RpcRequest request) {
		this.request = request;
	}
	
	public void write(Object data) {
		RpcSession session = request.getSession();
		session.send(request.getCommand(), request.getId(), data);
	}
	
	public RpcSession getSession() {
		return request.getSession();
	}
	
}
