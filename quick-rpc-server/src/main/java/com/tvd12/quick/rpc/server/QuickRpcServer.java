package com.tvd12.quick.rpc.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tvd12.ezyfox.bean.EzyBeanContext;
import com.tvd12.ezyfox.binding.EzyBindingContext;
import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.io.EzyMaps;
import com.tvd12.ezyfox.util.EzyLoggable;
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
import com.tvd12.ezyfoxserver.setting.EzySimpleUserManagementSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleZoneSetting;
import com.tvd12.ezyfoxserver.setting.EzyUserManagementSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyZoneSettingBuilder;
import com.tvd12.quick.rpc.server.annotation.RpcHandler;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandlers;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;
import com.tvd12.quick.rpc.server.transport.RpcAppEntryLoader;
import com.tvd12.quick.rpc.server.transport.RpcPluginEntryLoader;
import com.tvd12.quick.rpc.server.util.RpcHandlerAnnotations;

@SuppressWarnings("rawtypes")
public class QuickRpcServer extends EzyLoggable implements EzyStoppable {

	protected EzyEmbeddedServer transporter;
	protected EzyBeanContext beanContext;
	protected EzyBindingContext bindingContext;
	protected final QuickRpcSettings settings;
	protected final Set<String> packagesToScan;
	protected final Map<String, RpcRequestHandler> requestHandlers;
	
	public QuickRpcServer(QuickRpcSettings settings) {
		this.settings = settings;
		this.packagesToScan = new HashSet<>();
		this.requestHandlers = new HashMap<>();
	}
	
	public QuickRpcServer scan(String packageToScan) {
		this.packagesToScan.add(packageToScan);
		return this;
	}
	
	public QuickRpcServer scan(String... packagesToScan) {
		return scan(Arrays.asList(packagesToScan));
	}
	
	public QuickRpcServer scan(Iterable<String> packagesToScan) {
		for(String packageToScan : packagesToScan)
			scan(packageToScan);
		return this;
	}
	
	public QuickRpcServer beanContext(EzyBeanContext beanContext) {
		this.beanContext = beanContext;
		return this;
	}
	
	public QuickRpcServer bindingContext(EzyBindingContext bindingContext) {
		this.bindingContext = bindingContext;
		return this;
	}
	
	public QuickRpcServer addRequestHandler(String cmd, RpcRequestHandler handler) {
		this.requestHandlers.put(cmd, handler);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public RpcServerContext start() throws Exception {
		RpcSessionManager sessionManager = new RpcSessionManager();
		if(beanContext == null) {
			beanContext = EzyBeanContext.builder()
				.scan(packagesToScan)
				.build();
		}
		if(bindingContext == null) {
			bindingContext = EzyBindingContext.builder()
				.scan(packagesToScan)
				.build();
		}
		List requestHandlerBeans = beanContext.getSingletons(RpcHandler.class);
		RpcRequestHandlers requestHandlers = RpcRequestHandlers.builder()
				.addHandlers(this.requestHandlers)
				.addHandlers(EzyMaps.newHashMap(
						requestHandlerBeans, b -> RpcHandlerAnnotations.getCommand(b.getClass())))
				.build();
		RpcComponentManager componentManager = RpcComponentManager.getInstance();
		componentManager.addComponent(EzyBeanContext.class, beanContext);
		componentManager.addComponent(EzyMarshaller.class, bindingContext.newMarshaller());
		componentManager.addComponent(EzyUnmarshaller.class, bindingContext.newUnmarshaller());
		componentManager.addComponent(RpcSessionManager.class, sessionManager);
		componentManager.addComponent(RpcRequestHandlers.class, requestHandlers);
		RpcServerContext serverContext = new RpcServerContext();
		componentManager.addComponent(RpcServerContext.class, serverContext);
		
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
		EzySimpleUserManagementSetting userManagementSetting = new EzyUserManagementSettingBuilder()
				.maxSessionPerUser(Integer.MAX_VALUE)
				.build();
		EzySimpleZoneSetting zoneSetting = new EzyZoneSettingBuilder()
				.name("rpc")
				.plugin(pluginSetting)
				.application(appSetting)
				.userManagement(userManagementSetting)
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
		return serverContext;
	}
	
	@Override
	public void stop() {
		if(transporter != null)
			transporter.stop();
	}
	
	
}
