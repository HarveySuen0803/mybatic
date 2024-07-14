package com.harvey.mybatic.reflection;

import cn.hutool.core.collection.CollUtil;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author harvey
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Override
    public void setProperties(Properties props) {
    }
    
    @Override
    public <T> T create(Class<T> clazz) {
        return create(clazz, null, null);
    }
    
    @Override
    public <T> T create(Class<T> clazz, List<Class<?>> argClasses, List<Object> args) {
        Class<?> instanceClass = resolveInterface(clazz);
        
        return (T) instantiateClass(instanceClass, argClasses, args);
    }
    
    @Override
    public <T> boolean isCollection(Class<T> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }
    
    private <T> T instantiateClass(Class<T> clazz, List<Class<?>> argClasses, List<Object> args) {
        try {
            Constructor<T> constructor;
            if (CollUtil.isEmpty(args) || CollUtil.isEmpty(argClasses)) {
                constructor = clazz.getDeclaredConstructor();
            } else {
                constructor = clazz.getDeclaredConstructor(argClasses.toArray(new Class[0]));
            }
            
            if (!constructor.canAccess(null)) {
                constructor.setAccessible(true);
            }
            
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            StringBuilder argClassesStr = new StringBuilder();
            for (Class<?> argClass : argClasses) {
                argClassesStr.append(argClass);
                argClassesStr.append(",");
            }
            
            StringBuilder argsStr = new StringBuilder();
            for (Object arg : args) {
                argsStr.append(argsStr);
                argsStr.append(",");
            }
            
            throw new RuntimeException("Error instantiating " + clazz + " with invalid types (" + argClassesStr + ") or values (" + argsStr + "). Cause: " + e, e);
        }
    }
    
    private Class<?> resolveInterface(Class<?> clazz) {
        if (clazz == List.class || clazz == Collection.class || clazz == Iterator.class) {
            return ArrayList.class;
        } else if (clazz == Map.class) {
            return HashMap.class;
        } else if (clazz == Set.class) {
            return HashSet.class;
        } else if (clazz == SortedSet.class) {
            return TreeSet.class;
        } else {
            return clazz;
        }
    }
}
