package com.tvd12.quick.rpc.server.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RpcTryCatch {

    Class<?>[] value();
}
