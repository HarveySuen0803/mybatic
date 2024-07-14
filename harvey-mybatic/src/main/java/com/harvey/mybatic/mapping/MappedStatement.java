package com.harvey.mybatic.mapping;

/**
 * Represents a mapped SQL statement in MyBatis.
 * This class contains all the necessary configuration and metadata information
 * to execute a SQL statement defined in a MyBatis XML mapper file.
 *
 * @author harvey
 */
public class MappedStatement {
    private String id;
    
    private SqlCommandType sqlCommandType;
    
    private BoundSql boundSql;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }
    
    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }
    
    public BoundSql getBoundSql() {
        return boundSql;
    }
    
    public void setBoundSql(BoundSql boundSql) {
        this.boundSql = boundSql;
    }
}
