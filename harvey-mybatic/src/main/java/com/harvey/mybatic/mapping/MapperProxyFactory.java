package com.harvey.mybatic.mapping;

import com.harvey.mybatic.session.SqlSession;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author harvey
 */
public class MapperProxyFactory<T> {
    private final Map<String, MapperMethod> mapperMethodMap = new ConcurrentHashMap<>();
    
    private final Class<T> mapperInterface;
    
    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }
    
    public T getMapperProxy(SqlSession sqlSession) {
        MapperProxyInvocationHandler mapperProxyInvocationHandler = new MapperProxyInvocationHandler(sqlSession, mapperInterface, mapperMethodMap);
        
        T mapperProxy = (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxyInvocationHandler);
        
        return mapperProxy;
    }
    
    private class MapperProxyInvocationHandler implements InvocationHandler, Serializable {
        private final SqlSession sqlSession;
        
        private final Class<T> mapperInterfaceClass;
        
        private final Map<String, MapperMethod> mapperMethodCacheMap;
        
        @Serial
        private static final long serialVersionUID = 1L;
        
        public MapperProxyInvocationHandler(SqlSession sqlSession, Class<T> mapperInterfaceClass, Map<String, MapperMethod> mapperMethodCacheMap) {
            this.sqlSession = sqlSession;
            this.mapperInterfaceClass = mapperInterfaceClass;
            this.mapperMethodCacheMap = mapperMethodCacheMap;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            
            String mappedStatementId = mapperInterfaceClass.getName() + "." + method.getName();
            
            MapperMethod mapperMethod = mapperMethodCacheMap.computeIfAbsent(mappedStatementId, k -> new MapperMethod(sqlSession));
            MybaticRuntime mybaticRuntime = sqlSession.getMybaticRuntime();
            MappedStatement mappedStatement = mybaticRuntime.getMappedStatement(mappedStatementId);
            SqlCommand sqlCommand = new SqlCommand(mappedStatement.getId(), mappedStatement.getSqlCommandType(), args);
            
            return mapperMethod.execute(sqlCommand);
        }
    }
}