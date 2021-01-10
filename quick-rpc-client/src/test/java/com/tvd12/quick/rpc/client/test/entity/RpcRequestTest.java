package com.tvd12.quick.rpc.client.test.entity;

import org.testng.annotations.Test;

import com.tvd12.quick.rpc.client.entity.RpcRequest;

public class RpcRequestTest {

	@Test
	public void createWithData() {
		// given
		Object data = new Object();
		
		// when
		RpcRequest request = new RpcRequest(data);
		
		// then
		assert request.getCommand() == null;
		assert request.getData() == data;
		assert request.getDataType() == Object.class;
		assert request.getId() == null;
	}
	
	@Test
	public void createWithDataAndCommand() {
		// given
		String command = "testRequestCommand";
		Object data = new Object();
		
		// when
		RpcRequest request = new RpcRequest(command, data);
		
		// then
		assert request.getCommand().equals(command);
		assert request.getData() == data;
		assert request.getDataType() == Object.class;
		assert request.getId() == null;
	}
	
	@Test
	public void createWithDataAndCommandAndId() {
		// given
		String command = "testRequestCommand";
		String id = "testRequestId";
		Object data = new Object();
		
		// when
		RpcRequest request = new RpcRequest(command, id, data);
		
		// then
		assert request.getCommand().equals(command);
		assert request.getData() == data;
		assert request.getDataType() == Object.class;
		assert request.getId().equals(id);
	}
	
	@Test
	public void createFromBuilder() {
		// given
		String command = "testRequestCommand";
		String id = "testRequestId";
		Object data = new Object();
		
		// when
		RpcRequest request = RpcRequest.builder()
				.command(command)
				.id(id)
				.data(data)
				.build();
		
		// then
		assert request.getCommand().equals(command);
		assert request.getData() == data;
		assert request.getDataType() == Object.class;
		assert request.getId().equals(id);
		System.out.println(request);
	}
	
}
