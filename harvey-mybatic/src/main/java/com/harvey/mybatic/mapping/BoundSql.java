package com.harvey.mybatic.mapping;

import java.util.Map;

/**
 * @author harvey
 */
public class BoundSql {
    private String sql;
    
    private Map<Integer, String> parameterMapping;
    
    private String parameterType;
    
    private String resultType;
    
    public BoundSql() {
    }
    
    public BoundSql(String sql, Map<Integer, String> parameterMapping, String parameterType, String resultType) {
        this.sql = sql;
        this.parameterMapping = parameterMapping;
        this.parameterType = parameterType;
        this.resultType = resultType;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public Map<Integer, String> getParameterMapping() {
        return parameterMapping;
    }
    
    public void setParameterMapping(Map<Integer, String> parameterMapping) {
        this.parameterMapping = parameterMapping;
    }
    
    public String getParameterType() {
        return parameterType;
    }
    
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
    
    public String getResultType() {
        return resultType;
    }
    
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }
}
