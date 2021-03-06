/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.registry.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.registry.RegistryFactory;

public class UrlUtils {
    
	public static URL parseURL(String address, Map<String, String> defaults) {
	    if (address == null || address.length() == 0) {
            return null;
        }
	    String url;
	    if (address.indexOf("://") >= 0) {
	        url = address;
	    } else {
	        String[] addresses = Constants.COMMA_SPLIT_PATTERN.split(address);
	        url = addresses[0];
	        if (addresses.length > 1) {
	            StringBuilder backup = new StringBuilder();
	            for (int i = 1; i < addresses.length; i++) {
	                if (i > 1) {
	                    backup.append(",");
	                }
	                backup.append(addresses[i]);
	            }
	            url += "?" + Constants.BACKUP_KEY + "=" + backup.toString();
	        }
	    }
        String defaultProtocol = defaults == null ? null : defaults.get("protocol");
        if (defaultProtocol == null || defaultProtocol.length() == 0) {
            if (ExtensionLoader.getExtensionLoader(RegistryFactory.class).hasExtension("remote")) {
                defaultProtocol = "remote";
            } else {
                defaultProtocol = "dubbo";
            }
        }
        String defaultUsername = defaults == null ? null : defaults.get("username");
        String defaultPassword = defaults == null ? null : defaults.get("password");
        int defaultPort = StringUtils.parseInteger(defaults == null ? null : defaults.get("port"));
        String defaultPath = defaults == null ? null : defaults.get("path");
        Map<String, String> defaultParameters = defaults == null ? null : new HashMap<String, String>(defaults);
        if (defaultParameters != null) {
            defaultParameters.remove("protocol");
            defaultParameters.remove("username");
            defaultParameters.remove("password");
            defaultParameters.remove("host");
            defaultParameters.remove("port");
            defaultParameters.remove("path");
        }
        URL u = URL.valueOf(url);
        boolean changed = false;
        String protocol = u.getProtocol();
        String username = u.getUsername();
        String password = u.getPassword();
        String host = NetUtils.filterLocalHost(u.getHost());
        int port = u.getPort();
        String path = u.getPath();
        Map<String, String> parameters = new HashMap<String, String>(u.getParameters());
        if ((protocol == null || protocol.length() == 0)
                && defaultProtocol != null && defaultProtocol.length() > 0) {
            changed = true;
            protocol = defaultProtocol;
        }
        if ((username == null || username.length() == 0)
                && defaultUsername != null && defaultUsername.length() > 0) {
            changed = true;
            username = defaultUsername;
        }
        if ((password == null || password.length() == 0)
                && defaultPassword != null && defaultPassword.length() > 0) {
            changed = true;
            password = defaultPassword;
        }
        if (NetUtils.isInvalidLocalHost(host)) {
            changed = true;
            host = NetUtils.getLocalHost();
        }
        if (port <= 0) {
            if (defaultPort > 0) {
                changed = true;
                port = defaultPort;
            } else {
                changed = true;
                port = 9090;
            }
        }
        if (path == null || path.length() == 0) {
            if (defaultPath != null && defaultPath.length() > 0) {
                changed = true;
                path = defaultPath;
            }
        }
        if (defaultParameters != null && defaultParameters.size() > 0) {
            for (Map.Entry<String, String> entry : defaultParameters.entrySet()) {
                String key = entry.getKey();
                String defaultValue = entry.getValue();
                if (defaultValue != null && defaultValue.length() > 0) {
                    String value = parameters.get(key);
                    if (value == null || value.length() == 0) {
                        changed = true;
                        parameters.put(key, defaultValue);
                    }
                }
            }
        }
        if (changed) {
            u = new URL(protocol, username, password, host, port, path, parameters);
        }
        return u;
	}

	public static List<URL> parseURLs(String address, Map<String, String> defaults) {
		if (address == null || address.length() == 0) {
    		return null;
    	}
    	String[] addresses = Constants.REGISTRY_SPLIT_PATTERN.split(address);
    	if (addresses == null || addresses.length == 0) {
    		return null;
    	}
    	List<URL> registries = new ArrayList<URL>();
    	for (String addr : addresses) {
    	    registries.add(parseURL(addr, defaults));
    	}
    	return registries;
	}
	
	public static Map<String, Map<String, String>> convertRegister(Map<String, Map<String, String>> register) {
		Map<String, Map<String, String>> newRegister = new HashMap<String, Map<String, String>>();
    	for (Map.Entry<String, Map<String, String>> entry : register.entrySet()) {
    		String serviceName = entry.getKey();
    		Map<String, String> serviceUrls = entry.getValue();
    		if (! serviceName.contains(":") && ! serviceName.contains("/")) {
        		for (Map.Entry<String, String> entry2 : serviceUrls.entrySet()) {
        			String serviceUrl = entry2.getKey();
        			String serviceQuery = entry2.getValue();
        			Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
        			String group = params.get("group");
        			String version = params.get("version");
        			params.remove("group");
        			params.remove("version");
        			String name = serviceName;
        			if (group != null && group.length() > 0) {
        				name = group + "/" + name;
        			}
        			if (version != null && version.length() > 0) {
        				name = name + ":" + version;
        			}
        			Map<String, String> newUrls = newRegister.get(name);
        			if (newUrls == null) {
        				newUrls = new HashMap<String, String>();
        				newRegister.put(name, newUrls);
        			}
        			newUrls.put(serviceUrl, StringUtils.toQueryString(params));
        		}
    		} else {
    			newRegister.put(serviceName, serviceUrls);
    		}
    	}
    	return newRegister;
	}
	
	public static Map<String, String> convertSubscribe(Map<String, String> subscribe) {
		Map<String, String> newSubscribe = new HashMap<String, String>();
    	for (Map.Entry<String, String> entry : subscribe.entrySet()) {
    		String serviceName = entry.getKey();
    		String serviceQuery = entry.getValue();
			if (! serviceName.contains(":") && ! serviceName.contains("/")) {
        		Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
    			String group = params.get("group");
    			String version = params.get("version");
    			params.remove("group");
    			params.remove("version");
    			String name = serviceName;
    			if (group != null && group.length() > 0) {
    				name = group + "/" + name;
    			}
    			if (version != null && version.length() > 0) {
    				name = name + ":" + version;
    			}
    			newSubscribe.put(name, StringUtils.toQueryString(params));
    		} else {
    			newSubscribe.put(serviceName, serviceQuery);
    		}
    	}
    	return newSubscribe;
	}
	
	public static Map<String, Map<String, String>> revertRegister(Map<String, Map<String, String>> register) {
		Map<String, Map<String, String>> newRegister = new HashMap<String, Map<String, String>>();
    	for (Map.Entry<String, Map<String, String>> entry : register.entrySet()) {
    		String serviceName = entry.getKey();
    		Map<String, String> serviceUrls = entry.getValue();
    		if (serviceName.contains(":") || serviceName.contains("/")) {
        		for (Map.Entry<String, String> entry2 : serviceUrls.entrySet()) {
        			String serviceUrl = entry2.getKey();
        			String serviceQuery = entry2.getValue();
        			Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
        			String name = serviceName;
        			int i = name.indexOf('/');
        			if (i >= 0) {
        				params.put("group", name.substring(0, i));
        				name = name.substring(i + 1);
        			}
        			i = name.lastIndexOf(':');
        			if (i >= 0) {
        				params.put("version", name.substring(i + 1));
        				name = name.substring(0, i);
        			}
        			Map<String, String> newUrls = newRegister.get(name);
        			if (newUrls == null) {
        				newUrls = new HashMap<String, String>();
        				newRegister.put(name, newUrls);
        			}
        			newUrls.put(serviceUrl, StringUtils.toQueryString(params));
        		}
    		} else {
    			newRegister.put(serviceName, serviceUrls);
    		}
    	}
    	return newRegister;
	}
	
	public static Map<String, String> revertSubscribe(Map<String, String> subscribe) {
		Map<String, String> newSubscribe = new HashMap<String, String>();
    	for (Map.Entry<String, String> entry : subscribe.entrySet()) {
    		String serviceName = entry.getKey();
    		String serviceQuery = entry.getValue();
    		if (serviceName.contains(":") || serviceName.contains("/")) {
        		Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
        		String name = serviceName;
    			int i = name.indexOf('/');
    			if (i >= 0) {
    				params.put("group", name.substring(0, i));
    				name = name.substring(i + 1);
    			}
    			i = name.lastIndexOf(':');
    			if (i >= 0) {
    				params.put("version", name.substring(i + 1));
    				name = name.substring(0, i);
    			}
    			newSubscribe.put(name, StringUtils.toQueryString(params));
    		} else {
    			newSubscribe.put(serviceName, serviceQuery);
    		}
    	}
    	return newSubscribe;
	}
	
	public static Map<String, Map<String, String>> revertNotify(Map<String, Map<String, String>> notify) {
		if (notify != null && notify.size() > 0) {
    		Map<String, Map<String, String>> newNotify = new HashMap<String, Map<String, String>>();
    		for (Map.Entry<String, Map<String, String>> entry : notify.entrySet()) {
    			String serviceName = entry.getKey();
    			Map<String, String> serviceUrls = entry.getValue();
        		if (! serviceName.contains(":") && ! serviceName.contains("/")) {
	        		if (serviceUrls != null && serviceUrls.size() > 0) {
		        		for (Map.Entry<String, String> entry2 : serviceUrls.entrySet()) {
		        			String url = entry2.getKey();
		        			String query = entry2.getValue();
		        			Map<String, String> params = StringUtils.parseQueryString(query);
		        			String group = params.get("group");
		        			String version = params.get("version");
		        			//params.remove("group");
		        			//params.remove("version");
		        			String name = serviceName;
		        			if (group != null && group.length() > 0) {
		        				name = group + "/" + name;
		        			}
		        			if (version != null && version.length() > 0) {
		        				name = name + ":" + version;
		        			}
		        			Map<String, String> newUrls = newNotify.get(name);
			        		if (newUrls == null) {
			        			newUrls = new HashMap<String, String>();
			        			newNotify.put(name, newUrls);
			        		}
		        			newUrls.put(url, StringUtils.toQueryString(params));
		        		}
	        		}
    			} else {
    				newNotify.put(serviceName, serviceUrls);
    			}
    		}
    		return newNotify;
    	}
        return notify;
	}
	
	public static List<String> revertForbid(List<String> forbid, Set<String> subscribed) {
		if (forbid != null && forbid.size() > 0) {
        	List<String> newForbid = new ArrayList<String>();
        	for (String serviceName : forbid) {
        		if (! serviceName.contains(":") && ! serviceName.contains("/")) {
        			for (String name : subscribed) {
        				if (name.contains(serviceName)) {
        					newForbid.add(name);
        					break;
        				}
        			}
        		} else {
        			newForbid.add(serviceName);
        		}
        	}
        	return newForbid;
        }
		return forbid;
	}

}