package com.tvd12.quick.rpc.server.handler;

import java.util.ArrayList;
import java.util.List;

import com.tvd12.ezyfox.builder.EzyBuilder;

import lombok.Getter;

public class RpcRequestInterceptors {

	@Getter
	protected final List<RpcRequestInterceptor> interceptors;
	
	protected RpcRequestInterceptors(Builder builder) {
		this.interceptors = new ArrayList<>(builder.interceptors);
	}
	
	@Override
	public String toString() {
		return "RpcRequestInterceptors(" + interceptors + ")";
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcRequestInterceptors> {
		
		protected List<RpcRequestInterceptor> interceptors;
		
		public Builder() {
			this.interceptors = new ArrayList<>();
		}
		
		public Builder addInteceptor(RpcRequestInterceptor interceptor) {
			this.interceptors.add(interceptor);
			return this;
		}
		
		public Builder addInteceptors(List<RpcRequestInterceptor> interceptors) {
			for(RpcRequestInterceptor interceptor : interceptors)
				addInteceptor(interceptor);
			return this;
		}
		
		@Override
		public RpcRequestInterceptors build() {
			return new RpcRequestInterceptors(this);
		}
		
	}
	
}
