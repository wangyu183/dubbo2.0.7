package com.alibaba.dubbo.common.serialize.support.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.alibaba.dubbo.common.serialize.ObjectInput;

public class JavaObjectInput implements ObjectInput {
    
    public final static int MAX_BYTE_ARRAY_LENGTH = 8 * 1024 * 1024;
    
    private final ObjectInputStream mIn;
    
    public JavaObjectInput(InputStream is) throws IOException{
        mIn = new ObjectInputStream(is);
    }
    
    public JavaObjectInput(InputStream is, boolean compacted) throws IOException{
        mIn = compacted ? new CompactedObjectInputStream(is) : new ObjectInputStream(is);
    }

    public boolean readBool() throws IOException {
        return mIn.readBoolean();
    }

    public byte readByte() throws IOException {
        return mIn.readByte();
    }

    public short readShort() throws IOException {
        return mIn.readShort();
    }

    public int readInt() throws IOException {
        return mIn.readInt();
    }

    public long readLong() throws IOException {
        return mIn.readLong();
    }

    public float readFloat() throws IOException {
        return mIn.readFloat();
    }

    public double readDouble() throws IOException {
        return mIn.readDouble();
    }

    public String readUTF() throws IOException {
        int len = mIn.readInt();
        if( len < 0 )
            return null;

        return mIn.readUTF();
    }

    public byte[] readBytes() throws IOException {
        int len = mIn.readInt();
        if(len < 0) {
            return null;
        }
        if(len == 0) {
            return new byte[0];
        }
        if(len > MAX_BYTE_ARRAY_LENGTH) {
            throw new IOException("Byte array length too large." + len);
        }
        byte[] b = new byte[len];
        mIn.readFully(b);
        return b;
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        byte b = mIn.readByte();
        if( b == 0 )
            return null;

        return mIn.readObject();
    }

    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
        return (T) readObject();
    }

}
