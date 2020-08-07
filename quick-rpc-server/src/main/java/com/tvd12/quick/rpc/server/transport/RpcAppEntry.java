package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.command.EzyAppSetup;
import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.ext.EzyAppEntry;

public class RpcAppEntry implements EzyAppEntry {

	@Override
	public void config(EzyAppContext ctx) {
		ctx.get(EzyAppSetup.class)
			.setRequestController(new RpcAppRequestController())
			.addEventController(EzyEventType.SESSION_REMOVED, new RpcSessionRemoveController());
	}
	
}
