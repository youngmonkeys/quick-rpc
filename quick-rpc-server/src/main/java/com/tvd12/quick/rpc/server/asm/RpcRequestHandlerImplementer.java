package com.tvd12.quick.rpc.server.asm;

import com.tvd12.ezyfox.asm.EzyFunction;
import com.tvd12.ezyfox.asm.EzyFunction.EzyBody;
import com.tvd12.ezyfox.asm.EzyInstruction;
import com.tvd12.ezyfox.reflect.*;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.reflect.RpcControllerProxy;
import com.tvd12.quick.rpc.server.reflect.RpcExceptionHandlerMethod;
import com.tvd12.quick.rpc.server.reflect.RpcRequestHandlerMethod;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcRequestHandlerImplementer
    extends RpcAbstractHandlerImplementer<RpcRequestHandlerMethod> {

    protected static final String PARAMETER_PREFIX = "param";
    protected static final AtomicInteger COUNT = new AtomicInteger(0);
    @Setter
    private static boolean debug;
    protected final RpcControllerProxy controller;

    public RpcRequestHandlerImplementer(
        RpcControllerProxy controller, RpcRequestHandlerMethod handlerMethod) {
        super(handlerMethod);
        this.controller = controller;
    }

    public RpcAsmRequestHandler implement() {
        try {
            return doImplement();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    protected RpcAsmRequestHandler doImplement() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        String implClassName = getImplClassName();
        CtClass implClass = pool.makeClass(implClassName);
        EzyClass superClass = new EzyClass(getSuperClass());
        String controllerFieldContent = makeControllerFieldContent();
        String setControllerMethodContent = makeSetControllerMethodContent();
        String handleRequestMethodContent = makeHandleRequestMethodContent();
        String handleExceptionMethodContent = makeHandleExceptionMethodContent();
        String getRequestDataTypeMethodContent = makeGetRequestDataTypeMethodContent();
        printComponentContent(controllerFieldContent);
        printComponentContent(setControllerMethodContent);
        printComponentContent(handleRequestMethodContent);
        printComponentContent(handleExceptionMethodContent);
        printComponentContent(getRequestDataTypeMethodContent);
        implClass.setSuperclass(pool.get(superClass.getName()));
        implClass.addField(CtField.make(controllerFieldContent, implClass));
        implClass.addMethod(CtNewMethod.make(setControllerMethodContent, implClass));
        implClass.addMethod(CtNewMethod.make(handleRequestMethodContent, implClass));
        implClass.addMethod(CtNewMethod.make(handleExceptionMethodContent, implClass));
        implClass.addMethod(CtNewMethod.make(getRequestDataTypeMethodContent, implClass));
        Class answerClass = implClass.toClass();
        implClass.detach();
        RpcAsmRequestHandler handler = EzyClasses.newInstance(answerClass);
        handler.setCommand(handlerMethod.getCommand());
        setRepoComponent(handler);
        return handler;
    }

    protected void setRepoComponent(RpcAsmRequestHandler handler) {
        handler.setController(controller.getInstance());
    }

    protected String makeControllerFieldContent() {
        return new EzyInstruction()
            .append("private ")
            .append(controller.getClazz().getName())
            .append(" controller")
            .toString();
    }

    protected String makeSetControllerMethodContent() {
        return new EzyFunction(getSetControllerMethod())
            .body()
            .append(new EzyInstruction("\t", "\n")
                .append("this.controller")
                .equal()
                .brackets(controller.getClazz().getClazz())
                .append("arg0"))
            .function()
            .toString();
    }

    protected String makeHandleRequestMethodContent() {
        EzyMethod method = getHandleRequestMethod();
        EzyFunction function = new EzyFunction(method)
            .throwsException();
        EzyBody body = function.body();
        int paramCount = prepareHandleMethodArguments(body);
        EzyInstruction instruction = new EzyInstruction("\t", "\n");
        Class<?> returnType = handlerMethod.getReturnType();
        if (returnType != void.class) {
            instruction.answer();
        }
        StringBuilder answerExpression = new StringBuilder();
        answerExpression.append("this.controller.").append(handlerMethod.getName())
            .append("(");
        for (int i = 0; i < paramCount; ++i) {
            answerExpression.append(PARAMETER_PREFIX).append(i);
            if (i < paramCount - 1) {
                answerExpression.append(", ");
            }
        }
        answerExpression.append(")");
        if (returnType != void.class) {
            instruction.valueOf(returnType, answerExpression.toString());
        } else {
            instruction.append(answerExpression);
        }
        body.append(instruction);
        if (returnType == void.class) {
            body.append(new EzyInstruction("\t", "\n").append("return NO_RESPONSE"));
        }
        return function.toString();
    }

    protected String makeHandleExceptionMethodContent() {
        EzyMethod method = getHandleExceptionMethod();
        EzyFunction function = new EzyFunction(method)
            .throwsException();
        EzyBody body = function.body();
        Map<Class<?>, RpcExceptionHandlerMethod> exceptionHandlerMethodMap
            = controller.getExceptionHandlerMethodMap();
        Set<Class<?>> exceptionClasses = exceptionHandlerMethodMap.keySet();
        EzyClassTree exceptionTree = new EzyClassTree(exceptionClasses);
        for (Class<?> exceptionClass : exceptionTree.toList()) {
            RpcExceptionHandlerMethod m = exceptionHandlerMethodMap.get(exceptionClass);
            EzyInstruction instructionIf = new EzyInstruction("\t", "\n", false)
                .append("if(arg2 instanceof ")
                .append(exceptionClass.getName())
                .append(") {");
            body.append(instructionIf);
            EzyInstruction instructionHandle = new EzyInstruction("\t\t", "\n");
            instructionHandle
                .append("this.controller.").append(m.getName())
                .bracketopen();
            appendHandleExceptionMethodArguments(m, instructionHandle, exceptionClass);
            instructionHandle
                .bracketclose();
            body.append(instructionHandle);
            body.append(new EzyInstruction("\t", "\n", false).append("}"));
        }
        if (exceptionClasses.size() > 0) {
            body.append(new EzyInstruction("\t", "\n", false).append("else {"));
            body.append(new EzyInstruction("\t\t", "\n").append("throw arg2"));
            body.append(new EzyInstruction("\t", "\n", false).append("}"));
        } else {
            body.append(new EzyInstruction("\t", "\n").append("throw arg2"));
        }
        return function.toString();
    }

    protected void appendHandleExceptionMethodArguments(
        RpcExceptionHandlerMethod method,
        EzyInstruction instruction, Class<?> exceptionClass) {
        super.appendHandleExceptionMethodArguments(method, instruction, exceptionClass);
    }

    protected String makeGetRequestDataTypeMethodContent() {
        EzyInstruction answerInstruction = new EzyInstruction("\t", "\n")
            .answer();
        if (handlerMethod.getRequestDataType() == null) {
            answerInstruction.append("null");
        } else {
            answerInstruction.clazz(handlerMethod.getRequestDataType(), true);
        }
        return new EzyFunction(getGetResponseContentTypeMethod())
            .body()
            .append(answerInstruction)
            .function()
            .toString();
    }

    protected EzyMethod getSetControllerMethod() {
        Method method = EzyMethods.getMethod(
            RpcAsmAbstractRequestHandler.class, "setController", Object.class);
        return new EzyMethod(method);
    }

    protected EzyMethod getHandleRequestMethod() {
        Method method = EzyMethods.getMethod(
            RpcAsmAbstractRequestHandler.class, "handleRequest", RpcRequest.class, RpcResponse.class);
        return new EzyMethod(method);
    }

    protected EzyMethod getHandleExceptionMethod() {
        Method method = EzyMethods.getMethod(
            RpcAsmAbstractRequestHandler.class,
            "handleException",
            RpcRequest.class, RpcResponse.class, Exception.class);
        return new EzyMethod(method);
    }

    protected EzyMethod getGetResponseContentTypeMethod() {
        Method method = EzyMethods.getMethod(
            RpcAsmAbstractRequestHandler.class, "getDataType");
        return new EzyMethod(method);
    }

    protected Class<?> getSuperClass() {
        return RpcAsmAbstractRequestHandler.class;
    }

    protected String getImplClassName() {
        return controller.getControllerName()
            + "$" + handlerMethod.getName() + "$Handler$AutoImpl$" + COUNT.incrementAndGet();
    }

    protected void printComponentContent(String componentContent) {
        if (debug) {
            logger.debug("component content: \n{}", componentContent);
        }
    }

}
