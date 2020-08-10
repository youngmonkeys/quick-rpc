package com.tvd12.quick.rpc.client.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.client.annotation.RpcErrorData;

public final class RpcErrorDataAnnotations {

	private RpcErrorDataAnnotations() {}
	
	public static String getCommand(RpcErrorData annotation) {
		if(EzyStrings.isNoContent(annotation.value()))
			return annotation.command();
		return annotation.value();
	}
	
	public static String getCommand(Class<?> rpcErrorDataClass) {
		return getCommand(rpcErrorDataClass.getAnnotation(RpcErrorData.class));
	}
	
}
