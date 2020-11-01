package com.tvd12.quick.rpc.server.asm;

import com.tvd12.quick.rpc.server.handler.RpcUncaughtExceptionHandler;

public interface RpcAsmUncaughtExceptionHandler<D>
		extends RpcUncaughtExceptionHandler<D> {

	void setExceptionHandler(Object exceptionHandler);

}
