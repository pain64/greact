package jstack.ssql;

import java.util.function.Function;

public interface ConnectionHandle extends TransactionHandle {
    <T> T withTransaction(Function<TransactionHandle, T> handler);
}
