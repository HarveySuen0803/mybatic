package com.harvey.mybatic.reflection;

import java.util.List;

public interface ObjectWrapper {
    Object get(PropertyToken token);
    
    void set(PropertyToken token, Object value);

    String getPropertyName(String name, boolean useCamelCaseMapping);

    String[] getGetterNames();

    String[] getSetterNames();

    Class<?> getSetterType(String name);

    Class<?> getGetterType(String name);

    boolean hasSetter(String name);

    boolean hasGetter(String name);

    MetaObject createProperty(PropertyToken token, ObjectFactory objectFactory);

    boolean isCollection();

    void add(Object element);

    <E> void addAll(List<E> element);
}