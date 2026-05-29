# Logging System Module (LoggingInitless)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/initless/logging/`

## 🎯 Main Purpose
Establishes the plugin's boot-tier centralized logging infrastructure. It configures and supplies custom colored logging outputs (`LoggingInstance`) to all downstream services and writes size-managed circular JSON log files to local disk for system tracing.

## 🔗 Connections & Dependencies
* **Core Bootstrap:** Initializes first during server bootstrap, before any other components.
* **Global Consumer:** Loaded and used by every single module across the Essentials, Services, and Apps tiers to log lifecycle changes, transactions, and audit errors.\n