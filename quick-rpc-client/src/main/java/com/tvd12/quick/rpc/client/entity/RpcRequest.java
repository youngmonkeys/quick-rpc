package com.tvd12.quick.rpc.client.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import lombok.Getter;

@Getter
public class RpcRequest {

    public static final RpcRequest POISON = new RpcRequest(null, null, null);
    protected final String id;
    protected final Object data;
    protected final String command;

    public RpcRequest(Object data) {
        this(null, data);
    }

    public RpcRequest(String command, Object data) {
        this(command, null, data);
    }

    public RpcRequest(String command, String id, Object data) {
        this.id = id;
        this.data = data;
        this.command = command;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Class<?> getDataType() {
        return data.getClass();
    }

    @Override
    public String toString() {
        return "(" +
            "command: " + command + ", " +
            "id: " + id + ", " +
            "data: " + data +
            ")";
    }

    public static class Builder implements EzyBuilder<RpcRequest> {

        protected String id;
        protected Object data;
        protected String command;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        @Override
        public RpcRequest build() {
            return new RpcRequest(command, id, data);
        }
    }
}
