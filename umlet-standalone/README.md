# Building the standalone version from source

The standalone (desktop) version consists of the Maven modules `umlet-elements`, `umlet-res`, `umlet-swing` and `umlet-standalone`.
The other modules (Eclipse plugin, GWT/UMLetino, web, VS Code extension) need additional tooling and are not needed for the standalone version.

## Prerequisites

* **A JDK, not only a JRE.** The build needs `javac`; a machine that only has a JRE (e.g. `C:\Program Files\Java\jre1.8...`) cannot build UMLet.
  The continuous integration build (`.github/workflows/maven.yml`) uses JDK 11, which is the recommended version.
  The code is compiled for Java 8 (`maven.compiler.source/target` is `1.8` in the root `pom.xml`).
  Check with `javac -version`.
* **Apache Maven 3.x** (3.9.x was used). Check with `mvn -version`.
  * Download the binary zip from <https://maven.apache.org/download.cgi>, extract it anywhere (no admin rights needed) and add its `bin` folder to the `PATH`.
  * On Windows there is no `Apache.Maven` package in `winget` (`winget install Apache.Maven` fails); use the zip download. Chocolatey also offers a `maven` package (not tried).
* `JAVA_HOME` should point to the JDK, and `JAVA_HOME\bin` and the Maven `bin` folder must be on the `PATH`.
  Both can also be set only for the current terminal session, e.g. in PowerShell:

  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-11.0.33.10-hotspot"
  $env:Path = "C:\tools\apache-maven-3.9.16\bin;$env:JAVA_HOME\bin;$env:Path"
  ```

* Internet access for the first build (Maven downloads the dependencies into `~/.m2`).

## Build

Run this in the repository root. It builds only the modules of the standalone version and runs their unit tests (about one minute):

```text
mvn -pl "!umlet-eclipse-plugin-deps,!umlet-eclipse-plugin,!umlet-eclipse-feature,!umlet-eclipse-p2,!umlet-gwt,!umlet-web,!umlet-vscode" clean install
```

* On Linux/macOS use single quotes around the `-pl` argument because of the `!`.
* The same command is stored as the Eclipse launch configuration `launcher/mvn clean install (only standalone).launch`.
* Add `-DskipTests` to skip the tests.
* Do not use Maven's offline mode (`-o`) for the first build: the test runner (surefire) is downloaded when the tests run for the first time.
* `mvn clean install -P checks` is what the continuous integration build runs for all modules (it includes the static analysis with SpotBugs). Building the other modules was not tried for these instructions.

## Result and Starting UMLet

The build creates `umlet-standalone/target/umlet-standalone-<version>.zip`.
Extract it; the extracted `Umlet` folder contains `Umlet.exe` (Windows launcher), `umlet.sh` (Linux/macOS), `umlet.jar` and the `lib` folder with the dependencies.
Start it with `Umlet.exe` / `umlet.sh`, or with `java -jar umlet.jar` from inside that folder.

## Other Information

* UMLet only runs one instance per user. If another UMLet is running (for example an installed release), starting a second one only brings the first one to the front, so close it before testing a new build.
  This also means that a running UMLet locks its jar files, which prevents extracting a new build over the same folder.
* The configuration is saved on exit to `%LOCALAPPDATA%\Umlet\umlet.cfg` on Windows (see `Path.osConformConfig()` for other systems), and is shared by all UMLet versions of the same user.
  To try a build without touching your settings, set the environment variable `LOCALAPPDATA` to an empty folder before starting it (only for that terminal session); UMLet then starts with default settings and writes its config there.
* In builds made as described above the version is reported as `${project.version}` (visible in the log output and in `program_version` of the saved config). This was only observed and not investigated; as a side effect the update check may show a newer release than the build.
