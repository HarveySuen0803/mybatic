package com.harvey.mybatic.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author harvey
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {
    @Override
    public DataSource getDataSource(Properties properties) {
        UnpooledDataSource unpooledDataSource = new UnpooledDataSource(
            properties.getProperty("url"),
            properties.getProperty("username"),
            properties.getProperty("password"),
            properties.getProperty("driver")
        );
        return unpooledDataSource;
    }
}
