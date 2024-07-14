package com.harvey.mybatic.reflection;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author harvey
 */
public class Reflector {
    private static final Map<Class<?>, Reflector> REFLECTOR_CACHE = new ConcurrentHashMap<>();
    
    private Constructor<?> defaultConstructor;
    
    private Class<?> clazz;
    
    private Map<String, Invoker> getterInvokers = new HashMap<>();
    
    private Map<String, Class<?>> getterTypes = new HashMap<>();
    
    private Map<String, Invoker> setterInvokers = new HashMap<>();
    
    private Map<String, Class<?>> setterTypes = new HashMap<>();
    
    private List<String> readableProps = new ArrayList<>();
    
    private List<String> writableProps = new ArrayList<>();
    
    private Map<String, String> caseInsensitivePropsMap = new HashMap<>();
    
    private Reflector(Class<?> clazz) {
        this.clazz = clazz;
        
        addConstructor(clazz);
        
        addGetters(clazz);
        
        addSetters(clazz);
        
        addProps(clazz);
    }
    
    /**
     * Retrieves the Reflector instance for the specified class from the cache.
     * If the Reflector instance does not exist in the cache, a new instance is created,
     * added to the cache, and then returned.
     *
     * @param clazz the class for which the Reflector instance is needed
     * @return the Reflector instance for the specified class
     */
    public static Reflector forClass(Class<?> clazz) {
        Reflector reflector = REFLECTOR_CACHE.get(clazz);
        if (reflector == null) {
            REFLECTOR_CACHE.put(clazz, new Reflector(clazz));
        }
        
        return reflector;
    }
    
    /**
     * Retrieves the standardized property name for a given field name.
     *
     * This method takes a field name as input and converts it to uppercase
     * to perform a case-insensitive lookup in a predefined map of properties.
     * The purpose is to standardize property names by mapping them to a consistent format.
     *
     * @param fieldName the name of the field to be standardized. It is expected to be a non-null string.
     * @return the standardized property name corresponding to the given field name,
     *         or null if the field name does not exist in the map.
     */
    public String getPropertyName(String fieldName) {
        return caseInsensitivePropsMap.get(fieldName.toUpperCase(Locale.ENGLISH));
    }
    
    public Class<?> getClass(String propName) {
        return getterTypes.get(propName);
    }
    
    public Invoker getGetterInvoker(String propName) {
        Invoker invoker = getterInvokers.get(propName);
        if (invoker == null) {
            throw new RuntimeException("There is no getter for property named '" + propName + "' in '" + clazz + "'");
        }
        return invoker;
    }
    
    public Invoker getSetterInvoker(String propName) {
        Invoker invoker = setterInvokers.get(propName);
        if (invoker == null) {
            throw new RuntimeException("There is no setter for property named '" + propName + "' in '" + clazz + "'");
        }
        return invoker;
    }
    
    public Class<?> getGetterType(String propName) {
        Class<?> clazz = getterTypes.get(propName);
        if (clazz == null) {
            throw new RuntimeException("There is no getter for property named '" + propName + "' in '" + clazz + "'");
        }
        return clazz;
    }
    
    public Class<?> getSetterType(String propName) {
        Class<?> clazz = setterTypes.get(propName);
        if (clazz == null) {
            throw new RuntimeException("There is no setter for property named '" + propName + "' in '" + clazz + "'");
        }
        return clazz;
    }
    
    /**
     * Adds the default (no-argument) constructor of the given class to the current instance.
     * This method retrieves all constructors of the class, identifies the default constructor,
     * makes it accessible, and stores it if private methods can be accessed.
     *
     * @param clazz the class from which to retrieve the default constructor
     */
    private void addConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        
        boolean canAccessPrivateMethods = canAccessPrivateMembers();
        
        // Iterate over all constructors to find the default (no-argument) constructor
        for (Constructor<?> constructor : constructors) {
            // Check if the constructor has no parameters (default constructor)
            if (constructor.getParameterTypes().length == 0) {
                // Attempt to make the constructor accessible if private methods can be accessed
                if (canAccessPrivateMethods) {
                    try {
                        // Attempt to set the constructor accessible
                        constructor.setAccessible(true);
                    } catch (SecurityException e) {
                        throw new RuntimeException("Failed to set constructor accessible", e);
                    }
                }
                // Check if private methods can be accessed
                if (constructor.canAccess(null)) {
                    this.defaultConstructor = constructor;
                }
            }
        }
        
        if (this.defaultConstructor == null) {
            throw new RuntimeException("Not found no-argument constructor");
        }
    }
    
    /**
     * Adds getter methods for the specified class to a map of property names and their associated getter methods.
     * This method identifies all getter methods in the class and resolves any conflicts between overloaded getters.
     *
     * @param clazz the class to analyze for getter methods
     */
    private void addGetters(Class<?> clazz) {
        // A map to hold property names and their associated getter methods
        Map<String, List<Method>> conflictingGettersMap = getConflictingGettersMap(clazz);
        
        // Iterate over the map entries to resolve any conflicts between overloaded getter methods
        for (Map.Entry<String, List<Method>> conflictingGettersEntry : conflictingGettersMap.entrySet()) {
            String propName = conflictingGettersEntry.getKey();
            List<Method> conflictingGetters = conflictingGettersEntry.getValue();
            Method getter = resolveGetterConflicts(propName, conflictingGetters);
            addGetter(propName, getter);
        }
    }
    
    /**
     * Creates a map of field names to lists of conflicting getter methods for a given class.
     * This method identifies all getter methods in the class and groups them by their corresponding field names.
     *
     * @param clazz the class to inspect for getter methods
     * @return a map where the key is a field name and the value is a list of getter methods associated with that field
     */
    private Map<String, List<Method>> getConflictingGettersMap(Class<?> clazz) {
        // Initialize a map to hold lists of conflicting getter methods by field name
        Map<String, List<Method>> conflictingGettersMap = new HashMap<>();
        
        List<Method> methods = getMethods(clazz);
        
        // Iterate over all methods to identify getter methods
        for (Method method : methods) {
            if (isGetter(method)) {
                String propName = getPropertyName(method);
                // Add the getter method to the list of methods for the field name in the map
                conflictingGettersMap.computeIfAbsent(propName, n -> new ArrayList<>()).add(method);
            }
        }
        
        return conflictingGettersMap;
    }
    
    /**
     * Checks if a given method is a getter method.
     *
     * This method determines if the provided Method object represents a getter method.
     * A getter method is defined as a method with no parameters and a name that starts
     * with "get" or "is".
     *
     * @param method the Method object to be checked
     * @return true if the method is a getter, false otherwise
     */
    private boolean isGetter(Method method) {
        if (method.getParameters().length == 0) {
            String methodName = method.getName();
            if (methodName.startsWith("get") && methodName.length() > 3) {
                return true;
            } else if (methodName.startsWith("is") && methodName.length() > 2) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Resolves conflicts among multiple getter methods for a given field name by determining the most appropriate getter.
     * This method compares the return types of the conflicting getters and selects the most specific one, ensuring
     * compliance with the JavaBeans specification. If conflicts cannot be resolved, an exception is thrown.
     *
     * @param propName the name of the field for which getter methods are being resolved
     * @param conflictingGetters a list of conflicting getter methods for the specified field
     * @return the most appropriate getter method from the list of conflicting getters
     * @throws RuntimeException if the conflicting getters have incompatible return types
     */
    private static Method resolveGetterConflicts(String propName, List<Method> conflictingGetters) {
        Method getter = conflictingGetters.get(0);
        Class<?> returnType = getter.getReturnType();
        
        // Compare the return types of conflicting getter methods to resolve conflicts
        for (int i = 1; i < conflictingGetters.size(); i++) {
            Method curGetter = conflictingGetters.get(i);
            Class<?> curReturnType = curGetter.getReturnType();
            
            // If the return types are identical, mark as incompatible and break
            if (curReturnType.equals(returnType)) {
                throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                    + propName + " in class " + getter.getDeclaringClass()
                    + ".  This breaks the JavaBeans specification and can cause unpredictable results.");
            }
            
            // If the current return type is assignable from the next return type, update current getter and return type
            if (returnType.isAssignableFrom(curReturnType)) {
                getter = curGetter;
                returnType = curReturnType;
                continue;
            }
            
            // If neither return type is assignable from the other, mark as incompatible and break
            if (!curReturnType.isAssignableFrom(returnType)) {
                throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                    + propName + " in class " + curGetter.getDeclaringClass()
                    + ".  This breaks the JavaBeans specification and can cause unpredictable results.");
            }
        }
        
        return getter;
    }
    
    /**
     * Adds setters for the given class by resolving conflicting setters and adding the valid ones.
     *
     * @param clazz the class to process
     */
    private void addSetters(Class<?> clazz) {
        Map<String, List<Method>> conflictingSettersMap = getConflictingSettersMap(clazz);
        
        for (Map.Entry<String, List<Method>> conflictingSettersEntry : conflictingSettersMap.entrySet()) {
            String propName = conflictingSettersEntry.getKey();
            List<Method> conflictingSetters = conflictingSettersEntry.getValue();
            Method setter = resolveConflictingSetters(propName, conflictingSetters);
            addSetter(propName, setter);
        }
    }
    
    /**
     * Creates a map of field names to lists of conflicting setter methods for a given class.
     * This method identifies all setter methods in the class and groups them by their corresponding field names.
     *
     * @param clazz the class to inspect for setter methods
     * @return a map where the key is a field name and the value is a list of setter methods associated with that field
     */
    private Map<String, List<Method>> getConflictingSettersMap(Class<?> clazz) {
        Map<String, List<Method>> conflictingSettersMap = new HashMap<>();
        
        List<Method> methods = getMethods(clazz);
        
        for (Method method : methods) {
            if (isSetter(method)) {
                String propName = getPropertyName(method);
                conflictingSettersMap.computeIfAbsent(propName, n -> new ArrayList<>()).add(method);
            }
        }
        
        return conflictingSettersMap;
    }
    
    /**
     * Checks if a given method is a setter method.
     *
     * This method determines if the provided Method object represents a setter method.
     * A setter method is defined as a method with no parameters and a name that starts
     * with "set"
     *
     * @param method the Method object to be checked
     * @return true if the method is a setter, false otherwise
     */
    private boolean isSetter(Method method) {
        if (method.getParameters().length == 0) {
            String methodName = method.getName();
            if (methodName.startsWith("set") && methodName.length() > 3) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Resolves conflicts among multiple setter methods for a given field name by determining the most appropriate setter.
     * This method compares the return types of the conflicting setters and selects the most specific one, ensuring
     * compliance with the JavaBeans specification. If conflicts cannot be resolved, an exception is thrown.
     *
     * @param propName the name of the field for which setter methods are being resolved
     * @param conflictingSetters a list of conflicting setter methods for the specified field
     * @return the most appropriate setter method from the list of conflicting setters
     * @throws RuntimeException if the conflicting setters have incompatible return types
     */
    private Method resolveConflictingSetters(String propName, List<Method> conflictingSetters) {
        Method setter = conflictingSetters.get(0);

        Class<?> returnType = getterTypes.get(propName);
        if (returnType == null) {
            throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                + propName + " in class " + setter.getDeclaringClass() + ".  This breaks the JavaBeans " +
                "specification and can cause unpredictable results.");
        }
        
        for (int i = 1; i < conflictingSetters.size(); i++) {
            Method curSetter = conflictingSetters.get(i);
            
            if (curSetter.getParameterCount() != 1) {
                throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                    + propName + " in class " + curSetter.getDeclaringClass() + ".  This breaks the JavaBeans " +
                    "specification and can cause unpredictable results.");
            }
            
            if (curSetter.getReturnType().equals(returnType)) {
                setter = curSetter;
                break;
            }
        }

        return setter;
    }
    
    /**
     * Checks if the given field name is a valid JavaBean field name.
     *
     * This method validates a field name based on specific rules to ensure it adheres to JavaBean naming conventions.
     *
     * @param propName the name of the field to be checked
     * @return true if the field name is valid, false otherwise
     */
    private boolean isValidJavaBeanpropName(String propName) {
        if (propName.startsWith("$")) {
            return false;
        }
        if (propName.equals("serialVersionUID")) {
            return false;
        }
        if (propName.equals("class")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Extracts the field name from a given method.
     *
     * This method determines the field name based on the naming conventions
     * of getter and setter methods in Java. It supports methods starting with
     * "get", "is", and "set", and also handles methods with all lowercase names.
     * The extracted field name is capitalized.
     *
     * @param method the Method object from which to extract the field name
     * @return the extracted and capitalized field name
     * @throws RuntimeException if the method name doesn't start with "is", "get", or "set"
     */
    private String getPropertyName(Method method) {
        String methodName = method.getName();
        
        // Extract the substring after "is", "get" or "set"
        String propName;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            propName = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            propName = methodName.substring(2);
        } else if (methodName.startsWith("set") && methodName.length() > 3) {
            propName = methodName.substring(3);
        } else {
            throw new RuntimeException("Error parsing property name '" + methodName + "'.  Didn't start with 'is', 'get' or 'set'.");
        }
        
        // Capitalize the first letter of the extracted field name
        propName = propName.substring(0, 1).toUpperCase() + propName.substring(1);
        
        return propName;
    }
    
    private void addProps(Class<?> clazz) {
        Field[] props = clazz.getDeclaredFields();
        
        for (Field prop : props) {
            String propName = prop.getName();
            int modifiers = prop.getModifiers();
            if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                if (canAccessPrivateMembers()) {
                    try {
                        prop.setAccessible(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!setterInvokers.containsKey(propName)) {
                    addSetter(propName, prop);
                }
                if (!getterInvokers.containsKey(propName)) {
                    addGetter(propName, prop);
                }
            }
        }
        
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            addProps(superclass);
        }
    }
    
    /**
     * Adds a getter method for the specified field to the internal mappings if the field name is valid.
     *
     * @param propName the name of the field for which the getter method is added
     * @param getter the getter method to be added
     */
    private void addGetter(String propName, Method getter) {
        if (isValidJavaBeanpropName(propName)) {
            getterInvokers.put(propName, new MethodInvoker(getter));
            getterTypes.put(propName, getter.getReturnType());
            readableProps.add(propName);
            caseInsensitivePropsMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }
    
    /**
     * Adds a getter method for the specified prop to the internal mappings if the prop name is valid.
     *
     * @param propName the name of the prop for which the getter method is added
     * @param prop the Field object representing the prop for which the getter method is being added
     */
    private void addGetter(String propName, Field prop) {
        if (isValidJavaBeanpropName(propName)) {
            getterInvokers.put(propName, new GetterInvoker(prop));
            getterTypes.put(propName, prop.getType());
            readableProps.add(propName);
            caseInsensitivePropsMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }
    
    /**
     * Adds a setter method for the specified field to the internal mappings if the field name is valid.
     *
     * @param propName the name of the field for which the setter method is being added
     * @param setter the setter method to be added
     */
    private void addSetter(String propName, Method setter) {
        if (isValidJavaBeanpropName(propName)) {
            setterInvokers.put(propName, new MethodInvoker(setter));
            setterTypes.put(propName, setter.getReturnType());
            writableProps.add(propName);
            caseInsensitivePropsMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }
    
    /**
     * Adds a setter method for the specified field to the internal mappings if the field name is valid.
     *
     * @param propName the name of the field for which the setter method is being added
     * @param prop the Field object representing the field for which the setter method is being added
     */
    private void addSetter(String propName, Field prop) {
        if (isValidJavaBeanpropName(propName)) {
            setterInvokers.put(propName, new SetterInvoker(prop));
            setterTypes.put(propName, prop.getType());
            writableProps.add(propName);
            caseInsensitivePropsMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }
    
    /**
     * Retrieves all unique methods from the specified class, including methods from its superclasses and interfaces.
     *
     * This method collects all declared methods from the given class, its superclasses, and its implemented interfaces,
     * ensuring that each method is unique by using a method identifier.
     *
     * @param clazz the Class object representing the class from which to retrieve methods
     * @return a list of unique Method objects from the specified class
     */
    private List<Method> getMethods(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<>();
        
        // Traverse the class hierarchy to collect declared methods
        List<Method> declaredMethods = new LinkedList<>();
        Class<?> curClass = clazz;
        while (curClass != null) {
            declaredMethods.addAll(Arrays.stream(curClass.getDeclaredMethods()).toList());
            Class<?>[] interfaces = curClass.getInterfaces();
            for (Class<?> interfaze : interfaces) {
                declaredMethods.addAll(Arrays.stream(interfaze.getDeclaredMethods()).toList());
            }
            curClass = curClass.getSuperclass();
        }
        
        // Process each declared method to ensure uniqueness
        for (Method declaredMethod : declaredMethods) {
            // Generate a unique identifier for the method
            String methodId = getMethodId(declaredMethod);
            
            // Add the method to the map if it is not already present
            methods.computeIfAbsent(methodId, id -> {
                // If allowed, set the method accessible even if it is private
                if (canAccessPrivateMembers()) {
                    try {
                        declaredMethod.setAccessible(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return declaredMethod;
            });
        }
        
        return methods.values().stream().toList();
    }
    
    /**
     * Generates a unique identifier for a given method,
     * formatted as: methodName(paramType1, paramType2, ...)#returnType.
     *
     * @param method the Method object for which the identifier is generated
     * @return a unique identifier string for the method
     */
    private String getMethodId(Method method) {
        StringBuilder methodId = new StringBuilder();
        
        String methodName = method.getName();
        methodId.append(methodName);
        
        methodId.append("(");
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            methodId.append(parameterType.getName());
            methodId.append(", ");
        }
        
        methodId.append(")");
        
        methodId.append("#");
        
        Class<?> returnType = method.getReturnType();
        methodId.append(returnType);
        
        return methodId.toString();
    }
    
    /**
     * This method checks if the current Java runtime environment allows
     * access to private methods via reflection.
     *
     * @return true if access to private methods is allowed, false otherwise
     */
    private static boolean canAccessPrivateMembers() {
        try {
            // Create a dummy constructor object for the check
            Constructor<AccessibleObject> constructor = AccessibleObject.class.getDeclaredConstructor();
            
            // Try to set the constructor accessible
            constructor.setAccessible(true);
            
            // Check if we can access the private constructor
            return constructor.canAccess(null);
        } catch (Exception e) {
            // If any exception is thrown, it means we do not have the required access
            return false;
        }
    }
}
