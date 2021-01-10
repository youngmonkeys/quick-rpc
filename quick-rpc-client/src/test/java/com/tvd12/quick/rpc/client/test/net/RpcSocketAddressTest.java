package com.tvd12.quick.rpc.client.test.net;

import org.testng.annotations.Test;

import com.tvd12.quick.rpc.client.net.RpcSocketAddress;

public class RpcSocketAddressTest {

	@Test
	public void test() {
		// given
		String host = "testHost";
		int port = 3005;
		
		// when
		RpcSocketAddress address = new RpcSocketAddress(host, port);
		
		// then
		assert address.toString().equals(host + ":" + port);
		assert address.hashCode() != 0;
	}
	
}
