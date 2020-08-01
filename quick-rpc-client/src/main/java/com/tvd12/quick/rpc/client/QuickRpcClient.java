package com.tvd12.quick.rpc.client;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyCloseable;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.EzyClients;
import com.tvd12.ezyfoxserver.client.EzyTcpClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;

public class QuickRpcClient extends EzyLoggable implements EzyCloseable {

	protected final String name;
	protected final String type;
	protected final String username;
	protected final String password;
	protected final String host;
	protected final int port;
	protected volatile boolean active;
	protected EzyClient transporter;

	protected QuickRpcClient(Builder builder) {
		this.name = builder.name;
		this.type = builder.type;
		this.username = builder.username;
		this.password = builder.password;
		this.host = builder.host;
		this.port = builder.port;
		this.active = true;
		this.connect();
	}
	
	@Override
	public void close() {
		this.active = false;
		this.transporter.disconnect(EzyDisconnectReason.UNKNOWN.getId());
	}
	
	protected void connect() {
		EzyClientConfig clientConfig = EzyClientConfig.builder()
				.clientName(name)
				.zoneName("rpc")
				.reconnectConfigBuilder()
					.maxReconnectCount(Integer.MAX_VALUE)
					.done()
				.build();
		transporter = new EzyTcpClient(clientConfig);
		EzyClients.getInstance().addClient(transporter);
		Thread thread = new Thread(() -> {
			while(active) {
				try {
					Thread.sleep(3);
					transporter.processEvents();
				}
				catch (Exception e) {
					logger.warn("client: {} process events error", e);
				}
			}
		});
		thread.setName("quick-rpc-client-" + name);
		thread.start();
		transporter.connect(host, port);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements EzyBuilder<QuickRpcClient> {
	
		protected String name = "default";
		protected String type = "socket";
		protected String username = "admin";
		protected String password = "admin";
		protected String host = "127.0.0.1";
		protected int port = 3005;
	
		public Builder name(String name) {
			this.name = name;
			return this;
		}
	
		public Builder type(String type) {
			this.type = type;
			return this;
		}
	
		public Builder username(String username) {
			this.username = username;
			return this;
		}
	
		public Builder password(String password) {
			this.password = password;
			return this;
		}
	
		public Builder host(String host) {
			this.host = host;
			return this;
		}
	
		public Builder port(int port) {
			this.port = port;
			return this;
		}
	
		@Override
		public QuickRpcClient build() {
			return new QuickRpcClient(this);
		}
	
	}

}

