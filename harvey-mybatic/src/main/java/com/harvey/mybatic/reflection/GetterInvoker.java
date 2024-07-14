package com.harvey.mybatic.reflection;

import java.lang.reflect.Field;

/**
 * @author harvey
 */
public class GetterInvoker implements Invoker {
    private final Field prop;
    
    public GetterInvoker(Field prop) {
        this.prop = prop;
    }
    
    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException {
        return prop.get(target);
    }
    
    @Override
    public Class<?> getType() {
        return prop.getType();
    }
}
