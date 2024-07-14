package com.harvey.mybatic.session;

import com.harvey.mybatic.mapping.MybaticRuntime;
import com.harvey.mybatic.mapping.MybaticRuntimeHolder;

/**
 * @author harvey
 */
public class SqlSessionFactoryBuilder {
    private SqlSessionFactory sqlSessionFactory;
    
    public SqlSessionFactoryBuilder parseConfigXml() {
        MybaticRuntime mybaticRuntime = MybaticRuntimeHolder.getInstance();
        
        sqlSessionFactory = new DefaultSqlSessionFactory(mybaticRuntime);
        
        return this;
    }
    
    public SqlSessionFactory build() {
        return sqlSessionFactory;
    }
}
