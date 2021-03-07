package com.tvd12.quick.rpc.server.entity;

import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.entity.EzySession;
import com.tvd12.quick.rpc.core.constant.RpcInternalCommands;

public class RpcSession {

	protected final EzySession session;
	protected final EzyMarshaller marshaller;
	protected final EzyAppContext transporter;
	
	public RpcSession(
			EzyAppContext transporter,
			EzySession session, 
			EzyMarshaller marshaller) {
		this.transporter = transporter;
		this.session = session;
		this.marshaller = marshaller;
	}
	
	public void send(String cmd, String responseId, Object data) {
		internalSend(
				RpcInternalCommands.RESPONSE, 
				cmd, 
				responseId, 
				data
		);
	}
	
	public void sendError(String cmd, String responseId, Object data) {
		internalSend(
				RpcInternalCommands.ERROR, 
				cmd, 
				responseId, 
				data
		);
	}
	
	protected void internalSend(
			String internalCommand, 
			String cmd, String responseId, Object data) {
		Object responseData = marshaller.marshal(data);
		EzyArray commandData = EzyEntityFactory.newArray();
		commandData.add(cmd, responseId, responseData);
		EzyArray response = EzyEntityFactory.newArray();
		response.add(internalCommand, commandData);
		transporter.send(response, session);
	}
	
	public Object getKey() {
		return session;
	}
	
	public long getKeyId() {
		return session.getId();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(obj instanceof RpcSession)
			return session.equals(((RpcSession)obj).session);
		return false;
	}
	
	@Override
	public int hashCode() {
		return session.hashCode();
	}
	
	@Override
	public String toString() {
		return session.toString();
	}
	
}
