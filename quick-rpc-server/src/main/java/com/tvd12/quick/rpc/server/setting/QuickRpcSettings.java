package com.tvd12.quick.rpc.server.setting;

import com.tvd12.ezyfox.builder.EzyBuilder;

import lombok.Getter;

@Getter
public class QuickRpcSettings {

	protected final String username;
	protected final String password;
	protected final String host;
	protected final int port;

	protected QuickRpcSettings(Builder builder) {
		this.host = builder.host;
		this.port = builder.port;
		this.username = builder.username;
		this.password = builder.password;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements EzyBuilder<QuickRpcSettings> {
	
		protected String host = "0.0.0.0";
		protected int port = 3005;
		protected String username = "admin";
		protected String password = "admin";
		
		public Builder host(String host) {
			this.host = host;
			return this;
		}
		
		public Builder port(int port) {
			this.port = port;
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
	
		@Override
		public QuickRpcSettings build() {
			return new QuickRpcSettings(this);
		}
	
	}

}
