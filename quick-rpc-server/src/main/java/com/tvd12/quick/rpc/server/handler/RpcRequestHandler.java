package com.tvd12.quick.rpc.server.handler;

import com.tvd12.quick.rpc.server.entity.RpcSession;

public interface RpcRequestHandler<D> {

	void handle(RpcSession session, String requestId, D data);

	default D newData() {
		return null;
	}
}
