package com.tvd12.quick.rpc.client.net;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RpcSocketAddress {

	protected final String host;
	protected final int port;
	
	@Override
	public String toString() {
		return host + ":" + port;
	}
}
