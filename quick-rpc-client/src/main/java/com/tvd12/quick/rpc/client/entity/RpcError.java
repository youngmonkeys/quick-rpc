package com.tvd12.quick.rpc.client.entity;

import com.tvd12.ezyfox.entity.EzyData;

public interface RpcError {
	
	String getCommand();
	
	String getRequestId();
	
	EzyData getRawData();

	<T> T getData(Class<T> dataType);
	
}
