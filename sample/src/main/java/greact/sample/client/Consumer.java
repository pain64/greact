package greact.sample.client;

@FunctionalInterface
public interface Consumer<T> {
    void consume(T value);
}
