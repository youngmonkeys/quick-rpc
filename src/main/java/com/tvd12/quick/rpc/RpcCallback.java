package com.tvd12.quick.rpc;

public interface RpcCallback {

	void onSuccess(Object response);
	
	void onError(Object error);
	
}
