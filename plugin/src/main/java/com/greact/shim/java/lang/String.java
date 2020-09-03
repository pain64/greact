package com.greact.shim.java.lang;

import com.greact.model.ErasedMethod;
import com.greact.model.JSCode;
import com.greact.model.NativeMethod;

public abstract class String {

    @NativeMethod("includes({another})")
    abstract boolean contains(CharSequence another);

    @ErasedMethod
    public abstract java.lang.String toString();

    @JSCode(code = "return '' + {i}", isStatic = true)
    abstract java.lang.String valueOf(int i);

    @JSCode(code = "return '' + {l}", isStatic = true)
    abstract java.lang.String valueOf(long l);

    @JSCode(code = "return '' + {o}", isStatic = true)
    abstract java.lang.String valueOf(Object o);

    @JSCode(code = "return '' + {b}", isStatic = true)
    abstract java.lang.String valueOf(boolean b);

    @JSCode(code = "return {c}", isStatic = true)
    abstract java.lang.String valueOf(char c);

    @JSCode(code = "return '' + {f}", isStatic = true)
    abstract java.lang.String valueOf(float f);

    @JSCode(code = "return '' + {d}", isStatic = true)
    abstract java.lang.String valueOf(double d);
}
