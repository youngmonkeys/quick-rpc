package com.tvd12.quick.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.concurrent.EzyCallableFutureTask;
import com.tvd12.ezyfox.concurrent.EzyFuture;
import com.tvd12.ezyfox.concurrent.EzyFutureConcurrentHashMap;
import com.tvd12.ezyfox.concurrent.EzyFutureMap;
import com.tvd12.ezyfox.concurrent.EzyFutureTask;
import com.tvd12.ezyfox.concurrent.callback.EzyResultCallback;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.util.EzyCloseable;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.EzyClients;
import com.tvd12.ezyfoxserver.client.EzyTcpClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.handler.EzyAppAccessHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyHandshakeHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyLoginErrorHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyLoginSuccessHandler;
import com.tvd12.ezyfoxserver.client.request.EzyAppAccessRequest;
import com.tvd12.ezyfoxserver.client.request.EzyLoginRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.quick.rpc.client.callback.RpcCallback;
import com.tvd12.quick.rpc.client.exception.RpcClientLoginFailureException;
import com.tvd12.quick.rpc.client.exception.RpcClientMaxCapacityException;
import com.tvd12.quick.rpc.client.exception.RpcErrorException;
import com.tvd12.quick.rpc.client.request.RpcRequest;

public class QuickRpcClient extends EzyLoggable implements EzyCloseable {

	protected final String name;
	protected final int capacity;
	protected final String username;
	protected final String password;
	protected final String host;
	protected final int port;
	protected volatile boolean active;
	protected final int threadPoolSize;
	protected EzyClient transporter;
	protected EzyApp transporterApp;
	protected final AtomicInteger remainRequest;
	protected final Map<String, EzyFutureMap<String>> futureMap;

	protected QuickRpcClient(Builder builder) {
		this.name = builder.name;
		this.username = builder.username;
		this.password = builder.password;
		this.host = builder.host;
		this.port = builder.port;
		this.active = true;
		this.capacity = builder.capacity;
		this.threadPoolSize = builder.threadPoolSize;
		this.futureMap = new ConcurrentHashMap<>();
		this.transporter = this.connect();
		this.transporterApp = transporter.getApp();
		this.active = true;
		this.remainRequest = new AtomicInteger();
	}

	public void fire(RpcRequest request) {
		transporterApp.send(request.getCommand(), (EzyData) request.getData());
	}
	
	public <T> T call(RpcRequest request, Class<T> returnType) throws Exception {
		if(remainRequest.get() >= capacity)
			throw new RpcClientMaxCapacityException(capacity);
		EzyFutureMap<String> futures = getFutures(request.getCommand());
		EzyFuture future = futures.addFuture(request.getId());
		fire(request);
		return future.get();
	}
	
	public <R,E> void execute(RpcRequest request, RpcCallback<R,E> callback) {
		if(remainRequest.get() >= capacity)
			throw new RpcClientMaxCapacityException(capacity);
		EzyFutureMap<String> futures = getFutures(request.getCommand());
		futures.addFuture(request.getId(), new EzyCallableFutureTask(new EzyResultCallback<R>() {
			@Override
			public void onResponse(R response) {
				callback.onSuccess(response);
			}
			@SuppressWarnings("unchecked")
			@Override
			public void onException(Exception e) {
				if(e instanceof RpcErrorException)
					callback.onError((E) ((RpcErrorException) e).getData());
				else
					callback.onFailed(e);
				
			}
		}));
		fire(request);
	}
	
	private EzyFutureMap<String> getFutures(String requestCommand) {
		return futureMap.computeIfAbsent(
				requestCommand, k -> new EzyFutureConcurrentHashMap<>());
	}
	
	@Override
	public void close() {
		this.active = false;
		this.transporter.disconnect(EzyDisconnectReason.UNKNOWN.getId());
	}
	
	private EzyClient connect() {
		EzyFuture connectFuture = new EzyFutureTask();
		EzyClient transporter = newTransporter(connectFuture);
		Thread thread = new Thread(() -> {
			while(active) {
				try {
					Thread.sleep(3);
					transporter.processEvents();
				}
				catch (Exception e) {
					logger.warn("client: {} process events error", e);
				}
			}
		});
		thread.setName("qkrpc-client-" + name + "-socket-loop");
		thread.start();
		transporter.connect(host, port);
		try {
			connectFuture.get();
		}
		catch (Exception e) {
			throw new IllegalStateException("can't connect to: " + host + ":" + port, e);
		}
		return transporter;
	}
	
	private EzyClient newTransporter(EzyFuture connectFuture) {
		EzyClientConfig clientConfig = EzyClientConfig.builder()
				.clientName(name)
				.zoneName("rpc")
				.reconnectConfigBuilder()
					.maxReconnectCount(Integer.MAX_VALUE)
					.done()
				.build();
		EzyClient transporter = new EzyTcpClient(clientConfig);
		transporter.setup()
			.addDataHandler(EzyCommand.HANDSHAKE, new EzyHandshakeHandler() {
				@Override
				protected EzyRequest getLoginRequest() {
					return new EzyLoginRequest("rpc", username, password);
				}
			})
			.addDataHandler(EzyCommand.LOGIN, new EzyLoginSuccessHandler() {
				@Override
				protected void handleLoginSuccess(EzyData responseData) {
					client.send(new EzyAppAccessRequest("rpc"));
				}
			})
			.addDataHandler(EzyCommand.LOGIN_ERROR, new EzyLoginErrorHandler() {
				@Override
				protected void handleLoginError(EzyArray data) {
					connectFuture.setException(new RpcClientLoginFailureException(data.toString()));
				}
			})
			.addDataHandler(EzyCommand.APP_ACCESS, new EzyAppAccessHandler() {
				@Override
				protected void postHandle(EzyApp app, EzyArray data) {
					app.send("$c");
				}
			});
		transporter.setup().setupApp("rpc")
			.addDataHandler("$c", new EzyAppDataHandler<EzyData>() {
				@Override
				public void handle(EzyApp app, EzyData data) {
					connectFuture.setResult(Boolean.TRUE);
				}
			})
			.addDataHandler("$r", new EzyAppDataHandler<EzyArray>() {
				@Override
				public void handle(EzyApp app, EzyArray data) {
					String cmd = data.get(0, String.class);
					EzyFutureMap<String> futures = futureMap.get(cmd);
					if(futures == null) {
						logger.warn("has no future map to command: {}", cmd);
						return;
					}
					String id = data.get(1, String.class);
					EzyFuture future = futures.getFuture(id);
					if(future == null) {
						logger.warn("has no future map to command: {} and id: {}", cmd, id);
						return;
					}
					EzyData result = data.get(2, EzyData.class);
					future.setResult(result);
				}
			})
			.addDataHandler("$e", new EzyAppDataHandler<EzyArray>() {
				@Override
				public void handle(EzyApp app, EzyArray data) {
					String cmd = data.get(0, String.class);
					EzyFutureMap<String> futures = futureMap.get(cmd);
					if(futures == null) {
						logger.warn("has no future map to command: {}", cmd);
						return;
					}
					String id = data.get(1, String.class);
					EzyFuture future = futures.getFuture(id);
					if(future == null) {
						logger.warn("has no future map to command: {} and id: {}", cmd, id);
						return;
					}
					EzyData error = data.get(2, EzyData.class);
					future.setException(new RpcErrorException(cmd, id, error));
				}
			});
		EzyClients.getInstance().addClient(transporter);
		return transporter;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements EzyBuilder<QuickRpcClient> {
	
		protected int capacity;
		protected String name = "default";
		protected String username = "admin";
		protected String password = "admin";
		protected String host = "127.0.0.1";
		protected int port = 3005;
		protected int threadPoolSize = 8;
	
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder capacity(int capacity) {
			this.capacity = capacity;
			return this;
		}
	
		public Builder username(String username) {
			this.username = username;
			return this;
		}
	
		public Builder password(String password) {
			this.password = password;
			return this;
		}
	
		public Builder host(String host) {
			this.host = host;
			return this;
		}
	
		public Builder port(int port) {
			this.port = port;
			return this;
		}
		
		public Builder threadPoolSize(int threadPoolSize) {
			this.threadPoolSize = threadPoolSize;
			return this;
		}
	
		@Override
		public QuickRpcClient build() {
			return new QuickRpcClient(this);
		}
	
	}

}

