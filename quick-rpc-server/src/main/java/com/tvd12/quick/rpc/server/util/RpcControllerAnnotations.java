package com.tvd12.quick.rpc.server.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.server.annotation.RpcController;

public final class RpcControllerAnnotations {

	private RpcControllerAnnotations() {}

	public static String getGroup(RpcController annotation) {
		String group = annotation.value();
		if(EzyStrings.isNoContent(group))
			group = annotation.group();
		return group;
	}
	
	public static String getGroup(Class<?> controllerClass) {
		RpcController annotation = controllerClass.getAnnotation(RpcController.class);
		return getGroup(annotation);
	}
	
}
