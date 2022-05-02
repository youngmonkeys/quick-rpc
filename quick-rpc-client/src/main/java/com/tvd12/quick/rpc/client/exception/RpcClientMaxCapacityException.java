package com.tvd12.quick.rpc.client.exception;

public class RpcClientMaxCapacityException extends RuntimeException {
    private static final long serialVersionUID = 1627176401522995776L;

    public RpcClientMaxCapacityException(int capacity) {
        super("max capacity: " + capacity);
    }
}
