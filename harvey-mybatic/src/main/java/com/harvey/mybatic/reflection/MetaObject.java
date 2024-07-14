package com.harvey.mybatic.reflection;

import java.util.Collection;
import java.util.Map;

/**
 * @author harvey
 */
public class MetaObject {
    private Object originObject;
    
    private ObjectWrapper objectWrapper;
    
    private ObjectFactory objectFactory;
    
    private ObjectWrapperFactory objectWrapperFactory;
    
    public static MetaObject NULL = new MetaObject(null, null, null);
    
    private MetaObject(Object originObject, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        this.originObject = originObject;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;
        
        if (originObject instanceof ObjectWrapper) {
            this.objectWrapper = (ObjectWrapper) originObject;
        } else if (objectWrapperFactory.hasWrapper(originObject)) {
            this.objectWrapper = objectWrapperFactory.getWrapper(originObject, this);
        } else if (originObject instanceof Map) {
            this.objectWrapper = new MapWrapper((Map<String, Object>) originObject, this);
        } else if (originObject instanceof Collection) {
            this.objectWrapper = new CollectionWrapper((Collection<Object>) originObject, this);
        } else {
            this.objectWrapper = new BeanWrapper(originObject, this);
        }
    }
    
    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        if (object == null) {
            return NULL;
        } else {
            return new MetaObject(object, objectFactory, objectWrapperFactory);
        }
    }
    
    public Object getValue(String tokenName) {
        // tokenName like `users[0].address.city`
        PropertyToken token = new PropertyToken(tokenName);
        if (token.hasChildren()) {
            // metaObject: MetaObject(users[0])
            MetaObject metaObject = getMetaObject(token.getPropName());
            // Suppose accessing `users[0].address.city`. If `users[0]` is NULL, then it makes no sense to continue accessing `address.city`.
            if (metaObject == NULL) {
                return null;
            } else {
                return metaObject.getValue(token.getChildrenTokenValue());
            }
        } else {
            return objectWrapper.get(token);
        }
    }
    
    public void setValue(String tokenValue, Object value) {
        PropertyToken token = new PropertyToken(tokenValue);
        if (token.hasChildren()) {
            MetaObject metaObject = getMetaObject(token.getPropName());
            if (metaObject == NULL) {
                if (value == null && token.getChildrenTokenValue() != null) {
                    return;
                }
                metaObject = objectWrapper.createProperty(token, objectFactory);
            }
            metaObject.setValue(token.getChildrenTokenValue(), value);
        } else {
            objectWrapper.set(token, value);
        }
    }
    
    public MetaObject getMetaObject(Object object) {
        return forObject(object, objectFactory, objectWrapperFactory);
    }
    
    public MetaObject getMetaObject(String name) {
        Object object = getValue(name);
        return getMetaObject(object);
    }
    
    public Object getOriginObject() {
        return originObject;
    }
    
    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }
    
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }
    
    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }
    
    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }
}
