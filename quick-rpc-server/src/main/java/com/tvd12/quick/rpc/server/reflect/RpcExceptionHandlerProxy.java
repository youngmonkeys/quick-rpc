package com.tvd12.quick.rpc.server.reflect;

import com.tvd12.ezyfox.core.annotation.EzyTryCatch;
import com.tvd12.ezyfox.reflect.EzyClass;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.RpcTryCatch;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RpcExceptionHandlerProxy {

    protected final EzyClass clazz;
    protected final Object instance;
    protected final List<RpcExceptionHandlerMethod> exceptionHandlerMethods;

    public RpcExceptionHandlerProxy(Object instance) {
        this.instance = instance;
        this.clazz = new EzyClass(instance.getClass());
        this.exceptionHandlerMethods = fetchExceptionHandlerMethods();
    }

    public List<RpcExceptionHandlerMethod> fetchExceptionHandlerMethods() {
        List<RpcExceptionHandlerMethod> list = new ArrayList<>();
        List<EzyMethod> methods = clazz.getPublicMethods(m ->
            m.isAnnotated(EzyTryCatch.class) || m.isAnnotated(RpcTryCatch.class)
        );
        for (EzyMethod method : methods) {
            RpcExceptionHandlerMethod m = new RpcExceptionHandlerMethod(method);
            list.add(m);
        }
        return list;
    }

    public String getClassSimpleName() {
        return clazz.getClazz().getSimpleName();
    }

    @Override
    public String toString() {
        return clazz.getName() +
            "(\n" +
            "\tinstance: " + instance + ",\n" +
            "\texceptionHandlerMethods: " + exceptionHandlerMethods + "\n" +
            ")";
    }
}
