package com.tvd12.quick.rpc.client.util;

import com.tvd12.quick.rpc.client.annotation.RpcResponseData;

public final class RpcResponseDataAnnotations {

	private RpcResponseDataAnnotations() {}
	
	public static String[] getCommands(RpcResponseData annotation) {
		if(annotation.value().length > 0)
			return annotation.value();
		return annotation.commands();
	}
	
	public static String[] getCommands (Class<?> responseDataClass) {
		return getCommands(responseDataClass.getAnnotation(RpcResponseData.class));
	}
	
}
