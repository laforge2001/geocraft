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

1. **Start Eclipse with a new workspace**:
   - Launch Eclipse IDE for RCP and RAP Developers (2025-12).
   - When prompted for a workspace, pick a **new, empty directory** (e.g. `~/eclipse-workspaces/geocraft`). Do **not** pick the cloned `geocraft/` directory itself — Eclipse workspace metadata should live outside the source tree.
   - Close the Welcome tab once Eclipse opens.
2. **Import all projects into the workspace**:
   - `File > Import... > Maven > Existing Maven Projects`, click **Next**.
   - Set **Root Directory** to the cloned `geocraft/` directory.
   - Eclipse scans and lists every `pom.xml`. Leave all modules checked and click **Finish**.
   - Wait for the initial Maven/M2E import and workspace build to finish (check the Progress view — this can take several minutes on first import).
   - Expected result: all `org.geocraft.*` projects appear in the Package Explorer. At this point most projects will show compile errors because the target platform is not yet set — that is normal.
3. **Configure the target platform**:
   - In Package Explorer, expand `org.geocraft.target` and double-click `org.geocraft.target.target` to open the Target Editor.
   - Click **Reload** (top right of the editor) and wait for all p2 locations to resolve. Resolution requires network access to `download.eclipse.org`.
   - Once resolution completes with no errors, click **Set as Active Target Platform** (top right).
   - Eclipse will trigger a workspace rebuild. Compile errors should clear. If they don't, run `Project > Clean... > Clean all projects`.
4. **Create a run configuration and launch**:
   - Open `org.geocraft.product/GeoCraft.product`.
   - On the product editor **Overview** tab, click **Synchronize** to align the product's plug-in list with the target platform, then click **Launch an Eclipse application**. This creates a run configuration named after the product and launches GeoCraft.
   - For subsequent launches, use `Run > Run Configurations... > Eclipse Application > GeoCraft.product` (or the Run toolbar dropdown).
   - To tune the launch (VM args, workspace location, included plug-ins), edit the run configuration under `Run > Run Configurations...`.

### Troubleshooting

- **Target platform fails to resolve**: ensure network access to `download.eclipse.org` (the p2 repositories listed in `pom.xml`).
- **Tycho build fails on test bundles**: `testFailureIgnore` is set, but compilation errors still fail the build. Check the failing module's `pom.xml` and MANIFEST.MF.
- **Launch fails with bundle resolution errors**: see `SWT_MIGRATION_LOG.md` for known issues and workarounds.