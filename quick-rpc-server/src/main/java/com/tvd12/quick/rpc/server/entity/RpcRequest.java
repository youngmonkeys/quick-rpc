package com.tvd12.quick.rpc.server.entity;

import lombok.Getter;

@Getter
public class RpcRequest<D> {

    protected final String command;
    protected final String id;
    protected final D data;
    protected final RpcSession session;

    public RpcRequest(RpcSession session, String cmd, String id, D data) {
        this.session = session;
        this.command = cmd;
        this.id = id;
        this.data = data;
    }
}
