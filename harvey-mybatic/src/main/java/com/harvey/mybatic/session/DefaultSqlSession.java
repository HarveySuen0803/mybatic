package com.harvey.mybatic.session;

import com.harvey.mybatic.executor.Executor;
import com.harvey.mybatic.mapping.MappedStatement;
import com.harvey.mybatic.mapping.MybaticRuntime;

import java.util.List;

/**
 * @author harvey
 */
public class DefaultSqlSession implements SqlSession {
    private final MybaticRuntime mybaticRuntime;
    
    private final Executor executor;
    
    public DefaultSqlSession(MybaticRuntime mybaticRuntime, Executor executor) {
        this.mybaticRuntime = mybaticRuntime;
        this.executor = executor;
    }
    
    @Override
    public <T> T selectOne(String statementId) {
        MappedStatement mappedStatement = mybaticRuntime.getMappedStatement(statementId);
        return (T) String.format("%s, %s", statementId, mappedStatement);
    }
    
    @Override
    public <T> T selectOne(String statementId, Object parameter) {
        MappedStatement mappedStatement = mybaticRuntime.getMappedStatement(statementId);
        
        List<T> beanList = executor.query(mappedStatement, parameter);
        
        return beanList.get(0);
    }
    
    @Override
    public <T> T getMapperProxy(Class<T> mapperInterfaceClass) {
        return mybaticRuntime.getMapperProxy(mapperInterfaceClass, this);
    }
    
    @Override
    public MybaticRuntime getMybaticRuntime() {
        return mybaticRuntime;
    }
}
