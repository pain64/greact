package quickstart;

public @interface Component {
    Class[] require() default {};
}
