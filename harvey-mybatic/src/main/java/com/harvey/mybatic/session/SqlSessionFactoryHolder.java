package com.harvey.mybatic.session;

/**
 * @author harvey
 */
public class SqlSessionFactoryHolder {
    private static class Holder {
        private static final SqlSessionFactory INSTANCE = buildSqlSessionFactory();
        
        private static SqlSessionFactory buildSqlSessionFactory() {
            return new SqlSessionFactoryBuilder()
                .parseConfigXml()
                .build();
        }
    }
    
    public static SqlSessionFactory getInstance() {
        return Holder.INSTANCE;
    }
}
