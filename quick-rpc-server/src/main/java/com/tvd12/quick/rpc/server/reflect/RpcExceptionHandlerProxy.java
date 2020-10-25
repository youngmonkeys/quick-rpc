package com.tvd12.quick.rpc.server.reflect;

import java.util.ArrayList;
import java.util.List;

import com.tvd12.ezyfox.core.annotation.EzyTryCatch;
import com.tvd12.ezyfox.reflect.EzyClass;
import com.tvd12.ezyfox.reflect.EzyMethod;

import lombok.Getter;

@Getter
public class RpcExceptionHandlerProxy {

	protected final EzyClass clazz;
	protected final Object instance;
	protected final List<RpcExceptionHandlerMethod> exceptionHandlerMethods;
	
	public RpcExceptionHandlerProxy(Object instance) {
		this.instance = instance;
		this.clazz = new EzyClass(instance.getClass());
		this.exceptionHandlerMethods = fetchExceptionHandlerMethods();
	}
	
	public List<RpcExceptionHandlerMethod> fetchExceptionHandlerMethods() {
		List<RpcExceptionHandlerMethod> list = new ArrayList<>();
		List<EzyMethod> methods = clazz.getPublicMethods(m -> m.isAnnotated(EzyTryCatch.class));
		for(EzyMethod method : methods) {
			RpcExceptionHandlerMethod m = new RpcExceptionHandlerMethod(method);
			list.add(m);
		}
		return list;
	}
	
	public String getClassSimpleName() {
		return clazz.getClazz().getSimpleName();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(clazz.getName())
				.append("(\n")
					.append("\tinstance: ").append(instance).append(",\n")
					.append("\texceptionHandlerMethods: ").append(exceptionHandlerMethods).append("\n")
				.append(")")
				.toString();
	}
	
}
