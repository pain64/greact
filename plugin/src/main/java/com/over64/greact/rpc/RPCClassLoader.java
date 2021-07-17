package com.over64.greact.rpc;

import java.net.URL;
import java.net.URLClassLoader;

public class RPCClassLoader extends URLClassLoader {
    final ClassLoader parent;
    final String appPackage;

    public RPCClassLoader(ClassLoader parent, URL[] urls, String appPackage) {
        super(urls, null);
        this.parent = parent;
        this.appPackage = appPackage;
    }

    @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(name.startsWith(appPackage)) {
            System.out.println("<GReact RPC> hot load for class: " + name);
            return super.loadClass(name);
        } else
            return parent.loadClass(name);
    }
}