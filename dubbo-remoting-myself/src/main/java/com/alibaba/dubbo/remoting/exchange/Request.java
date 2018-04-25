package com.alibaba.dubbo.remoting.exchange;

import java.util.concurrent.atomic.AtomicLong;

public class Request {

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);
    
    private final long mId;
    
    private String mVersion;
    
    private boolean mTwoWay = true;
    
    private boolean mHeatbeat = false;
    
    private boolean mBroken = false;
    
    private Object mData;
    
    private static long newId() {
        return INVOKE_ID.getAndIncrement();
    }
    
    public Request() {
        mId = newId();
    }
    
    public Request(long id) {
        mId = id;
    }
    
    public long getId() {
        return mId;
    }
    
    public String getVersion() {
        return mVersion;
    }
    
    public void setVersion(String version) {
        mVersion = version;
    }

    public boolean isTwoWay() {
        return mTwoWay;
    }

    public void setTwoWay(boolean twoWay) {
        mTwoWay = twoWay;
    }
    
    public boolean isHeartbeat() {
        return mHeatbeat;
    }

    public void setHeartbeat(boolean isHeartbeat) {
        this.mHeatbeat = isHeartbeat;
    }

    public boolean isBroken() {
        return mBroken;
    }

    public void setBroken(boolean mBroken) {
        this.mBroken = mBroken;
    }

    public Object getData() {
        return mData;
    }

    public void setData(Object msg) {
        mData = msg;
    }
    
    @Override
    public String toString() {
        return "Request [id=" + mId + ", version=" + mVersion + ", twoway=" + mTwoWay + ", heatbeat=" + mHeatbeat
               + ", broken=" + mBroken + ", data=" + (mData == this ? "this" : mData) + "]";
    }
}
