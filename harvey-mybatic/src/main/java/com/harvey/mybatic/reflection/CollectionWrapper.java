package com.harvey.mybatic.reflection;

import java.util.Collection;
import java.util.List;

/**
 * @author harvey
 */
public class CollectionWrapper extends BaseObjectWrapper {
    private final Collection<Object> coll;
    
    public CollectionWrapper(Collection<Object> coll, MetaObject metaObject) {
        super(metaObject);
        this.coll = coll;
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
