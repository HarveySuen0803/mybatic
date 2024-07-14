package com.harvey.mybatic.mapping;

import com.harvey.mybatic.session.SqlSession;

/**
 * @author harvey
 */
public class MapperMethod {
    private final SqlSession sqlSession;
    
    public <T> MapperMethod(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }
    
    public Object execute(SqlCommand sqlCommand) {
        Object result = null;
        switch (sqlCommand.getType()) {
            case SELECT -> {
                result = sqlSession.selectOne(sqlCommand.getId(), sqlCommand.getArgs());
                break;
            }
            case INSERT -> {
                break;
            }
            case UPDATE -> {
                break;
            }
            case DELETE -> {
                break;
            }
            default -> {
                throw new RuntimeException("Unknown execution method for: " + sqlCommand.getId());
            }
        }
        
        return result;
    }
}
