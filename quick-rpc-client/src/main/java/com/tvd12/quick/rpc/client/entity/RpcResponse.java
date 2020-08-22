package com.tvd12.quick.rpc.client.entity;

import com.tvd12.ezyfox.entity.EzyData;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RpcResponse {

	protected final String command;
	protected final String requestId;
	protected final EzyData data;
	protected final boolean error;
	
}
