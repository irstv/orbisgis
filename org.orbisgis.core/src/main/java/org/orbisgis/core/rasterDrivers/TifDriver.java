package org.orbisgis.core.rasterDrivers;

import java.io.File;

import org.gdms.data.DataSourceFactory;
import org.gdms.data.metadata.DefaultMetadata;
import org.gdms.data.metadata.Metadata;
import org.gdms.data.types.TypeDefinition;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.gdms.driver.FileDriver;
import org.gdms.source.SourceManager;

public class TifDriver implements FileDriver {

	public String getName() {
		return "tif with world file driver";
	}

	public int getType() {
		return SourceManager.TFW;
	}

	public void setDataSourceFactory(DataSourceFactory dsf) {
	}

	public void close() throws DriverException {
	}

	public String completeFileName(String fileName) {
		if (!fileName.toLowerCase().endsWith(".tif")) {
			return fileName + ".tif";
		} else {
			return fileName;
		}
	}

	public boolean fileAccepted(File f) {
		return f.getName().toUpperCase().endsWith(".TIF");
	}

	public void open(File file) throws DriverException {
	}

	public Metadata getMetadata() throws DriverException {
		return new DefaultMetadata();
	}

	public TypeDefinition[] getTypesDefinitions() throws DriverException {
		return null;
	}

	public Value getFieldValue(long rowIndex, int fieldId)
			throws DriverException {
		return null;
	}

	public long getRowCount() throws DriverException {
		return 0;
	}

	public Number[] getScope(int dimension) throws DriverException {
		return null;
	}

}
