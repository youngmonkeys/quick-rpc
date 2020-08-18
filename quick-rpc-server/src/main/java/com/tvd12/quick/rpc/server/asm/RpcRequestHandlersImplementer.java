package com.tvd12.quick.rpc.server.asm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.quick.rpc.server.exception.RpcDuplicateRequestHandlerException;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.reflect.RpcControllerProxy;
import com.tvd12.quick.rpc.server.reflect.RpcRequestHandlerMethod;

@SuppressWarnings("rawtypes")
public class RpcRequestHandlersImplementer extends EzyLoggable {
	
	public Map<String, RpcRequestHandler> implement(Collection<Object> controllers) {
		Map<String, RpcRequestHandler> handlers = new HashMap<>();
		for(Object controller : controllers) {
			Map<String, RpcRequestHandler> map = implement(controller);
			for(String command : map.keySet()) {
				RpcRequestHandler handler = map.get(command);
				RpcRequestHandler old = handlers.put(command, handler);
				if(old != null)
					throw new RpcDuplicateRequestHandlerException(command, old, handler);
			}
		}
		return handlers;
	}
	
	public Map<String, RpcRequestHandler> implement(Object controller) {
		Map<String, RpcRequestHandler> handlers = new HashMap<>();
		RpcControllerProxy proxy = new RpcControllerProxy(controller);
		for(RpcRequestHandlerMethod method : proxy.getRequestHandlerMethods()) {
			RpcRequestHandlerImplementer implementer = newImplementer(proxy, method);
			RpcAsmRequestHandler handler = implementer.implement();
			String command = handler.getCommand();
			RpcRequestHandler old = handlers.put(command, handler);
			if(old != null)
				throw new RpcDuplicateRequestHandlerException(command, old, handler);
		}
		return handlers;
	}
	
	protected RpcRequestHandlerImplementer newImplementer(
			RpcControllerProxy controller, RpcRequestHandlerMethod method) {
		return new RpcRequestHandlerImplementer(controller, method);
	}
	
}
