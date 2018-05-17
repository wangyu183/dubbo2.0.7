package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

@Extension(RandomLoadBalance.NAME)
public class RandomLoadBalance extends AbstractLoadBalance {
    
    public static final String NAME = "random";
    
    private final Random random = new Random();
    
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation) {
        int length = invokers.size();
        int totalWeight = 0;
        boolean sameWeight = true;
        for(int i = 0; i < length; i ++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight += weight;
            if(sameWeight && i > 0 && weight != getWeight(invokers.get(i - 1), invocation)) {
                sameWeight = false;
            }
        }
        
        //权重不相等
        if(!sameWeight && totalWeight > 0) {
            int offset = random.nextInt(totalWeight);
            for(int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i), invocation);
                if(offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        //权重相等
        return invokers.get(random.nextInt(length));
    }
    
    public static void main(String[] args) {
        int a = 12;
        a -= 5;
        System.out.println(a);
        Random random = new Random();
        while(true) {
            System.out.println(random.nextInt(2));
        }
    }
    
    

}
