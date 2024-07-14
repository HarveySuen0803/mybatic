package com.harvey.mybatic.reflection;

/**
 * @author harvey
 */
public interface ObjectWrapperFactory {
    boolean hasWrapper(Object object);
    
    ObjectWrapper getWrapper(Object object, MetaObject metaObject);
}
