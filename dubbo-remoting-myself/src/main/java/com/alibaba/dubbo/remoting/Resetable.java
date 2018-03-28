package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.URL;

public interface Resetable {
    
    void reset(URL url);

}
