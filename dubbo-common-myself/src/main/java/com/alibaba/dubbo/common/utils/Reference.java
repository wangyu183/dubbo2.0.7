package com.alibaba.dubbo.common.utils;

public class Reference<T> {

	private volatile T value;
	
	public void set(T value){
		this.value = value;
	}
	
	public T get(){
		return value;
	}
}
