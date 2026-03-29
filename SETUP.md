# HeightControl — Codespace Setup

## Why you need this step

The `gradle/wrapper/gradle-wrapper.jar` is a binary bootstrap file that
`./gradlew` needs to download the correct Gradle version (8.8).
It is not included in the repository (standard practice — it's in `.gitignore`).

Without it, GitHub Codespaces falls back to the system Gradle (currently 9.x)
which is incompatible with ForgeGradle 6.

## One-time setup (run once after opening the Codespace)

```bash
# Downloads gradle-wrapper.jar and pins the wrapper to 8.8
gradle wrapper --gradle-version 8.8 --distribution-type bin
```

Then build normally:

```bash
./gradlew build
```

## What this does

`gradle wrapper` uses the **system** Gradle (9.x) purely to regenerate the
wrapper files — it does NOT build the project with Gradle 9. It writes:
- `gradle/wrapper/gradle-wrapper.jar`  ← the missing bootstrap
- `gradle/wrapper/gradle-wrapper.properties` ← already set to 8.8

After that, every `./gradlew` invocation uses the wrapper, which downloads
and uses Gradle 8.8 regardless of what's installed on the system.

## Verify

```bash
./gradlew --version
# Should print: Gradle 8.8
```
