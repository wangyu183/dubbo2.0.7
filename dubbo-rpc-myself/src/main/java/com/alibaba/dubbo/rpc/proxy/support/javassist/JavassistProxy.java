package com.alibaba.dubbo.rpc.proxy.support.javassist;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.bytecode.Proxy;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.proxy.ProxyFactory;
import com.alibaba.dubbo.rpc.proxy.support.InvokerHandler;
import com.alibaba.dubbo.rpc.proxy.support.InvokerWrapper;

@Extension("javassist")
public class JavassistProxy implements ProxyFactory {
    
    /**
     * Proxy.getProxy(interfaces) 生成的是创建代理对象的代理
     * Proxy.getProxy(interfaces).newInstance 生成的是代理对象
     * <a href="https://blog.csdn.net/quhongwei_zhanqiu/article/details/41597261">举例</a>
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>... types) throws RpcException {
        Class<?>[] interfaces;
        if(types != null && types.length > 0) {
            interfaces = new Class<?>[types.length + 1];
            interfaces[0] = invoker.getInterface();
            System.arraycopy(types, 0, interfaces, 1, types.length);
        }else {
            interfaces = new Class<?>[] {invoker.getInterface()};
        }
        return (T) Proxy.getProxy(interfaces).newInstance(new InvokerHandler(invoker));
    }
    
    
    /**
     * 代理对象的包装类
     * <p><a href="https://blog.csdn.net/quhongwei_zhanqiu/article/details/41597261">举例</a></p>
     */
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException {
        final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass());
        return new InvokerWrapper<T>(proxy, type, url) {

            @Override
            protected Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments)
                    throws Throwable {
                return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
            }
        };
    }

}
