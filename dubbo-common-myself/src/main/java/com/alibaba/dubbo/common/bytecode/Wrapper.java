package com.alibaba.dubbo.common.bytecode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ReflectUtils;

public abstract class Wrapper {
    private static AtomicLong WRAPPER_CLASS_COUNTER = new AtomicLong(0);

    private static final Map<Class<?>, Wrapper> WRAPPER_MAP = new ConcurrentHashMap<Class<?>, Wrapper>(); //class wrapper map

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String[] OBJECT_METHODS = new String[]{"getClass", "hashCode", "toString", "equals"};
    
    private static final Wrapper OBJECT_WRAPPER = new Wrapper(){
        public String[] getMethodNames(){ return OBJECT_METHODS; }
        public String[] getDeclaredMethodNames(){ return OBJECT_METHODS; }
        public String[] getPropertyNames(){ return EMPTY_STRING_ARRAY; }
        public Class<?> getPropertyType(String pn){ return null; }
        public Object getPropertyValue(Object instance, String pn) throws NoSuchPropertyException{ throw new NoSuchPropertyException("Property [" + pn + "] not found."); }
        public void setPropertyValue(Object instance, String pn, Object pv) throws NoSuchPropertyException{ throw new NoSuchPropertyException("Property [" + pn + "] not found."); }
        public boolean hasProperty(String name){ return false; }
        public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws NoSuchMethodException
        {
            if( "getClass".equals(mn) ) return instance.getClass();
            if( "hashCode".equals(mn) ) return instance.hashCode();
            if( "toString".equals(mn) ) return instance.toString();
            if( "equals".equals(mn) )
            {
                if( args.length == 1 ) return instance.equals(args[0]);
                throw new IllegalArgumentException("Invoke method [" + mn + "] argument number error.");
            }
            throw new NoSuchMethodException("Method [" + mn + "] not found.");
        }
    };    
    
    public static Wrapper getWrapper(Class<?> c) {
     // can not wrapper on dynamic class.
        while(ClassGenerator.isDynamicClass(c)) {
            c = c.getSuperclass();
        }
        if(c == Object.class) {
            return OBJECT_WRAPPER;
        }
        Wrapper ret = WRAPPER_MAP.get(c);
        if(ret == null) {
            ret = makeWrapper(c);
            WRAPPER_MAP.put(c, ret);
        }
        return ret;
    }
    
    private static Wrapper makeWrapper(Class<?> c) {
        if(c.isPrimitive()) {
            throw new IllegalArgumentException("Can not create wrapper for primitive type: " + c);
        }
        String name = c.getName();
        ClassLoader cl = ClassHelper.getClassLoader();
        
        StringBuilder c1 = new StringBuilder("public void setPropertyValue(Object o, String n, Object v){ ");
        StringBuilder c2 = new StringBuilder("public Object getPropertyValue(Object o, String n){ ");
        StringBuilder c3 = new StringBuilder("public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws " + InvocationTargetException.class.getName() + "{ ");

        c1.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c2.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c3.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); } try{");
        
        Map<String, Class<?>> pts = new HashMap<String, Class<?>>(); // <property name, property types>
        Map<String, Method> ms = new LinkedHashMap<String, Method>(); // <method desc, Method instance>
        List<String> mns = new ArrayList<String>(); // method names.
        List<String> dmns = new ArrayList<String>(); // declaring method names.
        
        // get all public field.
        for( Field f : c.getFields() )
        {
            String fn = f.getName();
            Class<?> ft = f.getType();
            if( Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()) )
                continue;

            c1.append(" if( $2.equals(\"").append(fn).append("\") ){ w.").append(fn).append("=").append(arg(ft, "$3")).append("; return; }");
            c2.append(" if( $2.equals(\"").append(fn).append("\") ){ return ($w)w.").append(fn).append("; }");
            pts.put(fn, ft);
        }
        
        Method[] methods = c.getMethods();
        // get all public method.
        for( Method m : methods )
        {
            if( m.getDeclaringClass() == Object.class ) //ignore Object's method.
                continue;

            String mn = m.getName();
            c3.append(" if( \"").append(mn).append("\".equals( $2 ) ");
            
            boolean override = false;
            for( Method m2 : methods ) {
                if (m != m2 && m.getName().equals(m2.getName())) {
                    override = true;
                    break;
                }
            }
            if (override) {
                int len = m.getParameterTypes().length;
                c3.append(" && ").append(" $3.length == ").append(len);
                if (len > 0) {
                    for (int l = 0; l < len; l ++) {
                        c3.append(" && ").append(" $3[").append(l).append("]").append(" == ")
                            .append(m.getParameterTypes()[l].getCanonicalName()).append(".class");
                    }
                }
            }
            
            c3.append(" ) { ");
            
            if( m.getReturnType() == Void.TYPE )
                c3.append(" w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");").append(" return null;");
            else
                c3.append(" return ($w)w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");");

            c3.append(" }");
            
            mns.add(mn);
            if( m.getDeclaringClass() == c )
                dmns.add(mn);
            ms.put(ReflectUtils.getDesc(m), m);
        }
        c3.append(" } catch(Throwable e) { " );
        c3.append("     throw new java.lang.reflect.InvocationTargetException(e); " );
        c3.append(" }");
        c3.append(" throw new " + NoSuchMethodException.class.getName() + "(\"Not found method \\\"\"+$2+\"\\\" in class " + c.getName() + ".\"); }");
        
        // deal with get/set method.
        Matcher matcher;
        for( Map.Entry<String,Method> entry : ms.entrySet() )
        {
            String md = entry.getKey();
            Method method = (Method)entry.getValue();
            if( ( matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(md) ).matches() )
            {
                String pn = propertyName(matcher.group(1));
                c2.append(" if( $2.equals(\"").append(pn).append("\") ){ return ($w)w.").append(method.getName()).append("(); }");
                pts.put(pn, method.getReturnType());
            }
            else if( ( matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(md) ).matches() )
            {
                String pn = propertyName(matcher.group(1));
                c2.append(" if( $2.equals(\"").append(pn).append("\") ){ return ($w)w.").append(method.getName()).append("(); }");
                pts.put(pn, method.getReturnType());
            }
            else if( ( matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(md) ).matches() )
            {
                Class<?> pt = method.getParameterTypes()[0];
                String pn = propertyName(matcher.group(1));
                c1.append(" if( $2.equals(\"").append(pn).append("\") ){ w.").append(method.getName()).append("(").append(arg(pt,"$3")).append("); return; }");
                pts.put(pn, pt);
            }
        }
        c1.append(" throw new " + NoSuchPropertyException.class.getName() + "(\"Not found property \\\"\"+$2+\"\\\" filed or setter method in class " + c.getName() + ".\"); }");
        c2.append(" throw new " + NoSuchPropertyException.class.getName() + "(\"Not found property \\\"\"+$2+\"\\\" filed or setter method in class " + c.getName() + ".\"); }");

        // make class
        long id = WRAPPER_CLASS_COUNTER.getAndIncrement();
        ClassGenerator cc = ClassGenerator.newInstance(cl);
        cc.setClassName( ( Modifier.isPublic(c.getModifiers()) ? Wrapper.class.getName() : c.getName() + "$sw" ) + id );
        cc.setSuperClass(Wrapper.class);

        cc.addDefaultConstructor();
        cc.addField("public static String[] pns;"); // property name array.
        cc.addField("public static " + Map.class.getName() + " pts;"); // property type map.
        cc.addField("public static String[] mns;"); // all method name array.
        cc.addField("public static String[] dmns;"); // declared method name array.
        for(int i=0,len=ms.size();i<len;i++)
            cc.addField("public static Class[] mts" + i + ";");

        cc.addMethod("public String[] getPropertyNames(){ return pns; }");
        cc.addMethod("public boolean hasProperty(String n){ return pts.containsKey($1); }");
        cc.addMethod("public Class getPropertyType(String n){ return (Class)pts.get($1); }");
        cc.addMethod("public String[] getMethodNames(){ return mns; }");
        cc.addMethod("public String[] getDeclaredMethodNames(){ return dmns; }");
        cc.addMethod(c1.toString());
        cc.addMethod(c2.toString());
        cc.addMethod(c3.toString());

        try
        {
            Class<?> wc = cc.toClass();
            // setup static field.
            wc.getField("pts").set(null, pts);
            wc.getField("pns").set(null, pts.keySet().toArray(new String[0]));
            wc.getField("mns").set(null, mns.toArray(new String[0]));
            wc.getField("dmns").set(null, dmns.toArray(new String[0]));
            int ix = 0;
            for( Method m : ms.values() )
                wc.getField("mts" + ix++).set(null, m.getParameterTypes());
            return (Wrapper)wc.newInstance();
        }
        catch(RuntimeException e)
        {
            throw e;
        }
        catch(Throwable e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally
        {
            cc.release();
            ms.clear();
            mns.clear();
            dmns.clear();
        }
    }
    
    private static String arg(Class<?> cl, String name) {
        if( cl.isPrimitive() )
        {
            if( cl == Boolean.TYPE )
                return "((Boolean)" + name + ").booleanValue()";
            if( cl == Byte.TYPE )
                return "((Byte)" + name + ").byteValue()";
            if( cl == Character.TYPE )
                return "((Character)" + name + ").charValue()";
            if( cl == Double.TYPE )
                return "((Number)" + name + ").doubleValue()";
            if( cl == Float.TYPE )
                return "((Number)" + name + ").floatValue()";
            if( cl == Integer.TYPE )
                return "((Number)" + name + ").intValue()";
            if( cl == Long.TYPE )
                return "((Number)" + name + ").longValue()";
            if( cl == Short.TYPE )
                return "((Number)" + name + ").shortValue()";
            throw new RuntimeException("Unknown primitive type: " + cl.getName());
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }
    
    private static String args(Class<?>[] cs,String name)
    {
        int len = cs.length;
        if( len == 0 ) return "";
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<len;i++)
        {
            if( i > 0 )
                sb.append(',');
            sb.append(arg(cs[i],name+"["+i+"]"));
        }
        return sb.toString();
    }
    
    private static String propertyName(String pn)
    {
        return pn.length() == 1 || Character.isLowerCase(pn.charAt(1)) ? Character.toLowerCase(pn.charAt(0)) + pn.substring(1) : pn;
    }
    
    abstract public String[] getMethodNames();
    
    abstract public String[] getDeclaredMethodNames();

    abstract public String[] getPropertyNames();

    abstract public Class<?> getPropertyType(String pn);

    abstract public Object getPropertyValue(Object instance, String pn) throws NoSuchPropertyException, IllegalArgumentException;

    abstract public void setPropertyValue(Object instance, String pn, Object pv) throws NoSuchPropertyException, IllegalArgumentException;
    
    abstract public boolean hasProperty(String name);
    
    abstract public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws NoSuchMethodException, InvocationTargetException;
}
