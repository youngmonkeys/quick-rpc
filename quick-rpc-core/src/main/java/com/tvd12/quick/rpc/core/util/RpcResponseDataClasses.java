package com.tvd12.quick.rpc.core.util;

import java.util.Set;

import com.tvd12.ezyfox.collect.Sets;
import com.tvd12.quick.rpc.core.annotation.RpcResponse;

public final class RpcResponseDataClasses {
	
	private static final String RESPONSE_SUFFIX = "Response";

	private RpcResponseDataClasses() {}
	
	public static Set<String> getCommands(Class<?> responseClass) {
		RpcResponse ann = responseClass.getAnnotation(RpcResponse.class);
		if(ann != null)
			return RpcResponseAnnotations.getCommands(ann);
		String className = responseClass.getSimpleName();
		if(className.equals(RESPONSE_SUFFIX) ||
				!className.endsWith(RESPONSE_SUFFIX))
			return Sets.newHashSet(className);
		return Sets.newHashSet(className.substring(
				0, className.length() - RESPONSE_SUFFIX.length()));
		
	}
	
}
