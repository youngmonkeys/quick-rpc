package com.tvd12.quick.rpc.client.test.data;

import com.tvd12.quick.rpc.core.annotation.RpcRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RpcRequest("Greet")
public class GreetResponse {

	protected String message;

	@Override
	public String toString() {
		return message;
	}
	
}
