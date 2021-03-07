package com.tvd12.quick.rpc.server;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.tvd12.ezyfox.bean.EzyBeanContext;
import com.tvd12.ezyfox.bean.EzyBeanContextBuilder;
import com.tvd12.ezyfox.binding.EzyBindingContext;
import com.tvd12.ezyfox.binding.EzyBindingContextBuilder;
import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.binding.writer.EzyDefaultWriter;
import com.tvd12.ezyfox.io.EzyMaps;
import com.tvd12.ezyfox.reflect.EzyReflection;
import com.tvd12.ezyfox.reflect.EzyReflectionProxy;
import com.tvd12.ezyfox.util.EzyStoppable;
import com.tvd12.ezyfoxserver.config.EzyConfig;
import com.tvd12.ezyfoxserver.config.EzyConfigBuilder;
import com.tvd12.ezyfoxserver.embedded.EzyEmbeddedServer;
import com.tvd12.ezyfoxserver.setting.EzyAdminSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyAppSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyPluginSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSettingBuilder.EzyMaxRequestPerSecondBuilder;
import com.tvd12.ezyfoxserver.setting.EzySettings;
import com.tvd12.ezyfoxserver.setting.EzySettingsBuilder;
import com.tvd12.ezyfoxserver.setting.EzySimpleAdminSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleAppSetting;
import com.tvd12.ezyfoxserver.setting.EzySimplePluginSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleSessionManagementSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleSessionManagementSetting.EzySimpleMaxRequestPerSecond;
import com.tvd12.ezyfoxserver.setting.EzySimpleSocketSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleThreadPoolSizeSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleUserManagementSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleWebSocketSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleZoneSetting;
import com.tvd12.ezyfoxserver.setting.EzySocketSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyThreadPoolSizeSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyUserManagementSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyWebSocketSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyZoneSettingBuilder;
import com.tvd12.quick.rpc.core.annotation.RpcError;
import com.tvd12.quick.rpc.core.annotation.RpcRequest;
import com.tvd12.quick.rpc.core.annotation.RpcResponse;
import com.tvd12.quick.rpc.core.data.RpcBadRequestErrorData;
import com.tvd12.quick.rpc.core.util.RpcPropertiesKeeper;
import com.tvd12.quick.rpc.core.util.RpcRequestDataClasses;
import com.tvd12.quick.rpc.server.annotation.RpcController;
import com.tvd12.quick.rpc.server.annotation.RpcEventHandled;
import com.tvd12.quick.rpc.server.annotation.RpcExceptionHandler;
import com.tvd12.quick.rpc.server.annotation.RpcInterceptor;
import com.tvd12.quick.rpc.server.annotation.RpcRequestHandled;
import com.tvd12.quick.rpc.server.asm.RpcExceptionHandlersImplementer;
import com.tvd12.quick.rpc.server.asm.RpcRequestHandlersImplementer;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.event.RpcEventType;
import com.tvd12.quick.rpc.server.event.RpcSessionRemoveEvent;
import com.tvd12.quick.rpc.server.handler.RpcEventHandler;
import com.tvd12.quick.rpc.server.handler.RpcEventHandlers;
import com.tvd12.quick.rpc.server.handler.RpcExceptionHandlers;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandlers;
import com.tvd12.quick.rpc.server.handler.RpcRequestInterceptor;
import com.tvd12.quick.rpc.server.handler.RpcRequestInterceptors;
import com.tvd12.quick.rpc.server.handler.RpcUncaughtExceptionHandler;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;
import com.tvd12.quick.rpc.server.transport.RpcAppEntryLoader;
import com.tvd12.quick.rpc.server.transport.RpcPluginEntryLoader;
import com.tvd12.quick.rpc.server.util.RpcRequestHandledAnnotations;

@SuppressWarnings("rawtypes")
public class QuickRpcServer 
		extends RpcPropertiesKeeper<QuickRpcServer> 
		implements EzyStoppable {

	protected EzyEmbeddedServer transporter;
	protected EzyBeanContext beanContext;
	protected EzyBindingContext bindingContext;
	protected EzyBeanContextBuilder beanContextBuilder;
	protected EzyBindingContextBuilder bindingContextBuilder;
	protected QuickRpcSettings settings;
	protected RpcEventHandlers eventHandlers;
	protected RpcRequestHandlers requestHandlers;
	protected RpcRequestInterceptors requestInterceptors;
	protected RpcExceptionHandlers exceptionHandlers;
	protected final Set<String> packagesToScan;
	protected final RpcEventHandlers.Builder eventHandlersBuilder;
	protected final RpcRequestHandlers.Builder requestHandlersBuilder;
	protected final RpcRequestInterceptors.Builder requestInterceptorsBuilder;
	protected final RpcExceptionHandlers.Builder exceptionHandlersBuilder;
	
	{
		this.packagesToScan = new HashSet<>();
		this.eventHandlersBuilder = RpcEventHandlers.builder();
		this.requestHandlersBuilder = RpcRequestHandlers.builder();
		this.requestInterceptorsBuilder = RpcRequestInterceptors.builder();
		this.exceptionHandlersBuilder = RpcExceptionHandlers.builder();
	}
	
	public QuickRpcServer() {}
	
	public QuickRpcServer(QuickRpcSettings settings) {
		this.settings = settings;
		
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
	
	public QuickRpcServer settings(QuickRpcSettings settings) {
		this.settings = settings;
		return this;
	}
	
	public QuickRpcServer beanContext(EzyBeanContext beanContext) {
		this.beanContext = beanContext;
		return this;
	}
	
	public QuickRpcServer beanContextBuilder(EzyBeanContextBuilder beanContextBuilder) {
		this.beanContextBuilder = beanContextBuilder;
		return this;
	}
	
	public QuickRpcServer bindingContextBuilder(EzyBindingContextBuilder bindingContextBuilder) {
		this.bindingContextBuilder = bindingContextBuilder;
		return this;
	}
	
	public QuickRpcServer bindingContext(EzyBindingContext bindingContext) {
		this.bindingContext = bindingContext;
		return this;
	}
	
	public <D> QuickRpcServer addRequestHandler(String cmd, RpcRequestHandler<D> handler) {
		this.requestHandlersBuilder.addHandler(cmd, handler);
		return this;
	}
	
	public <D> QuickRpcServer addRequestHandler(Class<D> requestDataClass, RpcRequestHandler<D> handler) {
		return addRequestHandler(RpcRequestDataClasses.getCommand(requestDataClass), handler);
	}
	
	public QuickRpcServer addRequestInterceptor(RpcRequestInterceptor interceptor) {
		this.requestInterceptorsBuilder.addInteceptor(interceptor);
		return this;
	}
	
	public QuickRpcServer addExceptionHandler(
			Class<?> exceptionClass, RpcUncaughtExceptionHandler handler) {
		this.exceptionHandlersBuilder.addHandler(exceptionClass, handler);
		return this;
	}
	
	public QuickRpcServer onSessionRemoved(Consumer<RpcSession> handler) {
		return this.addEventHandler(
				RpcEventType.SESSION_REMOVED, 
				e -> handler.accept(((RpcSessionRemoveEvent)e).getSession())
		);
	}
	
	public QuickRpcServer addEventHandler(RpcEventType eventType, RpcEventHandler handler) {
		this.eventHandlersBuilder.addHandler(eventType, handler);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public RpcServerContext start() throws Exception {
		if(settings == null) {
			settings = QuickRpcSettings.builder()
					.properties(properties)
					.build();
		}
		EzyReflection reflection = null;
		if(packagesToScan.size() > 0)
			reflection = new EzyReflectionProxy(packagesToScan);
		RpcSessionManager sessionManager = new RpcSessionManager();
		if(beanContext == null) {
			if(beanContextBuilder == null)
				beanContextBuilder= EzyBeanContext.builder();
			if(reflection != null) {
				beanContextBuilder
				.addSingletonClasses((Set)reflection.getAnnotatedClasses(RpcEventHandled.class))
					.addSingletonClasses((Set)reflection.getAnnotatedClasses(RpcRequestHandled.class))
					.addSingletonClasses((Set)reflection.getAnnotatedClasses(RpcController.class))
					.addSingletonClasses((Set)reflection.getAnnotatedClasses(RpcExceptionHandler.class))
					.addSingletonClasses((Set)reflection.getAnnotatedClasses(RpcInterceptor.class))
					.addAllClasses(reflection)
					.addSingleton("sessionManager", sessionManager);
			}
			beanContext = beanContextBuilder.build();
		}
		if(bindingContext == null) {
			if(bindingContextBuilder == null) {
				bindingContextBuilder = EzyBindingContext.builder()
					.addArrayBindingClass(RpcBadRequestErrorData.class)
					.addTemplate(BigDecimal.class, EzyDefaultWriter.getInstance())
					.addTemplate(BigInteger.class, EzyDefaultWriter.getInstance());
			}
			if(reflection != null) {
				Set<Class<?>> requestDataClasses = reflection.getAnnotatedClasses(RpcRequest.class);
				Set<Class<?>> responseDataClasses = reflection.getAnnotatedClasses(RpcResponse.class);
				Set<Class<?>> errorDataClasses = reflection.getAnnotatedClasses(RpcError.class);
				bindingContextBuilder.addClasses((Set)requestDataClasses);
				bindingContextBuilder.addClasses((Set)responseDataClasses);
				bindingContextBuilder.addClasses((Set)errorDataClasses);
				bindingContextBuilder.addAllClasses(reflection);
			}
			bindingContext = bindingContextBuilder.build();
		}
		eventHandlersBuilder.addHandlers(
			(Map)EzyMaps.newHashMap(
				beanContext.getSingletons(RpcEventHandled.class), 
				it -> it.getClass().getAnnotation(RpcEventHandled.class).value()
			)
		);
		
		List<Object> controllers = beanContext.getSingletons(RpcController.class);
		RpcRequestHandlersImplementer requestHandlersImplementer = new RpcRequestHandlersImplementer();
		requestHandlersBuilder.addHandlers(requestHandlersImplementer.implement(controllers));
		requestHandlersBuilder.addHandlers(
			(Map)EzyMaps.newHashMap(
				beanContext.getSingletons(RpcRequestHandled.class), 
				it -> RpcRequestHandledAnnotations.getCommand(it.getClass())
			)
		);
		
		requestInterceptorsBuilder.addInteceptors(beanContext.getSingletons(RpcInterceptor.class));
		
		List<Object> exceptionControllers = beanContext.getSingletons(RpcExceptionHandler.class);
		RpcExceptionHandlersImplementer exceptionHandlersImplementer = new RpcExceptionHandlersImplementer();
		exceptionHandlersBuilder.addHandlers(exceptionHandlersImplementer.implement(exceptionControllers));
		
		eventHandlers = eventHandlersBuilder.build();
		requestHandlers = requestHandlersBuilder.build();
		requestInterceptors = requestInterceptorsBuilder.build();
		exceptionHandlers = exceptionHandlersBuilder.build();
		
		RpcComponentManager componentManager = new RpcComponentManager();
		componentManager.addComponent(EzyBeanContext.class, beanContext);
		componentManager.addComponent(EzyMarshaller.class, bindingContext.newMarshaller());
		componentManager.addComponent(EzyUnmarshaller.class, bindingContext.newUnmarshaller());
		componentManager.addComponent(RpcSessionManager.class, sessionManager);
		componentManager.addComponent(RpcEventHandlers.class, eventHandlers);
		componentManager.addComponent(RpcRequestHandlers.class, requestHandlers);
		componentManager.addComponent(RpcExceptionHandlers.class, exceptionHandlers);
		componentManager.addComponent(RpcRequestInterceptors.class, requestInterceptors);
		RpcServerContext serverContext = new RpcServerContext(componentManager);
		componentManager.addComponent(RpcServerContext.class, serverContext);
		
		EzySimpleSocketSetting socketSetting = new EzySocketSettingBuilder()
				.address(settings.getHost())
				.port(settings.getPort())
				.build();
		EzySimpleWebSocketSetting webSocketSetting = new EzyWebSocketSettingBuilder()
				.active(false)
				.build();
		EzySimpleThreadPoolSizeSetting threadPoolSizeSetting = new EzyThreadPoolSizeSettingBuilder()
				.socketUserRemovalHandler(1)
				.socketDisconnectionHandler(1)
				.systemRequestHandler(2)
				.build();
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
				.entryLoaderArgs(new Object[] {componentManager})
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
		EzySimpleMaxRequestPerSecond maxRequestPerSecondSetting = new EzyMaxRequestPerSecondBuilder()
				.value(Integer.MAX_VALUE)
				.build();
		EzySimpleSessionManagementSetting sessionManagementSetting = new EzySessionManagementSettingBuilder()
				.sessionMaxRequestPerSecond(maxRequestPerSecondSetting)
				.build();
		EzySettings settings = new EzySettingsBuilder()
				.nodeName("rpc")
				.socket(socketSetting)
				.websocket(webSocketSetting)
				.admin(adminSetting)
				.zone(zoneSetting)
				.sessionManagement(sessionManagementSetting)
				.threadPoolSize(threadPoolSizeSetting)
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
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("eventHandlers: ").append(eventHandlers)
				.append("\nrequestHandlers: ").append(requestHandlers)
				.append("\nrequestIntercepters: ").append(requestInterceptors)
				.append("\nexceptionHandlers: ").append(exceptionHandlers)
				.toString();
	}
}
