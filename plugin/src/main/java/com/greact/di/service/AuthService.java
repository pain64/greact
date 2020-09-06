package com.greact.di.service;

import com.greact.di.Auth.Schoolboy;
import com.greact.di.DI;

public class AuthService extends DI {
    public static void login(String username, String passwd) {
        // select use and role from db
        di().auth().setRole(new Schoolboy("John", "Nop"));
    }
}
