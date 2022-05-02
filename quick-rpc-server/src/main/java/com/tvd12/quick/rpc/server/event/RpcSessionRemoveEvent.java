package com.tvd12.quick.rpc.server.event;

import com.tvd12.quick.rpc.server.entity.RpcSession;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RpcSessionRemoveEvent implements RpcEvent {

    private final RpcSession session;

    @Override
    public RpcEventType getEventType() {
        return RpcEventType.SESSION_REMOVED;
    }
}
