package com.tvd12.quick.rpc.client.handler;

import com.tvd12.quick.rpc.client.entity.RpcResponse;

public interface RpcResponseHandler {

    void handle(RpcResponse response);
}
