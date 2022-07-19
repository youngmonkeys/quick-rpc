package com.tvd12.quick.rpc.server.reflect;

import com.tvd12.ezyfox.reflect.EzyClass;
import com.tvd12.ezyfox.reflect.EzyMethod;
import com.tvd12.quick.rpc.server.annotation.Rpc;
import com.tvd12.quick.rpc.server.util.RpcControllerAnnotations;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RpcControllerProxy {

    protected final EzyClass clazz;
    protected final Object instance;
    protected final String commandGroup;
    protected final List<RpcRequestHandlerMethod> requestHandlerMethods;
    protected final List<RpcExceptionHandlerMethod> exceptionHandlerMethods;
    protected final Map<Class<?>, RpcExceptionHandlerMethod> exceptionHandlerMethodMap;

    public RpcControllerProxy(Object instance) {
        this.instance = instance;
        this.clazz = new EzyClass(instance.getClass());
        this.commandGroup = getCommandGroup();
        this.requestHandlerMethods = fetchRequestHandlerMethods();
        this.exceptionHandlerMethods = fetchExceptionHandlerMethods();
        this.exceptionHandlerMethodMap = fetchExceptionHandlerMethodMap();
    }

    protected String getCommandGroup() {
        return RpcControllerAnnotations.getGroup(clazz.getClazz());
    }

    protected List<RpcRequestHandlerMethod> fetchRequestHandlerMethods() {
        List<RpcRequestHandlerMethod> list = new ArrayList<>();
        List<EzyMethod> methods = clazz.getPublicMethods(this::isRequestHandlerMethod);
        for (EzyMethod method : methods) {
            RpcRequestHandlerMethod m = new RpcRequestHandlerMethod(commandGroup, method);
            list.add(m);
        }
        return list;
    }

    public List<RpcExceptionHandlerMethod> fetchExceptionHandlerMethods() {
        List<RpcExceptionHandlerMethod> list = new ArrayList<>();
        List<EzyMethod> methods = clazz.getMethods(
            RpcExceptionHandlerMethod::isExceptionHandlerMethod
        );
        for (EzyMethod method : methods) {
            RpcExceptionHandlerMethod m = new RpcExceptionHandlerMethod(method);
            list.add(m);
        }
        return list;
    }

    protected final Map<Class<?>, RpcExceptionHandlerMethod> fetchExceptionHandlerMethodMap() {
        Map<Class<?>, RpcExceptionHandlerMethod> answer = new HashMap<>();
        for (RpcExceptionHandlerMethod m : exceptionHandlerMethods) {
            for (Class<?> exceptionClass : m.getExceptionClasses()) {
                answer.put(exceptionClass, m);
            }
        }
        return answer;
    }

    protected boolean isRequestHandlerMethod(EzyMethod method) {
        return method.isAnnotated(Rpc.class);
    }

    public String getControllerName() {
        return clazz.getClazz().getSimpleName();
    }

    @Override
    public String toString() {
        return clazz.getName() +
            "(\n" +
            "\tinstance: " + instance + ",\n" +
            "\trequestHandlerMethods: " + requestHandlerMethods + ",\n" +
            ")";
    }
}
