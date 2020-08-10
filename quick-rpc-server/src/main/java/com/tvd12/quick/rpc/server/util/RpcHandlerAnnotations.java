package com.tvd12.quick.rpc.server.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.server.annotation.RpcHandler;

public final class RpcHandlerAnnotations {

	private RpcHandlerAnnotations() {}
	
	public static String getCommand(RpcHandler annotation) {
		if(EzyStrings.isNoContent(annotation.value()))
			return annotation.command();
		return annotation.value();
	}
	
	public static String getCommand(Class<?> rpcHandlerClass) {
		return getCommand(rpcHandlerClass.getAnnotation(RpcHandler.class));
	}
	
}
