package com.tvd12.quick.rpc.server;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfox.util.EzyStartable;
import com.tvd12.ezyfox.util.EzyStoppable;
import com.tvd12.ezyfoxserver.config.EzyConfig;
import com.tvd12.ezyfoxserver.config.EzyConfigBuilder;
import com.tvd12.ezyfoxserver.embedded.EzyEmbeddedServer;
import com.tvd12.ezyfoxserver.setting.EzyAdminSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyAppSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyPluginSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzySettings;
import com.tvd12.ezyfoxserver.setting.EzySettingsBuilder;
import com.tvd12.ezyfoxserver.setting.EzySimpleAdminSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleAppSetting;
import com.tvd12.ezyfoxserver.setting.EzySimplePluginSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleZoneSetting;
import com.tvd12.ezyfoxserver.setting.EzyZoneSettingBuilder;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;
import com.tvd12.quick.rpc.server.transport.RpcAppEntryLoader;
import com.tvd12.quick.rpc.server.transport.RpcPluginEntryLoader;

public class QuickRpcServer 
		extends EzyLoggable
		implements EzyStartable, EzyStoppable {

	protected EzyEmbeddedServer transporter;
	protected final QuickRpcSettings settings;
	
	public QuickRpcServer(QuickRpcSettings settings) {
		this.settings = settings;
	}
	
	@Override
	public void start() throws Exception {
		EzySimpleAdminSetting adminSetting = new EzyAdminSettingBuilder()
				.username(settings.getUsername())
				.password(settings.getPassword())
				.build();
		EzySimplePluginSetting pluginSetting = new EzyPluginSettingBuilder()
				.name("rpc")
				.entryLoader(RpcPluginEntryLoader.class)
				.build();
		EzySimpleAppSetting appSetting = new EzyAppSettingBuilder()
				.name("rpc")
				.entryLoader(RpcAppEntryLoader.class)
				.build();
		EzySimpleZoneSetting zoneSetting = new EzyZoneSettingBuilder()
				.name("rpc")
				.plugin(pluginSetting)
				.application(appSetting)
				.build();
		EzySettings settings = new EzySettingsBuilder()
				.nodeName("rpc")
				.admin(adminSetting)
				.zone(zoneSetting)
				.build();
		EzyConfig config = new EzyConfigBuilder()
				.bannerFile("quick-rpc-banner.txt")
				.build();
		transporter = EzyEmbeddedServer.builder()
				.config(config)
				.settings(settings)
				.build();
		transporter.start();
	}
	
	@Override
	public void stop() {
		if(transporter != null)
			transporter.stop();
	}
	
	
}
