# JPA transaction guide

There are two transaction types:
- @Transactional annotated methods
- TransactionTemplate

**The Transactional annotation does not work when a class calls one of its own methods.**

Example:
```java
class MyClass {
    @Transactional
    public void myMethod() {
        // ...
    }
    
    public void myOtherMethod() {
        myMethod();
    }
}
```

The transactional annotation will not work on the `myMethod()` method, it won't cause rollback when needed.

If only a part of the method is transactional, you can use the `TransactionTemplate` class.

Example:
```java
private final TransactionTemplate transactions;
//...
void myMethod() {
    // ...
    transactions.execute(status -> {
        // ...
    });
}
```

The usage of the `TransactionTemplate` requires a `TransactionTemplate` bean.

Example:
```java
@Configuration
public class MediaApiConfiguration {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
```