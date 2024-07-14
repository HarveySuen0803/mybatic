package com.harvey.mybatic.session;

import com.harvey.mybatic.mapping.MybaticRuntime;

/**
 * The primary Java interface for working with MyBatis.
 * Through this interface you can execute commands, get mappers and manage transactions.
 *
 * @author harvey
 */
public interface SqlSession {
    /**
     * Retrieve a single row mapped from the statement key.
     *
     * @param <T> the returned object type
     * @param statement the statement
     * @return Mapped object
     */
    <T> T selectOne(String statement);
    
    /**
     * Retrieve a single row mapped from the statement key and parameter.
     *
     * @param <T> the returned object type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return Mapped object
     */
    <T> T selectOne(String statement, Object parameter);
    
    /**
     * Retrieves a mapper.
     *
     * @param <T> the mapper type
     * @param type Mapper interface class
     * @return a mapper bound to this SqlSession
     */
    <T> T getMapperProxy(Class<T> type);
    
    /**
     * Retrieves current configuration
     * @return Configuration
     */
    MybaticRuntime getMybaticRuntime();
}