package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.ext.EzyAppEntry;
import com.tvd12.ezyfoxserver.ext.EzyAppEntryLoader;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;

public class RpcAppEntryLoader implements EzyAppEntryLoader {

    protected final RpcComponentManager componentManager;

    public RpcAppEntryLoader(RpcComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Override
    public EzyAppEntry load() {
        return new RpcAppEntry(componentManager);
    }
}
