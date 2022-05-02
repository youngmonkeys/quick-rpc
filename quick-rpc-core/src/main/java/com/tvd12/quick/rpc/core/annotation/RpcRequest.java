package com.tvd12.quick.rpc.core.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcRequest {

    String value() default "";

    String command() default "";
}
