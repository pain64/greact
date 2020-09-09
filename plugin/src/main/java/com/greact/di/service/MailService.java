package com.greact.di.service;

import static com.greact.di.DI.di;

public class MailService {

    static void send() {
        var x = di().commonDb();
    }
}
