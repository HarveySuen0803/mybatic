package com.harvey.mybatic.reflection;

import java.util.List;
import java.util.Map;

/**
 * @author harvey
 */
public abstract class BaseObjectWrapper implements ObjectWrapper {
    protected static final Object[] NO_ARGUMENTS = new Object[0];
    
    protected final MetaObject metaObject;
    
    protected BaseObjectWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }
}
