package com.tvd12.quick.rpc.server.transport;

import java.util.HashMap;
import java.util.Map;

import com.tvd12.ezyfox.binding.EzyMarshaller;
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
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.exception.RpcHandleErrorException;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandler;
import com.tvd12.quick.rpc.server.handler.RpcRequestHandlers;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class RpcAppRequestController
		extends EzyLoggable
		implements EzyAppRequestController {

	protected final EzyMarshaller marshaller;
	protected final EzyUnmarshaller unmarshaller;
	protected final RpcSessionManager sessionManager;
	protected final RpcRequestHandlers requestHandlers;
	protected final RpcComponentManager componentManager;
	protected final Map<String, AppRequestHandler> appRequestHandlers;
	
	public RpcAppRequestController() {
		this.componentManager = RpcComponentManager.getInstance();
		this.marshaller = componentManager.getComponent(EzyMarshaller.class);
		this.unmarshaller = componentManager.getComponent(EzyUnmarshaller.class);
		this.sessionManager = componentManager.getComponent(RpcSessionManager.class);
		this.requestHandlers = componentManager.getComponent(RpcRequestHandlers.class);
		this.appRequestHandlers = defaultAppRequestHandlers();
	}
	
	@Override
	public void handle(EzyAppContext ctx, EzyUserRequestAppEvent event) {
		EzyArray data = event.getData();
		String cmd = data.get(0, String.class);
		if(cmd == null) {
			logger.debug("rpc with no command, ignore it");
			return;
		}
		AppRequestHandler appRequestHandler = appRequestHandlers.get(cmd);
		if(appRequestHandler == null) {
			logger.debug("has no handle map to rpc command: {}", cmd);
			return;
		}
		EzyArray requestData = data.get(1, EzyArray.class);
		appRequestHandler.handle(ctx, event.getSession(), requestData);
	}
	
	private Map<String, AppRequestHandler> defaultAppRequestHandlers() {
		Map<String, AppRequestHandler> handlers = new HashMap<>();
		handlers.put("$c", (ctx, ss, d) -> {
			RpcSession session = new RpcSession(ctx, ss, marshaller);
			sessionManager.addSession(session);
			ctx.cmd(EzyAppResponse.class)
				.command("$c")
				.session(ss)
				.execute();
		});
		handlers.put("$r", (ctx, ss, d) -> {
			String cmd = d.get(0, String.class);
			String requestId = d.get(1, String.class);
			if(requestId == null) {
				logger.debug("rpc command: {} with no request id, ignore it", cmd);
				return;
			}
			RpcSession session = sessionManager.getSession(ss);
			if(session == null) {
				logger.debug("has no rpc session map to: {}", ss);
				return;
			}
			RpcRequestHandler requestHandler = requestHandlers.getHandler(cmd);
			if(requestHandler == null) {
				logger.debug("has handler for command: {}", cmd);
				return;
			}
			Object requestDataRaw = d.get(2);
			Class requestDataType = requestHandler.getDataType();
			Object requestData = requestDataRaw;
			if(requestDataType != null) {
				requestData = unmarshaller.unmarshal(requestDataRaw, requestDataType);
			}
			RpcRequest rpcRequest = new RpcRequest<>(session, cmd, requestId, requestData);
			RpcResponse rpcResponse = new RpcResponse(rpcRequest);
			try {
				requestHandler.handle(rpcRequest, rpcResponse);
			}
			catch (BadRequestException e) {
				EzyArray responseData = EzyEntityFactory.newArray();
				responseData.add(e.getCode(), e.getMessage());
				EzyArray commandData = EzyEntityFactory.newArray();
				commandData.add(cmd, requestId, responseData);
				ctx.cmd(EzyAppResponse.class)
					.command("$e")
					.params(commandData)
					.session(ss)
					.execute();
				logger.trace("bad request command: {} with data: {} error", cmd, requestData);
			}
			catch (RpcHandleErrorException e) {
				EzyArray responseData = marshaller
						.marshal(((RpcHandleErrorException)e).getResponseData());
				EzyArray commandData = EzyEntityFactory.newArray();
				commandData.add(cmd, requestId, responseData);
				ctx.cmd(EzyAppResponse.class)
					.command("$e")
					.params(commandData)
					.session(ss)
					.execute();
				logger.trace("error when handle command: {} with data: {} error", cmd, requestData);
			}
			catch (Exception e) {
				logger.warn("handle command: {} with data: {} error", cmd, requestData);
			}
		});
		return handlers;
	}
	
	private static interface AppRequestHandler {
		
		void handle(EzyAppContext ctx, EzySession session, EzyArray data);
		
	}

}
