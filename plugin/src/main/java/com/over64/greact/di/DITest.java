package com.over64.greact.di;

import com.over64.greact.di.DI.Data;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DITest {
    static class GReactRPC {
        void handle(String path, InputStream data, OutputStream out) {

        }
    }

    void mmain() throws Exception {
        DataSource commonDb = null;
        DataSource smsDb = null;
        var rpc = new GReactRPC();

        var server = new Server(8080);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String path, Request base,
                               HttpServletRequest req,
                               HttpServletResponse resp) throws IOException {

                var auth = new Auth(req.getSession(true));
//                DI.bound(new Data(commonDb, smsDb, auth), () -> {
//                    rpc.handle(path, req.getInputStream(), resp.getOutputStream());
//                });

            }
        });

        server.start();
        server.join();
    }
}
