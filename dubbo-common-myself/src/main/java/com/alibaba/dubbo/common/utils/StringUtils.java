package com.alibaba.dubbo.common.utils;

import java.io.PrintWriter;
import java.util.Collection;

import com.alibaba.dubbo.common.io.UnsafeStringWriter;

public final class StringUtils {
    
    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }
    
    public static String join(String[] array) {
        if(array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String s : array) {
            sb.append(s);
        }
        return sb.toString();
    }
    
    public static String join(String[] array, char split) {
        if(array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < array.length; i++) {
            if(i > 0) {
                sb.append(split);
            }
            sb.append(split);
        }
        return sb.toString();
    }
    
    public static String join(String[] array, String split){
        if( array.length == 0 ) return "";
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<array.length;i++)
        {
            if( i > 0 )
                sb.append(split);
            sb.append(array[i]);
        }
        return sb.toString();
    }
    
    public static String join(Collection<String> coll, String split) {
        if(coll.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(String s : coll) {
            if(isFirst) {
                isFirst = false;
            }else {
                sb.append(split);
            }
            sb.append(s);
        }
        return sb.toString();
        
    }
}
