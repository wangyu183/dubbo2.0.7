package com.alibaba.dubbo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.dubbo.common.utils.Reference;

public class ExtensionLoader<T> {
	
	private final Class<?> type;
	
	private final Reference<Map<String,Class<?>>> cachedClasses = new Reference<Map<String,Class<?>>>();
	
	private static final ConcurrentMap<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>,ExtensionLoader<?>>(); 
	
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
		
		
	}
	
	
}
