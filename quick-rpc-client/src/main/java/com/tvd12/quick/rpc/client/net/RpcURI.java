package com.tvd12.quick.rpc.client.net;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import com.tvd12.ezyfox.collect.Sets;
import com.tvd12.ezyfox.io.EzyStrings;

public class RpcURI {

	protected final URI uri;
	
	protected final static Set<String> SCHEMES = 
			Collections.unmodifiableSet(
					Sets.newHashSet("quickrpc", "qkrpc")
			);
	
	public RpcURI(String str) {
		validateScheme(str);
		this.uri = URI.create(str);
	}
	
	public RpcSocketAddress getSocketAddress() {
		return new RpcSocketAddress(uri.getHost(), uri.getPort());
	}
	
	public String getUsername() {
		String userInfo = uri.getUserInfo();
		if(EzyStrings.isNoContent(userInfo))
			return null;
		return userInfo.split(":")[0];
	}
	
	public String getPassword() {
		String userInfo = uri.getUserInfo();
		if(EzyStrings.isNoContent(userInfo))
			return null;
		if(userInfo.contains(":"))
			return userInfo.split(":")[1];
		return null;
	}
	
	private static void validateScheme(String str) {
		for(String scheme : SCHEMES) {
			if(str.startsWith(scheme))
				return;
		}
		throw new IllegalArgumentException("uri must start with one of scheme: " + SCHEMES);
	}
	
}
