package com.tvd12.quick.rpc.client;

public interface RpcCallback {

	void onSuccess(Object response);
	
	void onError(Object error);
	
}
