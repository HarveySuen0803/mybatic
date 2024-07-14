package com.harvey.mybatic.mapping;

import com.harvey.mybatic.datasource.DruidDataSourceFactory;
import com.harvey.mybatic.datasource.PooledDataSourceFactory;
import com.harvey.mybatic.datasource.UnpooledDataSourceFactory;
import com.harvey.mybatic.session.SqlSession;
import com.harvey.mybatic.transaction.JdbcTransactionFactory;
import com.harvey.mybatic.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author harvey
 */
public class MybaticRuntime {
    private Environment environment;
    
    private final Map<String, MappedStatement> mappedStatementMap = new HashMap<>();
    
    private final MapperProxyRegistry mapperProxyRegistry = MapperProxyRegistry.getInstance();
    
    private final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    
    public MybaticRuntime() {
        typeAliasRegistry.registerAlias("jdbc", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("druid", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("unpooled", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("pooled", PooledDataSourceFactory.class);
    }
    
    public <T> T getMapperProxy(Class<T> mapperInterfaceClass, SqlSession sqlSession) {
        return mapperProxyRegistry.getMapperProxy(mapperInterfaceClass, sqlSession);
    }
    
    public void addMapperProxy(String packageName) {
        mapperProxyRegistry.addMapperProxy(packageName);
    }
    
    public <T> void addMapperProxy(Class<T> mapperInterfaceClass) {
        mapperProxyRegistry.addMapperProxy(mapperInterfaceClass);
    }
    
    public <T> boolean isMapperExists(Class<T> mapperInterfaceClass) {
        return mapperProxyRegistry.isMapperExists(mapperInterfaceClass);
    }
    
    public <T> boolean isMapperNotExists(Class<T> mapperInterfaceClass) {
        return !isMapperExists(mapperInterfaceClass);
    }
    
    public void addMappedStatement(MappedStatement mappedStatement) {
        mappedStatementMap.put(mappedStatement.getId(), mappedStatement);
    }
    
    public MappedStatement getMappedStatement(String id) {
        return mappedStatementMap.get(id);
    }
    
    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }
    
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
}
