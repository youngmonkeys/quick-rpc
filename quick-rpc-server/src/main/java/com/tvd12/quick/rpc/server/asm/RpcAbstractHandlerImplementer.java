package com.tvd12.quick.rpc.server.asm;

import java.lang.reflect.Parameter;

import com.tvd12.ezyfox.asm.EzyFunction.EzyBody;
import com.tvd12.ezyfox.asm.EzyInstruction;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.reflect.RpcExceptionHandlerMethod;
import com.tvd12.quick.rpc.server.reflect.RpcHandlerMethod;

public class RpcAbstractHandlerImplementer<H extends RpcHandlerMethod> 
		extends EzyLoggable {
	
	protected final H handlerMethod;
	
	protected final static String PARAMETER_PREFIX = "param";
	
	public RpcAbstractHandlerImplementer(H handlerMethod) {
		this.handlerMethod = handlerMethod;
	}

	protected int prepareHandleMethodArguments(EzyBody body) {
		int paramCount = 0;
		Parameter[] parameters = handlerMethod.getParameters();
		for(Parameter parameter : parameters) {
			Class<?> parameterType = parameter.getType();
			EzyInstruction instruction = new EzyInstruction("\t", "\n")
					.clazz(parameterType)
					.append(" ").append(PARAMETER_PREFIX).append(paramCount)
					.equal();
			if(parameterType == RpcRequest.class) {
				instruction.append("arg0");
			}
			else if(parameterType == RpcResponse.class) {
				instruction.append("arg1");
			}
			else if(parameterType == RpcSession.class) {
				instruction.append("arg0.getSession()");
			}
			else if(parameterType == boolean.class) {
				instruction.append("false");
			}
			else if(parameterType.isPrimitive()) {
				instruction.append("0");
			}
			else {
				instruction.append("null");
			}
			body.append(instruction);
			++ paramCount;
		}
		return paramCount;
	}
	
	protected void appendHandleExceptionMethodArguments(
			RpcExceptionHandlerMethod method, 
			EzyInstruction instruction, Class<?> exceptionClass) {
		int paramCount = 0;
		Parameter[] parameters = method.getParameters();
		for(Parameter parameter : parameters) {
			Class<?> parameterType = parameter.getType();
			if(parameterType == RpcRequest.class) {
				instruction.append("arg0");
			}
			else if(parameterType == RpcResponse.class) {
				instruction.append("arg1");
			}
			else if(parameterType == RpcSession.class) {
				instruction.append("arg0.getSessioon()");
			}
			else if(Throwable.class.isAssignableFrom(parameterType)) {
				instruction.brackets(exceptionClass).append("arg2");
			}
			else if(parameterType == boolean.class) {
				instruction.append("false");
			}
			else if(parameterType.isPrimitive()) {
				instruction.append("0");
			}
			else {
				instruction.append("null");
			}
			if((paramCount ++) < (parameters.length - 1))
				instruction.append(", ");
		}
	}
	
}
