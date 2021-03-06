package com.tvd12.quick.rpc.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.tvd12.ezyfox.binding.EzyBindingContext;
import com.tvd12.ezyfox.binding.EzyBindingContextBuilder;
import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.binding.writer.EzyDefaultWriter;
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
import com.tvd12.quick.rpc.client.net.RpcURI;
import com.tvd12.quick.rpc.core.constant.RpcInternalCommands;
import com.tvd12.quick.rpc.core.data.RpcBadRequestErrorData;
import com.tvd12.quick.rpc.core.util.RpcPropertiesKeeper;
import com.tvd12.quick.rpc.core.util.RpcRequestDataClasses;

import lombok.AllArgsConstructor;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QuickRpcClient extends EzyLoggable implements EzyCloseable {

	protected final String name;
	protected final int capacity;
	protected final String username;
	protected final String password;
	protected volatile boolean active;
	protected final int threadPoolSize;
	protected final int processEventInterval;
	protected final int defaultRequestTimeout;
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
	
	public static final String HOST = "quickrpc.host";
	public static final String PORT = "quickrpc.port";
	public static final String URI = "quickrpc.uri";
	public static final String USERNAME = "quickrpc.username";
	public static final String PASSWORD = "quickrpc.password";

	protected QuickRpcClient(Builder builder) {
		this.name = builder.name;
		this.username = builder.username;
		this.password = builder.password;
		this.active = true;
		this.capacity = builder.capacity;
		this.clientType = builder.clientType;
		this.threadPoolSize = builder.threadPoolSize;
		this.processEventInterval = builder.processEventInterval;
		this.defaultRequestTimeout = builder.defaultRequestTimeout;
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

	public void fire(Object requestData) {
		fire(new RpcRequest(requestData));
	}
	
	public void fire(RpcRequest request) {
		String command = getRequestCommand(request);
		internalFire(command, getRequestId(request, command), request.getData());
	}
	
	public <T> T call(
			Object requestData, 
			Class<T> responseDataType) throws Exception {
		return call(new RpcRequest(requestData), responseDataType, defaultRequestTimeout);
	}
	
	public <T> T call(
			RpcRequest request, 
			Class<T> responseDataType) throws Exception {
		return call(request, responseDataType, defaultRequestTimeout);
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
		return call(request, defaultRequestTimeout);
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
		return new AbstractRpcFuture<RpcResponse>(future, requestId) {
			@Override
			protected EzyFutureMap getFutureMap() {
				return getFutures(command);
			}
			
			protected RpcResponse processResponse(RpcResponse response) throws ExecutionException {
				return response;
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
		return new AbstractRpcFuture<T>(future, requestId) {
			@Override
			protected EzyFutureMap getFutureMap() {
				return getFutures(command);
			}
			
			@Override
			protected T getResponseData(RpcResponse response) {
				return response.getData(responseDataType);
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
								break;
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
		EzyClient transporter = newSocketClient(clientConfig);
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
					Object result = data.get(2);
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
	
	protected EzyClient newSocketClient(EzyClientConfig clientConfig) {
		return new EzyTcpClient(clientConfig);
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder 
			extends RpcPropertiesKeeper<Builder> 
			implements EzyBuilder<QuickRpcClient> {
	
		protected int capacity = 10000;
		protected String name = "rpc-client";
		protected String username = "admin";
		protected String password = "admin";
		protected int threadPoolSize = 8;
		protected int processEventInterval = 3;
		protected int defaultRequestTimeout = 5000;
		protected EzyBindingContext bindingContext;
		protected EzyBindingContextBuilder bindingContextBuilder;
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
			if(username != null)
				this.username = username;
			return this;
		}
	
		public Builder password(String password) {
			if(password != null)
				this.password = password;
			return this;
		}
		
		public Builder connectTo(String uri) {
			if(uri != null) {
				RpcURI rpcUri = new RpcURI(uri);
				username(rpcUri.getUsername());
				password(rpcUri.getPassword());
				serverAddresses.add(rpcUri.getSocketAddress());
			}
			return this;
		}
		
		public Builder connectTo(String host, int port) {
			serverAddresses.addFirst(new RpcSocketAddress(host, port));
			return this;
		}
		
		public Builder connectTo(String host, String port) {
			if(host != null)
				connectTo(host, port != null ? Integer.parseInt(port) : 3005);
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
		
		public Builder defaultRequestTimeout(int defaultRequestTimeout) {
			this.defaultRequestTimeout = defaultRequestTimeout;
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
		
		public Builder bindingContextBuilder(EzyBindingContextBuilder bindingContextBuilder) {
			this.bindingContextBuilder = bindingContextBuilder;
			return this;
		}
		
		@Override
		public QuickRpcClient build() {
			if(serverAddresses.isEmpty())
				connectTo(properties.getProperty(URI));
			if(serverAddresses.isEmpty())
				connectTo(properties.getProperty(HOST), properties.getProperty(PORT));
			if(username == null)
				username(properties.getProperty(USERNAME));
			if(password == null)
				password(properties.getProperty(PASSWORD));
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
				if(bindingContextBuilder == null) {
					bindingContextBuilder = EzyBindingContext.builder()
						.addArrayBindingClass(RpcBadRequestErrorData.class)
						.addTemplate(BigDecimal.class, EzyDefaultWriter.getInstance())
						.addTemplate(BigInteger.class, EzyDefaultWriter.getInstance());
				}
				if(reflection != null) {
					Set<Class<?>> requestDataClasses = reflection.getAnnotatedClasses(
							com.tvd12.quick.rpc.core.annotation.RpcRequest.class);
					Set<Class<?>> responseDataClasses = reflection.getAnnotatedClasses(
							com.tvd12.quick.rpc.core.annotation.RpcResponse.class);
					Set<Class<?>> errorDataClasses = reflection.getAnnotatedClasses(
							com.tvd12.quick.rpc.core.annotation.RpcError.class);
					bindingContextBuilder.addClasses((Set)requestDataClasses);
					bindingContextBuilder.addClasses((Set)responseDataClasses);
					bindingContextBuilder.addClasses((Set)errorDataClasses);
					bindingContextBuilder.addAllClasses(reflection);
				}
				bindingContext = bindingContextBuilder.build();
			}
			if(serverAddresses.isEmpty())
				serverAddresses.add(new RpcSocketAddress("127.0.0.1", 3005));
			return newProduct();
		}
		
		protected QuickRpcClient newProduct() {
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

@AllArgsConstructor
abstract class AbstractRpcFuture<T> implements Future<T> {
	
	private final EzyFuture future;
	private final String requestId;
	
	@Override
	public T get() throws InterruptedException, ExecutionException {
		RpcResponse response;
		try {
			response = future.get();
		}
		catch(InterruptedException | ExecutionException e) {
			throw e;
		}
		catch(Exception e) {
			throw new ExecutionException(e);
		}
		return processResponse(response);
	}
	
	@Override
	public T get(long timeout, TimeUnit unit)
	        throws InterruptedException, ExecutionException, TimeoutException {
		RpcResponse response;
		try {
			response = future.get(TimeUnit.MILLISECONDS.convert(timeout, unit));
		}
		catch (TimeoutException e) {
			EzyFutureMap<String> futures = getFutureMap();
			futures.removeFuture(requestId);
			throw e;
		}
		catch(InterruptedException | ExecutionException e) {
			throw e;
		}
		catch(Exception e) {
			throw new ExecutionException(e);
		}
		return processResponse(response);
	}
	
	protected abstract EzyFutureMap<String> getFutureMap();
	
	protected T processResponse(RpcResponse response) throws ExecutionException {
		if(response.isError()) {
			RpcErrorException rpcException = new RpcErrorException(
					response.getCommand(), 
					response.getRequestId(), response);
			throw new ExecutionException(rpcException);
		}
		return getResponseData(response);
	}
	
	@SuppressWarnings("unchecked")
	protected T getResponseData(RpcResponse response) {
		return (T)response;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		future.cancel("cancel rpc call");
		return true;
	}

	@Override
	public boolean isCancelled() {
		future.cancel("cancel rpc call");
		return true;
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

}

