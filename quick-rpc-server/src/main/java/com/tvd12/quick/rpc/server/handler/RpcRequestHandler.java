package com.tvd12.quick.rpc.server.handler;

import com.tvd12.ezyfox.reflect.EzyGenerics;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;

public interface RpcRequestHandler<D> {

    void handle(RpcRequest<D> request, RpcResponse response);

    @SuppressWarnings("unchecked")
    default Class<D> getDataType() {
        try {
            return EzyGenerics.getGenericInterfacesArguments(
                getClass(),
                RpcRequestHandler.class, 1)[0];
        } catch (Exception e) {
            return null;
        }
    }
}
