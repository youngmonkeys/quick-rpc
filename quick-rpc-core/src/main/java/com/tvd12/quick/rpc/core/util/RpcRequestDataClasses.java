package com.tvd12.quick.rpc.core.util;

import java.util.Set;

import com.tvd12.ezyfox.collect.Sets;
import com.tvd12.quick.rpc.core.annotation.RpcRequest;

public final class RpcRequestDataClasses {
	
	private static final String REQUEST_SUFFIX = "Request";

	private RpcRequestDataClasses() {}
	
	public static Set<String> getCommands(Class<?> requestClass) {
		RpcRequest ann = requestClass.getAnnotation(RpcRequest.class);
		if(ann != null)
			return RpcRequestAnnotations.getCommands(ann);
		String className = requestClass.getSimpleName();
		if(className.equals(REQUEST_SUFFIX) ||
				!className.endsWith(REQUEST_SUFFIX))
			return Sets.newHashSet(className);
		return Sets.newHashSet(className.substring(
				0, className.length() - REQUEST_SUFFIX.length()));
		
	}
	
}
