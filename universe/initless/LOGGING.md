# Logging System Module (LoggingInitless) - Brief Overview

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/initless/logging/`
* **Detailed Docs:** [LOGGING_INITLESS.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/initless/logging/LOGGING_INITLESS.md)

---

## 🎯 Main Purpose
Establishes the plugin's boot-tier centralized logging infrastructure. It configures and supplies custom colored logging outputs (`LoggingInstance`) to all downstream services and writes size-managed circular JSON log files to local disk for system tracing.

---

## 🔗 Connections & Dependencies

* **Core Bootstrap (Lifecycle Start):** Initializes first during server bootstrap, before any other components, to guarantee logging availability during the start phase of lower tiers.
* **Global Consumer (API Producer):** Loaded and used by every single module across the Essentials, Services, and Apps tiers to log lifecycle changes, transactions, events, and audit errors. Every service invokes `plugin.loggingInitless.getLog(instanceName, tag)` to register itself and obtain a dedicated logger.
* **Lifecycle Teardown (MClass Integration):** Hooked into `MClass.onDisable()`. Upon server stop/reload, it receives the shutdown signal, iterates over all active loggers to synchronously flush their in-memory buffers to disk, and gracefully terminates its Coroutine I/O scope.

---

## 🛡️ Key Features & Refinement
* **Platform Independence:** Platform-agnostic file path generation and Windows-compatible safe filename timestamps (`yyyy-MM-dd_HH-mm-ss`).
* **Concurrency Protection:** Fully synchronized in-memory log queues and color printers, guaranteeing thread safety across Folia region threads.
* **Zero Data Loss:** Synchronously flushes cached memory logs on plugin disables.