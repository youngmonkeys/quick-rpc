package com.tvd12.quick.rpc.server.setting;

import com.tvd12.ezyfox.builder.EzyBuilder;

import lombok.Getter;

@Getter
public class QuickRpcSettings {

	protected final String username;
	protected final String password;

	protected QuickRpcSettings(Builder builder) {
		this.username = builder.username;
		this.password = builder.password;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements EzyBuilder<QuickRpcSettings> {
	
		protected String username;
		protected String password;
	
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
