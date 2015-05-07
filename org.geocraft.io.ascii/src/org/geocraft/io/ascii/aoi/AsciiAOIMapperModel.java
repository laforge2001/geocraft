package org.geocraft.io.ascii.aoi;

import java.io.File;

import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.osgi.service.prefs.Preferences;

public class AsciiAOIMapperModel extends MapperModel {

	/** The standard file extension. */
	public static final String AOI_FILE_EXTN = ".aoi";

	public static final String DIRECTORY = "Directory";

	public static final String FILE_NAME = "File Name";

	public static final String AOI_TYPE = "AOI Type";

	public static final String HAS_Z_RANGE = "Has Z Range";

	public static final String Z_START = "Z Start";

	public static final String Z_END = "Z End";

	public static final String Z_UNIT = "Z Unit";

	/** The directory in which the ASCII AOI file is located. */
	private final StringProperty _directory;

	/** The name of the ASCII AOI file. */
	private final StringProperty _fileName;

	/** The type of the AOI (e.g. MapPolygonAOI, SeismicSurvey3dAOI, etc). */
	private final StringProperty _aoiType;

	private final BooleanProperty _hasZRange;

	private final FloatProperty _zStart;

	private final FloatProperty _zEnd;

	private EnumProperty<Unit> _zUnit;

	/**
	 * Create a model with default values.
	 */
	public AsciiAOIMapperModel() {
		String directory;
		// Default the directory to the working directory if the Preferences key
		// has not been set yet.
		try {
			directory = PreferencesUtil.getPreferencesStore(
					"org.geocraft.ui.io").get("Workspace_DIR",
					Utilities.getWorkingDirectory());
			_zUnit = addEnumProperty(Z_UNIT, Unit.class, UnitPreferences
					.getInstance().getTimeUnit());
		} catch (NoClassDefFoundError e) {
			directory = Utilities.getWorkingDirectory();
			_zUnit = addEnumProperty(Z_UNIT, Unit.class, Unit.METER);
		}
		_directory = addStringProperty(DIRECTORY, directory);
		_fileName = addStringProperty(FILE_NAME, "");
		_aoiType = addStringProperty(AOI_TYPE, "");
		_hasZRange = addBooleanProperty(HAS_Z_RANGE, false);
		_zStart = addFloatProperty(Z_START, 0);
		_zEnd = addFloatProperty(Z_END, 0);
	}

	public AsciiAOIMapperModel(final AsciiAOIMapperModel model) {
		this();
		updateFrom(model);
	}

	public String getAOIType() {
		return _aoiType.get();
	}

	public void setAOIType(final String aoiType) {
		_aoiType.set(aoiType);
	}

	public String getDirectory() {
		return _directory.get();
	}

	public void setDirectory(final String directory) {
		_directory.set(directory);
	}

	public String getFileName() {
		return _fileName.get();
	}

	public void setFileName(final String fileName) {
		_fileName.set(fileName);
	}

	public boolean getZRangeFlag() {
		return _hasZRange.get();
	}

	public void setZRangeFlag(final boolean hasZRange) {
		_hasZRange.set(hasZRange);
	}

	public float getZStart() {
		return _zStart.get();
	}

	public void setZStart(final float zStart) {
		_zStart.set(zStart);
	}

	public float getZEnd() {
		return _zEnd.get();
	}

	public void setZEnd(final float zEnd) {
		_zEnd.set(zEnd);
	}

	public Unit getZUnit() {
		return _zUnit.get();
	}

	public void setZUnit(final Unit zUnit) {
		_zUnit.set(zUnit);
	}

	@Override
	public String getUniqueId() {
		return getDirectory() + File.separator + getFileName()
				+ getFileExtension();
	}

	protected String getFileExtension() {
		return AOI_FILE_EXTN;
	}

	@Override
	public void updateUniqueId(final String name) {
		setFileName(name);
	}

	@Override
	public boolean isRestoreable(final Preferences prefs) {
		if (!new File(prefs.get(UNIQUE_ID, "")).canRead()) {
			ServiceProvider
					.getLoggingService()
					.getLogger(getClass())
					.warn(prefs.name()
							+ "'s uniqueId was inconsistent with the saved file location. Skipping...");
			return false;
		}

		return true;
	}

	public void validate(final IValidation results) {
		if (_directory.isEmpty()) {
			results.error(AsciiAOIMapperModel.DIRECTORY,
					"Directory must be specified.");
		} else {
			File dir = new File(_directory.get());
			if (!dir.exists()) {
				results.error(AsciiAOIMapperModel.DIRECTORY,
						"Directory does not exist.");
			}
			if (!dir.isDirectory()) {
				results.error(AsciiAOIMapperModel.DIRECTORY,
						"Directory is not a directory.");
			}
			if (!dir.canRead()) {
				results.error(AsciiAOIMapperModel.DIRECTORY,
						"Directory is not readable.");
			}
			if (!dir.canWrite()) {
				results.error(AsciiAOIMapperModel.DIRECTORY,
						"Directory is not writable.");
			}
		}

		if (_fileName.isEmpty()) {
			results.error(AsciiAOIMapperModel.FILE_NAME,
					"File name must be defined.");
		}

		if (_aoiType.isEmpty()) {
			results.error(AsciiAOIMapperModel.AOI_TYPE,
					"No AOI type specified.");
		}

		if (_hasZRange.get()) {
			if (_zEnd.get() < _zStart.get()) {
				results.warning(Z_END, "Z End < Z Start.");
			}
			if (_zUnit.isNull()) {
				results.error(Z_UNIT, "Z Unit not specified.");
			}
		}

		IOMode ioMode = getIOMode();
		if (ioMode.equals(IOMode.OUTPUT)) {
			String fullPath = _directory.get() + File.separator
					+ _fileName.get() + getFileExtension();
			if (new File(fullPath).exists()) {
				results.error(AsciiAOIMapperModel.FILE_NAME, "AOI named '"
						+ fullPath + "' exists and cannot be overwritten");
			}
		}
	}

	@Override
	public boolean existsInStore() {
		return existsOnDisk(getFilePath());
	}

	@Override
	public boolean existsInStore(final String name) {
		return existsOnDisk(_directory.get() + File.separator + name
				+ getFileExtension());
	}

	public String getFilePath() {
		return _directory.get() + File.separator + _fileName.get()
				+ getFileExtension();
	}

	/**
	 * Returns <i>true</i> if the ASCII AOI file exists on disk; <i>false</i> if
	 * not.
	 * 
	 * @param filePath
	 *            the full file path of the ASCII AOI file to check.
	 * @return <i>true</i> if the ASCII AOI file exists on disk; <i>false</i> if
	 *         not.
	 */
	private boolean existsOnDisk(final String filePath) {
		File file = new File(filePath);
		return file.exists() && file.canRead();
	}

}
