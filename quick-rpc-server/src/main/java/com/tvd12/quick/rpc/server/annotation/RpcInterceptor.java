package com.tvd12.quick.rpc.server.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcInterceptor {

    int priority() default 0;
}
