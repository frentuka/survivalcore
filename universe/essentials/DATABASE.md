# Database Essential Module (DatabaseEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/database/`

## 🎯 Main Purpose
Lettuce-driven asynchronous Redis engine (`DatabaseEssential`). It manages real Redis connection pooling, executes async and sync key-value queries (`get`, `set`, `exists`, `ping`), and fires proprietary events on reconnection or database failures.

---

## ⚙️ Connection Pooling Configuration
To support high-throughput production environments, `DatabaseEssential` integrates **Apache Commons Pool 2** via Lettuce's `ConnectionPoolSupport`.

| Pool Param | Default Value | Description |
|---|---|---|
| `maxTotal` | 4 | The absolute maximum number of active database connections allocated at any given time. |
| `maxIdle` | 2 | The maximum number of connection instances allowed to remain idle in the pool. |
| `minIdle` | 1 | The minimum target of idle connections maintained in the pool at all times. |

---

## 🔒 Safety & Borrowing Patterns
The borrowing and returning of connections from/to the pool is guarded defensively to prevent connection leaks (starvation):

### Synchronous Utilities
Uses standard try-finally block to guarantee safe and immediate connection return:
```kotlin
val connection = pool.borrowObject()
try {
    val syncCommands = connection.sync()
    CompletableFuture.completedFuture(syncCommands.get(key))
} finally {
    pool.returnObject(connection)
}
```

### Asynchronous Utilities
Leverages Lettuce non-blocking operations combined with `.whenComplete` hooks to return connections asynchronously upon task resolution (success or failure):
```kotlin
val connection = pool.borrowObject()
try {
    val asyncCommands = connection.async()
    val future = asyncCommands.get(key).toCompletableFuture()
    future.whenComplete { _, _ ->
        pool.returnObject(connection)
    }
} catch (e: Exception) {
    pool.returnObject(connection)
    throw e
}
```

---

## 💎 Clean Type-Safety
Upgraded the primary utility interface to return non-nullable futures (`CompletableFuture<Boolean>`, `CompletableFuture<String?>`). This eliminates dangerous and awkward null-checking patterns (`?.get() != true`, `?.await()`) in caller components, ensuring structural stability and ease of integration for downstream services like `PlayerDataService` and `SingulaService`.

---

## 🔗 Connections & Dependencies
* **Critical Requisite:** Primary data persistence tier. 
* **State Saver:** Serves as the database backing for `PlayerDataService` profiles, `PermissionsService` groups, and username audit trails.
* **Server Health Watchdog:** Fires `DatabaseDisconnectEvent` immediately on Redis connection loss, commanding `PlayerDataListener` to execute local emergency dumps and safely shut down the Paper server to prevent data desynchronization.