package greact.sample;

import com.greact.model.JSSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ServiceLoader;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class Main {
    static JarFile openJar(String fName) {
        try {
            return new JarFile(fName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static byte[] readData(JarFile jar, ZipEntry entry) {
        try {
            return jar.getInputStream(entry).readAllBytes();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        var libraryCode = ServiceLoader.load(JSSource.class).stream()
            .flatMap(lib -> {
                var libClass = lib.get().getClass();
                var declaredAtUri = libClass.getProtectionDomain().getCodeSource().getLocation();
                var jar = openJar(declaredAtUri.getFile());

                return jar.stream()
                    .filter(entry -> entry.getName().endsWith(".js"))
                    .map(entry -> new String(readData(jar, entry)));
            })
            .collect(Collectors.joining("\n"));

        var appCode = Files.walk(Paths.get("./build/classes/"))
            .filter(Files::isRegularFile)
            .filter(p -> p.getFileName().toString().endsWith(".js"))
            .map(p -> {
                try {
                    return new String(new FileInputStream(p.toFile()).readAllBytes());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            })
            .collect(Collectors.joining("\n"));


        System.out.println(libraryCode);
        System.out.println(appCode);
    }
}
