package com.alibaba.dubbo.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.alibaba.dubbo.common.utils.Reference;

public class ExtensionLoader<T> {
    
    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    
    //for example "wangyu1,wangyu2";"wangyu1 , wangyu2" "wangyu1      ,      wangyu2"
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
	
	private final Class<?> type;
	
	private final Reference<Map<String,Class<?>>> cachedClasses = new Reference<Map<String,Class<?>>>();
	
	private final ConcurrentMap<String,Reference<Object>> cachedInstances = new ConcurrentHashMap<String,Reference<Object>>();
	
	private static final ConcurrentMap<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>,ExtensionLoader<?>>(); 
	
	private volatile Class<?> cachedAdaptiveClass = null;
	
	private Set<Class<?>> cachedWrapperClasses;
	
	private String cachedDefaultName;
	
	@SuppressWarnings("unchecked")
	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type){
		if(type == null){
			throw new IllegalArgumentException("Extension type == null");
		}
		ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		if(loader == null){
			//putIfAbsent() 当key不存在时就放入，返回值为newValue;当key存在时就不放入，返回值为oldValue
			EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type) );
			loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		}
		return loader;
	}
	
	private ExtensionLoader(Class<?> type){
		this.type = type;
	}
	
	public T getExtension(String name){
		if(name == null || name.length() == 0){
			throw new IllegalArgumentException("Extension name == null");
		}
	
		Reference<Object> reference = cachedInstances.get(name);
		if(reference == null) {
		    cachedInstances.putIfAbsent(name, new Reference<Object>());
		    reference = cachedInstances.get(name);
		}
		Object instance = reference.get();
		if(instance == null) {
		    synchronized(reference) {
		        instance = reference.get();
		        if(instance == null) {
		            //TODO 创建instance
		            
		            reference.set(instance);
		        }
		    }
		}        
		return (T)instance;
	}
	
	
	
	private T createExtension(String name) {
	    Class<?> clazz = getE
	    
	    return T;
	}
	
	
	private Map<String,Class<?>> getExtensionClasses(){
	    Map<String,Class<?>> classes = cachedClasses.get();
	    if(classes == null) {
	        synchronized (cachedClasses) {
	            classes = cachedClasses.get();
	            if(classes == null) {
	                classes = loadExtensionClasses();
	                cachedClasses.set(classes);
	            }
            }
	    }
	}
	
	private Map<String,Class<?>> loadExtensionClasses(){
	    final Extension defaultAnnotation = type.getAnnotation(Extension.class);
	    if(defaultAnnotation != null) {
	        String[] names = NAME_SEPARATOR.split(defaultAnnotation.value());
	        if(names.length > 1) {
	            throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                + ": " + Arrays.toString(names));
	        }
	        if(names.length == 1) {
	            cachedDefaultName = names[0];
	        }
	        ClassLoader classLoader = findClassLoader();
	        Map<String,Class<?>> extensionClasses = new HashMap<String,Class<?>>();
	        String fileName = null;
	        try {
    	        fileName = SERVICES_DIRECTORY + type.getName();
    	        Enumeration<java.net.URL> urls;
    	        if(classLoader != null) {
                    urls = classLoader.getResources(fileName);
                }else {
                    urls = ClassLoader.getSystemResources(fileName);
                } 
    	        
    	        if(urls != null) {
    	            while(urls.hasMoreElements()) {
    	                URL url = urls.nextElement();
    	                
    	                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    	                
    	                String line = null;
    	                while((line = reader.readLine()) != null) {
    	                    line = line.trim();
    	                    if(line.length() > 0) {
    	                        try {
                                    Class<?> clazz = Class.forName(line, true, classLoader);
                                    //superClass.isAssignableFrom(childClass) 属于 Class.java。它的对象和参数都是类，意思是“父类（或接口类）判断给定类是否是它本身或其子类”。
                                    if(!type.isAssignableFrom(clazz)) {
                                        throw new IllegalStateException("Error when load extension class(interface: " +
                                                type + ", class line: " + clazz.getName() + "), class " 
                                                + clazz.getName() + "is not subtype of interface.");
                                    }
                                    //boolean isAnnotationPresent(java.lang.Class annotationClass) 判断注解类是不是当前类的注解
                                    if(clazz.isAnnotationPresent(Adaptive.class)) {
                                        if(cachedAdaptiveClass == null) {
                                            cachedAdaptiveClass = clazz;
                                        }else if(! cachedAdaptiveClass.equals(clazz)) {
                                            throw new IllegalStateException("More than 1 adaptive class found: "
                                                    + cachedAdaptiveClass.getClass().getName()
                                                    + ", " + clazz.getClass().getName());
                                        }
                                    }else {
                                        try {
                                            clazz.getConstructor(type);
                                            Set<Class<?>> autoproxies = cachedWrapperClasses;
                                            if(autoproxies == null) {
                                                cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                                                autoproxies = cachedWrapperClasses;
                                            }
                                            
                                        } catch (NoSuchMethodException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        } catch (SecurityException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }
    	                        } catch (ClassNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
    	                    }
    	                }
    	            }
    	        }
	        }
	        catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	    }
	    
	    
	    return null;
	}
	
	private static ClassLoader findClassLoader() {
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    if(classLoader != null) {
	        return classLoader;
	    }
	    classLoader = ExtensionLoader.class.getClassLoader();
	    return classLoader;
	}
	
}
