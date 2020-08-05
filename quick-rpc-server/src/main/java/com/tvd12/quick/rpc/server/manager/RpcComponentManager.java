package com.tvd12.quick.rpc.server.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RpcComponentManager {

	private final Map<Object, Object> components;
	private final static RpcComponentManager INSTANCE = new RpcComponentManager();
	
	private RpcComponentManager() {
		this.components = new ConcurrentHashMap<>();
	}
	
	public static RpcComponentManager getInstance() {
		return INSTANCE;
	}
	
	public void addComponent(Object key, Object component) {
		this.components.put(key, component);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> type) {
		return (T)components.get(type);
	}
	
}
