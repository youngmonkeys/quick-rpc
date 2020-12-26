package com.tvd12.quick.rpc.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tvd12.quick.rpc.core.annotation.RpcResponse;

public final class RpcResponseAnnotations {

	private RpcResponseAnnotations() {}
	
	public static Set<String> getCommands(RpcResponse annotation) {
		Set<String> commands = new HashSet<>();
		commands.addAll(Arrays.asList(annotation.value()));
		commands.addAll(Arrays.asList(annotation.commands()));
		for(Class<?> requestClass : annotation.requests())
			commands.addAll(RpcRequestDataClasses.getCommands(requestClass));
		return commands;
	}
	
}
