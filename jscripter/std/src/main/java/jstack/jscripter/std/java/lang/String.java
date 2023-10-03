package jstack.jscripter.std.java.lang;


import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Static;

public class String {
    @Static public int length() {
        return JSExpression.of("this.length");
    }

    @Static public boolean isEmpty() {
        return JSExpression.of("this.length === 0");
    }
    @Static public char charAt(int i) { return JSExpression.of("this.charAt(:1)", i); }
    @Static public java.lang.String[] split(java.lang.String regex) {
        return JSExpression.of("this.split(new RegExp(:1))", regex);
    }

    @Static public boolean equals(Object other) {
        return JSExpression.of("this == :1", other);
    }

    // FIXME: overloaded args renaming
    public static java.lang.String valueOf(Object obj) { return "" + obj; }
    public static java.lang.String valueOf(boolean obj) { return "" + obj; }
    public static java.lang.String valueOf(int obj) { return "" + obj; }
    public static java.lang.String valueOf(long obj) { return "" + obj; }
    public static java.lang.String valueOf(float obj) { return "" + obj; }
    public static java.lang.String valueOf(double obj) { return "" + obj; }
    public static java.lang.String valueOf(char obj) { return "" + obj; }

//    @Replace("{this}.matches(new RegExp({regex})) != null")
//    public native boolean matches(java.lang.String regex);
//    @Replace("{this}.replace(new RegExp({regex}), {replacement})")
//    public native java.lang.String replaceFirst(java.lang.String regex, java.lang.String replacement);
//    @Replace("{this}.replaceAll(new RegExp({regex}), {replacement})")
      public native java.lang.String replaceAll(java.lang.String regex, java.lang.String replacement);
//    @Replace("{this}.split(new RegExp({regex}), {limit})")
//    public native java.lang.String[] split(java.lang.String regex, int limit);
//    @Replace("{this}.split(new RegExp({regex}))")
//    public native java.lang.String[] split(java.lang.String regex);
//    @Replace("{this}.localeCompare({anotherString}, undefined, { sensitivity: 'accent' })")
    @Static public boolean equalsIgnoreCase(java.lang.String another) {
        return JSExpression.of("this.localeCompare(:1, undefined, { sensitivity: 'accent' }) === 0", another);
    }
//    @Replace("{this} > s ? 1 : ({this} < s ? -1 : 0)")
//    public native int compareTo(java.lang.String s);

    public native int codePointAt(int index);
    public native boolean startsWith(java.lang.String prefix, int toffset);
    public native boolean startsWith(java.lang.String prefix);
    public native boolean endsWith(java.lang.String suffix);
    public native int indexOf(java.lang.String str);
    public native int indexOf(java.lang.String str, int fromIndex);
    public native int lastIndexOf(java.lang.String str);
    public native int lastIndexOf(java.lang.String str, int fromIndex);
    public native java.lang.String substring(int beginIndex);
    public native java.lang.String substring(int beginIndex, int endIndex);
    public native java.lang.String concat(java.lang.String str);
    public native java.lang.String toLowerCase();
    public native java.lang.String toUpperCase();
    public native java.lang.String trim();

//    // Unsupported API
//    @Replace("''") public String() { }
//    @Replace("'' + {original}") public String(java.lang.String original) { }
//    @UseJsApi public String(char[] value) { }
//    @UseJsApi public String(char[] value, int offset, int count) { }
//    @UseJsApi public String(int[] codePoints, int offset, int count) { }
//    @UseJsApi public String(byte[] ascii, int hibyte, int offset, int count) { }
//    @UseJsApi public String(byte[] ascii, int hibyte) { }
//    @UseJsApi public String(byte[] bytes, int offset, int length, java.lang.String charsetName) throws UnsupportedEncodingException { }
//    @UseJsApi public String(byte[] bytes, int offset, int length, Charset charset) { }
//    @UseJsApi public String(byte[] bytes, java.lang.String charsetName) throws UnsupportedEncodingException { }
//    @UseJsApi public String(byte[] bytes, Charset charset) { }
//    @UseJsApi public String(byte[] bytes, int offset, int length) { }
//    @UseJsApi public String(byte[] bytes) { }
//    @UseJsApi public String(StringBuffer buffer) { }
//    @UseJsApi public String(StringBuilder builder) { }
//    @UseJsApi public native char charAt(int index);
//    @UseJsApi public native int codePointBefore(int index);
//    @UseJsApi public native int codePointCount(int beginIndex, int endIndex);
//    @UseJsApi public native int offsetByCodePoints(int index, int codePointOffset);
//    @UseJsApi public native void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);
//    @UseJsApi public native void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin);
//    @UseJsApi public native byte[] getBytes(java.lang.String charsetName) throws UnsupportedEncodingException;
//    @UseJsApi public native byte[] getBytes(Charset charset);
//    @UseJsApi public native byte[] getBytes();
//    @UseJsApi public native boolean contentEquals(StringBuffer sb);
//    @UseJsApi public native boolean contentEquals(CharSequence cs);
//    @UseJsApi public native int compareToIgnoreCase(java.lang.String str);
//    @UseJsApi public native boolean regionMatches(int toffset, java.lang.String other, int ooffset, int len);
//    @UseJsApi public native boolean regionMatches(boolean ignoreCase, int toffset, java.lang.String other, int ooffset, int len);
//    @UseJsApi public native int indexOf(int ch);
//    @UseJsApi public native int indexOf(int ch, int fromIndex);
//    @UseJsApi public native int lastIndexOf(int ch);
//    @UseJsApi public native int lastIndexOf(int ch, int fromIndex);
//    @UseJsApi public native CharSequence subSequence(int beginIndex, int endIndex);
//    @UseJsApi public native java.lang.String replace(char oldChar, char newChar);
//    @UseJsApi public native boolean contains(CharSequence s);
//    @UseJsApi public native java.lang.String replace(CharSequence target, CharSequence replacement);
//    @UseJsApi public native java.lang.String toLowerCase(Locale locale);
//    @UseJsApi public native java.lang.String toUpperCase(Locale locale);
//    @UseJsApi public native java.lang.String toString(); // erased call
//    @UseJsApi public native java.lang.String repeat(int count);
//    @UseJsApi public native java.lang.String indent(int n);
//    @UseJsApi public native java.lang.String stripIndent();
//    @UseJsApi public native java.lang.String translateEscapes();
//    @UseJsApi public native <R> R transform(Function<? super java.lang.String, ? extends R> f);
//    @UseJsApi public native IntStream chars();
//    @UseJsApi public native IntStream codePoints();
//    @UseJsApi public native char[] toCharArray();
//    @UseJsApi public native java.lang.String formatted(Object... args);
//    @UseJsApi public native java.lang.String intern();
//    @UseJsApi public native Optional<java.lang.String> describeConstable();
//    @UseJsApi public native java.lang.String resolveConstantDesc(MethodHandles.Lookup lookup);
//    @UseJsApi public native java.lang.String strip();
//    @UseJsApi public native java.lang.String stripLeading();
//    @UseJsApi public native java.lang.String stripTrailing();
//    @UseJsApi public native boolean isBlank();
//    @UseJsApi public native Stream<java.lang.String> lines();
//    @UseJsApi public static native java.lang.String join(CharSequence delimiter, CharSequence... elements);
//    @UseJsApi public static native java.lang.String join(CharSequence delimiter, Iterable<? extends CharSequence> elements);
//    @UseJsApi public static native java.lang.String format(java.lang.String format, Object... args);
//    @UseJsApi public static native java.lang.String format(Locale l, java.lang.String format, Object... args);
//    @UseJsApi public static native java.lang.String valueOf(char[] data);
//    @UseJsApi public static native java.lang.String valueOf(char[] data, int offset, int count);
//    @UseJsApi public static native java.lang.String copyValueOf(char[] data, int offset, int count);
//    @UseJsApi public static native java.lang.String copyValueOf(char[] data);
//    @UseJsApi public static native java.lang.String valueOf(char c);
//    @UseJsApi public native int hashCode();
}
