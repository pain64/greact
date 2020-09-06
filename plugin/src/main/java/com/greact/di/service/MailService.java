package com.greact.di.service;

import com.greact.di.DI;

public class MailService extends DI {

    static void send() {
        var x = di().commonDb();
    }
}
