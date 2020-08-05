package com.tvd12.quick.rpc.server.entity;

import com.tvd12.ezyfoxserver.entity.EzySession;

public class RpcSession {

	protected final EzySession transporter;
	
	public RpcSession(EzySession transporter) {
		this.transporter = transporter;
	}
	
	public Object getKey() {
		return transporter;
	}
	
}
