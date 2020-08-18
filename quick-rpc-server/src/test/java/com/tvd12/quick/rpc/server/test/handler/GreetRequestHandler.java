package com.tvd12.quick.rpc.server.test.handler;

import com.tvd12.quick.rpc.server.annotation.RpcHandler;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.handler.RpcAbstractRequestHandler;
import com.tvd12.quick.rpc.server.test.data.GreetRequest;
import com.tvd12.quick.rpc.server.test.data.GreetResponse;

@RpcHandler("Greet")
public class GreetRequestHandler extends RpcAbstractRequestHandler<GreetRequest> {

	@Override
	public void handle(RpcRequest<GreetRequest> request, RpcResponse response) {
		response.write(new GreetResponse("Greet " + request.getData().getWho() + "!"));
	}

}
