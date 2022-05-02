package com.tvd12.quick.rpc.server.test.handler;

import com.tvd12.quick.rpc.server.annotation.RpcEventHandled;
import com.tvd12.quick.rpc.server.event.RpcEventType;
import com.tvd12.quick.rpc.server.event.RpcSessionRemoveEvent;
import com.tvd12.quick.rpc.server.handler.RpcEventHandler;

@RpcEventHandled(RpcEventType.SESSION_REMOVED)
public class GreetSessionRemoveHandler implements RpcEventHandler<RpcSessionRemoveEvent> {

    @Override
    public void handle(RpcSessionRemoveEvent event) {}
}
