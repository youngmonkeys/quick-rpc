package com.tvd12.quick.rpc.server.reflect;

import com.tvd12.ezyfox.core.annotation.EzyTryCatch;
import com.tvd12.ezyfox.core.util.EzyTryCatchAnnotations;
import com.tvd12.ezyfox.reflect.EzyMethod;

import lombok.Getter;

@Getter
public class RpcExceptionHandlerMethod extends RpcHandlerMethod {

	protected final Class<?>[] exceptionClasses;
	
	public RpcExceptionHandlerMethod(EzyMethod method) {
		super(method);
		this.exceptionClasses = fetchExceptionClasses();
		
	}
	
	protected Class<?>[] fetchExceptionClasses() {
		EzyTryCatch annotation = method.getAnnotation(EzyTryCatch.class);
		Class<?>[] classes = EzyTryCatchAnnotations.getExceptionClasses(annotation);
		return classes;
	}
	
}
