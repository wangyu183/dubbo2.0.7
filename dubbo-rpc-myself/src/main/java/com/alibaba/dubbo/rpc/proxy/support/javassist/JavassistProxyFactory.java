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
public class JavassistProxyFactory implements ProxyFactory {

    /**
     * Proxy.getProxy(interfaces) 生成的是创建代理对象的代理
     * Proxy.getProxy(interfaces).newInstance 生成的是代理对象 <a href=
     * "https://blog.csdn.net/quhongwei_zhanqiu/article/details/41597261">举例</a>
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>... types) throws RpcException {
        Class<?>[] interfaces;
        if (types != null && types.length > 0) {
            interfaces = new Class<?>[types.length + 1];
            interfaces[0] = invoker.getInterface();
            System.arraycopy(types, 0, interfaces, 1, types.length);
        } else {
            interfaces = new Class<?>[] { invoker.getInterface() };
        }
        return (T) Proxy.getProxy(interfaces).newInstance(new InvokerHandler(invoker));
    }

    /**
     * 代理对象的包装类
     * <p>
     * <a href=
     * "https://blog.csdn.net/quhongwei_zhanqiu/article/details/41597261">举例</a>
     * </p>
     * 
     * 
     * 
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

//    public Object invokeMethod(Object o, String n, Class[] p, Object[] v)
//            throws java.lang.reflect.InvocationTargetException {
//        com.fqh.arms.template.report.service.ILoanReportDubboService w;
//        try {
//            w = ((com.fqh.arms.template.report.service.ILoanReportDubboService) $1);
//        } catch (Throwable e) {
//            throw new IllegalArgumentException(e);
//        }
//        try {
//            if ("deleteData".equals($2) && $3.length == 1) {
//                return ($w) w.deleteData((com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//            if ("getLoanReportDataByInstenceId".equals($2) && $3.length == 1) {
//                return ($w) w.getLoanReportDataByInstenceId(
//                        (com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//            if ("saveArtificialField".equals($2) && $3.length == 1) {
//                return ($w) w
//                        .saveArtificialField((com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//            if ("getcreditSorceByLoanReportId".equals($2) && $3.length == 1) {
//                return ($w) w.getcreditSorceByLoanReportId((java.lang.String) $4[0]);
//            }
//            if ("selectLoanReportById".equals($2) && $3.length == 1) {
//                return ($w) w.selectLoanReportById((com.fqh.arms.template.report.condition.LoanReportCondition) $4[0]);
//            }
//            if ("getLoanReportByOrderId".equals($2) && $3.length == 1) {
//                return ($w) w.getLoanReportByOrderId((java.lang.String) $4[0]);
//            }
//            if ("getLoanReportDetailData".equals($2) && $3.length == 1) {
//                return ($w) w
//                        .getLoanReportDetailData((com.fqh.arms.template.report.condition.LoanReportCondition) $4[0]);
//            }
//            if ("getLoanReportData".equals($2) && $3.length == 1) {
//                return ($w) w.getLoanReportData((com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//            if ("getModelList".equals($2) && $3.length == 1 && $3[0].getName().equals("java.lang.Object")) {
//                return ($w) w.getModelList((java.lang.Object) $4[0]);
//            }
//            if ("getModelList".equals($2) && $3.length == 1
//                    && $3[0].getName().equals("com.fqh.arms.template.report.condition.LoanReportCondition")) {
//                return ($w) w.getModelList((com.fqh.arms.template.report.condition.LoanReportCondition) $4[0]);
//            }
//            if ("saveLoanReports".equals($2) && $3.length == 1) {
//                return ($w) w.saveLoanReports((com.fqh.arms.template.report.condition.LoanReportCondition) $4[0]);
//            }
//            if ("saveAddField".equals($2) && $3.length == 1) {
//                return ($w) w.saveAddField((com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//            if ("saveThirdField".equals($2) && $3.length == 1) {
//                return ($w) w.saveThirdField((com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//            if ("deleteLoanReport".equals($2) && $3.length == 1) {
//                return ($w) w.deleteLoanReport((com.fqh.arms.template.report.condition.LoanReportCondition) $4[0]);
//            }
//            if ("loanReportAudit".equals($2) && $3.length == 1) {
//                return ($w) w.loanReportAudit((com.fqh.arms.template.report.condition.LoanReportCondition) $4[0]);
//            }
//            if ("setUsedData".equals($2) && $3.length == 1) {
//                return ($w) w.setUsedData((com.fqh.arms.template.report.condition.LoanReportDataCondition) $4[0]);
//            }
//        } catch (Throwable e) {
//            throw new java.lang.reflect.InvocationTargetException(e);
//        }
//        throw new com.alibaba.dubbo.common.bytecode.NoSuchMethodException("Not found method \"" + $2
//                + "\" in class com.fqh.arms.template.report.service.ILoanReportDubboService.");
//    }

}
