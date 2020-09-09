package com.greact.di.service;

import com.greact.di.Auth.Schoolboy;

import static com.greact.di.DI.di;

public class AuthService {
    public static void login(String username, String passwd) {
        // select use and role from db
        di().auth().setRole(new Schoolboy("John", "Nop"));
    }
}
