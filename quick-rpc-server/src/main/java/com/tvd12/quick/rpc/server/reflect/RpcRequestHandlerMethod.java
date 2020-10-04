package com.tvd12.quick.rpc.server.reflect;

import java.lang.reflect.Parameter;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.Rpc;
import com.tvd12.quick.rpc.server.annotation.RpcRequestData;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.util.RpcAnnotations;

import lombok.Getter;

@Getter
public class RpcRequestHandlerMethod {

	protected final String command;
	protected final EzyMethod method;
	
	public RpcRequestHandlerMethod(String group, EzyMethod method) {
		this.method = method;
		this.command = fetchCommand(group);
	}
	
	protected String fetchCommand(String group) {
		String methodCommand = RpcAnnotations.getCommand(method.getAnnotation(Rpc.class));
		if(EzyStrings.isNoContent(group))
			return methodCommand;
		return group + "/" + methodCommand;
	}
	
	
	public String getName() {
		return method.getName();
	}
	
	public Class<?> getReturnType() {
		return method.getReturnType();
	}
	
	public Parameter[] getParameters() {
		return method.getMethod().getParameters();
	}
	
	public Class<?> getRequestDataType() {
		Class<?> dataType = null;
		for(Parameter parameter : getParameters()) {
			Class<?> type = parameter.getType();
			if(type != RpcRequest.class &&
					type != RpcResponse.class &&
					type != RpcSession.class) {
				dataType = type;
				if(type.isAnnotationPresent(RpcRequestData.class))
					break;
			}
		}
		return dataType;
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
