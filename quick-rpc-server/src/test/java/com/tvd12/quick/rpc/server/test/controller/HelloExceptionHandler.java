package com.tvd12.quick.rpc.server.test.controller;

import com.tvd12.quick.rpc.server.annotation.RpcExceptionHandler;
import com.tvd12.quick.rpc.server.annotation.RpcTryCatch;

@RpcExceptionHandler
public class HelloExceptionHandler {

	@RpcTryCatch(IllegalArgumentException.class)
	public void handleIllegalArgumentException(IllegalArgumentException e) {
	}

}
