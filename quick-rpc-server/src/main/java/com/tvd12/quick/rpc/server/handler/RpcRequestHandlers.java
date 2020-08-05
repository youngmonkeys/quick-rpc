package com.tvd12.quick.rpc.server.handler;

import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.builder.EzyBuilder;

@SuppressWarnings("rawtypes")
public class RpcRequestHandlers {

	protected final Map<String, RpcRequestHandler> handlers;
	
	protected RpcRequestHandlers(Builder builder) {
		this.handlers = new HashMap<>(builder.handlers);
	}
	
	public RpcRequestHandler getHandler(String command) {
		return handlers.get(command);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcRequestHandlers> {
		
		protected Map<String, RpcRequestHandler> handlers;
		
		public Builder() {
			this.handlers = new HashMap<>();
		}
		
		public Builder addHandler(String cmd, RpcRequestHandler handler) {
			handlers.put(cmd, handler);
			return this;
		}
		
		public Builder addHandlers(Map<String, RpcRequestHandler> handlers) {
			handlers.putAll(handlers);
			return this;
		}
		
		@Override
		public RpcRequestHandlers build() {
			return new RpcRequestHandlers(this);
		}
		
	}
	
}
