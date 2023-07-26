package jstack.ssql;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface TransactionHandle {
    <T> T exec(@Language("sql") String stmt, Class<T> toClass, Object... args);
    <T> T queryOne(@Language("sql") String stmt, Class<T> toClass, Object... args);
    @Nullable <T> T queryOneOrNull(@Language("sql") String stmt, Class<T> toClass, Object... args);
    <T> Stream<T> queryAsStream(@Language("sql") String stmt, Class<T> toClass, Object... args);
    <T> T[] query(@Language("sql") String stmt, Class<T> toClass, Object... args);
    <T> List<T> queryAsList(@Language("sql") String stmt, Class<T> toClass, Object... args);
}
