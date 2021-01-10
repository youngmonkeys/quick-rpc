package com.tvd12.quick.rpc.client.test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.testng.annotations.Test;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyObject;
import com.tvd12.ezyfox.util.EzyEntityArrays;
import com.tvd12.ezyfox.util.EzyEntityObjects;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.EzyTcpClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.quick.rpc.client.QuickRpcClient;
import com.tvd12.quick.rpc.client.constant.RpcClientType;
import com.tvd12.quick.rpc.client.exception.RpcClientMaxCapacityException;
import com.tvd12.quick.rpc.client.exception.RpcErrorException;
import com.tvd12.quick.rpc.client.test.data.GreetRequest;
import com.tvd12.quick.rpc.client.test.data.GreetResponse;
import com.tvd12.quick.rpc.core.constant.RpcInternalCommands;
import com.tvd12.quick.rpc.core.data.RpcBadRequestErrorData;

public class QuickRpcClientTest {

	private final String clientName = "testClientName";
	private final int clientCapacity = 100;
	private final String clientUsername = "testUsername";
	private final String clientPassword = "testPassword";
	private final String clientHost = "testHost";
	private final int clientPort = 3005;
	private final int clientThreadPoolSize = 3;
	private final int clientProcessEventInterval = 3;
	private final int clientDefaultRequestTimeout = 5000;
	
	@Test
	public void fire() {
		// given
		QuickRpcClient sut = makeQuickRpcClient();
		
		// when
		// then
		sut.fire(new GreetRequest("Dzung"));
	}
	
	@Test
	public void call() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient();
		
		// when
 		GreetResponse actual = sut.call(new GreetRequest("Dzung"), GreetResponse.class);
 		
		// then
 		assert actual.equals(new GreetResponse("Greet Dzung!"));
 		sut.close();
	}
	
	@Test
	public void callWithRoundRobin() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient(
				RpcClientType.ROUND_ROBIN,
				true
		);
		
		// when
 		GreetResponse actual = sut.call(new GreetRequest("Dzung"), GreetResponse.class);
 		
		// then
 		assert actual.equals(new GreetResponse("Greet Dzung!"));
	}
	
	@Test
	public void callError() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient(false);
		
		// when
		// then
		try {
			sut.call(new GreetRequest("Dzung"), GreetResponse.class);
		}
		catch (Exception e) {
			assert e instanceof RpcErrorException;
			RpcErrorException ex = (RpcErrorException)e;
			RpcBadRequestErrorData data = ex.getErrorData(RpcBadRequestErrorData.class);
			assert data.getCode() == 1;
			assert data.getMessage().equals("name too short");
		}
	}
	
	@Test
	public void submitGetResponse() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient();
		
		// when
 		GreetResponse actual = sut.submit(new GreetRequest("Dzung"))
 				.get()
 				.getData(GreetResponse.class);
 		
		// then
 		assert actual.equals(new GreetResponse("Greet Dzung!"));
	}
	
	@Test
	public void submitGetResponseError() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient(false);
		
		// when
		RpcBadRequestErrorData actual = sut.submit(new GreetRequest("Dzung"))
 				.get()
 				.getErrorData(RpcBadRequestErrorData.class);
 		
		// then
		assert actual.getCode() == 1;
		assert actual.getMessage().equals("name too short");
	}
	
	@Test
	public void submitGetObjectResult() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient();
		
		// when
 		GreetResponse actual = sut.submit(new GreetRequest("Dzung"), GreetResponse.class)
 				.get();
 		
		// then
 		assert actual.equals(new GreetResponse("Greet Dzung!"));
	}
	
	@Test
	public void submitGetObjectError() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient(false);
		
		// when
		// then
		try {
			sut.submit(new GreetRequest("Dzung"), GreetResponse.class)
				.get();
		}
		catch (Exception e) {
			assert e instanceof ExecutionException;
			RpcErrorException ex = (RpcErrorException) ((ExecutionException)e).getCause();
			RpcBadRequestErrorData data = ex.getErrorData(RpcBadRequestErrorData.class);
			assert data.getCode() == 1;
			assert data.getMessage().equals("name too short");
		}
	}
	
	@Test
	public void submitGetObjectResultWithTimeout() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClient();
		
		// when
 		GreetResponse actual = sut.submit(new GreetRequest("Dzung"), GreetResponse.class)
 				.get(1, TimeUnit.HOURS);
 		
		// then
 		assert actual.equals(new GreetResponse("Greet Dzung!"));
	}
	
	@Test(expectedExceptions = RpcClientMaxCapacityException.class)
	public void maxCapacityTest() throws Exception {
		// given
		QuickRpcClient sut = makeQuickRpcClientBuilder()
				.capacity(0)
				.build();
		
		// when
		// then
		sut.call(new GreetRequest("Dzung"));
	}
	
	private QuickRpcClient makeQuickRpcClient() {
		return makeQuickRpcClient(true);
	}
	
	private QuickRpcClient makeQuickRpcClient(boolean success) {
		return makeQuickRpcClient(RpcClientType.SINGLE, success);
	}
	
	private QuickRpcClient makeQuickRpcClient(
			RpcClientType clientType,
			boolean success) {
		return makeQuickRpcClientBuilder(clientType, success)
				.build();
	}
	
	private QuickRpcClient.Builder makeQuickRpcClientBuilder() {
		return makeQuickRpcClientBuilder(RpcClientType.SINGLE, true);
	}
	
	private QuickRpcClient.Builder makeQuickRpcClientBuilder(
			RpcClientType clientType,
			boolean success) {
		Function<EzyClientConfig, EzyClient> clientSupplier = config -> {
			return new EzyTcpClient(config) {
				@SuppressWarnings("unchecked")
				public void connect(String host, int port) {
					status = EzyConnectionStatus.CONNECTED;
					handlerManager
						.getDataHandler(EzyCommand.LOGIN)
						.handle(EzyEntityArrays.newArray(
								1, 
								"rpc",
								1L,
								"test",
								EzyEntityArrays.newArray()
						));
					handlerManager
						.getDataHandler(EzyCommand.APP_ACCESS)
						.handle(EzyEntityArrays.newArray(1, "rpc"));
					handlerManager
						.getAppDataHandlers("rpc")
						.getHandler(RpcInternalCommands.CONFIRM_CONNECTED)
						.handle(null, null);
				}
				
				@SuppressWarnings("unchecked")
				public void send(EzyCommand cmd, EzyArray data) {
					if(cmd != EzyCommand.APP_REQUEST)
						return;
					String rpcCommand = "greet";
					String requestId = "1";
					EzyArray rpcRequestData = data.get(1, EzyArray.class);
					String requestCommand = rpcRequestData.get(0, String.class);
					if(!requestCommand.equals(RpcInternalCommands.REQUEST))
						return;
					if(success) {
						String who = rpcRequestData
								.get(1, EzyArray.class)
								.get(2, EzyObject.class)
								.get("who", String.class);
						EzyApp app = getApp();
						handlerManager
							.getAppDataHandlers("rpc")
							.getHandler(RpcInternalCommands.RESPONSE)
							.handle(app, EzyEntityArrays.newArray(
									rpcCommand,
									requestId,
									EzyEntityObjects.newObject("message", "Greet " + who + "!"))
							);
					}
					else {
						EzyApp app = getApp();
						handlerManager
							.getAppDataHandlers("rpc")
							.getHandler(RpcInternalCommands.ERROR)
							.handle(app, EzyEntityArrays.newArray(
									rpcCommand,
									requestId,
									EzyEntityArrays.newArray(1, "name too short"))
							);
					}
				}
			};
		};
		return newQuickRpcClientBuilder(clientType, clientSupplier);
	}
	
	private QuickRpcClient.Builder newQuickRpcClientBuilder(
			RpcClientType clientType,
			Function<EzyClientConfig, EzyClient> clientSupplier) {
		return new QuickRpcClientForTest.Builder()
				.clientSupplier(clientSupplier)
				.clientType(clientType)
				.name(clientName)
				.capacity(clientCapacity)
				.username(clientUsername)
				.password(clientPassword)
				.connectTo(clientHost, clientPort)
				.threadPoolSize(clientThreadPoolSize)
				.processEventInterval(clientProcessEventInterval)
				.defaultRequestTimeout(clientDefaultRequestTimeout)
				.scan("com.tvd12.quick.rpc.client.test.data")
				.scan("com.tvd12.quick.rpc.client.test.data", "com.tvd12.quick.rpc.client.test.data")
				.scan(Arrays.asList("com.tvd12.quick.rpc.client.test.data"));
	}
	
	private static class QuickRpcClientForTest extends QuickRpcClient {
		
		protected QuickRpcClientForTest(Builder builder) {
			super(builder);
		}
		
		public static class Builder extends QuickRpcClient.Builder {
			
			private Function<EzyClientConfig, EzyClient> clientSupplier;
			
			public Builder clientSupplier(
					Function<EzyClientConfig, EzyClient> clientSupplier) {
				this.clientSupplier = clientSupplier;
				return this;
			}
			
			@Override
			public QuickRpcClient newProduct() {
				return new QuickRpcClientForTest(this) {
					@Override
					protected EzyClient newSocketClient(EzyClientConfig clientConfig) {
						return clientSupplier.apply(clientConfig);
					}
				};
			}
		}
	}
	
}
