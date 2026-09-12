# 41 Client (fortyone-client)

**Fabric 1.21.11 client mod — version 2.9.59**

| | |
|---|---|
| **Mod ID** | `fortyone-client` |
| **Package** | `dev.anticheatqa` |
| **Minecraft** | 1.21.11 |
| **Java** | 21 |

## Repository status

This repo was built from the **compiled** release JAR (`41-client-2.9.59.jar`).

- Gradle project, `fabric.mod.json`, mixins, CI workflow, and core entry class are on `main`.
- **162 Java files** were decompiled with CFR from the JAR (local workspace).
- Decompiled code uses **intermediary mappings** (`class_310`, etc.). That is expected from a release JAR and will **not** compile cleanly against Yarn without remapping.

**If you have the real source tree** (not a JAR), clone this repo and force-push your sources:

```bash
git clone https://github.com/atlxsvr-create/anticheat-qa.git
cd anticheat-qa
# replace contents with your real sources, then:
git add -A
git commit -m "Add real source tree"
git push
```

## Build (after Yarn-mapped sources)

```bash
gradle build
```

Requires Java 21, Fabric Loader, Fabric API.

## License

MIT
