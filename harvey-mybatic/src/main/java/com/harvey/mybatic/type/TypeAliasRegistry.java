package com.harvey.mybatic.type;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author harvey
 */
public class TypeAliasRegistry {
    private final Map<String, Class<?>> typeAliasMap = new HashMap<>();
    
    public TypeAliasRegistry() {
        registerAlias("string", String.class);
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);
    }
    
    public void registerAlias(String alias, Class<?> value) {
        String key = alias.toLowerCase(Locale.ENGLISH);
        typeAliasMap.put(key, value);
    }
    
    public <T> Class<T> resolveAlias(String str) {
        String key = str.toLowerCase(Locale.ENGLISH);
        return (Class<T>) typeAliasMap.get(key);
    }
}
