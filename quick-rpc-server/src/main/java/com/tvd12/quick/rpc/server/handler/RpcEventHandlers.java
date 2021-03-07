package com.tvd12.quick.rpc.server.handler;

import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.quick.rpc.server.event.RpcEvent;
import com.tvd12.quick.rpc.server.event.RpcEventType;

@SuppressWarnings("rawtypes")
public class RpcEventHandlers {

	protected final Map<RpcEventType, RpcEventHandler> handlers;
	
	protected RpcEventHandlers(Builder builder) {
		this.handlers = new HashMap<>(builder.handlers);
	}
	
	public RpcEventHandler getHandler(RpcEventType eventType) {
		return handlers.get(eventType);
	}
	
	@SuppressWarnings("unchecked")
	public void handle(RpcEvent event) {
		RpcEventHandler handler = handlers.get(event.getEventType());
		if(handler != null)
			handler.handle(event);
	}
	
	@Override
	public String toString() {
		return "RpcEventHandlers(" + handlers + ")";
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcEventHandlers> {
		
		protected Map<RpcEventType, RpcEventHandler> handlers;
		
		public Builder() {
			this.handlers = new HashMap<>();
		}
		
		public Builder addHandler(RpcEventType eventType, RpcEventHandler handler) {
			handlers.put(eventType, handler);
			return this;
		}
		
		public Builder addHandlers(Map<RpcEventType, RpcEventHandler> handlers) {
			for(RpcEventType eventType : handlers.keySet()) {
				RpcEventHandler handler = handlers.get(eventType);
				addHandler(eventType, handler);
			}
			return this;
		}
		
		@Override
		public RpcEventHandlers build() {
			return new RpcEventHandlers(this);
		}
	}
}