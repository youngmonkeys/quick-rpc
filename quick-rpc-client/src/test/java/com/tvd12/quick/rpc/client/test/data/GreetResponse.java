package com.tvd12.quick.rpc.client.test.data;

import com.tvd12.quick.rpc.client.annotation.RpcResponseData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RpcResponseData("Greet")
public class GreetResponse {

	protected String message;

	@Override
	public String toString() {
		return message;
	}
	
}
