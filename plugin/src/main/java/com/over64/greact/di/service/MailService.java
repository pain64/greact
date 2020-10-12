package com.over64.greact.di.service;

import static com.over64.greact.di.DI.di;

public class MailService {

    static void send() {
        var x = di().commonDb();
    }
}
