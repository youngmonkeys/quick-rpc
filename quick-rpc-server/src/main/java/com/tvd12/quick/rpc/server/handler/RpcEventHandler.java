package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.event.RpcEvent;

public interface RpcEventHandler<E extends RpcEvent> {

	void handle(E event);
	
}
