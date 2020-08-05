package com.tvd12.quick.rpc.server.transport;

import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.exception.BadRequestException;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.app.EzyAppRequestController;
import com.tvd12.ezyfoxserver.command.EzyAppResponse;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.entity.EzySession;
import com.tvd12.ezyfoxserver.event.EzyUserRequestAppEvent;
import com.tvd12.quick.rpc.server.RpcServerContext;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandlers;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

public class RpcAppRequestController
		extends EzyLoggable
		implements EzyAppRequestController {

	protected final EzyUnmarshaller unmarshaller;
	protected final RpcServerContext serverContext;
	protected final RpcSessionManager sessionManager;
	protected final RpcRequestHandlers requestHandlers;
	protected final RpcComponentManager componentManager;
	protected final Map<String, SystemRequestHandler> systemRequestHandlers;
	
	public RpcAppRequestController() {
		this.componentManager = RpcComponentManager.getInstance();
		this.unmarshaller = componentManager.getComponent(EzyUnmarshaller.class);
		this.serverContext = componentManager.getComponent(RpcServerContext.class);
		this.sessionManager = serverContext.getSessionManager();
		this.requestHandlers = serverContext.getRequestHandlers();
		this.systemRequestHandlers = defaultSystemRequestHandlers();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void handle(EzyAppContext ctx, EzyUserRequestAppEvent event) {
		EzyArray data = event.getData();
		String cmd = data.get(0, String.class);
		if(cmd == null) {
			logger.debug("rpc with no command, ignore it");
			return;
		}
		SystemRequestHandler systemRequestHandler = systemRequestHandlers.get(cmd);
		if(systemRequestHandler != null) {
			systemRequestHandler.handle(ctx, event.getSession());
			return;
		}
		String requestId = data.get(1, String.class);
		if(requestId == null) {
			logger.debug("rpc command: {} with no request id, ignore it", cmd);
			return;
		}
		RpcSession session = sessionManager.getSession(event.getSession());
		if(session == null) {
			logger.debug("has no rpc session map to: {}", event.getSession());
			return;
		}
		RpcRequestHandler requestHandler = requestHandlers.getHandler(cmd);
		if(requestHandler == null) {
			logger.debug("has handler for command: {}", cmd);
			return;
		}
		Object requestDataRaw = data.get(2);
		Object requestData = requestHandler.newData();
		if(requestData == null) {
			requestData = requestDataRaw;
		}
		else {
			unmarshaller.unwrap(requestDataRaw, requestData);
		}
		try {
			requestHandler.handle(session, requestId, requestData);
		}
		catch (BadRequestException e) {
			EzyArray responseData = EzyEntityFactory.newArray();
			responseData.add(e.getCode(), e.getMessage());
			ctx.cmd(EzyAppResponse.class)
				.command("$e")
				.params(responseData)
				.session(event.getSession())
				.execute();;
			logger.trace("bad request command: {} with data: {} error", cmd, requestData);
		}
		catch (Exception e) {
			logger.warn("handle command: {} with data: {} error", cmd, requestData);
		}
	}
	
	private Map<String, SystemRequestHandler> defaultSystemRequestHandlers() {
		Map<String, SystemRequestHandler> handlers = new HashMap<>();
		handlers.put("$c", (ctx, ss) -> {
			RpcSession session = new RpcSession(ss);
			sessionManager.addSession(session);
			ctx.cmd(EzyAppResponse.class)
				.command("$c")
				.session(ss)
				.execute();
		});
		return handlers;
	}
	
	private static interface SystemRequestHandler {
		
		void handle(EzyAppContext ctx, EzySession session);
		
	}

}
