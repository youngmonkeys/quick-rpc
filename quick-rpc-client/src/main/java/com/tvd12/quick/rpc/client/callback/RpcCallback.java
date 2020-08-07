package com.tvd12.quick.rpc.client.callback;

public interface RpcCallback<R,E> {

	default void onSuccess(R response) {}
	
	default void onError(E error) {}
	
	default void onFailed(Exception e) {}
	
}
