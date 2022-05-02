package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.controller.EzyAbstractAppEventController;
import com.tvd12.ezyfoxserver.event.EzySessionRemovedEvent;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.event.RpcSessionRemoveEvent;
import com.tvd12.quick.rpc.server.handler.RpcEventHandlers;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

public class RpcSessionRemoveController
    extends EzyAbstractAppEventController<EzySessionRemovedEvent> {

    protected final RpcSessionManager sessionManager;
    protected final RpcComponentManager componentManager;
    protected final RpcEventHandlers eventHandlers;

    public RpcSessionRemoveController(RpcComponentManager componentManager) {
        this.componentManager = componentManager;
        this.sessionManager = componentManager.getComponent(RpcSessionManager.class);
        this.eventHandlers = componentManager.getComponent(RpcEventHandlers.class);
    }

    @Override
    public void handle(EzyAppContext ctx, EzySessionRemovedEvent event) {
        RpcSession session = sessionManager.removeSession(event.getSession());
        if (session != null) {
            eventHandlers.handle(new RpcSessionRemoveEvent(session));
        }
        logger.debug(
            "session: {} has removed, remain {} rpc session",
            event.getSession(),
            sessionManager.getSessionCount()
        );
    }
}
