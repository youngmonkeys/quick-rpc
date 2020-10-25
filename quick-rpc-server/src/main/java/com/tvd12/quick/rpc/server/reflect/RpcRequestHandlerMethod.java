package com.tvd12.quick.rpc.server.reflect;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.Rpc;
import com.tvd12.quick.rpc.server.util.RpcAnnotations;

import lombok.Getter;

@Getter
public class RpcRequestHandlerMethod extends RpcHandlerMethod {

	protected final String command;
	
	public RpcRequestHandlerMethod(String group, EzyMethod method) {
		super(method);
		this.command = fetchCommand(group);
	}
	
	protected String fetchCommand(String group) {
		String methodCommand = RpcAnnotations.getCommand(method.getAnnotation(Rpc.class));
		if(EzyStrings.isNoContent(group))
			return methodCommand;
		return group + "/" + methodCommand;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(method.getName())
				.append("(")
					.append("command: ").append(command)
				.append(")")
				.toString();
	}
	
}
