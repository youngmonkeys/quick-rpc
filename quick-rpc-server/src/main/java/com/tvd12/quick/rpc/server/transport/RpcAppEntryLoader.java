package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.ext.EzyAppEntry;
import com.tvd12.ezyfoxserver.ext.EzyAppEntryLoader;

public class RpcAppEntryLoader implements EzyAppEntryLoader {

	@Override
	public EzyAppEntry load() throws Exception {
		return new RpcAppEntry();
	}
	
}
