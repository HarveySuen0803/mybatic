package com.harvey.mybatic.mapping;

/**
 * @author harvey
 */
public class SqlCommand {
    private final String id;
    
    private final SqlCommandType type;
    
    private final Object[] args;
    
    public SqlCommand(String id, SqlCommandType type, Object[] args) {
        this.id = id;
        this.type = type;
        this.args = args;
    }
    
    public String getId() {
        return id;
    }
    
    public SqlCommandType getType() {
        return type;
    }
    
    public Object[] getArgs() {
        return args;
    }
}
