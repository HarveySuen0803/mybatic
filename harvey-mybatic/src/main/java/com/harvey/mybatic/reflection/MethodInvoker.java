package com.harvey.mybatic.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author harvey
 */
public class MethodInvoker implements Invoker {
    private final Method method;
    
    private final Class<?> type;
    
    public MethodInvoker(Method method) {
        this.method = method;
        
        if (method.getParameters().length == 1) {
            type = method.getParameters()[0].getClass();
        } else {
            type = method.getReturnType();
        }
    }
    
    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(target, args);
    }
    
    @Override
    public Class<?> getType() {
        return type;
    }
}
