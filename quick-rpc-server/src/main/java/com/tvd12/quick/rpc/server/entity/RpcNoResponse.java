package com.tvd12.quick.rpc.server.entity;

public final class RpcNoResponse {

    public static final RpcNoResponse INSTANCE = new RpcNoResponse();

    private RpcNoResponse() {}

    public static RpcNoResponse getInstance() {
        return INSTANCE;
    }
}
