package com.harvey.mybatic.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author harvey
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {
    @Override
    public DataSource getDataSource(Properties properties) {
        PooledDataSource pooledDataSource = new PooledDataSource(
            properties.getProperty("url"),
            properties.getProperty("username"),
            properties.getProperty("password"),
            properties.getProperty("driver")
        );
        return pooledDataSource;
    }
}
