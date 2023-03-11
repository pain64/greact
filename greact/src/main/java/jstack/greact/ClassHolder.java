package jstack.greact;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class ClassHolder {
    public static final Map<String, Pair<Long, Class<?>>> classes = new HashMap<>();
    public static final Context.Key<Object> greactInstanceKey = new Context.Key<>();
}
