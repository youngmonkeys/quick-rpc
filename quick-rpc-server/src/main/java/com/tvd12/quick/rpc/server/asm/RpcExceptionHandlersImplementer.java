package com.tvd12.quick.rpc.server.asm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.quick.rpc.server.handler.RpcUncaughtExceptionHandler;
import com.tvd12.quick.rpc.server.reflect.RpcExceptionHandlerMethod;
import com.tvd12.quick.rpc.server.reflect.RpcExceptionHandlerProxy;

public class RpcExceptionHandlersImplementer extends EzyLoggable {
	
	public Map<Class<?>, RpcUncaughtExceptionHandler> 
			implement(Collection<Object> exceptionHandlers) {
		Map<Class<?>, RpcUncaughtExceptionHandler> handlers = new HashMap<>();
		for(Object controller : exceptionHandlers)
			handlers.putAll(implement(controller));
		return handlers;
	}
	
	public Map<Class<?>, RpcUncaughtExceptionHandler> implement(Object exceptionHandler) {
		Map<Class<?>, RpcUncaughtExceptionHandler> handlers = new HashMap<>();
		RpcExceptionHandlerProxy proxy = new RpcExceptionHandlerProxy(exceptionHandler);
		for(RpcExceptionHandlerMethod method : proxy.getExceptionHandlerMethods()) {
			RpcExceptionHandlerImplementer implementer = newImplementer(proxy, method);
			RpcUncaughtExceptionHandler handler = implementer.implement();
			for(Class<?> exceptionClass : method.getExceptionClasses())
				handlers.put(exceptionClass, handler);
		}
		return handlers;
	}
	
	protected RpcExceptionHandlerImplementer newImplementer(
			RpcExceptionHandlerProxy exceptionHandler, RpcExceptionHandlerMethod method) {
		return new RpcExceptionHandlerImplementer(exceptionHandler, method);
	}
	
}
