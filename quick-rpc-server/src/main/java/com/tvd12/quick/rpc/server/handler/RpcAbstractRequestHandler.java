package com.tvd12.quick.rpc.server.handler;

import com.tvd12.ezyfox.reflect.EzyGenerics;
import com.tvd12.ezyfox.util.EzyLoggable;

public abstract class RpcAbstractRequestHandler<R>
    extends EzyLoggable
    implements RpcRequestHandler<R> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<R> getDataType() {
        try {
            return EzyGenerics.getOneGenericClassArgument(getClass().getGenericSuperclass());
        } catch (Exception e) {
            return null;
        }
    }
}
