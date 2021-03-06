package com.alibaba.dubbo.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.dubbo.common.utils.CollectionUtils;

public final class URL implements Serializable {

    private static final long serialVersionUID = 5837487850456900161L;

    private final String protocol;

    private final String username;

    private final String password;

    private final String host;

    private final int port;

    private final String path;

    private final Map<String, String> parameters;

    protected URL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String... pairs) {
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String path, String... pairs) {
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String username, String password, String host, int port, String path,
            Map<String, String> parameters) {

        if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url,password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.parameters = Collections.unmodifiableMap(
                parameters != null ? new HashMap<String, String>(parameters) : new HashMap<String, String>(0));
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public String getPath() {
        return path;
    }

    /**
     * absolute 绝对路径
     * 
     * @return
     */
    public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public URL setProtocol(String protocol) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL setUsername(String username) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL setPassword(String password) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL setHost(String host) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL setPort(int port) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL setPath(String path) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public URL addParameterAndEncoded(String key, String value) {
        try {
            value = URLEncoder.encode("value", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return addParameter(key, value);
    }

    public URL addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Enum<?> value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Number value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, CharSequence value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        Map<String,String> map = new HashMap<String,String>(getParameters());
        map.put(key, value);
        return new URL(protocol,username,password,host,port,path,map);
    }
    
    public URL addParameterIfAbsent(String key, Object value) {
        if (key == null || key.length() == 0
                || value == null || String.valueOf(value).length() == 0) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, String.valueOf(value));
        return new URL(protocol, username, password, host, port, path, map);
    }
    
    public URL addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        return new URL(protocol, username, password, host, port, path, map);
    }
    
    public URL addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(getParameters());
        return new URL(protocol, username, password, host, port, path, map);
    }
    
    public URL addParameters(String... pairs) {
        if(pairs == null || pairs.length == 0) {
            return this;
        }
        
        if(pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        
        Map<String,String> map = new HashMap<String,String>();
        int len = pairs.length / 2;
        for(int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }
    
    public URL removeParameter(String key) {
        if(key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }
    
    public URL removeParameters(Collection<String> keys) {
        if(keys == null || keys.size() == 0) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }
    
    public URL removeParameters(String... keys) {
        if(keys == null || keys.length == 0) {
            return this;
        }
        Map<String,String> map = new HashMap<String,String>(getParameters());
        for(String key : keys) {
            map.remove(key);
        }
        if(map.size() == getParameters().size()) {
            return this;
        }
        return new URL(protocol, username, password, host, port, path, map);
    }
    
    public URL cleatParameters() {
        return new URL(protocol, username, password, host, port, path, new HashMap<String, String>());
    }
    
    public static URL valueOf(String url) {
        if(url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String,String> parameters = null;
        int i = url.indexOf("?");
        if(i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String,String>();
            for(String part : parts) {
                part = part.trim();
                if(part.length() > 0) {
                    int j = part.indexOf('=');
                    if( j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    }else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if(i >= 0) {
            if(i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0,i);
            url = url.substring(i + 3);
        }else {
            i = url.indexOf(":/");
            if(i >= 0) {
                if(i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }
        
        i = url.indexOf("/");
        if(i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0,i);
        }
        i = url.indexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if(url.length() > 0) host = url;
        return new URL(protocol, username, password, host, port, path, parameters);
    }
    
    public String toIdentityString() {
        return buildString(false, false); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toFullString() {
        return buildString(true, true);
    }

    public String toParameterString() {
        StringBuilder buf = new StringBuilder();
        if (getParameters().size() > 0) {
            boolean first = true;
            for (Map.Entry<String, String> entry : getParameters().entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
        return buf.toString();
    }
    
    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String toString() {
        return buildString(false, true); // no show username and password
    }
    
    private String buildString(boolean u, boolean p) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (u && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        if(host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (p) {
            if (getParameters().size() > 0) {
                boolean first = true;
                for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                    if (entry.getKey() != null && entry.getKey().length() > 0) {
                        if (first) {
                            buf.append("?");
                            first = false;
                        } else {
                            buf.append("&");
                        }
                        buf.append(entry.getKey());
                        buf.append("=");
                        buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                    }
                }
            }
        }
        return buf.toString();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        result = prime * result
                + ((protocol == null) ? 0 : protocol.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        URL other = (URL) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (port != other.port)
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        return true;
    }

    public String getServiceKey() {
        StringBuilder buf = new StringBuilder();
        String group = getParameter(Constants.GROUP_KEY);
        if (group != null && group.length()>0){
            buf.append(group).append("/");
        }
        buf.append(getParameter(Constants.INTERFACE_KEY, path));
        String version = getParameter(Constants.VERSION_KEY);
        if (version!= null && version.length()>0){
            buf.append(":").append(version);
        }
        return buf.toString();
    }
    

    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public String getParameterAndDecoded(String key) {
        return getParameterAndDecoded(key, null);
    }
    
    public String getParameterAndDecoded(String key, String defaultValue) {
        String value = getParameter(key, defaultValue);
        if (value != null && value.length() > 0) { 
            try {
                value = URLDecoder.decode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return value;
    }

    public String getParameter(String key) {
        String value = parameters.get(key);
        if (value == null || value.length() == 0) {
            value = parameters.get(Constants.HIDE_KEY_PREFIX + key);
        }
        if (value == null || value.length() == 0) {
            value = parameters.get(Constants.DEFAULT_KEY_PREFIX + key);
        }
        if (value == null || value.length() == 0) {
            value = parameters.get(Constants.HIDE_KEY_PREFIX + Constants.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public float getFloatParameter(String key) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Float.parseFloat(value);
    }

    public float getLongParameter(String key, float defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Float.parseFloat(value);
    }

    public double getDoubleParameter(String key) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    public double getDoubleParameter(String key, double defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Double.parseDouble(value);
    }
    
    public long getLongParameter(String key) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public long getLongParameter(String key, long defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }
    
    public int getIntParameter(String key) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public int getIntParameter(String key, int defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
    
    public int getPositiveIntParameter(String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        if (i > 0) {
            return i;
        }
        return defaultValue;
    }

    public boolean getBooleanParameter(String key) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getBooleanParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }
    
    public String getMethodParameter(String method, String key) {
        String value = parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            value = parameters.get(Constants.HIDE_KEY_PREFIX + method + "." + key);
        }
        if (value == null || value.length() == 0) {
            return getParameter(key);
        }
        return value;
    }

    public String getMethodParameter(String method, String key, String defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public int getMethodIntParameter(String method, String key) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public int getMethodIntParameter(String method, String key, int defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public int getMethodPositiveIntParameter(String method, String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        if (i > 0) {
            return i;
        }
        return defaultValue;
    }

    public boolean getMethodBooleanParameter(String method, String key) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getMethodBooleanParameter(String method, String key, boolean defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasMethodParamter(String method, String key) {
        String value = getMethodParameter(method, key);
        return value != null && value.length() > 0;
    }
}
