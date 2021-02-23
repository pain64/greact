package greact.sample.server;

public class DbUtil {
    public static String like(String value) {
        if (value == null) value = "";
        return "%" + value + "%";
    }
}
