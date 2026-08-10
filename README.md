# Compose Advanced Hello World Messages

Compose Multiplatform message presentation and feature registration for the
Advanced Hello World mobile repository family.

It owns the shared message presentation state holder and unidirectional event
flow over the KMP messages repository. The UI renders local messages, offline
submission, refresh, retry, and synchronization feedback with the shared
Compose Core state and accessibility foundations. Networking, SQL, outbox
processing, and Android scheduling remain outside this package.

## Published dependency graph

Version `0.1.0` compiles against the independently released `0.1.0` artifacts
from:

- `kmp-advanced-hello-world-core`
- `compose-advanced-hello-world-core`
- `kmp-advanced-hello-world-messages`

Sibling composite builds are opt-in for local development:

```shell
./gradlew -PuseLocalFamilyBuilds=true check
```

CI resolves immutable upstream release commits and validates the generated
Maven publication from a clean consumer. Release tags must match `VERSION` and
produce signed Maven artifacts, an SPDX SBOM, SHA-256 checksums, and commit
metadata.
