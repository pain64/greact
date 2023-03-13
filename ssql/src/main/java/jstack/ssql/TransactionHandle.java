package jstack.ssql;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

public interface TransactionHandle {
    <T> T exec(@Language("sql") String stmt, Class<T> toClass, Object... args);
    <T> T[] query(String stmt, Class<T> toClass, Object... args);
    <T> T queryOne(@Language("sql") String stmt, Class<T> toClass, Object... args);
    @Nullable <T> T queryOneOrNull(@Language("sql") String stmt, Class<T> toClass, Object... args);
}
