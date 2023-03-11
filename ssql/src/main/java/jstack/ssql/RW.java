package jstack.ssql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface RW {
    Object read(ResultSet rs, int i, Class<?> toClass) throws SQLException;
    void write(PreparedStatement stmt, int i, Object v) throws SQLException;
}
