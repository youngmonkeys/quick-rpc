package com.tvd12.quick.rpc.server.entity;

public class RpcResponse {

    @SuppressWarnings("rawtypes")
    protected final RpcRequest request;

    public static final RpcNoResponse NO_RESPONSE
        = RpcNoResponse.getInstance();

    @SuppressWarnings("rawtypes")
    public RpcResponse(RpcRequest request) {
        this.request = request;
    }

    public void write(Object data) {
        RpcSession session = request.getSession();
        session.send(request.getCommand(), request.getId(), data);
    }

    public void writeError(Object data) {
        RpcSession session = request.getSession();
        session.sendError(request.getCommand(), request.getId(), data);
    }

    public RpcSession getSession() {
        return request.getSession();
    }
}
