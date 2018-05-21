package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcStatus;

@Extension(LeastActiveLoadBalance.NAME)
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "leastactive";
    
    private final Random random = new Random();
    
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation) {
        int length = invokers.size();
        int leastActive = -1;
        int leastCount = 0;
        int [] leastIndexs = new int[length];
        int totalWeight = 0;
        int firstWeight = 0;
        boolean sameWeight = true;
        for(int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive();
            int weight = getWeight(invoker, invocation);
            if(leastActive == -1 || active < leastActive) {
                //重置
                leastActive = active;
                leastCount = 1;
                leastIndexs[0] = i;
                totalWeight = weight;
                firstWeight = weight;
                sameWeight = true;
            }else if(active == leastActive) {
                leastIndexs[leastCount++] = i;
                totalWeight += weight;
                /**当i > 0的invoker的权重和firstWeight的权重不等时，sameWeight = false
                 * 一旦sameWeight = flase; 如果没有再次进行"重置",则sameWeight === false;
                 */
                if(sameWeight && i > 0 && weight != firstWeight) {
                    sameWeight = false;
                }
            }
        }
        if(leastCount == 1) {
            return invokers.get(leastIndexs[0]);
        }
        
        //如果权重不相同且权重 > 0
        if(!sameWeight && totalWeight > 0) {
            int offsetWeight = random.nextInt(totalWeight);
            for(int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexs[i];
                offsetWeight -= getWeight(invokers.get(leastIndex), invocation);
                if(offsetWeight <= 0) {
                    return invokers.get(leastIndex);
                }
            }
        }
        
        //如果权重相同或者totaoWeight = 0
        return invokers.get(leastIndexs[random.nextInt(leastCount)]);
    }

}
