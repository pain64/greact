package jstack.demo;

import jstack.ssql.RW;
import jstack.ssql.dialect.Bind;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static jstack.demo.Dialect.*;

@Bind(klass = Term.class, sqlType = "term", using = EnumRW.class)
@Bind(klass = Gender.class, sqlType = "gender", using = EnumRW.class)
public interface Dialect {
    enum Gender {MALE, FEMALE}
    enum Term {SPRING, AUTUMN}

    class EnumRW implements RW {
        @Override @SuppressWarnings({"unchecked", "rawtypes"})
        public Object read(ResultSet rs, int i, Class<?> toClass) throws SQLException {
            return Enum.valueOf((Class<? extends Enum>) toClass, rs.getString(i));
        }
        @Override
        public void write(PreparedStatement stmt, int i, Object v) throws SQLException {
            stmt.setObject(i, v, Types.OTHER);
        }
    }
}
