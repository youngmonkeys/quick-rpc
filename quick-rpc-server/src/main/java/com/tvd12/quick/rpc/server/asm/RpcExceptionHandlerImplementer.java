package com.tvd12.quick.rpc.server.asm;

import com.tvd12.ezyfox.asm.EzyFunction;
import com.tvd12.ezyfox.asm.EzyFunction.EzyBody;
import com.tvd12.ezyfox.asm.EzyInstruction;
import com.tvd12.ezyfox.reflect.*;
import com.tvd12.quick.rpc.server.entity.RpcRequest;
import com.tvd12.quick.rpc.server.entity.RpcResponse;
import com.tvd12.quick.rpc.server.handler.RpcUncaughtExceptionHandler;
import com.tvd12.quick.rpc.server.reflect.RpcExceptionHandlerMethod;
import com.tvd12.quick.rpc.server.reflect.RpcExceptionHandlerProxy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public class RpcExceptionHandlerImplementer
    extends RpcAbstractHandlerImplementer<RpcExceptionHandlerMethod> {

    @Setter
    private static boolean debug;
    protected final RpcExceptionHandlerProxy exceptionHandler;

    protected static final AtomicInteger COUNT
        = new AtomicInteger(0);

    public RpcExceptionHandlerImplementer(
        RpcExceptionHandlerProxy exceptionHandler,
        RpcExceptionHandlerMethod handlerMethod
    ) {
        super(handlerMethod);
        this.exceptionHandler = exceptionHandler;
    }

    public RpcUncaughtExceptionHandler implement() {
        try {
            return doImplement();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected RpcUncaughtExceptionHandler doImplement() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        String implClassName = getImplClassName();
        CtClass implClass = pool.makeClass(implClassName);
        EzyClass superClass = new EzyClass(getSuperClass());
        String exceptionHandlerFieldContent = makeExceptionHandlerFieldContent();
        String setExceptionHandlerMethodContent = makeSetExceptionHandlerMethodContent();
        String handleExceptionMethodContent = makeHandleExceptionMethodContent();
        printComponentContent(implClassName);
        printComponentContent(exceptionHandlerFieldContent);
        printComponentContent(setExceptionHandlerMethodContent);
        printComponentContent(handleExceptionMethodContent);
        implClass.setSuperclass(pool.get(superClass.getName()));
        implClass.addField(CtField.make(exceptionHandlerFieldContent, implClass));
        implClass.addMethod(CtNewMethod.make(setExceptionHandlerMethodContent, implClass));
        implClass.addMethod(CtNewMethod.make(handleExceptionMethodContent, implClass));
        Class answerClass = implClass.toClass();
        implClass.detach();
        RpcAsmUncaughtExceptionHandler handler = EzyClasses.newInstance(answerClass);
        setRepoComponent(handler);
        return handler;
    }

    protected void setRepoComponent(RpcAsmUncaughtExceptionHandler handler) {
        handler.setExceptionHandler(exceptionHandler.getInstance());
    }

    protected String makeExceptionHandlerFieldContent() {
        return new EzyInstruction()
            .append("private ")
            .append(exceptionHandler.getClazz().getName())
            .append(" exceptionHandler")
            .toString();
    }

    protected String makeSetExceptionHandlerMethodContent() {
        return new EzyFunction(getSetExceptionHandlerMethod())
            .body()
            .append(new EzyInstruction("\t", "\n")
                .append("this.exceptionHandler")
                .equal()
                .brackets(exceptionHandler.getClazz().getClazz())
                .append("arg0"))
            .function()
            .toString();
    }

    protected String makeHandleExceptionMethodContent() {
        EzyMethod method = getHandleExceptionMethod();
        EzyFunction function = new EzyFunction(method)
            .throwsException();
        EzyBody body = function.body();
        Class<?>[] exceptionClasses = handlerMethod.getExceptionClasses();
        EzyClassTree exceptionTree = new EzyClassTree(exceptionClasses);
        for (Class<?> exceptionClass : exceptionTree.toList()) {
            EzyInstruction instructionIf = new EzyInstruction("\t", "\n", false)
                .append("if(arg2 instanceof ")
                .append(exceptionClass.getName())
                .append(") {");
            body.append(instructionIf);
            EzyInstruction instructionHandle = new EzyInstruction("\t\t", "\n");
            if (handlerMethod.getReturnType() != void.class) {
                instructionHandle.variable(Object.class, "errorData")
                    .equal();
            }
            instructionHandle
                .append("this.exceptionHandler.").append(handlerMethod.getName())
                .bracketopen();
            appendHandleExceptionMethodArguments(instructionHandle, exceptionClass);
            instructionHandle
                .bracketclose();
            body.append(instructionHandle);
            if (handlerMethod.getReturnType() != void.class) {
                body.append(
                    new EzyInstruction("\t\t", "\n").append("arg1.writeError(errorData)")
                );
            }
            body.append(new EzyInstruction("\t", "\n", false).append("}"));
        }
        body.append(new EzyInstruction("\t", "\n", false).append("else {"));
        body.append(new EzyInstruction("\t\t", "\n").append("throw arg2"));
        body.append(new EzyInstruction("\t", "\n", false).append("}"));
        return function.toString();
    }

    protected void appendHandleExceptionMethodArguments(
        EzyInstruction instruction,
        Class<?> exceptionClass
    ) {
        super.appendHandleExceptionMethodArguments(
            handlerMethod, instruction, exceptionClass);
    }

    protected EzyMethod getSetExceptionHandlerMethod() {
        Method method = EzyMethods.getMethod(
            RpcAsmAbstractUncaughtExceptionHandler.class, "setExceptionHandler", Object.class);
        return new EzyMethod(method);
    }

    protected EzyMethod getHandleExceptionMethod() {
        Method method = EzyMethods.getMethod(
            RpcAsmAbstractUncaughtExceptionHandler.class,
            "handleException",
            RpcRequest.class, RpcResponse.class, Exception.class
        );
        return new EzyMethod(method);
    }

    protected Class<?> getSuperClass() {
        return RpcAsmAbstractUncaughtExceptionHandler.class;
    }

    protected String getImplClassName() {
        return exceptionHandler.getClassSimpleName()
            + "$" + handlerMethod.getName() + "$ExceptionHandler$AutoImpl$" + COUNT.incrementAndGet();
    }

    protected void printComponentContent(String componentContent) {
        if (debug) {
            logger.debug("component content: \n{}", componentContent);
        }
    }

}
