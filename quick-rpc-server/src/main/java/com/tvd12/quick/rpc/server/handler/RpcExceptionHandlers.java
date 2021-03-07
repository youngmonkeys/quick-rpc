package com.tvd12.quick.rpc.server.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.reflect.EzyClassTree;

import lombok.Getter;

public class RpcExceptionHandlers {

	@Getter
	protected final List<Class<?>> exceptionClasses;
	@Getter
	protected final Map<Class<?>, RpcUncaughtExceptionHandler> handlers;
	
	protected RpcExceptionHandlers(Builder builder) {
		this.handlers = new HashMap<>(builder.handlers);
		this.exceptionClasses = new EzyClassTree(handlers.keySet()).toList();
	}
	
	@Override
	public String toString() {
		return "RpcExceptionHandlers(" + handlers + ")";
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcExceptionHandlers> {
		
		protected Map<Class<?>, RpcUncaughtExceptionHandler> handlers;
		
		public Builder() {
			this.handlers = new HashMap<>();
		}
		
		public Builder addHandler(
				Class<?> exceptionClass, RpcUncaughtExceptionHandler handler) {
			handlers.put(exceptionClass, handler);
			return this;
		}
		
		public Builder addHandlers(Map<Class<?>, RpcUncaughtExceptionHandler> handlers) {
			for(Class<?> exceptionClass : handlers.keySet()) {
				RpcUncaughtExceptionHandler handler = handlers.get(exceptionClass);
				addHandler(exceptionClass, handler);
			}
			return this;
		}
		
		@Override
		public RpcExceptionHandlers build() {
			return new RpcExceptionHandlers(this);
		}
		
	}
	
}
