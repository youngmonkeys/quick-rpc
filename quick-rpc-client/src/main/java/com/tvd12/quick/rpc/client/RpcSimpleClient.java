package com.tvd12.quick.rpc.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.tvd12.ezyfox.concurrent.EzyCallableFutureTask;
import com.tvd12.ezyfox.concurrent.EzyFuture;
import com.tvd12.ezyfox.concurrent.EzyFutureConcurrentHashMap;
import com.tvd12.ezyfox.concurrent.EzyFutureMap;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.entity.EzyPlugin;

public class RpcSimpleClient extends EzyLoggable implements RpcClient {

	protected EzyPlugin rpcPlugin;
	protected final EzyFutureMap<String> futureMap;
	protected final BlockingQueue<RpcRequest> requestQueue;
	
	public RpcSimpleClient(int capacity) {
		this.requestQueue = new LinkedBlockingQueue<>(capacity);
		this.futureMap = new EzyFutureConcurrentHashMap<>();
	}
	
	@Override
	public void fire(RpcRequest request) {
		rpcPlugin.send(request.getRequestType(), request.getData());
	}
	
	@Override
	public <T> T call(RpcRequest request, Class<T> returnType) throws Exception {
		EzyFuture future = futureMap.addFuture(request.getRequestType());
		requestQueue.offer(request);
		return future.get();
	}
	
	@Override
	public void execute(RpcRequest request, RpcCallback callback) {
		EzyCallableFutureTask future = new EzyCallableFutureTask();
		requestQueue.offer(request);
	}
	
}
