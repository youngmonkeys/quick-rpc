package com.tvd12.quick.rpc.server.reflect;

import java.util.ArrayList;
import java.util.List;

import com.tvd12.ezyfox.reflect.EzyClass;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.Rpc;
import com.tvd12.quick.rpc.server.util.RpcControllerAnnotations;

import lombok.Getter;

@Getter
public class RpcControllerProxy {

	protected final EzyClass clazz;
	protected final Object instance;
	protected final String commandGroup;
	protected final List<RpcRequestHandlerMethod> requestHandlerMethods;
	
	public RpcControllerProxy(Object instance) {
		this.instance = instance;
		this.clazz = new EzyClass(instance.getClass());
		this.commandGroup = getCommandGroup();
		this.requestHandlerMethods = fetchRequestHandlerMethods();
	}
	
	protected String getCommandGroup() {
		String uri = RpcControllerAnnotations.getGroup(clazz.getClazz());
		return uri;
	}
	
	protected List<RpcRequestHandlerMethod> fetchRequestHandlerMethods() {
		List<RpcRequestHandlerMethod> list = new ArrayList<>();
		List<EzyMethod> methods = clazz.getPublicMethods(m -> isRequestHandlerMethod(m));
		for(EzyMethod method : methods) {
			RpcRequestHandlerMethod m = new RpcRequestHandlerMethod(commandGroup, method);
			list.add(m);
		}
		return list;
	}
	
	protected boolean isRequestHandlerMethod(EzyMethod method) {
		return method.isAnnotated(Rpc.class);
	}
	
	public String getControllerName() {
		return clazz.getClazz().getSimpleName();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(clazz.getName())
				.append("(\n")
					.append("\tinstance: ").append(instance).append(",\n")
					.append("\trequestHandlerMethods: ").append(requestHandlerMethods).append(",\n")
				.append(")")
				.toString();
	}
	
}
