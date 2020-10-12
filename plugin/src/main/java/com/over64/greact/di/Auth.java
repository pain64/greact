package com.over64.greact.di;

import javax.servlet.http.HttpSession;
import java.util.Optional;

public record Auth(HttpSession session) {
    public interface Role {}
    public record Anon() implements Role {}
    public record Schoolboy(String name, String school) implements Role {}

    static final String ROLE_SESSION_KEY = "user_role";

    public Role getRole() {
        return Optional.ofNullable(session.getAttribute(ROLE_SESSION_KEY))
            .map(obj -> (Role) obj)
            .orElse(new Anon());
    }

    public void setRole(Role role) {
        session.setAttribute(ROLE_SESSION_KEY, role);
    }
}
