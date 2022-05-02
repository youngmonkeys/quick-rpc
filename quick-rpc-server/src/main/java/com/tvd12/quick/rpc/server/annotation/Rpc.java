package com.tvd12.quick.rpc.server.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Rpc {

    String value() default "";

    String command() default "";
}
