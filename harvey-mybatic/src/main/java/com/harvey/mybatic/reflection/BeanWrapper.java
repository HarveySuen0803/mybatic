package com.harvey.mybatic.reflection;

import java.util.List;
import java.util.Map;

/**
 * @author harvey
 */
public class BeanWrapper extends BaseObjectWrapper {
    private final Object object;
    
    private final MetaClass metaClass;
    
    public BeanWrapper(Object object, MetaObject metaObject) {
        super(metaObject);
        
        this.object = object;
        this.metaClass = MetaClass.forClass(object.getClass());
    }
    
    @Override
    public Object get(PropertyToken token) {
        if ("".equals(token.getBasePropName())) {
            throw new IllegalArgumentException("PropertyToken name cannot be empty");
        }
        
        if (token.isContainer()) {
            Object container = metaObject.getValue(token.getBasePropName());
            return getContainerValue(token, container);
        } else {
            Invoker invoker = metaClass.getGetterInvoker(token.getBasePropName());
            try {
                return invoker.invoke(object, NO_ARGUMENTS);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Could not get property '" + token.getBasePropName() + "' from " + object.getClass() + ".  Cause: " + e, e);
            }
        }
    }
    
    private Object getContainerValue(PropertyToken token, Object container) {
        if (container instanceof Map) {
            return ((Map<String, Object>) container).get(token.getIdx());
        }
        
        int i = Integer.parseInt(token.getIdx());
        
        if (container instanceof List) {
            return ((List<Object>) container).get(i);
        }
        
        if (container instanceof Object[]) {
            return ((Object[]) container)[i];
        }
        
        if (container instanceof char[]) {
            return ((char[]) container)[i];
        }
        
        if (container instanceof boolean[]) {
            return ((boolean[]) container)[i];
        }
        
        if (container instanceof byte[]) {
            return ((byte[]) container)[i];
        }
        
        if (container instanceof double[]) {
            return ((double[]) container)[i];
        }
        
        if (container instanceof float[]) {
            return ((float[]) container)[i];
        }
        
        if (container instanceof int[]) {
            return ((int[]) container)[i];
        }
        
        if (container instanceof long[]) {
            return ((long[]) container)[i];
        }
        
        if (container instanceof short[]) {
            return ((short[]) container)[i];
        }
        
        throw new RuntimeException("The '" + token.getBasePropName() + "' property of " + container + " is not a List or Array.");
    }
    
    @Override
    public void set(PropertyToken token, Object value) {
        if (token.isContainer()) {
            Object container = metaObject.getValue(token.getBasePropName());
            setContainerValue(token, container, value);
        } else {
            Invoker invoker = metaClass.getSetterInvoker(token.getPropName());
            Object[] args = {value};
            try {
                invoker.invoke(object, args);
            } catch (Throwable t) {
                throw new RuntimeException("Could not set property '" + token.getBasePropName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t.toString(), t);
            }
        }
    }
    
    protected void setContainerValue(PropertyToken token, Object container, Object value) {
        if (container instanceof Map) {
            ((Map<String, Object>) container).put(token.getIdx(), value);
            return;
        }
        
        int i = Integer.parseInt(token.getIdx());
        
        if (container instanceof List) {
            ((List<Object>) container).set(i, value);
        }
        
        if (container instanceof Object[]) {
            ((Object[]) container)[i] = value;
        }
        
        if (container instanceof char[]) {
            ((char[]) container)[i] = (Character) value;
        }
        
        if (container instanceof boolean[]) {
            ((boolean[]) container)[i] = (Boolean) value;
        }
        
        if (container instanceof byte[]) {
            ((byte[]) container)[i] = (Byte) value;
        }
        
        if (container instanceof double[]) {
            ((double[]) container)[i] = (Double) value;
        }
        
        if (container instanceof float[]) {
            ((float[]) container)[i] = (Float) value;
        }
        
        if (container instanceof int[]) {
            ((int[]) container)[i] = (Integer) value;
        }
        
        if (container instanceof long[]) {
            ((long[]) container)[i] = (Long) value;
        }
        
        if (container instanceof short[]) {
            ((short[]) container)[i] = (Short) value;
        }
        
        throw new RuntimeException("The '" + token.getBasePropName() + "' property of " + container + " is not a List or Array.");
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
        PropertyToken token = new PropertyToken(name);
        if (token.hasChildren()) {
            MetaObject newMetaObject = metaObject.getMetaObject(token.getPropName());
            if (newMetaObject == MetaObject.NULL) {
                return metaClass.getSetterType(name);
            } else {
                return newMetaObject.getSetterType(token.getChildrenTokenValue());
            }
        } else {
            return metaClass.getSetterType(name);
        }
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
        MetaObject newMetaObject;
        Class<?> type = getSetterType(token.getBasePropName());
        try {
            Object newObject = objectFactory.create(type);
            newMetaObject = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory());
            set(token, newObject);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set value of property '" + token.getPropName() + "' because '" + token.getPropName()+ "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
        }
        return newMetaObject;
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
