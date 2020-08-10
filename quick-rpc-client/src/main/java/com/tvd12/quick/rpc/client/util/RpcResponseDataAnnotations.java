package com.tvd12.quick.rpc.client.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.client.annotation.RpcResponseData;

public final class RpcResponseDataAnnotations {

	private RpcResponseDataAnnotations() {}
	
	public static String getCommand(RpcResponseData annotation) {
		if(EzyStrings.isNoContent(annotation.value()))
			return annotation.command();
		return annotation.value();
	}
	
	public static String getCommand(Class<?> rpcResponseDataClass) {
		return getCommand(rpcResponseDataClass.getAnnotation(RpcResponseData.class));
	}
	
}
