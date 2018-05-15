package com.alibaba.dubbo.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;

public class ConfigUtils {
    
    private ConfigUtils() {}
    
    public static List<String> mergeValues(Class<?> type, String cfg, List<String> def) {
        List<String> defaults = new ArrayList<String>();
        if(def != null) {
            for(String name : def) {
                if(ExtensionLoader.getExtensionLoader(type).hasExtension(name)) {
                    defaults.add(name);
                }
            }
        }
        
        List<String> names = new ArrayList<String>();
        String[] configs = cfg == null ? new String[0] : Constants.COMMA_SPLIT_PATTERN.split(cfg);
        for(String config : configs) {
            if(config != null && config.length() > 0) {
                String[] fs = Constants.COMMA_SPLIT_PATTERN.split(cfg);
                names.addAll(Arrays.asList(fs));
            }
        }
        
        if(!names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {
            int i = names.indexOf(Constants.DEFAULT_KEY);
            if(i > 0) {
                names.addAll(i,defaults);
            }else {
                names.addAll(defaults);
            }
            names.remove(Constants.DEFAULT_KEY);
        }
        for (String name : new ArrayList<String>(names)) {
            if (name.startsWith(Constants.REMOVE_VALUE_PREFIX)) {
                names.remove(name);
                names.remove(name.substring(1));
            }
        }
        return names;
    }
    
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
    
    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0
                || "null".equalsIgnoreCase(value)
                || "false".equalsIgnoreCase(value)
                || "N/A".equalsIgnoreCase(value);
    }
    
    public static boolean isDefault(String value) {
        return "true".equalsIgnoreCase(value)
                || "default".equalsIgnoreCase(value);
    }
}
