package com.greact;

public class ShimLibrary {
    final String searchPackage;
    public final String libraryPackage;

    public ShimLibrary(String searchPackage, String libraryPackage) {
        this.searchPackage = searchPackage;
        this.libraryPackage = libraryPackage;
    }

    String trimPrefix(String packageName) {
        return packageName.substring(libraryPackage.length());
    }

    public Class resolve(String packageName, String className) throws ClassNotFoundException {
        return Class.forName(searchPackage + trimPrefix(packageName) + "." + className);
    }
}
