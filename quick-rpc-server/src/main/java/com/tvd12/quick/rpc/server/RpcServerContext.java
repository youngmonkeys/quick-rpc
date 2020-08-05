package com.tvd12.quick.rpc.server;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandlers;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

import lombok.Getter;

public class RpcServerContext {

	@Getter
	protected final RpcSessionManager sessionManager;
	@Getter
	protected final RpcRequestHandlers requestHandlers;
	
	protected RpcServerContext(Builder builder) {
		this.sessionManager = builder.sessionManager;
		this.requestHandlers = builder.requestHandlers;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcServerContext> {
		
		protected RpcSessionManager sessionManager;
		protected RpcRequestHandlers requestHandlers;
		
		public Builder sessionManager(RpcSessionManager sessionManager) {
			this.sessionManager = sessionManager;
			return this;
		}
		
		public Builder requestHandlers(RpcRequestHandlers requestHandlers) {
			this.requestHandlers = requestHandlers;
			return this;
		}
		
		@Override
		public RpcServerContext build() {
			return new RpcServerContext(this);
		}
		
	}
	
}
