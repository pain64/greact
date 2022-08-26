package com.over64.jscripter.bundler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import java.io.Serializable;
import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.eclipse.jetty.websocket.api.StatusCode.NORMAL;

public class WebsocketSender {
    public static class WorkServerParams implements WorkParameters, Serializable {
        String message;
    }

    public abstract static class WebServer implements WorkAction<WorkServerParams> {
        @Override public void execute() {
            try {
                System.out.println("AT EXECUTOR");
                _execute();
            } catch (Exception e) {
                System.out.println("RT EXCEPTION: " + e);
                throw new RuntimeException(e);
            }
        }

        void _execute() throws Exception {
            try {
                var client = new WebSocketClient();
                try {
                    client.start();
                    var socket = new ClientHandler();
                    var session = client.connect(socket, URI.create("ws://localhost:8080/greact_livereload_events/")).get();

                    session.getRemote().sendString(getParameters().message);

                    session.close(NORMAL, "I'm done");
                    System.out.println("AFTER SEND");
                } finally {
                    client.stop();
                }
            } catch (ExecutionException t) {
                if (t.getCause() instanceof ConnectException) {
                    System.out.println("start livereload server!");
                    var server = new Server();
                    var connector = new ServerConnector(server);
                    connector.setPort(8080);
                    server.addConnector(connector);

                    var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                    context.setContextPath("/");
                    server.setHandler(context);

                    JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
                        wsContainer.setMaxTextMessageSize(65535);
                        wsContainer.setIdleTimeout(Duration.ofHours(1));
                        wsContainer.addMapping("/greact_livereload_events/*", ServerHandler.class);
                    });

                    server.start();
                } else throw t;
            } catch (java.io.IOException t) {
                throw new GradleException("Error: cannot use port 8080 for GReact livereload. Is it available?", t);
            }
        }

        public static class ClientHandler implements WebSocketListener { }

        public static class ServerHandler implements WebSocketListener {
            static ConcurrentHashMap.KeySetView<Session, Boolean> sessions = ConcurrentHashMap.newKeySet();
            volatile Session session = null;

            @Override public void onWebSocketConnect(Session ss) {
                this.session = ss;
                sessions.add(ss);
            }

            @Override public void onWebSocketClose(int statusCode, String reason) {
                var ss = session;
                if (ss != null) sessions.remove(ss);
            }

            @Override public void onWebSocketText(String message) {
                System.out.println("####HAS NEW WEBSOCKET MESSAGE: " + message);
                if (message.equals("heartbeat")) return;
                var me = session;

                sessions.forEach(ss -> {
                    if (ss != me)
                        try {
                            ss.getRemote().sendString(message);
                        } catch (java.lang.Exception ex) {
                            System.out.println("failed to send livereload message to remote: " + ss.getRemoteAddress());
                        }
                });
            }
        }
    }
}
