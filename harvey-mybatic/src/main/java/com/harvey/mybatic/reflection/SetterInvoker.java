package com.harvey.mybatic.reflection;

import java.lang.reflect.Field;

/**
 * @author harvey
 */
public class SetterInvoker implements Invoker {
    private final Field prop;
    
    public SetterInvoker(Field prop) {
        this.prop = prop;
    }
    
    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException {
        prop.set(target, args[0]);
        return null;
    }
    
    @Override
    public Class<?> getType() {
        return prop.getType();
    }
}
