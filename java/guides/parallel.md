# Java parallelization cheatsheet

## Executors

### Platform threads

For CPU intensive tasks.

Platform threads are linked directly to hardware threads.

```java
import java.util.concurrent.Executors;

ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
```

### Virtual threads

For blocking IO tasks.

Virtual threads are not always directly linked threads.
The CPU scheduler iterates over the existing virtual threads.

```java
import java.util.concurrent.Executors;

void main() {
    // executor.close() is called by the try-with block that awaits for the tasks
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        executor.submit(() -> callDatabase());
        executor.submit(() -> callHttpService());
        executor.submit(() -> readFile());
    }

    try (var executor = Executors.newFixedThreadPool(8)) {
        Future<Result> a = executor.submit(() -> calculateA());
        Future<Result> b = executor.submit(() -> calculateB());

        Result resultA = a.get();
        Result resultB = b.get();
    }

    // close without awaiting the tasks
    executor.shutdownNow();
}
```

The CPU is not parallel here.

### Futures

Futures can depend on each other.

```java
CompletableFuture<A> a =
        CompletableFuture.supplyAsync(this::loadA, executor);

CompletableFuture<B> b =
        CompletableFuture.supplyAsync(this::loadB, executor);

CompletableFuture<Result> result =
        a.thenCombine(b, this::combine);
```

### Closing

When the executor is closed, it awaits the tasks.

An error inside the tasks will propagate to the executor's scope, but
it does not stop other tasks unless `shutdownNow` is called in finally.

If the thread where the `close` is waiting is interrupted, `shutdownNow` is called.

Cancelling a request causes thread interruption.

Interruption does not force the tasks to be closed, some tasks ignore it.

## Task scope

A more convenient way for linking the lifecycle of the tasks to their source.

The task scope closes the awaited tasks by default when it's closed.
When one task fails, the others are canceled.
It uses virtual threads by default.

```java
void main() {
    try (var scope = new StructuredTaskScope.open()) {

        var user = scope.fork(() -> loadUser());
        var posts = scope.fork(() -> loadPosts());

        scope.join();
        scope.throwIfFailed();

        return new Response(user.get(), posts.get());
    }
}
```

Using hardware threads.

```java
void main() {
    ThreadFactory platformFactory = Thread.ofPlatform().factory();

    try (var scope = StructuredTaskScope.open(cf -> cf.withThreadFactory(platformFactory))) {
        scope.fork(() -> {
            // This subtask runs on a platform thread
            return performBlockingTask();
        });
        scope.join();
    }
}
```

## This project

The project uses the executor because the structured task scope was in preview
when this decision was made.
It is excepted to be finalized in JDK 27.