/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-1012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.sif;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * This class handles the panel used to import the content of a folder in the geocatalog
 * @author Alexis Guéganno, Jean-Yves Martin
 */
public class OpenFolderPanel extends AbstractOpenPanel {

	public static final String FIELD_NAME = "folder";

	public static final String FILTER_NAME = "filter";
	
	public OpenFolderPanel(String id, String title) {
		super(id, title);
	}

	public FileFilter getSelectedFilter(){
		return getFileChooser().getFileFilter();
	}

	/**
	 * This method validates the input selected in the panel. it returns a message
	 * when a problem has been encountered, and null otherwise.
	 * @return
	 */
	@Override
	public String validateInput() {
		File file = getSelectedFile();
		if (file == null) {
			return i18n.tr("sif.folderMustBeSelected");
		} else if (!file.exists()) {
			return i18n.tr("sif.folderMustExist");
		} else if(!file.isDirectory()){
			return i18n.tr("sif.folderMustBeDirectory");
		} else {
			return null;
		}
	}

	@Override
	public boolean showFoldersOnly(){
		return true;
	}

	/**
	 * Return the names of the fields in the FileChooser.
	 * @return
	 */
	@Override
	public String[] getFieldNames() {
		return new String[] { FIELD_NAME, FILTER_NAME };
	}

	@Override
	public int[] getFieldTypes() {
		return new int[] { SQLUIPanel.STRING, SQLUIPanel.STRING };
	}

	@Override
	public void setValue(String fieldName, String fieldValue) {
		if (fieldName.equals(FIELD_NAME)) {
			String[] files = fieldValue.split("\\Q||\\E");
			File[] selectedFiles = new File[files.length];
			for (int i = 0; i < selectedFiles.length; i++) {
				selectedFiles[i] = new File(files[i]);
			}
			getFileChooser().setSelectedFiles(selectedFiles);
		} else {
			FileFilter[] filters = getFileChooser().getChoosableFileFilters();
			for (FileFilter fileFilter : filters) {
				if (fieldValue.equals(fileFilter.getDescription())) {
					getFileChooser().setFileFilter(fileFilter);
				}
			}
		}
	}

}
