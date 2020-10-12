package com.over64.greact.model;

public @interface Require {
    Class<? extends Component>[] value();
}
