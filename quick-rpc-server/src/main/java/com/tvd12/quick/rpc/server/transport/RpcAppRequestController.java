package com.tvd12.quick.rpc.server.transport;

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
import com.tvd12.quick.rpc.core.constant.RpcInternalCommands;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.entity.RpcSession;
import com.tvd12.quick.rpc.server.exception.RpcHandleErrorException;
import com.tvd12.quick.rpc.server.handler.*;
import com.tvd12.quick.rpc.server.manager.RpcComponentManager;
import com.tvd12.quick.rpc.server.manager.RpcSessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RpcAppRequestController
    extends EzyLoggable
    implements EzyAppRequestController {

    protected final EzyMarshaller marshaller;
    protected final EzyUnmarshaller unmarshaller;
    protected final RpcSessionManager sessionManager;
    protected final RpcRequestHandlers requestHandlers;
    protected final RpcComponentManager componentManager;
    protected final List<Class<?>> handledExceptionClasses;
    protected final List<RpcRequestInterceptor> requestInterceptors;
    protected final Map<String, AppRequestHandler> appRequestHandlers;
    protected final Map<Class<?>, RpcUncaughtExceptionHandler> exceptionHandlers;

    public RpcAppRequestController(RpcComponentManager componentManager) {
        this.componentManager = componentManager;
        this.marshaller = componentManager.getComponent(EzyMarshaller.class);
        this.unmarshaller = componentManager.getComponent(EzyUnmarshaller.class);
        this.sessionManager = componentManager.getComponent(RpcSessionManager.class);
        this.requestHandlers = componentManager.getComponent(RpcRequestHandlers.class);
        this.requestInterceptors = componentManager.getComponent(RpcRequestInterceptors.class)
            .getInterceptors();
        this.exceptionHandlers = componentManager.getComponent(RpcExceptionHandlers.class)
            .getHandlers();
        this.handledExceptionClasses = componentManager.getComponent(RpcExceptionHandlers.class)
            .getExceptionClasses();
        this.appRequestHandlers = defaultAppRequestHandlers();
    }

    @Override
    public void handle(EzyAppContext ctx, EzyUserRequestAppEvent event) {
        EzyArray data = event.getData();
        String cmd = data.get(0, String.class);
        if (cmd == null) {
            logger.debug("rpc with no command, ignore it");
            return;
        }
        AppRequestHandler appRequestHandler = appRequestHandlers.get(cmd);
        if (appRequestHandler == null) {
            logger.debug("has no handle map to rpc command: {}", cmd);
            return;
        }
        EzyArray requestData = data.get(1, EzyArray.class);
        appRequestHandler.handle(ctx, event.getSession(), requestData);
    }

    @SuppressWarnings("MethodLength")
    private Map<String, AppRequestHandler> defaultAppRequestHandlers() {
        Map<String, AppRequestHandler> handlers = new HashMap<>();
        handlers.put(RpcInternalCommands.CONFIRM_CONNECTED, (ctx, ss, d) -> {
            RpcSession session = new RpcSession(ctx, ss, marshaller);
            sessionManager.addSession(session);
            ctx.cmd(EzyAppResponse.class)
                .command(RpcInternalCommands.CONFIRM_CONNECTED)
                .session(ss)
                .execute();
        });
        handlers.put(RpcInternalCommands.REQUEST, (ctx, ss, d) -> {
            String cmd = d.get(0, String.class);
            String requestId = d.get(1, String.class);
            if (requestId == null) {
                logger.debug("rpc command: {} with no request id, ignore it", cmd);
                return;
            }
            RpcSession session = sessionManager.getSession(ss);
            if (session == null) {
                logger.debug("has no rpc session map to: {}", ss);
                return;
            }
            RpcRequestHandler requestHandler = requestHandlers.getHandler(cmd);
            if (requestHandler == null) {
                logger.debug("has handler for command: {}", cmd);
                return;
            }
            Object requestDataRaw = d.get(2);
            Class requestDataType = requestHandler.getDataType();
            Object requestData = requestDataRaw;
            if (requestDataType != null) {
                requestData = unmarshaller.unmarshal(requestDataRaw, requestDataType);
            }
            RpcRequest rpcRequest = new RpcRequest<>(session, cmd, requestId, requestData);
            RpcResponse rpcResponse = new RpcResponse(rpcRequest);
            try {
                preHandle(rpcRequest, rpcResponse);
                requestHandler.handle(rpcRequest, rpcResponse);
                postHandle(rpcRequest, rpcResponse);
            } catch (Exception e) {
                postHandle(rpcRequest, rpcResponse, e);
                if (e instanceof BadRequestException) {
                    BadRequestException ex = (BadRequestException) e;
                    EzyArray responseData = EzyEntityFactory.newArray();
                    responseData.add(ex.getCode(), ex.getMessage());
                    EzyArray commandData = EzyEntityFactory.newArray();
                    commandData.add(cmd, requestId, responseData);
                    ctx.cmd(EzyAppResponse.class)
                        .command(RpcInternalCommands.ERROR)
                        .params(commandData)
                        .session(ss)
                        .execute();
                    logger.debug("bad request command: {} with data: {} error", cmd, requestData, e);
                } else if (e instanceof RpcHandleErrorException) {
                    EzyArray responseData = marshaller
                        .marshal(((RpcHandleErrorException) e).getResponseData());
                    EzyArray commandData = EzyEntityFactory.newArray();
                    commandData.add(cmd, requestId, responseData);
                    ctx.cmd(EzyAppResponse.class)
                        .command(RpcInternalCommands.ERROR)
                        .params(commandData)
                        .session(ss)
                        .execute();
                    logger.debug("error when handle command: {} with data: {} error", cmd, requestData, e);
                } else {
                    Exception exception = e;
                    RpcUncaughtExceptionHandler exceptionHandler = getExceptionHandler(e.getClass());
                    if (exceptionHandler != null) {
                        try {
                            exceptionHandler.handleException(rpcRequest, rpcResponse, e);
                            exception = null;
                        } catch (Exception ex) {
                            exception = ex;
                        }
                    }
                    if (exception != null) {
                        logger.warn("handle command: {} with data: {} error", cmd, requestData, exception);
                    }
                }
            }
        });
        return handlers;
    }

    private void preHandle(RpcRequest request, RpcResponse response) {
        for (RpcRequestInterceptor interceptor : requestInterceptors) {
            interceptor.preHandle(request, response);
        }
    }

    private void postHandle(RpcRequest request, RpcResponse response) {
        for (RpcRequestInterceptor interceptor : requestInterceptors) {
            interceptor.postHandle(request, response);
        }
    }

    private void postHandle(RpcRequest request, RpcResponse response, Exception e) {
        for (RpcRequestInterceptor interceptor : requestInterceptors) {
            interceptor.postHandle(request, response, e);
        }
    }

    protected RpcUncaughtExceptionHandler getExceptionHandler(Class<?> exceptionClass) {
        for (Class<?> exc : handledExceptionClasses) {
            if (exc.isAssignableFrom(exceptionClass)) {
                return exceptionHandlers.get(exc);
            }
        }
        return null;
    }

    private interface AppRequestHandler {
        void handle(EzyAppContext ctx, EzySession session, EzyArray data);
    }
}
