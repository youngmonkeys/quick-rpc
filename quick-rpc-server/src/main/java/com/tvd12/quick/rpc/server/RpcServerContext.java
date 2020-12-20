package com.tvd12.quick.rpc.server;

import com.tvd12.ezyfox.bean.EzyBeanContext;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

import lombok.Getter;

public class RpcServerContext {

	@Getter
	protected final EzyBeanContext beanContext;
	@Getter
	protected final RpcSessionManager sessionManager;
	protected final RpcComponentManager componentManager;
	
	protected RpcServerContext(RpcComponentManager componentManager) {
		this.componentManager = componentManager;
		this.beanContext = componentManager.getComponent(EzyBeanContext.class);
		this.sessionManager = componentManager.getComponent(RpcSessionManager.class);
	}
	
}
