package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.command.EzyAppSetup;
import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.ext.EzyAppEntry;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;

public class RpcAppEntry implements EzyAppEntry {

    protected final RpcComponentManager componentManager;

    public RpcAppEntry(RpcComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Override
    public void config(EzyAppContext ctx) {
        ctx.get(EzyAppSetup.class)
            .setRequestController(new RpcAppRequestController(componentManager))
            .addEventController(
                EzyEventType.SESSION_REMOVED,
                new RpcSessionRemoveController(componentManager)
            );
    }
}
