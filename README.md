# nas-media-batch

A batch-oriented Java application responsible for scanning user uploads, sorting media files, and persisting operational logs for a NAS-style media system.

This README documents **only the Java batch component** (`nas-media-batch`). Server setup, user management, and filesystem provisioning are intentionally out of scope here.

---

## What this project does

`nas-media-batch` is a **one-shot batch job** that runs periodically (typically via `cron`) and performs the following steps:

1. Scans user upload directories to calculate daily upload statistics
2. Sorts uploaded media files into a centralized media directory
3. Handles failures by quarantining problematic files
4. Persists batch-level, user-level, and file-level results to PostgreSQL

The job is designed to be:

* Deterministic
* Failure-tolerant
* Observable (everything important is logged)

---

## Execution model

* **Not a daemon**
* **Not a web service**
* Runs once per invocation
* Safe to execute from `cron`

Each run is independent and produces an immutable batch record in the database.

---

## High-level flow

```
┌──────────────┐
│ UploadScanner│  → scans uploads (read-only)
└──────┬───────┘
       ↓
┌──────────────┐
│ FileSorter   │  → moves files, quarantines failures
└──────┬───────┘
       ↓
┌──────────────┐
│ Persistence  │  → writes logs to PostgreSQL
└──────────────┘
```

---

## Directory assumptions

The batch job assumes the following logical structure (paths are configurable):

```
/uploads/<user>/incoming     # user uploads (recursive allowed)
/quarantine/<user>           # failed files per user
/media/<year>/<month>        # aggregated media store
```

* Users upload files into their own `incoming` directory
* All files (including those in subdirectories) are processed
* After a successful run, `incoming` directories are left empty

---

## Configuration

Configuration is externalized using a properties file.

### application.properties (example)

```properties
db.url=jdbc:postgresql://localhost:5432/nas_logging
db.username=nas_app
db.password=secret

uploads.root=/srv/uploads
media.root=/srv/media
quarantine.root=/srv/quarantine
```

The file is loaded using the JVM system property:

```
-Dnas.config=/opt/nas-media-batch/conf/application.properties
```

The application fails fast if required configuration keys are missing.

---

## Database interaction

The batch job writes to PostgreSQL using plain JDBC.

### Tables written by this job

* `batch_run` – one row per batch execution
* `user_daily_upload` – one row per user per batch
* `file_failure` – one row per failed file

### Database role expectations

The runtime database user:

* **Must** have `INSERT` and `SELECT` privileges
* **Must not** require schema-altering privileges

Sequences are expected to be non-transactional; gaps in IDs are normal and intentional.

---

## Failure handling philosophy

This project follows a **fail-soft** approach:

* Individual file failures do **not** abort the batch
* All failures are logged with context
* Files that cannot be processed are moved to quarantine
* Batch results are persisted even when failures occur

Only fatal startup errors (e.g., database unavailable) prevent persistence.

---

## Build & run

### Build

```
mvn clean package
```

### Run manually

```
java \
  -Dnas.config=/path/to/application.properties \
  -jar nas-media-batch.jar
```

Exit codes:

* `0` – batch completed successfully
* `2` – batch failed (but results were persisted)
* `3` – fatal startup error

---

## Design principles

* Batch-first, not real-time
* Explicit over clever
* Observability over silence
* Linux and filesystem semantics respected
* Minimal dependencies

---

## Non-goals (by design)

This project intentionally does **not** include:

* A web UI
* Real-time ingestion
* EXIF-based media parsing
* Deduplication
* Retry logic

These are deferred to future versions to keep v1 focused and auditable.

---

## Status

**Version:** v1 (stable)

The batch job has been tested against a real Linux filesystem and PostgreSQL database and is considered feature-complete for its intended scope.
