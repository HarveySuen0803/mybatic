package com.harvey.mybatic.mapping;

/**
 * @author harvey
 */
public class MybaticRuntimeHolder {
    private static class Holder {
        private static final MybaticRuntime INSTANCE = buildMyBaticRuntime();
        
        private static MybaticRuntime buildMyBaticRuntime() {
            return new MybaticRuntimeBuilder()
                .parseConfigXml("mybatic-config.xml")
                .build();
        }
    }
    
    public static MybaticRuntime getInstance() {
        return Holder.INSTANCE;
    }
}
