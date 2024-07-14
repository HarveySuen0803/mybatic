package com.harvey.mybatic;

import com.harvey.mybatic.datasource.PooledDataSource;
import com.harvey.mybatic.mapper.UserMapper;
import com.harvey.mybatic.model.po.UserPo;
import com.harvey.mybatic.reflection.Reflector;
import com.harvey.mybatic.session.SqlSession;
import com.harvey.mybatic.session.SqlSessionFactory;
import com.harvey.mybatic.session.SqlSessionFactoryHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MainTest {
    @Test
    public void testMapping() throws IOException {
        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryHolder.getInstance();
        
        SqlSession sqlSession = sqlSessionFactory.getSqlSession();

        UserMapper userMapper = sqlSession.getMapperProxy(UserMapper.class);

        UserPo userPo = userMapper.selectById(1L);

        System.out.println(userPo);
    }
    
    @Test
    public void testPooledDataSource() throws SQLException, InterruptedException {
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/db?useUnicode=true";
        String username = "root";
        String password = "111";
        
        PooledDataSource pooledDataSource = new PooledDataSource(driverClassName, url, username, password);
        
        Connection connection = pooledDataSource.getConnection();
        
        connection.close();
    }
    
    @Test
    public void testReflection() {
        // Reflector reflector = new Reflector(UserPo.class);
        String str = "a";
        
        System.out.println(str.substring(1));
    }
}
