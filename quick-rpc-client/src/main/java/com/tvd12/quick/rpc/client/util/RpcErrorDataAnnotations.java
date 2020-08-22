package com.tvd12.quick.rpc.client.util;

import com.tvd12.quick.rpc.client.annotation.RpcErrorData;

public final class RpcErrorDataAnnotations {

	private RpcErrorDataAnnotations() {}
	
	public static String[] getCommands(RpcErrorData annotation) {
		if(annotation.value().length > 0)
			return annotation.value();
		return annotation.commands();
	}
	
	public static String[] getCommands(Class<?> errorDataClass) {
		return getCommands(errorDataClass.getAnnotation(RpcErrorData.class));
	}
	
}
