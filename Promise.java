import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Promise<T> {

    private final CompletableFuture<T> future;

    private Promise(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <T> Promise<T> create(Supplier<T> supplier) {
        return new Promise<>(CompletableFuture.supplyAsync(supplier));
    }

    public static Promise<Void> create(Runnable runnable) {
        return new Promise<>(CompletableFuture.runAsync(runnable));
    }

    public <U> Promise<U> successThen(Function<T, U> function) {
        return new Promise<U>(future.thenApply(function));
    }

    public Promise<Void> successThen(Consumer<T> consumer) {
        return successThen(value -> {
            consumer.accept(value);
            return null;
        });
    }

    public <U> Promise<U> failThen(Function<Throwable, U> function) {
        AtomicReference<U> u = new AtomicReference<>();
        return new Promise<>(future.exceptionally(throwable -> {
            u.set(function.apply(throwable));
            return null;
        }).thenApply(unused -> u.get()));
    }

    public Promise<Void> failThen(Consumer<Throwable> consumer) {
        return failThen(throwable -> {
            consumer.accept(throwable);
            return null;
        });
    }

    public <U> Promise<U> completeThen(BiFunction<? super T, Throwable, ? extends U> biFunction) {
        return new Promise<>(future.handle(biFunction));
    }

    public <U> Promise<U> completeThen(Supplier<U> supplier) {
        return completeThen((t, e) -> supplier.get());
    }

    public Promise<Void> completeThen(Runnable runnable) {
        return completeThen((t, e) -> {
            runnable.run();
            return null;
        });
    }

    public T join() {
        return future.join();
    }

    public static Promise<ArrayList<?>> all(Collection<Promise<?>> promises) {
        CompletableFuture<?>[] futures = promises.stream().map(promise -> promise.future).toArray(CompletableFuture[]::new);
        return new Promise<>(CompletableFuture.allOf(futures).thenApply(v -> Arrays.stream(futures).map(CompletableFuture::join).collect(Collectors.toCollection(ArrayList::new))));
    }

    public static Promise<ArrayList<Result<?>>> allSettled(Collection<Promise<?>> promises) {
        CompletableFuture<?>[] futures = promises.stream().map(promise -> promise.future).toArray(CompletableFuture[]::new);
        return Promise.create(() -> {
            ArrayList<Result<?>> results = new ArrayList<>();
            for (CompletableFuture<?> future : futures)
                try {
                    results.add(new Result.Success<>(future.join()));
                } catch (Throwable e) {
                    results.add(new Result.Error(e));
                }
            return results;
        });
    }

    public static abstract class Result<T> {

        public final static class Success<T> extends Result<T> {
            public final T value;

            public Success(T value) {
                this.value = value;
            }
        }

        @SuppressWarnings("rawtypes")
        public final static class Error extends Result {
            public final Throwable error;

            public Error(Throwable error) {
                this.error = error;
            }
        }
    }

    public static Promise<Object> any(Collection<Promise<?>> promises) {
        return Promise.create(() -> {
            List<CompletableFuture<?>> futureList = promises.stream().map(promise -> promise.future).collect(Collectors.toList());
            while (futureList.size() > 0)
                try {
                    return CompletableFuture.anyOf(futureList.toArray(CompletableFuture[]::new)).join();
                } catch (Throwable e) {
                    futureList = futureList.stream().filter(future -> !future.isDone()).collect(Collectors.toList());
                }
            throw new CompletionException("All promises were rejected", null);
        });
    }

    public static Promise<Object> race(Collection<Promise<?>> promises) {
        return new Promise<>(CompletableFuture.anyOf(promises.stream().map(promise -> promise.future).toArray(CompletableFuture[]::new)));
    }

    public static <U> Promise<U> resolve(Promise<U> promise) {
        return promise;
    }

    public static <U> Promise<U> resolve(U value) {
        return new Promise<>(CompletableFuture.completedFuture(value));
    }

    public static Promise<Void> reject(Throwable throwable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return new Promise<>(future);
    }
}
