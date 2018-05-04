package com.alibaba.dubbo.common.serialize.support.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;

@Extension("java")
public class JavaSerialization implements Serialization {

    public byte getContentTypeId() {
        return 3;
    }

    public String getContentType() {
        return "x-application/java";
    }

    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        return new JavaObjectOutput(output);
    }

    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        return new JavaObjectInput(input);
    }

}
