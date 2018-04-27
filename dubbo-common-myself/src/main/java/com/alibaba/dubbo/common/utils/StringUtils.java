package com.alibaba.dubbo.common.utils;

import java.io.PrintWriter;

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
    
}
