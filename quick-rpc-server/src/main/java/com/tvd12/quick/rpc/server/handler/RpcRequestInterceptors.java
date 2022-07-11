package com.tvd12.quick.rpc.server.handler;

import com.tvd12.ezyfox.builder.EzyBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class RpcRequestInterceptors {

    @Getter
    protected final List<RpcRequestInterceptor> interceptors;

    protected RpcRequestInterceptors(Builder builder) {
        this.interceptors = new ArrayList<>(builder.interceptors);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "RpcRequestInterceptors(" + interceptors + ")";
    }

    public static class Builder implements EzyBuilder<RpcRequestInterceptors> {

        protected List<RpcRequestInterceptor> interceptors;

        public Builder() {
            this.interceptors = new ArrayList<>();
        }

        public Builder addInterceptor(RpcRequestInterceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder addInterceptors(List<RpcRequestInterceptor> interceptors) {
            for (RpcRequestInterceptor interceptor : interceptors) {
                addInterceptor(interceptor);
            }
            return this;
        }

        @Override
        public RpcRequestInterceptors build() {
            return new RpcRequestInterceptors(this);
        }
    }
}
