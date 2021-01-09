package com.tvd12.quick.rpc.client.test.data;

import com.tvd12.quick.rpc.core.annotation.RpcResponse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RpcResponse
@EqualsAndHashCode
@AllArgsConstructor
public class GreetResponse {

	protected String message;

	@Override
	public String toString() {
		return message;
	}
	
}
