# Java Promise
An ECMAScript [Promise](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise) styled Java utility class.

## Class `Promise<T>`
### Public Methods
- [`static Promise<ArrayList<?>> all(Collection<Promise<?>> promises)`](#all-promises)
- [`static Promise<ArrayList<Result<?>>> allSettled(Collection<Promise<?>> promises)`](#allSettled-promises)
- [`static Promise<Object> any(Collection<Promise<?>> promises)`](#any-promises)
- [`static <T> Promise<T> create(Supplier<T> supplier)`](#create-supplier)
- [`static Promise<Void> create(Runnable runnable)`](#create-runnable)
- [`static Promise<Object> race(Collection<Promise<?>> promises)`](#race-promises)
- [`static Promise<Void> reject(Throwable throwable)`](#reject-throwable)
- [`static <U> Promise<U> resolve(Promise<U> promise)`](#resolve-promise)
- [`static <U> Promise<U> resolve(U value)`](#resolve-value)
- [`<U> Promise<U> completeThen(BiFunction<? super T, Throwable, ? extends U> biFunction)`](#completeThen-biFunction)
- [`Promise<Void> completeThen(BiConsumer<? super T, Throwable> biConsumer)`](#completeThen-biConsumer)
- [`<U> Promise<U> completeThen(Supplier<U> supplier)`](#completeThen-supplier)
- [`Promise<Void> completeThen(Runnable runnable)`](#completeThen-runnable)
- [`<U> Promise<U> failThen(Function<Throwable, U> function)`](#failThen-function)
- [`Promise<Void> failThen(Consumer<Throwable> consumer)`](#failThen-consumer)
- [`T join()`](#join)
- [`<U> Promise<U> successThen(Function<T, U> function)`](#successThen-function)
- [`Promise<Void> successThen(Consumer<T> consumer)`](#successThen-consumer)

## Class `Promise.Result.Success<T>`
### Public Properties
- `final T value`

## Class `Promise.Result.Fail`
### Public Properties
- `final Throwable throwable`

## Examples
### <a id="all-promises"></a>`static Promise<ArrayList<?>> all(Collection<Promise<?>> promises)`
#### In JavaScript
```js
let promise1 = Promise.resolve(3);
let promise2 = new Promise((resolve, reject) => {
    setTimeout(resolve, 100, 'foo');
});

Promise.all([promise1, promise2]).then(console.log); // Array [3, "foo"]
```
#### Equivalent Here
```java
Promise<Integer> promise1 = Promise.resolve(3);
Promise<String> promise2 = Promise.create(() -> {
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {}
    return "foo";
});

Promise.all(Arrays.asList(promise1, promise2)).successThen((Consumer<ArrayList<?>>) System.out::println); // [3, foo]
```
### <a id="allSettled-promises"></a>`static Promise<ArrayList<Result<?>>> allSettled(Collection<Promise<?>> promises)`
#### In JavaScript
```js
let promise1 = Promise.resolve(3);
let promise2 = new Promise((resolve, reject) => setTimeout(reject, 100, 'foo'));

Promise.allSettled([promise1, promise2]).then((results) => console.log(results.map(result => result.status))); // Array ["fulfilled", "rejected"]
```
#### Equivalent Here
```java
Promise<Integer> promise1 = Promise.resolve(3);
Promise<String> promise2 = Promise.create(() -> {
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {}
    throw new RuntimeException("foo");
});

Promise.allSettled(Arrays.asList(promise1, promise2)).successThen(results -> {
    System.out.println(results.stream().map(result -> result instanceof Promise.Result.Success? "fulfilled" : "rejected").collect(Collectors.toList()));
    // [fulfilled, rejected]
});
```
### <a id="any-promises"></a>`static Promise<Object> any(Collection<Promise<?>> promises)`
#### In JavaScript
```js
let promise1 = Promise.reject(0);
let promise2 = new Promise((resolve) => setTimeout(resolve, 100, 'quick'));
let promise3 = new Promise((resolve) => setTimeout(resolve, 500, 'slow'));

Promise.any([promise1, promise2, promise3]).then(console.log); // quick
```
#### Equivalent Here
```java
Promise<String> promise1 = Promise.reject(new Throwable("0"));
Promise<String> promise2 = Promise.create(() -> {
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {}
    return "quick";
});
Promise<String> promise3 = Promise.create(() -> {
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {}
    return "slow";
});

Promise.any(Arrays.asList(promise1, promise2, promise3)).successThen((Consumer<Object>) System.out::println); // quick
```
### <a id="create-supplier"></a>`static <T> Promise<T> create(Supplier<T> supplier)`
#### In JavaScript
```js
let myPromise = new Promise((resolve, reject) => {
    setTimeout(resolve, 300, 'foo');
});
```
#### Equivalent Here
```java
Promise<String> myPromise = Promise.create(() -> {
    try {
        Thread.sleep(300);
    } catch (InterruptedException e) {}
    return "foo";
});
```
### <a id="create-runnable"></a>`static Promise<Void> create(Runnable runnable)`
#### In JavaScript
```js
let myPromise = new Promise((resolve, reject) => {
    setTimeout(() => {
        console.log('foo');
        resolve();
    }, 300);
});
```
#### Equivalent Here
```java
Promise<String> myPromise = Promise.create(() -> {
    try {
        Thread.sleep(300);
    } catch (InterruptedException e) {}
    System.out.println("foo");
});
```
### <a id="race-promises"></a>`static Promise<Object> race(Collection<Promise<?>> promises)`
#### In JavaScript
```js
let promise1 = new Promise((resolve, reject) => setTimeout(resolve, 500, 'one'));
let promise2 = new Promise((resolve, reject) => setTimeout(resolve, 100, 'two'));

Promise.race([promise1, promise2]).then((value) => {
    console.log(value); // two
});
```
#### Equivalent Here
```java
Promise<String> promise1 = Promise.create(() -> {
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {}
    return "one";
});
Promise<String> promise2 = Promise.create(() -> {
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {}
    return "two";
});

Promise.race(Arrays.asList(promise1, promise2)).successThen((Consumer<Object>) System.out::println); // two
```
### <a id="reject-throwable"></a>`static Promise<Void> reject(Throwable throwable)`
#### In JavaScript
```js
let myPromise = Promise.reject(new Error('fail'));
```
#### Equivalent Here
```java
Promise<Void> promise = Promise.reject(new Throwable("fail"));
```
### <a id="resolve-promise"></a>`static <U> Promise<U> resolve(Promise<U> promise)`
#### In JavaScript
```js
let original = Promise.resolve(33);
let cast = Promise.resolve(original);
console.log(original === cast); // true
```
#### Equivalent Here
```java
Promise<Integer> original = Promise.resolve(33);
Promise<Integer> cast = Promise.resolve(original);
System.out.println(original == cast); // true
```
### <a id="resolve-value"></a>`static <U> Promise<U> resolve(U value)`
#### In JavaScript
```js
let promise = Promise.resolve(33);
```
#### Equivalent Here
```java
Promise<Integer> promise = Promise.resolve(33);
```
### <a id="completeThen-biFunction"></a>`<U> Promise<U> completeThen(BiFunction<? super T, Throwable, ? extends U> biFunction)`
#### In JavaScript
```js
let newPromise = promise.then(value => {
    // handle resolve
    return something;
}, error => {
    // handle reject
    return something;
});
promise.finally(() => {
    // handle complete
});
```
#### Equivalent Here
```java
Promise<U> newPromise = promise.completeThen((value, throwable) -> {
    if (value != null) {
        // handle resolve
    } else if (throwable != null) {
        // handle reject
    }
    // handle complete
    return something;
});
```
### <a id="completeThen-biConsumer"></a>`Promise<Void> completeThen(BiConsumer<? super T, Throwable> biConsumer)`
#### In JavaScript
```js
promise.then(value => {
    // handle resolve
}, error => {
    // handle reject
});
let newPromise = promise.finally(() => {
    // handle complete
});
```
#### Equivalent Here
```java
Promise<Void> newPromise = promise.completeThen((value, throwable) -> {
    if (value != null) {
        // handle resolve
    } else if (throwable != null) {
        // handle reject
    }
    // handle complete
});
```
### <a id="completeThen-supplier"></a>`<U> Promise<U> completeThen(Supplier<U> supplier)`
#### In JavaScript
```js
let newPromise = promise.finally(() => {
    // handle complete
    return something;
});
```
#### Equivalent Here
```java
Promise<U> newPromise = promise.completeThen(() -> {
    // handle complete
    return something;
});
```
### <a id="completeThen-runnable"></a>`Promise<Void> completeThen(Runnable runnable)`
#### In JavaScript
```js
let newPromise = promise.finally(() => {
    // handle complete
});
```
#### Equivalent Here
```java
Promise<Void> newPromise = promise.completeThen(() -> {
    // handle complete
});
```
### <a id="failThen-function"></a>`<U> Promise<U> failThen(Function<Throwable, U> function)`
#### In JavaScript
```js
let newPromise = promise.catch(error => {
    // handle reject
    return something;
});
```
#### Equivalent Here
```java
Promise<U> newPromise = promise.failThen(throwable -> {
    // handle reject
    return something;
});
```
### <a id="failThen-consumer"></a>`Promise<Void> failThen(Consumer<Throwable> consumer)`
#### In JavaScript
```js
let newPromise = promise.catch(error => {
    // handle reject
});
```
#### Equivalent Here
```java
Promise<Void> newPromise = promise.failThen(throwable -> {
    // handle reject
});
```
### <a id="join"></a>`T join()`
#### In JavaScript
```js
let value = await promise;
```
#### Equivalent Here
```java
T value = promise.join();
```
### <a id="successThen-function"></a>`<U> Promise<U> successThen(Function<T, U> function)`
#### In JavaScript
```js
let newPromise = promise.then(value => {
    // handle resolve
    return something;
});
```
#### Equivalent Here
```java
Promise<U> newPromise = promise.successThen(value -> {
    // handle resolve
    return something;
});
```
### <a id="successThen-consumer"></a>`Promise<Void> successThen(Consumer<T> consumer)`
#### In JavaScript
```js
let newPromise = promise.then(value => {
    // handle resolve
});
```
#### Equivalent Here
```java
Promise<Void> newPromise = promise.successThen(value -> {
    // handle resolve
});
```
