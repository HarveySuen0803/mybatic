package com.harvey.mybatic.reflection;

import java.lang.reflect.InvocationTargetException;

/**
 * @author harvey
 */
public interface Invoker {
    Object invoke(Object target, Object[] args) throws Exception;
    
    Class<?> getType();
}
