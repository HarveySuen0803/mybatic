package com.harvey.mybatic.reflection;

import java.util.List;
import java.util.Map;

/**
 * @author harvey
 */
public class MapWrapper extends BaseObjectWrapper {
    private Map<String, Object> map;
    
    public MapWrapper(Map<String, Object> map, MetaObject metaObject) {
        super(metaObject);
        
        this.map = map;
    }
    
    @Override
    public Object get(PropertyToken token) {
        return null;
    }
    
    @Override
    public void set(PropertyToken token, Object value) {
    
    }
    
    @Override
    public String getPropertyName(String name, boolean useCamelCaseMapping) {
        return "";
    }
    
    @Override
    public String[] getGetterNames() {
        return new String[0];
    }
    
    @Override
    public String[] getSetterNames() {
        return new String[0];
    }
    
    @Override
    public Class<?> getSetterType(String name) {
        return null;
    }
    
    @Override
    public Class<?> getGetterType(String name) {
        return null;
    }
    
    @Override
    public boolean hasSetter(String name) {
        return false;
    }
    
    @Override
    public boolean hasGetter(String name) {
        return false;
    }
    
    @Override
    public MetaObject createProperty(PropertyToken token, ObjectFactory objectFactory) {
        return null;
    }
    
    @Override
    public boolean isCollection() {
        return false;
    }
    
    @Override
    public void add(Object element) {
    
    }
    
    @Override
    public <E> void addAll(List<E> element) {
    
    }
}
