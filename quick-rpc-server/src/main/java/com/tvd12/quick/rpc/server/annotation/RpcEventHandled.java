package com.tvd12.quick.rpc.server.annotation;

import com.tvd12.quick.rpc.server.event.RpcEventType;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcEventHandled {

    RpcEventType value();
}
