package com.alibaba.dubbo.common.utils;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
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
    
    public static String toString(String msg, Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);
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
    
    public static String getServiceKey(Map<String, String> ps) {
        StringBuilder buf = new StringBuilder();
        String group = ps.get(Constants.GROUP_KEY);
        if (group != null && group.length()>0){
            buf.append(group).append("/");
        }
        buf.append(ps.get(Constants.INTERFACE_KEY));
        String version = ps.get(Constants.VERSION_KEY);
        if (version!= null && version.length()>0){
            buf.append(":").append(version);
        }
        return buf.toString();
    }
}
