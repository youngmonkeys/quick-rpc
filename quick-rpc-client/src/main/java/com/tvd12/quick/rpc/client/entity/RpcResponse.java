package com.tvd12.quick.rpc.client.entity;

import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.entity.EzyData;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RpcResponse implements RpcError {

	@Getter(AccessLevel.NONE)
	protected final EzyUnmarshaller unmarshaller;
	protected final String command;
	protected final String requestId;
	protected final EzyData rawData;
	protected final boolean error;
	
	public <T> T getData(Class<T> dataType) {
		return unmarshaller.unmarshal(rawData, dataType);
	}
	
	@Override
	public <T> T getErrorData(Class<T> dataType) {
		return getData(dataType);
	}
	
}
