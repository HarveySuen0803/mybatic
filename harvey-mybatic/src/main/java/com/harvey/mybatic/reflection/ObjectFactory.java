package com.harvey.mybatic.reflection;

import java.util.List;
import java.util.Properties;

/**
 * @author harvey
 */
public interface ObjectFactory {
    void setProperties(Properties props);
    
    <T> T create(Class<T> clazz);
    
    <T> T create(Class<T> clazz, List<Class<?>> argClasses, List<Object> args);
    
    <T> boolean isCollection(Class<T> type);
}
