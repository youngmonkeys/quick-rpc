package com.tvd12.quick.rpc.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tvd12.quick.rpc.server.event.RpcEventType;

/**
 * 
 * @author tavandung12
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface RpcEventHandled {
	
	RpcEventType value();
	
}
