package greact.sample;

import com.greact.model.JSSource;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardWatchEventKinds.*;

public class Loader {
    static JarFile openJar(String fName) {
        try {
            return new JarFile(fName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static byte[] readJar(JarFile jar, ZipEntry entry) {
        try {
            return jar.getInputStream(entry).readAllBytes();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static byte[] readFile(File file) {
        try {
            return new FileInputStream(file).readAllBytes();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static String libraryCode() {
        return ServiceLoader.load(JSSource.class).stream()
            .flatMap(lib -> {
                var libClass = lib.get().getClass();
                var declaredAtUri = libClass.getProtectionDomain().getCodeSource().getLocation();
                var jar = openJar(declaredAtUri.getFile());

                return jar.stream()
                    .filter(entry -> entry.getName().endsWith(".js"))
                    .map(entry -> new String(readJar(jar, entry)));
            })
            .collect(Collectors.joining("\n"));
    }

    public static String appCode() throws IOException {
        return Files.walk(Paths.get("./build/classes/"))
            .filter(Files::isRegularFile)
            .filter(p -> p.getFileName().toString().endsWith(".js"))
            .map(p -> new String(readFile(p.toFile())))
            .collect(Collectors.joining("\n"));
    }

    @WebSocket
    public static class FileWatcher {
        private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

        public FileWatcher() throws IOException {
            var watcher = FileSystems.getDefault().newWatchService();
            Files.walk(Paths.get("./build/classes/"))
                .filter(Files::isDirectory)
                .forEach(p -> {
                    try {
                        p.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

            new Thread(() -> {
                while (true)
                    try {
                        var key = watcher.take();
                        var needReload = false;
                        for (var event : key.pollEvents()) {
                            if (event.context().toString().endsWith(".js")) needReload = true;
                            System.out.println(
                                "Event kind:" + event.kind()
                                    + ". File affected: " + event.context() + ".");
                        }
                        if (needReload)
                            for (var session : sessions) {
                                System.out.println("send reload");
                                session.getRemote().sendString("reload!");
                            }
                        key.reset();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
            }).start();
        }

        @OnWebSocketConnect
        public void connected(Session session) {
            session.setIdleTimeout(60 * 60 * 1000);
            System.out.println("connect");
            sessions.add(session);
        }

        @OnWebSocketClose
        public void closed(Session session, int statusCode, String reason) {
            sessions.remove(session);
        }


    }
}
