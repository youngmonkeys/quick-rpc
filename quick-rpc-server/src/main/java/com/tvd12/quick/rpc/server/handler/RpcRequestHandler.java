package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.RpcServerContext;
import com.tvd12.quick.rpc.server.entity.RpcSession;

public interface RpcRequestHandler<R> {

	void handle(RpcServerContext context, RpcSession session, R request);
	
}
