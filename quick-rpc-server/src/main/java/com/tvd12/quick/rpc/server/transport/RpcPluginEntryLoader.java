package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.ext.EzyPluginEntry;
import com.tvd12.ezyfoxserver.ext.EzyPluginEntryLoader;

public class RpcPluginEntryLoader implements EzyPluginEntryLoader {

	@Override
	public EzyPluginEntry load() throws Exception {
		return new RpcPluginEntry();
	}
	
}
