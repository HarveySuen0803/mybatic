package com.harvey.mybatic.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @author harvey
 */
public enum JdbcType {
    INTEGER(Types.INTEGER),
    FLOAT(Types.FLOAT),
    DOUBLE(Types.DOUBLE),
    DECIMAL(Types.DECIMAL),
    VARCHAR(Types.VARCHAR),
    TimeMillis(Types.TIMESTAMP);

    public final int TYPE_CODE;
    
    private static final Map<Integer,JdbcType> JDBC_TYPE_MAP = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            JDBC_TYPE_MAP.put(type.TYPE_CODE, type);
        }
    }

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    public static JdbcType getType(int typeCode)  {
        return JDBC_TYPE_MAP.get(typeCode);
    }
}