package com.alibaba.dubbo.common.io;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    private StreamUtils() {}
    
    public static InputStream limitedInputStream(final InputStream is, final int limit) throws IOException{
        return new InputStream() {
            
            private int mPosition = 0;
            
            private int mMark = 0;
            
            private int mLimit = Math.min(limit, is.available());
            
            
            @Override
            public int read() throws IOException {
                if(mPosition < mLimit) {
                    mPosition++;
                    return is.read();
                }
                return -1;
            }
            
            @Override
            public int read(byte[] b, int off, int len) throws IOException{
                if(b == null) {
                    throw new NullPointerException();
                }
                
                if(off < 0 || len < 0 || len > b.length - off) {
                    throw new IndexOutOfBoundsException();
                }
                
                if(mPosition >= mLimit) {
                    return -1;
                }
                if(mPosition + len > mLimit) {
                    len = mLimit - mPosition;
                }
                
                is.read(b, off, len);
                mPosition += len;
                return len;
            }
            
            @Override
            public long skip(long len) throws IOException
            {
                if( mPosition + len > mLimit )
                    len = mLimit - mPosition;

                if( len <= 0 )
                    return 0;

                is.skip(len);
                mPosition += len;
                return len;
            }
            
            @Override
            public int available(){
                return mLimit - mPosition;
            }
            
            @Override
            public boolean markSupported() {
                return is.markSupported();
            }
            
            @Override
            public void mark(int readlimit) {
                is.mark(readlimit);
                mMark = mPosition;
            }
            
            @Override
            public void reset() throws IOException {
                is.reset();
                mPosition = mMark;
            }
            
            @Override
            public void close() throws IOException {}
        };
    }
    
}
