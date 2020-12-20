package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.controller.EzyAbstractAppEventController;
import com.tvd12.ezyfoxserver.event.EzySessionRemovedEvent;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

public class RpcSessionRemoveController 
		extends EzyAbstractAppEventController<EzySessionRemovedEvent> {

	protected final RpcSessionManager sessionManager;
	protected final RpcComponentManager componentManager;
	
	public RpcSessionRemoveController(RpcComponentManager componentManager) {
		this.componentManager = componentManager;
		this.sessionManager = componentManager.getComponent(RpcSessionManager.class);
	}
	
	@Override
	public void handle(EzyAppContext ctx, EzySessionRemovedEvent event) {
		sessionManager.removeSession(event.getSession());
		logger.debug("session: {} has removed, remain {} rpc session", event.getSession(), sessionManager.getSessionCount());
	}
	
}
