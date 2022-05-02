package com.tvd12.quick.rpc.server.handler;

import com.tvd12.ezyfox.builder.EzyBuilder;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class RpcRequestHandlers {

    protected final Map<String, RpcRequestHandler> handlers;

    protected RpcRequestHandlers(Builder builder) {
        this.handlers = new HashMap<>(builder.handlers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public RpcRequestHandler getHandler(String command) {
        return handlers.get(command);
    }

    @Override
    public String toString() {
        return "RpcRequestHandlers(" + handlers + ")";
    }

    public static class Builder implements EzyBuilder<RpcRequestHandlers> {

        protected Map<String, RpcRequestHandler> handlers;

        public Builder() {
            this.handlers = new HashMap<>();
        }

        public Builder addHandler(String cmd, RpcRequestHandler handler) {
            handlers.put(cmd, new RpcRequestHandlerProxy(handler));
            return this;
        }

        public Builder addHandlers(Map<String, RpcRequestHandler> handlers) {
            for (String cmd : handlers.keySet()) {
                RpcRequestHandler handler = handlers.get(cmd);
                addHandler(cmd, handler);
            }
            return this;
        }

        @Override
        public RpcRequestHandlers build() {
            return new RpcRequestHandlers(this);
        }
    }
}
