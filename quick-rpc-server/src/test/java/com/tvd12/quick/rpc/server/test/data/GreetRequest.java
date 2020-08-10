package com.tvd12.quick.rpc.server.test.data;

import com.tvd12.ezyfox.binding.annotation.EzyObjectBinding;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EzyObjectBinding
public class GreetRequest {

	protected String who;
	
}
