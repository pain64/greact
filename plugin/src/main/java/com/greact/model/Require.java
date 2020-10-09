package com.greact.model;

public @interface Require {
    Class<? extends Component>[] value();
}
