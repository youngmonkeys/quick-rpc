package com.tvd12.quick.rpc.client.request;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyEquals;
import com.tvd12.ezyfox.util.EzyHashCodes;

import lombok.Getter;

@Getter
public class RpcRequest {

	protected final String id;
	protected final Object data;
	protected final String command;
	
	public static final RpcRequest POISON = new RpcRequest(null, null, null);
	
	public RpcRequest(String command, String id, Object data) {
		this.id = id;
		this.data = data;
		this.command = command;
	}
	
	@Override
	public boolean equals(Object obj) {
		return new EzyEquals<RpcRequest>()
				.function(t -> t.command)
				.function(t -> t.id)
				.isEquals(this, obj);
	}
	
	@Override
	public int hashCode() {
		return new EzyHashCodes(2)
				.append(command)
				.append(id)
				.toHashCode();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("(")
					.append("command: ").append(command).append(", ")
					.append("id: ").append(id).append(", ")
					.append("data: ").append(data)
				.append(")")
				.toString();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<RpcRequest> {
		
		protected String id;
		protected Object data;
		protected String command;
	
		public Builder id(String id) {
			this.id = id;
			return this;
		}
	
		public Builder data(Object data) {
			this.data = data;
			return this;
		}
	
		public Builder command(String command) {
			this.command = command;
			return this;
		}
	
		@Override
		public RpcRequest build() {
			return new RpcRequest(command, id, data);
		}
	
	}
	
}
