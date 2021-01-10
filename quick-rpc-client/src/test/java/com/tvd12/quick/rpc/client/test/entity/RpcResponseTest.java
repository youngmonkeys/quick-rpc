package com.tvd12.quick.rpc.client.test.entity;

import org.testng.annotations.Test;

import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.entity.EzyObject;
import com.tvd12.ezyfox.util.EzyEntityObjects;
import com.tvd12.quick.rpc.client.entity.RpcResponse;
import static org.mockito.Mockito.*;

import java.util.Random;

public class RpcResponseTest {

	@Test
	public void test() {
		// given
		EzyUnmarshaller unmarshaller = mock(EzyUnmarshaller.class);
		String command = "testCommand";
		String requestId = "testRequestId";
		EzyObject rawData = EzyEntityObjects.newObject("test", "test");
		boolean error = new Random().nextBoolean();
		
		// when
		RpcResponse response = new RpcResponse(
				unmarshaller, 
				command, 
				requestId, 
				rawData, 
				error
		);
		
		// then
		assert response.getCommand().equals(command);
		assert response.getRequestId().equals(requestId);
		assert response.getRawData() == rawData;
	}
	
}
