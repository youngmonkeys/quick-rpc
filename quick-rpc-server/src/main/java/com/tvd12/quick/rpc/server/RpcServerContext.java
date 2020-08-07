package com.tvd12.quick.rpc.server;

import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

import lombok.Getter;

public class RpcServerContext {

	@Getter
	protected final RpcSessionManager sessionManager;
	protected final RpcComponentManager componentManager;
	
	protected RpcServerContext() {
		this.componentManager = RpcComponentManager.getInstance();
		this.sessionManager = componentManager.getComponent(RpcSessionManager.class);
	}
	
}
