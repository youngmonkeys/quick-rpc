package com.tvd12.quick.rpc.core.util;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.quick.rpc.core.annotation.RpcRequest;

public final class RpcRequestAnnotations {

	private RpcRequestAnnotations() {}
	
	public static String getCommand(RpcRequest annotation) {
		String command = annotation.value();
		if(EzyStrings.isNoContent(command))
			command = annotation.command();
		return command;
	}
	
}
