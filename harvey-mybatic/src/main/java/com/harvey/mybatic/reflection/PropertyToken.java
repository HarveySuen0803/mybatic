package com.harvey.mybatic.reflection;

/**
 * @author harvey
 */
public class PropertyToken {
    private final String value;
    
    private String basePropName;
    
    private String propName;
    
    private String idx;
    
    private String childrenTokenValue;
    
    public PropertyToken(String value) {
        this.value = value;
        
        // value: users[0].address.city
        int delim1 = value.indexOf(".");
        if (delim1 > -1) {
            // prop: user[0]
            propName = value.substring(0, delim1);
            // children: address.city
            childrenTokenValue = value.substring(delim1 + 1);
        } else {
            propName = value;
            childrenTokenValue = null;
        }
        
        int delim2 = propName.indexOf("[");
        if (delim2 > -1) {
            // baseProp: users
            basePropName = propName.substring(0, delim2);
            idx = propName.substring(delim2, propName.length() - 1);
        }
    }
    
    public String getValue() {
        return value;
    }
    
    public String getBasePropName() {
        return basePropName;
    }
    
    public String getPropName() {
        return propName;
    }
    
    public String getIdx() {
        return idx;
    }
    
    public String getChildrenTokenValue() {
        return childrenTokenValue;
    }
    
    public boolean isContainer() {
        return idx != null;
    }
    
    public boolean hasChildren() {
        return childrenTokenValue != null;
    }
}
