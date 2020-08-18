package com.tvd12.quick.rpc.server.asm;

import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;

public interface RpcAsmRequestHandler extends RpcRequestHandler<Object> {
	
	void setController(Object controller);
	
	String getCommand();
	
	void setCommand(String command);
	
}
