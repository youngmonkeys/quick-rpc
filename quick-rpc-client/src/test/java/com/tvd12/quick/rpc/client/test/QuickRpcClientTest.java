package com.tvd12.quick.rpc.client.test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.tvd12.quick.rpc.client.QuickRpcClient;
import com.tvd12.quick.rpc.client.entity.RpcRequest;
import com.tvd12.quick.rpc.client.test.data.GreetRequest;

public class QuickRpcClientTest {

	public static void main(String[] args) throws Exception {
		QuickRpcClient client = QuickRpcClient.builder()
				.scan("com.tvd12.quick.rpc.client.test.data")
				.build();
		System.out.println("connect success");
		RpcRequest request1 = RpcRequest.builder()
				.command("Greet")
				.id("1")
				.data(new GreetRequest("Dzung"))
				.build();
		System.out.println("response 1: " + client.call(request1));
//		Thread.sleep(20000);
		Thread[] threads = new Thread[1];
		RpcRequest[] requests = new RpcRequest[threads.length];
		for(int i = 0 ; i < threads.length ; ++i) {
			requests[i] = RpcRequest.builder()
					.command("Big/Hello")
					.id(UUID.randomUUID().toString())
					.data(new GreetRequest("Dzung"))
					.build();
		}
		CountDownLatch cd = new CountDownLatch(threads.length);
		for(int i = 0 ; i < threads.length ; ++i) {
			final int index = i;
			threads[i] = new Thread(() -> {
				try {
					client.call(requests[index]);
					cd.countDown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		long start = System.currentTimeMillis();
		for(int i = 0 ; i < threads.length ; ++i) {
			threads[i].start();
		}
		cd.await();
		long offset = System.currentTimeMillis() - start;
		System.out.println("elapsed: " + offset);
//		client.close();
	}
	
}
