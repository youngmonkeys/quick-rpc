package com.tvd12.quick.rpc.server.test;

import com.tvd12.quick.rpc.server.QuickRpcServer;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;

public class QuickRpcServerTest {

	public static void main(String[] args) throws Exception {
		QuickRpcSettings settings = QuickRpcSettings.builder()
				.username("admin")
				.password("admin")
				.build();
		QuickRpcServer server = new QuickRpcServer(settings);
		server.start();
		server.stop();
	}
	
}
