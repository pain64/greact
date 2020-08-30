package js;

public class Test1 {
    // must be ignored
    interface TestInterface {

    }

    class Inner1 {
        String f1;
    }

    static class Inner2 {
        int f1;
    }

    // members with init
    int f1 = 42;
    Integer f2 = 42;
    float f3 = 42.0f;
    Float f4 = 42.0f;
    double f5 = 42.0d;
    Double f6 = 42.0d;
    boolean f7 = true;
    String f8 = "hello";

    // must be null
    int undefinedField;

    static int s1 = 42;
    static void mStatic() {
    }

    void m1() {
    }

    int m2() {
        return 42;
    }
}
