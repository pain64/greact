package org.over64.jscripter.std.java.lang;

import com.greact.model.JSExpression;

public class Integer {
    public static java.lang.Integer valueOf(java.lang.String str) throws NumberFormatException {
        return JSExpression.of("parseInt(str)");
    }
    public static int parseInt(java.lang.String str) throws NumberFormatException {
        return JSExpression.of("parseInt(str)");
    }
//    public static final int MIN_VALUE = -2147483648;
//    public static final int MAX_VALUE = 2147483647;
//
//    public static java.org.over64.jscripter.std.java.lang.String toString(int i, int radix) {
//        if (radix < 2 || radix > 36) {
//            radix = 10;
//        }
//
//        if (radix == 10) {
//            return toString(i);
//        } else if (!java.org.over64.jscripter.std.java.lang.String.COMPACT_STRINGS) {
//            return toStringUTF16(i, radix);
//        } else {
//            byte[] buf = new byte[33];
//            boolean negative = i < 0;
//            int charPos = 32;
//            if (!negative) {
//                i = -i;
//            }
//
//            while(i <= -radix) {
//                buf[charPos--] = (byte)digits[-(i % radix)];
//                i /= radix;
//            }
//
//            buf[charPos] = (byte)digits[-i];
//            if (negative) {
//                --charPos;
//                buf[charPos] = 45;
//            }
//
//            return StringLatin1.newString(buf, charPos, 33 - charPos);
//        }
//    }
//
//
//    public static java.org.over64.jscripter.std.java.lang.String toUnsignedString(int i, int radix) {
//        return Long.toUnsignedString(toUnsignedLong(i), radix);
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.String toHexString(int i) {
//        return toUnsignedString0(i, 4);
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.String toOctalString(int i) {
//        return toUnsignedString0(i, 3);
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.String toBinaryString(int i) {
//        return toUnsignedString0(i, 1);
//    }
//
//
//
//
//
//
//
//    @HotSpotIntrinsicCandidate
//    public static java.org.over64.jscripter.std.java.lang.String toString(int i) {
//        int size = stringSize(i);
//        byte[] buf;
//        if (java.org.over64.jscripter.std.java.lang.String.COMPACT_STRINGS) {
//            buf = new byte[size];
//            getChars(i, size, buf);
//            return new java.org.over64.jscripter.std.java.lang.String(buf, (byte)0);
//        } else {
//            buf = new byte[size * 2];
//            StringUTF16.getChars(i, size, buf);
//            return new java.org.over64.jscripter.std.java.lang.String(buf, (byte)1);
//        }
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.String toUnsignedString(int i) {
//        return Long.toString(toUnsignedLong(i));
//    }
//
//    public static int parseInt(java.org.over64.jscripter.std.java.lang.String s, int radix) throws NumberFormatException {
//        if (s == null) {
//            throw new NumberFormatException("null");
//        } else if (radix < 2) {
//            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
//        } else if (radix > 36) {
//            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
//        } else {
//            boolean negative = false;
//            int i = 0;
//            int len = s.length();
//            int limit = -2147483647;
//            if (len <= 0) {
//                throw NumberFormatException.forInputString(s, radix);
//            } else {
//                char firstChar = s.charAt(0);
//                if (firstChar < '0') {
//                    if (firstChar == '-') {
//                        negative = true;
//                        limit = -2147483648;
//                    } else if (firstChar != '+') {
//                        throw NumberFormatException.forInputString(s, radix);
//                    }
//
//                    if (len == 1) {
//                        throw NumberFormatException.forInputString(s, radix);
//                    }
//
//                    ++i;
//                }
//
//                int multmin = limit / radix;
//
//                int result;
//                int digit;
//                for(result = 0; i < len; result -= digit) {
//                    digit = Character.digit(s.charAt(i++), radix);
//                    if (digit < 0 || result < multmin) {
//                        throw NumberFormatException.forInputString(s, radix);
//                    }
//
//                    result *= radix;
//                    if (result < limit + digit) {
//                        throw NumberFormatException.forInputString(s, radix);
//                    }
//                }
//
//                return negative ? result : -result;
//            }
//        }
//    }
//
//    public static int parseInt(CharSequence s, int beginIndex, int endIndex, int radix) throws NumberFormatException {
//        Objects.requireNonNull(s);
//        if (beginIndex >= 0 && beginIndex <= endIndex && endIndex <= s.length()) {
//            if (radix < 2) {
//                throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
//            } else if (radix > 36) {
//                throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
//            } else {
//                boolean negative = false;
//                int i = beginIndex;
//                int limit = -2147483647;
//                if (beginIndex >= endIndex) {
//                    throw NumberFormatException.forInputString("", radix);
//                } else {
//                    char firstChar = s.charAt(beginIndex);
//                    if (firstChar < '0') {
//                        if (firstChar == '-') {
//                            negative = true;
//                            limit = -2147483648;
//                        } else if (firstChar != '+') {
//                            throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, beginIndex);
//                        }
//
//                        i = beginIndex + 1;
//                        if (i == endIndex) {
//                            throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
//                        }
//                    }
//
//                    int multmin = limit / radix;
//
//                    int result;
//                    int digit;
//                    for(result = 0; i < endIndex; result -= digit) {
//                        digit = Character.digit(s.charAt(i), radix);
//                        if (digit < 0 || result < multmin) {
//                            throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
//                        }
//
//                        result *= radix;
//                        if (result < limit + digit) {
//                            throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
//                        }
//
//                        ++i;
//                    }
//
//                    return negative ? result : -result;
//                }
//            }
//        } else {
//            throw new IndexOutOfBoundsException();
//        }
//    }
//
//    public static int parseInt(java.org.over64.jscripter.std.java.lang.String s) throws NumberFormatException {
//        return parseInt(s, 10);
//    }
//
//    public static int parseUnsignedInt(java.org.over64.jscripter.std.java.lang.String s, int radix) throws NumberFormatException {
//        if (s == null) {
//            throw new NumberFormatException("null");
//        } else {
//            int len = s.length();
//            if (len > 0) {
//                char firstChar = s.charAt(0);
//                if (firstChar == '-') {
//                    throw new NumberFormatException(java.org.over64.jscripter.std.java.lang.String.format("Illegal leading minus sign on unsigned string %s.", s));
//                } else if (len <= 5 || radix == 10 && len <= 9) {
//                    return parseInt(s, radix);
//                } else {
//                    long ell = Long.parseLong(s, radix);
//                    if ((ell & -4294967296L) == 0L) {
//                        return (int)ell;
//                    } else {
//                        throw new NumberFormatException(java.org.over64.jscripter.std.java.lang.String.format("String value %s exceeds range of unsigned int.", s));
//                    }
//                }
//            } else {
//                throw NumberFormatException.forInputString(s, radix);
//            }
//        }
//    }
//
//    public static int parseUnsignedInt(CharSequence s, int beginIndex, int endIndex, int radix) throws NumberFormatException {
//        Objects.requireNonNull(s);
//        if (beginIndex >= 0 && beginIndex <= endIndex && endIndex <= s.length()) {
//            int len = endIndex - beginIndex;
//            if (len > 0) {
//                char firstChar = s.charAt(beginIndex);
//                if (firstChar == '-') {
//                    throw new NumberFormatException(java.org.over64.jscripter.std.java.lang.String.format("Illegal leading minus sign on unsigned string %s.", s));
//                } else if (len <= 5 || radix == 10 && len <= 9) {
//                    return parseInt(s, beginIndex, beginIndex + len, radix);
//                } else {
//                    long ell = Long.parseLong(s, beginIndex, beginIndex + len, radix);
//                    if ((ell & -4294967296L) == 0L) {
//                        return (int)ell;
//                    } else {
//                        throw new NumberFormatException(java.org.over64.jscripter.std.java.lang.String.format("String value %s exceeds range of unsigned int.", s));
//                    }
//                }
//            } else {
//                throw new NumberFormatException("");
//            }
//        } else {
//            throw new IndexOutOfBoundsException();
//        }
//    }
//
//    public static int parseUnsignedInt(java.org.over64.jscripter.std.java.lang.String s) throws NumberFormatException {
//        return parseUnsignedInt(s, 10);
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.Integer valueOf(java.org.over64.jscripter.std.java.lang.String s, int radix) throws NumberFormatException {
//        return parseInt(s, radix);
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.Integer valueOf(java.org.over64.jscripter.std.java.lang.String s) throws NumberFormatException {
//        return parseInt(s, 10);
//    }
//
//    @HotSpotIntrinsicCandidate
//    public static java.org.over64.jscripter.std.java.lang.Integer valueOf(int i) {
//        return i >= -128 && i <= java.org.over64.jscripter.std.java.lang.Integer.IntegerCache.high ? java.org.over64.jscripter.std.java.lang.Integer.IntegerCache.cache[i + 128] : new java.org.over64.jscripter.std.java.lang.Integer(i);
//    }
//
//    /** @deprecated */
//    @Deprecated(
//        since = "9"
//    )
//    public Integer(int value) {
//        this.value = value;
//    }
//
//    /** @deprecated */
//    @Deprecated(
//        since = "9"
//    )
//    public Integer(java.org.over64.jscripter.std.java.lang.String s) throws NumberFormatException {
//        this.value = parseInt(s, 10);
//    }
//
//    public byte byteValue() {
//        return (byte)this.value;
//    }
//
//    public short shortValue() {
//        return (short)this.value;
//    }
//
//    @HotSpotIntrinsicCandidate
//    public int intValue() {
//        return this.value;
//    }
//
//    public long longValue() {
//        return (long)this.value;
//    }
//
//    public float floatValue() {
//        return (float)this.value;
//    }
//
//    public double doubleValue() {
//        return (double)this.value;
//    }
//
//    public java.org.over64.jscripter.std.java.lang.String toString() {
//        return toString(this.value);
//    }
//
//    public int hashCode() {
//        return hashCode(this.value);
//    }
//
//    public static int hashCode(int value) {
//        return value;
//    }
//
//    public boolean equals(Object obj) {
//        if (obj instanceof java.org.over64.jscripter.std.java.lang.Integer) {
//            return this.value == (java.org.over64.jscripter.std.java.lang.Integer)obj;
//        } else {
//            return false;
//        }
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.Integer getInteger(java.org.over64.jscripter.std.java.lang.String nm) {
//        return getInteger(nm, (java.org.over64.jscripter.std.java.lang.Integer)null);
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.Integer getInteger(java.org.over64.jscripter.std.java.lang.String nm, int val) {
//        java.org.over64.jscripter.std.java.lang.Integer result = getInteger(nm, (java.org.over64.jscripter.std.java.lang.Integer)null);
//        return result == null ? val : result;
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.Integer getInteger(java.org.over64.jscripter.std.java.lang.String nm, java.org.over64.jscripter.std.java.lang.Integer val) {
//        java.org.over64.jscripter.std.java.lang.String v = null;
//
//        try {
//            v = System.getProperty(nm);
//        } catch (NullPointerException | IllegalArgumentException var4) {
//        }
//
//        if (v != null) {
//            try {
//                return decode(v);
//            } catch (NumberFormatException var5) {
//            }
//        }
//
//        return val;
//    }
//
//    public static java.org.over64.jscripter.std.java.lang.Integer decode(java.org.over64.jscripter.std.java.lang.String nm) throws NumberFormatException {
//        int radix = 10;
//        int index = 0;
//        boolean negative = false;
//        if (nm.isEmpty()) {
//            throw new NumberFormatException("Zero length string");
//        } else {
//            char firstChar = nm.charAt(0);
//            if (firstChar == '-') {
//                negative = true;
//                ++index;
//            } else if (firstChar == '+') {
//                ++index;
//            }
//
//            if (!nm.startsWith("0x", index) && !nm.startsWith("0X", index)) {
//                if (nm.startsWith("#", index)) {
//                    ++index;
//                    radix = 16;
//                } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
//                    ++index;
//                    radix = 8;
//                }
//            } else {
//                index += 2;
//                radix = 16;
//            }
//
//            if (!nm.startsWith("-", index) && !nm.startsWith("+", index)) {
//                java.org.over64.jscripter.std.java.lang.Integer result;
//                try {
//                    result = valueOf(nm.substring(index), radix);
//                    result = negative ? -result : result;
//                } catch (NumberFormatException var8) {
//                    java.org.over64.jscripter.std.java.lang.String constant = negative ? "-" + nm.substring(index) : nm.substring(index);
//                    result = valueOf(constant, radix);
//                }
//
//                return result;
//            } else {
//                throw new NumberFormatException("Sign character in wrong position");
//            }
//        }
//    }
//
//    public int compareTo(java.org.over64.jscripter.std.java.lang.Integer anotherInteger) {
//        return compare(this.value, anotherInteger.value);
//    }
//
//    public static int compare(int x, int y) {
//        return x < y ? -1 : (x == y ? 0 : 1);
//    }
//
//    public static int compareUnsigned(int x, int y) {
//        return compare(x + -2147483648, y + -2147483648);
//    }
//
//    public static long toUnsignedLong(int x) {
//        return (long)x & 4294967295L;
//    }
//
//    public static int divideUnsigned(int dividend, int divisor) {
//        return (int)(toUnsignedLong(dividend) / toUnsignedLong(divisor));
//    }
//
//    public static int remainderUnsigned(int dividend, int divisor) {
//        return (int)(toUnsignedLong(dividend) % toUnsignedLong(divisor));
//    }
//
//    public static int highestOneBit(int i) {
//        return i & -2147483648 >>> numberOfLeadingZeros(i);
//    }
//
//    public static int lowestOneBit(int i) {
//        return i & -i;
//    }
//
//    @HotSpotIntrinsicCandidate
//    public static int numberOfLeadingZeros(int i) {
//        if (i <= 0) {
//            return i == 0 ? 32 : 0;
//        } else {
//            int n = 31;
//            if (i >= 65536) {
//                n -= 16;
//                i >>>= 16;
//            }
//
//            if (i >= 256) {
//                n -= 8;
//                i >>>= 8;
//            }
//
//            if (i >= 16) {
//                n -= 4;
//                i >>>= 4;
//            }
//
//            if (i >= 4) {
//                n -= 2;
//                i >>>= 2;
//            }
//
//            return n - (i >>> 1);
//        }
//    }
//
//    @HotSpotIntrinsicCandidate
//    public static int numberOfTrailingZeros(int i) {
//        i = ~i & i - 1;
//        if (i <= 0) {
//            return i & 32;
//        } else {
//            int n = 1;
//            if (i > 65536) {
//                n += 16;
//                i >>>= 16;
//            }
//
//            if (i > 256) {
//                n += 8;
//                i >>>= 8;
//            }
//
//            if (i > 16) {
//                n += 4;
//                i >>>= 4;
//            }
//
//            if (i > 4) {
//                n += 2;
//                i >>>= 2;
//            }
//
//            return n + (i >>> 1);
//        }
//    }
//
//    @HotSpotIntrinsicCandidate
//    public static int bitCount(int i) {
//        i -= i >>> 1 & 1431655765;
//        i = (i & 858993459) + (i >>> 2 & 858993459);
//        i = i + (i >>> 4) & 252645135;
//        i += i >>> 8;
//        i += i >>> 16;
//        return i & 63;
//    }
//
//    public static int rotateLeft(int i, int distance) {
//        return i << distance | i >>> -distance;
//    }
//
//    public static int rotateRight(int i, int distance) {
//        return i >>> distance | i << -distance;
//    }
//
//    public static int reverse(int i) {
//        i = (i & 1431655765) << 1 | i >>> 1 & 1431655765;
//        i = (i & 858993459) << 2 | i >>> 2 & 858993459;
//        i = (i & 252645135) << 4 | i >>> 4 & 252645135;
//        return reverseBytes(i);
//    }
//
//    public static int signum(int i) {
//        return i >> 31 | -i >>> 31;
//    }
//
//    public static int reverseBytes(int i) {
//        return i << 24 | (i & '\uff00') << 8 | i >>> 8 & '\uff00' | i >>> 24;
//    }
//
//    public static int sum(int a, int b) {
//        return a + b;
//    }
//
//    public static int max(int a, int b) {
//        return Math.max(a, b);
//    }
//
//    public static int min(int a, int b) {
//        return Math.min(a, b);
//    }
//
//    // Unsupported API
//    public native Optional<java.org.over64.jscripter.std.java.lang.Integer> describeConstable();
//    public native java.org.over64.jscripter.std.java.lang.Integer resolveConstantDesc(MethodHandles.Lookup lookup);
}
