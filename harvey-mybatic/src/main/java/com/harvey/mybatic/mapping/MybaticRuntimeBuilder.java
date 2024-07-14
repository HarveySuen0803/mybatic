package com.harvey.mybatic.mapping;

import com.harvey.mybatic.datasource.DataSourceFactory;
import com.harvey.mybatic.support.ResourceUtil;
import com.harvey.mybatic.transaction.TransactionFactory;
import com.harvey.mybatic.type.TypeAliasRegistry;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author harvey
 */
public class MybaticRuntimeBuilder {
    private final MybaticRuntime mybaticRuntime;
    
    private final TypeAliasRegistry typeAliasRegistry;
    
    public MybaticRuntimeBuilder() {
        this.mybaticRuntime = new MybaticRuntime();
        this.typeAliasRegistry = mybaticRuntime.getTypeAliasRegistry();
    }
    
    public MybaticRuntimeBuilder parseConfigXml(String fileName) {
        Reader configXmlReader;
        try {
            configXmlReader = ResourceUtil.getResourceAsReader(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return parseConfigXml(configXmlReader);
    }
    
    public MybaticRuntimeBuilder parseConfigXml(Reader configXmlReader) {
        Element rootEle;
        try {
            rootEle = getRootElement(configXmlReader);
        } catch (DocumentException e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        
        if (rootEle == null) {
            throw new RuntimeException("Not found the root element.");
        }
        
        Element environmentsEle = rootEle.element("environments");
        parseEnvironmentsEle(environmentsEle);
        
        Element mappersEle = rootEle.element("mappers");
        parseMappersEle(mappersEle);
        
        return this;
    }
    
    private void parseEnvironmentsEle(Element environmentsEle) {
        String defaultId = environmentsEle.attributeValue("default");
        List<Element> environmentEleList = environmentsEle.elements("environment");
        for (Element environmentEle : environmentEleList) {
            String id = environmentEle.attributeValue("id");
            if (!defaultId.equals(id)) {
                continue;
            }
            
            // Get TransactionFactory.
            Element transactionManagerEle = environmentEle.element("transactionManager");
            String transactionManagerType = transactionManagerEle.attributeValue("type");
            TransactionFactory transactionFactory;
            try {
                transactionFactory = (TransactionFactory) typeAliasRegistry.resolveAlias(transactionManagerType)
                    .getDeclaredConstructor()
                    .newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            
            // Get DataSourceFactory.
            Element dataSourceEle = environmentEle.element("dataSource");
            String dataSourceType = dataSourceEle.attributeValue("type");
            DataSourceFactory dataSourceFactory;
            try {
                dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(dataSourceType)
                    .getDeclaredConstructor()
                    .newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            
            // Get property of datasource.
            List<Element> propertyEleList = dataSourceEle.elements("property");
            Properties properties = new Properties();
            for (Element propertyEle : propertyEleList) {
                String name = propertyEle.attributeValue("name");
                String value = propertyEle.attributeValue("value");
                properties.setProperty(name, value);
            }
            
            // Get DataSource.
            DataSource dataSource = dataSourceFactory.getDataSource(properties);
            
            // Get Environment.
            Environment environment = new Environment.Builder()
                .id(id)
                .transactionFactory(transactionFactory)
                .dataSource(dataSource)
                .build();
            
            mybaticRuntime.setEnvironment(environment);
        }
    }
    
    private void parseMappersEle(Element mappersEle) {
        List<Element> mapperEleList = mappersEle.elements("mapper");
        
        try {
            for (Element mapperEle : mapperEleList) {
                String resourceAttr = mapperEle.attributeValue("resource");
                Reader sqlMapperXmlReader = ResourceUtil.getResourceAsReader(resourceAttr);
                parseSqlMapperXml(sqlMapperXmlReader);
            }
        } catch (DocumentException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }
    
    private void parseSqlMapperXml(Reader sqlMapperXmlReader) throws ClassNotFoundException, DocumentException {
        Element rootEle = getRootElement(sqlMapperXmlReader);
        
        String namespace = rootEle.attributeValue("namespace");
        Class<?> mapperInterfaceClass = ResourceUtil.classForName(namespace);
        mybaticRuntime.addMapperProxy(mapperInterfaceClass);
        
        addSelectMapper(rootEle);
        addInsertMapper(rootEle);
        addUpdateMapper(rootEle);
        addDeleteMapper(rootEle);
    }
    
    private void addSelectMapper(Element rootEle) {
        String namespace = rootEle.attributeValue("namespace");
        
        List<Element> selectEleList = rootEle.elements("select");
        
        for (Element node : selectEleList) {
            String id = node.attributeValue("id");
            String resultType = node.attributeValue("resultType");
            String parameterType = node.attributeValue("parameterType");
            String sql = node.getText();
            
            Map<Integer, String> parameterMapping = new HashMap<>();
            Pattern pattern = Pattern.compile("(#\\{(.*?)})");
            Matcher matcher = pattern.matcher(sql);
            for (int i = 1; matcher.find(); i++) {
                String g1 = matcher.group(1);
                String g2 = matcher.group(2);
                parameterMapping.put(i, g2);
                sql = sql.replace(g1, "?");
            }
            
            String mappedStatementId = namespace + "." + id;
            String nodeName = node.getName();
            SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
            
            BoundSql boundSql = new BoundSql();
            boundSql.setSql(sql);
            boundSql.setResultType(resultType);
            boundSql.setParameterType(parameterType);
            boundSql.setParameterMapping(parameterMapping);
            
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setId(mappedStatementId);
            mappedStatement.setSqlCommandType(sqlCommandType);
            mappedStatement.setBoundSql(boundSql);
            
            mybaticRuntime.addMappedStatement(mappedStatement);
        }
    }
    
    private void addInsertMapper(Element rootEle) {
    }
    
    private void addUpdateMapper(Element rootEle) {
    }
    
    private void addDeleteMapper(Element rootEle) {
    }
    
    private static Element getRootElement(Reader reader) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Element rootEle = saxReader.read(new InputSource(reader)).getRootElement();
        return rootEle;
    }
    
    public MybaticRuntime build() {
        return mybaticRuntime;
    }
}
