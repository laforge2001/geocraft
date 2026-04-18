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

1. **Set the target platform**:
   - `File > Import > General > Existing Projects into Workspace` and select `org.geocraft.target`.
   - Open `org.geocraft.target/org.geocraft.target.target`.
   - Wait for it to resolve, then click **Set as Active Target Platform** (top right of the editor).
2. **Import the remaining projects**:
   - `File > Import > Maven > Existing Maven Projects`, select the cloned `geocraft` directory, and import all modules.
3. **Build**: `Project > Build All` (or let autobuild run). Every bundle must compile cleanly before launch.
4. **Run the product**:
   - Open `org.geocraft.product/GeoCraft.product`.
   - Click **Launch an Eclipse application** (green play button) in the product editor overview page.

### Troubleshooting

- **Target platform fails to resolve**: ensure network access to `download.eclipse.org` (the p2 repositories listed in `pom.xml`).
- **Tycho build fails on test bundles**: `testFailureIgnore` is set, but compilation errors still fail the build. Check the failing module's `pom.xml` and MANIFEST.MF.
- **Launch fails with bundle resolution errors**: see `SWT_MIGRATION_LOG.md` for known issues and workarounds.