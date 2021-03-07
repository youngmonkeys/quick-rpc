package com.tvd12.quick.rpc.client.handler;

import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.builder.EzyBuilder;

public class RpcResponseHandlers {

	protected final Map<String, RpcResponseHandler> handlers;
	
	protected RpcResponseHandlers(Builder builder) {
		this.handlers = new HashMap<>(builder.handlers);
	}
	
	public RpcResponseHandler getHandler(String command) {
		return handlers.get(command);
	}
	
	@Override
	public String toString() {
		return "RpcResponseHandlers(" + handlers + ")";
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcResponseHandlers> {
		
		protected Map<String, RpcResponseHandler> handlers;
		
		public Builder() {
			this.handlers = new HashMap<>();
		}
		
		public Builder addHandler(String cmd, RpcResponseHandler handler) {
			handlers.put(cmd, handler);
			return this;
		}
		
		public Builder addHandlers(Map<String, RpcResponseHandler> handlers) {
			for(String cmd : handlers.keySet()) {
				RpcResponseHandler handler = handlers.get(cmd);
				addHandler(cmd, handler);
			}
			return this;
		}
		
		@Override
		public RpcResponseHandlers build() {
			return new RpcResponseHandlers(this);
		}
		
	}
	
}
