# Changelog

## Unreleased

## 0.2.0 - 2026-08-12

- Compile and publish against KMP Messages `0.2.0`, including its backend
  synchronization, durable outbox retry, and reconciliation contracts.
- Keep KMP Core and Compose Core pinned independently at `0.1.0`.
- Force fresh instrumented Kotlin compilation for deterministic CodeQL
  extraction.

## 0.1.0 - 2026-08-10

- Add the message list, composer, refresh, retry, and synchronization-state UI.
- Add repository-backed presentation state and unidirectional message events.
- Recover repository observation after a successful load-error retry.
- Render shared accessible loading, empty, error, pending, and retry states.
- Exercise Compose behavior and published-artifact rendering in UI tests.
- Correct nested-checkout release artifact upload paths.
- Export `MessagesFeatureFactory` through the Compose core feature contract.
- Publish JVM, Android, and Kotlin Multiplatform variants with MIT metadata and
  detached signatures.
- Add immutable dependency validation, clean-consumer verification, security
  scanning, SBOM generation, and reproducible release metadata.
