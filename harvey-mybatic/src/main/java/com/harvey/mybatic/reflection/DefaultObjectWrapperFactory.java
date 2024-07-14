package com.harvey.mybatic.reflection;

/**
 * @author harvey
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {
    @Override
    public boolean hasWrapper(Object object) {
        return false;
    }
    
    @Override
    public ObjectWrapper getWrapper(Object object, MetaObject metaObject) {
        throw new RuntimeException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
