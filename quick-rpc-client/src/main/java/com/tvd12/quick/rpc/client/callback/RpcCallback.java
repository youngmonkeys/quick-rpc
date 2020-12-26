package com.tvd12.quick.rpc.client.callback;

import com.tvd12.quick.rpc.client.entity.RpcError;

public interface RpcCallback<R> {

	default void onSuccess(R response) {}
	
	default void onError(RpcError error) {}
	
	default void onFailed(Exception e) {}
	
}
