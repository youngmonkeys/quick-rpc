package com.tvd12.quick.rpc.client.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcResponseHandled {

    String value() default "";

    String command() default "";
}
