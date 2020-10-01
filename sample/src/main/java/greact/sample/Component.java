package greact.sample;

public @interface Component {
    Class[] require() default {};
}
