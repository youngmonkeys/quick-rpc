package com.tvd12.quick.rpc.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tvd12.quick.rpc.core.annotation.RpcRequest;

public final class RpcRequestAnnotations {

	private RpcRequestAnnotations() {}
	
	public static Set<String> getCommands(RpcRequest annotation) {
		Set<String> commands = new HashSet<>();
		commands.addAll(Arrays.asList(annotation.value()));
		commands.addAll(Arrays.asList(annotation.commands()));
		return commands;
	}
	
}
