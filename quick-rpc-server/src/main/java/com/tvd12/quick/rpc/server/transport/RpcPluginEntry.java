package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfoxserver.command.EzyPluginSetup;
import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.context.EzyPluginContext;
import com.tvd12.ezyfoxserver.ext.EzyPluginEntry;

public class RpcPluginEntry implements EzyPluginEntry {

	@Override
	public void config(EzyPluginContext ctx) {
		ctx.get(EzyPluginSetup.class)
			.addEventController(EzyEventType.USER_LOGIN, new RpcAuthenController());
	}

}
