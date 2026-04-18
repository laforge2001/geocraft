# GeoCraft

GeoCraft is lightweight framework for rapidly prototyping and deploying new geoscience algorithms.

GeoCraft consists of:

- a general purpose geoscience development platform
- the ABAVO perspective for performing AVO analysis
- the GeoMath perspective that contains geoscience algorithms
- simple visualization and data exploration tools

GeoCraft is not designed to be a seismic data processing system, nor is it a replacement for seismic interpretation applications.

## Developer Quick Start

### Prerequisites

- **JDK 21** (Temurin recommended). Verify with `java -version`.
- **Maven 3.9+** (only required for Tycho command-line builds). Verify with `mvn -v`.
- **Git**.
- **Eclipse IDE for RCP and RAP Developers, 2025-12** (only required for in-IDE development).

### 1. Clone

```bash
git clone https://github.com/laforge2001/geocraft.git
cd geocraft
```

### 2a. Build and Launch via Tycho (command line)

Builds every bundle, resolves the target platform from `org.geocraft.target/org.geocraft.target.target`, and assembles the product under `org.geocraft.repository/target/products/`.

```bash
mvn clean verify
```

Launch the built product:

- **macOS (Apple Silicon)**:
  ```bash
  ./launch-geocraft.sh
  ```
  Or run the native app directly:
  ```bash
  open org.geocraft.repository/target/products/org.geocraft.product.product/macosx/cocoa/aarch64/Eclipse.app
  ```
- **Linux (x86_64)**:
  ```bash
  org.geocraft.repository/target/products/org.geocraft.product.product/linux/gtk/x86_64/eclipse
  ```
- **Windows (x86_64)**:
  ```cmd
  org.geocraft.repository\target\products\org.geocraft.product.product\win32\win32\x86_64\eclipse.exe
  ```

### 2b. Build and Launch from the Eclipse IDE

> **Use a fresh install of "Eclipse IDE for RCP and RAP Developers, 2025-12".** Reusing a previous Eclipse install that has the deprecated `org.sonatype.tycho.m2e` connector will cause hundreds of "Conflicting lifecycle mapping metadata" errors — see Troubleshooting.

1. **Start Eclipse with a new workspace**:
   - Launch Eclipse IDE for RCP and RAP Developers (2025-12).
   - When prompted for a workspace, pick a **new, empty directory** (e.g. `~/eclipse-workspaces/geocraft`). Do **not** pick the cloned `geocraft/` directory itself.
2. **Import all projects into the workspace**:
   - `File > Import... > Maven > Existing Maven Projects`, click **Next**.
   - Set **Root Directory** to the cloned `geocraft/` directory, leave all modules checked, click **Finish**.
   - Wait for the initial Maven/M2E import to finish. Compile errors are expected until the target platform is set in step 3.
3. **Configure the target platform**:
   - Double-click `org.geocraft.target/org.geocraft.target.target` to open the Target Editor.
   - Click **Reload** (top right) and wait for all p2 locations to resolve (requires `download.eclipse.org` access).
   - Click **Set as Active Target Platform** (top right).
   - Eclipse rebuilds the workspace and the errors clear. If they don't, run `Project > Clean... > Clean all projects`.
4. **Launch GeoCraft**:
   - In Package Explorer, right-click `org.geocraft.product/GeoCraft.launch` > **Run As > GeoCraft**.
   - Or: `Run > Run Configurations... > Eclipse Application > GeoCraft` > **Run**.
   - The committed launch config (`GeoCraft.launch`) is feature-based and matches `GeoCraft.product`, so every algorithm and viewer bundle is loaded without manual plug-in selection.

### Troubleshooting

- **Target platform fails to resolve**: ensure network access to `download.eclipse.org` (the p2 repositories listed in `pom.xml`).
- **Tycho build fails on test bundles**: `testFailureIgnore` is set, but compilation errors still fail the build. Check the failing module's `pom.xml` and MANIFEST.MF.
- **Launch fails with bundle resolution errors**: see `SWT_MIGRATION_LOG.md` for known issues and workarounds.
- **Eclipse IDE — `Conflicting lifecycle mapping metadata ... org.sonatype.tycho.m2e` and hundreds of `does not have an expanded version` errors**: the deprecated `org.sonatype.tycho.m2e` connector conflicts with the modern `org.eclipse.m2e.pde.connector` bundled in Eclipse 2025-12 for RCP and RAP Developers. Uninstall the old one:
  1. `Help > About Eclipse > Installation Details > Installed Software`.
  2. Select **Tycho Project Configurators** (`org.sonatype.tycho.m2e`), click **Uninstall**, restart Eclipse.
  3. Select all projects in Package Explorer, right-click > **Maven > Update Project...** (Alt+F5), click **OK**.
- **Eclipse IDE — "An API baseline has not been set for the current workspace"**: non-blocking warning from PDE API Tools. Suppress via `Window > Preferences > Plug-in Development > API Errors/Warnings > General` tab, set **Missing API baseline** to **Ignore**.