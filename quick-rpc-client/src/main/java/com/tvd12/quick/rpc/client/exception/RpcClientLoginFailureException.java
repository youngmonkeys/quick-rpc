package com.tvd12.quick.rpc.client.exception;

public class RpcClientLoginFailureException extends RuntimeException {
    private static final long serialVersionUID = 4019339876991821477L;

    public RpcClientLoginFailureException(String msg) {
        super(msg);
    }
}
