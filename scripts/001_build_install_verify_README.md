# Build, install, and verify

Run from the repository root:

```bash
FIJI_DIR=/absolute/path/to/Fiji bash scripts/001_build_install_verify.sh
```

The script:

1. runs the Maven test suite;
2. builds Java 8-compatible plugin bytecode;
3. installs `OpenGridSeg.jar` into Fiji's `plugins` folder;
4. asks Fiji's SciJava `PluginService` to find the command;
5. checks the exact menu path `Plugins > OpenGridSeg`;
6. writes the JAR and SHA-256 file under `dist/`.

Set `MAVEN=/absolute/path/to/mvn` if Maven is not available as `mvn` on `PATH`.
