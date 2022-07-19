package com.tvd12.quick.rpc.server.reflect;

import com.tvd12.ezyfox.core.annotation.EzyTryCatch;
import com.tvd12.ezyfox.core.util.EzyTryCatchAnnotations;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.RpcTryCatch;
import lombok.Getter;

@Getter
public class RpcExceptionHandlerMethod extends RpcHandlerMethod {

    protected final Class<?>[] exceptionClasses;

    public RpcExceptionHandlerMethod(EzyMethod method) {
        super(method);
        this.exceptionClasses = fetchExceptionClasses();

    }

    public static boolean isExceptionHandlerMethod(EzyMethod method) {
        return method.isAnnotated(EzyTryCatch.class)
            || method.isAnnotated(RpcTryCatch.class);
    }

    protected Class<?>[] fetchExceptionClasses() {
        EzyTryCatch tryCatchAnnotation = method.getAnnotation(EzyTryCatch.class);
        if (tryCatchAnnotation != null) {
            return EzyTryCatchAnnotations.getExceptionClasses(tryCatchAnnotation);
        }
        return method.getAnnotation(RpcTryCatch.class).value();
    }
}
