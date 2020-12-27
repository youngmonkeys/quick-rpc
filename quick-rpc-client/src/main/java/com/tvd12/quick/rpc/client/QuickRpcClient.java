package com.tvd12.quick.rpc.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.tvd12.ezyfox.binding.EzyBindingContext;
import com.tvd12.ezyfox.binding.EzyBindingContextBuilder;
import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.concurrent.EzyFuture;
import com.tvd12.ezyfox.concurrent.EzyFutureConcurrentHashMap;
import com.tvd12.ezyfox.concurrent.EzyFutureMap;
import com.tvd12.ezyfox.concurrent.EzyFutureTask;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfox.reflect.EzyReflection;
import com.tvd12.ezyfox.reflect.EzyReflectionProxy;
import com.tvd12.ezyfox.util.EzyCloseable;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.EzyTcpClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.handler.EzyAppAccessHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyHandshakeHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyLoginErrorHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyLoginSuccessHandler;
import com.tvd12.ezyfoxserver.client.request.EzyAppAccessRequest;
import com.tvd12.ezyfoxserver.client.request.EzyLoginRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.quick.rpc.client.constant.RpcClientType;
import com.tvd12.quick.rpc.client.entity.RpcRequest;
import com.tvd12.quick.rpc.client.entity.RpcResponse;
import com.tvd12.quick.rpc.client.exception.RpcClientLoginFailureException;
import com.tvd12.quick.rpc.client.exception.RpcClientMaxCapacityException;
import com.tvd12.quick.rpc.client.exception.RpcClientNotConnectedException;
import com.tvd12.quick.rpc.client.exception.RpcErrorException;
import com.tvd12.quick.rpc.client.net.RpcSocketAddress;
import com.tvd12.quick.rpc.core.constant.RpcInternalCommands;
import com.tvd12.quick.rpc.core.util.RpcRequestDataClasses;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QuickRpcClient extends EzyLoggable implements EzyCloseable {

	protected final String name;
	protected final int capacity;
	protected final String username;
	protected final String password;
	protected volatile boolean active;
	protected final int threadPoolSize;
	protected final int processEventInterval;
	protected final RpcClientType clientType;
	protected final AtomicInteger remainRequest;
	protected final EzyMarshaller marshaller;
	protected final EzyUnmarshaller unmarshaller;
	protected final EzyBindingContext bindingContext;
	protected final Map<Class, String> commands;
	protected final Consumer<EzyArray> messageSender;
	protected final LinkedList<EzyClient> transporters;
	protected final RpcRequestIdGenerator requestIdGenerator;
	protected final Map<String, EzyFutureMap<String>> futureMap;
	protected final LinkedList<RpcSocketAddress> serverAddresses;

	protected QuickRpcClient(Builder builder) {
		this.name = builder.name;
		this.username = builder.username;
		this.password = builder.password;
		this.active = true;
		this.capacity = builder.capacity;
		this.clientType = builder.clientType;
		this.threadPoolSize = builder.threadPoolSize;
		this.processEventInterval = builder.processEventInterval;
		this.futureMap = new ConcurrentHashMap<>();
		this.remainRequest = new AtomicInteger();
		this.transporters = new LinkedList<>();
		this.bindingContext = builder.bindingContext;
		this.marshaller = bindingContext.newMarshaller();
		this.unmarshaller = bindingContext.newUnmarshaller();
		this.commands = new ConcurrentHashMap<>(builder.commands);
		this.serverAddresses = new LinkedList<>(builder.serverAddresses);
		this.requestIdGenerator = new RpcRequestIdGenerator();
		this.connect();
		this.messageSender = newMessageSender();
	}

	public void fire(RpcRequest request) {
		String command = getRequestCommand(request);
		internalFire(command, getRequestId(request, command), request.getData());
	}
	
	public <T> T call(
			Object requestData, 
			Class<T> responseDataType) throws Exception {
		return call(new RpcRequest(requestData), responseDataType, -1);
	}
	
	public <T> T call(
			RpcRequest request, 
			Class<T> responseDataType) throws Exception {
		return call(request, responseDataType, -1);
	}
	
	public <T> T call(
			Object requestData, 
			Class<T> responseDataType, int timeout) throws Exception {
		return call(new RpcRequest(requestData), responseDataType, timeout);
	}
	
	public <T> T call(
			RpcRequest request, 
			Class<T> responseDataType, int timeout) throws Exception {
		RpcResponse response = call(request, timeout);
		if(response.isError()) {
			throw new RpcErrorException(
					response.getCommand(), 
					response.getRequestId(), response);
		}
		return response.getData(responseDataType);
	}
	
	public RpcResponse call(Object requestData) throws Exception {
		return call(new RpcRequest(requestData));
	}
	
	public RpcResponse call(RpcRequest request) throws Exception {
		return call(request, -1);
	}
	
	public RpcResponse call(Object requestData, int timeout) throws Exception {
		return call(new RpcRequest(requestData), timeout);
	}
	
	public RpcResponse call(RpcRequest request, int timeout) throws Exception {
		if(remainRequest.get() >= capacity)
			throw new RpcClientMaxCapacityException(capacity);
		String command = getRequestCommand(request);
		EzyFutureMap<String> futures = getFutures(command);
		String requestId = getRequestId(request, command);
		EzyFuture future = futures.addFuture(requestId);
		internalFire(command, requestId, request.getData());
		RpcResponse response;
		try {
			response = future.get(timeout);
		}
		catch (TimeoutException e) {
			futures.removeFuture(command);
			throw e;
		}
		return response;
	}
	
	public Future<RpcResponse> submit(Object requestData) {
		return submit(new RpcRequest(requestData));
	}
	
	public Future<RpcResponse> submit(RpcRequest request) {
		if(remainRequest.get() >= capacity)
			throw new RpcClientMaxCapacityException(capacity);
		String command = getRequestCommand(request);
		String requestId = getRequestId(request, command);
		EzyFuture future = internalSubmit(command, requestId, request.getData());
		return new FutureTask<RpcResponse>(new Callable<RpcResponse>() {
			@Override
			public RpcResponse call() throws Exception {
				return future.get();
			}
		}) {
			@Override
			public RpcResponse get(long timeout, TimeUnit unit)
			        throws InterruptedException, ExecutionException, TimeoutException {
				try {
					return super.get(timeout, unit);
				}
				catch (TimeoutException e) {
					EzyFutureMap<String> futures = getFutures(command);
					futures.removeFuture(requestId);
					throw e;
				}
			}
		};
	}
	
	public <T> Future<T> submit(Object requestData, Class<T> responseDataType) {
		return submit(new RpcRequest(requestData), responseDataType);
	}
	
	public <T> Future<T> submit(RpcRequest request, Class<T> responseDataType) {
		if(remainRequest.get() >= capacity)
			throw new RpcClientMaxCapacityException(capacity);
		String command = getRequestCommand(request);
		String requestId = getRequestId(request, command);
		EzyFuture future = internalSubmit(command, requestId, request.getData());
		return new FutureTask<T>(new Callable<T>() {
			@Override
			public T call() throws Exception {
				RpcResponse response = future.get();
				if(response.isError()) {
					throw new RpcErrorException(
							response.getCommand(), 
							response.getRequestId(), response);
				}
				return response.getData(responseDataType);
			}
		}) {
			@Override
			public T get(long timeout, TimeUnit unit)
			        throws InterruptedException, ExecutionException, TimeoutException {
				try {
					return super.get(timeout, unit);
				}
				catch (TimeoutException e) {
					EzyFutureMap<String> futures = getFutures(command);
					futures.removeFuture(requestId);
					throw e;
				}
			}
		};
	}
	
	protected EzyFuture internalSubmit(
			String command, String requestId, Object requestData) {
		EzyFutureMap<String> futures = getFutures(command);
		EzyFuture future = futures.addFuture(requestId);
		internalFire(command, requestId, requestData);
		return future;
	}
	
	protected void internalFire(
			String command, String requestId, Object requestData) {
		EzyArray commandData = EzyEntityFactory.newArray();
		Object mdata = marshaller.marshal(requestData);
		commandData.add(command, requestId, mdata);
		EzyArray sdata = EzyEntityFactory.newArray();
		sdata.add(RpcInternalCommands.REQUEST, commandData);
		messageSender.accept(sdata);
	}
	
	private String getRequestCommand(RpcRequest request) {
		String command = request.getCommand();
		if(command == null)
			command = commands.get(request.getDataType());
		if(command == null) {
			command = commands.computeIfAbsent(
					request.getDataType(), 
					k -> RpcRequestDataClasses.getCommand(request.getDataType()));
		}
		return command;
	}
	
	private String getRequestId(RpcRequest request, String command) {
		String requestId = request.getId();
		if(requestId == null) {
			requestId = requestIdGenerator.generate(command);
		}
		return requestId;
	}
	
	private EzyFutureMap<String> getFutures(String command) {
		return futureMap.computeIfAbsent(
				command, k -> new EzyFutureConcurrentHashMap<>());
	}
	
	@Override
	public void close() {
		this.active = false;
		for(EzyClient transporter : transporters)
			transporter.close();
	}
	
	private Consumer<EzyArray> newMessageSender() {
		if(clientType == RpcClientType.SINGLE) {
			return new Consumer<EzyArray>() {
				@Override
				public void accept(EzyArray m) {
					boolean sent = false;
					for(EzyClient transporter : transporters) {
						if(transporter.isConnected()) {
							EzyApp app = transporter.getApp();
							if(app != null) { 
								app.send(m);
								sent = true;
							}
						}
					}
					if(!sent)
						throw new RpcClientNotConnectedException();
					
				}
			};
		}
		return new Consumer<EzyArray>() {
			@Override
			public void accept(EzyArray m) {
				EzyApp app = null;
				synchronized (transporters) {
					EzyClient transporter = transporters.poll();
					transporters.offer(transporter);
					EzyClient firstTransporter =  transporter;
					while(true) {
						if(transporter.isConnected()) {
							app = transporter.getApp();
						}
						if(app != null)
							break;
						transporter = transporters.poll();
						transporters.offer(transporter);
						if(transporter == firstTransporter)
							break;
					}
				}
				if(app == null)
					throw new RpcClientNotConnectedException();
				app.send(m);
			}
		};
	}
	
	private void connect() {
		EzyFuture connectFuture = new EzyFutureTask();
		for(int i = 0 ; i < serverAddresses.size() ; ++i) {
			EzyClient transporter = newTransporter(i, connectFuture);
			transporters.add(transporter);
		}
		Thread thread = new Thread(() -> {
			while(active) {
				try {
					Thread.sleep(processEventInterval);
					for(EzyClient transporter : transporters)
						transporter.processEvents();
				}
				catch (Exception e) {
					logger.warn("client: {} process events error", e);
				}
			}
		});
		thread.setName("qkrpc-client-" + name + "-socket-loop");
		thread.start();
		for(int i = 0 ; i < serverAddresses.size() ; ++i) {
			EzyClient transporter = transporters.get(i);
			RpcSocketAddress serverAddress = serverAddresses.get(i);
			transporter.connect(serverAddress.getHost(), serverAddress.getPort());
		}
		try {
			connectFuture.get();
		}
		catch (Exception e) {
			throw new IllegalStateException("can't connect to: " + serverAddresses);
		}
	}
	
	private EzyClient newTransporter(int index, EzyFuture connectFuture) {
		EzyClientConfig clientConfig = EzyClientConfig.builder()
				.clientName(name + "-" + index)
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
					app.send(RpcInternalCommands.CONFIRM_CONNECTED, EzyEntityFactory.EMPTY_ARRAY);
				}
			});
		transporter.setup().setupApp("rpc")
			.addDataHandler(RpcInternalCommands.CONFIRM_CONNECTED, new EzyAppDataHandler<EzyData>() {
				@Override
				public void handle(EzyApp app, EzyData data) {
					connectFuture.setResult(Boolean.TRUE);
				}
			})
			.addDataHandler(RpcInternalCommands.RESPONSE, new EzyAppDataHandler<EzyArray>() {
				@Override
				public void handle(EzyApp app, EzyArray data) {
					String cmd = data.get(0, String.class);
					EzyFutureMap<String> futures = futureMap.get(cmd);
					if(futures == null) {
						logger.warn("has no future map to command: {}", cmd);
						return;
					}
					String id = data.get(1, String.class);
					EzyFuture future = futures.removeFuture(id);
					if(future == null) {
						logger.warn("has no future map to command: {} and id: {}", cmd, id);
						return;
					}
					EzyData result = data.get(2, EzyData.class);
					future.setResult(new RpcResponse(unmarshaller, cmd, id, result, false));
				}
			})
			.addDataHandler(RpcInternalCommands.ERROR, new EzyAppDataHandler<EzyArray>() {
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
					future.setResult(new RpcResponse(unmarshaller, cmd, id, error, true));
				}
			});
		return transporter;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements EzyBuilder<QuickRpcClient> {
	
		protected int capacity = 10000;
		protected String name = "rpc-client";
		protected String username = "admin";
		protected String password = "admin";
		protected int threadPoolSize = 8;
		protected int processEventInterval = 3;
		protected EzyBindingContext bindingContext;
		protected Set<String> packagesToScan = new HashSet<>();
		protected Map<Class, String> commands = new HashMap<>();
		protected RpcClientType clientType = RpcClientType.SINGLE;
		protected LinkedList<RpcSocketAddress> serverAddresses = new LinkedList<>();
	
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
		
		public Builder connectTo(String host, int port) {
			serverAddresses.addFirst(new RpcSocketAddress(host, port));
			return this;
		}
	
		public Builder threadPoolSize(int threadPoolSize) {
			this.threadPoolSize = threadPoolSize;
			return this;
		}
		
		public Builder processEventInterval(int processEventInterval) {
			this.processEventInterval = processEventInterval;
			return this;
		}
		
		public Builder clientType(RpcClientType clientType) {
			this.clientType = clientType;
			return this;
		}
		
		public Builder scan(String packageToScan) {
			this.packagesToScan.add(packageToScan);
			return this;
		}
		
		public Builder scan(String... packagesToScan) {
			return scan(Arrays.asList(packagesToScan));
		}
		
		public Builder scan(Iterable<String> packagesToScan) {
			for(String packageToScan : packagesToScan)
				scan(packageToScan);
			return this;
		}
		
		public Builder bindingContext(EzyBindingContext bindingContext) {
			this.bindingContext = bindingContext;
			return this;
		}
		
		@Override
		public QuickRpcClient build() {
			EzyReflection reflection = null;
			if(packagesToScan.size() > 0)
				reflection = new EzyReflectionProxy(packagesToScan);
			if(reflection != null) {
				Set<Class<?>> requestDataClasses = reflection.getAnnotatedClasses(
						com.tvd12.quick.rpc.core.annotation.RpcRequest.class);
				for(Class<?> cls : requestDataClasses)
					commands.put(cls, RpcRequestDataClasses.getCommand(cls));
			}
			if(bindingContext == null) {
				EzyBindingContextBuilder builder = EzyBindingContext.builder();
				if(reflection != null) {
					Set<Class<?>> requestDataClasses = reflection.getAnnotatedClasses(
							com.tvd12.quick.rpc.core.annotation.RpcRequest.class);
					Set<Class<?>> responseDataClasses = reflection.getAnnotatedClasses(
							com.tvd12.quick.rpc.core.annotation.RpcResponse.class);
					Set<Class<?>> errorDataClasses = reflection.getAnnotatedClasses(
							com.tvd12.quick.rpc.core.annotation.RpcError.class);
					builder.addClasses((Set)requestDataClasses);
					builder.addClasses((Set)responseDataClasses);
					builder.addClasses((Set)errorDataClasses);
					builder.addAllClasses(reflection);
				}
				bindingContext = builder.build();
			}
			if(serverAddresses.isEmpty())
				serverAddresses.add(new RpcSocketAddress("127.0.0.1", 3005));
			return new QuickRpcClient(this);
		}
	
	}
	
	private static class RpcRequestIdGenerator {
		
		private final Map<String, AtomicLong> incrementers = 
				new ConcurrentHashMap<>();
		
		public String generate(String requestCommand) {
			AtomicLong incrementer = incrementers.computeIfAbsent(
					requestCommand, k -> new AtomicLong());
			return Long.toString(incrementer.incrementAndGet(), Character.MAX_RADIX);
		}
		
	}

}

