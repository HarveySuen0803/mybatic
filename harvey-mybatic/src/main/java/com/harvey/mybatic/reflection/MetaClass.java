package com.harvey.mybatic.reflection;

/**
 * @author harvey
 */
public class MetaClass {
    private final Reflector reflector;
    
    private MetaClass(Class<?> clazz) {
        this.reflector = Reflector.forClass(clazz);
    }
    
    public static MetaClass forClass(Class<?> clazz) {
        return new MetaClass(clazz);
    }
    
    public String getPropertyName(String fullName) {
        return buildPropertyName(fullName);
    }
    
    public String getPropertyName(String fullName, boolean useCamelCaseMapping) {
        if (useCamelCaseMapping) {
            fullName.replace("_", "");
        }
        return getPropertyName(fullName);
    }
    
    private String buildPropertyName(String fullName) {
        StringBuilder builder = doBuildPropertyName(fullName, new StringBuilder());
        
        return builder.isEmpty() ? null : builder.toString();
    }
    
    private StringBuilder doBuildPropertyName(String fullName, StringBuilder builder) {
        PropertyToken token = new PropertyToken(fullName);
        
        if (token.hasChildren()) {
            String fieldName = token.getBasePropName();
            String propName = reflector.getPropertyName(fieldName);
            if (propName != null) {
                builder.append(propName);
                builder.append(".");
                Class<?> clazz = reflector.getClass(propName);
                MetaClass metaClass = MetaClass.forClass(clazz);
                metaClass.doBuildPropertyName(propName, builder);
            }
        } else {
            String propName = reflector.getPropertyName(fullName);
            if (propName != null) {
                builder.append(propName);
            }
        }
        
        return builder;
    }
    
    public Invoker getGetterInvoker(String propName) {
        return reflector.getGetterInvoker(propName);
    }
    
    public Invoker getSetterInvoker(String propName) {
        return reflector.getSetterInvoker(propName);
    }
    
    public Class<?> getGetterType(String propName) {
        return reflector.getGetterType(propName);
    }
    
    public Class<?> getSetterType(String propName) {
        return reflector.getSetterType(propName);
    }
}
