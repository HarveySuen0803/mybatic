package com.harvey.mybatic.mapping;

import cn.hutool.core.lang.ClassScanner;
import com.harvey.mybatic.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author harvey
 */
public class MapperProxyRegistry {
    private final Map<Class<?>, MapperProxyFactory<?>> mapperProxyFactoryMap = new HashMap<>();
    
    private MapperProxyRegistry() {}
    
    private static class SingletonHolder {
        private static final MapperProxyRegistry INSTANCE = new MapperProxyRegistry();
    }
    
    public static MapperProxyRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    public <T> T getMapperProxy(Class<T> mapperInterfaceClass, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) mapperProxyFactoryMap.get(mapperInterfaceClass);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type " + mapperInterfaceClass + " is not known to the MapperRegistry.");
        }
        
        T mapperProxy;
        try {
            mapperProxy = mapperProxyFactory.getMapperProxy(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mapper proxy instance. Cause: " + e, e);
        }
        
        return mapperProxy;
    }
    
    public void addMapperProxy(String packageName) {
        Set<Class<?>> mapperClassSet = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mapperClassSet) {
            addMapperProxy(mapperClass);
        }
    }
    
    public <T> void addMapperProxy(Class<T> mapperInterfaceClass) {
        if (!mapperInterfaceClass.isInterface()) {
            throw new RuntimeException("Type " + mapperInterfaceClass + " is not an interface");
        }
        
        if (isMapperExists(mapperInterfaceClass)) {
            throw new RuntimeException("Type " + mapperInterfaceClass + " is already known to the MapperRegistry.");
        }
        
        MapperProxyFactory<T> mapperProxyFactory = new MapperProxyFactory<>(mapperInterfaceClass);
        mapperProxyFactoryMap.put(mapperInterfaceClass, mapperProxyFactory);
    }
    
    public <T> boolean isMapperExists(Class<T> mapperInterfaceClass) {
        return mapperProxyFactoryMap.containsKey(mapperInterfaceClass);
    }
    
    public <T> boolean isMapperNotExists(Class<T> mapperInterfaceClass) {
        return !isMapperExists(mapperInterfaceClass);
    }
}
