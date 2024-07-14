package com.harvey.mybatic.mapping;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author harvey
 */
public class ModelMapper {
    public static <T> List<T> toBeanList(ResultSet resultSet, Class<?> clazz) {
        List<T> beanList = new ArrayList<>();
        
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                T bean = (T) clazz.getDeclaredConstructor().newInstance();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    
                    Field field = getField(clazz, columnName);
                    if (field == null) {
                        continue;
                    }
                    
                    field.setAccessible(true);
                    if (columnValue instanceof Timestamp) {
                        columnValue = new Date(((Timestamp) columnValue).getTime());
                    } else if (columnValue instanceof java.sql.Date) {
                        columnValue = new Date(((java.sql.Date) columnValue).getTime());
                    } else if (columnValue instanceof Boolean) {
                        columnValue = resultSet.getBoolean(i);
                    }
                    
                    columnValue = convertValueToRequiredType(field, columnValue);
                    
                    field.set(bean, columnValue);
                }
                beanList.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return beanList;
    }
    
    private static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
    
    private static Object convertValueToRequiredType(Field field, Object value) {
        if (value == null) {
            return null;
        }
        
        Class<?> fieldType = field.getType();
        
        if (fieldType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        if (fieldType == int.class || fieldType == Integer.class) {
            return ((Number) value).intValue();
        } else if (fieldType == long.class || fieldType == Long.class) {
            return ((Number) value).longValue();
        } else if (fieldType == float.class || fieldType == Float.class) {
            return ((Number) value).floatValue();
        } else if (fieldType == double.class || fieldType == Double.class) {
            return ((Number) value).doubleValue();
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return (Boolean) value;
        } else if (fieldType == String.class) {
            return value.toString();
        } else if (fieldType == Date.class && value instanceof java.sql.Date) {
            return new Date(((java.sql.Date) value).getTime());
        } else if (fieldType == Date.class && value instanceof Timestamp) {
            return new Date(((Timestamp) value).getTime());
        }
        
        return value;
    }
}
