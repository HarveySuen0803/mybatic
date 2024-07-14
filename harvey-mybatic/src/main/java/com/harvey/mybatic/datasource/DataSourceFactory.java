package com.harvey.mybatic.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author harvey
 */
public interface DataSourceFactory {
    DataSource getDataSource(Properties properties);
}
