package org.geocraft.io.ascii.aoi;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.ui.io.DatastoreFileSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The class for handling the selection of ASCII AOI files on disk.
 */
public class AsciiAOIFileSelector extends DatastoreFileSelector implements
		AsciiAOIConstants {

	/**
	 * The default constructor.
	 */
	public AsciiAOIFileSelector() {
		super("ASCII AOI", new String[] { "ASCII AOI Files (.aoi)" },
				new String[] { "*.aoi" }, "LoadAsciiAOI_DIR");
	}

	@Override
	protected MapperModel[] createMapperModelsFromSelectedFiles(
			final File[] files) {
		int numFiles = files.length;
		AsciiAOIMapperModel[] mapperModels = new AsciiAOIMapperModel[numFiles];
		for (int i = 0; i < numFiles; i++) {
			mapperModels[i] = createMapperModel(files[i]);
		}
		return mapperModels;
	}

	/**
	 * Scans the specified ASCII AOI file and returns a mapper model of
	 * datastore properties.
	 * 
	 * @param file
	 *            the ASCII AOI file.
	 * @return the mapper model of datastore properties.
	 */
	public static AsciiAOIMapperModel createMapperModel(final File file) {
		// Create a new mapper model.
		AsciiAOIMapperModel model = new AsciiAOIMapperModel();
		String filePath = file.getAbsolutePath();
		model.setDirectory(FileUtil.getPathName(filePath));
		model.setFileName(FileUtil.getBaseName(filePath));
		model.setAOIType(scanAOIType(filePath));

		// Restore the previously specified settings.
		MapperParameterStore.restore(model);

		return model;
	}

	/**
	 * Scans the specified ASCII AOI file and returns a mapper model of
	 * datastore properties.
	 * 
	 * @param file
	 *            the ASCII AOI file.
	 * @return the mapper model of datastore properties.
	 */
	public static AsciiAOIMapperModel createMapperModelNoRestore(final File file) {
		// Create a new mapper model.
		AsciiAOIMapperModel model = new AsciiAOIMapperModel();
		String filePath = file.getAbsolutePath();
		model.setDirectory(FileUtil.getPathName(filePath));
		model.setFileName(FileUtil.getBaseName(filePath));
		model.setAOIType(scanAOIType(filePath));

		return model;
	}

	private static String scanAOIType(final String filePath) {
		try {
			// Create file input stream.
			File file = new File(filePath);
			FileInputStream istream = new FileInputStream(file);

			// Create a DOM document.
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			docFactory.setValidating(false);
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(istream);

			if (doc == null) {
				throw new Exception(
						"Error: Unable to generate internal XML document.");
			}

			// Look for the AOI node.
			NodeList nodeList = doc.getElementsByTagName(AOI_NODE);
			int nodeCount = nodeList.getLength();
			if (nodeCount > 0) {
				Element node = (Element) nodeList.item(0);
				return node.getAttribute(TYPE_ATTR);
			}
		} catch (Exception ex) {
			return "";
		}
		return "";
	}

}
